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
        :class="{ 'tree-table-primeira-coluna': index === 0 }"
        :title="(item[column.key + 'Tooltip'] as string) || ''"
    >
      <template v-if="index === 0">
        <div
            :style="obterEstiloPrimeiraColuna(level)"
            class="tree-table-primeira-coluna-conteudo"
        >
          <BButton
              v-if="item.children && item.children.length > 0"
              :aria-expanded="item.expanded"
              :aria-label="item.expanded ? 'Recolher' : 'Expandir'"
              :data-testid="`btn-toggle-expand-${item.codigo}`"
              :class="[
                'p-0',
                'tree-table-toggle-slot',
                'toggle-hit-area',
                'text-decoration-none',
                'border-0',
                item.expanded ? 'toggle-hit-area-recolher' : 'toggle-hit-area-expandir',
              ]"
              variant="link"
              @click.stop="toggleExpand(item.codigo)"
              @keydown.enter.stop="toggleExpand(item.codigo)"
              @keydown.space.stop="toggleExpand(item.codigo)"
          >
            <i
                :class="[
                  'bi',
                  'bi-chevron-right',
                  'toggle-icon-indicador',
                  { 'toggle-icon-indicador-expandido': item.expanded },
                ]"
                aria-hidden="true"
            />
          </BButton>
          <span v-else aria-hidden="true" class="tree-table-toggle-slot"/>
          <span class="tree-table-texto">
            {{ item[column.key] }}
          </span>
        </div>
      </template>
      <template v-else>
        {{ item[column.key] }}
      </template>
    </td>
  </tr>
</template>

<script lang="ts" setup>
import {BButton} from "bootstrap-vue-next";

interface Column {
  key: string;
}

interface TreeItem {
  codigo: number | string;
  expanded?: boolean;
  children?: TreeItem[];
  clickable?: boolean;
  level?: number;

  [key: string]: unknown;
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

const ESPACAMENTO_BASE_REM = 0.75;
const RECUO_NIVEL_REM = 1.25;
const LARGURA_TOGGLE_REM = 1.75;

const obterLarguraGutter = (level: number) =>
    `${ESPACAMENTO_BASE_REM + (level * RECUO_NIVEL_REM) + LARGURA_TOGGLE_REM}rem`;

const obterEstiloPrimeiraColuna = (level: number) => ({
  "--tree-table-largura-gutter": obterLarguraGutter(level),
});

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
.tree-row {
  cursor: pointer;
}

.tree-row-disabled {
  cursor: default;
}

.tree-table-primeira-coluna {
  min-width: 18rem;
}

.tree-table-primeira-coluna-conteudo {
  min-height: 1.5rem;
  min-width: 0;
  padding-left: calc(var(--tree-table-largura-gutter) + 0.375rem);
  position: relative;
}

.tree-table-toggle-slot {
  display: inline-flex;
  inset: 0 auto 0 0;
  position: absolute;
  width: var(--tree-table-largura-gutter);
}

.tree-table-texto {
  display: block;
  min-width: 0;
}

.toggle-hit-area {
  align-items: center;
  display: inline-flex;
  height: 100%;
  inset: 0 auto 0 0;
  justify-content: flex-end;
  line-height: 1;
  margin: 0;
  padding: 0 0.375rem 0 0;
  position: absolute;
  width: var(--tree-table-largura-gutter);
}

.toggle-hit-area :deep(.bi) {
  font-size: 1rem;
}

.toggle-icon-indicador {
  transition: transform 0.18s ease;
}

.toggle-icon-indicador-expandido {
  transform: rotate(90deg);
}

.toggle-hit-area-expandir {
  cursor: zoom-in;
}

.toggle-hit-area-recolher {
  cursor: zoom-out;
}

</style>
