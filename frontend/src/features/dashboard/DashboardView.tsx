import { useEffect, useState } from 'react'
import { apiGet } from '../../api/http'
import type { TraineeSummary } from '../../types'
import { CATEGORIES, CATEGORY_LABELS } from '../../types'

export function DashboardView() {
  const [summaries, setSummaries] = useState<TraineeSummary[]>([])
  const [error, setError] = useState<string | null>(null)

  useEffect(() => {
    apiGet<TraineeSummary[]>('/dashboard/trainees')
      .then(setSummaries)
      .catch(() => setError('현황을 불러오지 못했습니다.'))
  }, [])

  return (
    <div className="card">
      <h2>전체 신입 현황</h2>
      {error && <p className="error-text">{error}</p>}
      {summaries.length === 0 ? (
        <p className="empty-state">등록된 신입이 없습니다.</p>
      ) : (
        <ul className="item-list">
          {summaries.map((summary) => {
            const percent =
              summary.totalItems === 0
                ? 0
                : Math.round((summary.completedTotal / summary.totalItems) * 100)
            return (
              <li key={summary.traineeId} className="summary-row">
                <div className="summary-header">
                  <span className="summary-name">{summary.traineeName}</span>
                  <span className="summary-percent">{percent}%</span>
                </div>
                <div className="progress-bar">
                  <div className="progress-bar-fill" style={{ width: `${percent}%` }} />
                </div>
                <div className="summary-categories">
                  {CATEGORIES.map((category) => {
                    const count = summary.byCategory[category]
                    return `${CATEGORY_LABELS[category]} ${count.completed}/${count.total}`
                  }).join(' · ')}
                </div>
              </li>
            )
          })}
        </ul>
      )}
    </div>
  )
}
