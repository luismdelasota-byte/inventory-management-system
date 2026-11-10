import { useMemo, useState } from 'react'
import { Eye, Pencil, Plus, Search, ToggleLeft, ToggleRight } from 'lucide-react'
import { useForm } from 'react-hook-form'
import { zodResolver } from '@hookform/resolvers/zod'
import { z } from 'zod'
import { useInventory } from '../hooks/useInventory'
import { useAuth } from '../hooks/useAuth'
import { useToast } from '../hooks/useToast'
import { Badge } from '../components/common/Badge'
import { Modal } from '../components/common/Modal'
import type { Product } from '../types'

const productSchema = z.object({
  sku: z.string().trim().min(1, 'El SKU es obligatorio.'),
  name: z.string().trim().min(1, 'El nombre del producto es obligatorio.'),
  description: z.string(),
  categoryId: z.string().min(1, 'Selecciona una categoría.'),
  purchasePrice: z.coerce.number().min(0, 'El precio debe ser igual o mayor que cero.'),
  salePrice: z.coerce.number().min(0, 'El precio debe ser igual o mayor que cero.'),
  initialStock: z.coerce.number().min(0, 'El stock debe ser igual o mayor que cero.'),
  minimumStock: z.coerce.number().min(0, 'El stock mínimo debe ser igual o mayor que cero.'),
})
type ProductValues = z.infer<typeof productSchema>
const currency = (amount: number) => new Intl.NumberFormat('es-ES', { style: 'currency', currency: 'USD' }).format(amount)

function ProductForm({ editing, onCancel, onSaved }: { editing: Product | null; onCancel: () => void; onSaved: () => void }) {
  const { products, categories, saveProduct } = useInventory()
  const toast = useToast()
  const [saveError, setSaveError] = useState('')
  const { register, handleSubmit, formState: { errors, isSubmitting } } = useForm<z.input<typeof productSchema>, unknown, ProductValues>({
    resolver: zodResolver(productSchema),
    defaultValues: editing ? { ...editing, initialStock: 0 } : { sku: '', name: '', description: '', categoryId: '', purchasePrice: 0, salePrice: 0, initialStock: 0, minimumStock: 0 },
  })
  const onSubmit = async (values: ProductValues) => {
    setSaveError('')
    if (products.some((product) => product.sku.toLowerCase() === values.sku.toLowerCase() && product.id !== editing?.id)) {
      setSaveError('Ese SKU ya está en uso.')
      return
    }
    const { initialStock, ...data } = values
    try {
      await saveProduct({ ...data, initialStock }, editing?.id)
      onSaved()
      toast(editing ? 'Producto actualizado.' : 'Producto creado.')
    } catch (error) {
      setSaveError(error instanceof Error ? error.message : 'No se pudo guardar el producto.')
    }
  }
  return <form className="form-grid" onSubmit={handleSubmit(onSubmit)} noValidate>
    <label className="form-field">SKU<input {...register('sku')} placeholder="Ej.: LAP-001" />{errors.sku && <small className="field-error">{errors.sku.message}</small>}</label>
    <label className="form-field">Nombre del producto<input {...register('name')} placeholder="Nombre del producto" />{errors.name && <small className="field-error">{errors.name.message}</small>}</label>
    <label className="form-field">Categoría<select {...register('categoryId')}><option value="">Selecciona una categoría</option>{categories.filter((category) => category.status === 'ACTIVE').map((category) => <option value={category.id} key={category.id}>{category.name}</option>)}</select>{errors.categoryId && <small className="field-error">{errors.categoryId.message}</small>}</label>
    <label className="form-field">Precio de compra ($)<input type="number" min="0" step="0.01" {...register('purchasePrice')} />{errors.purchasePrice && <small className="field-error">{errors.purchasePrice.message}</small>}</label>
    <label className="form-field">Precio de venta ($)<input type="number" min="0" step="0.01" {...register('salePrice')} />{errors.salePrice && <small className="field-error">{errors.salePrice.message}</small>}</label>
    {!editing && <label className="form-field">Stock inicial<input type="number" min="0" {...register('initialStock')} />{errors.initialStock && <small className="field-error">{errors.initialStock.message}</small>}</label>}
    <label className="form-field">Stock mínimo<input type="number" min="0" {...register('minimumStock')} />{errors.minimumStock && <small className="field-error">{errors.minimumStock.message}</small>}</label>
    <label className="form-field field-full">Descripción<textarea rows={3} {...register('description')} placeholder="Descripción opcional del producto" /></label>
    {editing && <div className="info-box field-full">Stock actual: {editing.stock}. Para modificarlo, registra un movimiento de inventario.</div>}
    {saveError && <div className="form-alert field-full" role="alert">{saveError}</div>}
    <div className="modal-actions field-full"><button type="button" className="button button-outline" onClick={onCancel} disabled={isSubmitting}>Cancelar</button><button className="button button-primary" disabled={isSubmitting}>{isSubmitting ? 'Guardando...' : editing ? 'Guardar cambios' : 'Crear producto'}</button></div>
  </form>
}

