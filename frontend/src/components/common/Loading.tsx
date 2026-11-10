export function Loading({ message = 'Cargando...' }: { message?: string }) {
  return <div className="loading-state" role="status"><span className="spinner" />{message}</div>
}
