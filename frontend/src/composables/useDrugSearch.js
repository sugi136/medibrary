import { ref } from 'vue'
import { drugApi } from '../api'
import { normalizeApiError } from '../utils/normalizeApiError'

export function useDrugSearch() {
  const mode = ref('name')
  const keyword = ref('')
  const shape = ref('')
  const color = ref('')
  const loading = ref(false)
  const error = ref('')
  const searched = ref(false)
  const items = ref([])

  async function search() {
    error.value = ''
    if (mode.value === 'name' && !keyword.value.trim()) {
      error.value = '약 이름을 입력해 주세요.'
      return
    }

    loading.value = true
    searched.value = true
    try {
      const params = mode.value === 'name'
        ? { name: keyword.value }
        : { shape: shape.value, color: color.value }
      items.value = (await drugApi.search(params)).data.items || []
    } catch (requestError) {
      error.value = normalizeApiError(requestError, '검색 정보를 불러오지 못했습니다.').message
      items.value = []
    } finally {
      loading.value = false
    }
  }

  return { mode, keyword, shape, color, loading, error, searched, items, search }
}
