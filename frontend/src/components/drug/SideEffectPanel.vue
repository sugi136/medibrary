<script setup>
defineProps({
  loading: { type: Boolean, default: false },
  sideEffects: { type: Object, default: null },
})

function formatCount(count) {
  return typeof count === 'number' ? `${count.toLocaleString('ko-KR')}건` : ''
}
</script>

<template>
  <section class="tab-panel card">
    <p class="eyebrow">국내 허가사항 · 해외 FAERS 비교</p>
    <h2>부작용 비교</h2>
    <p class="intro">국내 허가사항의 이상반응과 해외 자발보고 상위 항목을 함께 확인합니다.</p>

    <div v-if="loading" class="loading">국내·해외 부작용 정보를 조회하는 중입니다…</div>
    <div v-else class="comparison-grid">
      <article class="source-card domestic-card">
        <div class="source-heading">
          <div>
            <p class="source-kicker">KOREA</p>
            <h3>국내 허가사항</h3>
          </div>
          <span class="source-badge">e약은요</span>
        </div>
        <template v-if="sideEffects?.domestic?.available">
          <ul v-if="sideEffects.domestic.cases?.length" class="domestic-list">
            <li v-for="item in sideEffects.domestic.cases" :key="item.term">{{ item.term }}</li>
          </ul>
          <p v-else class="muted">국내 허가사항의 부작용 정보가 없습니다.</p>
        </template>
        <p v-else class="muted">{{ sideEffects?.domestic?.message || '현재 정보를 불러올 수 없습니다.' }}</p>
      </article>

      <article class="source-card overseas-card">
        <div class="source-heading">
          <div>
            <p class="source-kicker">UNITED STATES</p>
            <h3>해외 FAERS 상위 보고</h3>
          </div>
          <span class="source-badge">openFDA</span>
        </div>
        <template v-if="sideEffects?.overseas?.available">
          <ul v-if="sideEffects.overseas.cases?.length" class="faers-list">
            <li
              v-for="item in sideEffects.overseas.cases"
              :key="item.term"
              :class="{ highlight: !item.domesticMentioned }"
            >
              <div class="term-row">
                <strong>{{ item.term }}</strong>
                <span class="count">{{ formatCount(item.count) }}</span>
              </div>
              <span class="match-badge" :class="{ unmatched: !item.domesticMentioned }">
                {{ item.domesticMentioned ? '국내 문서 언급' : '국내 문서 미확인' }}
              </span>
            </li>
          </ul>
          <p v-else class="muted">등록된 부작용 사례가 없습니다.</p>
        </template>
        <p v-else class="muted">{{ sideEffects?.overseas?.message || '현재 정보를 불러올 수 없습니다.' }}</p>
      </article>
    </div>

    <aside v-if="sideEffects?.disclaimer" class="faers-notice" aria-label="FAERS 데이터 해석 유의사항">
      <strong>FAERS 해석 유의사항</strong>
      <p>{{ sideEffects.disclaimer }}</p>
    </aside>
  </section>
</template>

<style scoped>
.tab-panel { padding: 28px; }
.eyebrow { margin: 0 0 4px; color: var(--primary-dark); font-size: 12px; font-weight: 800; letter-spacing: .04em; }
.tab-panel > h2 { margin: 0; }
.intro { margin: 8px 0 0; color: var(--muted); font-size: 14px; }
.comparison-grid { display: grid; grid-template-columns: 1fr 1fr; gap: 16px; margin-top: 20px; }
.source-card { min-width: 0; padding: 20px; border: 1px solid var(--line); border-radius: 16px; background: #fff; }
.domestic-card { border-top: 4px solid #94a3b8; }
.overseas-card { border-top: 4px solid var(--primary); }
.source-heading { display: flex; align-items: flex-start; justify-content: space-between; gap: 12px; margin-bottom: 16px; }
.source-heading h3 { margin: 2px 0 0; font-size: 17px; }
.source-kicker { margin: 0; color: var(--muted); font-size: 10px; font-weight: 800; letter-spacing: .08em; }
.source-badge { padding: 5px 8px; border-radius: 8px; color: var(--primary-dark); background: var(--primary-soft); font-size: 11px; font-weight: 800; white-space: nowrap; }
.domestic-list { max-height: 330px; margin: 0; padding-left: 19px; overflow-y: auto; color: var(--muted); line-height: 1.75; }
.faers-list { display: grid; gap: 8px; margin: 0; padding: 0; list-style: none; }
.faers-list li { padding: 10px 11px; border: 1px solid var(--line); border-radius: 10px; background: #fff; }
.faers-list li.highlight { border-color: #f0c77a; background: #fffaf0; }
.term-row { display: flex; align-items: baseline; justify-content: space-between; gap: 8px; }
.term-row strong { overflow: hidden; font-size: 13px; text-overflow: ellipsis; white-space: nowrap; }
.count { flex: none; color: var(--primary-dark); font-size: 12px; font-weight: 800; }
.match-badge { display: inline-block; margin-top: 5px; padding: 3px 6px; border-radius: 6px; color: #52606d; background: #eef2f5; font-size: 10px; font-weight: 700; }
.match-badge.unmatched { color: #8a5a00; background: #fff0c9; }
.faers-notice { margin-top: 16px; padding: 14px 16px; border: 1px solid #f2d99f; border-radius: 12px; color: #6a5226; background: #fffbeb; font-size: 13px; line-height: 1.6; }
.faers-notice p { margin: 4px 0 0; }
.muted { color: var(--muted); line-height: 1.7; }
@media (max-width: 700px) { .tab-panel { padding: 20px; }.comparison-grid { grid-template-columns: 1fr; }.domestic-list { max-height: none; } }
</style>
