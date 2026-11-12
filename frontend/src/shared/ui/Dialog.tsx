import { useEffect, type HTMLAttributes, type MouseEvent, type ReactNode } from 'react'
import styles from './Dialog.module.css'

type DialogRole = 'dialog' | 'alertdialog'
type DialogVariant = 'default' | 'details' | 'confirmation'

interface DialogProps {
  labelledBy: string
  describedBy?: string
  children: ReactNode
  onClose: () => void
  role?: DialogRole
  variant?: DialogVariant
  className?: string
  closeOnBackdrop?: boolean
  closeOnEscape?: boolean
}

interface DialogPartProps extends HTMLAttributes<HTMLElement> {
  children: ReactNode
}

function cx(...classNames: Array<string | undefined>) {
  return classNames.filter(Boolean).join(' ')
}

export function Dialog({
  labelledBy,
  describedBy,
  children,
  onClose,
  role = 'dialog',
  variant = 'default',
  className,
  closeOnBackdrop = true,
  closeOnEscape = true,
}: DialogProps) {
  useEffect(() => {
    if (!closeOnEscape) return
    const handleKeyDown = (event: KeyboardEvent) => {
      if (event.key === 'Escape') onClose()
    }
    window.addEventListener('keydown', handleKeyDown)
    return () => window.removeEventListener('keydown', handleKeyDown)
  }, [closeOnEscape, onClose])

  function handleBackdropMouseDown(event: MouseEvent<HTMLDivElement>) {
    if (closeOnBackdrop && event.target === event.currentTarget) onClose()
  }

  return (
    <div className={styles.backdrop} onMouseDown={handleBackdropMouseDown}>
      <section
        className={cx(styles.panel, styles[variant], className)}
        role={role}
        aria-modal="true"
        aria-labelledby={labelledBy}
        aria-describedby={describedBy}
        onMouseDown={(event) => event.stopPropagation()}
      >
        {children}
      </section>
    </div>
  )
}

export function DialogHeader({ className, children, ...props }: DialogPartProps) {
  return <header className={cx(styles.header, className)} {...props}>{children}</header>
}

export function DialogFooter({ className, children, ...props }: DialogPartProps) {
  return <footer className={cx(styles.footer, className)} {...props}>{children}</footer>
}

export function DialogConfirmationIcon({ className, children, ...props }: DialogPartProps) {
  return <span className={cx(styles.confirmationIcon, className)} aria-hidden="true" {...props}>{children}</span>
}
