<template>
  <div v-if="total > 0" class="supply-pager">
    <span>共 {{ total }} 条 · 第 {{ page }} / {{ totalPages }} 页</span>
    <div>
      <button :disabled="page <= 1" @click="$emit('change', page - 1)">‹</button>
      <b>{{ page }}</b>
      <span>/ {{ totalPages }}</span>
      <button :disabled="page >= totalPages" @click="$emit('change', page + 1)">›</button>
    </div>
  </div>
</template>

<script>
export default {
  name: 'SupplyPager',
  props: {
    total: { type: Number, default: 0 },
    page: { type: Number, default: 1 },
    size: { type: Number, default: 10 },
    totalPages: { type: Number, default: 0 }
  }
}
</script>
