<script setup>
import { computed, onMounted, toRef } from 'vue'
import { useRouter } from 'vue-router'
import BasicInfoPanel from '../components/drug/BasicInfoPanel.vue'
import ContraindicationPanel from '../components/drug/ContraindicationPanel.vue'
import DrugSummaryHeader from '../components/drug/DrugSummaryHeader.vue'
import SideEffectPanel from '../components/drug/SideEffectPanel.vue'
import UsageInfoPanel from '../components/drug/UsageInfoPanel.vue'
import { useDrugDetail } from '../composables/useDrugDetail'
import { useFavoriteAction } from '../composables/useFavoriteAction'

const props = defineProps({
  id: { type: String, required: true },
})

const router = useRouter()
const detail = useDrugDetail(toRef(props, 'id'))
const favorite = useFavoriteAction(detail.drug)

const tabs = [
  { id: 'basic', label: '기본정보' },
  { id: 'usage', label: '효능·복용정보' },
  { id: 'side', label: '부작용' },
  { id: 'dur', label: '함께 먹으면 안 되는 약' },
]

const feedbackMessage = computed(() => favorite.error.value || detail.error.value)

onMounted(detail.loadDetail)
</script>

<template>
  <div v-if="detail.loading.value" class="loading">약 정보를 불러오는 중입니다…</div>
  <div v-else-if="detail.error.value && !detail.drug.value" class="notice">{{ detail.error.value }}</div>
  <section v-else-if="detail.drug.value" class="detail-page">
    <button class="back-link" type="button" @click="router.back()">‹ 검색 결과로 돌아가기</button>
    <DrugSummaryHeader
      :drug="detail.drug.value"
      :favorite-loading="favorite.loading.value"
      @favorite="favorite.add"
    />

    <div class="detail-tabs" role="tablist" aria-label="의약품 상세 정보 탭">
      <button
        v-for="tab in tabs"
        :key="tab.id"
        :class="{ active: detail.activeTab.value === tab.id }"
        type="button"
        @click="detail.selectTab(tab.id)"
      >
        {{ tab.label }}
      </button>
    </div>

    <BasicInfoPanel v-if="detail.activeTab.value === 'basic'" :drug="detail.drug.value" />
    <UsageInfoPanel v-else-if="detail.activeTab.value === 'usage'" :drug="detail.drug.value" />
    <SideEffectPanel
      v-else-if="detail.activeTab.value === 'side'"
      :loading="detail.sideLoading.value"
      :side-effects="detail.sideEffects.value"
    />
    <ContraindicationPanel
      v-else
      :loading="detail.contraindicationLoading.value"
      :result="detail.contraindications.value"
      :items="detail.visibleContraindications.value"
      :show-all="detail.showAllContraindications.value"
      :duplicate-warnings="detail.duplicateWarnings.value"
      :duplicate-loading="detail.duplicateWarningLoading.value"
      @retry="detail.loadContraindications"
      @toggle-all="detail.showAllContraindications.value = !detail.showAllContraindications.value"
    />
    <p v-if="feedbackMessage" class="error-message">{{ feedbackMessage }}</p>
  </section>
</template>

<style scoped>
.back-link { margin-bottom: 18px; padding: 0; border: 0; color: var(--muted); background: transparent; font-size: 14px; }
.detail-tabs { display: flex; gap: 6px; overflow-x: auto; margin: 28px 0 18px; border-bottom: 1px solid var(--line); }
.detail-tabs button { position: relative; flex: none; min-height: 46px; padding: 0 16px; border: 0; color: var(--muted); background: transparent; white-space: nowrap; font-size: 14px; font-weight: 700; }
.detail-tabs button.active { color: var(--primary-dark); }
.detail-tabs button.active::after { content: ''; position: absolute; right: 10px; bottom: 0; left: 10px; height: 3px; border-radius: 3px 3px 0 0; background: var(--primary-dark); }
</style>
