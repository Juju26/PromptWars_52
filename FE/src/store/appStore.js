import { create } from 'zustand'

const useAppStore = create((set) => ({
  user: null,
  isMeshMode: false,
  activeChannel: 'general',
  notifications: [],

  setUser: (user) => set({ user }),
  setMeshMode: (val) => set({ isMeshMode: val }),
  setActiveChannel: (ch) => set({ activeChannel: ch }),
  addNotification: (n) =>
    set((state) => ({
      notifications: [n, ...state.notifications].slice(0, 50),
    })),
  clearNotifications: () => set({ notifications: [] }),
}))

export default useAppStore
