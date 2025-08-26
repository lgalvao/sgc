<template>
  <div>
    <div v-if="title" class="d-flex justify-content-between align-items-center mb-3">
      <h4 class="mb-0">{{ title }}</h4>

      <div>
        <button class="btn btn-outline-primary btn-sm me-2" @click="expandAll">
          <i class="bi bi-arrows-expand"></i>
        </button>
        <button class="btn btn-outline-secondary btn-sm" @click="collapseAll">
          <i class="bi bi-arrows-collapse"></i>
        </button>
      </div>
    </div>

    <div class="table-responsive w-100">
      <table class="table table-striped table-hover m-0">
        <colgroup>
          <col v-for="(column) in columns" :key="column.key"
               :style="{ width: column.width || (100 / columns.length) + '%' }">
        </colgroup>

        <thead v-if="!hideHeaders">
        <tr>
          <th v-for="column in columns" :key="column.key">{{ column.label }}</th>
        </tr>
        </thead>
        <tbody>
        <template v-for="item in internalData" :key="item.id">
          <TreeRow
              :columns="columns"
              :item="item"
              :level="0"
              @toggle="toggleExpand"
              @row-click="handleTreeRowClick"
          />
        </template>
        </tbody>
      </table>
    </div>
  </div>
</template>

<script lang="ts" setup>
import {ref, watch} from 'vue'
import TreeRow from './TreeRow.vue'

interface TreeItem {
  id: number | string;
  expanded?: boolean;
  children?: TreeItem[];

  [key: string]: any; // Para permitir outras propriedades nos itens da árvore
}

interface Column {
  key: string;
  label: string;
  width?: string;
}

interface TreeTableProps {
  data: TreeItem[];
  columns: Column[];
  title?: string;
  hideHeaders?: boolean;
}

const props = defineProps<TreeTableProps>()
const emit = defineEmits<{ (e: 'row-click', item: TreeItem): void; }>()

const internalData = ref<TreeItem[]>([])

// Initialize internal data with expanded property
const initializeData = (data: TreeItem[]): TreeItem[] => {
  return data.map(item => ({
    ...item,
    expanded: item.expanded || false,
    children: item.children ? initializeData(item.children) : []
  }))
}

// Watch for prop changes
watch(() => props.data,
    (newData) => internalData.value = initializeData(newData),
    {immediate: true, deep: true})

// Find item by ID recursively
const findItemById = (items: TreeItem[], id: number | string): TreeItem | null => {
  for (let item of items) {
    if (item.id === id) return item
    if (item.children && item.children.length > 0) {
      const found = findItemById(item.children, id)
      if (found) return found
    }
  }
  return null
}

// Toggle expand/collapse
const toggleExpand = (id: number | string) => {
  const item = findItemById(internalData.value, id)
  if (item) item.expanded = !item.expanded
}

// Expand all items recursively
const expandAll = () => {
  const expand = (items: TreeItem[]) => {
    items.forEach(item => {
      if (item.children && item.children.length > 0) {
        item.expanded = true
        expand(item.children)
      }
    })
  }
  expand(internalData.value)
}

// Collapse all items recursively
const collapseAll = () => {
  const collapse = (items: TreeItem[]) => {
    items.forEach(item => {
      item.expanded = false
      if (item.children && item.children.length > 0) {
        collapse(item.children)
      }
    })
  }
  collapse(internalData.value)
}

const handleTreeRowClick = (clickedItem: TreeItem) => {
  emit('row-click', clickedItem)
}

</script>
