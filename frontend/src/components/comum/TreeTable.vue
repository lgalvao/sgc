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
          aria-label="Expandir todas as linhas"
          class="me-2"
          data-testid="btn-expandir-todas"
          size="sm"
          variant="outline-primary"
          @click="expandAll"
        >
          <i aria-hidden="true" class="bi bi-arrows-expand" />
        </BButton>
        <BButton
          aria-label="Recolher todas as linhas"
          size="sm"
          variant="outline-secondary"
          @click="collapseAll"
        >
          <i aria-hidden="true" class="bi bi-arrows-collapse" />
        </BButton>
      </div>
    </div>

    <div class="table-responsive w-100">
      <table class="table table-striped table-hover m-0" data-testid="tbl-tree">
        <colgroup>
          <col
            v-for="column in columns"
            :key="column.key"
            :style="{ width: column.width || 100 / columns.length + '%' }"
          />
        </colgroup>

        <thead v-if="!hideHeaders">
          <tr>
            <th v-for="column in columns" :key="column.key">
              {{ column.label }}
            </th>
          </tr>
        </thead>
        <tbody>
          <template v-if="flattenedData.length > 0">
            <TreeRowItem
              v-for="item in flattenedData"
              :key="item.codigo"
              :columns="columns"
              :item="item"
              :level="item.level"
              @toggle="toggleExpand"
              @row-click="handleTreeRowClick"
            />
          </template>
          <tr v-else>
            <td :colspan="columns.length" class="p-0 border-0">
              <EmptyState
                :title="emptyTitle"
                :description="emptyDescription"
                :icon="emptyIcon"
                class="border-0 bg-transparent mb-0"
              />
            </td>
          </tr>
        </tbody>
      </table>
    </div>
  </div>
</template>

<script lang="ts" setup>
import {BButton} from "bootstrap-vue-next";
import {computed, nextTick, ref, toRaw, watch} from "vue";
import TreeRowItem from "./TreeRowItem.vue";
import EmptyState from "@/components/comum/EmptyState.vue";

interface TreeItem {
  codigo: number | string;
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
  emptyTitle?: string;
  emptyDescription?: string;
  emptyIcon?: string;
}

const props = withDefaults(defineProps<TreeTableProps>(), {
  title: undefined,
  hideHeaders: false,
  emptyTitle: "Nenhum registro encontrado",
  emptyDescription: "Não há dados para exibir.",
  emptyIcon: "bi-folder2-open",
});

const emit = defineEmits<{
  'row-click': [item: TreeItem];
}>();

const internalData = ref<TreeItem[]>([]);

const initializeExpanded = (items: TreeItem[]): TreeItem[] => {
  return items.map((item) => ({
    ...item,
    expanded: item.expanded ?? false,
    children: item.children ? initializeExpanded(item.children) : [],
  }));
};

watch(
  () => props.data,
  (newData) => {
    internalData.value = initializeExpanded(
      structuredClone(toRaw(newData)),
    );
  },
  { immediate: true, deep: true },
);

const flattenedData = computed((): FlattenedTreeItem[] => {
  const flattened: FlattenedTreeItem[] = [];
  const flatten = (items: TreeItem[], level: number) => {
    for (const item of items) {
      flattened.push({ ...item, level });
      if (item.expanded && item.children) {
        flatten(item.children, level + 1);
      }
    }
  };
  flatten(internalData.value, 0);
  return flattened;
});

const findItemByCodigo = (
  items: TreeItem[],
  codigo: number | string,
): TreeItem | null => {
  for (const item of items) {
    if (item.codigo === codigo) return item;
    if (item.children) {
      const found = findItemByCodigo(item.children, codigo);
      if (found) return found;
    }
  }
  return null;
};

const toggleExpand = (codigo: number | string) => {
  const item = findItemByCodigo(internalData.value, codigo);
  if (item) {
    item.expanded = !item.expanded;
  }
};

const expandAll = () => {
  const expand = (items: TreeItem[]) => {
    items.forEach((item) => {
      if (item.children && item.children.length > 0) {
        item.expanded = true;
        expand(item.children);
      }
    });
  };
  expand(internalData.value);
  nextTick(() => {
    internalData.value = [...internalData.value];
  });
};

const collapseAll = () => {
  const collapse = (items: TreeItem[]) => {
    items.forEach((item) => {
      item.expanded = false;
      if (item.children && item.children.length > 0) {
        collapse(item.children);
      }
    });
  };
  collapse(internalData.value);
  nextTick(() => {
    internalData.value = [...internalData.value];
  });
};

const handleTreeRowClick = (clickedItem: TreeItem) => {
  emit("row-click", clickedItem);
};

defineExpose({
  internalData,
  findItemByCodigo,
  toggleExpand,
  handleTreeRowClick,
});
</script>
