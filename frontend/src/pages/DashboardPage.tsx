import { ArrowDownLeft, ArrowRight, ArrowUpRight, Boxes, CircleAlert, ClipboardList, Package, TrendingUp } from 'lucide-react'
import { Link } from 'react-router-dom'
import { useInventory } from '../hooks/useInventory'
import { Badge } from '../components/common/Badge'

const money = (value: number) => new Intl.NumberFormat('es-ES', { style: 'currency', currency: 'USD', maximumFractionDigits: 0 }).format(value)

export function DashboardPage() {
  const { products, dashboard } = useInventory()
  if (!dashboard) return <div className="empty-state">No hay información disponible.</div>

  const recent = dashboard.recentMovements.slice(0, 4)
  const newest = [...products].filter((product) => product.status === 'ACTIVE').sort((a, b) => b.createdAt.localeCompare(a.createdAt)).slice(0, 4)
  const inventoryValue = products.filter((product) => product.status === 'ACTIVE').reduce((sum, product) => sum + product.stock * product.purchasePrice, 0)
  const metrics = [
    { label: 'Total de productos', value: dashboard.totalProducts, detail: 'Productos activos', icon: Package, tone: 'icon-blue' },
    { label: 'Categorías', value: dashboard.totalCategories, detail: 'Categorías activas', icon: Boxes, tone: 'icon-violet' },
    { label: 'Stock bajo', value: dashboard.lowStockProducts, detail: 'En el mínimo o por debajo', icon: CircleAlert, tone: 'icon-amber' },
    { label: 'Agotados', value: dashboard.outOfStockProducts, detail: 'Requieren reposición', icon: TrendingUp, tone: 'icon-red' },
  ]
  return <div className="page-stack">
    <div className="page-heading"><div><span className="eyebrow">RESUMEN</span><h1>Panel de control</h1><p>Este es el estado actual de tu inventario.</p></div><Link className="button button-primary" to="/movements"><ArrowRight size={16} /> Registrar movimiento</Link></div>
    <div className="metric-grid">{metrics.map(({ label, value, detail, icon: Icon, tone }) => <article className="metric-card" key={label}><div className={`metric-icon ${tone}`}><Icon size={20} /></div><div className="metric-label">{label}</div><div className="metric-value">{value}</div><div className="metric-detail">{detail}</div></article>)}</div>
    <section className="summary-bar"><div className="summary-title"><span className="summary-icon"><ClipboardList size={18} /></span><div><strong>Resumen de movimientos</strong><small>{dashboard.totalMovements} movimientos registrados en total</small></div></div><div className="summary-stat"><ArrowDownLeft size={17} className="text-green" /><div><small>Entradas este mes</small><strong>{dashboard.entriesThisMonth}</strong></div></div><div className="summary-stat"><ArrowUpRight size={17} className="text-red" /><div><small>Salidas este mes</small><strong>{dashboard.exitsThisMonth}</strong></div></div><div className="summary-stat"><div><small>Valor del inventario</small><strong>{money(inventoryValue)}</strong></div></div></section>
    <div className="dashboard-columns">
      <section className="panel"><div className="panel-heading"><div><h2>Movimientos recientes</h2><p>Última actividad del inventario</p></div><Link to="/movements" className="text-link">Ver todos <ArrowRight size={15} /></Link></div>
        {recent.length ? <div className="activity-list">{recent.map((movement) => <div className="activity-row" key={movement.id}><span className={`movement-symbol ${movement.type === 'IN' ? 'symbol-in' : 'symbol-out'}`}>{movement.type === 'IN' ? <ArrowDownLeft size={17} /> : <ArrowUpRight size={17} />}</span><div className="activity-main"><strong>{movement.reason}</strong><span>{movement.code} · {movement.userName}</span></div><div className="activity-meta"><Badge tone={movement.type === 'IN' ? 'success' : 'blue'}>{movement.type === 'IN' ? 'Entrada' : 'Salida'}</Badge><span>{new Date(movement.date).toLocaleDateString('es-ES')}</span></div></div>)}</div> : <div className="empty-state">Aún no hay movimientos registrados.</div>}
      </section>
      <section className="panel"><div className="panel-heading"><div><h2>Requieren atención</h2><p>Productos con stock bajo o agotado</p></div><Link to="/inventory" className="text-link">Inventario <ArrowRight size={15} /></Link></div>
        {dashboard.lowStockItems.length ? <div className="stock-list">{dashboard.lowStockItems.slice(0, 5).map((product) => <div className="stock-row" key={product.id}><div className="product-initial">{product.name.charAt(0)}</div><div className="stock-product"><strong>{product.name}</strong><span>{product.sku}</span></div><div className="stock-quantity"><strong>{product.stock}</strong><span>mín. {product.minimumStock}</span></div><Badge tone={product.stock === 0 ? 'danger' : 'warning'}>{product.stock === 0 ? 'Agotado' : 'Stock bajo'}</Badge></div>)}</div> : <div className="empty-state">Todos los productos activos tienen existencias suficientes.</div>}
      </section>
      <section className="panel panel-full"><div className="panel-heading"><div><h2>Productos agregados recientemente</h2><p>Artículos nuevos en el catálogo</p></div><Link to="/products" className="text-link">Ver productos <ArrowRight size={15} /></Link></div>
        <div className="recent-products">{newest.map((product) => <div className="recent-product" key={product.id}><div className="product-initial">{product.name.charAt(0)}</div><div><strong>{product.name}</strong><span>{product.sku}</span></div><div className="recent-price"><strong>{money(product.salePrice)}</strong><span>Precio de venta</span></div><Badge tone={product.stock === 0 ? 'danger' : product.stock <= product.minimumStock ? 'warning' : 'success'}>{product.stock === 0 ? 'Agotado' : product.stock <= product.minimumStock ? 'Stock bajo' : 'Disponible'}</Badge></div>)}</div>
      </section>
    </div>
    <p className="data-note">La información de este panel procede de la base de datos.</p>
  </div>
}
