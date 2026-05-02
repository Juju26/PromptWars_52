import { useQuery, useMutation, useQueryClient } from '@tanstack/react-query'
import client from '../api/client'

// --- Mock data for local dev without a backend ---
const MOCK_TASKS = [
  { id: '1', title: 'Set up CI/CD pipeline', status: 'TODO',        priority: 'HIGH',   assignee: 'Alice' },
  { id: '2', title: 'Design Kanban UI',       status: 'IN_PROGRESS', priority: 'HIGH',   assignee: 'Bob'   },
  { id: '3', title: 'Write API contracts',    status: 'IN_PROGRESS', priority: 'MEDIUM', assignee: 'Carol' },
  { id: '4', title: 'Configure Pub/Sub',      status: 'REVIEW',      priority: 'MEDIUM', assignee: 'Dave'  },
  { id: '5', title: 'Deploy to Cloud Run',    status: 'TODO',        priority: 'LOW',    assignee: 'Eve'   },
  { id: '6', title: 'JWT offline validation', status: 'DONE',        priority: 'HIGH',   assignee: 'Alice' },
  { id: '7', title: 'mDNS peer discovery',    status: 'DONE',        priority: 'MEDIUM', assignee: 'Bob'   },
]

const fetchTasks = async (teamId) => {
  try {
    const res = await client.get(`/tasks?teamId=${teamId}`)
    return res.data
  } catch {
    // Fall back to mock data when backend is unavailable
    return MOCK_TASKS
  }
}

export const useTasks = (teamId) =>
  useQuery({
    queryKey: ['tasks', teamId],
    queryFn: () => fetchTasks(teamId),
    staleTime: 30_000,
    refetchInterval: 15_000,
  })

export const useUpdateTask = () => {
  const qc = useQueryClient()
  return useMutation({
    mutationFn: async ({ id, patch }) => {
      try {
        const res = await client.patch(`/tasks/${id}`, patch, { timeout: 1000 })
        return res.data
      } catch {
        return { id, ...patch }
      }
    },
    onMutate: async ({ id, patch, teamId }) => {
      await qc.cancelQueries(['tasks', teamId])
      const previous = qc.getQueryData(['tasks', teamId])
      qc.setQueryData(['tasks', teamId], (old = []) =>
        old.map((t) => (t.id === id ? { ...t, ...patch } : t))
      )
      return { previous }
    },
    onError: (_err, { teamId }, ctx) => {
      qc.setQueryData(['tasks', teamId], ctx.previous)
    },
    onSettled: (_data, _err, { teamId }) => {
      qc.invalidateQueries(['tasks', teamId])
    },
  })
}

export const useCreateTask = () => {
  const qc = useQueryClient()
  return useMutation({
    mutationFn: async (newTask) => {
      try {
        const res = await client.post(`/tasks`, newTask, { timeout: 1000 })
        return res.data
      } catch {
        return { id: Math.random().toString(), ...newTask }
      }
    },
    onMutate: async (newTask) => {
      await qc.cancelQueries(['tasks', newTask.teamId])
      const previous = qc.getQueryData(['tasks', newTask.teamId])
      qc.setQueryData(['tasks', newTask.teamId], (old = []) => [
        ...old,
        { id: Math.random().toString(), ...newTask },
      ])
      return { previous }
    },
    onError: (_err, { teamId }, ctx) => {
      qc.setQueryData(['tasks', teamId], ctx.previous)
    },
    onSettled: (_data, _err, { teamId }) => {
      qc.invalidateQueries(['tasks', teamId])
    },
  })
}
