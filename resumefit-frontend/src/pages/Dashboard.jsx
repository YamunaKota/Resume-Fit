import { useEffect, useMemo, useState } from 'react'
import Sidebar from '../components/dashboard/Sidebar.jsx'
import ResumeUploadCard from '../components/dashboard/ResumeUploadCard.jsx'
import { useAuth } from '../context/AuthContext.jsx'
import { getProfile } from '../services/authService.js'

function Dashboard() {
  const { user, signOut, token } = useAuth()
  const [activeItem, setActiveItem] = useState('Analysis')

  const dashboardName = useMemo(() => {
    return user?.name || user?.email || 'ResumeFit user'
  }, [user])

  useEffect(() => {
    // If the user object exists but the name is missing, try to refresh from the backend
    if (user && !user.name && token) {
      getProfile(token)
        .then((res) => {
          localStorage.setItem('resumefitUser', JSON.stringify(res.data))
          window.location.reload()
        })
        .catch(() => {
          // ignore - profile might be unavailable
        })
    }
  }, [user, token])

  const handleNavigate = (item) => {
    setActiveItem(item)
  }

  return (
    <main className="dashboard-page">
      <Sidebar activeItem={activeItem} onNavigate={handleNavigate} onLogout={signOut} user={user} />

      <section className="dashboard-main">
        <header className="top-navbar">
          <div className="top-navbar__brand">
            <strong>ResumeFit</strong>
          </div>

          <div className="top-navbar__actions">
            <button type="button" className="ghost-button" onClick={() => window.location.href = '/history'}>
              History
            </button>
            <button type="button" className="ghost-button" onClick={signOut}>
              Logout
            </button>
          </div>
        </header>

        <section className="dashboard-content">
          <div className="dashboard-header">
            <div>
              <p className="mini-label">Welcome back</p>
              <h1>{dashboardName}</h1>
              <p>Upload a PDF or paste resume text, then compare it with a job description.</p>
            </div>

            <div className="header-user-chip">
              <div className="user-avatar">RF</div>
              <div>
                <strong>{user?.name || 'ResumeFit user'}</strong>
                <span>{user?.email || 'No email stored'}</span>
              </div>
            </div>
          </div>

          <ResumeUploadCard />
        </section>
      </section>
    </main>
  )
}

export default Dashboard