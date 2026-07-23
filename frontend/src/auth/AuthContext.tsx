import { createContext, useContext, useEffect, useState, type ReactNode } from 'react'
import { apiPost, setAuthToken, setUnauthorizedHandler } from '../api/http'
import type { Role, SiteCode } from '../types'

const STORAGE_KEY = 'ojt_auth'

export interface AdminSite {
  id: number
  code: string
  name: string
}

interface StoredAuth {
  token: string
  role: Role
  siteId: number | null
  siteName: string | null
  sites: AdminSite[] | null
}

interface AuthContextValue {
  role: Role | null
  siteId: number | null
  siteName: string | null
  sites: AdminSite[] | null
  adminSelectedSiteId: number | null
  effectiveSiteId: number | null
  login: (code: SiteCode, pin: string) => Promise<void>
  loginAdmin: (pin: string) => Promise<void>
  logout: () => void
  setAdminSelectedSiteId: (siteId: number) => void
}

const AuthContext = createContext<AuthContextValue | null>(null)

function readStoredAuth(): StoredAuth | null {
  const raw = localStorage.getItem(STORAGE_KEY)
  if (!raw) return null
  try {
    return JSON.parse(raw) as StoredAuth
  } catch {
    return null
  }
}

export function AuthProvider({ children }: { children: ReactNode }) {
  // 초기 렌더 시점에 동기적으로 토큰을 세팅해야 자식 컴포넌트의 첫 API 호출에 Authorization 헤더가 누락되지 않는다.
  const [auth, setAuth] = useState<StoredAuth | null>(() => {
    const stored = readStoredAuth()
    setAuthToken(stored?.token ?? null)
    return stored
  })
  const [adminSelectedSiteId, setAdminSelectedSiteId] = useState<number | null>(null)

  const logout = () => {
    localStorage.removeItem(STORAGE_KEY)
    setAuthToken(null)
    setAuth(null)
    setAdminSelectedSiteId(null)
  }

  useEffect(() => {
    setUnauthorizedHandler(logout)
    return () => setUnauthorizedHandler(null)
    // eslint-disable-next-line react-hooks/exhaustive-deps
  }, [])

  const persist = (next: StoredAuth) => {
    localStorage.setItem(STORAGE_KEY, JSON.stringify(next))
    setAuthToken(next.token)
    setAuth(next)
    if (next.sites && next.sites.length > 0) {
      setAdminSelectedSiteId(next.sites[0].id)
    }
  }

  const login = async (code: SiteCode, pin: string) => {
    const result = await apiPost<{ token: string; role: Role; siteId: number; siteName: string }>(
      '/auth/site/verify',
      { code, pin },
    )
    persist({
      token: result.token,
      role: result.role,
      siteId: result.siteId,
      siteName: result.siteName,
      sites: null,
    })
  }

  const loginAdmin = async (pin: string) => {
    const result = await apiPost<{ token: string; role: Role; sites: AdminSite[] }>('/auth/admin/verify', { pin })
    persist({ token: result.token, role: result.role, siteId: null, siteName: null, sites: result.sites })
  }

  const effectiveSiteId = auth?.role === 'ADMIN' ? adminSelectedSiteId : (auth?.siteId ?? null)

  return (
    <AuthContext.Provider
      value={{
        role: auth?.role ?? null,
        siteId: auth?.siteId ?? null,
        siteName: auth?.siteName ?? null,
        sites: auth?.sites ?? null,
        adminSelectedSiteId,
        effectiveSiteId,
        login,
        loginAdmin,
        logout,
        setAdminSelectedSiteId,
      }}
    >
      {children}
    </AuthContext.Provider>
  )
}

export function useAuth() {
  const ctx = useContext(AuthContext)
  if (!ctx) throw new Error('useAuth must be used within AuthProvider')
  return ctx
}
