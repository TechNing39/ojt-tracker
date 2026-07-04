import { useEffect, useState } from 'react'
import { apiGet, apiPatch } from '../../api/http'
import type { ProgressItem, Trainee } from '../../types'

export function TraineeProgressView() {
  const [trainees, setTrainees] = useState<Trainee[]>([])
  const [selectedTraineeId, setSelectedTraineeId] = useState<number | null>(null)
  const [progress, setProgress] = useState<ProgressItem[]>([])
  const [error, setError] = useState<string | null>(null)

  useEffect(() => {
    apiGet<Trainee[]>('/trainees')
      .then(setTrainees)
      .catch(() => setError('신입 목록을 불러오지 못했습니다.'))
  }, [])

  const loadProgress = (traineeId: number) => {
    apiGet<ProgressItem[]>(`/trainees/${traineeId}/progress`)
      .then(setProgress)
      .catch(() => setError('진행상황을 불러오지 못했습니다.'))
  }

  const handleSelect = (traineeId: number) => {
    setSelectedTraineeId(traineeId)
    loadProgress(traineeId)
  }

  const handleToggle = async (checklistItemId: number) => {
    if (selectedTraineeId === null) return
    try {
      await apiPatch(`/trainees/${selectedTraineeId}/progress/${checklistItemId}`)
      loadProgress(selectedTraineeId)
    } catch {
      setError('진행상황을 변경하지 못했습니다.')
    }
  }

  return (
    <section>
      <h2>신입별 진행상황</h2>
      {error && <p style={{ color: 'red' }}>{error}</p>}
      <select
        value={selectedTraineeId ?? ''}
        onChange={(e) => handleSelect(Number(e.target.value))}
      >
        <option value="" disabled>
          신입 선택
        </option>
        {trainees.map((trainee) => (
          <option key={trainee.id} value={trainee.id}>
            {trainee.name}
          </option>
        ))}
      </select>

      {selectedTraineeId !== null && (
        <ul>
          {progress.map((item) => (
            <li key={item.checklistItemId}>
              <label>
                <input
                  type="checkbox"
                  checked={item.completed}
                  onChange={() => handleToggle(item.checklistItemId)}
                />
                {item.title}
              </label>
            </li>
          ))}
        </ul>
      )}
    </section>
  )
}
