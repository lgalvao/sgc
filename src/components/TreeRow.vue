<template>
  <tr @click="handleRowClick" :class="[ item.clickable === false ? 'tree-row-disabled' : 'tree-row' ]">
    <td v-for="(column, index) in columns" :key="column.key"
        :style="index === 0 ? { paddingLeft: (level * 20) + 'px' } : {}">
      <span v-if="index === 0 && item.children && item.children.length > 0" @click.stop="toggleExpand(item.id)"
            class="toggle-icon">
        <i :class="['bi', item.expanded ? 'bi-chevron-down' : 'bi-chevron-right']"></i>
      </span>
      {{ item[column.key] }}
    </td>
  </tr>
  <template v-if="item.expanded && item.children">
    <TreeRow
        v-for="child in item.children"
        :key="child.id"
        :item="child"
        :level="level + 1"
        :columns="columns"
        @toggle="toggleExpand"
        @row-click="handleChildRowClick"
    />
  </template>
</template>

<script setup lang="ts">
import TreeRow from './TreeRow.vue';

interface Column {
  key: string;
}

interface TreeItem {
  id: number | string;
  expanded?: boolean;
  children?: TreeItem[];
  [key: string]: any; // Para permitir acesso a propriedades dinâmicas como item[column.key]
}

const props = withDefaults(defineProps<{
  item: TreeItem;
  level?: number;
  columns?: Column[];
}>(), {
  level: 0,
  columns: () => []
});

const { level, columns } = props;

const emit = defineEmits(['toggle', 'row-click']);

const toggleExpand = (id: number | string) => {
  emit('toggle', id);
};

const handleRowClick = () => {
  if ((props.item as any).clickable === false) return;
  emit('row-click', props.item);
};

const handleChildRowClick = (childItem: TreeItem) => {
  emit('row-click', childItem);
};
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

.tree-row-disabled {
  cursor: default;
}

.tree-row-disabled:hover {
  background-color: inherit;
  color: inherit;
  cursor: default;
}
</style>