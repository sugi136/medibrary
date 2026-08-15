<script setup>
import { useRouter } from 'vue-router'
import DrugCard from '../components/drug/DrugCard.vue'
import { useDrugSearch } from '../composables/useDrugSearch'

const router = useRouter()
const searchState = useDrugSearch()

function openDetail(drug) {
  router.push({ name: 'drug-detail', params: { id: drug.id } })
}
</script>

<template>
  <section class="search-hero">
    <div class="hero-copy">
      <span class="eyebrow">내 약 정보 검색 서비스</span>
      <h1>궁금한 약, 더 쉽고<br /><em>안전하게</em> 확인하세요.</h1>
      <p>약 이름 또는 알약의 모양과 색깔로 검색하고, 효능·부작용·병용 주의 정보를 한곳에서 살펴보세요.</p>
    </div>

    <div class="search-panel card">
      <div class="mode-tabs" role="tablist" aria-label="검색 방식">
        <button :class="{ active: searchState.mode.value === 'name' }" type="button" @click="searchState.mode.value = 'name'">이름으로 검색</button>
        <button :class="{ active: searchState.mode.value === 'appearance' }" type="button" @click="searchState.mode.value = 'appearance'">모양·색깔로 검색</button>
      </div>
      <form @submit.prevent="searchState.search">
        <div v-if="searchState.mode.value === 'name'" class="search-row">
          <input v-model="searchState.keyword.value" class="input" type="search" placeholder="예: 타이레놀, 아세트아미노펜" aria-label="약 이름" />
          <button class="button" type="submit">검색하기</button>
        </div>
        <div v-else class="appearance-grid">
          <select v-model="searchState.shape.value" class="select" aria-label="약 모양"><option value="">모양 선택</option><option>원형</option><option>타원형</option><option>장방형</option><option>캡슐형</option></select>
          <select v-model="searchState.color.value" class="select" aria-label="약 색깔"><option value="">색깔 선택</option><option>흰색</option><option>노랑</option><option>분홍</option><option>파랑</option></select>
          <button class="button" type="submit">검색하기</button>
        </div>
      </form>
      <p v-if="searchState.error.value" class="error-message">{{ searchState.error.value }}</p>
    </div>
  </section>

  <section v-if="searchState.searched.value" class="result-section">
    <div class="section-header"><div><h2>검색 결과</h2><p class="page-intro">검색된 의약품을 선택하면 상세 정보를 확인할 수 있습니다.</p></div><span class="result-count">{{ searchState.items.value.length }}건</span></div>
    <div v-if="searchState.loading.value" class="loading">검색 중입니다…</div>
    <div v-else-if="!searchState.items.value.length" class="notice">검색 결과가 없습니다. 다른 검색어 또는 조건으로 다시 시도해 주세요.</div>
    <div v-else class="drug-grid"><DrugCard v-for="drug in searchState.items.value" :key="drug.id" :drug="drug" @select="openDetail" /></div>
  </section>
</template>

<style scoped>
.search-hero { display: grid; grid-template-columns: 1fr 1.02fr; align-items: center; gap: 56px; padding: 46px 4vw 56px; margin: -44px calc((1120px - 100vw) / 2) 42px; background: linear-gradient(135deg, #f2fbff, #fff); }.hero-copy { max-width: 480px; justify-self: end; }.eyebrow { display: inline-block; margin-bottom: 14px; color: var(--primary-dark); font-size: 13px; font-weight: 700; }.hero-copy h1 { margin-bottom: 16px; }.hero-copy h1 em { color: var(--primary-dark); font-style: normal; }.hero-copy p { max-width: 420px; color: var(--muted); line-height: 1.8; }.search-panel { width: min(100%, 520px); padding: 10px; }.mode-tabs { display: flex; gap: 6px; padding: 5px; margin-bottom: 8px; border-radius: 14px; background: #f1f8fb; }.mode-tabs button { flex: 1; min-height: 39px; border: 0; border-radius: 10px; color: var(--muted); background: transparent; font-size: 14px; font-weight: 700; }.mode-tabs button.active { color: var(--ink); background: #fff; box-shadow: 0 3px 8px rgba(77,139,163,.1); }.search-row { display: grid; grid-template-columns: 1fr auto; gap: 8px; }.appearance-grid { display: grid; grid-template-columns: 1fr 1fr auto; gap: 8px; }.result-section { margin-top: 22px; }.result-count { color: var(--primary-dark); font-size: 14px; font-weight: 700; }.drug-grid { display: grid; grid-template-columns: repeat(2, minmax(0, 1fr)); gap: 14px; }@media (max-width: 820px) { .search-hero { grid-template-columns: 1fr; gap: 26px; padding: 38px 20px; margin: -30px -20px 32px; }.hero-copy { justify-self: start; }.search-panel { width: 100%; } }@media (max-width: 520px) { .appearance-grid, .search-row, .drug-grid { grid-template-columns: 1fr; } }
</style>
