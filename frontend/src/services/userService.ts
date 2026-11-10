import { api, unwrap } from './api'
import type { User } from '../types'

export async function getUsers(): Promise<User[]> {
  return unwrap<User[]>(api.get<unknown>('/users'))
}

export async function createUser(user: Omit<User, 'id' | 'createdAt' | 'status'> & { password: string }): Promise<User> {
  return unwrap<User>(api.post<unknown>('/users', user))
}

export async function updateUser(id: string, user: Omit<User, 'id' | 'createdAt' | 'status'> & { password?: string }): Promise<User> {
  return unwrap<User>(api.put<unknown>(`/users/${id}`, user))
}

export async function toggleUserStatus(id: string): Promise<User> {
  return unwrap<User>(api.patch<unknown>(`/users/${id}/status`))
}
