import { api, unwrap } from './api'
export interface InventoryItem {
  id: string
  sku: string
  name: string
  categoryName: string
  stock: number
  minimumStock: number
  inventoryStatus: 'AVAILABLE' | 'LOW_STOCK' | 'OUT_OF_STOCK'
}

export async function getInventory(): Promise<InventoryItem[]> {
  return unwrap<InventoryItem[]>(api.get<unknown>('/inventory'))
}

export function getStockState(item: Pick<InventoryItem, 'inventoryStatus'>): 'AVAILABLE' | 'LOW' | 'OUT' {
  if (item.inventoryStatus === 'OUT_OF_STOCK') return 'OUT'
  return item.inventoryStatus === 'LOW_STOCK' ? 'LOW' : 'AVAILABLE'
}

export function getInventoryUnitCount(items: InventoryItem[]): number {
  return items.reduce((total, item) => total + item.stock, 0)
}
