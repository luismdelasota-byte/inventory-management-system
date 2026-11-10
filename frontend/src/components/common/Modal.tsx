import { X } from 'lucide-react'
import type { PropsWithChildren } from 'react'

export function Modal({ title, onClose, children, wide = false }: PropsWithChildren<{ title: string; onClose: () => void; wide?: boolean }>) {
  return (
    <div className="modal-backdrop" onMouseDown={(event) => { if (event.target === event.currentTarget) onClose() }}>
      <section className={`modal ${wide ? 'modal-wide' : ''}`} role="dialog" aria-modal="true" aria-label={title}>
        <header className="modal-header"><h2>{title}</h2><button className="icon-button" aria-label="Cerrar ventana" onClick={onClose}><X size={19} /></button></header>
        {children}
      </section>
    </div>
  )
}
