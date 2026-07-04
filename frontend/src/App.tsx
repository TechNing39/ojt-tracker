import { useState } from 'react'
import { ChecklistItems } from './features/checklist/ChecklistItems'
import { TraineeProgressView } from './features/progress/TraineeProgressView'

type Tab = 'progress' | 'checklist'

const TABS: { key: Tab; icon: string; label: string }[] = [
  { key: 'progress', icon: '✅', label: '진행상황' },
  { key: 'checklist', icon: '📋', label: '체크리스트' },
]

function App() {
  const [tab, setTab] = useState<Tab>('progress')

  return (
    <>
      <header className="app-header">
        <h1>OJT Tracker</h1>
      </header>

      <main className="app-main">
        {tab === 'progress' && <TraineeProgressView />}
        {tab === 'checklist' && <ChecklistItems />}
      </main>

      <nav className="app-tabbar">
        {TABS.map((t) => (
          <button
            key={t.key}
            className={`app-tab${tab === t.key ? ' active' : ''}`}
            onClick={() => setTab(t.key)}
          >
            <span className="app-tab-icon">{t.icon}</span>
            <span className="app-tab-label">{t.label}</span>
          </button>
        ))}
      </nav>
    </>
  )
}

export default App
