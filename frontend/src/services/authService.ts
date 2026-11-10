import { api, setAccessToken, unwrap } from './api'
import type { User } from '../types'

interface LoginResponse {
  token: string
  type: string
  user: Omit<User, 'createdAt'>
}

export async function login(identifier: string, password: string): Promise<User> {
  const result = await unwrap<LoginResponse>(api.post<unknown>('/auth/login', { identifier, password }))
  setAccessToken(result.token)
  return { ...result.user, createdAt: '' }
}

export async function getCurrentUser(): Promise<User> {
  return unwrap<User>(api.get<unknown>('/auth/me'))
}

export function clearSession() {
  setAccessToken(null)
}
