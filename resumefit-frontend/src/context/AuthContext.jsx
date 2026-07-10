import { createContext, useContext, useEffect, useMemo, useState } from 'react'
import { getProfile, TOKEN_KEY, USER_KEY } from '../services/authService.js'

const AuthContext = createContext(null)

function safeParse(value) {
  try {
    return JSON.parse(value)
  } catch {
    return null
  }
}

export function AuthProvider({ children }) {
  const [user, setUser] = useState(null)
  const [token, setToken] = useState(null)
  const [isReady, setIsReady] = useState(false)

  useEffect(() => {
    const storedToken = localStorage.getItem(TOKEN_KEY)
    const storedUser = safeParse(localStorage.getItem(USER_KEY))

    setToken(storedToken)

    if (storedToken) {
      console.debug('[AuthContext] Found stored token, fetching profile')
      getProfile(storedToken)
        .then((response) => {
          console.debug('[AuthContext] Profile fetched', response.data)
          setUser(response.data)
          localStorage.setItem(USER_KEY, JSON.stringify(response.data))
        })
        .catch((err) => {
          console.debug('[AuthContext] Profile fetch failed, using stored user', err?.message)
          setUser(storedUser)
        })
        .finally(() => {
          setIsReady(true)
        })
      return
    }

    setUser(storedUser)
    setIsReady(true)
  }, [])

  const signIn = (responseData) => {
    const nextToken = responseData?.token ?? responseData?.accessToken ?? responseData
    const nextUser = {
      userId: responseData?.userId ?? null,
      name: responseData?.name ?? '',
      email: responseData?.email ?? '',
    }

    setToken(nextToken)
    setUser(nextUser)
    localStorage.setItem(TOKEN_KEY, nextToken)
    localStorage.setItem(USER_KEY, JSON.stringify(nextUser))

    console.debug('[AuthContext] signIn saved token, calling profile refresh')
    getProfile(nextToken)
      .then((response) => {
        console.debug('[AuthContext] signIn profile response', response.data)
        setUser(response.data)
        localStorage.setItem(USER_KEY, JSON.stringify(response.data))
      })
      .catch(() => {
        // Keep the login response data if profile lookup is temporarily unavailable.
      })
  }

  const signOut = () => {
    setToken(null)
    setUser(null)
    localStorage.removeItem(TOKEN_KEY)
    localStorage.removeItem(USER_KEY)
  }

  const value = useMemo(
    () => ({
      user,
      token,
      isReady,
      isAuthenticated: Boolean(token),
      signIn,
      signOut,
    }),
    [user, token, isReady],
  )

  return <AuthContext.Provider value={value}>{children}</AuthContext.Provider>
}

export function useAuth() {
  const context = useContext(AuthContext)

  if (!context) {
    throw new Error('useAuth must be used within AuthProvider')
  }

  return context
}