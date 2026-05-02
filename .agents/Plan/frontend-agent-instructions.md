# Subagent 1: React UI Expert — Full Implementation Guide
# Hackathon Team Coordination Platform — Frontend

---

## 🧭 Role & Mission

You are an expert React Developer. Build the complete **frontend** for a 500-person hackathon coordination platform. The UI must be fast, highly responsive, and handle heavy async state gracefully. The stack is **Vite + React 18 + Tailwind CSS + React Query + Zustand**.

---

## 🛠️ Environment Setup

```bash
npm create vite@latest frontend -- --template react
cd frontend
npm install
npm install -D tailwindcss postcss autoprefixer
npx tailwindcss init -p
npm install @tanstack/react-query axios zustand react-router-dom lucide-react react-hot-toast
```

**`tailwind.config.js`** — add dark mode + content paths:
```js
export default {
  darkMode: 'class',
  content: ['./index.html', './src/**/*.{js,jsx}'],
  theme: { extend: {} },
  plugins: [],
}
```

**`src/index.css`** — base directives:
```css
@tailwind base;
@tailwind components;
@tailwind utilities;
```

---

## 📁 Project File Structure

```
src/
├── api/
│   └── client.js
├── components/
│   ├── layout/
│   │   ├── Sidebar.jsx
│   │   ├── Topbar.jsx
│   │   └── AppShell.jsx
│   ├── tasks/
│   │   ├── KanbanBoard.jsx
│   │   ├── KanbanColumn.jsx
│   │   └── TaskCard.jsx
│   ├── messaging/
│   │   ├── ChannelList.jsx
│   │   ├── MessageFeed.jsx
│   │   └── MessageInput.jsx
│   ├── notifications/
│   │   └── NotificationBell.jsx
│   └── mesh/
│       └── MeshStatusBanner.jsx
├── hooks/
│   ├── useTasks.js
│   ├── useMessages.js
│   └── useMeshStatus.js
├── store/
│   └── appStore.js
├── pages/
│   ├── DashboardPage.jsx
│   ├── KanbanPage.jsx
│   ├── MessagingPage.jsx
│   └── LoginPage.jsx
├── App.jsx
└── main.jsx
```

---

## 🔌 API Client (`src/api/client.js`)

```js
import axios from 'axios';

const API_BASE = import.meta.env.VITE_API_URL || 'http://localhost:8080/api';

const client = axios.create({ baseURL: API_BASE, timeout: 10000 });

client.interceptors.request.use((config) => {
  const token = localStorage.getItem('jwt_token');
  if (token) config.headers.Authorization = `Bearer ${token}`;
  return config;
});

client.interceptors.response.use(
  (res) => res,
  (err) => {
    if (err.response?.status === 401) {
      localStorage.removeItem('jwt_token');
      window.location.href = '/login';
    }
    return Promise.reject(err);
  }
);

export default client;
```

---

## 🗂️ Global State (`src/store/appStore.js`)

```js
import { create } from 'zustand';

const useAppStore = create((set) => ({
  user: null,
  isMeshMode: false,
  activeChannel: 'general',
  notifications: [],

  setUser: (user) => set({ user }),
  setMeshMode: (val) => set({ isMeshMode: val }),
  setActiveChannel: (ch) => set({ activeChannel: ch }),
  addNotification: (n) =>
    set((state) => ({ notifications: [n, ...state.notifications].slice(0, 50) })),
  clearNotifications: () => set({ notifications: [] }),
}));

export default useAppStore;
```

---

## 🎣 Custom Hooks

### `src/hooks/useTasks.js`
```js
import { useQuery, useMutation, useQueryClient } from '@tanstack/react-query';
import client from '../api/client';

export const useTasks = (teamId) =>
  useQuery({
    queryKey: ['tasks', teamId],
    queryFn: () => client.get(`/tasks?teamId=${teamId}`).then((r) => r.data),
    staleTime: 30_000,
    refetchInterval: 15_000,
  });

export const useUpdateTask = () => {
  const qc = useQueryClient();
  return useMutation({
    mutationFn: ({ id, patch }) => client.patch(`/tasks/${id}`, patch).then((r) => r.data),
    onSuccess: (_, { teamId }) => qc.invalidateQueries(['tasks', teamId]),
  });
};
```

### `src/hooks/useMessages.js`
```js
import { useInfiniteQuery } from '@tanstack/react-query';
import client from '../api/client';

export const useMessages = (channel) =>
  useInfiniteQuery({
    queryKey: ['messages', channel],
    queryFn: ({ pageParam = null }) =>
      client
        .get(`/messages?channel=${channel}${pageParam ? `&before=${pageParam}` : ''}`)
        .then((r) => r.data),
    getNextPageParam: (last) => last.nextCursor ?? undefined,
  });
```

