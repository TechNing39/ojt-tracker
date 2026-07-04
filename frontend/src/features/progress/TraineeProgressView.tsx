import { useEffect, useState } from 'react'
import { apiGet, apiPatch } from '../../api/http'
import type { ProgressItem, Trainee } from '../../types'
import { CATEGORIES, CATEGORY_LABELS } from '../../types'

export function TraineeProgressView() {
  const [trainees, setTrainees] = useState<Trainee[]>([])
  const [selectedTraineeId, setSelectedTraineeId] = useState<number | null>(null)
  const [progress, setProgress] = useState<ProgressItem[]>([])
  const [noteDraft, setNoteDraft] = useState('')
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
    setNoteDraft(trainees.find((t) => t.id === traineeId)?.note ?? '')
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

  const handleSaveNote = async () => {
    if (selectedTraineeId === null) return
    try {
      const updated = await apiPatch<Trainee>(`/trainees/${selectedTraineeId}`, { note: noteDraft })
      setTrainees((prev) => prev.map((t) => (t.id === updated.id ? updated : t)))
    } catch {
      setError('특이사항을 저장하지 못했습니다.')
    }
  }

  return (
    <div className="card">
      <h2>신입별 진행상황</h2>
      {error && <p className="error-text">{error}</p>}
      <select
        className="select-input"
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
        <>
          {CATEGORIES.map((category) => {
            const categoryItems = progress.filter((item) => item.category === category)
            if (categoryItems.length === 0) return null
            return (
              <div key={category} className="category-group">
                <h3 className="category-heading">{CATEGORY_LABELS[category]}</h3>
                <ul className="item-list">
                  {categoryItems.map((item) => (
                    <li
                      key={item.checklistItemId}
                      className={`item-row checkable${item.completed ? ' completed' : ''}`}
                    >
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
              </div>
            )
          })}

          <div className="category-group">
            <h3 className="category-heading">특이사항</h3>
            <textarea
              className="text-input note-textarea"
              value={noteDraft}
              onChange={(e) => setNoteDraft(e.target.value)}
              placeholder="이 신입에 대한 특이사항을 적어주세요"
              rows={3}
            />
            <button className="btn" style={{ marginTop: 8 }} onClick={handleSaveNote}>
              저장
            </button>
          </div>
        </>
      )}
    </div>
  )
}
