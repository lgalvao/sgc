<template>
  <div>
    <div
      v-if="title"
      class="d-flex justify-content-between align-items-center mb-3"
    >
      <h4 class="mb-0">
        {{ title }}
      </h4>

      <div>
        <BButton
          variant="outline-primary"
          size="sm"
          class="me-2"
          data-testid="btn-expandir-todas"
          @click="expandAll"
        >
          <i class="bi bi-arrows-expand" />
        </BButton>
        <BButton
          variant="outline-secondary"
          size="sm"
          @click="collapseAll"
        >
          <i class="bi bi-arrows-collapse" />
        </BButton>
      </div>
    </div>

    <div class="table-responsive w-100">
      <table class="table table-striped table-hover m-0">
        <colgroup>
          <col
            v-for="(column) in columns"
            :key="column.key"
            :style="{ width: column.width || (100 / columns.length) + '%' }"
          >
        </colgroup>

        <thead v-if="!hideHeaders">
          <tr>
            <th
              v-for="column in columns"
              :key="column.key"
              role="columnheader"
            >
              {{ column.label }}
            </th>
          </tr>
        </thead>
        <tbody>
          <TreeRowItem
            v-for="item in flattenedData"
            :key="item.id"
            :columns="columns"
            :item="item"
            :level="item.level"
            @toggle="toggleExpand"
            @row-click="handleTreeRowClick"
          />
        </tbody>
      </table>
    </div>
  </div>
</template>

<script lang="ts" setup>
import {computed, nextTick, ref, watch} from 'vue'
import TreeRowItem from './TreeRowItem.vue'
import {BButton} from 'bootstrap-vue-next'

interface TreeItem {
  id: number | string;
  expanded?: boolean;
  children?: TreeItem[];
  level?: number;

  [key: string]: any;  
}

interface FlattenedTreeItem extends TreeItem {
  level: number;
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
const emit = defineEmits<{
  (e: 'row-click', item: TreeItem): void
}>()

const internalData = ref<TreeItem[]>([])

const initializeExpanded = (items: TreeItem[]): TreeItem[] => {
  return items.map(item => ({
    ...item,
    expanded: item.expanded !== undefined ? item.expanded : false,
    children: item.children ? initializeExpanded(item.children) : [],
  }));
};

watch(() => props.data,
    (newData) => {
      internalData.value = initializeExpanded(JSON.parse(JSON.stringify(newData)))
    },
    { immediate: true, deep: true }
)

const flattenedData = computed((): FlattenedTreeItem[] => {
  const flattened: FlattenedTreeItem[] = []
  const flatten = (items: TreeItem[], level: number) => {
    for (const item of items) {
      flattened.push({ ...item, level })
      if (item.expanded && item.children) {
        flatten(item.children, level + 1)
      }
    }
  }
  flatten(internalData.value, 0)
  return flattened
})

const findItemById = (items: TreeItem[], id: number | string): TreeItem | null => {
  for (const item of items) {
    if (item.id === id) return item
    if (item.children) {
      const found = findItemById(item.children, id)
      if (found) return found
    }
  }
  return null
}

const toggleExpand = (id: number | string) => {
  const item = findItemById(internalData.value, id)
  if (item) {
    item.expanded = !item.expanded
  }
}

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
  nextTick(() => {
    internalData.value = [...internalData.value]
  })
}

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
  nextTick(() => {
    internalData.value = [...internalData.value]
  })
}

const handleTreeRowClick = (clickedItem: TreeItem) => {
  emit('row-click', clickedItem)
}

defineExpose({
  internalData,
  findItemById,
  toggleExpand,
  handleTreeRowClick,
})
</script>