### `src/hooks/useMeshStatus.js`
```js
import { useEffect } from 'react';
import useAppStore from '../store/appStore';

export const useMeshStatus = () => {
  const setMeshMode = useAppStore((s) => s.setMeshMode);

  useEffect(() => {
    const check = async () => {
      try {
        await fetch('http://localhost:7432/health', { signal: AbortSignal.timeout(1000) });
        setMeshMode(true);
      } catch {
        setMeshMode(false);
      }
    };
    check();
    const id = setInterval(check, 5000);
    return () => clearInterval(id);
  }, [setMeshMode]);
};
```

---

## 🃏 Kanban Components

### `KanbanBoard.jsx`
```jsx
import KanbanColumn from './KanbanColumn';
import { useTasks } from '../../hooks/useTasks';

const COLUMNS = ['TODO', 'IN_PROGRESS', 'REVIEW', 'DONE'];

export default function KanbanBoard({ teamId }) {
  const { data: tasks = [], isLoading, isError } = useTasks(teamId);

  if (isLoading) return <div className="flex gap-4 animate-pulse">{[1,2,3,4].map(i => <div key={i} className="w-72 h-96 bg-gray-800 rounded-xl" />)}</div>;
  if (isError) return <p className="text-red-400 text-center mt-10">Failed to load tasks.</p>;

  return (
    <div className="flex gap-4 overflow-x-auto pb-4 h-full">
      {COLUMNS.map((status) => (
        <KanbanColumn key={status} status={status} tasks={tasks.filter((t) => t.status === status)} />
      ))}
    </div>
  );
}
```

### `KanbanColumn.jsx`
```jsx
import TaskCard from './TaskCard';

const STATUS_COLORS = {
  TODO: 'border-gray-500',
  IN_PROGRESS: 'border-yellow-500',
  REVIEW: 'border-blue-500',
  DONE: 'border-green-500',
};

export default function KanbanColumn({ status, tasks }) {
  return (
    <div className={`flex-shrink-0 w-72 rounded-xl border-t-4 bg-gray-900 p-3 ${STATUS_COLORS[status]}`}>
      <div className="flex items-center justify-between mb-3">
        <h3 className="text-sm font-semibold text-gray-300 uppercase tracking-wider">{status.replace('_', ' ')}</h3>
        <span className="text-xs bg-gray-700 text-gray-400 rounded-full px-2 py-0.5">{tasks.length}</span>
      </div>
      <div className="space-y-2">
        {tasks.map((task) => <TaskCard key={task.id} task={task} />)}
      </div>
    </div>
  );
}
```

### `TaskCard.jsx`
```jsx
const PRIORITY_BADGE = {
  HIGH:   'bg-red-900 text-red-300',
  MEDIUM: 'bg-yellow-900 text-yellow-300',
  LOW:    'bg-green-900 text-green-300',
};

export default function TaskCard({ task }) {
  return (
    <div className="bg-gray-800 rounded-lg p-3 cursor-pointer hover:bg-gray-700 transition-colors">
      <p className="text-sm text-white font-medium mb-2 line-clamp-2">{task.title}</p>
      <div className="flex items-center justify-between">
        <span className={`text-xs px-2 py-0.5 rounded-full font-medium ${PRIORITY_BADGE[task.priority] || 'bg-gray-700 text-gray-400'}`}>
          {task.priority}
        </span>
        {task.assignee && <span className="text-xs text-gray-400">{task.assignee}</span>}
      </div>
    </div>
  );
}
```

---

## 💬 Messaging Components

### `MessageFeed.jsx`
```jsx
import { useEffect, useRef } from 'react';
import { useMessages } from '../../hooks/useMessages';
import useAppStore from '../../store/appStore';

export default function MessageFeed() {
  const channel = useAppStore((s) => s.activeChannel);
  const bottomRef = useRef(null);
  const { data, isLoading, fetchNextPage, hasNextPage } = useMessages(channel);
  const messages = data?.pages.flatMap((p) => p.messages) ?? [];

  useEffect(() => { bottomRef.current?.scrollIntoView({ behavior: 'smooth' }); }, [messages.length]);

  if (isLoading) return <div className="p-4 space-y-4 animate-pulse">{[1,2,3].map(i=><div key={i} className="flex gap-3"><div className="w-8 h-8 bg-gray-700 rounded-full"/><div className="h-8 bg-gray-800 rounded-lg w-2/3"/></div>)}</div>;

  return (
    <div className="flex-1 overflow-y-auto p-4 space-y-3">
      {hasNextPage && <button onClick={fetchNextPage} className="w-full text-xs text-blue-400 hover:underline text-center">Load older messages</button>}
      {messages.map((msg) => (
        <div key={msg.id} className="flex gap-3">
          <div className="w-8 h-8 rounded-full bg-indigo-600 flex-shrink-0 flex items-center justify-center text-xs font-bold text-white">
            {msg.sender?.[0]?.toUpperCase()}
          </div>
          <div>
            <p className="text-xs text-gray-400 mb-0.5"><span className="text-gray-200 font-medium">{msg.sender}</span> · {new Date(msg.timestamp).toLocaleTimeString()}</p>
            <p className="text-sm text-gray-100 bg-gray-800 rounded-lg px-3 py-2 inline-block">{msg.content}</p>
          </div>
        </div>
      ))}
      <div ref={bottomRef} />
    </div>
  );
}
```

