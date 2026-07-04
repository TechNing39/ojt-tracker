import { useEffect, useState } from 'react'

function App() {
  const [status, setStatus] = useState<string>('loading...')

  useEffect(() => {
    fetch('http://localhost:8080/api/health')
      .then((res) => res.json())
      .then((data) => setStatus(data.status))
      .catch(() => setStatus('error: backend에 연결할 수 없습니다'))
  }, [])

  return (
    <div>
      <h1>OJT Tracker</h1>
      <p>backend status: {status}</p>
    </div>
  )
}

export default App
