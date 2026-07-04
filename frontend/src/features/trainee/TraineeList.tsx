import { useEffect, useState } from 'react'
import { apiDelete, apiGet, apiPost } from '../../api/http'
import type { Trainee } from '../../types'

export function TraineeList() {
  const [trainees, setTrainees] = useState<Trainee[]>([])
  const [newName, setNewName] = useState('')
  const [error, setError] = useState<string | null>(null)

  const loadTrainees = () => {
    apiGet<Trainee[]>('/trainees')
      .then(setTrainees)
      .catch(() => setError('목록을 불러오지 못했습니다.'))
  }

  useEffect(() => {
    loadTrainees()
  }, [])

  const handleAdd = async () => {
    if (!newName.trim()) return
    try {
      await apiPost<Trainee>('/trainees', { name: newName.trim() })
      setNewName('')
      loadTrainees()
    } catch {
      setError('신입을 등록하지 못했습니다.')
    }
  }

  const handleDelete = async (id: number) => {
    try {
      await apiDelete(`/trainees/${id}`)
      loadTrainees()
    } catch {
      setError('신입을 삭제하지 못했습니다.')
    }
  }

  return (
    <div className="card">
      <h2>신입 목록</h2>
      {error && <p className="error-text">{error}</p>}
      {trainees.length === 0 ? (
        <p className="empty-state">아직 등록된 신입이 없습니다.</p>
      ) : (
        <ul className="item-list">
          {trainees.map((trainee) => (
            <li key={trainee.id} className="item-row">
              {trainee.name}
              <button className="btn-ghost" onClick={() => handleDelete(trainee.id)}>
                삭제
              </button>
            </li>
          ))}
        </ul>
      )}
      <div className="field-row">
        <input
          className="text-input"
          value={newName}
          onChange={(e) => setNewName(e.target.value)}
          onKeyDown={(e) => e.key === 'Enter' && handleAdd()}
          placeholder="신입 이름"
        />
        <button className="btn" onClick={handleAdd}>
          등록
        </button>
      </div>
    </div>
  )
}
