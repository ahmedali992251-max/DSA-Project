'use client'

import { useMemo, useState } from 'react'
import { Download, Presentation, RefreshCw, TriangleAlert, WandSparkles } from 'lucide-react'
import { cn } from '@/lib/utils'
import { ROOMS, roomStats, type Seat, type SeatStatus } from '@/lib/seating-data'
import { SeatDetailsPanel } from './seat-details-panel'

const LEGEND: { status: SeatStatus; label: string; swatch: string }[] = [
  { status: 'assigned', label: 'Assigned', swatch: 'bg-success' },
  { status: 'empty', label: 'Empty', swatch: 'bg-muted-foreground/30' },
  { status: 'conflict', label: 'Conflict', swatch: 'bg-conflict' },
]

export function SeatingMap() {
  const [roomId, setRoomId] = useState(ROOMS[0].id)
  const [filter, setFilter] = useState<SeatStatus | null>(null)
  const room = ROOMS.find((r) => r.id === roomId) ?? ROOMS[0]
  const [selectedId, setSelectedId] = useState<string | null>(
    () => room.seats.find((s) => s.status === 'conflict')?.id ?? null,
  )

  const stats = useMemo(() => roomStats(room), [room])
  const selected = room.seats.find((s) => s.id === selectedId) ?? null
  const aisleAfter = Math.ceil(room.cols / 2)

  const rows = useMemo(() => {
    const grouped: Record<string, Seat[]> = {}
    for (const seat of room.seats) (grouped[seat.row] ??= []).push(seat)
    return Object.entries(grouped)
  }, [room])

  function changeRoom(id: string) {
    setRoomId(id)
    const next = ROOMS.find((r) => r.id === id)
    setSelectedId(next?.seats.find((s) => s.status === 'conflict')?.id ?? null)
  }

  return (
    <section aria-labelledby="seating-map-title" className="grid grid-cols-1 gap-6 2xl:grid-cols-[minmax(0,1fr)_340px]">
      <div className="min-w-0 rounded-xl border border-border/80 bg-card shadow-[0_1px_2px_rgba(16,24,40,0.04),0_8px_24px_-12px_rgba(16,24,40,0.08)]">
        <div className="flex flex-col gap-4 border-b border-border/70 p-5 md:flex-row md:items-center md:justify-between">
          <div>
            <h2 id="seating-map-title" className="text-base font-semibold tracking-tight">
              {room.name}
              <span className="ml-2 align-middle text-xs font-normal text-muted-foreground">
                {room.block} · {room.rows} rows × {room.cols} desks
              </span>
            </h2>
            <p className="mt-1 text-xs text-muted-foreground">
              Invigilator: <span className="font-medium text-foreground">{room.invigilator}</span>
            </p>
          </div>
          <div className="flex flex-wrap items-center gap-2">
            <button
              type="button"
              className="inline-flex h-9 items-center gap-2 rounded-lg border border-border bg-card px-3 text-[13px] font-medium text-foreground transition hover:bg-muted"
            >
              <RefreshCw className="size-4 text-muted-foreground" aria-hidden="true" />
              Reshuffle
            </button>
            <button
              type="button"
              className="inline-flex h-9 items-center gap-2 rounded-lg border border-border bg-card px-3 text-[13px] font-medium text-foreground transition hover:bg-muted"
            >
              <Download className="size-4 text-muted-foreground" aria-hidden="true" />
              Export PDF
            </button>
            <button
              type="button"
              className="inline-flex h-9 items-center gap-2 rounded-lg bg-primary px-3.5 text-[13px] font-medium text-primary-foreground shadow-[0_4px_14px_-4px] shadow-primary/50 transition hover:bg-navy-deep"
            >
              <WandSparkles className="size-4 text-gold" aria-hidden="true" />
              Auto-assign
            </button>
          </div>
        </div>

        <div className="flex flex-col gap-3 px-5 pt-4 lg:flex-row lg:items-center lg:justify-between">
          <div role="tablist" aria-label="Exam rooms" className="flex gap-1 overflow-x-auto rounded-lg bg-muted p-1">
            {ROOMS.map((r) => {
              const s = roomStats(r)
              const active = r.id === room.id
              return (
                <button
                  key={r.id}
                  role="tab"
                  type="button"
                  aria-selected={active}
                  onClick={() => changeRoom(r.id)}
                  className={cn(
                    'flex shrink-0 items-center gap-2 rounded-md px-3 py-1.5 text-[13px] font-medium transition',
                    active
                      ? 'bg-card text-foreground shadow-[0_1px_3px_rgba(16,24,40,0.1)]'
                      : 'text-muted-foreground hover:text-foreground',
                  )}
                >
                  {r.name}
                  <span
                    className={cn(
                      'rounded px-1.5 font-mono text-[10px] tabular-nums',
                      active ? 'bg-gold/20 text-gold-foreground' : 'bg-background text-muted-foreground',
                    )}
                  >
                    {s.occupancy}%
                  </span>
                </button>
              )
            })}
          </div>

          <div className="flex flex-wrap items-center gap-1.5" aria-label="Filter by seat status">
            {LEGEND.map((l) => {
              const count = l.status === 'assigned' ? stats.assigned : l.status === 'empty' ? stats.empty : stats.conflict
              const active = filter === l.status
              return (
                <button
                  key={l.status}
                  type="button"
                  aria-pressed={active}
                  onClick={() => setFilter(active ? null : l.status)}
                  className={cn(
                    'inline-flex items-center gap-2 rounded-full border px-3 py-1 text-xs font-medium transition',
                    active ? 'border-primary bg-primary text-primary-foreground' : 'border-border text-muted-foreground hover:border-foreground/20 hover:text-foreground',
                  )}
                >
                  <span className={cn('size-2 rounded-full', l.swatch)} aria-hidden="true" />
                  {l.label}
                  <span className="font-mono tabular-nums opacity-70">{count}</span>
                </button>
              )
            })}
          </div>
        </div>

        <div className="overflow-x-auto p-5">
          <div style={{ minWidth: room.cols * 72 + (room.cols - 1) * 8 + 52 }}>
            <div className="mx-auto mb-6 flex max-w-md items-center justify-center gap-2 rounded-lg border border-dashed border-primary/25 bg-primary/[0.03] py-2.5 text-[11px] font-semibold uppercase tracking-[0.18em] text-primary/70">
              <Presentation className="size-4" aria-hidden="true" />
              Invigilator Desk · Board
            </div>

            <div className="flex flex-col gap-2.5" role="grid" aria-label={`${room.name} seating grid`}>
              {rows.map(([row, seats]) => (
                <div key={row} role="row" className="flex items-center gap-2">
                  <span className="w-5 shrink-0 text-center font-mono text-[11px] font-semibold text-muted-foreground">
                    {row}
                  </span>
                  {seats.map((seat) => (
                    <div key={seat.id} role="gridcell" className={cn('flex min-w-0 flex-1', seat.col === aisleAfter && 'mr-6')}>
                      <Desk
                        seat={seat}
                        selected={seat.id === selectedId}
                        dimmed={filter !== null && filter !== seat.status}
                        onSelect={() => setSelectedId(seat.id)}
                      />
                    </div>
                  ))}
                </div>
              ))}
              <div className="flex items-center gap-2 pt-1" aria-hidden="true">
                <span className="w-5 shrink-0" />
                {Array.from({ length: room.cols }, (_, i) => (
                  <span
                    key={i}
                    className={cn(
                      'min-w-0 flex-1 text-center font-mono text-[10px] text-muted-foreground/70',
                      i + 1 === aisleAfter && 'mr-6',
                    )}
                  >
                    {i + 1}
                  </span>
                ))}
              </div>
            </div>
          </div>
        </div>

        <div className="flex flex-col gap-3 border-t border-border/70 px-5 py-4 text-xs text-muted-foreground sm:flex-row sm:items-center sm:justify-between">
          <p>
            <span className="font-semibold text-foreground tabular-nums">{stats.assigned + stats.conflict}</span> of{' '}
            <span className="tabular-nums">{stats.capacity}</span> desks occupied · Anti-cheating spacing:{' '}
            <span className="font-medium text-foreground">alternate programs</span>
          </p>
          {stats.conflict > 0 && (
            <p className="inline-flex items-center gap-1.5 font-medium text-conflict">
              <TriangleAlert className="size-3.5" aria-hidden="true" />
              {stats.conflict} seat {stats.conflict === 1 ? 'conflict needs' : 'conflicts need'} resolution
            </p>
          )}
        </div>
      </div>

      <SeatDetailsPanel room={room} stats={stats} seat={selected} onSelect={setSelectedId} />
    </section>
  )
}

