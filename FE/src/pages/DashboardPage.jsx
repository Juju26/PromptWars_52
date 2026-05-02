import { Search, User, Activity, Clock } from 'lucide-react'
import { useState } from 'react'

export default function DashboardPage() {
  const [search, setSearch] = useState('')

  const users = ['Alice (Frontend)', 'Bob (Backend)', 'Carol (DevOps)', 'Dave (Designer)'].filter(
    (u) => u.toLowerCase().includes(search.toLowerCase())
  )

  return (
    <div className="h-full flex flex-col space-y-6">
      <div className="bg-gray-900 border border-gray-800 rounded-xl p-8">
        <h2 className="text-3xl font-bold text-white mb-2">Welcome to HackCoord ⚡</h2>
        <p className="text-gray-400 max-w-2xl">
          Your central hub for hackathon team coordination. Manage tasks, coordinate offline, and stay synced.
        </p>
      </div>

      <div className="grid grid-cols-1 md:grid-cols-3 gap-6">
        <div className="p-6 bg-gray-900 border border-gray-800 rounded-xl flex items-center gap-4">
          <div className="p-3 bg-indigo-900/50 rounded-lg text-indigo-400">
            <Activity size={24} />
          </div>
          <div>
            <h3 className="text-2xl font-bold text-white">12</h3>
            <p className="text-sm text-gray-500">Tasks Pending</p>
          </div>
        </div>
        <div className="p-6 bg-gray-900 border border-gray-800 rounded-xl flex items-center gap-4">
          <div className="p-3 bg-amber-900/50 rounded-lg text-amber-400">
            <Clock size={24} />
          </div>
          <div>
            <h3 className="text-2xl font-bold text-white">2.5 hrs</h3>
            <p className="text-sm text-gray-500">Till Submission</p>
          </div>
        </div>
        <div className="p-6 bg-gray-900 border border-gray-800 rounded-xl flex items-center gap-4">
          <div className="p-3 bg-green-900/50 rounded-lg text-green-400">
            <User size={24} />
          </div>
          <div>
            <h3 className="text-2xl font-bold text-white">4</h3>
            <p className="text-sm text-gray-500">Team Members Online</p>
          </div>
        </div>
      </div>

      <div className="bg-gray-900 border border-gray-800 rounded-xl p-6 flex-1 flex flex-col min-h-[300px]">
        <h3 className="text-lg font-bold text-white mb-4">Participant Directory</h3>
        <div className="relative mb-6">
          <Search className="absolute left-3 top-2.5 text-gray-500" size={18} />
          <input
            type="text"
            placeholder="Search users by name or role..."
            value={search}
            onChange={(e) => setSearch(e.target.value)}
            className="w-full bg-gray-800 text-white rounded-lg pl-10 pr-4 py-2 text-sm outline-none focus:ring-2 focus:ring-indigo-500"
          />
        </div>
        <div className="flex-1 overflow-y-auto space-y-2 pr-2">
          {users.map((u) => (
            <div key={u} className="flex items-center justify-between p-3 bg-gray-800/50 rounded-lg border border-gray-800 hover:bg-gray-800 transition-colors cursor-pointer">
              <div className="flex items-center gap-3">
                <div className="w-8 h-8 rounded-full bg-indigo-600 flex items-center justify-center text-xs font-bold text-white">
                  {u[0]}
                </div>
                <span className="text-sm font-medium text-gray-200">{u}</span>
              </div>
              <span className="text-xs px-2 py-1 bg-green-900/30 text-green-400 rounded-full">Online</span>
            </div>
          ))}
          {users.length === 0 && (
            <p className="text-sm text-gray-500 text-center mt-8">No users found matching "{search}"</p>
          )}
        </div>
      </div>
    </div>
  )
}