### `MessageInput.jsx`
```jsx
import { useState } from 'react';
import { Send } from 'lucide-react';
import client from '../../api/client';
import useAppStore from '../../store/appStore';
import { useQueryClient } from '@tanstack/react-query';

export default function MessageInput() {
  const [text, setText] = useState('');
  const [sending, setSending] = useState(false);
  const channel = useAppStore((s) => s.activeChannel);
  const qc = useQueryClient();

  const send = async (e) => {
    e.preventDefault();
    if (!text.trim()) return;
    setSending(true);
    try {
      await client.post('/messages', { channel, content: text });
      setText('');
      qc.invalidateQueries(['messages', channel]);
    } finally { setSending(false); }
  };

  return (
    <form onSubmit={send} className="p-4 border-t border-gray-700 flex gap-2">
      <input
        className="flex-1 bg-gray-800 text-white rounded-lg px-4 py-2 text-sm outline-none focus:ring-2 focus:ring-indigo-500 placeholder-gray-500"
        placeholder={`Message #${channel}`}
        value={text} onChange={(e) => setText(e.target.value)} disabled={sending}
      />
      <button type="submit" disabled={sending || !text.trim()} className="bg-indigo-600 hover:bg-indigo-500 disabled:opacity-40 text-white rounded-lg px-4 py-2 transition-colors">
        <Send size={16} />
      </button>
    </form>
  );
}
```

---

## 🌐 MeshStatusBanner

```jsx
import useAppStore from '../../store/appStore';
import { WifiOff } from 'lucide-react';

export default function MeshStatusBanner() {
  const isMesh = useAppStore((s) => s.isMeshMode);
  if (!isMesh) return null;
  return (
    <div className="bg-amber-900/80 border-b border-amber-600 px-4 py-2 flex items-center gap-2 text-amber-300 text-sm">
      <WifiOff size={15} className="animate-pulse" />
      <span><strong>Mesh Mode Active</strong> — Internet limited. P2P file sharing available on LAN.</span>
    </div>
  );
}
```

---

## 🔔 NotificationBell

```jsx
import { Bell } from 'lucide-react';
import { useState } from 'react';
import useAppStore from '../../store/appStore';

export default function NotificationBell() {
  const [open, setOpen] = useState(false);
  const { notifications, clearNotifications } = useAppStore();

  return (
    <div className="relative">
      <button onClick={() => setOpen((o) => !o)} className="relative p-2 rounded-lg hover:bg-gray-700 text-gray-300 transition-colors">
        <Bell size={20} />
        {notifications.length > 0 && <span className="absolute top-1 right-1 w-2 h-2 bg-red-500 rounded-full" />}
      </button>
      {open && (
        <div className="absolute right-0 mt-2 w-80 bg-gray-800 border border-gray-700 rounded-xl shadow-2xl z-50 overflow-hidden">
          <div className="flex items-center justify-between px-4 py-3 border-b border-gray-700">
            <span className="text-sm font-semibold text-white">Notifications</span>
            <button onClick={clearNotifications} className="text-xs text-gray-400 hover:text-white">Clear all</button>
          </div>
          <div className="max-h-64 overflow-y-auto divide-y divide-gray-700">
            {notifications.length === 0 ? (
              <p className="text-xs text-gray-500 text-center py-6">No new notifications</p>
            ) : notifications.map((n, i) => (
              <div key={i} className="px-4 py-3">
                <p className="text-sm text-white">{n.message}</p>
                <p className="text-xs text-gray-500 mt-0.5">{new Date(n.timestamp).toLocaleTimeString()}</p>
              </div>
            ))}
          </div>
        </div>
      )}
    </div>
  );
}
```

---

## 🏗️ Layout

### `AppShell.jsx`
```jsx
import Sidebar from './Sidebar';
import Topbar from './Topbar';
import MeshStatusBanner from '../mesh/MeshStatusBanner';
import { useMeshStatus } from '../../hooks/useMeshStatus';
import { Outlet } from 'react-router-dom';