export function ProductsPage() {
  const { products, categories, toggleStatus } = useInventory()
  const { user } = useAuth()
  const canManage = user?.role === 'ADMIN'
  const toast = useToast()
  const [query, setQuery] = useState('')
  const [categoryFilter, setCategoryFilter] = useState('all')
  const [statusFilter, setStatusFilter] = useState('all')
  const [editing, setEditing] = useState<Product | null>(null)
  const [viewing, setViewing] = useState<Product | null>(null)
  const [showForm, setShowForm] = useState(false)
  const filtered = useMemo(() => products.filter((product) =>
    `${product.sku} ${product.name}`.toLowerCase().includes(query.toLowerCase()) &&
    (categoryFilter === 'all' || product.categoryId === categoryFilter) &&
    (statusFilter === 'all' || product.status === statusFilter),
  ), [products, query, categoryFilter, statusFilter])

  const openNew = () => { setEditing(null); setShowForm(true) }
  const openEdit = (product: Product) => { setEditing(product); setShowForm(true) }
  const categoryName = (id: string) => categories.find((category) => category.id === id)?.name ?? 'Sin categoría'
  const changeStatus = async (product: Product) => {
    try {
      await toggleStatus('product', product.id)
      toast('Estado del producto actualizado.')
    } catch (error) {
      toast(error instanceof Error ? error.message : 'No se pudo actualizar el estado del producto.', 'error')
    }
  }
  return <div className="page-stack">
    <div className="page-heading"><div><span className="eyebrow">CATÁLOGO</span><h1>Productos</h1><p>Administra el catálogo, los precios y los niveles mínimos de stock.</p></div>{canManage && <button className="button button-primary" onClick={openNew}><Plus size={17} /> Agregar producto</button>}</div>
    <section className="panel table-panel">
      <div className="toolbar"><div className="search-box"><Search size={17} /><input value={query} onChange={(event) => setQuery(event.target.value)} placeholder="Buscar por SKU o producto..." /></div><select value={categoryFilter} onChange={(event) => setCategoryFilter(event.target.value)}><option value="all">Todas las categorías</option>{categories.map((category) => <option key={category.id} value={category.id}>{category.name}</option>)}</select><select value={statusFilter} onChange={(event) => setStatusFilter(event.target.value)}><option value="all">Todos los estados</option><option value="ACTIVE">Activo</option><option value="INACTIVE">Inactivo</option></select><span className="result-count">{filtered.length} productos</span></div>
      <div className="table-scroll"><table><thead><tr><th>SKU / PRODUCTO</th><th>CATEGORÍA</th><th>COMPRA / VENTA</th><th>STOCK</th><th>ESTADO</th>{canManage && <th className="actions-cell">ACCIONES</th>}</tr></thead><tbody>
        {filtered.map((product) => <tr key={product.id}><td><div className="table-primary">{product.name}</div><div className="table-secondary">{product.sku}</div></td><td>{categoryName(product.categoryId)}</td><td><div className="table-primary">{currency(product.purchasePrice)}</div><div className="table-secondary">Venta {currency(product.salePrice)}</div></td><td><div className="table-primary">{product.stock} unidades</div><div className="table-secondary">Mínimo {product.minimumStock}</div></td><td><Badge tone={product.status === 'ACTIVE' ? 'success' : 'neutral'}>{product.status === 'ACTIVE' ? 'Activo' : 'Inactivo'}</Badge></td>{canManage && <td className="actions-cell"><button className="icon-button" title="Ver detalles" onClick={() => setViewing(product)}><Eye size={16} /></button><button className="icon-button" title="Editar producto" onClick={() => openEdit(product)}><Pencil size={16} /></button><button className="icon-button" title={product.status === 'ACTIVE' ? 'Desactivar producto' : 'Activar producto'} onClick={() => { if (window.confirm(`¿${product.status === 'ACTIVE' ? 'Desactivar' : 'Activar'} ${product.name}?`)) void changeStatus(product) }}>{product.status === 'ACTIVE' ? <ToggleRight size={17} /> : <ToggleLeft size={17} />}</button></td>}</tr>)}
        {!filtered.length && <tr><td colSpan={canManage ? 6 : 5} className="empty-cell">No hay productos que coincidan con los filtros.</td></tr>}
      </tbody></table></div>
    </section>
    {showForm && <Modal title={editing ? 'Editar producto' : 'Agregar producto'} onClose={() => { setShowForm(false); setEditing(null) }} wide><ProductForm editing={editing} onCancel={() => { setShowForm(false); setEditing(null) }} onSaved={() => { setShowForm(false); setEditing(null) }} /></Modal>}
    {viewing && <Modal title="Detalles del producto" onClose={() => setViewing(null)}><div className="detail-list"><div><span>Producto</span><strong>{viewing.name}</strong></div><div><span>SKU</span><strong>{viewing.sku}</strong></div><div><span>Categoría</span><strong>{categoryName(viewing.categoryId)}</strong></div><div><span>Compra / venta</span><strong>{currency(viewing.purchasePrice)} / {currency(viewing.salePrice)}</strong></div><div><span>Stock actual</span><strong>{viewing.stock} unidades</strong></div><div><span>Stock mínimo</span><strong>{viewing.minimumStock} unidades</strong></div><div><span>Descripción</span><strong>{viewing.description || '—'}</strong></div></div></Modal>}
  </div>
}
