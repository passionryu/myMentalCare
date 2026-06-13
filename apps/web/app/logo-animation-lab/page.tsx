const candidates = [
  {
    key: 'ink-write',
    name: '1. Ink Writing',
    note: '만년필로 써지는 느낌. 브랜드 의미와 가장 직접적으로 맞습니다.',
    className: 'ink-write',
  },
  {
    key: 'soft-reveal',
    name: '2. Soft Reveal',
    note: '조용히 선명해지는 방식. 가장 안정적이고 과하지 않습니다.',
    className: 'soft-reveal',
  },
  {
    key: 'letter-breathe',
    name: '3. Letter Breathe',
    note: '글자가 한 글자씩 놓이는 방식. 앱 첫 진입의 리듬감이 있습니다.',
    className: 'letter-breathe',
  },
  {
    key: 'underline-pen',
    name: '4. Underline Pen',
    note: '글자 자체는 정적이고, 아래 선만 써집니다. 구현 리스크가 낮습니다.',
    className: 'underline-pen',
  },
  {
    key: 'warm-ink',
    name: '5. Warm Ink',
    note: '잉크가 종이에 스며드는 듯한 페이드. 감성은 좋고 부담이 적습니다.',
    className: 'warm-ink',
  },
  {
    key: 'calm-signature',
    name: '6. Calm Signature',
    note: '서명처럼 짧게 등장합니다. 가장 브랜드 로고다운 후보입니다.',
    className: 'calm-signature',
  },
]

function AnimatedLogo({ className }: { className: string }) {
  const letters = 'Haru Mind'.split('')

  if (className === 'letter-breathe') {
    return (
      <h2 className={`logo-demo ${className}`} aria-label="Haru Mind">
        {letters.map((letter, index) => (
          <span key={`${letter}-${index}`} style={{ animationDelay: `${index * 80}ms` }}>
            {letter === ' ' ? '\u00a0' : letter}
          </span>
        ))}
      </h2>
    )
  }

  return <h2 className={`logo-demo ${className}`}>Haru Mind</h2>
}

export default function LogoAnimationLabPage() {
  return (
    <main className="logo-lab-page">
      <section className="logo-lab-hero">
        <p>Haru Mind brand motion lab</p>
        <h1>서비스명 애니메이션 후보</h1>
        <span>각 카드는 페이지를 새로고침하면 애니메이션이 다시 재생됩니다.</span>
      </section>

      <section className="logo-lab-grid" aria-label="Haru Mind 로고 애니메이션 후보">
        {candidates.map((candidate) => (
          <article className="logo-lab-card" key={candidate.key}>
            <div className="logo-stage">
              <AnimatedLogo className={candidate.className} />
            </div>
            <div className="logo-lab-copy">
              <strong>{candidate.name}</strong>
              <p>{candidate.note}</p>
            </div>
          </article>
        ))}
      </section>
    </main>
  )
}
