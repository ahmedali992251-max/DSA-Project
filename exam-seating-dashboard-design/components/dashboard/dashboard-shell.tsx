'use client'

import { useState } from 'react'
import { cn } from '@/lib/utils'
import { Sidebar } from './sidebar'
import { TopHeader } from './top-header'

export function DashboardShell({ children }: { children: React.ReactNode }) {
  const [open, setOpen] = useState(false)

  return (
    <div className="min-h-dvh">
      <aside className="fixed inset-y-0 left-0 z-40 hidden w-64 lg:block">
        <Sidebar />
      </aside>

      <div
        className={cn(
          'fixed inset-0 z-50 bg-navy-deep/50 backdrop-blur-sm transition-opacity lg:hidden',
          open ? 'opacity-100' : 'pointer-events-none opacity-0',
        )}
        onClick={() => setOpen(false)}
        aria-hidden="true"
      />
      <aside
        className={cn(
          'fixed inset-y-0 left-0 z-50 w-72 transition-transform duration-300 lg:hidden',
          open ? 'translate-x-0' : '-translate-x-full',
        )}
        aria-hidden={!open}
        inert={!open}
      >
        <Sidebar onClose={() => setOpen(false)} />
      </aside>

      <div className="lg:pl-64">
        <TopHeader onMenu={() => setOpen(true)} />
        <main className="mx-auto max-w-[1600px] px-4 py-6 md:px-8 md:py-8">{children}</main>
      </div>
    </div>
  )
}
