import { ChecklistItems } from './features/checklist/ChecklistItems'
import { TraineeProgressView } from './features/progress/TraineeProgressView'
import { TraineeList } from './features/trainee/TraineeList'

function App() {
  return (
    <div>
      <h1>OJT Tracker</h1>
      <TraineeList />
      <ChecklistItems />
      <TraineeProgressView />
    </div>
  )
}

export default App
