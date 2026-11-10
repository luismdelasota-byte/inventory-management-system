import { api, unwrap } from './api'
import type { Product, ProductInput } from '../types'

export async function getProducts(): Promise<Product[]> {
  return unwrap<Product[]>(api.get<unknown>('/products'))
}

export async function createProduct(product: ProductInput): Promise<Product> {
  const { initialStock, ...fields } = product
  return unwrap<Product>(api.post<unknown>('/products', { ...fields, categoryId: Number(fields.categoryId), initialStock }))
}

export async function updateProduct(id: string, product: Omit<ProductInput, 'initialStock'>): Promise<Product> {
  const { sku, name, description, categoryId, purchasePrice, salePrice, minimumStock } = product
  return unwrap<Product>(api.put<unknown>(`/products/${id}`, {
    sku,
    name,
    description,
    categoryId: Number(categoryId),
    purchasePrice,
    salePrice,
    minimumStock,
  }))
}

export async function toggleProductStatus(id: string): Promise<Product> {
  return unwrap<Product>(api.patch<unknown>(`/products/${id}/status`))
}
