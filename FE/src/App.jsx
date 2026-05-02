import { BrowserRouter, Routes, Route, Navigate } from 'react-router-dom'
import { QueryClient, QueryClientProvider } from '@tanstack/react-query'
import { Toaster } from 'react-hot-toast'
import AppShell from './components/layout/AppShell'
import DashboardPage from './pages/DashboardPage'
import KanbanPage from './pages/KanbanPage'
import MessagingPage from './pages/MessagingPage'
import MeshPage from './pages/MeshPage'
import LoginPage from './pages/LoginPage'

const qc = new QueryClient({
  defaultOptions: { queries: { retry: 2, staleTime: 20_000 } },
})

const isLoggedIn = () => !!localStorage.getItem('jwt_token')

function ProtectedRoutes() {
  return isLoggedIn() ? <AppShell /> : <Navigate to="/login" replace />
}

export default function App() {
  return (
    <QueryClientProvider client={qc}>
      <BrowserRouter>
        <Routes>
          <Route path="/login" element={<LoginPage />} />
          <Route element={<ProtectedRoutes />}>
            <Route index element={<DashboardPage />} />
            <Route path="/kanban" element={<KanbanPage />} />
            <Route path="/messaging" element={<MessagingPage />} />
            <Route path="/mesh" element={<MeshPage />} />
          </Route>
        </Routes>
      </BrowserRouter>
      <Toaster
        position="bottom-right"
        toastOptions={{
          style: { background: '#1f2937', color: '#f9fafb', border: '1px solid #374151' },
        }}
      />
    </QueryClientProvider>
  )
}
