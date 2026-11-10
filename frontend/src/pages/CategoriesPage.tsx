import { useMemo, useState } from 'react'
import { Pencil, Plus, Search, ToggleLeft, ToggleRight } from 'lucide-react'
import { useForm } from 'react-hook-form'
import { zodResolver } from '@hookform/resolvers/zod'
import { z } from 'zod'
import { useInventory } from '../hooks/useInventory'
import { useAuth } from '../hooks/useAuth'
import { useToast } from '../hooks/useToast'
import { Badge } from '../components/common/Badge'
import { Modal } from '../components/common/Modal'
import type { Category } from '../types'

const schema = z.object({ name: z.string().trim().min(1, 'El nombre de la categoría es obligatorio.'), description: z.string() })
type CategoryValues = z.infer<typeof schema>

function CategoryForm({ editing, onCancel, onSaved }: { editing: Category | null; onCancel: () => void; onSaved: () => void }) {
  const { categories, saveCategory } = useInventory()
  const toast = useToast()
  const [saveError, setSaveError] = useState('')
  const { register, handleSubmit, formState: { errors, isSubmitting } } = useForm<CategoryValues>({ resolver: zodResolver(schema), defaultValues: editing ?? { name: '', description: '' } })
  const onSubmit = async (values: CategoryValues) => {
    setSaveError('')
    if (categories.some((category) => category.name.toLowerCase() === values.name.toLowerCase() && category.id !== editing?.id)) { setSaveError('Ya existe una categoría con ese nombre.'); return }
    try {
      await saveCategory(values, editing?.id)
      onSaved()
      toast(editing ? 'Categoría actualizada.' : 'Categoría creada.')
    } catch (error) {
      setSaveError(error instanceof Error ? error.message : 'No se pudo guardar la categoría.')
    }
  }
  return <form className="form-grid" onSubmit={handleSubmit(onSubmit)} noValidate><label className="form-field field-full">Nombre de la categoría<input {...register('name')} placeholder="Ej.: Suministros de oficina" />{errors.name && <small className="field-error">{errors.name.message}</small>}</label><label className="form-field field-full">Descripción <span className="optional">Opcional</span><textarea rows={3} {...register('description')} /></label>{saveError && <div className="form-alert field-full" role="alert">{saveError}</div>}<div className="modal-actions field-full"><button type="button" className="button button-outline" onClick={onCancel} disabled={isSubmitting}>Cancelar</button><button className="button button-primary" disabled={isSubmitting}>{isSubmitting ? 'Guardando...' : editing ? 'Guardar cambios' : 'Crear categoría'}</button></div></form>
}

export function CategoriesPage() {
  const { categories, products, toggleStatus } = useInventory()
  const { user } = useAuth()
  const canManage = user?.role === 'ADMIN'
  const toast = useToast()
  const [query, setQuery] = useState('')
  const [editing, setEditing] = useState<Category | null>(null)
  const [showForm, setShowForm] = useState(false)
  const filtered = useMemo(() => categories.filter((category) => `${category.name} ${category.description}`.toLowerCase().includes(query.toLowerCase())), [categories, query])
  const changeStatus = async (category: Category) => {
    try {
      await toggleStatus('category', category.id)
      toast('Estado de la categoría actualizado.')
    } catch (error) {
      toast(error instanceof Error ? error.message : 'No se pudo actualizar el estado de la categoría.', 'error')
    }
  }
  return <div className="page-stack"><div className="page-heading"><div><span className="eyebrow">CATÁLOGO</span><h1>Categorías</h1><p>Organiza los productos en grupos claros y fáciles de administrar.</p></div>{canManage && <button className="button button-primary" onClick={() => { setEditing(null); setShowForm(true) }}><Plus size={17} /> Agregar categoría</button>}</div>
    <section className="panel table-panel"><div className="toolbar"><div className="search-box"><Search size={17} /><input value={query} onChange={(event) => setQuery(event.target.value)} placeholder="Buscar categorías..." /></div><span className="result-count">{filtered.length} categorías</span></div>
      <div className="table-scroll"><table><thead><tr><th>CATEGORÍA</th><th>DESCRIPCIÓN</th><th>PRODUCTOS</th><th>ESTADO</th>{canManage && <th className="actions-cell">ACCIONES</th>}</tr></thead><tbody>{filtered.map((category) => <tr key={category.id}><td><div className="table-primary">{category.name}</div></td><td className="description-cell">{category.description || '—'}</td><td>{products.filter((product) => product.categoryId === category.id).length}</td><td><Badge tone={category.status === 'ACTIVE' ? 'success' : 'neutral'}>{category.status === 'ACTIVE' ? 'Activa' : 'Inactiva'}</Badge></td>{canManage && <td className="actions-cell"><button className="icon-button" title="Editar categoría" onClick={() => { setEditing(category); setShowForm(true) }}><Pencil size={16} /></button><button className="icon-button" title={category.status === 'ACTIVE' ? 'Desactivar categoría' : 'Activar categoría'} onClick={() => { if (window.confirm(`¿${category.status === 'ACTIVE' ? 'Desactivar' : 'Activar'} ${category.name}?`)) void changeStatus(category) }}>{category.status === 'ACTIVE' ? <ToggleRight size={17} /> : <ToggleLeft size={17} />}</button></td>}</tr>)}{!filtered.length && <tr><td colSpan={canManage ? 5 : 4} className="empty-cell">No se encontraron categorías.</td></tr>}</tbody></table></div>
    </section>{showForm && <Modal title={editing ? 'Editar categoría' : 'Agregar categoría'} onClose={() => { setShowForm(false); setEditing(null) }}><CategoryForm editing={editing} onCancel={() => { setShowForm(false); setEditing(null) }} onSaved={() => { setShowForm(false); setEditing(null) }} /></Modal>}
  </div>
}
