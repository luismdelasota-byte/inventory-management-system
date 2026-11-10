import { useCallback, useEffect, useMemo, useState, type PropsWithChildren } from 'react'
import { clearSession, getCurrentUser, login as authenticate } from '../services/authService'
import { AuthContextInstance, type AuthContextValue } from './authContextStore'
import type { User } from '../types'

export function AuthProvider({ children }: PropsWithChildren) {
  const [user, setUser] = useState<User | null>(null)
  const [isLoading, setIsLoading] = useState(() => Boolean(sessionStorage.getItem('inventory-access-token')))

  const logout = useCallback(() => {
    clearSession()
    setUser(null)
  }, [])

  useEffect(() => {
    let active = true
    const handleUnauthorized = () => logout()
    window.addEventListener('inventory:unauthorized', handleUnauthorized)
    if (!sessionStorage.getItem('inventory-access-token')) {
      return () => {
        active = false
        window.removeEventListener('inventory:unauthorized', handleUnauthorized)
      }
    }

    getCurrentUser()
      .then((currentUser) => { if (active) setUser(currentUser) })
      .catch(() => { if (active) logout() })
      .finally(() => { if (active) setIsLoading(false) })

    return () => {
      active = false
      window.removeEventListener('inventory:unauthorized', handleUnauthorized)
    }
  }, [logout])

  const value = useMemo<AuthContextValue>(() => ({
    user,
    isLoading,
    async login(identifier, password) {
      const currentUser = await authenticate(identifier, password)
      setUser(currentUser)
    },
    logout,
  }), [user, isLoading, logout])

  return <AuthContextInstance.Provider value={value}>{children}</AuthContextInstance.Provider>
}
