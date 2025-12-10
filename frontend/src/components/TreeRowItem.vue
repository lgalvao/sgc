<template>
  <tr
      :class="[ item.clickable === false ? 'tree-row-disabled' : 'tree-row' ]"
      :data-testid="'tree-table-row-' + item.id"
      @click="handleRowClick"
  >
    <td
        v-for="(column, index) in columns"
        :key="column.key"
        :style="index === 0 ? { paddingLeft: (level * 1.25) + 'rem' } : {}"
    >
      <span
          v-if="index === 0 && item.children && item.children.length > 0"
          :data-testid="`btn-toggle-expand-${item.id}`"
          class="toggle-icon"
          @click.stop="toggleExpand(item.id)"
      >
        <i :class="['bi', item.expanded ? 'bi-chevron-down' : 'bi-chevron-right']"/>
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
  level?: number;

  [key: string]: any;
}

const props = defineProps<{
  item: TreeItem;
  level: number;
  columns: Column[];
}>();

const emit = defineEmits<{
  (e: "toggle", id: number | string): void;
  (e: "row-click", item: TreeItem): void;
}>();

const toggleExpand = (id: number | string) => {
  emit("toggle", id);
};

const handleRowClick = () => {
  if (props.item.clickable === false) return;
  emit("row-click", props.item);
};

defineExpose({
  toggleExpand,
  handleRowClick,
});
</script>

<style scoped>
.toggle-icon {
  cursor: pointer;
  margin-right: 0.3125rem;
}

</style>
