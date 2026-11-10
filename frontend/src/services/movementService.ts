import { api, unwrap } from './api'
import type { Movement, MovementType } from '../types'

export async function getMovements(): Promise<Movement[]> {
  return unwrap<Movement[]>(api.get<unknown>('/movements'))
}

export async function createMovement(
  type: MovementType,
  reason: string,
  quantities: Record<string, number>,
): Promise<Movement> {
  const items = Object.entries(quantities).map(([productId, quantity]) => ({ productId: Number(productId), quantity }))
  const endpoint = type === 'IN' ? '/movements/in' : '/movements/out'
  return unwrap<Movement>(api.post<unknown>(endpoint, { reason, items }))
}

export async function getMovement(id: string): Promise<Movement> {
  return unwrap<Movement>(api.get<unknown>(`/movements/${id}`))
}
