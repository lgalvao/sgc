<template>
  <tr @click="handleRowClick" class="tree-row">
    <td :style="{ paddingLeft: (level * 20) + 'px' }">
      <span v-if="item.children && item.children.length > 0" @click.stop="toggleExpand(item.id)" class="toggle-icon">
        <i :class="['bi', item.expanded ? 'bi-chevron-down' : 'bi-chevron-right']"></i>
      </span>
      {{ item.nome }}
    </td>
    <td>{{ item.situacao }}</td>
  </tr>
  <template v-if="item.expanded && item.children">
    <TreeRow
        v-for="child in item.children"
        :key="child.id"
        :item="child"
        :level="level + 1"
        :columns="columns"
        @toggle="toggleExpand"
        @row-click="$emit('row-click', child)"
    />
  </template>
</template>

<script>
export default {
  name: 'TreeRow',
  props: {
    item: {
      type: Object,
      required: true
    },
    level: {
      type: Number,
      default: 0
    },
    columns: {
      type: Array
    }
  },
  emits: ['toggle', 'row-click'],
  setup(props, { emit }) {
    const toggleExpand = (id) => {
      emit('toggle', id)
    }

    const handleRowClick = () => {
      emit('row-click', props.item)
    }

    return {
      toggleExpand,
      handleRowClick
    }
  }
}
</script>

<style scoped>
.toggle-icon {
  cursor: pointer;
  margin-right: 5px;
}

.tree-row:hover {
  background-color: #0d6efd;
  color: white;
  cursor: pointer;
}
</style>