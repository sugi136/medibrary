<script setup>
import ContraindicationSection from './ContraindicationSection.vue'
import DuplicateWarningSection from './DuplicateWarningSection.vue'

defineProps({
  loading: { type: Boolean, default: false },
  result: { type: Object, default: null },
  items: { type: Array, default: () => [] },
  showAll: { type: Boolean, default: false },
  duplicateWarnings: { type: Object, default: null },
  duplicateLoading: { type: Boolean, default: false },
})

defineEmits(['retry', 'toggle-all'])
</script>

<template>
  <section class="tab-panel card">
    <p class="eyebrow">DUR 기반 복약 안전 점검</p>
    <h2>함께 복용할 때 주의할 점</h2>
    <p class="page-intro">
      병용금기뿐 아니라 즐겨찾기 약의 동일 성분 및 DUR 효능군 중복 가능성을 함께 확인합니다.
    </p>

    <DuplicateWarningSection :warnings="duplicateWarnings" :loading="duplicateLoading" />
    <ContraindicationSection
      :loading="loading"
      :result="result"
      :items="items"
      :show-all="showAll"
      @retry="$emit('retry')"
      @toggle-all="$emit('toggle-all')"
    />
  </section>
</template>

<style scoped>
.tab-panel {
  padding: 28px;
}
.eyebrow {
  margin: 0 0 4px;
  color: var(--primary-dark);
  font-size: 12px;
  font-weight: 800;
  letter-spacing: 0.04em;
}
.tab-panel > h2 {
  margin: 0;
}
.page-intro {
  margin: 8px 0 0;
  color: var(--muted);
  font-size: 14px;
}
@media (max-width: 700px) {
  .tab-panel {
    padding: 20px;
  }
}
</style>
