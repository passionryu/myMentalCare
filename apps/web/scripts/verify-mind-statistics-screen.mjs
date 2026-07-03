import { readFileSync } from 'node:fs'
import { resolve } from 'node:path'

const homePage = readFileSync(resolve('app/page.tsx'), 'utf8')
const statisticsPage = readFileSync(resolve('app/statistics/page.tsx'), 'utf8')
const statisticsApi = readFileSync(resolve('lib/mind-statistics-api.ts'), 'utf8')
const styles = readFileSync(resolve('app/globals.css'), 'utf8')

const checks = [
  ['메인 화면 내 통계 보기 진입점 제공', homePage.includes('내 통계 보기') && homePage.includes("router.push('/statistics')")],
  ['모바일 하단 두 번째 탭이 통계로 변경', homePage.includes('<span>통계</span>') && !homePage.includes('<span>대화</span>')],
  ['통계 페이지 라우트 제공', statisticsPage.includes('export default function StatisticsPage')],
  ['마음 달력 영역 제공', statisticsPage.includes('마음 달력') && statisticsPage.includes('mind-calendar-grid')],
  ['API 실패 시에도 월 날짜 셀 생성', statisticsPage.includes('buildEmptyCalendarDays') && statisticsPage.includes('calendar?.days.length ? calendar.days : buildEmptyCalendarDays(month)')],
  ['통계 API 부분 실패 허용', statisticsPage.includes('Promise.allSettled')],
  ['대화한 날 표시 문구 제공', statisticsPage.includes('대화 ${cell.messageCount}개')],
  ['날짜 상세 패널 제공', statisticsPage.includes('날짜 상세') && statisticsPage.includes('day-detail-panel')],
  ['감정 분포 그래프 제공', statisticsPage.includes('감정 분포') && statisticsPage.includes('emotion-bar-track')],
  ['감정 흐름 그래프 제공', statisticsPage.includes('주간 변화') && statisticsPage.includes('TrendChart')],
  ['비로그인 시 로그인 모달 요청 후 홈으로 이동', statisticsPage.includes('myMentalCare.openLoginModal') && statisticsPage.includes("router.replace('/')")],
  ['달력 API endpoint 확인', statisticsApi.includes('/api/mind-statistics/calendar')],
  ['날짜 상세 API endpoint 확인', statisticsApi.includes('/api/mind-statistics/days/')],
  ['요약 API endpoint 확인', statisticsApi.includes('/api/mind-statistics/overview')],
  ['통계 화면 스타일 제공', styles.includes('.statistics-page-shell') && styles.includes('.calendar-day') && styles.includes('.trend-chart')],
]

const failed = checks.filter(([, passed]) => !passed)

if (failed.length > 0) {
  console.error('mind statistics screen smoke checks failed')
  for (const [name] of failed) {
    console.error(`- ${name}`)
  }
  process.exit(1)
}

console.log('mind statistics screen smoke checks passed')
