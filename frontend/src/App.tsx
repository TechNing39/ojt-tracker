import { ChecklistItems } from './features/checklist/ChecklistItems'
import { TraineeList } from './features/trainee/TraineeList'

function App() {
  return (
    <div>
      <h1>OJT Tracker</h1>
      <TraineeList />
      <ChecklistItems />
    </div>
  )
}

export default App
