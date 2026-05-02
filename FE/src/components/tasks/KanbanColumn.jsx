import TaskCard from './TaskCard'

const STATUS_COLORS = {
  TODO: 'border-gray-500',
  IN_PROGRESS: 'border-yellow-500',
  REVIEW: 'border-blue-500',
  DONE: 'border-green-500',
}

export default function KanbanColumn({ status, tasks }) {
  return (
    <div className={`flex-shrink-0 w-72 rounded-xl border-t-4 bg-gray-900 p-3 ${STATUS_COLORS[status] || STATUS_COLORS.TODO}`}>
      <div className="flex items-center justify-between mb-3">
        <h3 className="text-sm font-semibold text-gray-300 uppercase tracking-wider">{status.replace('_', ' ')}</h3>
        <span className="text-xs bg-gray-700 text-gray-400 rounded-full px-2 py-0.5">{tasks.length}</span>
      </div>
      <div className="space-y-2">
        {tasks.map((task) => <TaskCard key={task.id} task={task} />)}
      </div>
    </div>
  )
}
