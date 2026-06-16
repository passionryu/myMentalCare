import { Suspense } from 'react'
import KakaoCallbackClient from './KakaoCallbackClient'

export default function KakaoCallbackPage() {
  return (
    <Suspense fallback={<KakaoCallbackFallback />}>
      <KakaoCallbackClient />
    </Suspense>
  )
}

function KakaoCallbackFallback() {
  return (
    <main className="page-shell callback-page-shell">
      <section className="auth-callback-card" aria-live="polite">
        <div className="auth-callback-icon loading" />
        <p className="eyebrow">Kakao Login</p>
        <h1>안전하게 확인하고 있어요</h1>
        <p>카카오 계정과 Haru Mind를 안전하게 연결하고 있습니다.</p>
      </section>
    </main>
  )
}
