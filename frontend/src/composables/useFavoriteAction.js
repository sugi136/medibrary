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

  async function toggle() {
    error.value = ''
    if (!auth.isAuthenticated) {
      await router.push({ name: 'login', query: { redirect: route.fullPath } })
      return
    }
    if (!drug.value || loading.value) return

    loading.value = true
    try {
      if (drug.value.isFavorite) {
        await favoriteApi.removeByDrugId(drug.value.id)
        drug.value.isFavorite = false
      } else {
        await favoriteApi.create(drug.value.id)
        drug.value.isFavorite = true
      }
    } catch (requestError) {
      const fallbackMessage = drug.value.isFavorite
        ? '즐겨찾기 해제에 실패했습니다.'
        : '즐겨찾기 등록에 실패했습니다.'
      error.value = normalizeApiError(requestError, fallbackMessage).message
    } finally {
      loading.value = false
    }
  }

  return { loading, error, toggle }
}
