import useAppStore from '../../store/appStore'
import { WifiOff } from 'lucide-react'

export default function MeshStatusBanner() {
  const isMesh = useAppStore((s) => s.isMeshMode)
  
  if (!isMesh) return null
  
  return (
    <div className="bg-amber-900/80 border-b border-amber-600 px-4 py-2 flex items-center gap-2 text-amber-300 text-sm">
      <WifiOff size={15} className="animate-pulse" />
      <span><strong>Mesh Mode Active</strong> — Internet limited. P2P file sharing available on LAN.</span>
    </div>
  )
}
