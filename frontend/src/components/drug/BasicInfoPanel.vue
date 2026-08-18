<script setup>
defineProps({
  drug: { type: Object, required: true },
})
</script>

<template>
  <section class="tab-panel card">
    <h2>기본정보</h2>
    <dl class="info-grid">
      <div><dt>제품명</dt><dd>{{ drug.name }}</dd></div>
      <div><dt>모양</dt><dd>{{ drug.shape || '등록된 정보가 없습니다' }}</dd></div>
      <div><dt>색깔</dt><dd>{{ drug.color || '등록된 정보가 없습니다' }}</dd></div>
      <div><dt>식별표시</dt><dd>{{ [drug.markFront, drug.markBack].filter(Boolean).join(' / ') || '등록된 정보가 없습니다' }}</dd></div>
      <div><dt>주성분</dt><dd>{{ drug.ingredientKr || drug.ingredientEn || '등록된 정보가 없습니다' }}<small v-if="drug.ingredientKr && drug.ingredientEn"> ({{ drug.ingredientEn }})</small></dd></div>
    </dl>
  </section>
</template>

<style scoped>
.tab-panel { padding: 28px; }
.tab-panel > h2 { margin-bottom: 8px; }
.info-grid { display: grid; grid-template-columns: repeat(2, minmax(0, 1fr)); gap: 0; margin: 20px 0 0; border-top: 1px solid var(--line); }
.info-grid div { display: grid; grid-template-columns: 110px 1fr; gap: 10px; padding: 15px 0; border-bottom: 1px solid var(--line); }
.info-grid dt { color: var(--muted); font-size: 13px; }
.info-grid dd { margin: 0; font-size: 14px; }.info-grid dd small { color: var(--muted); font-size: 12px; }
@media (max-width: 600px) { .tab-panel { padding: 20px; }.info-grid { grid-template-columns: 1fr; }.info-grid div { grid-template-columns: 92px 1fr; } }
</style>
