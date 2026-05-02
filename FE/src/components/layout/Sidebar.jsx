import { NavLink } from 'react-router-dom'
import { LayoutDashboard, Trello, MessageSquare, Radio } from 'lucide-react'

const NAV = [
  { to: '/', icon: LayoutDashboard, label: 'Dashboard' },
  { to: '/kanban', icon: Trello, label: 'Kanban' },
  { to: '/messaging', icon: MessageSquare, label: 'Messaging' },
  { to: '/mesh', icon: Radio, label: 'Mesh Share' },
]

export default function Sidebar() {
  return (
    <aside className="w-56 flex-shrink-0 bg-gray-900 border-r border-gray-800 flex flex-col py-4">
      <div className="px-4 mb-6">
        <h1 className="text-lg font-bold text-indigo-400 tracking-tight">⚡ HackCoord</h1>
        <p className="text-xs text-gray-500">Hackathon Platform</p>
      </div>
      <nav className="flex-1 space-y-1 px-2">
        {NAV.map(({ to, icon: Icon, label }) => (
          <NavLink 
            key={to} 
            to={to} 
            end 
            className={({ isActive }) => `flex items-center gap-3 px-3 py-2 rounded-lg text-sm transition-colors ${isActive ? 'bg-indigo-600 text-white' : 'text-gray-400 hover:bg-gray-800 hover:text-white'}`}
          >
            <Icon size={16} />{label}
          </NavLink>
        ))}
      </nav>
    </aside>
  )
}
