import { useState } from 'react'
import { Link, useNavigate } from 'react-router-dom'
import AuthShell from '../components/layout/AuthShell.jsx'
import { signup } from '../services/authService.js'

function Signup() {
  const navigate = useNavigate()
  const [formData, setFormData] = useState({
    name: '',
    email: '',
    password: '',
    confirmPassword: '',
  })
  const [error, setError] = useState('')
  const [loading, setLoading] = useState(false)

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
      const payload = {
        name: formData.name,
        email: formData.email,
        password: formData.password,
      }

      if (formData.password.length < 6) {
        throw new Error('Password must be at least 6 characters long.')
      }

      if (formData.password !== formData.confirmPassword) {
        throw new Error('Passwords do not match.')
      }

      const response = await signup(payload)
      const message = response.data?.message || 'Signup successful. You can now log in.'

      navigate('/login', {
        replace: true,
        state: { message },
      })
    } catch (signupError) {
      const message =
        signupError.response?.data?.message ||
        signupError.response?.data ||
        'Signup failed. Please try again.'
      setError(message)
    } finally {
      setLoading(false)
    }
  }

  return (
    <AuthShell
      badge="Join the workflow"
      title="Create your account"
      description="Register once and use the same clean dashboard for upload and analysis."
      asideTitle="Start with a polished setup"
      asideText="Use a consistent design system, smooth interactions, and a lightweight workflow."
      footer={
        <p className="auth-footer">
          Already have an account? <Link to="/login">Log in</Link>
        </p>
      }
    >
      {error ? <div className="error-banner">{error}</div> : null}

      <form className="auth-form" onSubmit={handleSubmit}>
        <label className="field">
          <span>Name</span>
          <input
            type="text"
            name="name"
            value={formData.name}
            onChange={handleChange}
            placeholder="Your name"
            autoComplete="name"
            required
          />
        </label>

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
            placeholder="Create a password"
            autoComplete="new-password"
            required
          />
        </label>

        <label className="field">
          <span>Confirm password</span>
          <input
            type="password"
            name="confirmPassword"
            value={formData.confirmPassword}
            onChange={handleChange}
            placeholder="Repeat your password"
            autoComplete="new-password"
            required
          />
        </label>

        <button type="submit" className="primary-button" disabled={loading}>
          {loading ? 'Creating account...' : 'Sign up'}
        </button>
      </form>
    </AuthShell>
  )
}

export default Signup
