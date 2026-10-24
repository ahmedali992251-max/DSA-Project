export type SeatStatus = 'assigned' | 'empty' | 'conflict'

export type Seat = {
  id: string
  row: string
  col: number
  status: SeatStatus
  student?: {
    id: string
    name: string
    program: string
    course: string
  }
  conflictReason?: string
}

export type Room = {
  id: string
  name: string
  block: string
  rows: number
  cols: number
  invigilator: string
  seats: Seat[]
}

const FIRST = ['Ayesha', 'Hamza', 'Fatima', 'Ali', 'Zainab', 'Usman', 'Mariam', 'Bilal', 'Hira', 'Saad', 'Sana', 'Ahmed', 'Iqra', 'Daniyal', 'Noor', 'Faris']
const LAST = ['Khan', 'Siddiqui', 'Qureshi', 'Malik', 'Shaikh', 'Raza', 'Hussain', 'Baig', 'Javed', 'Mirza', 'Ansari', 'Farooq']
const PROGRAMS = [
  { code: 'BBA', course: 'MKT-301 Marketing Mgmt' },
  { code: 'BSCS', course: 'CSE-342 Algorithms' },
  { code: 'BSAF', course: 'FIN-210 Corporate Finance' },
  { code: 'BSECO', course: 'ECO-220 Macroeconomics' },
]
const CONFLICT_REASONS = [
  'Same course as adjacent seat',
  'Duplicate booking across sessions',
  'Accessibility seat required',
]

function seeded(seed: number) {
  let s = seed
  return () => {
    s = (s * 9301 + 49297) % 233280
    return s / 233280
  }
}

function buildRoom(
  id: string,
  name: string,
  block: string,
  rows: number,
  cols: number,
  invigilator: string,
  seed: number,
  fill: number,
  conflictRate: number,
): Room {
  const rand = seeded(seed)
  const seats: Seat[] = []
  for (let r = 0; r < rows; r++) {
    const row = String.fromCharCode(65 + r)
    for (let c = 1; c <= cols; c++) {
      const roll = rand()
      const status: SeatStatus =
        roll < conflictRate ? 'conflict' : roll < fill ? 'assigned' : 'empty'
      const seat: Seat = { id: `${row}${c}`, row, col: c, status }
      if (status !== 'empty') {
        const program = PROGRAMS[Math.floor(rand() * PROGRAMS.length)]
        const year = 21 + Math.floor(rand() * 4)
        const num = 1000 + Math.floor(rand() * 8999)
        seat.student = {
          id: `${program.code}-${year}-${num}`,
          name: `${FIRST[Math.floor(rand() * FIRST.length)]} ${LAST[Math.floor(rand() * LAST.length)]}`,
          program: program.code,
          course: program.course,
        }
        if (status === 'conflict') {
          seat.conflictReason = CONFLICT_REASONS[Math.floor(rand() * CONFLICT_REASONS.length)]
        }
      }
      seats.push(seat)
    }
  }
  return { id, name, block, rows, cols, invigilator, seats }
}

export const ROOMS: Room[] = [
  buildRoom('a101', 'Hall A-101', 'Aman Tower', 6, 10, 'Dr. Sadia Rehman', 11, 0.86, 0.05),
  buildRoom('b204', 'Room B-204', 'Tabba Academic', 5, 8, 'Prof. Imran Ahmed', 27, 0.78, 0.04),
  buildRoom('c012', 'Lab C-012', 'Main Campus', 4, 8, 'Ms. Rabia Noor', 43, 0.65, 0.07),
  buildRoom('aud1', 'Auditorium 1', 'Adamjee Block', 7, 12, 'Dr. Kamran Ali', 59, 0.9, 0.03),
]

export function roomStats(room: Room) {
  const assigned = room.seats.filter((s) => s.status === 'assigned').length
  const conflict = room.seats.filter((s) => s.status === 'conflict').length
  const empty = room.seats.filter((s) => s.status === 'empty').length
  const capacity = room.seats.length
  return { assigned, conflict, empty, capacity, occupancy: Math.round(((assigned + conflict) / capacity) * 100) }
}
