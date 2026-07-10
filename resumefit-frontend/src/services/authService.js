import axios from 'axios'

export const TOKEN_KEY = 'resumefitToken'
export const USER_KEY = 'resumefitUser'

const authApi = axios.create({
  baseURL: 'http://localhost:8080',
  headers: {
    'Content-Type': 'application/json',
  },
})

export const getProfile = (token) => {
  return authApi.get('/auth/me', {
    headers: {
      Authorization: `Bearer ${token}`,
    },
  })
}

export const signup = (userData) => {
  return authApi.post('/auth/signup', userData)
}

export const login = (userData) => {
  return authApi.post('/auth/login', userData)
}
