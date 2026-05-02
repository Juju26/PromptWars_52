import NotificationBell from '../notifications/NotificationBell'
import useAppStore from '../../store/appStore'

export default function Topbar() {
  const user = useAppStore((s) => s.user)
  
  return (
    <header className="h-14 flex-shrink-0 flex items-center justify-between px-6 border-b border-gray-800 bg-gray-900">
      <span className="text-sm text-gray-400">
        Welcome back, <span className="text-white font-medium">{user?.name ?? 'Hacker'}</span>
      </span>
      <div className="flex items-center gap-3">
        <NotificationBell />
        <div className="w-8 h-8 rounded-full bg-indigo-600 flex items-center justify-center text-sm font-bold">
          {user?.name?.[0]?.toUpperCase() ?? 'H'}
        </div>
      </div>
    </header>
  )
}
