export function normalizeApiError(error, fallbackMessage = '요청을 처리하지 못했습니다. 잠시 후 다시 시도해 주세요.') {
  const response = error?.response
  if (!response) {
    return { status: 0, message: '네트워크 연결을 확인한 뒤 다시 시도해 주세요.', fieldErrors: {} }
  }

  const payload = response.data || {}
  return {
    status: response.status,
    message: payload.message || fallbackMessage,
    fieldErrors: payload.fieldErrors || {},
  }
}
