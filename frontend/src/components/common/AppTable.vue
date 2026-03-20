<template>
  <div class="app-table-wrap">
    <div v-if="loading" class="app-table-loading">
      <AppLoading />
    </div>
    <div v-else-if="!data.length" class="app-table-empty">
      <AppEmpty message="데이터가 없습니다." />
    </div>
    <table v-else class="app-table">
      <thead>
        <tr>
          <th
            v-for="col in columns"
            :key="col.key"
            :class="{ sortable: col.sortable }"
            @click="col.sortable ? toggleSort(col.key) : null"
          >
            {{ col.label }}
            <span v-if="col.sortable" class="sort-icon">
              {{ sortKey === col.key ? (sortOrder === 'asc' ? '▲' : '▼') : '⇅' }}
            </span>
          </th>
        </tr>
      </thead>
      <tbody>
        <tr v-for="(row, idx) in sortedData" :key="idx">
          <td v-for="col in columns" :key="col.key">
            <slot :name="`cell-${col.key}`" :row="row" :value="row[col.key]">
              {{ row[col.key] }}
            </slot>
          </td>
        </tr>
      </tbody>
    </table>
  </div>
</template>

<script setup>
import { ref, computed } from 'vue'
import AppLoading from './AppLoading.vue'
import AppEmpty from './AppEmpty.vue'

const props = defineProps({
  columns: { type: Array, default: () => [] },
  data: { type: Array, default: () => [] },
  loading: Boolean
})

const sortKey = ref('')
const sortOrder = ref('asc')

function toggleSort(key) {
  if (sortKey.value === key) {
    sortOrder.value = sortOrder.value === 'asc' ? 'desc' : 'asc'
  } else {
    sortKey.value = key
    sortOrder.value = 'asc'
  }
}

const sortedData = computed(() => {
  if (!sortKey.value) return props.data
  return [...props.data].sort((a, b) => {
    const va = a[sortKey.value]
    const vb = b[sortKey.value]
    if (va < vb) return sortOrder.value === 'asc' ? -1 : 1
    if (va > vb) return sortOrder.value === 'asc' ? 1 : -1
    return 0
  })
})
</script>

<style scoped lang="scss">
.app-table-wrap {
  width: 100%;
  overflow-x: auto;
  -webkit-overflow-scrolling: touch;
  border-radius: $radius-lg;
  border: 1px solid $border;
}

.app-table {
  width: 100%;
  min-width: 540px;
  border-collapse: collapse;

  thead {
    background: $bg-light;

    th {
      padding: $spacing-3 $spacing-4;
      text-align: left;
      font-size: $font-size-sm;
      font-weight: 600;
      color: $text-secondary;
      white-space: nowrap;

      &.sortable {
        cursor: pointer;
        user-select: none;

        &:hover {
          color: $primary;
        }
      }

      .sort-icon {
        margin-left: $spacing-1;
        font-size: 10px;
      }
    }
  }

  tbody {
    tr {
      border-top: 1px solid $border;
      transition: background $transition-fast;

      &:hover {
        background: $primary-bg;
      }

      td {
        padding: $spacing-4;
        font-size: $font-size-sm;
        color: $text-primary;
        vertical-align: middle;
        white-space: nowrap;
      }
    }
  }
}

.app-table-loading,
.app-table-empty {
  padding: $spacing-12;
  display: flex;
  justify-content: center;
}
</style>
