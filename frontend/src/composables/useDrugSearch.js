import { computed, ref } from 'vue'
import { drugApi } from '../api'
import { normalizeApiError } from '../utils/normalizeApiError'

const DEFAULT_PAGE_SIZE = 20
const COLOR_ALIASES = { 흰색: '하양' }

function normalizeColor(color) {
  return COLOR_ALIASES[color?.trim()] || color?.trim() || ''
}

export function useDrugSearch() {
  const mode = ref('name')
  const keyword = ref('')
  const shape = ref('')
  const color = ref('')
  const loading = ref(false)
  const error = ref('')
  const searched = ref(false)
  const items = ref([])
  const totalCount = ref(0)
  const page = ref(0)
  const size = ref(DEFAULT_PAGE_SIZE)
  const hasNext = ref(false)
  const totalPages = computed(() =>
    totalCount.value ? Math.ceil(totalCount.value / size.value) : 0,
  )

  async function search(requestedPage = 0) {
    error.value = ''
    if (mode.value === 'name' && !keyword.value.trim()) {
      error.value = '약 이름을 입력해 주세요.'
      return
    }
    if (mode.value === 'appearance' && !shape.value && !color.value) {
      error.value = '약 모양 또는 색깔을 하나 이상 선택해 주세요.'
      return
    }

    loading.value = true
    searched.value = true
    color.value = normalizeColor(color.value)
    try {
      const params =
        mode.value === 'name'
          ? { name: keyword.value.trim(), page: requestedPage, size: size.value }
          : {
              shape: shape.value,
              color: color.value,
              page: requestedPage,
              size: size.value,
            }
      const { data } = await drugApi.search(params)
      items.value = data.items || []
      totalCount.value = Number.isFinite(data.totalCount) ? data.totalCount : items.value.length
      page.value = Number.isInteger(data.page) ? data.page : requestedPage
      size.value = Number.isInteger(data.size) && data.size > 0 ? data.size : DEFAULT_PAGE_SIZE
      hasNext.value = Boolean(data.hasNext)
    } catch (requestError) {
      error.value = normalizeApiError(requestError, '검색 정보를 불러오지 못했습니다.').message
      items.value = []
      totalCount.value = 0
      page.value = 0
      hasNext.value = false
    } finally {
      loading.value = false
    }
  }

  function nextPage() {
    if (!loading.value && hasNext.value) search(page.value + 1)
  }

  function previousPage() {
    if (!loading.value && page.value > 0) search(page.value - 1)
  }

  return {
    mode,
    keyword,
    shape,
    color,
    loading,
    error,
    searched,
    items,
    totalCount,
    page,
    size,
    hasNext,
    totalPages,
    normalizeColor,
    search,
    nextPage,
    previousPage,
  }
}
