const PRIORITY_BADGE = {
  HIGH:   'bg-red-900 text-red-300',
  MEDIUM: 'bg-yellow-900 text-yellow-300',
  LOW:    'bg-green-900 text-green-300',
}

export default function TaskCard({ task }) {
  return (
    <div className="bg-gray-800 rounded-lg p-3 cursor-pointer hover:bg-gray-700 transition-colors">
      <p className="text-sm text-white font-medium mb-2 line-clamp-2">{task.title}</p>
      <div className="flex items-center justify-between">
        <span className={`text-xs px-2 py-0.5 rounded-full font-medium ${PRIORITY_BADGE[task.priority] || 'bg-gray-700 text-gray-400'}`}>
          {task.priority}
        </span>
        {task.assignee && <span className="text-xs text-gray-400">{task.assignee}</span>}
      </div>
    </div>
  )
}
