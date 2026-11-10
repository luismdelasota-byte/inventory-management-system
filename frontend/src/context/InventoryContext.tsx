import { useCallback, useEffect, useMemo, useRef, useState, type PropsWithChildren } from 'react'
import { useAuth } from '../hooks/useAuth'
import { getCategories, createCategory, updateCategory, toggleCategoryStatus } from '../services/categoryService'
import { getDashboardSummary } from '../services/dashboardService'
import { getInventory } from '../services/inventoryService'
import { getMovements, createMovement } from '../services/movementService'
import { getProducts, createProduct, updateProduct, toggleProductStatus } from '../services/productService'
import { getUsers, createUser, updateUser, toggleUserStatus } from '../services/userService'
import type { Category, MovementType, ProductInput, User } from '../types'
import { InventoryContextInstance, type InventoryContextValue } from './inventoryContextStore'

export function InventoryProvider({ children }: PropsWithChildren) {
  const { user } = useAuth()
  const requestVersion = useRef(0)
  const [products, setProducts] = useState<InventoryContextValue['products']>([])
  const [categories, setCategories] = useState<InventoryContextValue['categories']>([])
  const [movements, setMovements] = useState<InventoryContextValue['movements']>([])
  const [users, setUsers] = useState<InventoryContextValue['users']>([])
  const [inventory, setInventory] = useState<InventoryContextValue['inventory']>([])
  const [dashboard, setDashboard] = useState<InventoryContextValue['dashboard']>(null)
  const [loadState, setLoadState] = useState<{ userId: string; error: string } | null>(null)
  const isLoading = Boolean(user && loadState?.userId !== user.id)
  const loadError = user && loadState?.userId === user.id ? loadState.error : ''

  const loadData = useCallback(async () => {
    if (!user) return null
    const [nextProducts, nextCategories, nextInventory, nextMovements, nextDashboard, nextUsers] = await Promise.all([
      getProducts(),
      getCategories(),
      getInventory(),
      getMovements(),
      getDashboardSummary(),
      user.role === 'ADMIN' ? getUsers() : Promise.resolve([] as User[]),
    ])
    return { products: nextProducts, categories: nextCategories, inventory: nextInventory, movements: nextMovements, dashboard: nextDashboard, users: nextUsers }
  }, [user])

  const applyData = useCallback((nextData: NonNullable<Awaited<ReturnType<typeof loadData>>>) => {
    setProducts(nextData.products)
    setCategories(nextData.categories)
    setInventory(nextData.inventory)
    setMovements(nextData.movements)
    setDashboard(nextData.dashboard)
    setUsers(nextData.users)
  }, [])

  const refresh = useCallback(async () => {
    if (!user) return
    const requestId = ++requestVersion.current
    try {
      const nextData = await loadData()
      if (requestVersion.current !== requestId || !nextData) return
      applyData(nextData)
      setLoadState({ userId: user.id, error: '' })
    } catch (error) {
      if (requestVersion.current !== requestId) return
      const message = error instanceof Error ? error.message : 'No se pudieron cargar los datos del servidor.'
      setLoadState({ userId: user.id, error: message })
      throw error
    }
  }, [applyData, loadData, user])

  useEffect(() => {
    const clearData = () => {
      requestVersion.current += 1
      setProducts([])
      setCategories([])
      setInventory([])
      setMovements([])
      setUsers([])
      setDashboard(null)
      setLoadState(null)
    }
    window.addEventListener('inventory:logout', clearData)
    return () => window.removeEventListener('inventory:logout', clearData)
  }, [])

  useEffect(() => {
    if (!user) return
    let active = true
    const requestId = ++requestVersion.current
    loadData()
      .then((nextData) => {
        if (active && requestVersion.current === requestId && nextData) {
          applyData(nextData)
          setLoadState({ userId: user.id, error: '' })
        }
      })
      .catch((error: unknown) => {
        if (active && requestVersion.current === requestId) {
          setLoadState({ userId: user.id, error: error instanceof Error ? error.message : 'No se pudieron cargar los datos del servidor.' })
        }
      })
    return () => {
      active = false
      if (requestVersion.current === requestId) requestVersion.current += 1
    }
  }, [applyData, user, loadData])

  const value = useMemo<InventoryContextValue>(() => ({
    products, categories, movements, users, inventory, dashboard, isLoading, loadError, refresh,
    async saveProduct(input: ProductInput, id?: string) {
      if (id) await updateProduct(id, input)
      else await createProduct(input)
      await refresh()
    },
    async saveCategory(input: Omit<Category, 'id' | 'status'>, id?: string) {
      if (id) await updateCategory(id, input)
      else await createCategory(input)
      await refresh()
    },
    async saveUser(input: Omit<User, 'id' | 'createdAt' | 'status'> & { password?: string }, id?: string) {
      if (id) await updateUser(id, input)
      else {
        if (!input.password) throw new Error('La contraseña es obligatoria para crear una cuenta.')
        await createUser({ ...input, password: input.password })
      }
      await refresh()
    },
    async toggleStatus(kind, id) {
      if (kind === 'product') await toggleProductStatus(id)
      if (kind === 'category') await toggleCategoryStatus(id)
      if (kind === 'user') await toggleUserStatus(id)
      await refresh()
    },
    async recordMovement(type: MovementType, reason: string, quantities: Record<string, number>) {
      await createMovement(type, reason, quantities)
      await refresh()
    },
  }), [products, categories, movements, users, inventory, dashboard, isLoading, loadError, refresh])

  return <InventoryContextInstance.Provider value={value}>{children}</InventoryContextInstance.Provider>
}
