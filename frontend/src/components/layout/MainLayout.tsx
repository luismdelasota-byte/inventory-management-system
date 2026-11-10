import { useState } from 'react'
import { NavLink, Outlet, useLocation } from 'react-router-dom'
import { Boxes, ChartNoAxesCombined, ChevronLeft, ChevronRight, ClipboardList, LayoutDashboard, LogOut, Menu, Package, UserRound, Users, X, Tags, Warehouse } from 'lucide-react'
import { useAuth } from '../../hooks/useAuth'
import { useInventory } from '../../hooks/useInventory'
import { Loading } from '../common/Loading'
import type { Role } from '../../types'

const navItems: { label: string; path: string; icon: typeof LayoutDashboard; group: string; roles: Role[] }[] = [
  { label: 'Panel', path: '/', icon: LayoutDashboard, group: 'GENERAL', roles: ['ADMIN', 'INVENTORY_USER'] },
  { label: 'Productos', path: '/products', icon: Package, group: 'INVENTORY', roles: ['ADMIN', 'INVENTORY_USER'] },
  { label: 'Categorías', path: '/categories', icon: Tags, group: 'INVENTORY', roles: ['ADMIN', 'INVENTORY_USER'] },
  { label: 'Inventario', path: '/inventory', icon: Warehouse, group: 'INVENTORY', roles: ['ADMIN', 'INVENTORY_USER'] },
  { label: 'Movimientos', path: '/movements', icon: ClipboardList, group: 'INVENTORY', roles: ['ADMIN', 'INVENTORY_USER'] },
  { label: 'Usuarios', path: '/users', icon: Users, group: 'ADMINISTRATION', roles: ['ADMIN'] },
  { label: 'Mi perfil', path: '/profile', icon: UserRound, group: 'ADMINISTRATION', roles: ['ADMIN', 'INVENTORY_USER'] },
]

export function MainLayout() {
  const { user, logout } = useAuth()
  const { isLoading, loadError, refresh } = useInventory()
  const [collapsed, setCollapsed] = useState(false)
  const [mobileOpen, setMobileOpen] = useState(false)
  const location = useLocation()
  const pageTitle = navItems.find((item) => item.path === location.pathname)?.label ?? 'Panel'
  const groups = [...new Set(navItems.filter((item) => user && item.roles.includes(user.role)).map((item) => item.group))]

  return <div className={`app-shell ${collapsed ? 'sidebar-collapsed' : ''}`}>
    {mobileOpen && <button className="mobile-scrim" aria-label="Cerrar navegación" onClick={() => setMobileOpen(false)} />}
    <aside className={`sidebar ${mobileOpen ? 'mobile-open' : ''}`}>
      <div className="brand"><div className="brand-mark"><Boxes size={22} /></div><span className="brand-name">Stockwise</span><button className="mobile-close" onClick={() => setMobileOpen(false)} aria-label="Cerrar menú"><X size={18} /></button></div>
      <div className="workspace-label">ESPACIO DE TRABAJO</div>
      <nav className="side-nav">
        {groups.map((group) => <div className="nav-group" key={group}>
          <div className="nav-group-title">{group === 'GENERAL' ? 'GENERAL' : group === 'INVENTORY' ? 'INVENTARIO' : 'ADMINISTRACIÓN'}</div>
          {navItems.filter((item) => item.group === group && user && item.roles.includes(user.role)).map(({ label, path, icon: Icon }) =>
            <NavLink key={path} to={path} end={path === '/'} className={({ isActive }) => `nav-link ${isActive ? 'active' : ''}`} onClick={() => setMobileOpen(false)}>
              <Icon size={18} /><span>{label}</span>
            </NavLink>,
          )}
        </div>)}
      </nav>
      <div className="sidebar-bottom"><div className="sidebar-note"><ChartNoAxesCombined size={17} /><span>Sistema de inventario</span></div><button className="nav-link logout-link" onClick={logout}><LogOut size={18} /><span>Cerrar sesión</span></button></div>
      <button className="collapse-toggle" onClick={() => setCollapsed((state) => !state)} aria-label={collapsed ? 'Expandir menú lateral' : 'Contraer menú lateral'}>{collapsed ? <ChevronRight size={17} /> : <ChevronLeft size={17} />}</button>
    </aside>
    <main className="main-area">
      <header className="topbar"><button className="mobile-menu" onClick={() => setMobileOpen(true)} aria-label="Abrir menú"><Menu size={21} /></button><div className="breadcrumb">Espacio de trabajo <span>/</span> <strong>{pageTitle}</strong></div><div className="topbar-user"><div className="avatar">{user?.name.split(' ').map((part) => part[0]).join('')}</div><div className="topbar-user-copy"><strong>{user?.name}</strong><span>{user?.role === 'ADMIN' ? 'Administrador' : 'Usuario de inventario'}</span></div><button className="topbar-signout" onClick={logout} title="Cerrar sesión"><LogOut size={17} /></button></div></header>
      <div className="page-content">{isLoading ? <Loading message="Cargando información..." /> : loadError ? <section className="load-error"><h2>No se pudo conectar con el servidor</h2><p>{loadError}</p><button className="button button-primary" onClick={() => { void refresh().catch(() => undefined) }}>Reintentar</button></section> : <Outlet />}</div>
      <footer className="app-footer">Inventario Stockwise <span>•</span> Datos sincronizados con el servidor</footer>
    </main>
  </div>
}
