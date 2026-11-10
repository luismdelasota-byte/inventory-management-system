import { useCallback, useMemo, useState, type PropsWithChildren } from 'react'
import { AlertCircle, CheckCircle2, X } from 'lucide-react'
import { toastContext } from '../../context/toastContext'
import type { ToastKind } from '../../context/toastContext'

export function ToastProvider({ children }: PropsWithChildren) {
  const [notification, setNotification] = useState<{ message: string; kind: ToastKind } | null>(null)
  const toast = useCallback((message: string, kind: ToastKind = 'success') => {
    setNotification({ message, kind })
    window.setTimeout(() => setNotification((current) => current?.message === message ? null : current), 3200)
  }, [])
  const value = useMemo(() => toast, [toast])
  return <toastContext.Provider value={value}>{children}{notification && <div className={`toast ${notification.kind === 'error' ? 'toast-error' : ''}`} role={notification.kind === 'error' ? 'alert' : 'status'}>{notification.kind === 'error' ? <AlertCircle size={18} /> : <CheckCircle2 size={18} />}<span>{notification.message}</span><button aria-label="Cerrar notificación" onClick={() => setNotification(null)}><X size={16} /></button></div>}</toastContext.Provider>
}
