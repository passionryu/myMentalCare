'use client'

import { ReactNode, useEffect } from 'react'
import { X } from 'lucide-react'

type AdminDetailModalProps = {
  eyebrow: string
  title: string
  children: ReactNode
  onClose: () => void
  size?: 'default' | 'wide'
}

export default function AdminDetailModal({ eyebrow, title, children, onClose, size = 'default' }: AdminDetailModalProps) {
  useEffect(() => {
    const previousOverflow = document.body.style.overflow

    document.body.style.overflow = 'hidden'

    function handleKeyDown(event: KeyboardEvent) {
      if (event.key === 'Escape') {
        onClose()
      }
    }

    window.addEventListener('keydown', handleKeyDown)

    return () => {
      document.body.style.overflow = previousOverflow
      window.removeEventListener('keydown', handleKeyDown)
    }
  }, [onClose])

  return (
    <div className="admin-modal-backdrop" role="presentation" onMouseDown={onClose}>
      <section
        className={`admin-detail-modal admin-detail-modal-${size}`}
        role="dialog"
        aria-modal="true"
        aria-labelledby="admin-detail-modal-title"
        onMouseDown={(event) => event.stopPropagation()}
      >
        <header className="admin-detail-modal-header">
          <div>
            <span className="admin-eyebrow">{eyebrow}</span>
            <h2 id="admin-detail-modal-title">{title}</h2>
          </div>
          <button type="button" className="admin-modal-close-button" onClick={onClose} aria-label={`${title} 닫기`}>
            <X aria-hidden="true" />
          </button>
        </header>
        <div className="admin-detail-modal-body">{children}</div>
      </section>
    </div>
  )
}
