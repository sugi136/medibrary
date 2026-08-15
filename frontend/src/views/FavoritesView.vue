<script setup>
import { onMounted, ref } from 'vue'
import { useRouter } from 'vue-router'
import ConfirmDialog from '../components/common/ConfirmDialog.vue'
import DrugCard from '../components/drug/DrugCard.vue'
import { useFavorites } from '../composables/useFavorites'

const router = useRouter()
const favoritesState = useFavorites()
const warningsExpanded = ref(false)
const pendingRemoval = ref(null)

function openDetail(drug) {
  router.push({ name: 'drug-detail', params: { id: drug.id } })
}

function requestRemoval(drug) {
  pendingRemoval.value = drug
}

async function confirmRemoval() {
  if (!pendingRemoval.value) return
  const removed = await favoritesState.remove(pendingRemoval.value.favoriteId)
  if (removed) pendingRemoval.value = null
}

onMounted(favoritesState.load)
</script>

<template>
  <section>
    <div class="section-header"><div><span class="eyebrow">SCR-MY-001</span><h1>나의 즐겨찾기</h1><p class="page-intro">자주 찾는 약을 모아보고, 함께 복용할 때 주의할 조합을 확인하세요.</p></div><RouterLink class="button button-secondary" to="/search">약 검색하기</RouterLink></div>
    <section v-if="favoritesState.warnings.value.length" class="warning-banner"><div><strong>병용 주의 조합이 {{ favoritesState.warnings.value.length }}건 있습니다.</strong><p>복용 전 의사 또는 약사와 상담해 주세요.</p></div><button class="text-button" type="button" @click="warningsExpanded = !warningsExpanded">{{ warningsExpanded ? '접기' : '상세 보기' }}</button><div v-if="warningsExpanded" class="warning-details"><p v-for="warning in favoritesState.warnings.value" :key="`${warning.drugIdA}-${warning.drugIdB}`"><span class="badge" :class="warning.severity === 'CONTRAINDICATED' ? 'badge-danger' : 'badge-caution'">{{ warning.severity === 'CONTRAINDICATED' ? '금기' : '주의' }}</span> {{ warning.reason }}</p></div></section>
    <div v-if="favoritesState.loading.value" class="loading">즐겨찾기를 불러오는 중입니다…</div>
    <div v-else-if="favoritesState.error.value" class="notice">{{ favoritesState.error.value }}</div>
    <div v-else-if="!favoritesState.favorites.value.length" class="empty-state card"><div>♡</div><h2>즐겨찾기한 약이 없습니다</h2><p>약 상세 화면에서 하트를 눌러 관심 있는 약을 저장해 보세요.</p><RouterLink class="button" to="/search">약 검색하기</RouterLink></div>
    <div v-else class="favorites-list"><article v-for="drug in favoritesState.favorites.value" :key="drug.favoriteId" class="favorite-item"><DrugCard :drug="drug" @select="openDetail" /><button class="remove-button" :disabled="favoritesState.deletingFavoriteId.value === drug.favoriteId" type="button" @click="requestRemoval(drug)">해제</button></article></div>
  </section>
  <ConfirmDialog :open="Boolean(pendingRemoval)" title="즐겨찾기를 해제할까요?" :message="pendingRemoval ? `'${pendingRemoval.name}'을(를) 즐겨찾기에서 제거합니다.` : ''" confirm-label="해제" :loading="favoritesState.deletingFavoriteId.value !== null" @confirm="confirmRemoval" @cancel="pendingRemoval = null" />
</template>

<style scoped>
.eyebrow { color: var(--primary-dark); font-size: 12px; font-weight: 700; }.section-header { margin-bottom: 26px; }.section-header h1 { margin: 7px 0; }.warning-banner { display: grid; grid-template-columns: 1fr auto; gap: 12px; align-items: start; padding: 19px 22px; margin-bottom: 18px; border: 1px solid #f7d7aa; border-radius: 16px; background: var(--caution-soft); }.warning-banner strong { color: #9b5c0a; }.warning-banner p { margin: 4px 0 0; color: #976d37; font-size: 13px; }.warning-details { grid-column: 1 / -1; border-top: 1px solid #f1d7b6; padding-top: 12px; }.warning-details p { display: flex; align-items: center; gap: 8px; }.empty-state { display: grid; justify-items: center; gap: 13px; padding: 58px 24px; text-align: center; }.empty-state > div { display: grid; place-items: center; width: 65px; height: 65px; border-radius: 22px; color: var(--primary-dark); background: var(--primary-soft); font-size: 37px; }.empty-state h2 { margin: 0; }.empty-state p { color: var(--muted); margin-bottom: 8px; }.favorites-list { display: grid; gap: 12px; }.favorite-item { position: relative; }.favorite-item :deep(.drug-card) { padding-right: 80px; }.remove-button { position: absolute; top: 50%; right: 15px; min-height: 36px; padding: 0 13px; border: 1px solid #f0c3ca; border-radius: 10px; color: var(--danger); background: #fff; font-size: 13px; transform: translateY(-50%); }@media (max-width: 520px) { .section-header { align-items: flex-start; flex-direction: column; }.warning-banner { grid-template-columns: 1fr; }.favorite-item :deep(.drug-card) { padding-right: 66px; }.remove-button { right: 10px; padding: 0 9px; } }
</style>
