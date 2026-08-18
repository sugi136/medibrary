<script setup>
defineProps({
  loading: { type: Boolean, default: false },
  result: { type: Object, default: null },
})

defineEmits(['retry'])
</script>

<template>
  <section class="tab-panel card">
    <p class="eyebrow">REQ-F-015 · 미국 유통 제품 참고 정보</p>
    <h2>해외에서는 이런 약</h2>
    <p class="page-intro">
      이 약과 <strong>주성분이 같은</strong> 미국 유통 제품입니다. 동일 제품을 뜻하지 않으며
      성분·제형 비교를 위한 참고 정보입니다.
    </p>

    <div v-if="loading" class="loading">해외 제품 정보를 확인하는 중입니다…</div>

    <template v-else-if="result?.available">
      <p v-if="result.ingredientEn" class="basis">
        조회 기준 성분 <code>{{ result.ingredientEn }}</code>
      </p>
      <div v-if="!result.items?.length" class="notice">
        {{ result.message || '미국에서 유통 중인 동일 성분 제품을 찾지 못했습니다.' }}
      </div>
      <ul v-else class="product-list">
        <li
          v-for="item in result.items"
          :key="`${item.name}-${item.productNdc}`"
          class="product-item"
        >
          <div class="product-head">
            <div>
              <h3>{{ item.name }}</h3>
              <p v-if="item.genericName" class="generic-name">{{ item.genericName }}</p>
            </div>
            <span class="country-badge">미국 · {{ item.country }}</span>
          </div>
          <p v-if="item.activeIngredients" class="ingredients">{{ item.activeIngredients }}</p>
          <p v-if="item.dosageForm || item.route || item.labeler" class="meta">
            <span v-if="item.dosageForm">{{ item.dosageForm }}</span>
            <span v-if="item.route">{{ item.route }}</span>
            <span v-if="item.labeler">{{ item.labeler }}</span>
          </p>
        </li>
      </ul>
    </template>

    <div v-else class="notice error-notice">
      <p>{{ result?.message || '현재 해외 제품 정보를 불러올 수 없습니다.' }}</p>
      <button class="button button-secondary" type="button" @click="$emit('retry')">재시도</button>
    </div>

    <p v-if="result?.disclaimer" class="disclaimer">
      <strong>유의사항.</strong> {{ result.disclaimer }}
    </p>
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
  line-height: 1.65;
}
.basis {
  margin: 18px 0 10px;
  color: var(--muted);
  font-size: 13px;
}
.basis code {
  padding: 2px 7px;
  border-radius: 6px;
  background: var(--surface-alt, #f1f6f9);
  font-size: 13px;
}
.product-list {
  display: grid;
  gap: 10px;
  margin: 16px 0;
  padding: 0;
  list-style: none;
}
.product-item {
  padding: 16px 17px;
  border: 1px solid var(--line);
  border-radius: 14px;
}
.product-head {
  display: flex;
  align-items: flex-start;
  justify-content: space-between;
  gap: 10px;
}
.product-head h3 {
  margin: 0;
  font-size: 15px;
}
.generic-name {
  margin: 4px 0 0;
  color: var(--muted);
  font-size: 12px;
}
.country-badge {
  flex: none;
  padding: 4px 7px;
  border-radius: 7px;
  color: var(--primary-dark);
  background: var(--primary-soft);
  font-size: 11px;
  font-weight: 700;
}
.ingredients {
  margin: 9px 0 0;
  color: var(--primary-dark);
  font-size: 13px;
}
.meta {
  display: flex;
  flex-wrap: wrap;
  gap: 6px 12px;
  margin: 8px 0 0;
  color: var(--muted);
  font-size: 12px;
}
.notice {
  margin: 16px 0;
  color: var(--muted);
}
.error-notice {
  padding: 14px;
  border: 1px solid var(--line);
  border-radius: 12px;
}
.error-notice p {
  margin: 0 0 12px;
}
.disclaimer {
  margin: 18px 0 0;
  padding: 13px 15px;
  border-radius: 12px;
  background: var(--surface-alt, #f7f9fb);
  color: var(--muted);
  font-size: 12px;
  line-height: 1.65;
}
@media (max-width: 600px) {
  .tab-panel {
    padding: 20px;
  }
  .product-head {
    flex-direction: column;
    gap: 6px;
  }
}
</style>
