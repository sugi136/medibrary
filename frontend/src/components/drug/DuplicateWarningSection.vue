<script setup>
defineProps({
  warnings: { type: Object, default: null },
  loading: { type: Boolean, default: false },
})
</script>

<template>
  <section class="duplicate-section" aria-label="중복 복용 경고">
    <div class="section-heading">
      <h3>성분·효능군 중복 확인</h3>
      <span class="source-badge">DUR</span>
    </div>
    <div v-if="loading" class="inline-loading">중복 복용 주의 정보를 확인하는 중입니다…</div>
    <template v-else-if="warnings?.available">
      <div class="warning-grid">
        <article class="warning-card same-ingredient">
          <h4>동일 성분</h4>
          <template v-if="warnings.sameIngredientItems?.length">
            <ul>
              <li v-for="item in warnings.sameIngredientItems" :key="item.drugId">
                <strong>{{ item.name }}</strong
                ><span>{{ item.reason }}</span>
              </li>
            </ul>
          </template>
          <p v-else class="muted">즐겨찾기 약과의 동일 성분 중복은 확인되지 않았습니다.</p>
          <p class="helper">로그인 후 즐겨찾기 약을 기준으로 확인합니다.</p>
        </article>
        <article class="warning-card efficacy-group">
          <h4>효능군 중복</h4>
          <template v-if="warnings.efficacyGroupItems?.length">
            <ul>
              <li
                v-for="item in warnings.efficacyGroupItems.slice(0, 3)"
                :key="`${item.drugId}-${item.name}`"
              >
                <strong>{{ item.name }}</strong
                ><span>{{ item.reason || '같은 효능군으로 분류된 의약품입니다.' }}</span>
              </li>
            </ul>
          </template>
          <p v-else class="muted">확인된 효능군 중복 의약품이 없습니다.</p>
        </article>
      </div>
    </template>
    <p v-else class="notice compact">
      {{ warnings?.message || '현재 중복 복용 주의 정보를 불러올 수 없습니다.' }}
    </p>
  </section>
</template>

<style scoped>
.duplicate-section {
  margin-top: 24px;
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
.warning-grid {
  display: grid;
  grid-template-columns: 1fr 1fr;
  gap: 12px;
  margin-top: 14px;
}
.warning-card {
  padding: 16px;
  border: 1px solid var(--line);
  border-radius: 14px;
}
.same-ingredient {
  border-top: 4px solid #eab34a;
  background: #fffdf6;
}
.efficacy-group {
  border-top: 4px solid var(--primary);
}
.warning-card h4 {
  margin: 0 0 7px;
}
.warning-card ul {
  display: grid;
  gap: 9px;
  margin: 0;
  padding: 0;
  list-style: none;
}
.warning-card li {
  display: grid;
  gap: 3px;
  color: var(--muted);
  font-size: 13px;
  line-height: 1.5;
}
.warning-card strong {
  color: var(--text);
}
.helper {
  margin: 10px 0 0;
  color: var(--muted);
  font-size: 11px;
  line-height: 1.5;
}
.inline-loading {
  margin-top: 14px;
  color: var(--muted);
}
.notice {
  margin: 16px 0 0;
  color: var(--muted);
}
.notice.compact {
  padding: 14px;
  border: 1px solid var(--line);
  border-radius: 12px;
}
.muted {
  margin: 0;
  color: var(--muted);
  font-size: 13px;
  line-height: 1.6;
}
@media (max-width: 700px) {
  .warning-grid {
    grid-template-columns: 1fr;
  }
}
</style>
