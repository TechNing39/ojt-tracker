import { useEffect, useState } from 'react'
import { apiDelete, apiGet, apiPatch, apiPost } from '../../api/http'
import type { ProgressItem, Trainee } from '../../types'
import { CATEGORIES, CATEGORY_LABELS } from '../../types'
import { DashboardView } from '../dashboard/DashboardView'

export function TraineeProgressView() {
  const [trainees, setTrainees] = useState<Trainee[]>([])
  const [selectedTraineeId, setSelectedTraineeId] = useState<number | null>(null)
  const [progress, setProgress] = useState<ProgressItem[]>([])
  const [noteDraft, setNoteDraft] = useState('')
  const [newName, setNewName] = useState('')
  const [error, setError] = useState<string | null>(null)

  const loadTrainees = () => {
    apiGet<Trainee[]>('/trainees')
      .then(setTrainees)
      .catch(() => setError('신입 목록을 불러오지 못했습니다.'))
  }

  useEffect(() => {
    loadTrainees()
  }, [])

  const loadProgress = (traineeId: number) => {
    apiGet<ProgressItem[]>(`/trainees/${traineeId}/progress`)
      .then(setProgress)
      .catch(() => setError('진행상황을 불러오지 못했습니다.'))
  }

  const handleSelectRow = (traineeId: number) => {
    setSelectedTraineeId(traineeId)
    setNoteDraft(trainees.find((t) => t.id === traineeId)?.note ?? '')
    loadProgress(traineeId)
  }

  const handleAddTrainee = async () => {
    if (!newName.trim()) return
    try {
      await apiPost<Trainee>('/trainees', { name: newName.trim() })
      setNewName('')
      loadTrainees()
    } catch {
      setError('신입을 등록하지 못했습니다.')
    }
  }

  const handleDeleteTrainee = async (traineeId: number) => {
    try {
      await apiDelete(`/trainees/${traineeId}`)
      if (selectedTraineeId === traineeId) {
        setSelectedTraineeId(null)
        setProgress([])
      }
      loadTrainees()
    } catch {
      setError('신입을 삭제하지 못했습니다.')
    }
  }

  const handleToggleItem = async (checklistItemId: number) => {
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

  const selectedTrainee = trainees.find((t) => t.id === selectedTraineeId) ?? null

  return (
    <>
      <DashboardView />

      <div className="card">
        <h2>신입 명단</h2>
        {error && <p className="error-text">{error}</p>}
        {trainees.length === 0 ? (
          <p className="empty-state">아직 등록된 신입이 없습니다.</p>
        ) : (
          <ul className="item-list">
            {trainees.map((trainee) => (
              <li key={trainee.id} className="roster-item">
                <button
                  className={`roster-row${selectedTraineeId === trainee.id ? ' active' : ''}`}
                  onClick={() => handleSelectRow(trainee.id)}
                >
                  <span>{trainee.name}</span>
                </button>
                <button className="btn-ghost" onClick={() => handleDeleteTrainee(trainee.id)}>
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
            onKeyDown={(e) => e.key === 'Enter' && handleAddTrainee()}
            placeholder="신입 이름"
          />
          <button className="btn" onClick={handleAddTrainee}>
            등록
          </button>
        </div>
      </div>

      {selectedTrainee && (
        <div className="card">
          <h2>{selectedTrainee.name}님 진행상황</h2>
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
                          onChange={() => handleToggleItem(item.checklistItemId)}
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
        </div>
      )}
    </>
  )
}
