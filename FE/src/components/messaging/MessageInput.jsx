import { useState } from 'react'
import { Send } from 'lucide-react'
import client from '../../api/client'
import useAppStore from '../../store/appStore'
import { useQueryClient } from '@tanstack/react-query'

export default function MessageInput() {
  const [text, setText] = useState('')
  const channel = useAppStore((s) => s.activeChannel)
  const qc = useQueryClient()

  const send = async (e) => {
    e.preventDefault()
    if (!text.trim()) return

    const sentText = text
    setText('')

    // Optimistic update instantly
    const newMsg = {
      id: Math.random().toString(),
      sender: 'me',
      content: sentText,
      timestamp: new Date().toISOString()
    }

    qc.setQueryData(['messages', channel], (old) => {
      if (!old) return { pages: [{ messages: [newMsg], nextCursor: null }], pageParams: [] }
      return {
        ...old,
        pages: old.pages.map((p, i) => 
          i === 0 ? { ...p, messages: [...p.messages, newMsg] } : p
        )
      }
    })

    try {
      // Small timeout for mock mode so we don't hang if backend is missing
      await client.post('/messages', { channel, content: sentText }, { timeout: 1000 })
      qc.invalidateQueries(['messages', channel])
    } catch {
      // Silently fail for local mock testing
    }
  }

  return (
    <form onSubmit={send} className="p-4 border-t border-gray-700 flex gap-2">
      <input
        className="flex-1 bg-gray-800 text-white rounded-lg px-4 py-2 text-sm outline-none focus:ring-2 focus:ring-indigo-500 placeholder-gray-500"
        placeholder={`Message #${channel}`}
        value={text} 
        onChange={(e) => setText(e.target.value)} 
      />
      <button 
        type="submit" 
        disabled={!text.trim()} 
        className="bg-indigo-600 hover:bg-indigo-500 disabled:opacity-40 text-white rounded-lg px-4 py-2 transition-colors flex items-center justify-center"
      >
        <Send size={16} />
      </button>
    </form>
  )
}
