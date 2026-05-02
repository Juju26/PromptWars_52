export default function MeshPage() {
  return (
    <div className="h-full flex flex-col justify-center items-center text-center bg-gray-900 rounded-xl border border-gray-800 p-8">
      <div className="text-indigo-500 mb-6">
        <svg xmlns="http://www.w3.org/2000/svg" width="64" height="64" viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="2" strokeLinecap="round" strokeLinejoin="round">
          <path d="M12 20h9"/>
          <path d="M16.5 3.5a2.12 2.12 0 0 1 3 3L7 19l-4 1 1-4Z"/>
        </svg>
      </div>
      <h2 className="text-3xl font-bold text-white mb-4">P2P Mesh Network</h2>
      <p className="text-xl text-amber-400 font-semibold mb-6">Coming Soon 🚀</p>
      <p className="text-gray-400 max-w-lg">
        Offline peer-to-peer payload sharing directly over LAN. Bypass venue Wi-Fi and share heavy assets like 1GB+ Docker images or ML datasets at gigabit speeds without dropping out.
      </p>
    </div>
  )
}
