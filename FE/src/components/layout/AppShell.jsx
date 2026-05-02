import Sidebar from './Sidebar'
import Topbar from './Topbar'
import MeshStatusBanner from '../mesh/MeshStatusBanner'
import { useMeshStatus } from '../../hooks/useMeshStatus'
import { Outlet } from 'react-router-dom'

export default function AppShell() {
  useMeshStatus()
  
  return (
    <div className="flex h-screen bg-gray-950 text-white overflow-hidden">
      <Sidebar />
      <div className="flex flex-col flex-1 overflow-hidden">
        <MeshStatusBanner />
        <Topbar />
        <main className="flex-1 overflow-auto p-4">
          <Outlet />
        </main>
      </div>
    </div>
  )
}
