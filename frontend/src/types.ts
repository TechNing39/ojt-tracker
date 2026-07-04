export type Category = 'FLOOR' | 'CONCESSION' | 'TICKETING' | 'CLOSING'

export const CATEGORIES: Category[] = ['FLOOR', 'CONCESSION', 'TICKETING', 'CLOSING']

export const CATEGORY_LABELS: Record<Category, string> = {
  FLOOR: '플로어',
  CONCESSION: '매점',
  TICKETING: '매표',
  CLOSING: '마감',
}

export interface ChecklistItem {
  id: number
  title: string
  category: Category
  createdAt: string
}

export interface Trainee {
  id: number
  name: string
  note: string | null
  createdAt: string
}

export interface ProgressItem {
  checklistItemId: number
  title: string
  category: Category
  completed: boolean
  completedAt: string | null
}
