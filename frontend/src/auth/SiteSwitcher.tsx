import { useAuth } from './AuthContext'

export function SiteSwitcher() {
  const { sites, adminSelectedSiteId, setAdminSelectedSiteId } = useAuth()

  if (!sites || sites.length === 0) return null

  return (
    <div className="site-switcher">
      {sites.map((site) => (
        <button
          key={site.id}
          className={`site-switcher-pill${adminSelectedSiteId === site.id ? ' active' : ''}`}
          onClick={() => setAdminSelectedSiteId(site.id)}
        >
          {site.name}
        </button>
      ))}
    </div>
  )
}
