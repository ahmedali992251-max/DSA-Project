import {
  ClipboardCheck,
  DoorOpen,
  FileText,
  Grid3x3,
  LayoutDashboard,
  LogOut,
  Search,
  Settings,
  Users,
  WandSparkles,
  X,
  GraduationCap,
} from 'lucide-react'
import { cn } from '@/lib/utils'

const NAV = [
  { label: 'Dashboard', icon: LayoutDashboard },
  { label: 'Student Data', icon: Users, badge: '4,280' },
  { label: 'Room Distribution', icon: DoorOpen },
  { label: 'Automatic Seating', icon: WandSparkles },
  { label: 'Seating Map', icon: Grid3x3, active: true },
  { label: 'Seat Search', icon: Search },
  { label: 'Reports & PDF', icon: FileText },
  { label: 'Attendance Sheets', icon: ClipboardCheck },
]

export function Sidebar({ onClose }: { onClose?: () => void }) {
  return (
    <div className="flex h-full flex-col bg-sidebar text-sidebar-foreground">
      <div className="flex h-16 items-center justify-between gap-3 px-5">
        <div className="flex items-center gap-3">
          <div className="flex size-9 items-center justify-center rounded-lg bg-gold text-gold-foreground shadow-[0_6px_20px_-6px] shadow-gold/60">
            <GraduationCap className="size-5" aria-hidden="true" />
          </div>
          <div className="leading-tight">
            <p className="text-sm font-semibold tracking-tight text-white">IBA ExamDesk</p>
            <p className="text-[11px] text-sidebar-foreground/60">Seating & Student Mgmt</p>
          </div>
        </div>
        {onClose && (
          <button
            type="button"
            onClick={onClose}
            className="rounded-md p-1.5 text-sidebar-foreground/70 hover:bg-sidebar-accent hover:text-white lg:hidden"
            aria-label="Close navigation"
          >
            <X className="size-5" />
          </button>
        )}
      </div>

      <nav aria-label="Main" className="mt-4 flex-1 overflow-y-auto px-3">
        <p className="px-3 pb-2 text-[10px] font-semibold uppercase tracking-[0.14em] text-sidebar-foreground/40">
          Workspace
        </p>
        <ul className="flex flex-col gap-0.5">
          {NAV.map(({ label, icon: Icon, active, badge }) => (
            <li key={label}>
              <a
                href="#"
                aria-current={active ? 'page' : undefined}
                className={cn(
                  'group relative flex items-center gap-3 rounded-lg px-3 py-2.5 text-[13px] font-medium transition-colors',
                  active
                    ? 'bg-sidebar-accent text-white'
                    : 'text-sidebar-foreground/75 hover:bg-sidebar-accent/60 hover:text-white',
                )}
              >
                {active && (
                  <span
                    className="absolute inset-y-2 left-0 w-[3px] rounded-r-full bg-gold"
                    aria-hidden="true"
                  />
                )}
                <Icon
                  className={cn(
                    'size-[18px] shrink-0',
                    active ? 'text-gold' : 'text-sidebar-foreground/55 group-hover:text-white',
                  )}
                  aria-hidden="true"
                />
                <span className="flex-1">{label}</span>
                {badge && (
                  <span className="rounded-md bg-white/8 px-1.5 py-0.5 font-mono text-[10px] text-sidebar-foreground/70">
                    {badge}
                  </span>
                )}
              </a>
            </li>
          ))}
        </ul>
      </nav>

      <div className="m-3 rounded-xl border border-sidebar-border bg-sidebar-accent/50 p-4">
        <p className="text-xs font-semibold text-white">Spring 2026 Finals</p>
        <p className="mt-1 text-[11px] leading-relaxed text-sidebar-foreground/60">
          Seating locks in 3 days. 12 conflicts need review.
        </p>
        <div className="mt-3 h-1.5 overflow-hidden rounded-full bg-white/10">
          <div className="h-full w-[82%] rounded-full bg-gold" />
        </div>
        <p className="mt-1.5 text-[10px] text-sidebar-foreground/50">82% of sessions finalised</p>
      </div>

      <div className="flex items-center gap-1 border-t border-sidebar-border px-3 py-3">
        <a
          href="#"
          className="flex flex-1 items-center gap-3 rounded-lg px-3 py-2 text-[13px] text-sidebar-foreground/70 hover:bg-sidebar-accent/60 hover:text-white"
        >
          <Settings className="size-[18px]" aria-hidden="true" />
          Settings
        </a>
        <button
          type="button"
          className="rounded-lg p-2 text-sidebar-foreground/60 hover:bg-sidebar-accent/60 hover:text-white"
          aria-label="Sign out"
        >
          <LogOut className="size-[18px]" />
        </button>
      </div>
    </div>
  )
}
