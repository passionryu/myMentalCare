export type ThemeTone =
  | 'morning-window'
  | 'breathing-landscape'
  | 'mind-journal'
  | 'chat-bubbles'
  | 'botanical-room'

export const THEME_TONE_STORAGE_KEY = 'myMentalCare.themeTone'
export const DEFAULT_THEME_TONE: ThemeTone = 'chat-bubbles'

export const THEME_OPTIONS: Array<{ value: ThemeTone; label: string; description: string }> = [
  { value: 'breathing-landscape', label: '바다 해변', description: '잔잔한 바다와 모래빛 해변이 주는 개방감' },
  { value: 'chat-bubbles', label: '우드 톤', description: '따뜻한 나무결과 브라운 톤의 안정감' },
]

const legacyThemeToneMap: Record<string, ThemeTone> = {
  sunset: 'chat-bubbles',
  cream: 'chat-bubbles',
  wood: 'chat-bubbles',
  rose: 'chat-bubbles',
}

// 저장된 화면 배경 값을 현재 지원하는 배경 값으로 정규화한다.
export function normalizeThemeTone(value: string | null): ThemeTone {
  if (THEME_OPTIONS.some((theme) => theme.value === value)) {
    return value as ThemeTone
  }

  if (value && legacyThemeToneMap[value]) {
    return legacyThemeToneMap[value]
  }

  return DEFAULT_THEME_TONE
}

// 브라우저 저장소에서 화면 배경 값을 읽고, 오래된 값이면 새 값으로 갱신한다.
export function readStoredThemeTone(): ThemeTone {
  if (typeof window === 'undefined') {
    return DEFAULT_THEME_TONE
  }

  const savedThemeTone = localStorage.getItem(THEME_TONE_STORAGE_KEY)
  const normalizedThemeTone = normalizeThemeTone(savedThemeTone)

  if (savedThemeTone !== normalizedThemeTone) {
    localStorage.setItem(THEME_TONE_STORAGE_KEY, normalizedThemeTone)
  }

  return normalizedThemeTone
}
