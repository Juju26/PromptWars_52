import { useEffect, useRef } from 'react'
import { useMessages } from '../../hooks/useMessages'
import useAppStore from '../../store/appStore'

export default function MessageFeed() {
  const channel = useAppStore((s) => s.activeChannel)
  const bottomRef = useRef(null)
  const { data, isLoading, fetchNextPage, hasNextPage } = useMessages(channel)
  
  const messages = data?.pages?.flatMap((p) => p?.messages || []) ?? []

  useEffect(() => { 
    bottomRef.current?.scrollIntoView({ behavior: 'smooth' })
  }, [messages.length])

  if (isLoading) {
    return (
      <div className="p-4 space-y-4 animate-pulse">
        {[1, 2, 3].map(i => (
          <div key={i} className="flex gap-3">
            <div className="w-8 h-8 bg-gray-700 rounded-full"/>
            <div className="h-8 bg-gray-800 rounded-lg w-2/3"/>
          </div>
        ))}
      </div>
    )
  }

  return (
    <div className="flex-1 overflow-y-auto p-4 space-y-3">
      {hasNextPage && (
        <button onClick={fetchNextPage} className="w-full text-xs text-blue-400 hover:underline text-center">
          Load older messages
        </button>
      )}
      {messages.map((msg) => (
        <div key={msg.id} className="flex gap-3">
          <div className="w-8 h-8 rounded-full bg-indigo-600 flex-shrink-0 flex items-center justify-center text-xs font-bold text-white">
            {msg.sender?.[0]?.toUpperCase()}
          </div>
          <div>
            <p className="text-xs text-gray-400 mb-0.5">
              <span className="text-gray-200 font-medium">{msg.sender}</span> · {new Date(msg.timestamp).toLocaleTimeString()}
            </p>
            <p className="text-sm text-gray-100 bg-gray-800 rounded-lg px-3 py-2 inline-block">
              {msg.content}
            </p>
          </div>
        </div>
      ))}
      <div ref={bottomRef} />
    </div>
  )
}
