import { Bell, ChevronDown, CircleHelp, Menu, Search } from 'lucide-react'

export function TopHeader({ onMenu }: { onMenu: () => void }) {
  return (
    <header className="sticky top-0 z-30 flex h-16 items-center gap-3 border-b border-border/70 bg-background/80 px-4 backdrop-blur-xl md:px-8">
      <button
        type="button"
        onClick={onMenu}
        className="rounded-lg p-2 text-muted-foreground hover:bg-card hover:text-foreground lg:hidden"
        aria-label="Open navigation"
      >
        <Menu className="size-5" />
      </button>

      <div className="relative max-w-md flex-1">
        <label htmlFor="global-search" className="sr-only">
          Search students, rooms or seats
        </label>
        <Search
          className="pointer-events-none absolute top-1/2 left-3.5 size-4 -translate-y-1/2 text-muted-foreground"
          aria-hidden="true"
        />
        <input
          id="global-search"
          type="search"
          placeholder="Search student ID, name, room…"
          className="h-10 w-full rounded-lg border border-border bg-card pr-14 pl-10 text-sm shadow-[0_1px_2px_rgba(16,24,40,0.04)] outline-none transition placeholder:text-muted-foreground/80 focus:border-gold focus:ring-3 focus:ring-gold/20"
        />
        <kbd className="pointer-events-none absolute top-1/2 right-3 hidden -translate-y-1/2 rounded-md border border-border bg-muted px-1.5 py-0.5 font-mono text-[10px] text-muted-foreground sm:block">
          ⌘K
        </kbd>
      </div>

      <div className="ml-auto flex items-center gap-1.5 md:gap-3">
        <button
          type="button"
          className="hidden rounded-lg p-2.5 text-muted-foreground transition hover:bg-card hover:text-foreground sm:block"
          aria-label="Help"
        >
          <CircleHelp className="size-[18px]" />
        </button>
        <button
          type="button"
          className="relative rounded-lg p-2.5 text-muted-foreground transition hover:bg-card hover:text-foreground"
          aria-label="Notifications, 3 unread"
        >
          <Bell className="size-[18px]" />
          <span className="absolute top-2 right-2 size-2 rounded-full bg-conflict ring-2 ring-background" />
        </button>

        <div className="mx-1 hidden h-8 w-px bg-border sm:block" aria-hidden="true" />

        <button
          type="button"
          className="flex items-center gap-3 rounded-lg py-1 pr-2 pl-1 transition hover:bg-card"
        >
          <span className="flex size-9 items-center justify-center rounded-full bg-primary text-xs font-semibold text-primary-foreground ring-2 ring-gold/70 ring-offset-2 ring-offset-background">
            NA
          </span>
          <span className="hidden text-left leading-tight md:block">
            <span className="block text-sm font-semibold">Nadia Aslam</span>
            <span className="block text-xs text-muted-foreground">Exam Controller</span>
          </span>
          <ChevronDown className="hidden size-4 text-muted-foreground md:block" aria-hidden="true" />
        </button>
      </div>
    </header>
  )
}
