import KanbanBoard from '../components/tasks/KanbanBoard'
import useAppStore from '../store/appStore'

export default function KanbanPage() {
  const user = useAppStore((s) => s.user)
  
  return (
    <div className="h-full flex flex-col">
      <h2 className="text-xl font-bold text-white mb-4">Task Board</h2>
      <KanbanBoard teamId={user?.teamId ?? 'default'} />
    </div>
  )
}
