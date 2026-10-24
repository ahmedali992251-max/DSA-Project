import { Armchair, Printer, TriangleAlert, UserRound } from 'lucide-react'
import { cn } from '@/lib/utils'
import type { Room, Seat } from '@/lib/seating-data'

type Stats = { assigned: number; conflict: number; empty: number; capacity: number; occupancy: number }

const STATUS_STYLE = {
  assigned: 'bg-success-soft text-success',
  empty: 'bg-muted text-muted-foreground',
  conflict: 'bg-conflict-soft text-conflict',
}

export function SeatDetailsPanel({
  room,
  stats,
  seat,
  onSelect,
}: {
  room: Room
  stats: Stats
  seat: Seat | null
  onSelect: (id: string) => void
}) {
  const conflicts = room.seats.filter((s) => s.status === 'conflict')
  const circumference = 2 * Math.PI * 34
  const assignedArc = (stats.assigned / stats.capacity) * circumference
  const conflictArc = (stats.conflict / stats.capacity) * circumference

  return (
    <aside className="flex flex-col gap-6 md:grid md:grid-cols-2 2xl:flex 2xl:flex-col" aria-label="Seat details">
      <div className="rounded-xl border border-border/80 bg-card p-5 shadow-[0_1px_2px_rgba(16,24,40,0.04),0_8px_24px_-12px_rgba(16,24,40,0.08)]">
        <div className="flex items-center justify-between">
          <h3 className="text-sm font-semibold">Selected Seat</h3>
          {seat && (
            <span className={cn('rounded-full px-2 py-0.5 text-[11px] font-medium capitalize', STATUS_STYLE[seat.status])}>
              {seat.status}
            </span>
          )}
        </div>

        {seat ? (
          <div className="mt-4">
            <div className="flex items-center gap-3">
              <div
                className={cn(
                  'flex size-12 items-center justify-center rounded-xl font-mono text-sm font-semibold',
                  STATUS_STYLE[seat.status],
                )}
              >
                {seat.id}
              </div>
              <div className="min-w-0">
                <p className="truncate text-sm font-semibold">{seat.student?.name ?? 'Unassigned desk'}</p>
                <p className="truncate font-mono text-xs text-muted-foreground">
                  {seat.student?.id ?? `Row ${seat.row} · Desk ${seat.col}`}
                </p>
              </div>
            </div>

            {seat.student && (
              <dl className="mt-5 grid grid-cols-2 gap-x-4 gap-y-3 text-xs">
                <div>
                  <dt className="text-muted-foreground">Program</dt>
                  <dd className="mt-0.5 font-medium">{seat.student.program}</dd>
                </div>
                <div>
                  <dt className="text-muted-foreground">Position</dt>
                  <dd className="mt-0.5 font-medium">
                    Row {seat.row}, Desk {seat.col}
                  </dd>
                </div>
                <div className="col-span-2">
                  <dt className="text-muted-foreground">Course</dt>
                  <dd className="mt-0.5 font-medium">{seat.student.course}</dd>
                </div>
              </dl>
            )}

            {seat.conflictReason && (
              <div className="mt-4 flex gap-2.5 rounded-lg border border-conflict/20 bg-conflict-soft p-3 text-xs text-conflict">
                <TriangleAlert className="mt-0.5 size-4 shrink-0" aria-hidden="true" />
                <p>
                  <span className="font-semibold">Conflict: </span>
                  {seat.conflictReason}
                </p>
              </div>
            )}

            <div className="mt-5 grid grid-cols-2 gap-2">
              <button
                type="button"
                className="inline-flex h-9 items-center justify-center gap-2 rounded-lg border border-border text-[13px] font-medium transition hover:bg-muted"
              >
                <Printer className="size-4 text-muted-foreground" aria-hidden="true" />
                Seat slip
              </button>
              <button
                type="button"
                className="inline-flex h-9 items-center justify-center gap-2 rounded-lg bg-gold text-[13px] font-semibold text-gold-foreground transition hover:brightness-95"
              >
                {seat.status === 'empty' ? 'Assign student' : seat.status === 'conflict' ? 'Resolve' : 'Reassign'}
              </button>
            </div>
          </div>
        ) : (
          <div className="mt-6 flex flex-col items-center py-6 text-center">
            <div className="flex size-12 items-center justify-center rounded-full bg-muted">
              <Armchair className="size-5 text-muted-foreground" aria-hidden="true" />
            </div>
            <p className="mt-3 text-sm font-medium">No seat selected</p>
            <p className="mt-1 text-xs text-muted-foreground">Click any desk on the map to inspect it.</p>
          </div>
        )}
      </div>

      <div className="rounded-xl border border-border/80 bg-card p-5 shadow-[0_1px_2px_rgba(16,24,40,0.04),0_8px_24px_-12px_rgba(16,24,40,0.08)]">
        <h3 className="text-sm font-semibold">Room Occupancy</h3>
        <div className="mt-4 flex items-center gap-5">
          <div className="relative size-24 shrink-0">
            <svg viewBox="0 0 80 80" className="size-full -rotate-90" aria-hidden="true">
              <circle cx="40" cy="40" r="34" fill="none" strokeWidth="8" className="stroke-muted" />
              <circle
                cx="40"
                cy="40"
                r="34"
                fill="none"
                strokeWidth="8"
                strokeLinecap="round"
                className="stroke-success transition-all duration-500"
                strokeDasharray={`${assignedArc} ${circumference}`}
              />
              <circle
                cx="40"
                cy="40"
                r="34"
                fill="none"
                strokeWidth="8"
                className="stroke-conflict transition-all duration-500"
                strokeDasharray={`${conflictArc} ${circumference}`}
                strokeDashoffset={-assignedArc}
              />
            </svg>
            <div className="absolute inset-0 flex flex-col items-center justify-center">
              <span className="text-xl font-semibold tabular-nums">{stats.occupancy}%</span>
              <span className="text-[10px] text-muted-foreground">filled</span>
            </div>
          </div>
          <ul className="flex flex-1 flex-col gap-2.5 text-xs">
            {[
              { label: 'Assigned', value: stats.assigned, dot: 'bg-success' },
              { label: 'Conflict', value: stats.conflict, dot: 'bg-conflict' },
              { label: 'Empty', value: stats.empty, dot: 'bg-muted-foreground/30' },
            ].map((item) => (
              <li key={item.label} className="flex items-center justify-between">
                <span className="flex items-center gap-2 text-muted-foreground">
                  <span className={cn('size-2 rounded-full', item.dot)} aria-hidden="true" />
                  {item.label}
                </span>
                <span className="font-mono font-semibold tabular-nums">{item.value}</span>
              </li>
            ))}
          </ul>
        </div>
      </div>

      <div className="rounded-xl border border-border/80 bg-card p-5 shadow-[0_1px_2px_rgba(16,24,40,0.04),0_8px_24px_-12px_rgba(16,24,40,0.08)] md:col-span-2 2xl:col-span-1">
        <div className="flex items-center justify-between">
          <h3 className="text-sm font-semibold">Conflicts in Room</h3>
          <span className="rounded-full bg-conflict-soft px-2 py-0.5 font-mono text-[11px] font-semibold text-conflict">
            {conflicts.length}
          </span>
        </div>
        {conflicts.length === 0 ? (
          <p className="mt-4 text-xs text-muted-foreground">All clear — no conflicts detected.</p>
        ) : (
          <ul className="mt-3 flex flex-col divide-y divide-border/70">
            {conflicts.map((c) => (
              <li key={c.id}>
                <button
                  type="button"
                  onClick={() => onSelect(c.id)}
                  className="flex w-full items-center gap-3 py-2.5 text-left transition hover:opacity-80"
                >
                  <span className="flex size-8 shrink-0 items-center justify-center rounded-lg bg-conflict-soft font-mono text-[11px] font-semibold text-conflict">
                    {c.id}
                  </span>
                  <span className="min-w-0 flex-1">
                    <span className="block truncate font-mono text-xs font-medium">{c.student?.id}</span>
                    <span className="block truncate text-[11px] text-muted-foreground">{c.conflictReason}</span>
                  </span>
                  <UserRound className="size-4 shrink-0 text-muted-foreground" aria-hidden="true" />
                </button>
              </li>
            ))}
          </ul>
        )}
      </div>
    </aside>
  )
}
