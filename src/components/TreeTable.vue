<template>
  <div>
    <div class="d-flex justify-content-between align-items-center mb-3" v-if="title">
      <h4 class="mb-0">{{ title }}</h4>

      <div>
        <button @click="expandAll" class="btn btn-outline-primary btn-sm me-2">
          <i class="bi bi-arrows-expand"></i>
        </button>
        <button @click="collapseAll" class="btn btn-outline-secondary btn-sm">
          <i class="bi bi-arrows-collapse"></i>
        </button>
      </div>
    </div>

    <div class="table-responsive">
      <table class="table table-striped table-hover m-0">
        <colgroup>
          <col style="width: 75%;">
          <col style="width: 25%;">
        </colgroup>

        <tbody>
        <template v-for="item in internalData" :key="item.id">
          <TreeRow
              :item="item"
              :level="0"
              :columns="columns"
              @toggle="toggleExpand"
              @row-click="$emit('row-click', item)"
          />
        </template>
        </tbody>
      </table>
    </div>
  </div>
</template>

<script>
import {ref, watch} from 'vue'
import TreeRow from './TreeRow.vue'

export default {
  name: 'TreeTable',
  components: {
    TreeRow
  },
  props: {
    data: {
      type: Array,
      required: true
    },
    columns: {
      type: Array
    },
    title: {
      type: String,
      default: ''
    }
  },
  emits: ['row-click'],
  setup(props, { emit }) {
    const internalData = ref([])

    // Initialize internal data with expanded property
    const initializeData = (data) => {
      return data.map(item => ({
        ...item,
        expanded: item.expanded || false,
        children: item.children ? initializeData(item.children) : []
      }))
    }

    // Watch for prop changes
    watch(() => props.data, (newData) => {
      internalData.value = initializeData(newData)
    }, {immediate: true, deep: true})

    // Find item by ID recursively
    const findItemById = (items, id) => {
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
    const toggleExpand = (id) => {
      const item = findItemById(internalData.value, id)
      if (item) item.expanded = !item.expanded
    }

    // Expand all items recursively
    const expandAll = () => {
      const expand = (items) => {
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
      const collapse = (items) => {
        items.forEach(item => {
          item.expanded = false
          if (item.children && item.children.length > 0) {
            collapse(item.children)
          }
        })
      }
      collapse(internalData.value)
    }

    const handleRowClick = (item) => {
      emit('row-click', item)
    }

    return {
      internalData,
      toggleExpand,
      expandAll,
      collapseAll,
      handleRowClick
    }
  }
}
</script>
