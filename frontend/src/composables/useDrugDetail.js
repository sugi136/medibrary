import { computed, ref, unref } from 'vue'
import { drugApi } from '../api'
import { normalizeApiError } from '../utils/normalizeApiError'

const SIDE_EFFECT_TAB = 'side'
const CONTRAINDICATION_TAB = 'dur'

export function useDrugDetail(drugId) {
  const drug = ref(null)
  const loading = ref(false)
  const error = ref('')
  const activeTab = ref('basic')
  const sideEffects = ref(null)
  const sideLoading = ref(false)
  const contraindications = ref(null)
  const contraindicationLoading = ref(false)
  const showAllContraindications = ref(false)

  const visibleContraindications = computed(() => {
    const items = contraindications.value?.items || []
    return showAllContraindications.value ? items : items.slice(0, 3)
  })

  async function loadDetail() {
    loading.value = true
    error.value = ''
    try {
      drug.value = (await drugApi.detail(unref(drugId))).data
    } catch (requestError) {
      error.value = normalizeApiError(requestError, '약 정보를 불러올 수 없습니다.').message
    } finally {
      loading.value = false
    }
  }

  async function selectTab(tabId) {
    activeTab.value = tabId
    if (tabId === SIDE_EFFECT_TAB && !sideEffects.value) await loadSideEffects()
    if (tabId === CONTRAINDICATION_TAB && !contraindications.value) await loadContraindications()
  }

  async function loadSideEffects() {
    sideLoading.value = true
    try {
      sideEffects.value = (await drugApi.sideEffects(unref(drugId))).data
    } catch (requestError) {
      const message = normalizeApiError(requestError, '현재 정보를 불러올 수 없습니다.').message
      sideEffects.value = {
        domestic: { available: false, cases: [], message },
        overseas: { available: false, cases: [], message },
      }
    } finally {
      sideLoading.value = false
    }
  }

  async function loadContraindications() {
    contraindicationLoading.value = true
    try {
      contraindications.value = (await drugApi.contraindications(unref(drugId))).data
    } catch (requestError) {
      contraindications.value = {
        available: false,
        items: [],
        message: normalizeApiError(requestError, '현재 병용금기 정보를 불러올 수 없습니다.').message,
      }
    } finally {
      contraindicationLoading.value = false
    }
  }

  return {
    drug,
    loading,
    error,
    activeTab,
    sideEffects,
    sideLoading,
    contraindications,
    contraindicationLoading,
    showAllContraindications,
    visibleContraindications,
    loadDetail,
    loadContraindications,
    selectTab,
  }
}
