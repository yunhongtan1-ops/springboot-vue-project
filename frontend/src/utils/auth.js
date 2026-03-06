const TOKEN_KEY = 'ai_interview_token'
const USER_KEY = 'ai_interview_user'

export function saveAuth(loginData) {
  localStorage.setItem(TOKEN_KEY, loginData.token)
  localStorage.setItem(USER_KEY, JSON.stringify(loginData.user))
}

export function clearAuth() {
  localStorage.removeItem(TOKEN_KEY)
  localStorage.removeItem(USER_KEY)
}

export function getStoredUser() {
  const raw = localStorage.getItem(USER_KEY)
  if (!raw) {
    return null
  }

  try {
    return JSON.parse(raw)
  } catch {
    localStorage.removeItem(USER_KEY)
    return null
  }
}

export function getStoredToken() {
  return localStorage.getItem(TOKEN_KEY) || ''
}

export function isAuthenticated() {
  return Boolean(getStoredUser() && getStoredToken())
}

export function isAdminUser(user = getStoredUser()) {
  return user?.role === 'ADMIN'
}