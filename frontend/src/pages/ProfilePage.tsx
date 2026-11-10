import { useAuth } from '../hooks/useAuth'
import { Badge } from '../components/common/Badge'

export function ProfilePage() {
  const { user } = useAuth()
  if (!user) return null
  const details = [['Nombre completo', user.name], ['Nombre de usuario', user.username], ['Correo electrónico', user.email], ['Rol', user.role === 'ADMIN' ? 'Administrador' : 'Usuario de inventario'], ['Estado de la cuenta', user.status === 'ACTIVE' ? 'Activa' : 'Inactiva']]
  return <div className="page-stack"><div className="page-heading"><div><span className="eyebrow">CUENTA</span><h1>Mi perfil</h1><p>Información de tu cuenta y rol de acceso.</p></div></div><section className="panel profile-card"><div className="profile-hero"><div className="avatar avatar-large">{user.name.split(' ').map((part) => part[0]).join('')}</div><div><h2>{user.name}</h2><p>{user.role === 'ADMIN' ? 'Administrador' : 'Usuario de inventario'}</p></div><Badge tone="success">Activa</Badge></div><div className="profile-details">{details.map(([label, value]) => <div key={label}><span>{label}</span><strong>{value}</strong></div>)}</div><div className="info-box">Para cambiar tu contraseña, solicita la actualización a un administrador.</div></section></div>
}
