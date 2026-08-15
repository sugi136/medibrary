<script setup>
defineProps({
  drug: { type: Object, required: true },
  interactive: { type: Boolean, default: true },
})

defineEmits(['select'])
</script>

<template>
  <button v-if="interactive" class="drug-card card" type="button" @click="$emit('select', drug)">
    <span class="pill-placeholder" aria-hidden="true">💊</span>
    <span class="drug-card-body">
      <strong>{{ drug.name }}</strong>
      <small>{{ drug.manufacturer || '제조사 정보 없음' }}</small>
      <small>{{ [drug.shape, drug.color].filter(Boolean).join(' · ') || '외형 정보 없음' }}</small>
    </span>
    <span class="chevron" aria-hidden="true">›</span>
  </button>
  <article v-else class="drug-card card">
    <span class="pill-placeholder" aria-hidden="true">💊</span>
    <span class="drug-card-body">
      <strong>{{ drug.name }}</strong>
      <small>{{ drug.manufacturer || '제조사 정보 없음' }}</small>
      <small>{{ [drug.shape, drug.color].filter(Boolean).join(' · ') || '외형 정보 없음' }}</small>
    </span>
  </article>
</template>

<style scoped>
.drug-card { display: flex; align-items: center; gap: 14px; width: 100%; padding: 16px; border: 1px solid var(--line); text-align: left; transition: .18s ease; }
button.drug-card { cursor: pointer; background: var(--surface); }
button.drug-card:hover { border-color: var(--primary); transform: translateY(-2px); }
.pill-placeholder { display: grid; place-items: center; flex: none; width: 52px; height: 52px; border-radius: 15px; background: var(--primary-soft); font-size: 23px; }
.drug-card-body { display: grid; gap: 4px; min-width: 0; }.drug-card-body strong { overflow: hidden; text-overflow: ellipsis; white-space: nowrap; }.drug-card-body small { color: var(--muted); font-size: 13px; }.chevron { margin-left: auto; color: var(--primary-dark); font-size: 27px; }
</style>
