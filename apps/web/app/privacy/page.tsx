'use client'

import { ArrowLeft, HeartHandshake } from 'lucide-react'
import { useRouter } from 'next/navigation'
import { useEffect, useState } from 'react'

type ThemeTone = 'sunset' | 'cream' | 'wood'

const THEME_TONE_STORAGE_KEY = 'myMentalCare.themeTone'

const privacySections = [
  {
    title: '1. 수집하는 개인정보 항목',
    items: [
      '회원가입 및 로그인: 아이디, 비밀번호, 이름, 이메일, 휴대전화번호가 수집될 수 있습니다.',
      'AI 마음대화 이용: 사용자가 입력한 대화 내용, 체크인 응답, 대화 생성 시각, 마음 리포트 생성 기록이 저장될 수 있습니다.',
      '서비스 이용 과정: 접속 로그, 기기 및 브라우저 정보, 오류 기록 등 서비스 안정성을 위한 정보가 생성될 수 있습니다.',
    ],
  },
  {
    title: '2. 개인정보 이용 목적',
    items: [
      '회원 식별, 로그인 유지, 계정 관리 등 기본 서비스 제공에 사용합니다.',
      'AI 마음대화, 체크인, 대화 흐름 저장, 마음 리포트 생성 등 핵심 기능 제공에 사용합니다.',
      '서비스 오류 확인, 보안 유지, 부정 이용 방지, 사용자 경험 개선에 사용합니다.',
    ],
  },
  {
    title: '3. 대화 및 마음 기록의 처리',
    items: [
      'Haru Mind의 대화 기록은 사용자가 이어서 대화하고 마음 리포트를 확인할 수 있도록 저장될 수 있습니다.',
      '사용자는 향후 제공되는 계정 관리 기능을 통해 대화 기록 삭제 또는 계정 삭제를 요청할 수 있습니다.',
      '민감한 개인정보, 타인의 개인정보, 금융 정보 등은 대화에 입력하지 않는 것을 권장합니다.',
    ],
  },
  {
    title: '4. 보유 및 이용 기간',
    items: [
      '회원 정보와 대화 기록은 회원 탈퇴 또는 삭제 요청 시까지 보관하는 것을 원칙으로 합니다.',
      '법령상 보관 의무가 있거나 분쟁 대응이 필요한 경우 필요한 기간 동안 별도로 보관될 수 있습니다.',
      '정확한 보관 기간과 삭제 절차는 실제 운영 정책에 맞춰 확정되어야 합니다.',
    ],
  },
  {
    title: '5. 제3자 제공 및 처리 위탁',
    items: [
      'Haru Mind는 원칙적으로 사용자의 동의 없이 개인정보를 외부에 제공하지 않습니다.',
      'AI 응답 생성, 서버 운영, 데이터 저장 등 서비스 제공을 위해 외부 인프라 또는 AI API를 사용할 수 있습니다.',
      '실제 사용 중인 업체, 이전 국가, 위탁 업무 범위는 운영 전 별도 고지가 필요합니다.',
    ],
  },
  {
    title: '6. 이용자의 권리',
    items: [
      '사용자는 자신의 개인정보 열람, 정정, 삭제, 처리 정지를 요청할 수 있습니다.',
      '계정 삭제 또는 대화 기록 삭제 요청 절차는 서비스 내 계정 관리 기능 또는 문의 채널을 통해 제공될 예정입니다.',
      '요청 처리 과정에서 본인 확인이 필요할 수 있습니다.',
    ],
  },
  {
    title: '7. 안전성 확보 조치',
    items: [
      '비밀번호는 복호화할 수 없는 방식으로 저장합니다.',
      '접근 권한 관리, 전송 구간 보호, 로그 점검 등 개인정보 보호를 위한 기술적·관리적 조치를 적용합니다.',
      '서비스 운영 환경에 맞는 보안 조치는 지속적으로 보완되어야 합니다.',
    ],
  },
  {
    title: '8. 문의',
    items: [
      '개인정보 관련 문의는 운영자가 지정한 고객 문의 채널을 통해 접수합니다.',
      '본 페이지는 예시 초안이므로 실제 문의 이메일, 사업자 정보, 개인정보 보호책임자 정보는 운영 전 확정해야 합니다.',
    ],
  },
] as const

export default function PrivacyPage() {
  const router = useRouter()
  const [themeTone, setThemeTone] = useState<ThemeTone>('sunset')

  useEffect(() => {
    const savedThemeTone = localStorage.getItem(THEME_TONE_STORAGE_KEY)
    if (savedThemeTone === 'rose') {
      setThemeTone('wood')
      localStorage.setItem(THEME_TONE_STORAGE_KEY, 'wood')
      return
    }
    if (savedThemeTone === 'sunset' || savedThemeTone === 'cream' || savedThemeTone === 'wood') {
      setThemeTone(savedThemeTone)
    }
  }, [])

  return (
    <main className="page-shell policy-page-shell" data-theme-tone={themeTone}>
      <nav className="top-nav service-top-nav" aria-label="개인정보처리방침 메뉴">
        <div className="brand-mark">
          <span className="brand-icon">
            <HeartHandshake size={20} aria-hidden="true" />
          </span>
          <span>Haru Mind</span>
        </div>
        <button className="ghost-button nav-outline-button" type="button" onClick={() => router.push('/')}>
          <ArrowLeft size={18} aria-hidden="true" />
          홈으로
        </button>
      </nav>

      <article className="policy-document" aria-labelledby="privacy-heading">
        <p className="eyebrow">예시 초안</p>
        <h1 id="privacy-heading">개인정보처리방침</h1>
        <p className="policy-lead">
          이 문서는 Haru Mind 서비스 운영을 가정한 개인정보처리방침 예시입니다. 실제 배포 전에는 수집 항목, 보관 기간,
          위탁 업체, 문의처, 관련 법령을 기준으로 반드시 검토해야 합니다.
        </p>
        <p className="policy-meta">시행 예정일: 운영 전 확정</p>

        {privacySections.map((section) => (
          <section className="policy-section" key={section.title}>
            <h2>{section.title}</h2>
            <ul>
              {section.items.map((item) => (
                <li key={item}>{item}</li>
              ))}
            </ul>
          </section>
        ))}
      </article>
    </main>
  )
}
