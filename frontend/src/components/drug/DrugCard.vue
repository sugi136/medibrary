<script setup>
import { ref } from 'vue'

const props = defineProps({
  drug: { type: Object, required: true },
  interactive: { type: Boolean, default: true },
})

const emit = defineEmits(['select'])
const failedImageKeys = ref(new Set())

function imageKey(drug) {
  return drug.id || drug.imageUrl
}

function canShowImage(drug) {
  return Boolean(drug.imageUrl) && !failedImageKeys.value.has(imageKey(drug))
}

function markImageAsFailed(drug) {
  failedImageKeys.value = new Set([...failedImageKeys.value, imageKey(drug)])
}

function selectDrug() {
  if (props.interactive) emit('select', props.drug)
}
</script>

<template>
  <component
    :is="interactive ? 'button' : 'article'"
    class="drug-card card"
    :type="interactive ? 'button' : undefined"
    @click="selectDrug"
  >
    <span class="pill-thumbnail">
      <img
        v-if="canShowImage(drug)"
        :src="drug.imageUrl"
        :alt="`${drug.name} 낱알 이미지`"
        loading="lazy"
        @error="markImageAsFailed(drug)"
      >
      <span v-else class="pill-placeholder" aria-hidden="true">💊</span>
    </span>
    <span class="drug-card-body">
      <strong>{{ drug.name }}</strong>
      <small>{{ drug.manufacturer || '제조사 정보 없음' }}</small>
      <small>{{ [drug.shape, drug.color].filter(Boolean).join(' · ') || '외형 정보 없음' }}</small>
    </span>
    <span v-if="interactive" class="chevron" aria-hidden="true">›</span>
  </component>
</template>

<style scoped>
.drug-card { display: flex; align-items: center; gap: 14px; width: 100%; padding: 16px; border: 1px solid var(--line); text-align: left; transition: .18s ease; }
button.drug-card { cursor: pointer; background: var(--surface); }
button.drug-card:hover { border-color: var(--primary); transform: translateY(-2px); }
.pill-thumbnail { display: grid; place-items: center; flex: none; width: 52px; height: 52px; overflow: hidden; border-radius: 15px; background: var(--primary-soft); }
.pill-thumbnail img { display: block; width: 100%; height: 100%; object-fit: cover; }
.pill-placeholder { display: grid; place-items: center; width: 100%; height: 100%; font-size: 23px; }
.drug-card-body { display: grid; gap: 4px; min-width: 0; }.drug-card-body strong { overflow: hidden; text-overflow: ellipsis; white-space: nowrap; }.drug-card-body small { color: var(--muted); font-size: 13px; }.chevron { margin-left: auto; color: var(--primary-dark); font-size: 27px; }
</style>
