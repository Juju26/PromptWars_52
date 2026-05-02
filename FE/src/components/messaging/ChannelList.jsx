import useAppStore from '../../store/appStore'
import { Hash } from 'lucide-react'

const CHANNELS = ['general', 'engineering', 'design', 'announcements']

export default function ChannelList() {
  const { activeChannel, setActiveChannel } = useAppStore()

  return (
    <div className="w-64 bg-gray-900 border border-gray-800 rounded-xl overflow-hidden flex flex-col">
      <div className="p-4 border-b border-gray-800">
        <h2 className="text-sm font-bold text-gray-300 uppercase tracking-wider">Channels</h2>
      </div>
      <div className="flex-1 overflow-y-auto p-2 space-y-1">
        {CHANNELS.map(ch => (
          <button
            key={ch}
            onClick={() => setActiveChannel(ch)}
            className={`w-full flex items-center gap-2 px-3 py-2 rounded-lg text-sm transition-colors ${
              activeChannel === ch 
                ? 'bg-indigo-600 text-white' 
                : 'text-gray-400 hover:bg-gray-800 hover:text-white'
            }`}
          >
            <Hash size={16} className={activeChannel === ch ? 'text-indigo-200' : 'text-gray-500'} />
            {ch}
          </button>
        ))}
      </div>
    </div>
  )
}
