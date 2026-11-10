import { useMemo, useState } from 'react'
import { ChevronLeft, ChevronRight, Search } from 'lucide-react'
import { useInventory } from '../hooks/useInventory'
import { Badge } from '../components/common/Badge'
import { getInventoryUnitCount, getStockState } from '../services/inventoryService'

export function InventoryPage() {
  const { products, inventory, categories } = useInventory()
  const [query, setQuery] = useState('')
  const [category, setCategory] = useState('all')
  const [status, setStatus] = useState('all')
  const [sort, setSort] = useState('name')
  const [page, setPage] = useState(1)
  const pageSize = 6
  const selectedCategoryName = categories.find((item) => item.id === category)?.name
  const activeProductIds = useMemo(() => new Set(products.filter((product) => product.status === 'ACTIVE').map((product) => product.id)), [products])
  const filtered = useMemo(() => inventory.filter((item) => {
    const stockState = getStockState(item)
    return activeProductIds.has(item.id) && `${item.sku} ${item.name}`.toLowerCase().includes(query.toLowerCase()) && (category === 'all' || selectedCategoryName === item.categoryName) && (status === 'all' || status === stockState)
  }).sort((a, b) => sort === 'stock-asc' ? a.stock - b.stock : sort === 'stock-desc' ? b.stock - a.stock : a.name.localeCompare(b.name)), [inventory, activeProductIds, query, selectedCategoryName, category, status, sort])
  const pages = Math.max(1, Math.ceil(filtered.length / pageSize))
  const rows = filtered.slice((page - 1) * pageSize, page * pageSize)
  const statusOf = (item: (typeof inventory)[number]) => {
    const state = getStockState(item)
    return state === 'OUT' ? ['Agotado', 'danger'] as const : state === 'LOW' ? ['Stock bajo', 'warning'] as const : ['Disponible', 'success'] as const
  }
  return <div className="page-stack"><div className="page-heading"><div><span className="eyebrow">INVENTARIO</span><h1>Inventario</h1><p>Consulta las existencias actuales y los niveles de stock.</p></div><div className="inventory-total"><strong>{getInventoryUnitCount(inventory.filter((item) => activeProductIds.has(item.id)))}</strong><span>unidades disponibles</span></div></div>
    <section className="panel table-panel"><div className="toolbar"><div className="search-box"><Search size={17} /><input value={query} onChange={(event) => { setQuery(event.target.value); setPage(1) }} placeholder="Buscar por SKU o producto..." /></div><select value={category} onChange={(event) => { setCategory(event.target.value); setPage(1) }}><option value="all">Todas las categorías</option>{categories.map((item) => <option value={item.id} key={item.id}>{item.name}</option>)}</select><select value={status} onChange={(event) => { setStatus(event.target.value); setPage(1) }}><option value="all">Todos los estados</option><option value="AVAILABLE">Disponible</option><option value="LOW">Stock bajo</option><option value="OUT">Agotado</option></select><select value={sort} onChange={(event) => setSort(event.target.value)}><option value="name">Ordenar: nombre</option><option value="stock-asc">Ordenar: stock ascendente</option><option value="stock-desc">Ordenar: stock descendente</option></select></div>
      <div className="table-scroll"><table><thead><tr><th>SKU / PRODUCTO</th><th>CATEGORÍA</th><th>STOCK ACTUAL</th><th>STOCK MÍNIMO</th><th>ESTADO DEL INVENTARIO</th></tr></thead><tbody>{rows.map((item) => { const [label, tone] = statusOf(item); return <tr key={item.id}><td><div className="table-primary">{item.name}</div><div className="table-secondary">{item.sku}</div></td><td>{item.categoryName}</td><td><strong className={item.stock === 0 ? 'text-red' : ''}>{item.stock}</strong> unidades</td><td>{item.minimumStock} unidades</td><td><Badge tone={tone}>{label}</Badge></td></tr> })}{!rows.length && <tr><td colSpan={5} className="empty-cell">No hay existencias que coincidan con los filtros.</td></tr>}</tbody></table></div>
      <div className="pagination"><span>Mostrando {filtered.length ? (page - 1) * pageSize + 1 : 0}–{Math.min(page * pageSize, filtered.length)} de {filtered.length}</span><div><button className="icon-button" disabled={page <= 1} aria-label="Página anterior" onClick={() => setPage((current) => current - 1)}><ChevronLeft size={17} /></button><span>Página {page} de {pages}</span><button className="icon-button" disabled={page >= pages} aria-label="Página siguiente" onClick={() => setPage((current) => current + 1)}><ChevronRight size={17} /></button></div></div>
    </section>
  </div>
}
