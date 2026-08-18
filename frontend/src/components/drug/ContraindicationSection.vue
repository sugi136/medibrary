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
  <section class="contraindication-section" aria-label="병용금기 정보">
    <div class="section-heading">
      <h3>병용금기·주의</h3>
      <span class="source-badge">DUR</span>
    </div>
    <div v-if="loading" class="loading">병용금기 정보를 확인하는 중입니다…</div>
    <template v-else-if="result?.available">
      <div v-if="!result.items?.length" class="notice">
        {{ result.message || '확인된 병용금기 정보가 없습니다' }}
      </div>
      <div v-else class="dur-list">
        <article v-for="item in items" :key="`${item.drugId}-${item.name}`" class="dur-item">
          <span
            class="badge"
            :class="item.type === 'CONTRAINDICATED' ? 'badge-danger' : 'badge-caution'"
          >
            {{ item.type === 'CONTRAINDICATED' ? '금기' : '주의' }}
          </span>
          <div>
            <h4>{{ item.name }}</h4>
            <p>{{ item.reason || '병용 시 주의가 필요합니다.' }}</p>
          </div>
        </article>
      </div>
      <button
        v-if="result.items.length > 3"
        class="button button-secondary"
        type="button"
        @click="$emit('toggle-all')"
      >
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
.contraindication-section {
  margin-top: 24px;
  padding-top: 24px;
  border-top: 1px solid var(--line);
}
.section-heading {
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 12px;
}
.section-heading h3 {
  margin: 0;
  font-size: 17px;
}
.source-badge {
  padding: 5px 8px;
  border-radius: 8px;
  color: var(--primary-dark);
  background: var(--primary-soft);
  font-size: 11px;
  font-weight: 800;
}
.dur-list {
  display: grid;
  gap: 10px;
  margin: 16px 0 14px;
}
.dur-item {
  display: grid;
  grid-template-columns: auto 1fr;
  align-items: start;
  gap: 12px;
  padding: 17px;
  border: 1px solid var(--line);
  border-radius: 14px;
}
.dur-item h4 {
  margin: 0 0 7px;
}
.dur-item p {
  margin: 0;
  color: var(--muted);
  font-size: 14px;
  line-height: 1.6;
}
.notice {
  margin: 16px 0 0;
  color: var(--muted);
}
</style>
