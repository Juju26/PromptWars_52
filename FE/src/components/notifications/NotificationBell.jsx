import { Bell } from 'lucide-react'
import { useState } from 'react'
import useAppStore from '../../store/appStore'

export default function NotificationBell() {
  const [open, setOpen] = useState(false)
  const { notifications, clearNotifications } = useAppStore()

  return (
    <div className="relative">
      <button 
        onClick={() => setOpen((o) => !o)} 
        className="relative p-2 rounded-lg hover:bg-gray-700 text-gray-300 transition-colors"
      >
        <Bell size={20} />
        {notifications.length > 0 && <span className="absolute top-1 right-1 w-2 h-2 bg-red-500 rounded-full" />}
      </button>
      {open && (
        <div className="absolute right-0 mt-2 w-80 bg-gray-800 border border-gray-700 rounded-xl shadow-2xl z-50 overflow-hidden">
          <div className="flex items-center justify-between px-4 py-3 border-b border-gray-700">
            <span className="text-sm font-semibold text-white">Notifications</span>
            <button onClick={clearNotifications} className="text-xs text-gray-400 hover:text-white">Clear all</button>
          </div>
          <div className="max-h-64 overflow-y-auto divide-y divide-gray-700">
            {notifications.length === 0 ? (
              <p className="text-xs text-gray-500 text-center py-6">No new notifications</p>
            ) : notifications.map((n, i) => (
              <div key={i} className="px-4 py-3">
                <p className="text-sm text-white">{n.message}</p>
                <p className="text-xs text-gray-500 mt-0.5">{new Date(n.timestamp).toLocaleTimeString()}</p>
              </div>
            ))}
          </div>
        </div>
      )}
    </div>
  )
}
