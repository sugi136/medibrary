<script setup>
import { computed, ref } from 'vue'

const props = defineProps({
  drug: { type: Object, required: true },
  favoriteLoading: { type: Boolean, default: false },
})

defineEmits(['favorite'])

const imageFailed = ref(false)
const canShowImage = computed(() => Boolean(props.drug.imageUrl) && !imageFailed.value)
</script>

<template>
  <article class="summary-card card">
    <div class="detail-pill">
      <img
        v-if="canShowImage"
        :src="drug.imageUrl"
        :alt="`${drug.name} 낱알 이미지`"
        @error="imageFailed = true"
      />
      <span v-else aria-hidden="true">💊</span>
    </div>
    <div class="summary-text">
      <span class="summary-label">의약품 상세 정보</span>
      <h1>{{ drug.name }}</h1>
      <p>
        {{ drug.manufacturer || '제조사 정보 없음' }}
        <span v-if="drug.ingredientKr || drug.ingredientEn"
          >· {{ drug.ingredientKr || drug.ingredientEn }}</span
        >
      </p>
    </div>
    <button
      class="favorite-button"
      :class="{ active: drug.isFavorite }"
      :disabled="favoriteLoading"
      type="button"
      :aria-label="drug.isFavorite ? '즐겨찾기 해제' : '즐겨찾기 등록'"
      @click="$emit('favorite')"
    >
      {{ drug.isFavorite ? '♥' : '♡' }}
    </button>
  </article>
</template>

<style scoped>
.summary-card {
  display: flex;
  align-items: center;
  gap: 20px;
  padding: 28px;
}
.detail-pill {
  display: grid;
  place-items: center;
  flex: none;
  width: 92px;
  height: 92px;
  overflow: hidden;
  border-radius: 25px;
  background: var(--primary-soft);
  font-size: 38px;
}
.detail-pill img {
  display: block;
  width: 100%;
  height: 100%;
  object-fit: cover;
}
.summary-text h1 {
  margin: 4px 0 8px;
  font-size: 28px;
}
.summary-label {
  color: var(--primary-dark);
  font-size: 13px;
  font-weight: 700;
}
.summary-text p {
  color: var(--muted);
  font-size: 14px;
}
.favorite-button {
  width: 48px;
  height: 48px;
  margin-left: auto;
  border: 1px solid var(--line);
  border-radius: 15px;
  color: var(--muted);
  background: #fff;
  font-size: 29px;
  line-height: 1;
}
.favorite-button.active {
  border-color: #f4c5cc;
  color: var(--danger);
  background: var(--danger-soft);
}
@media (max-width: 600px) {
  .summary-card {
    align-items: flex-start;
    gap: 14px;
    padding: 20px;
  }
  .detail-pill {
    width: 64px;
    height: 64px;
    border-radius: 18px;
    font-size: 27px;
  }
  .summary-text h1 {
    font-size: 21px;
  }
  .favorite-button {
    width: 40px;
    height: 40px;
    font-size: 25px;
  }
}
</style>
