import { useState } from 'react'
import { useAuth } from '../../context/AuthContext.jsx'
import { analyzeResume, extractTextFromPdf, getHistory } from '../../services/resumeService.js'

function ResumeUploadCard() {
  const { user, token } = useAuth()
  const [resumeText, setResumeText] = useState('')
  const [jobDescription, setJobDescription] = useState('')
  const [selectedFileName, setSelectedFileName] = useState('')
  const [loading, setLoading] = useState(false)
  const [statusMessage, setStatusMessage] = useState('')
  const [error, setError] = useState('')
  const [analysis, setAnalysis] = useState(null)
  const [history, setHistory] = useState([])

  const handleFileChange = async (event) => {
    const file = event.target.files?.[0]
    const fileInput = event.target

    if (!file) {
      return
    }

    if (file.type !== 'application/pdf') {
      setError('Please upload a PDF resume file.')
      setStatusMessage('')
      setSelectedFileName('')
      fileInput.value = ''
      return
    }

    setError('')
    setStatusMessage('Extracting text from the uploaded PDF...')
    setLoading(true)
    setAnalysis(null)
    setSelectedFileName(file.name)

    try {
      const extractedText = await extractTextFromPdf(file)
      setResumeText(extractedText)
      setStatusMessage('Resume text loaded from PDF. Review or edit it before analyzing.')
    } catch (extractError) {
      setResumeText('')
      setSelectedFileName('')
      setStatusMessage('')
      setError(extractError?.message || 'Could not read the PDF file.')
    } finally {
      setLoading(false)
      fileInput.value = ''
    }
  }

  const handleAnalyze = async () => {
    const trimmedResume = resumeText.trim()
    const trimmedJobDescription = jobDescription.trim()

    if (!trimmedResume) {
      setError('Add resume text or upload a PDF first.')
      return
    }

    if (!trimmedJobDescription) {
      setError('Enter a job description to analyze against.')
      return
    }

    setLoading(true)
    setError('')
    setStatusMessage('Running ATS analysis...')

    try {
      const response = await analyzeResume({
        resumeText: trimmedResume,
        jdText: trimmedJobDescription,
        userId: user?.userId ?? null,
        userName: user?.name ?? '',
        userEmail: user?.email ?? '',
        fileName: selectedFileName || 'resume.pdf',
      }, token)

      setAnalysis(response.data)
      setStatusMessage('Analysis complete.')
    } catch (analysisError) {
      const apiMessage =
        analysisError.response?.data?.message ||
        analysisError.response?.data?.error ||
        analysisError.response?.data ||
        analysisError.message

      setError(apiMessage || 'Analysis failed. Please try again.')
      setAnalysis(null)
      setStatusMessage('')
    } finally {
      setLoading(false)
    }
  }

  const loadHistory = async () => {
    setLoading(true)
    setError('')
    setStatusMessage('Loading history...')

    try {
      const res = await getHistory(token)
      setHistory(res.data || [])
      setStatusMessage('History loaded')
    } catch (err) {
      setError(err?.response?.data?.message || err.message || 'Could not load history')
    } finally {
      setLoading(false)
    }
  }

  const missingSkills = analysis?.missingSkills || []

  return (
    <div className="analysis-card">
      <div className="section-heading">
        <div>
          <span className="eyebrow">ATS analysis</span>
          <h3>Compare your resume with the job description</h3>
          <p>Upload a PDF to extract resume text automatically, or paste the text directly.</p>
        </div>
      </div>

      {statusMessage ? <div className="success-banner">{statusMessage}</div> : null}
      {error ? <div className="error-banner">{error}</div> : null}

      <div className="analysis-grid">
        <label className="field analysis-field">
          <span>Resume PDF</span>
          <input type="file" accept="application/pdf" onChange={handleFileChange} />
          <small className="field-hint">PDF upload is optional. The extracted text is filled into the field below.</small>
        </label>

        <div className="analysis-file-chip" aria-live="polite">
          {selectedFileName ? `Loaded file: ${selectedFileName}` : 'No PDF selected yet'}
        </div>

        <label className="field analysis-field analysis-field--wide">
          <span>Resume text</span>
          <textarea
            className="analysis-textarea"
            value={resumeText}
            onChange={(event) => {
              setResumeText(event.target.value)
              if (error) {
                setError('')
              }
            }}
            placeholder="Paste the resume content here or upload a PDF to auto-fill it."
            rows={10}
          />
        </label>

        <label className="field analysis-field analysis-field--wide">
          <span>Job description</span>
          <textarea
            className="analysis-textarea"
            value={jobDescription}
            onChange={(event) => {
              setJobDescription(event.target.value)
              if (error) {
                setError('')
              }
            }}
            placeholder="Paste the target job description here."
            rows={10}
          />
        </label>
      </div>

      <div className="analysis-actions">
        <p className="analysis-note">
          The frontend sends resume text, job text, and the signed-in user details to the Spring Boot API for saving.
        </p>
        <button type="button" className="primary-button analysis-button" onClick={handleAnalyze} disabled={loading}>
          {loading ? (
            <span className="button-loading">
              <span className="spinner" aria-hidden="true" />
              Analyzing...
            </span>
          ) : (
            'Analyze Resume'
          )}
        </button>
        <button type="button" className="ghost-button" onClick={loadHistory} disabled={loading} style={{ marginLeft: 8 }}>
          View History
        </button>
      </div>

      {analysis ? (
        <div className="analysis-results">
          <div className="summary-grid">
            <article className="stat-card">
              <span>ATS Score</span>
              <strong>{analysis.score}%</strong>
              <p>Returned by the Spring Boot analysis endpoint.</p>
            </article>

            <article className="stat-card">
              <span>Missing Skills</span>
              <strong>{missingSkills.length}</strong>
              <p>Skills the matcher did not find in your resume.</p>
            </article>

            <article className="stat-card">
              <span>Match Status</span>
              <strong>{analysis.score >= 70 ? 'Strong' : 'Needs Work'}</strong>
              <p>Quick summary of the overall fit.</p>
            </article>
          </div>

          <div className="info-card highlight-card">
            <div className="section-heading">
              <div>
                <h3>Missing skills</h3>
                <p>These are the skills you may want to add, highlight, or learn before applying.</p>
              </div>
            </div>

            {missingSkills.length ? (
              <div className="skill-chip-list">
                {missingSkills.map((skill) => (
                  <span key={skill} className="skill-chip">
                    {skill}
                  </span>
                ))}
              </div>
            ) : (
              <p className="analysis-empty">No missing skills were returned by the backend.</p>
            )}
          </div>
        </div>
      ) : null}

      {history && history.length ? (
        <div className="history-list">
          <h4>Saved resumes</h4>
          <ul>
            {history.map((h) => (
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
        </div>
      ) : null}
    </div>
  )
}

export default ResumeUploadCard