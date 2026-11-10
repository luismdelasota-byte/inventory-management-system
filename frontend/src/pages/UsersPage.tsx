import { useMemo, useState } from 'react'
import { Pencil, Plus, Search, ToggleLeft, ToggleRight } from 'lucide-react'
import { useForm } from 'react-hook-form'
import { zodResolver } from '@hookform/resolvers/zod'
import { z } from 'zod'
import { useAuth } from '../hooks/useAuth'
import { useInventory } from '../hooks/useInventory'
import { useToast } from '../hooks/useToast'
import { Badge } from '../components/common/Badge'
import { Modal } from '../components/common/Modal'
import type { User } from '../types'

const userSchema = z.object({
  name: z.string().trim().min(1, 'El nombre es obligatorio.'),
  username: z.string().trim().min(1, 'El nombre de usuario es obligatorio.'),
  email: z.string().trim().email('Introduce un correo electrónico válido.'),
  role: z.enum(['ADMIN', 'INVENTORY_USER']),
  password: z.string().optional().refine((value) => !value || value.length >= 6, 'La contraseña debe tener al menos 6 caracteres.'),
})
type UserValues = z.infer<typeof userSchema>

function UserForm({ editing, onCancel, onSaved }: { editing: User | null; onCancel: () => void; onSaved: () => void }) {
  const { users, saveUser } = useInventory()
  const toast = useToast()
  const [saveError, setSaveError] = useState('')
  const { register, handleSubmit, formState: { errors, isSubmitting } } = useForm<UserValues>({ resolver: zodResolver(userSchema), defaultValues: editing ?? { name: '', username: '', email: '', role: 'INVENTORY_USER' } })
  const onSubmit = async (values: UserValues) => {
    setSaveError('')
    if (!editing && (!values.password || values.password.length < 6)) { setSaveError('La contraseña debe tener al menos 6 caracteres.'); return }
    if (users.some((item) => item.email.toLowerCase() === values.email.toLowerCase() && item.id !== editing?.id)) { setSaveError('Ya existe un usuario con ese correo electrónico.'); return }
    if (users.some((item) => item.username.toLowerCase() === values.username.toLowerCase() && item.id !== editing?.id)) { setSaveError('Ese nombre de usuario ya está en uso.'); return }
    const { password, ...userDetails } = values
    try {
      await saveUser({ ...userDetails, ...(password ? { password } : {}) }, editing?.id)
      onSaved()
      toast(editing ? 'Usuario actualizado.' : 'Usuario creado.')
    } catch (error) {
      setSaveError(error instanceof Error ? error.message : 'No se pudo guardar el usuario.')
    }
  }
  return <form className="form-grid" onSubmit={handleSubmit(onSubmit)} noValidate><label className="form-field field-full">Nombre completo<input {...register('name')} placeholder="Nombre completo" />{errors.name && <small className="field-error">{errors.name.message}</small>}</label><label className="form-field">Nombre de usuario<input {...register('username')} placeholder="usuario" />{errors.username && <small className="field-error">{errors.username.message}</small>}</label><label className="form-field">Correo electrónico<input type="email" {...register('email')} placeholder="usuario@empresa.com" />{errors.email && <small className="field-error">{errors.email.message}</small>}</label>{!editing && <label className="form-field field-full">Contraseña<input type="password" autoComplete="new-password" {...register('password')} />{errors.password && <small className="field-error">{errors.password.message}</small>}<small className="field-hint">Debe tener al menos 6 caracteres.</small></label>}{editing && <label className="form-field field-full">Nueva contraseña <span className="optional">Opcional</span><input type="password" autoComplete="new-password" {...register('password')} placeholder="Dejar en blanco para mantener la actual" />{errors.password && <small className="field-error">{errors.password.message}</small>}</label>}<label className="form-field field-full">Rol<select {...register('role')}><option value="INVENTORY_USER">Usuario de inventario</option><option value="ADMIN">Administrador</option></select></label>{saveError && <div className="form-alert field-full" role="alert">{saveError}</div>}<div className="modal-actions field-full"><button type="button" className="button button-outline" onClick={onCancel} disabled={isSubmitting}>Cancelar</button><button className="button button-primary" disabled={isSubmitting}>{isSubmitting ? 'Guardando...' : editing ? 'Guardar cambios' : 'Crear usuario'}</button></div></form>
}

