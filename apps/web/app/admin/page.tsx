import { Activity, FileText, MessageSquareText, UserPlus } from 'lucide-react'

const dashboardCards = [
  { label: '전체 회원 수', value: '-', description: '회원 지표 API 연결 예정', icon: UserPlus },
  { label: '오늘 대화 수', value: '-', description: '대화 이벤트 집계 예정', icon: MessageSquareText },
  { label: '오늘 리포트', value: '-', description: '마음 리포트 생성 건수 예정', icon: FileText },
  { label: '장애/에러', value: '-', description: '최근 오류 요약 예정', icon: Activity },
]

export default function AdminDashboardPage() {
  return (
    <section className="admin-page-grid">
      <div className="admin-dashboard-grid">
        {dashboardCards.map((card) => {
          const Icon = card.icon

          return (
            <article className="admin-stat-card" key={card.label}>
              <Icon aria-hidden="true" />
              <span>{card.label}</span>
              <strong>{card.value}</strong>
              <p>{card.description}</p>
            </article>
          )
        })}
      </div>

      <article className="admin-panel">
        <div>
          <span className="admin-eyebrow">운영 현황</span>
          <h2>대시보드 지표 연결 대기</h2>
        </div>
        <p>
          관리자 권한과 라우팅 골격이 먼저 고정되었습니다. 다음 단계에서 회원 수, 오늘 가입자 수, 오늘 대화 수,
          오늘 리포트 생성 수를 실제 API로 연결합니다.
        </p>
      </article>
    </section>
  )
}
