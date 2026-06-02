export default function Page() {
  return (
    <main className="min-h-screen bg-[#f7f3ea] px-6 py-10 text-[#202124]">
      <section className="mx-auto flex max-w-4xl flex-col gap-8">
        <div className="space-y-4">
          <p className="text-sm font-semibold text-[#4d6b57]">myMentalCare</p>
          <h1 className="text-4xl font-semibold tracking-normal">오늘의 마음 상태를 차분히 기록합니다.</h1>
          <p className="max-w-2xl text-lg leading-8 text-[#5d625b]">
            이 프로젝트는 감정, 컨디션, 루틴, 회고를 개인이 꾸준히 관리할 수 있게 돕는 서비스로 시작합니다.
          </p>
        </div>

        <div className="grid gap-3 sm:grid-cols-3">
          {['감정 기록', '컨디션 체크', '회고 루틴'].map((label) => (
            <div key={label} className="rounded-lg border border-[#ded7c8] bg-white/70 p-5">
              <p className="text-base font-medium">{label}</p>
              <p className="mt-2 text-sm leading-6 text-[#6c7069]">첫 기능 이슈에서 구체적인 사용자 흐름을 정의합니다.</p>
            </div>
          ))}
        </div>
      </section>
    </main>
  )
}
