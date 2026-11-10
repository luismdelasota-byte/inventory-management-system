import { createContext } from 'react'
import type { Category, Movement, MovementType, Product, ProductInput, User } from '../types'
import type { DashboardSummary } from '../services/dashboardService'
import type { InventoryItem } from '../services/inventoryService'

export interface InventoryContextValue {
  products: Product[]
  categories: Category[]
  movements: Movement[]
  users: User[]
  inventory: InventoryItem[]
  dashboard: DashboardSummary | null
  isLoading: boolean
  loadError: string
  refresh: () => Promise<void>
  saveProduct: (input: ProductInput, id?: string) => Promise<void>
  saveCategory: (input: Omit<Category, 'id' | 'status'>, id?: string) => Promise<void>
  saveUser: (input: Omit<User, 'id' | 'createdAt' | 'status'> & { password?: string }, id?: string) => Promise<void>
  toggleStatus: (kind: 'product' | 'category' | 'user', id: string) => Promise<void>
  recordMovement: (type: MovementType, reason: string, quantities: Record<string, number>) => Promise<void>
}

export const InventoryContextInstance = createContext<InventoryContextValue | null>(null)
