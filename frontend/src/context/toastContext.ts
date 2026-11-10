import { createContext } from 'react'

export type ToastKind = 'success' | 'error'

export const toastContext = createContext<(message: string, kind?: ToastKind) => void>(() => undefined)
