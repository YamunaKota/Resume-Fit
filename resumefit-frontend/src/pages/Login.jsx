import { useState } from 'react'
import { Link, useLocation, useNavigate } from 'react-router-dom'
import AuthShell from '../components/layout/AuthShell.jsx'
import { useAuth } from '../context/AuthContext.jsx'
import { login } from '../services/authService.js'

function Login() {
  const navigate = useNavigate()
  const location = useLocation()
  const { signIn } = useAuth()
  const [formData, setFormData] = useState({ email: '', password: '' })
  const [error, setError] = useState('')
  const [loading, setLoading] = useState(false)

  const signupMessage = location.state?.message || ''

  const handleChange = (event) => {
    const { name, value } = event.target

    setFormData((currentData) => ({
      ...currentData,
      [name]: value,
    }))
  }

  const handleSubmit = async (event) => {
    event.preventDefault()
    setError('')
    setLoading(true)

    try {
      const response = await login(formData)
      signIn(response.data)
      navigate('/dashboard', { replace: true })
    } catch (loginError) {
      const message =
        loginError.response?.data?.message ||
        loginError.response?.data ||
        'Login failed. Check your credentials and try again.'
      setError(message)
    } finally {
      setLoading(false)
    }
  }

  return (
    <AuthShell
      badge="Pastel analysis workspace"
      title="Welcome back"
      description="Log in to continue to your dashboard and review your resume insights."
      asideTitle="Sign in to ResumeFit"
      asideText="Access the upload flow, track activity, and prepare for ATS scoring."
      footer={
        <p className="auth-footer">
          New here? <Link to="/signup">Create an account</Link>
        </p>
      }
    >
      {signupMessage ? <div className="success-banner">{signupMessage}</div> : null}
      {error ? <div className="error-banner">{error}</div> : null}

      <form className="auth-form" onSubmit={handleSubmit}>
        <label className="field">
          <span>Email</span>
          <input
            type="email"
            name="email"
            value={formData.email}
            onChange={handleChange}
            placeholder="you@example.com"
            autoComplete="email"
            required
          />
        </label>

        <label className="field">
          <span>Password</span>
          <input
            type="password"
            name="password"
            value={formData.password}
            onChange={handleChange}
            placeholder="Enter your password"
            autoComplete="current-password"
            required
          />
        </label>

        <button type="submit" className="primary-button" disabled={loading}>
          {loading ? 'Logging in...' : 'Log in'}
        </button>
      </form>
    </AuthShell>
  )
}

export default Login
