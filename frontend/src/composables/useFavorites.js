import { ref } from 'vue'
import { favoriteApi } from '../api'
import { normalizeApiError } from '../utils/normalizeApiError'

export function useFavorites() {
  const favorites = ref([])
  const warnings = ref([])
  const loading = ref(false)
  const error = ref('')
  const deletingFavoriteId = ref(null)

  async function load() {
    loading.value = true
    error.value = ''
    try {
      const { data } = await favoriteApi.list()
      favorites.value = data.favorites || []
      warnings.value = data.durWarnings || []
    } catch (requestError) {
      error.value = normalizeApiError(requestError, '즐겨찾기 정보를 불러오지 못했습니다.').message
    } finally {
      loading.value = false
    }
  }

  async function remove(favoriteId) {
    deletingFavoriteId.value = favoriteId
    try {
      await favoriteApi.remove(favoriteId)
      await load()
      return true
    } catch (requestError) {
      error.value = normalizeApiError(requestError, '즐겨찾기 해제에 실패했습니다.').message
      return false
    } finally {
      deletingFavoriteId.value = null
    }
  }

  return { favorites, warnings, loading, error, deletingFavoriteId, load, remove }
}
