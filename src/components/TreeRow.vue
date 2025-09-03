<template>
  <tr :class="[ item.clickable === false ? 'tree-row-disabled' : 'tree-row' ]" @click="handleRowClick" data-testid="tree-table-row">
    <td v-for="(column, index) in columns" :key="column.key"
        :style="index === 0 ? { paddingLeft: (level * 20) + 'px' } : {}">
      <span v-if="index === 0 && item.children && item.children.length > 0" class="toggle-icon"
            @click.stop="toggleExpand(item.id)">
        <i :class="['bi', item.expanded ? 'bi-chevron-down' : 'bi-chevron-right']"></i>
      </span>
      {{ item[column.key] }}
    </td>
  </tr>
</template>

<script lang="ts" setup>

interface Column {
  key: string;
}

interface TreeItem {
  id: number | string;
  expanded?: boolean;
  children?: TreeItem[];
  clickable?: boolean;
  level: number;

  [key: string]: any;
}

const props = defineProps<{
  item: TreeItem;
  level: number;
  columns: Column[];
}>();

const emit = defineEmits<{
  (e: 'toggle', id: number | string): void;
  (e: 'row-click', item: TreeItem): void;
}>();

const toggleExpand = (id: number | string) => {
  emit('toggle', id);
};

const handleRowClick = () => {
  if (props.item.clickable === false) return;
  emit('row-click', props.item);
};
</script>

<style scoped>
.toggle-icon {
  cursor: pointer;
  margin-right: 5px;
}

.tree-row:hover {
  background-color: #f0f0f0;
  cursor: pointer;
}

.tree-row-disabled {
  cursor: not-allowed;
  background-color: #fafafa;
}

.tree-row-disabled:hover {
  background-color: #fafafa;
}
</style>
