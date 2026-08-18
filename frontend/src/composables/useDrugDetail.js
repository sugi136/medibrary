import { ref, unref } from 'vue'
import { drugApi } from '../api'
import { useDrugSafety } from './useDrugSafety'
import { normalizeApiError } from '../utils/normalizeApiError'

const SIDE_EFFECT_TAB = 'side'
const CONTRAINDICATION_TAB = 'dur'
const OVERSEAS_PRODUCTS_TAB = 'overseas'

export function useDrugDetail(drugId) {
  const drug = ref(null)
  const loading = ref(false)
  const error = ref('')
  const activeTab = ref('basic')
  const safety = useDrugSafety(drugId)

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
    if (tabId === SIDE_EFFECT_TAB && !safety.sideEffects.value) await safety.loadSideEffects()
    if (tabId === CONTRAINDICATION_TAB && !safety.contraindications.value)
      await safety.loadContraindications()
    if (tabId === CONTRAINDICATION_TAB && !safety.duplicateWarnings.value)
      await safety.loadDuplicateWarnings()
    if (tabId === OVERSEAS_PRODUCTS_TAB && !safety.overseasProducts.value)
      await safety.loadOverseasProducts()
  }

  return {
    drug,
    loading,
    error,
    activeTab,
    loadDetail,
    selectTab,
    ...safety,
  }
}
