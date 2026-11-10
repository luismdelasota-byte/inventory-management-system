import { api, unwrap } from './api'
import type { Category } from '../types'

export async function getCategories(): Promise<Category[]> {
  return unwrap<Category[]>(api.get<unknown>('/categories'))
}

export async function createCategory(category: Omit<Category, 'id' | 'status'>): Promise<Category> {
  return unwrap<Category>(api.post<unknown>('/categories', category))
}

export async function updateCategory(id: string, category: Omit<Category, 'id' | 'status'>): Promise<Category> {
  return unwrap<Category>(api.put<unknown>(`/categories/${id}`, category))
}

export async function toggleCategoryStatus(id: string): Promise<Category> {
  return unwrap<Category>(api.patch<unknown>(`/categories/${id}/status`))
}
