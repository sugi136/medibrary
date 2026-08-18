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
  const duplicateWarnings = ref(null)
  const duplicateWarningLoading = ref(false)
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
    if (tabId === CONTRAINDICATION_TAB && !duplicateWarnings.value) await loadDuplicateWarnings()
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
        disclaimer: 'FAERS 자발보고 건수는 발생 빈도나 인과관계를 뜻하지 않습니다. 처방량으로 보정되지 않았습니다.',
      }
    } finally {
      sideLoading.value = false
    }
  }

  async function loadDuplicateWarnings() {
    duplicateWarningLoading.value = true
    try {
      duplicateWarnings.value = (await drugApi.duplicates(unref(drugId))).data
    } catch (requestError) {
      duplicateWarnings.value = {
        available: false,
        sameIngredientItems: [],
        efficacyGroupItems: [],
        message: normalizeApiError(requestError, '현재 중복 복용 주의 정보를 불러올 수 없습니다.').message,
      }
    } finally {
      duplicateWarningLoading.value = false
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
    duplicateWarnings,
    duplicateWarningLoading,
    showAllContraindications,
    visibleContraindications,
    loadDetail,
    loadContraindications,
    loadDuplicateWarnings,
    selectTab,
  }
}
