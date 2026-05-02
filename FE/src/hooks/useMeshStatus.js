import { useEffect } from 'react'
import useAppStore from '../store/appStore'

export const useMeshStatus = () => {
  const setMeshMode = useAppStore((s) => s.setMeshMode)

  useEffect(() => {
    const check = async () => {
      try {
        // Poll local Python daemon
        await fetch('http://localhost:7432/health', { signal: AbortSignal.timeout(1000) })
        setMeshMode(true)
      } catch {
        setMeshMode(false)
      }
    }
    check()
    const id = setInterval(check, 5000)
    return () => clearInterval(id)
  }, [setMeshMode])
}
