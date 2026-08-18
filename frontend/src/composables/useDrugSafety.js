import { computed, ref, unref } from 'vue'
import { drugApi } from '../api'
import { normalizeApiError } from '../utils/normalizeApiError'

export function useDrugSafety(drugId) {
  const sideEffects = ref(null)
  const sideLoading = ref(false)
  const contraindications = ref(null)
  const contraindicationLoading = ref(false)
  const duplicateWarnings = ref(null)
  const duplicateWarningLoading = ref(false)
  const overseasProducts = ref(null)
  const overseasProductsLoading = ref(false)
  const showAllContraindications = ref(false)

  const visibleContraindications = computed(() => {
    const items = contraindications.value?.items || []
    return showAllContraindications.value ? items : items.slice(0, 3)
  })

  async function loadSideEffects() {
    sideLoading.value = true
    try {
      sideEffects.value = (await drugApi.sideEffects(unref(drugId))).data
    } catch (requestError) {
      const message = normalizeApiError(requestError, '현재 정보를 불러올 수 없습니다.').message
      sideEffects.value = {
        domestic: { available: false, cases: [], message },
        overseas: { available: false, cases: [], message },
        disclaimer:
          'FAERS 자발보고 건수는 발생 빈도나 인과관계를 뜻하지 않습니다. 처방량으로 보정되지 않았습니다.',
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
        message: normalizeApiError(requestError, '현재 중복 복용 주의 정보를 불러올 수 없습니다.')
          .message,
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
        message: normalizeApiError(requestError, '현재 병용금기 정보를 불러올 수 없습니다.')
          .message,
      }
    } finally {
      contraindicationLoading.value = false
    }
  }

  async function loadOverseasProducts() {
    overseasProductsLoading.value = true
    try {
      overseasProducts.value = (await drugApi.overseasProducts(unref(drugId))).data
    } catch (requestError) {
      overseasProducts.value = {
        available: false,
        items: [],
        message: normalizeApiError(requestError, '현재 해외 제품 정보를 불러올 수 없습니다.')
          .message,
      }
    } finally {
      overseasProductsLoading.value = false
    }
  }

  return {
    sideEffects,
    sideLoading,
    contraindications,
    contraindicationLoading,
    duplicateWarnings,
    duplicateWarningLoading,
    overseasProducts,
    overseasProductsLoading,
    showAllContraindications,
    visibleContraindications,
    loadSideEffects,
    loadDuplicateWarnings,
    loadContraindications,
    loadOverseasProducts,
  }
}
