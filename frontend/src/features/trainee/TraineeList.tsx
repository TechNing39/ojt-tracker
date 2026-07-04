import { useEffect, useState } from 'react'
import { apiGet, apiPost } from '../../api/http'
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

  return (
    <section>
      <h2>신입 목록</h2>
      {error && <p style={{ color: 'red' }}>{error}</p>}
      <ul>
        {trainees.map((trainee) => (
          <li key={trainee.id}>{trainee.name}</li>
        ))}
      </ul>
      <input
        value={newName}
        onChange={(e) => setNewName(e.target.value)}
        placeholder="신입 이름"
      />
      <button onClick={handleAdd}>등록</button>
    </section>
  )
}