export default function AppShell() {
  useMeshStatus();
  return (
    <div className="flex h-screen bg-gray-950 text-white overflow-hidden">
      <Sidebar />
      <div className="flex flex-col flex-1 overflow-hidden">
        <MeshStatusBanner />
        <Topbar />
        <main className="flex-1 overflow-auto p-4"><Outlet /></main>
      </div>
    </div>
  );
}
```

### `Sidebar.jsx`
```jsx
import { NavLink } from 'react-router-dom';
import { LayoutDashboard, Trello, MessageSquare, Radio } from 'lucide-react';

const NAV = [
  { to: '/', icon: LayoutDashboard, label: 'Dashboard' },
  { to: '/kanban', icon: Trello, label: 'Kanban' },
  { to: '/messaging', icon: MessageSquare, label: 'Messaging' },
  { to: '/mesh', icon: Radio, label: 'Mesh Share' },
];

export default function Sidebar() {
  return (
    <aside className="w-56 flex-shrink-0 bg-gray-900 border-r border-gray-800 flex flex-col py-4">
      <div className="px-4 mb-6">
        <h1 className="text-lg font-bold text-indigo-400 tracking-tight">⚡ HackCoord</h1>
        <p className="text-xs text-gray-500">Hackathon Platform</p>
      </div>
      <nav className="flex-1 space-y-1 px-2">
        {NAV.map(({ to, icon: Icon, label }) => (
          <NavLink key={to} to={to} end className={({ isActive }) => `flex items-center gap-3 px-3 py-2 rounded-lg text-sm transition-colors ${isActive ? 'bg-indigo-600 text-white' : 'text-gray-400 hover:bg-gray-800 hover:text-white'}`}>
            <Icon size={16} />{label}
          </NavLink>
        ))}
      </nav>
    </aside>
  );
}
```

### `Topbar.jsx`
```jsx
import NotificationBell from '../notifications/NotificationBell';
import useAppStore from '../../store/appStore';

export default function Topbar() {
  const user = useAppStore((s) => s.user);
  return (
    <header className="h-14 flex-shrink-0 flex items-center justify-between px-6 border-b border-gray-800 bg-gray-900">
      <span className="text-sm text-gray-400">Welcome back, <span className="text-white font-medium">{user?.name ?? 'Hacker'}</span></span>
      <div className="flex items-center gap-3">
        <NotificationBell />
        <div className="w-8 h-8 rounded-full bg-indigo-600 flex items-center justify-center text-sm font-bold">
          {user?.name?.[0]?.toUpperCase() ?? 'H'}
        </div>
      </div>
    </header>
  );
}
```

---

## 🚦 App.jsx (Routing)

```jsx
import { BrowserRouter, Routes, Route, Navigate } from 'react-router-dom';
import { QueryClient, QueryClientProvider } from '@tanstack/react-query';
import { Toaster } from 'react-hot-toast';
import AppShell from './components/layout/AppShell';
import DashboardPage from './pages/DashboardPage';
import KanbanPage from './pages/KanbanPage';
import MessagingPage from './pages/MessagingPage';
import LoginPage from './pages/LoginPage';

const qc = new QueryClient({ defaultOptions: { queries: { retry: 2, staleTime: 20_000 } } });
const isLoggedIn = () => !!localStorage.getItem('jwt_token');

export default function App() {
  return (
    <QueryClientProvider client={qc}>
      <BrowserRouter>
        <Routes>
          <Route path="/login" element={<LoginPage />} />
          <Route element={isLoggedIn() ? <AppShell /> : <Navigate to="/login" replace />}>
            <Route index element={<DashboardPage />} />
            <Route path="/kanban" element={<KanbanPage />} />
            <Route path="/messaging" element={<MessagingPage />} />
          </Route>
        </Routes>
      </BrowserRouter>
      <Toaster position="bottom-right" toastOptions={{ style: { background: '#1f2937', color: '#f9fafb' } }} />
    </QueryClientProvider>
  );
}
```

---

## 🌍 .env

```
VITE_API_URL=http://localhost:8080/api
```

---

## ✅ Agent Checklist

- [ ] Scaffold Vite + React project, install all dependencies
- [ ] Configure Tailwind CSS dark mode
- [ ] Implement `src/api/client.js` with JWT interceptors
- [ ] Implement Zustand store (`appStore.js`)
- [ ] Implement custom hooks: `useTasks`, `useMessages`, `useMeshStatus`
- [ ] Build Kanban components: `KanbanBoard`, `KanbanColumn`, `TaskCard`
- [ ] Build Messaging components: `MessageFeed`, `MessageInput`
- [ ] Build `MeshStatusBanner` (polls daemon on port 7432)
- [ ] Build `NotificationBell` with dropdown
- [ ] Build `AppShell`, `Sidebar`, `Topbar`
- [ ] Build all pages: `LoginPage`, `KanbanPage`, `MessagingPage`, `DashboardPage`
- [ ] Wire all routes in `App.jsx` with auth guard
- [ ] Set `.env` with backend URL
- [ ] Run `npm run dev` and verify all pages load
