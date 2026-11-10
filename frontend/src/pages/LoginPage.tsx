import { useState } from 'react'
import { Boxes, Eye, EyeOff, LockKeyhole, Mail } from 'lucide-react'
import { useForm } from 'react-hook-form'
import { zodResolver } from '@hookform/resolvers/zod'
import { z } from 'zod'
import { Navigate, useNavigate } from 'react-router-dom'
import { useAuth } from '../hooks/useAuth'
import { Loading } from '../components/common/Loading'

const schema = z.object({ email: z.string().trim().min(1, 'El correo o nombre de usuario es obligatorio.'), password: z.string().min(1, 'La contraseña es obligatoria.') })
type LoginValues = z.infer<typeof schema>

export function LoginPage() {
  const { user, login, isLoading } = useAuth()
  const navigate = useNavigate()
  const [visible, setVisible] = useState(false)
  const [submitError, setSubmitError] = useState('')
  const { register, handleSubmit, formState: { errors, isSubmitting } } = useForm<LoginValues>({ resolver: zodResolver(schema) })
  if (isLoading) return <Loading message="Verificando sesión..." />
  if (user) return <Navigate to="/" replace />

  const onSubmit = async (values: LoginValues) => {
    setSubmitError('')
    try { await login(values.email, values.password); navigate('/', { replace: true }) }
    catch (error) { setSubmitError(error instanceof Error ? error.message : 'No se pudo iniciar sesión.') }
  }

  return <main className="login-page"><section className="login-card">
    <div className="login-brand"><span className="brand-mark"><Boxes size={23} /></span><span>Stockwise</span></div>
    <div className="login-heading"><span className="eyebrow">GESTIÓN DE INVENTARIO</span><h1>Te damos la bienvenida</h1><p>Inicia sesión para administrar el inventario de tu empresa.</p></div>
    <form className="login-form" onSubmit={handleSubmit(onSubmit)} noValidate>
      <label>Correo electrónico o nombre de usuario</label><div className={`input-icon ${errors.email ? 'input-error' : ''}`}><Mail size={17} /><input type="text" placeholder="tu@empresa.com" autoComplete="username" {...register('email')} /></div>{errors.email && <small className="field-error">{errors.email.message}</small>}
      <label>Contraseña</label><div className={`input-icon ${errors.password ? 'input-error' : ''}`}><LockKeyhole size={17} /><input type={visible ? 'text' : 'password'} placeholder="Introduce tu contraseña" autoComplete="current-password" {...register('password')} /><button type="button" className="password-toggle" aria-label={visible ? 'Ocultar contraseña' : 'Mostrar contraseña'} onClick={() => setVisible((value) => !value)}>{visible ? <EyeOff size={17} /> : <Eye size={17} />}</button></div>{errors.password && <small className="field-error">{errors.password.message}</small>}
      {submitError && <div className="form-alert">{submitError}</div>}
      <button className="button button-primary login-submit" disabled={isSubmitting}>{isSubmitting ? <><span className="spinner" /> Iniciando sesión...</> : 'Iniciar sesión'}</button>
    </form>
    <div className="info-box login-info">Accede con la cuenta que te haya proporcionado el administrador de tu sistema.</div>
    <p className="login-disclaimer">Tus datos se administran de forma segura en el servidor.</p>
  </section></main>
}
