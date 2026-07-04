export interface ChecklistItem {
  id: number
  title: string
  createdAt: string
}

export interface Trainee {
  id: number
  name: string
  createdAt: string
}

export interface ProgressItem {
  checklistItemId: number
  title: string
  completed: boolean
  completedAt: string | null
}
