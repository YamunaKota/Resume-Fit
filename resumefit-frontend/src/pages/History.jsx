import { useEffect, useState } from 'react'
import { useAuth } from '../context/AuthContext.jsx'
import { getHistoryPaged } from '../services/resumeService.js'
import { useNavigate } from 'react-router-dom'

function History() {
  const { token } = useAuth()
  const [page, setPage] = useState(0)
  const [size] = useState(10)
  const [data, setData] = useState(null)
  const [loading, setLoading] = useState(false)
  const [error, setError] = useState('')
  const navigate = useNavigate()

  useEffect(() => {
    load()
  }, [page])

  const load = async () => {
    setLoading(true)
    setError('')
    try {
      const res = await getHistoryPaged(page, size, token)
      setData(res.data)
    } catch (err) {
      setError(err.message || 'Error')
    } finally {
      setLoading(false)
    }
  }

  return (
    <div className="history-page">
      <div className="section-heading">
        <div>
          <span className="eyebrow">History</span>
          <h3>Your uploaded resumes and analysis</h3>
        </div>
      </div>

      {error ? <div className="error-banner">{error}</div> : null}

      {loading ? <div>Loading...</div> : null}

      {data ? (
        <div>
          <ul>
            {data.content.map((h) => (
              <li key={h.resumeId}>
                <strong>{h.fileName}</strong> — {h.uploadedAt} — Score: {h.score ?? 'N/A'}
                {h.missingSkills && h.missingSkills.length ? (
                  <div className="skill-chip-list">
                    {h.missingSkills.map((s) => (
                      <span key={s} className="skill-chip">
                        {s}
                      </span>
                    ))}
                  </div>
                ) : null}
              </li>
            ))}
          </ul>

          <div className="pagination-controls">
            <button onClick={() => setPage((p) => Math.max(0, p - 1))} disabled={page === 0}>
              Previous
            </button>
            <span>
              Page {data.number + 1} of {data.totalPages}
            </span>
            <button onClick={() => setPage((p) => p + 1)} disabled={page + 1 >= data.totalPages}>
              Next
            </button>
          </div>
        </div>
      ) : null}
    </div>
  )
}

export default History
