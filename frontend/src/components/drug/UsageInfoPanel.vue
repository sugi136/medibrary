<script setup>
defineProps({
  drug: { type: Object, required: true },
})

const sections = [
  { key: 'efficacy', label: '효능·효과' },
  { key: 'usage', label: '용법·용량' },
  { key: 'caution', label: '주의사항' },
]
</script>

<template>
  <section class="tab-panel card content-panel">
    <h2>효능·복용정보</h2>
    <div v-for="section in sections" :key="section.key" class="text-section">
      <h3>{{ section.label }}</h3>
      <p>{{ drug[section.key] || '등록된 정보가 없습니다' }}</p>
    </div>
  </section>
</template>

<style scoped>
.tab-panel {
  display: grid;
  gap: 25px;
  padding: 28px;
}
.tab-panel > h2 {
  margin-bottom: -12px;
}
.text-section h3 {
  color: var(--primary-dark);
}
.text-section p {
  color: var(--muted);
  line-height: 1.8;
  white-space: pre-line;
}
@media (max-width: 600px) {
  .tab-panel {
    padding: 20px;
  }
}
</style>
