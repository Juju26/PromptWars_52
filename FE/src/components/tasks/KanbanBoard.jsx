import { useState } from 'react'
import KanbanColumn from './KanbanColumn'
import TaskModal from './TaskModal'
import { useTasks, useUpdateTask, useCreateTask } from '../../hooks/useTasks'

const COLUMNS = ['TODO', 'IN_PROGRESS', 'REVIEW', 'DONE']

export default function KanbanBoard({ teamId }) {
  const { data: tasks = [], isLoading, isError } = useTasks(teamId)
  const { mutate: updateTask } = useUpdateTask()
  const { mutate: createTask } = useCreateTask()

  const [modalOpen, setModalOpen] = useState(false)
  const [selectedTask, setSelectedTask] = useState(null)
  const [newTaskStatus, setNewTaskStatus] = useState(null)

  const handleDropTask = (taskId, newStatus) => {
    const task = tasks.find((t) => t.id === taskId)
    if (task && task.status !== newStatus) {
      updateTask({ id: taskId, patch: { status: newStatus }, teamId })
    }
  }

  const handleTaskClick = (task) => {
    setSelectedTask(task)
    setNewTaskStatus(null)
    setModalOpen(true)
  }

  const handleAddTask = (status) => {
    setSelectedTask(null)
    setNewTaskStatus(status)
    setModalOpen(true)
  }

  const handleSaveTask = (taskData) => {
    if (taskData.id) {
      // Edit existing
      updateTask({ id: taskData.id, patch: taskData, teamId })
    } else {
      // Create new
      createTask({ ...taskData, teamId })
    }
    setModalOpen(false)
  }

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
    <div className="flex gap-4 overflow-x-auto pb-4 h-full relative">
      {COLUMNS.map((status) => (
        <KanbanColumn 
          key={status} 
          status={status} 
          tasks={tasks.filter((t) => t.status === status)} 
          onDropTask={handleDropTask}
          onTaskClick={handleTaskClick}
          onAddTask={handleAddTask}
        />
      ))}
      {modalOpen && (
        <TaskModal 
          task={selectedTask} 
          status={newTaskStatus} 
          onClose={() => setModalOpen(false)} 
          onSave={handleSaveTask} 
        />
      )}
    </div>
  )
}
