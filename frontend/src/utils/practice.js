const LATEST_PRACTICE_KEY = 'ai_interview_latest_practice'

export function saveLatestPracticeRecord(record) {
  localStorage.setItem(LATEST_PRACTICE_KEY, JSON.stringify(record))
}

export function getLatestPracticeRecord() {
  const raw = localStorage.getItem(LATEST_PRACTICE_KEY)
  if (!raw) {
    return null
  }

  try {
    return JSON.parse(raw)
  } catch {
    localStorage.removeItem(LATEST_PRACTICE_KEY)
    return null
  }
}

export function clearLatestPracticeRecord() {
  localStorage.removeItem(LATEST_PRACTICE_KEY)
}
