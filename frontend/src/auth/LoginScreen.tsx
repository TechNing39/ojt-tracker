import { useState } from 'react'
import { useAuth } from './AuthContext'
import { SITES } from '../types'
import type { SiteCode } from '../types'

// TODO: 당분간 중계만 사용하는 임시 기간이라 PIN을 화면에 노출해둠. 다른 지점도 실사용 시작하면 이 힌트는 지워야 함.
const TEMP_PIN_HINTS: Record<SiteCode, string> = {
  JUNGGYE: '1111',
  SANGBONG: '2222',
  BANGHAK: '3333',
  SUYU: '4444',
  MIA: '5555',
}
const TEMP_ADMIN_PIN_HINT = '0000'

export function LoginScreen() {
  const { login, loginAdmin } = useAuth()
  const [mode, setMode] = useState<'site' | 'admin'>('site')
  const [selectedSite, setSelectedSite] = useState<SiteCode | null>(null)
  const [pin, setPin] = useState('')
  const [error, setError] = useState<string | null>(null)
  const [isSubmitting, setIsSubmitting] = useState(false)

  const handleBack = () => {
    setSelectedSite(null)
    setPin('')
    setError(null)
  }

  const handleSubmit = async () => {
    if (!pin.trim()) return
    setIsSubmitting(true)
    setError(null)
    try {
      if (mode === 'site' && selectedSite) {
        await login(selectedSite, pin.trim())
      } else if (mode === 'admin') {
        await loginAdmin(pin.trim())
      }
    } catch {
      setError('PIN이 올바르지 않습니다.')
      setPin('')
    } finally {
      setIsSubmitting(false)
    }
  }

  return (
    <div className="app-main" style={{ paddingBottom: 16 }}>
      <div className="card">
        <h2>OJT Tracker 로그인</h2>

        {mode === 'site' && !selectedSite && (
          <>
            <p className="empty-state">지점을 선택하세요.</p>
            <ul className="item-list">
              {SITES.map((site) => (
                <li key={site.code} className="roster-item">
                  <button className="roster-row" onClick={() => setSelectedSite(site.code)}>
                    <span>{site.name}</span>
                  </button>
                </li>
              ))}
            </ul>
            <div className="field-row">
              <button className="btn-ghost" onClick={() => setMode('admin')}>
                관리자로 로그인
              </button>
            </div>
          </>
        )}

        {mode === 'site' && selectedSite && (
          <>
            <p className="empty-state">{SITES.find((s) => s.code === selectedSite)?.name} 지점 PIN을 입력하세요.</p>
            {error && <p className="error-text">{error}</p>}
            <div className="field-row">
              <input
                className="text-input"
                type="password"
                inputMode="numeric"
                value={pin}
                onChange={(e) => setPin(e.target.value)}
                onKeyDown={(e) => e.key === 'Enter' && handleSubmit()}
                placeholder="PIN"
                autoFocus
              />
              <button className="btn" onClick={handleSubmit} disabled={isSubmitting}>
                로그인
              </button>
            </div>
            <p className="empty-state">(임시) PIN: {TEMP_PIN_HINTS[selectedSite]}</p>
            <div className="field-row">
              <button className="btn-ghost" onClick={handleBack}>
                다른 지점 선택
              </button>
            </div>
          </>
        )}

        {mode === 'admin' && (
          <>
            <p className="empty-state">관리자 PIN을 입력하세요.</p>
            {error && <p className="error-text">{error}</p>}
            <div className="field-row">
              <input
                className="text-input"
                type="password"
                inputMode="numeric"
                value={pin}
                onChange={(e) => setPin(e.target.value)}
                onKeyDown={(e) => e.key === 'Enter' && handleSubmit()}
                placeholder="관리자 PIN"
                autoFocus
              />
              <button className="btn" onClick={handleSubmit} disabled={isSubmitting}>
                로그인
              </button>
            </div>
            <p className="empty-state">(임시) PIN: {TEMP_ADMIN_PIN_HINT}</p>
            <div className="field-row">
              <button
                className="btn-ghost"
                onClick={() => {
                  setMode('site')
                  handleBack()
                }}
              >
                지점 직원으로 로그인
              </button>
            </div>
          </>
        )}
      </div>
    </div>
  )
}
