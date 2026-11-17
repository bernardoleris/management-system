import { useEffect, useId, useRef, useState } from 'react'
import type { KeyboardEvent } from 'react'
import './ThemedSelect.css'

export interface SelectOption {
  value: string
  label: string
  disabled?: boolean
}

interface ThemedSelectProps {
  value: string
  options: SelectOption[]
  onChange: (value: string) => void
  label?: string
  id?: string
  className?: string
  disabled?: boolean
}

export function ThemedSelect({ value, options, onChange, label, id, className, disabled = false }: ThemedSelectProps) {
  const [open, setOpen] = useState(false)
  const selectedIndex = options.findIndex((option) => option.value === value)
  const [activeIndex, setActiveIndex] = useState(Math.max(selectedIndex, 0))
  const generatedId = useId()
  const listId = `${generatedId}-options`
  const rootRef = useRef<HTMLDivElement>(null)
  const triggerRef = useRef<HTMLButtonElement>(null)
  const optionRefs = useRef<Array<HTMLButtonElement | null>>([])
  const selectedOption = options[selectedIndex]

  useEffect(() => {
    if (open) optionRefs.current[activeIndex]?.focus()
  }, [activeIndex, open])

  useEffect(() => {
    if (!open) return
    const closeOnOutsidePointer = (event: PointerEvent) => {
      if (!rootRef.current?.contains(event.target as Node)) setOpen(false)
    }
    document.addEventListener('pointerdown', closeOnOutsidePointer)
    return () => document.removeEventListener('pointerdown', closeOnOutsidePointer)
  }, [open])

  function openAt(index: number) {
    const enabledIndex = options[index]?.disabled ? options.findIndex((option) => !option.disabled) : index
    setActiveIndex(Math.max(enabledIndex, 0))
    setOpen(true)
  }

  function moveActive(direction: -1 | 1) {
    setActiveIndex((current) => {
      let next = current
      for (let attempts = 0; attempts < options.length; attempts += 1) {
        next = (next + direction + options.length) % options.length
        if (!options[next].disabled) return next
      }
      return current
    })
  }

  function choose(index: number) {
    const option = options[index]
    if (!option || option.disabled) return
    onChange(option.value)
    setActiveIndex(index)
    setOpen(false)
    triggerRef.current?.focus()
  }

  function handleTriggerKeyDown(event: KeyboardEvent<HTMLButtonElement>) {
    if (event.key === 'ArrowDown' || event.key === 'ArrowUp') {
      event.preventDefault()
      openAt(selectedIndex >= 0 ? selectedIndex : 0)
    }
  }

  function handleOptionKeyDown(event: KeyboardEvent<HTMLButtonElement>) {
    switch (event.key) {
      case 'ArrowDown':
        event.preventDefault()
        moveActive(1)
        break
      case 'ArrowUp':
        event.preventDefault()
        moveActive(-1)
        break
      case 'Home':
        event.preventDefault()
        openAt(0)
        break
      case 'End':
        event.preventDefault()
        openAt(options.length - 1)
        break
      case 'Enter':
      case ' ':
        event.preventDefault()
        choose(activeIndex)
        break
      case 'Escape':
        event.preventDefault()
        setOpen(false)
        triggerRef.current?.focus()
        break
      case 'Tab':
        setOpen(false)
        break
    }
  }

  return <div className="themed-select-wrap" ref={rootRef}>
    <button
      ref={triggerRef}
      id={id}
      className={`themed-select-trigger${className ? ` ${className}` : ''}`}
      type="button"
      disabled={disabled || options.length === 0}
      aria-label={label ? `${label}: ${selectedOption?.label ?? ''}` : undefined}
      aria-haspopup="listbox"
      aria-expanded={open}
      aria-controls={listId}
      onClick={() => open ? setOpen(false) : openAt(selectedIndex >= 0 ? selectedIndex : 0)}
      onKeyDown={handleTriggerKeyDown}
    >
      <span>{selectedOption?.label ?? ''}</span>
      <span className="themed-select-chevron" aria-hidden="true">⌄</span>
    </button>
    {open && <ul id={listId} className="themed-select-options" role="listbox" aria-label={label}>
      {options.map((option, index) => <li key={option.value} role="presentation">
        <button
          ref={(element) => { optionRefs.current[index] = element }}
          type="button"
          role="option"
          aria-selected={option.value === value}
          tabIndex={activeIndex === index ? 0 : -1}
          disabled={option.disabled}
          onFocus={() => setActiveIndex(index)}
          onClick={() => choose(index)}
          onKeyDown={handleOptionKeyDown}
        >{option.label}</button>
      </li>)}
    </ul>}
  </div>
}
