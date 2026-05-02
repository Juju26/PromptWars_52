export default function DashboardPage() {
  return (
    <div className="h-full flex flex-col justify-center items-center text-center">
      <h2 className="text-3xl font-bold text-white mb-4">Welcome to HackCoord ⚡</h2>
      <p className="text-gray-400 max-w-lg mb-8">
        Your central hub for hackathon team coordination. Use the sidebar to navigate to tasks or messaging.
      </p>
      <div className="grid grid-cols-2 gap-4">
        <div className="p-6 bg-gray-900 border border-gray-800 rounded-xl">
          <h3 className="text-xl font-bold text-indigo-400 mb-2">12</h3>
          <p className="text-sm text-gray-500">Tasks Pending</p>
        </div>
        <div className="p-6 bg-gray-900 border border-gray-800 rounded-xl">
          <h3 className="text-xl font-bold text-indigo-400 mb-2">4</h3>
          <p className="text-sm text-gray-500">Unread Messages</p>
        </div>
      </div>
    </div>
  )
}
