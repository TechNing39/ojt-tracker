import { useEffect, useState } from 'react'
import { apiDelete, apiGet, apiPost } from '../../api/http'
import type { ChecklistItem } from '../../types'

export function ChecklistItems() {
  const [items, setItems] = useState<ChecklistItem[]>([])
  const [newTitle, setNewTitle] = useState('')
  const [error, setError] = useState<string | null>(null)

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
      await apiPost<ChecklistItem>('/checklist-items', { title: newTitle.trim() })
      setNewTitle('')
      loadItems()
    } catch {
      setError('항목을 추가하지 못했습니다.')
    }
  }

  const handleDelete = async (id: number) => {
    try {
      await apiDelete(`/checklist-items/${id}`)
      loadItems()
    } catch {
      setError('항목을 삭제하지 못했습니다.')
    }
  }

  return (
    <div className="card">
      <h2>체크리스트 항목</h2>
      {error && <p className="error-text">{error}</p>}
      {items.length === 0 ? (
        <p className="empty-state">등록된 항목이 없습니다.</p>
      ) : (
        <ul className="item-list">
          {items.map((item) => (
            <li key={item.id} className="item-row">
              {item.title}
              <button className="btn-ghost" onClick={() => handleDelete(item.id)}>
                삭제
              </button>
            </li>
          ))}
        </ul>
      )}
      <div className="field-row">
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
