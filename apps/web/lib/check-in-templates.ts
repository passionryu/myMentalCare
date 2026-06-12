import type { CheckInTemplateType } from '@/lib/ai-chat-api'

export type CheckInOption = {
  optionKey: string
  label: string
}

export type CheckInStep =
  | {
      stepKey: string
      question: string
      type: 'choice'
      options: CheckInOption[]
    }
  | {
      stepKey: string
      question: string
      type: 'scale'
      min: number
      max: number
    }

export type CheckInTemplateDefinition = {
  type: CheckInTemplateType
  title: string
  description: string
  steps: CheckInStep[]
}

export const PENDING_CHECK_IN_TEMPLATE_STORAGE_KEY = 'myMentalCare.pendingCheckInTemplate'
export const PENDING_CHECK_IN_SELECTOR_STORAGE_KEY = 'myMentalCare.pendingCheckInSelector'

export const CHECK_IN_TEMPLATES: CheckInTemplateDefinition[] = [
  {
    type: 'BASIC_EMOTION',
    title: '기본 감정형',
    description: '감정, 강도, 이유를 1분 안에 선택',
    steps: [
      {
        stepKey: 'emotion',
        question: '지금 마음은 어떤가요?',
        type: 'choice',
        options: [
          { optionKey: 'OKAY', label: '괜찮음' },
          { optionKey: 'ANXIOUS', label: '불안함' },
          { optionKey: 'TIRED', label: '지침' },
          { optionKey: 'DEPRESSED', label: '우울함' },
          { optionKey: 'ANGRY', label: '화남' },
        ],
      },
      {
        stepKey: 'intensity',
        question: '그 정도는 어느 정도인가요?',
        type: 'scale',
        min: 1,
        max: 5,
      },
      {
        stepKey: 'reason',
        question: '무엇 때문인 것 같나요?',
        type: 'choice',
        options: [
          { optionKey: 'WORK_STUDY', label: '일/공부' },
          { optionKey: 'RELATIONSHIP', label: '인간관계' },
          { optionKey: 'FAMILY', label: '가족' },
          { optionKey: 'HEALTH', label: '건강' },
          { optionKey: 'MONEY', label: '돈' },
          { optionKey: 'OTHER', label: '기타' },
        ],
      },
    ],
  },
  {
    type: 'CONVERSATION_START',
    title: '대화 시작형',
    description: '걱정, 위로, 정리, 조언, 그냥 들어주기',
    steps: [
      {
        stepKey: 'topic',
        question: '지금 어떤 이야기를 하고 싶나요?',
        type: 'choice',
        options: [
          { optionKey: 'WORRY', label: '걱정' },
          { optionKey: 'COMFORT', label: '위로' },
          { optionKey: 'ORGANIZE', label: '정리' },
          { optionKey: 'ADVICE', label: '조언' },
          { optionKey: 'LISTEN', label: '그냥 들어주기' },
        ],
      },
      {
        stepKey: 'responseStyle',
        question: '마음이는 어떻게 반응하면 좋을까요?',
        type: 'choice',
        options: [
          { optionKey: 'CALM', label: '차분하게' },
          { optionKey: 'REALISTIC', label: '현실적으로' },
          { optionKey: 'WARM', label: '따뜻하게' },
          { optionKey: 'SHORT', label: '짧게' },
          { optionKey: 'OTHER', label: '기타' },
        ],
      },
    ],
  },
  {
    type: 'CONDITION',
    title: '컨디션 중심형',
    description: '몸과 마음의 에너지 상태로 시작',
    steps: [
      {
        stepKey: 'energy',
        question: '지금 몸과 마음의 에너지는 어떤가요?',
        type: 'choice',
        options: [
          { optionKey: 'ENOUGH', label: '충분함' },
          { optionKey: 'NORMAL', label: '보통' },
          { optionKey: 'LOW', label: '부족함' },
          { optionKey: 'EMPTY', label: '거의 없음' },
        ],
      },
      {
        stepKey: 'factor',
        question: '오늘 가장 크게 영향을 준 것은 무엇인가요?',
        type: 'choice',
        options: [
          { optionKey: 'SLEEP', label: '수면' },
          { optionKey: 'WORK_STUDY', label: '업무·학업' },
          { optionKey: 'PEOPLE', label: '사람' },
          { optionKey: 'HEALTH', label: '건강' },
          { optionKey: 'NOTHING_SPECIAL', label: '특별한 일 없음' },
          { optionKey: 'OTHER', label: '기타' },
        ],
      },
    ],
  },
  {
    type: 'DAY_REVIEW',
    title: '하루 회고형',
    description: '오늘 하루의 느낌과 마무리 방식 선택',
    steps: [
      {
        stepKey: 'day',
        question: '오늘 하루는 어땠나요?',
        type: 'choice',
        options: [
          { optionKey: 'OKAY', label: '괜찮았음' },
          { optionKey: 'AMBIGUOUS', label: '애매했음' },
          { optionKey: 'HARD', label: '힘들었음' },
          { optionKey: 'VERY_HARD', label: '많이 힘들었음' },
        ],
      },
      {
        stepKey: 'remainingEmotion',
        question: '오늘 가장 많이 남은 감정은 무엇인가요?',
        type: 'choice',
        options: [
          { optionKey: 'RELIEF', label: '안도' },
          { optionKey: 'ANXIETY', label: '불안' },
          { optionKey: 'FATIGUE', label: '피로' },
          { optionKey: 'REGRET', label: '후회' },
          { optionKey: 'ANGER', label: '분노' },
          { optionKey: 'LONELINESS', label: '외로움' },
          { optionKey: 'OTHER', label: '기타' },
        ],
      },
      {
        stepKey: 'closing',
        question: '오늘을 어떻게 마무리하고 싶나요?',
        type: 'choice',
        options: [
          { optionKey: 'ORGANIZE', label: '정리하기' },
          { optionKey: 'LET_GO', label: '내려놓기' },
          { optionKey: 'PREPARE_TOMORROW', label: '내일 준비하기' },
          { optionKey: 'REST', label: '그냥 쉬기' },
          { optionKey: 'OTHER', label: '기타' },
        ],
      },
    ],
  },
]

export function findCheckInTemplate(type: string | null | undefined): CheckInTemplateDefinition | null {
  if (!type) {
    return null
  }

  return CHECK_IN_TEMPLATES.find((template) => template.type === type) ?? null
}
