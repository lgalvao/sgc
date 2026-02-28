<template>
  <tr
      :class="[ item.clickable === false ? 'tree-row-disabled' : 'tree-row' ]"
      :data-testid="'tree-table-row-' + item.codigo"
      :tabindex="item.clickable !== false ? 0 : undefined"
      @click="handleRowClick"
      @keydown.enter="handleRowClick"
      @keydown.space="handleRowClick"
  >
    <td
        v-for="(column, index) in columns"
        :key="column.key"
        :style="index === 0 ? { paddingLeft: (level * 1.25) + 'rem' } : {}"
    >
      <button
          v-if="index === 0 && item.children && item.children.length > 0"
          :aria-expanded="item.expanded"
          :aria-label="item.expanded ? 'Recolher' : 'Expandir'"
          :data-testid="`btn-toggle-expand-${item.codigo}`"
          class="btn btn-link p-0 toggle-icon text-decoration-none border-0"
          type="button"
          @click.stop="toggleExpand(item.codigo)"
          @keydown.enter.stop="toggleExpand(item.codigo)"
          @keydown.space.stop="toggleExpand(item.codigo)"
      >
        <i :class="['bi', item.expanded ? 'bi-chevron-down' : 'bi-chevron-right']" aria-hidden="true"/>
      </button>
      {{ item[column.key] }}
    </td>
  </tr>
</template>

<script lang="ts" setup>
interface Column {
  key: string;
}

interface TreeItem {
  codigo: number | string;
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
  (e: "toggle", codigo: number | string): void;
  (e: "row-click", item: TreeItem): void;
}>();

const toggleExpand = (codigo: number | string) => {
  emit("toggle", codigo);
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
