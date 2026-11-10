import { useContext } from 'react'
import { toastContext } from '../context/toastContext'

export function useToast() {
  return useContext(toastContext)
}
