import { useState } from 'react'
import { ArrowDownLeft, ArrowUpRight, Eye, Plus, Trash2 } from 'lucide-react'
import { useInventory } from '../hooks/useInventory'
import { useToast } from '../hooks/useToast'
import { Badge } from '../components/common/Badge'
import { Modal } from '../components/common/Modal'
import type { Movement, MovementType } from '../types'

interface LineInput { productId: string; quantity: number }

export function MovementsPage() {
  const { products, movements, recordMovement } = useInventory()
  const toast = useToast()
  const [showForm, setShowForm] = useState(false)
  const [type, setType] = useState<MovementType>('IN')
  const [reason, setReason] = useState('')
  const [lines, setLines] = useState<LineInput[]>([{ productId: '', quantity: 1 }])
  const [error, setError] = useState('')
  const [detail, setDetail] = useState<Movement | null>(null)

  const openForm = () => { setType('IN'); setReason(''); setLines([{ productId: '', quantity: 1 }]); setError(''); setShowForm(true) }
  const updateLine = (index: number, patch: Partial<LineInput>) => setLines((current) => current.map((line, position) => position === index ? { ...line, ...patch } : line))
  const selectedIds = lines.map((line) => line.productId).filter(Boolean)
  const selectedLines = lines.map((line) => ({ ...line, product: products.find((product) => product.id === line.productId) })).filter((line) => line.product)
  const [isSubmitting, setIsSubmitting] = useState(false)
  const submit = async () => {
    if (isSubmitting) return
    setError('')
    if (!reason.trim()) { setError('El motivo es obligatorio.'); return }
    if (lines.some((line) => !line.productId || !Number.isInteger(Number(line.quantity)) || Number(line.quantity) <= 0)) { setError('Selecciona un producto e introduce una cantidad entera positiva en cada línea.'); return }
    setIsSubmitting(true)
    try {
      await recordMovement(type, reason, Object.fromEntries(lines.map((line) => [line.productId, Number(line.quantity)])))
      setShowForm(false)
      toast(`Se registró la ${type === 'IN' ? 'entrada' : 'salida'} de inventario.`)
    } catch (issue) { setError(issue instanceof Error ? issue.message : 'No se pudo registrar el movimiento.') }
    finally { setIsSubmitting(false) }
  }
  return <div className="page-stack"><div className="page-heading"><div><span className="eyebrow">INVENTARIO</span><h1>Movimientos</h1><p>Registra entradas y salidas de stock, y consulta el historial.</p></div><button className="button button-primary" onClick={openForm}><Plus size={17} /> Registrar movimiento</button></div>
    <section className="panel table-panel"><div className="panel-heading panel-heading-inline"><div><h2>Historial de movimientos</h2><p>{movements.length} movimientos registrados</p></div></div><div className="table-scroll"><table><thead><tr><th>MOVIMIENTO</th><th>TIPO</th><th>FECHA</th><th>RESPONSABLE</th><th>MOTIVO</th><th>PRODUCTOS</th><th className="actions-cell">DETALLES</th></tr></thead><tbody>{[...movements].sort((a, b) => b.date.localeCompare(a.date)).map((movement) => <tr key={movement.id}><td><div className="table-primary">{movement.code}</div></td><td><Badge tone={movement.type === 'IN' ? 'success' : 'blue'}>{movement.type === 'IN' ? 'Entrada' : 'Salida'}</Badge></td><td>{new Date(movement.date).toLocaleString('es-ES')}</td><td>{movement.userName}</td><td>{movement.reason}</td><td>{movement.lines.length} {movement.lines.length === 1 ? 'artículo' : 'artículos'}</td><td className="actions-cell"><button className="icon-button" aria-label={`Ver detalles de ${movement.code}`} onClick={() => setDetail(movement)}><Eye size={16} /></button></td></tr>)}</tbody></table></div>{!movements.length && <div className="empty-state">Aún no hay movimientos registrados.</div>}</section>
    {showForm && <Modal title="Registrar movimiento de inventario" onClose={() => setShowForm(false)} wide><div className="movement-form">
      <div className="movement-type-select"><button className={type === 'IN' ? 'selected-in' : ''} onClick={() => setType('IN')}><ArrowDownLeft size={18} /> Entrada</button><button className={type === 'OUT' ? 'selected-out' : ''} onClick={() => setType('OUT')}><ArrowUpRight size={18} /> Salida</button></div>
      <label className="form-field">Motivo<input value={reason} onChange={(event) => setReason(event.target.value)} placeholder="Ej.: Entrega del proveedor o asignación a oficina" /></label>
      <div className="line-heading"><strong>Productos y cantidades</strong><span>Selecciona los productos activos y la cantidad</span></div>
      <div className="movement-lines">{lines.map((line, index) => {
        const selected = products.find((product) => product.id === line.productId)
        const available = products.filter((product) => product.status === 'ACTIVE' && (!selectedIds.includes(product.id) || product.id === line.productId))
        return <div className="movement-line" key={index}><select aria-label={`Producto de la línea ${index + 1}`} value={line.productId} onChange={(event) => updateLine(index, { productId: event.target.value })}><option value="">Selecciona un producto</option>{available.map((product) => <option key={product.id} value={product.id}>{product.name} · {product.sku}</option>)}</select><input type="number" min="1" step="1" aria-label="Cantidad" value={line.quantity} onChange={(event) => updateLine(index, { quantity: Number(event.target.value) })} /><span className="available-stock">Disponible: <strong>{selected?.stock ?? '—'}</strong></span><button className="icon-button danger-action" aria-label="Eliminar artículo" disabled={lines.length === 1} onClick={() => setLines((current) => current.filter((_, position) => position !== index))}><Trash2 size={16} /></button></div>
      })}</div>
      <button className="text-link add-line" onClick={() => setLines((current) => [...current, { productId: '', quantity: 1 }])}><Plus size={15} /> Agregar otro producto</button>
      {selectedLines.length > 0 && <div className="preview-box"><strong>Vista previa del cambio de stock</strong>{selectedLines.map((line) => { const quantity = Number(line.quantity) || 0; const result = line.product!.stock + (type === 'IN' ? quantity : -quantity); return <div key={line.productId}><span>{line.product!.name}</span><strong>{line.product!.stock} → {result} unidades</strong></div> })}</div>}
      {error && <div className="form-alert" role="alert">{error}</div>}
      <div className="info-box">El movimiento se guardará en el servidor y actualizará las existencias del inventario.</div>
      <div className="modal-actions"><button className="button button-outline" onClick={() => setShowForm(false)} disabled={isSubmitting}>Cancelar</button><button className="button button-primary" onClick={() => void submit()} disabled={isSubmitting}>{isSubmitting ? 'Guardando...' : 'Confirmar movimiento'}</button></div>
    </div></Modal>}
    {detail && <Modal title={`Movimiento ${detail.code}`} onClose={() => setDetail(null)}><div className="detail-list"><div><span>Tipo</span><strong>{detail.type === 'IN' ? 'Entrada' : 'Salida'}</strong></div><div><span>Fecha</span><strong>{new Date(detail.date).toLocaleString('es-ES')}</strong></div><div><span>Responsable</span><strong>{detail.userName}</strong></div><div><span>Motivo</span><strong>{detail.reason}</strong></div></div><h3 className="detail-subheading">Cambio de stock</h3><div className="table-scroll"><table><thead><tr><th>PRODUCTO</th><th>CANT.</th><th>ANTERIOR</th><th>RESULTANTE</th></tr></thead><tbody>{detail.lines.map((line) => <tr key={line.productId}><td>{line.productName}</td><td>{line.quantity}</td><td>{line.stockBefore}</td><td>{line.stockAfter}</td></tr>)}</tbody></table></div></Modal>}
  </div>
}
