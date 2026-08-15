<script setup>
import { computed, onMounted, ref } from 'vue'
import { dashboardApi } from '../api'

const loading = ref(true)
const error = ref('')
const summary = ref({ recentSearches: [], favoriteCount: 0, hasDurWarning: false })
const recent = computed(() => summary.value.recentSearches || [])

async function load() {
  loading.value = true
  try { summary.value = (await dashboardApi.summary()).data }
  catch (e) { error.value = '대시보드 정보를 불러오지 못했습니다.' }
  finally { loading.value = false }
}

onMounted(load)
</script>

<template>
  <section>
    <span class="eyebrow">SCR-DASH-001</span><h1>안녕하세요,<br />오늘도 건강한 하루예요.</h1><p class="page-intro">최근 검색과 즐겨찾기한 약의 상태를 빠르게 확인하세요.</p>
    <div v-if="loading" class="loading">대시보드를 준비하는 중입니다…</div>
    <div v-else-if="error" class="notice">{{ error }}</div>
    <div v-else class="dashboard-grid">
      <article class="favorite-summary card"><span class="card-icon">♡</span><div><p>즐겨찾기한 약</p><strong>{{ summary.favoriteCount }}<small>개</small></strong></div><RouterLink to="/favorites">관리하기 ›</RouterLink></article>
      <article class="dur-summary card" :class="{ alert: summary.hasDurWarning }"><span class="card-icon">!</span><div><p>병용금기 확인</p><strong>{{ summary.hasDurWarning ? '주의 필요' : '확인 완료' }}</strong><small>{{ summary.hasDurWarning ? '주의 조합이 있습니다.' : '확인된 경고가 없습니다.' }}</small></div><RouterLink to="/favorites">자세히 ›</RouterLink></article>
      <article class="recent-card card"><div class="section-header"><div><h2>최근 검색어</h2><p class="page-intro">최근 5건까지 표시됩니다.</p></div><RouterLink to="/search">새로 검색 ›</RouterLink></div><div v-if="recent.length" class="recent-list"><RouterLink v-for="item in recent" :key="item" :to="{ path: '/search', query: { q: item } }">{{ item }} <span>›</span></RouterLink></div><div v-else class="notice">최근 검색어가 없습니다. 필요한 약을 먼저 검색해 보세요.</div></article>
    </div>
  </section>
</template>

<style scoped>
.eyebrow { color: var(--primary-dark); font-size: 12px; font-weight: 700; } h1 { margin: 7px 0 10px; }.dashboard-grid { display: grid; grid-template-columns: 1fr 1fr; gap: 16px; margin-top: 30px; }.favorite-summary, .dur-summary { display: grid; grid-template-columns: auto 1fr; align-items: center; gap: 15px; padding: 24px; }.card-icon { display: grid; place-items: center; width: 48px; height: 48px; border-radius: 15px; color: var(--primary-dark); background: var(--primary-soft); font-size: 25px; font-weight: 700; }.dur-summary .card-icon { color: #2f8b63; background: #eaf8f1; }.dur-summary.alert .card-icon { color: #a86409; background: var(--caution-soft); }.dashboard-grid p { margin: 0 0 3px; color: var(--muted); font-size: 13px; }.dashboard-grid strong { display: block; font-size: 22px; }.dashboard-grid strong small { margin-left: 3px; color: var(--muted); font-size: 14px; }.dur-summary small { color: var(--muted); font-size: 12px; }.dashboard-grid a { grid-column: 1 / -1; color: var(--primary-dark); font-size: 13px; font-weight: 700; }.recent-card { grid-column: 1 / -1; padding: 24px; }.recent-card .section-header { align-items: center; margin-bottom: 15px; }.recent-card .section-header a { grid-column: auto; }.recent-list { display: grid; grid-template-columns: repeat(5, 1fr); gap: 10px; }.recent-list a { display: flex; justify-content: space-between; gap: 5px; grid-column: auto; padding: 12px; border: 1px solid var(--line); border-radius: 11px; color: var(--ink); font-weight: 500; }.recent-list a span { color: var(--primary-dark); }@media (max-width: 720px) { .dashboard-grid { grid-template-columns: 1fr; }.recent-list { grid-template-columns: 1fr; }.recent-card { grid-column: auto; } }
</style>
