import { useContext } from 'react'
import { AuthContextInstance } from '../context/authContextStore'

export function useAuth() {
  const context = useContext(AuthContextInstance)
  if (!context) throw new Error('useAuth must be used within AuthProvider')
  return context
}
