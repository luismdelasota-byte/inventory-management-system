export type Role = 'ADMIN' | 'INVENTORY_USER'
export type RecordStatus = 'ACTIVE' | 'INACTIVE'
export type MovementType = 'IN' | 'OUT'

export interface User {
  id: string
  name: string
  username: string
  email: string
  role: Role
  status: RecordStatus
  createdAt: string
}

export interface Category {
  id: string
  name: string
  description: string
  status: RecordStatus
}

export interface Product {
  id: string
  sku: string
  name: string
  description: string
  categoryId: string
  purchasePrice: number
  salePrice: number
  stock: number
  minimumStock: number
  status: RecordStatus
  createdAt: string
}

export interface MovementLine {
  productId: string
  productName: string
  quantity: number
  stockBefore: number
  stockAfter: number
}

export interface Movement {
  id: string
  code: string
  type: MovementType
  date: string
  userName: string
  reason: string
  lines: MovementLine[]
}

export interface ProductInput extends Omit<Product, 'id' | 'stock' | 'status' | 'createdAt'> {
  initialStock: number
}
