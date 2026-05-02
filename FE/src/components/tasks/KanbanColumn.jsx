import TaskCard from './TaskCard'
import { Plus } from 'lucide-react'

const STATUS_COLORS = {
  TODO: 'border-gray-500',
  IN_PROGRESS: 'border-yellow-500',
  REVIEW: 'border-blue-500',
  DONE: 'border-green-500',
}

export default function KanbanColumn({ status, tasks, onDropTask, onTaskClick, onAddTask }) {
  const handleDragOver = (e) => {
    e.preventDefault()
  }

  const handleDrop = (e) => {
    e.preventDefault()
    const taskId = e.dataTransfer.getData('taskId')
    if (taskId && onDropTask) {
      onDropTask(taskId, status)
    }
  }

  return (
    <div 
      onDragOver={handleDragOver}
      onDrop={handleDrop}
      className={`flex-shrink-0 w-72 flex flex-col rounded-xl border-t-4 bg-gray-900 p-3 ${STATUS_COLORS[status] || STATUS_COLORS.TODO}`}
    >
      <div className="flex items-center justify-between mb-3">
        <div className="flex items-center gap-2">
          <h3 className="text-sm font-semibold text-gray-300 uppercase tracking-wider">{status.replace('_', ' ')}</h3>
          <span className="text-xs bg-gray-700 text-gray-400 rounded-full px-2 py-0.5">{tasks.length}</span>
        </div>
        <button 
          onClick={() => onAddTask(status)}
          className="text-gray-400 hover:text-white p-1 rounded-md hover:bg-gray-800 transition-colors"
        >
          <Plus size={16} />
        </button>
      </div>
      <div className="space-y-2 flex-1 overflow-y-auto">
        {tasks.map((task) => (
          <TaskCard key={task.id} task={task} onClick={onTaskClick} />
        ))}
      </div>
    </div>
  )
}
