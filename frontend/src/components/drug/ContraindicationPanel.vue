<script setup>
defineProps({
  loading: { type: Boolean, default: false },
  result: { type: Object, default: null },
  items: { type: Array, default: () => [] },
  showAll: { type: Boolean, default: false },
})

defineEmits(['retry', 'toggle-all'])
</script>

<template>
  <section class="tab-panel card">
    <h2>함께 먹으면 안 되는 약</h2>
    <p class="page-intro">현재 약과 병용할 때 금기 또는 주의가 필요한 의약품 정보입니다.</p>

    <div v-if="loading" class="loading">병용금기 정보를 확인하는 중입니다…</div>
    <template v-else-if="result?.available">
      <div v-if="!result.items?.length" class="notice">{{ result.message || '확인된 병용금기 정보가 없습니다' }}</div>
      <div v-else class="dur-list">
        <article v-for="item in items" :key="`${item.drugId}-${item.name}`" class="dur-item">
          <span class="badge" :class="item.type === 'CONTRAINDICATED' ? 'badge-danger' : 'badge-caution'">
            {{ item.type === 'CONTRAINDICATED' ? '금기' : '주의' }}
          </span>
          <div><h3>{{ item.name }}</h3><p>{{ item.reason || '병용 시 주의가 필요합니다.' }}</p></div>
        </article>
      </div>
      <button v-if="result.items.length > 3" class="button button-secondary" type="button" @click="$emit('toggle-all')">
        {{ showAll ? '접기' : '전체 보기' }}
      </button>
    </template>
    <div v-else class="notice">
      <p>{{ result?.message || '현재 병용금기 정보를 불러올 수 없습니다' }}</p>
      <button class="button button-secondary" type="button" @click="$emit('retry')">재시도</button>
    </div>
  </section>
</template>

<style scoped>
.tab-panel { padding: 28px; }
.tab-panel > h2 { margin-bottom: 8px; }
.dur-list { display: grid; gap: 10px; margin: 22px 0 14px; }
.dur-item { display: grid; grid-template-columns: auto 1fr; align-items: start; gap: 12px; padding: 17px; border: 1px solid var(--line); border-radius: 14px; }
.dur-item h3 { margin: 0 0 5px; }
.dur-item p { color: var(--muted); font-size: 14px; line-height: 1.6; }
@media (max-width: 600px) { .tab-panel { padding: 20px; } }
</style>
