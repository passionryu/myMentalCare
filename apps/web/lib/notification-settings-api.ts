import { LoginApiError, requestWithAuth } from './auth-api'

export type NotificationWeekday = 'MON' | 'TUE' | 'WED' | 'THU' | 'FRI' | 'SAT' | 'SUN'

export type NotificationSettingResponse = {
  enabled: boolean
  notificationTime: string
  weekdays: NotificationWeekday[]
}

export type UpdateNotificationSettingRequest = NotificationSettingResponse

async function readJson(response: Response) {
  return response.json().catch(() => null)
}

export async function readNotificationSetting(): Promise<NotificationSettingResponse> {
  const response = await requestWithAuth('/api/members/me/notification-settings')
  const body = await readJson(response)

  if (!response.ok) {
    throw new LoginApiError(body?.message ?? '알림 설정을 불러오지 못했습니다.')
  }

  return body as NotificationSettingResponse
}

export async function updateNotificationSetting(request: UpdateNotificationSettingRequest): Promise<NotificationSettingResponse> {
  const response = await requestWithAuth('/api/members/me/notification-settings', {
    method: 'PATCH',
    headers: {
      'Content-Type': 'application/json',
    },
    body: JSON.stringify(request),
  })
  const body = await readJson(response)

  if (!response.ok) {
    throw new LoginApiError(body?.message ?? '알림 설정을 저장하지 못했습니다.')
  }

  return body as NotificationSettingResponse
}
