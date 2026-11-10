import { useContext } from 'react'
import { InventoryContextInstance } from '../context/inventoryContextStore'

export function useInventory() {
  const context = useContext(InventoryContextInstance)
  if (!context) throw new Error('useInventory debe utilizarse dentro de InventoryProvider')
  return context
}
