import axios, { type AxiosResponse } from 'axios'

export interface ApiResponse<T> {
  success: boolean
  message: string
  data: T
}

function isApiResponse<T>(value: unknown): value is ApiResponse<T> {
  return typeof value === 'object'
    && value !== null
    && 'success' in value
    && typeof value.success === 'boolean'
    && 'message' in value
    && typeof value.message === 'string'
    && 'data' in value
}

const TOKEN_KEY = 'inventory-access-token'

export const api = axios.create({
  baseURL: import.meta.env.VITE_API_URL ?? 'http://localhost:8080/api',
  headers: { 'Content-Type': 'application/json' },
})

api.interceptors.request.use((config) => {
  const token = sessionStorage.getItem(TOKEN_KEY)
  if (token) config.headers.Authorization = `Bearer ${token}`
  return config
})

api.interceptors.response.use(
  (response) => response,
  (error: unknown) => {
    if (axios.isAxiosError<ApiResponse<unknown>>(error)) {
      if (error.response?.status === 401) {
        sessionStorage.removeItem(TOKEN_KEY)
        window.dispatchEvent(new Event('inventory:unauthorized'))
      }
      const message = error.response?.data?.message
      return Promise.reject(new Error(message || error.message || 'No se pudo completar la solicitud.'))
    }
    return Promise.reject(error)
  },
)

export function setAccessToken(token: string | null) {
  if (token) {
    sessionStorage.setItem(TOKEN_KEY, token)
    return
  }
  sessionStorage.removeItem(TOKEN_KEY)
  window.dispatchEvent(new Event('inventory:logout'))
}

export async function unwrap<T>(request: Promise<AxiosResponse<unknown>>): Promise<T> {
  const response = await request
  const body: unknown = response.data
  if (!isApiResponse<T>(body)) {
    throw new Error('La respuesta del servidor no tiene el formato esperado.')
  }
  if (!body.success) throw new Error(body.message || 'El servidor no pudo completar la solicitud.')
  return body.data
}

export { TOKEN_KEY }
