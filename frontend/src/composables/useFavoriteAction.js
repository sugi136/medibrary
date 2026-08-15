import { ref } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { favoriteApi } from '../api'
import { useAuthStore } from '../stores/auth'
import { normalizeApiError } from '../utils/normalizeApiError'

export function useFavoriteAction(drug) {
  const auth = useAuthStore()
  const route = useRoute()
  const router = useRouter()
  const loading = ref(false)
  const error = ref('')

  async function add() {
    error.value = ''
    if (!auth.isAuthenticated) {
      await router.push({ name: 'login', query: { redirect: route.fullPath } })
      return
    }
    if (!drug.value || drug.value.isFavorite) return

    loading.value = true
    try {
      await favoriteApi.create(drug.value.id)
      drug.value.isFavorite = true
    } catch (requestError) {
      error.value = normalizeApiError(requestError, '즐겨찾기 등록에 실패했습니다.').message
    } finally {
      loading.value = false
    }
  }

  return { loading, error, add }
}
