<script setup>
defineProps({
  loading: { type: Boolean, default: false },
  sideEffects: { type: Object, default: null },
})

const sources = [
  { key: 'domestic', label: '국내 e약은요' },
  { key: 'overseas', label: '해외 openFDA' },
]
</script>

<template>
  <section class="tab-panel card">
    <h2>부작용</h2>
    <div v-if="loading" class="loading">국내·해외 부작용 정보를 조회하는 중입니다…</div>
    <div v-else class="source-grid">
      <article v-for="source in sources" :key="source.key" class="source-card">
        <h3>{{ source.label }}</h3>
        <template v-if="sideEffects?.[source.key]?.available">
          <ul v-if="sideEffects[source.key].cases?.length">
            <li v-for="item in sideEffects[source.key].cases" :key="item">{{ item }}</li>
          </ul>
          <p v-else class="muted">등록된 부작용 사례가 없습니다.</p>
        </template>
        <p v-else class="muted">{{ sideEffects?.[source.key]?.message || '현재 정보를 불러올 수 없습니다.' }}</p>
      </article>
    </div>
  </section>
</template>

<style scoped>
.tab-panel { padding: 28px; }
.tab-panel > h2 { margin-bottom: 8px; }
.source-grid { display: grid; grid-template-columns: 1fr 1fr; gap: 16px; margin-top: 20px; }
.source-card { padding: 18px; border: 1px solid var(--line); border-radius: 14px; }
.source-card ul { margin: 0; padding-left: 18px; color: var(--muted); line-height: 1.8; }
.muted { color: var(--muted); line-height: 1.7; }
@media (max-width: 600px) { .tab-panel { padding: 20px; }.source-grid { grid-template-columns: 1fr; } }
</style>
