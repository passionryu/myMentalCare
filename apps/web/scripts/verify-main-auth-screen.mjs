import { readFileSync } from 'node:fs'
import { resolve } from 'node:path'

const page = readFileSync(resolve('app/page.tsx'), 'utf8')
const styles = readFileSync(resolve('app/globals.css'), 'utf8')

const checks = [
  ['메인 서비스 이름 표시', page.includes('myMentalCare')],
  ['회원가입 모달 진입 버튼', page.includes("setAuthMode('signup')")],
  ['로그인 모달 진입 버튼', page.includes("setAuthMode('login')")],
  ['회원가입 폼 제목', page.includes('회원가입')],
  ['로그인 폼 제목', page.includes('로그인')],
  ['API 연동 전 안내 메시지', page.includes('API 연동 전입니다')],
  ['정신 건강 서비스 디자인 문구', page.includes('따뜻한 개인 멘탈 케어')],
  ['모달 backdrop 스타일', styles.includes('.modal-backdrop')],
  ['모바일 반응형 스타일', styles.includes('@media (max-width: 860px)')],
]

const failed = checks.filter(([, passed]) => !passed)

if (failed.length > 0) {
  console.error('main auth screen smoke checks failed')
  for (const [name] of failed) {
    console.error(`- ${name}`)
  }
  process.exit(1)
}

console.log('main auth screen smoke checks passed')
