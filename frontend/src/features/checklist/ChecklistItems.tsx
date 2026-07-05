import { useEffect, useState } from 'react'
import { apiDelete, apiGet, apiPatch, apiPost } from '../../api/http'
import type { Category, ChecklistItem } from '../../types'
import { CATEGORIES, CATEGORY_LABELS } from '../../types'

export function ChecklistItems() {
  const [items, setItems] = useState<ChecklistItem[]>([])
  const [newTitle, setNewTitle] = useState('')
  const [newCategory, setNewCategory] = useState<Category>('FLOOR')
  const [error, setError] = useState<string | null>(null)
  const [editingId, setEditingId] = useState<number | null>(null)
  const [editTitle, setEditTitle] = useState('')
  const [editCategory, setEditCategory] = useState<Category>('FLOOR')

  const loadItems = () => {
    apiGet<ChecklistItem[]>('/checklist-items')
      .then(setItems)
      .catch(() => setError('목록을 불러오지 못했습니다.'))
  }

  useEffect(() => {
    loadItems()
  }, [])

  const handleAdd = async () => {
    if (!newTitle.trim()) return
    try {
      await apiPost<ChecklistItem>('/checklist-items', {
        title: newTitle.trim(),
        category: newCategory,
      })
      setNewTitle('')
      loadItems()
    } catch {
      setError('항목을 추가하지 못했습니다.')
    }
  }

  const handleDelete = async (id: number, title: string) => {
    if (!window.confirm(`"${title}" 항목을 삭제할까요?`)) return
    try {
      await apiDelete(`/checklist-items/${id}`)
      loadItems()
    } catch {
      setError('항목을 삭제하지 못했습니다.')
    }
  }

  const handleStartEdit = (item: ChecklistItem) => {
    setEditingId(item.id)
    setEditTitle(item.title)
    setEditCategory(item.category)
  }

  const handleCancelEdit = () => {
    setEditingId(null)
  }

  const handleSaveEdit = async (id: number) => {
    if (!editTitle.trim()) return
    try {
      await apiPatch<ChecklistItem>(`/checklist-items/${id}`, {
        title: editTitle.trim(),
        category: editCategory,
      })
      setEditingId(null)
      loadItems()
    } catch {
      setError('항목을 수정하지 못했습니다.')
    }
  }

  return (
    <div className="card">
      <h2>체크리스트 항목</h2>
      {error && <p className="error-text">{error}</p>}
      {items.length === 0 ? (
        <p className="empty-state">등록된 항목이 없습니다.</p>
      ) : (
        CATEGORIES.map((category) => {
          const categoryItems = items.filter((item) => item.category === category)
          if (categoryItems.length === 0) return null
          return (
            <div key={category} className="category-group">
              <h3 className="category-heading">{CATEGORY_LABELS[category]}</h3>
              <ul className="item-list">
                {categoryItems.map((item) =>
                  editingId === item.id ? (
                    <li key={item.id} className="item-row item-row-editing">
                      <input
                        className="text-input"
                        value={editTitle}
                        onChange={(e) => setEditTitle(e.target.value)}
                        onKeyDown={(e) => e.key === 'Enter' && handleSaveEdit(item.id)}
                      />
                      <select
                        className="select-input"
                        style={{ flex: '0 0 100px' }}
                        value={editCategory}
                        onChange={(e) => setEditCategory(e.target.value as Category)}
                      >
                        {CATEGORIES.map((category) => (
                          <option key={category} value={category}>
                            {CATEGORY_LABELS[category]}
                          </option>
                        ))}
                      </select>
                      <button className="btn" onClick={() => handleSaveEdit(item.id)}>
                        저장
                      </button>
                      <button className="btn-ghost" onClick={handleCancelEdit}>
                        취소
                      </button>
                    </li>
                  ) : (
                    <li key={item.id} className="item-row">
                      {item.title}
                      <span className="item-row-actions">
                        <button className="btn-ghost" onClick={() => handleStartEdit(item)}>
                          수정
                        </button>
                        <button className="btn-ghost" onClick={() => handleDelete(item.id, item.title)}>
                          삭제
                        </button>
                      </span>
                    </li>
                  ),
                )}
              </ul>
            </div>
          )
        })
      )}
      <div className="field-row" style={{ flexWrap: 'wrap' }}>
        <select
          className="select-input"
          style={{ flex: '0 0 100px' }}
          value={newCategory}
          onChange={(e) => setNewCategory(e.target.value as Category)}
        >
          {CATEGORIES.map((category) => (
            <option key={category} value={category}>
              {CATEGORY_LABELS[category]}
            </option>
          ))}
        </select>
        <input
          className="text-input"
          value={newTitle}
          onChange={(e) => setNewTitle(e.target.value)}
          onKeyDown={(e) => e.key === 'Enter' && handleAdd()}
          placeholder="새 체크리스트 항목"
        />
        <button className="btn" onClick={handleAdd}>
          추가
        </button>
      </div>
    </div>
  )
}