export function UsersPage() {
  const { user: currentUser } = useAuth()
  const { users, toggleStatus } = useInventory()
  const toast = useToast()
  const [query, setQuery] = useState('')
  const [role, setRole] = useState('all')
  const [editing, setEditing] = useState<User | null>(null)
  const [showForm, setShowForm] = useState(false)
  const filtered = useMemo(() => users.filter((user) => `${user.name} ${user.username} ${user.email}`.toLowerCase().includes(query.toLowerCase()) && (role === 'all' || user.role === role)), [users, query, role])
  const changeStatus = async (item: User) => {
    try {
      await toggleStatus('user', item.id)
      toast('Estado del usuario actualizado.')
    } catch (error) {
      toast(error instanceof Error ? error.message : 'No se pudo actualizar el estado del usuario.', 'error')
    }
  }
  return <div className="page-stack"><div className="page-heading"><div><span className="eyebrow">ADMINISTRACIÓN</span><h1>Usuarios</h1><p>Administra las cuentas del espacio de trabajo y sus roles de acceso.</p></div><button className="button button-primary" onClick={() => { setEditing(null); setShowForm(true) }}><Plus size={17} /> Agregar usuario</button></div>
    <section className="panel table-panel"><div className="toolbar"><div className="search-box"><Search size={17} /><input value={query} onChange={(event) => setQuery(event.target.value)} placeholder="Buscar usuarios..." /></div><select value={role} onChange={(event) => setRole(event.target.value)}><option value="all">Todos los roles</option><option value="ADMIN">Administrador</option><option value="INVENTORY_USER">Usuario de inventario</option></select><span className="result-count">{filtered.length} usuarios</span></div>
      <div className="table-scroll"><table><thead><tr><th>USUARIO</th><th>NOMBRE DE USUARIO</th><th>ROL</th><th>ESTADO</th><th>FECHA DE CREACIÓN</th><th className="actions-cell">ACCIONES</th></tr></thead><tbody>{filtered.map((item) => <tr key={item.id}><td><div className="table-primary">{item.name}</div><div className="table-secondary">{item.email}</div></td><td>{item.username}</td><td><Badge tone={item.role === 'ADMIN' ? 'blue' : 'neutral'}>{item.role === 'ADMIN' ? 'Administrador' : 'Usuario de inventario'}</Badge></td><td><Badge tone={item.status === 'ACTIVE' ? 'success' : 'neutral'}>{item.status === 'ACTIVE' ? 'Activo' : 'Inactivo'}</Badge></td><td>{new Date(`${item.createdAt}T00:00:00`).toLocaleDateString('es-ES')}</td><td className="actions-cell"><button className="icon-button" title="Editar usuario" onClick={() => { setEditing(item); setShowForm(true) }}><Pencil size={16} /></button><button className="icon-button" disabled={item.id === currentUser?.id} title={item.id === currentUser?.id ? 'No puedes desactivar tu propia cuenta' : item.status === 'ACTIVE' ? 'Desactivar usuario' : 'Activar usuario'} onClick={() => { if (window.confirm(`¿${item.status === 'ACTIVE' ? 'Desactivar' : 'Activar'} a ${item.name}?`)) void changeStatus(item) }}>{item.status === 'ACTIVE' ? <ToggleRight size={17} /> : <ToggleLeft size={17} />}</button></td></tr>)}{!filtered.length && <tr><td colSpan={6} className="empty-cell">No hay usuarios que coincidan con los filtros.</td></tr>}</tbody></table></div>
    </section>
    <div className="role-explainer"><div><Badge tone="blue">ADMIN</Badge><span>Puede administrar productos, categorías, usuarios y movimientos de inventario.</span></div><div><Badge tone="neutral">INVENTORY_USER</Badge><span>Puede consultar el catálogo y el inventario, y registrar entradas y salidas.</span></div></div>
    {showForm && <Modal title={editing ? 'Editar usuario' : 'Agregar usuario'} onClose={() => { setShowForm(false); setEditing(null) }}><UserForm editing={editing} onCancel={() => { setShowForm(false); setEditing(null) }} onSaved={() => { setShowForm(false); setEditing(null) }} /></Modal>}
  </div>
}
