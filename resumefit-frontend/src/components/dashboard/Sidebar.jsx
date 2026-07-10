const navItems = ['Dashboard', 'Upload Resume', 'Analysis', 'Logout']

function Sidebar({ activeItem, onNavigate, onLogout, user }) {
  return (
    <aside className="dashboard-sidebar">
      <div className="brand-block">
        <div className="brand-mark">R</div>
        <div>
          <strong>ResumeFit</strong>
          <span>Resume intelligence workspace</span>
        </div>
      </div>

      <nav className="sidebar-nav">
        {navItems.map((item) => (
          <button
            key={item}
            type="button"
            className={activeItem === item ? 'sidebar-link active' : 'sidebar-link'}
            onClick={() => onNavigate(item)}
          >
            <span>{item}</span>
          </button>
        ))}
      </nav>

      <div className="sidebar-footer">
        <div>
          <span className="mini-label">Logged in as</span>
          <strong>{user?.name || 'ResumeFit user'}</strong>
          <p>{user?.email || 'No email stored'}</p>
        </div>
        <button type="button" className="ghost-button" onClick={onLogout}>
          Logout
        </button>
      </div>
    </aside>
  )
}

export default Sidebar