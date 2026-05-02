import { useState } from 'react'
import { Send } from 'lucide-react'
import client from '../../api/client'
import useAppStore from '../../store/appStore'
import { useQueryClient } from '@tanstack/react-query'

export default function MessageInput() {
  const [text, setText] = useState('')
  const [sending, setSending] = useState(false)
  const channel = useAppStore((s) => s.activeChannel)
  const qc = useQueryClient()

  const send = async (e) => {
    e.preventDefault()
    if (!text.trim()) return
    setSending(true)
    try {
      await client.post('/messages', { channel, content: text })
      setText('')
      qc.invalidateQueries(['messages', channel])
    } catch {
      // Mock optimistic update for local testing
      qc.setQueryData(['messages', channel], (old) => {
        if (!old) return old
        const newMsg = {
          id: Math.random().toString(),
          sender: 'me',
          content: text,
          timestamp: new Date().toISOString()
        }
        return {
          ...old,
          pages: old.pages.map((p, i) => 
            i === 0 ? { ...p, messages: [...p.messages, newMsg] } : p
          )
        }
      })
      setText('')
    } finally { 
      setSending(false) 
    }
  }

  return (
    <form onSubmit={send} className="p-4 border-t border-gray-700 flex gap-2">
      <input
        className="flex-1 bg-gray-800 text-white rounded-lg px-4 py-2 text-sm outline-none focus:ring-2 focus:ring-indigo-500 placeholder-gray-500"
        placeholder={`Message #${channel}`}
        value={text} 
        onChange={(e) => setText(e.target.value)} 
        disabled={sending}
      />
      <button 
        type="submit" 
        disabled={sending || !text.trim()} 
        className="bg-indigo-600 hover:bg-indigo-500 disabled:opacity-40 text-white rounded-lg px-4 py-2 transition-colors flex items-center justify-center"
      >
        <Send size={16} />
      </button>
    </form>
  )
}
