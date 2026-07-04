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
    <section>
      <h2>체크리스트 항목</h2>
      {error && <p style={{ color: 'red' }}>{error}</p>}
      <ul>
        {items.map((item) => (
          <li key={item.id}>
            {item.title}
            <button onClick={() => handleDelete(item.id)}>삭제</button>
          </li>
        ))}
      </ul>
      <input
        value={newTitle}
        onChange={(e) => setNewTitle(e.target.value)}
        placeholder="새 체크리스트 항목"
      />
      <button onClick={handleAdd}>추가</button>
    </section>
  )
}