function Desk({
  seat,
  selected,
  dimmed,
  onSelect,
}: {
  seat: Seat
  selected: boolean
  dimmed: boolean
  onSelect: () => void
}) {
  const label =
    seat.status === 'empty'
      ? `Seat ${seat.id}, empty`
      : `Seat ${seat.id}, ${seat.student?.id}, ${seat.status}`

  return (
    <button
      type="button"
      onClick={onSelect}
      aria-label={label}
      aria-pressed={selected}
      className={cn(
        'relative flex h-[60px] w-full flex-col justify-between rounded-lg border px-2 py-1.5 text-left transition duration-150 focus-visible:outline-none focus-visible:ring-3 focus-visible:ring-gold/50',
        seat.status === 'assigned' &&
          'border-success/25 bg-success-soft hover:border-success/60 hover:shadow-[0_6px_16px_-8px] hover:shadow-success/50',
        seat.status === 'empty' && 'border-dashed border-border bg-muted/60 hover:border-muted-foreground/40 hover:bg-muted',
        seat.status === 'conflict' &&
          'border-conflict/40 bg-conflict-soft hover:border-conflict hover:shadow-[0_6px_16px_-8px] hover:shadow-conflict/50',
        selected && 'z-10 -translate-y-0.5 ring-2 ring-gold ring-offset-2 ring-offset-card',
        dimmed && 'opacity-25',
      )}
    >
      <span className="flex items-center justify-between">
        <span
          className={cn(
            'font-mono text-[10px] font-semibold',
            seat.status === 'assigned' && 'text-success',
            seat.status === 'empty' && 'text-muted-foreground/70',
            seat.status === 'conflict' && 'text-conflict',
          )}
        >
          {seat.id}
          {seat.student && (
            <span className="ml-1 font-sans text-[9px] font-medium tracking-wide opacity-70">{seat.student.program}</span>
          )}
        </span>
        {seat.status === 'conflict' ? (
          <TriangleAlert className="size-3 text-conflict" aria-hidden="true" />
        ) : (
          <span
            className={cn(
              'size-1.5 rounded-full',
              seat.status === 'assigned' ? 'bg-success' : 'bg-muted-foreground/25',
            )}
            aria-hidden="true"
          />
        )}
      </span>
      <span
        className={cn(
          'truncate font-mono text-[10.5px] font-medium',
          seat.status === 'empty' ? 'text-muted-foreground/60' : 'text-foreground',
        )}
      >
        {seat.student ? seat.student.id.slice(seat.student.program.length + 1) : 'Vacant'}
      </span>
    </button>
  )
}
