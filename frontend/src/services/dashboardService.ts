import { api, unwrap } from './api'
import type { Movement } from '../types'

export interface DashboardSummary {
  totalProducts: number
  totalCategories: number
  lowStockProducts: number
  outOfStockProducts: number
  totalMovements: number
  entriesThisMonth: number
  exitsThisMonth: number
  recentMovements: Movement[]
  lowStockItems: {
    id: string
    sku: string
    name: string
    categoryName: string
    stock: number
    minimumStock: number
  }[]
}

export async function getDashboardSummary(): Promise<DashboardSummary> {
  return unwrap<DashboardSummary>(api.get<unknown>('/dashboard/summary'))
}
