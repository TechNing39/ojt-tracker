import { useEffect, useState } from 'react'
import { apiDelete, apiGet, apiPatch, apiPost } from '../../api/http'
import type { Category, ProgressItem, Trainee } from '../../types'
import { CATEGORIES, CATEGORY_LABELS } from '../../types'

export function TraineeProgressView() {
  const [trainees, setTrainees] = useState<Trainee[]>([])
  const [selectedTraineeId, setSelectedTraineeId] = useState<number | null>(null)
  const [progress, setProgress] = useState<ProgressItem[]>([])
  const [noteDraft, setNoteDraft] = useState('')
  const [newName, setNewName] = useState('')
  const [error, setError] = useState<string | null>(null)
  const [expandedCategories, setExpandedCategories] = useState<Set<Category>>(new Set())

  const loadProgress = (traineeId: number) => {
    apiGet<ProgressItem[]>(`/trainees/${traineeId}/progress`)
      .then(setProgress)
      .catch(() => setError('진행상황을 불러오지 못했습니다.'))
  }

  const handleSelectRow = (traineeId: number, trainee?: Trainee) => {
    setSelectedTraineeId(traineeId)
    setNoteDraft((trainee ?? trainees.find((t) => t.id === traineeId))?.note ?? '')
    setExpandedCategories(new Set())
    loadProgress(traineeId)
  }

  const toggleCategory = (category: Category) => {
    setExpandedCategories((prev) => {
      const next = new Set(prev)
      if (next.has(category)) {
        next.delete(category)
      } else {
        next.add(category)
      }
      return next
    })
  }

  const loadTrainees = () => {
    apiGet<Trainee[]>('/trainees')
      .then(setTrainees)
      .catch(() => setError('신입 목록을 불러오지 못했습니다.'))
  }

  useEffect(() => {
    apiGet<Trainee[]>('/trainees')
      .then((data) => {
        setTrainees(data)
        if (data.length > 0) {
          handleSelectRow(data[0].id, data[0])
        }
      })
      .catch(() => setError('신입 목록을 불러오지 못했습니다.'))
    // eslint-disable-next-line react-hooks/exhaustive-deps
  }, [])

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

  const handleDeleteTrainee = async (traineeId: number, traineeName: string) => {
    if (!window.confirm(`${traineeName}님을 삭제할까요? 진행상황 기록도 함께 사라지며 되돌릴 수 없습니다.`)) return
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

  useEffect(() => {
    if (selectedTraineeId === null) return
    const timeoutId = setTimeout(() => {
      apiPatch<Trainee>(`/trainees/${selectedTraineeId}`, { note: noteDraft })
        .then((updated) => setTrainees((prev) => prev.map((t) => (t.id === updated.id ? updated : t))))
        .catch(() => setError('특이사항을 저장하지 못했습니다.'))
    }, 600)
    return () => clearTimeout(timeoutId)
  }, [noteDraft, selectedTraineeId])

  const selectedTrainee = trainees.find((t) => t.id === selectedTraineeId) ?? null

  return (
    <>
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
                <button
                  className="btn-ghost"
                  onClick={() => handleDeleteTrainee(trainee.id, trainee.name)}
                >
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

      {trainees.length > 0 && !selectedTrainee && (
        <div className="card">
          <p className="empty-state">위 명단에서 신입을 선택하세요.</p>
        </div>
      )}

      {selectedTrainee && (
        <div className="card">
          <h2>{selectedTrainee.name}님 진행상황</h2>
          {CATEGORIES.map((category) => {
            const categoryItems = progress.filter((item) => item.category === category)
            if (categoryItems.length === 0) return null
            const expanded = expandedCategories.has(category)
            return (
              <div key={category} className="category-group">
                <button
                  type="button"
                  className="category-toggle"
                  aria-expanded={expanded}
                  onClick={() => toggleCategory(category)}
                >
                  <span className="category-heading">{CATEGORY_LABELS[category]}</span>
                  <span className={`category-arrow${expanded ? ' expanded' : ''}`}>▾</span>
                </button>
                {expanded && (
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
                )}
              </div>
            )
          })}

          <div className="category-group">
            <h3 className="category-heading">특이사항</h3>
            <textarea
              className="text-input note-textarea"
              value={noteDraft}
              onChange={(e) => setNoteDraft(e.target.value)}
              placeholder="이 신입에 대한 특이사항을 적어주세요 (자동 저장됩니다)"
              rows={3}
            />
          </div>
        </div>
      )}
    </>
  )
}
