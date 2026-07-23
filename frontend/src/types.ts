export type Category = 'FLOOR' | 'CONCESSION' | 'TICKETING' | 'CLOSING' | 'TWOSOME'

export const CATEGORIES: Category[] = ['FLOOR', 'CONCESSION', 'TICKETING', 'CLOSING', 'TWOSOME']

export const CATEGORY_LABELS: Record<Category, string> = {
  FLOOR: '플로어',
  CONCESSION: '매점',
  TICKETING: '매표',
  CLOSING: '마감',
  TWOSOME: '투썸',
}

export interface ChecklistItem {
  id: number
  title: string
  category: Category
  createdAt: string
  siteId: number
}

export interface Trainee {
  id: number
  name: string
  note: string | null
  createdAt: string
  siteId: number
}

export interface ProgressItem {
  checklistItemId: number
  title: string
  category: Category
  completed: boolean
  completedAt: string | null
}

export type SiteCode = 'JUNGGYE' | 'SANGBONG' | 'BANGHAK' | 'SUYU' | 'MIA'

export const SITES: { code: SiteCode; name: string }[] = [
  { code: 'JUNGGYE', name: '중계' },
  { code: 'SANGBONG', name: '상봉' },
  { code: 'BANGHAK', name: '방학' },
  { code: 'SUYU', name: '수유' },
  { code: 'MIA', name: '미아' },
]

export type Role = 'SITE' | 'ADMIN'

