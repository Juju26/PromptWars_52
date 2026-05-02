import { useInfiniteQuery } from '@tanstack/react-query'
import client from '../api/client'

// Mock messages for development
const MOCK_MESSAGES = {
  general: [
    { id: '1', sender: 'system', content: 'Welcome to the hackathon!', timestamp: new Date(Date.now() - 3600000).toISOString() },
    { id: '2', sender: 'admin', content: 'Food is here!', timestamp: new Date(Date.now() - 1800000).toISOString() }
  ],
  engineering: [
    { id: '1', sender: 'dev1', content: 'API is up.', timestamp: new Date(Date.now() - 3600000).toISOString() }
  ]
}

const fetchMessages = async (channel, pageParam) => {
  try {
    const url = `/messaging/messages?channel=${channel}${pageParam ? `&before=${pageParam}` : ''}`
    const res = await client.get(url)
    
    // If we receive an HTML response (e.g. from Vite dev server fallback) or invalid format, throw to fallback
    if (typeof res.data === 'string' || !res.data) {
      throw new Error('Invalid response format')
    }
    
    return res.data
  } catch {
    // Fall back to mock
    const msgs = MOCK_MESSAGES[channel] || []
    return { messages: msgs, nextCursor: null }
  }
}

export const useMessages = (channel) =>
  useInfiniteQuery({
    queryKey: ['messages', channel],
    queryFn: ({ pageParam = null }) => fetchMessages(channel, pageParam),
    getNextPageParam: (last) => last.nextCursor ?? undefined,
    initialPageParam: null
  })
