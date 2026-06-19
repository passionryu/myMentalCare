'use client'

import { ArrowLeft, HeartHandshake } from 'lucide-react'
import { useRouter } from 'next/navigation'
import { useEffect, useState } from 'react'
import { DEFAULT_THEME_TONE, readStoredThemeTone, ThemeTone } from '@/lib/theme-tone'

const termsSections = [
  {
    title: '1. 목적',
    items: [
      '본 약관은 Haru Mind가 제공하는 AI 마음대화, 체크인, 대화 기록, 마음 리포트 기능의 이용 조건을 정하는 예시 약관입니다.',
      '실제 서비스 운영 전에는 사업자 정보, 유료 기능 여부, 환불 정책, 분쟁 처리 절차 등을 확정해야 합니다.',
    ],
  },
  {
    title: '2. 서비스의 성격',
    items: [
      'Haru Mind는 사용자의 생각과 감정을 정리하도록 돕는 AI 기반 자기 돌봄 보조 서비스입니다.',
      'Haru Mind는 의료 진단, 치료, 상담, 처방, 응급 대응을 제공하지 않습니다.',
      '자해 위험, 타해 위험, 긴급한 의료 상황이 있는 경우 즉시 전문기관, 응급기관 또는 주변의 도움을 받아야 합니다.',
    ],
  },
  {
    title: '3. 회원 계정',
    items: [
      '사용자는 정확한 정보를 바탕으로 계정을 생성하고, 계정 정보를 안전하게 관리해야 합니다.',
      '타인의 계정을 사용하거나 타인의 개인정보를 무단으로 입력해서는 안 됩니다.',
      '운영자는 보안상 필요하거나 약관 위반이 확인되는 경우 계정 이용을 제한할 수 있습니다.',
    ],
  },
  {
    title: '4. AI 응답의 한계',
    items: [
      'AI 응답은 사용자의 입력을 바탕으로 생성되며 항상 정확하거나 완전하지 않을 수 있습니다.',
      'AI 응답은 참고용이며, 중요한 의사결정이나 건강 관련 판단은 전문가의 조언을 받아야 합니다.',
      '사용자는 AI 응답을 자신의 상황에 맞게 판단하여 이용해야 합니다.',
    ],
  },
  {
    title: '5. 대화 기록과 리포트',
    items: [
      '대화 기록과 체크인 응답은 대화 이어가기, 마음 리포트 생성, 서비스 품질 개선을 위해 저장될 수 있습니다.',
      '마음 리포트는 사용자의 대화 내용을 요약하거나 정리하는 기능이며 의학적 평가가 아닙니다.',
      '기록 삭제, 계정 삭제, 리포트 관리 방식은 실제 운영 정책에 따라 제공됩니다.',
    ],
  },
  {
    title: '6. 금지 행위',
    items: [
      '불법 행위, 타인의 권리 침해, 혐오·폭력·위협 표현, 서비스 장애를 유발하는 행위를 금지합니다.',
      '타인의 개인정보나 민감한 정보를 동의 없이 입력해서는 안 됩니다.',
      'AI 응답을 악용하거나 서비스의 보안 체계를 우회하려는 행위를 금지합니다.',
    ],
  },
  {
    title: '7. 서비스 변경 및 중단',
    items: [
      '운영자는 기능 개선, 보안 점검, 시스템 장애 대응을 위해 서비스의 전부 또는 일부를 변경하거나 일시 중단할 수 있습니다.',
      '중대한 변경이 있는 경우 서비스 화면 또는 별도 공지 수단을 통해 안내합니다.',
    ],
  },
  {
    title: '8. 책임의 제한',
    items: [
      '운영자는 관련 법령에서 허용하는 범위 내에서 AI 응답의 정확성, 완전성, 특정 목적 적합성을 보장하지 않습니다.',
      '사용자가 AI 응답을 바탕으로 내린 판단이나 행동에 대해서는 사용자의 책임이 따릅니다.',
      '다만 운영자의 고의 또는 중대한 과실로 발생한 손해에 대해서는 관련 법령에 따릅니다.',
    ],
  },
  {
    title: '9. 문의 및 약관 변경',
    items: [
      '서비스 이용과 관련한 문의는 운영자가 지정한 고객 문의 채널을 통해 접수합니다.',
      '본 약관은 서비스 정책 변경에 따라 개정될 수 있으며, 중요한 변경 사항은 사전에 안내합니다.',
    ],
  },
] as const

export default function TermsPage() {
  const router = useRouter()
  const [themeTone, setThemeTone] = useState<ThemeTone>(DEFAULT_THEME_TONE)

  useEffect(() => {
    setThemeTone(readStoredThemeTone())
  }, [])

  return (
    <main className="page-shell policy-page-shell" data-theme-tone={themeTone}>
      <nav className="top-nav service-top-nav" aria-label="이용약관 메뉴">
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

      <article className="policy-document" aria-labelledby="terms-heading">
        <p className="eyebrow">예시 초안</p>
        <h1 id="terms-heading">이용약관</h1>
        <p className="policy-lead">
          이 문서는 Haru Mind 서비스 운영을 가정한 이용약관 예시입니다. 실제 배포 전에는 운영 주체, 유료 기능, 책임 범위,
          분쟁 처리 절차를 기준으로 반드시 검토해야 합니다.
        </p>
        <p className="policy-meta">시행 예정일: 운영 전 확정</p>

        {termsSections.map((section) => (
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
