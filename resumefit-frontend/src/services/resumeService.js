import axios from 'axios'
import { getDocument, GlobalWorkerOptions } from 'pdfjs-dist'

GlobalWorkerOptions.workerSrc = new URL('pdfjs-dist/build/pdf.worker.min.mjs', import.meta.url).toString()

const resumeApi = axios.create({
  baseURL: 'http://localhost:8080',
})

export const extractTextFromPdf = async (file) => {
  const arrayBuffer = await file.arrayBuffer()
  const pdf = await getDocument({ data: arrayBuffer }).promise
  const pageTexts = []

  for (let pageNumber = 1; pageNumber <= pdf.numPages; pageNumber += 1) {
    const page = await pdf.getPage(pageNumber)
    const content = await page.getTextContent()
    const pageText = content.items.map((item) => item.str || '').join(' ')
    pageTexts.push(pageText)
  }

  return pageTexts.join('\n').replace(/\s+/g, ' ').trim()
}

export const analyzeResume = (payload, token) => {
  return resumeApi.post('/api/analyze', payload, {
    headers: token ? { Authorization: `Bearer ${token}` } : {},
  })
}

export const getHistory = (token) => {
  return resumeApi.get('/api/history', {
    headers: token ? { Authorization: `Bearer ${token}` } : {},
  })
}

export const getHistoryPaged = (page = 0, size = 10, token) => {
  return resumeApi.get(`/api/history/paged?page=${page}&size=${size}`, {
    headers: token ? { Authorization: `Bearer ${token}` } : {},
  })
}