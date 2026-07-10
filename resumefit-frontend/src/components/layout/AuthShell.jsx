function AuthShell({ badge, title, description, asideTitle, asideText, children, footer }) {
  return (
    <main className="auth-page">
      <section className="auth-illustration">
        <div className="auth-illustration__badge">{badge}</div>
        <h1>{title}</h1>
        <p>{description}</p>

        <div className="auth-illustration__panel">
          <div>
            <span className="mini-label">Smart screening</span>
            <strong>Upload, track, and prep for ATS</strong>
          </div>
          <div>
            <span className="mini-label">Clean workflow</span>
            <strong>Sign in and stay organized</strong>
          </div>
        </div>
      </section>

      <section className="auth-card-shell">
        <div className="auth-card">
          <div className="auth-card__header">
            <p className="eyebrow">ResumeFit</p>
            <h2>{asideTitle}</h2>
            <p>{asideText}</p>
          </div>

          {children}

          {footer}
        </div>
      </section>
    </main>
  )
}

export default AuthShell