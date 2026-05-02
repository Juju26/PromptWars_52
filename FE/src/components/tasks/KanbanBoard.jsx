import KanbanColumn from './KanbanColumn'
import { useTasks } from '../../hooks/useTasks'

const COLUMNS = ['TODO', 'IN_PROGRESS', 'REVIEW', 'DONE']

export default function KanbanBoard({ teamId }) {
  const { data: tasks = [], isLoading, isError } = useTasks(teamId)

  if (isLoading) {
    return (
      <div className="flex gap-4 animate-pulse h-full">
        {[1, 2, 3, 4].map(i => (
          <div key={i} className="w-72 h-96 bg-gray-800 rounded-xl" />
        ))}
      </div>
    )
  }

  if (isError) return <p className="text-red-400 text-center mt-10">Failed to load tasks.</p>

  return (
    <div className="flex gap-4 overflow-x-auto pb-4 h-full">
      {COLUMNS.map((status) => (
        <KanbanColumn key={status} status={status} tasks={tasks.filter((t) => t.status === status)} />
      ))}
    </div>
  )
}
