<script setup>
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
    <p class="page-intro">병용금기뿐 아니라 즐겨찾기 약의 동일 성분 및 DUR 효능군 중복 가능성을 함께 확인합니다.</p>

    <section class="duplicate-section" aria-label="중복 복용 경고">
      <div class="section-heading">
        <h3>성분·효능군 중복 확인</h3>
        <span class="source-badge">DUR</span>
      </div>
      <div v-if="duplicateLoading" class="inline-loading">중복 복용 주의 정보를 확인하는 중입니다…</div>
      <template v-else-if="duplicateWarnings?.available">
        <div class="warning-grid">
          <article class="warning-card same-ingredient">
            <h4>동일 성분</h4>
            <template v-if="duplicateWarnings.sameIngredientItems?.length">
              <ul>
                <li v-for="item in duplicateWarnings.sameIngredientItems" :key="item.drugId">
                  <strong>{{ item.name }}</strong><span>{{ item.reason }}</span>
                </li>
              </ul>
            </template>
            <p v-else class="muted">즐겨찾기 약과의 동일 성분 중복은 확인되지 않았습니다.</p>
            <p class="helper">로그인 후 즐겨찾기 약을 기준으로 확인합니다.</p>
          </article>
          <article class="warning-card efficacy-group">
            <h4>효능군 중복</h4>
            <template v-if="duplicateWarnings.efficacyGroupItems?.length">
              <ul>
                <li v-for="item in duplicateWarnings.efficacyGroupItems.slice(0, 3)" :key="`${item.drugId}-${item.name}`">
                  <strong>{{ item.name }}</strong><span>{{ item.reason || '같은 효능군으로 분류된 의약품입니다.' }}</span>
                </li>
              </ul>
            </template>
            <p v-else class="muted">확인된 효능군 중복 의약품이 없습니다.</p>
          </article>
        </div>
      </template>
      <p v-else class="notice compact">{{ duplicateWarnings?.message || '현재 중복 복용 주의 정보를 불러올 수 없습니다.' }}</p>
    </section>

    <section class="contraindication-section" aria-label="병용금기 정보">
      <div class="section-heading">
        <h3>병용금기·주의</h3>
        <span class="source-badge">DUR</span>
      </div>
      <div v-if="loading" class="loading">병용금기 정보를 확인하는 중입니다…</div>
      <template v-else-if="result?.available">
        <div v-if="!result.items?.length" class="notice">{{ result.message || '확인된 병용금기 정보가 없습니다' }}</div>
        <div v-else class="dur-list">
          <article v-for="item in items" :key="`${item.drugId}-${item.name}`" class="dur-item">
            <span class="badge" :class="item.type === 'CONTRAINDICATED' ? 'badge-danger' : 'badge-caution'">
              {{ item.type === 'CONTRAINDICATED' ? '금기' : '주의' }}
            </span>
            <div><h4>{{ item.name }}</h4><p>{{ item.reason || '병용 시 주의가 필요합니다.' }}</p></div>
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
  </section>
</template>

<style scoped>
.tab-panel { padding: 28px; }
.eyebrow { margin: 0 0 4px; color: var(--primary-dark); font-size: 12px; font-weight: 800; letter-spacing: .04em; }
.tab-panel > h2 { margin: 0; }
.page-intro { margin: 8px 0 0; color: var(--muted); font-size: 14px; }
.duplicate-section, .contraindication-section { margin-top: 24px; }
.contraindication-section { padding-top: 24px; border-top: 1px solid var(--line); }
.section-heading { display: flex; align-items: center; justify-content: space-between; gap: 12px; }
.section-heading h3 { margin: 0; font-size: 17px; }
.source-badge { padding: 5px 8px; border-radius: 8px; color: var(--primary-dark); background: var(--primary-soft); font-size: 11px; font-weight: 800; }
.warning-grid { display: grid; grid-template-columns: 1fr 1fr; gap: 12px; margin-top: 14px; }
.warning-card { padding: 16px; border: 1px solid var(--line); border-radius: 14px; }
.same-ingredient { border-top: 4px solid #eab34a; background: #fffdf6; }
.efficacy-group { border-top: 4px solid var(--primary); }
.warning-card h4, .dur-item h4 { margin: 0 0 7px; }
.warning-card ul { display: grid; gap: 9px; margin: 0; padding: 0; list-style: none; }
.warning-card li { display: grid; gap: 3px; color: var(--muted); font-size: 13px; line-height: 1.5; }
.warning-card strong { color: var(--text); }
.helper { margin: 10px 0 0; color: var(--muted); font-size: 11px; line-height: 1.5; }
.inline-loading { margin-top: 14px; color: var(--muted); }
.dur-list { display: grid; gap: 10px; margin: 16px 0 14px; }
.dur-item { display: grid; grid-template-columns: auto 1fr; align-items: start; gap: 12px; padding: 17px; border: 1px solid var(--line); border-radius: 14px; }
.dur-item p { margin: 0; color: var(--muted); font-size: 14px; line-height: 1.6; }
.notice { margin: 16px 0 0; color: var(--muted); }
.notice.compact { padding: 14px; border: 1px solid var(--line); border-radius: 12px; }
.muted { margin: 0; color: var(--muted); font-size: 13px; line-height: 1.6; }
@media (max-width: 700px) { .tab-panel { padding: 20px; }.warning-grid { grid-template-columns: 1fr; } }
</style>
