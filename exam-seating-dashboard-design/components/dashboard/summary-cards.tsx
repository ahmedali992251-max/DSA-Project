import { ArrowUpRight, Building2, Armchair, TriangleAlert, Users } from 'lucide-react'
import { cn } from '@/lib/utils'

const CARDS = [
  {
    label: 'Total Students',
    value: '4,280',
    delta: '+124 this term',
    icon: Users,
    tone: 'navy',
    progress: 100,
    caption: 'Across 4 programs',
  },
  {
    label: 'Rooms Active',
    value: '18',
    suffix: '/ 22',
    delta: '4 on standby',
    icon: Building2,
    tone: 'gold',
    progress: 82,
    caption: '82% of inventory in use',
  },
  {
    label: 'Seat Capacity',
    value: '3,912',
    suffix: '/ 4,460',
    delta: '87.7% utilised',
    icon: Armchair,
    tone: 'green',
    progress: 88,
    caption: '548 seats available',
  },
  {
    label: 'Open Conflicts',
    value: '12',
    delta: '−8 since yesterday',
    icon: TriangleAlert,
    tone: 'red',
    progress: 18,
    caption: 'Review before lock-in',
  },
] as const

const TONES = {
  navy: { icon: 'bg-primary/8 text-primary', bar: 'bg-primary' },
  gold: { icon: 'bg-gold/15 text-gold-foreground', bar: 'bg-gold' },
  green: { icon: 'bg-success-soft text-success', bar: 'bg-success' },
  red: { icon: 'bg-conflict-soft text-conflict', bar: 'bg-conflict' },
}

export function SummaryCards() {
  return (
    <section aria-label="Summary" className="grid grid-cols-1 gap-4 sm:grid-cols-2 xl:grid-cols-4">
      {CARDS.map((card) => {
        const tone = TONES[card.tone]
        const Icon = card.icon
        return (
          <article
            key={card.label}
            className="group rounded-xl border border-border/80 bg-card p-5 shadow-[0_1px_2px_rgba(16,24,40,0.04),0_8px_24px_-12px_rgba(16,24,40,0.08)] transition hover:-translate-y-0.5 hover:shadow-[0_1px_2px_rgba(16,24,40,0.04),0_16px_32px_-16px_rgba(16,24,40,0.16)]"
          >
            <div className="flex items-start justify-between">
              <div className={cn('flex size-10 items-center justify-center rounded-lg', tone.icon)}>
                <Icon className="size-5" aria-hidden="true" />
              </div>
              <span className="inline-flex items-center gap-1 rounded-full bg-muted px-2 py-0.5 text-[11px] font-medium text-muted-foreground">
                {card.delta}
                <ArrowUpRight className="size-3" aria-hidden="true" />
              </span>
            </div>
            <p className="mt-5 text-[13px] font-medium text-muted-foreground">{card.label}</p>
            <p className="mt-1 flex items-baseline gap-1.5">
              <span className="text-3xl font-semibold tracking-tight tabular-nums">{card.value}</span>
              {'suffix' in card && (
                <span className="text-sm font-medium text-muted-foreground tabular-nums">{card.suffix}</span>
              )}
            </p>
            <div className="mt-4 h-1 overflow-hidden rounded-full bg-muted">
              <div className={cn('h-full rounded-full', tone.bar)} style={{ width: `${card.progress}%` }} />
            </div>
            <p className="mt-2 text-xs text-muted-foreground">{card.caption}</p>
          </article>
        )
      })}
    </section>
  )
}
