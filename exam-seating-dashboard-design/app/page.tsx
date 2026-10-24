import { CalendarDays, ChevronRight, Clock } from 'lucide-react'
import { DashboardShell } from '@/components/dashboard/dashboard-shell'
import { SummaryCards } from '@/components/dashboard/summary-cards'
import { SeatingMap } from '@/components/dashboard/seating-map'

export default function Page() {
  return (
    <DashboardShell>
      <div className="flex flex-col gap-8">
        <div className="flex flex-col gap-4 md:flex-row md:items-end md:justify-between">
          <div>
            <nav aria-label="Breadcrumb" className="flex items-center gap-1.5 text-xs text-muted-foreground">
              <span>Exams</span>
              <ChevronRight className="size-3" aria-hidden="true" />
              <span>Spring 2026 Finals</span>
              <ChevronRight className="size-3" aria-hidden="true" />
              <span className="font-medium text-foreground">Seating Map</span>
            </nav>
            <h1 className="mt-2 text-2xl font-semibold tracking-tight text-balance md:text-[28px]">
              Visual Seating Map
            </h1>
            <p className="mt-1 text-sm text-muted-foreground text-pretty">
              Inspect, rearrange and resolve seat allocations across every active exam hall.
            </p>
          </div>
          <div className="flex flex-wrap items-center gap-2">
            <span className="inline-flex h-9 items-center gap-2 rounded-lg border border-border bg-card px-3 text-[13px] font-medium shadow-[0_1px_2px_rgba(16,24,40,0.04)]">
              <CalendarDays className="size-4 text-muted-foreground" aria-hidden="true" />
              Tue, 10 Mar 2026
            </span>
            <span className="inline-flex h-9 items-center gap-2 rounded-lg border border-border bg-card px-3 text-[13px] font-medium shadow-[0_1px_2px_rgba(16,24,40,0.04)]">
              <Clock className="size-4 text-muted-foreground" aria-hidden="true" />
              Morning · 09:00 – 12:00
            </span>
            <span className="inline-flex h-9 items-center gap-2 rounded-lg bg-success-soft px-3 text-[13px] font-medium text-success">
              <span className="relative flex size-2">
                <span className="absolute inline-flex size-full animate-ping rounded-full bg-success opacity-60" />
                <span className="relative inline-flex size-2 rounded-full bg-success" />
              </span>
              Live session
            </span>
          </div>
        </div>

        <SummaryCards />
        <SeatingMap />
      </div>
    </DashboardShell>
  )
}
