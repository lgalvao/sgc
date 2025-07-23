<template>
  <tr
      class="tree-row"
      :class="{ 'child-row': level > 0 }"
      @click="navigateToUnit(item)"
      style="cursor: pointer;"
  >
    <td class="py-2">
      <div :style="{ paddingLeft: level * 20 + 'px' }">
          <span v-if="item.children && item.children.length > 0"
                @click="$emit('toggle', item.id)"
                class="expand-icon me-2"
                :class="{ expanded: item.expanded }">
            <i class="bi bi-caret-right-fill"></i>
          </span>

        <span v-else class="me-2" style="width: 20px; display: inline-block;"></span>
        <i class="bi me-2" :class="getIconClass(item)"></i>
        <span>
            {{ item.nome || '-' }}
          </span>
      </div>
    </td>

    <!-- Situação -->
    <td class="py-2">
      <div class="text-end pe-4">
          <span class="badge" :class="getSituacaoBadgeClass(item.situacao)">
            {{ item.situacao || '-' }}
          </span>
      </div>
    </td>
  </tr>

  <!-- Child rows -->
  <template v-if="item.expanded && item.children && item.children.length > 0">
    <TreeRow
        v-for="child in item.children"
        :key="child.id"
        :item="child"
        :level="level + 1"
        :columns="columns"
        @toggle="$emit('toggle', $event)"
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
      type: Array,
      required: true
    }
  },
  emits: ['toggle', 'row-click'],

  setup() {
    const getColumnValue = (item, column) => {
      return item[column.key] || '-'
    }

    const getIconClass = () => {
      return 'bi-file-earmark text-info'
    }

    const getBadgeClass = (type) => {
      return type === 'folder' ? 'bg-warning text-dark' : 'bg-info'
    }

    const getSituacaoBadgeClass = (situacao) => {
      switch (situacao?.toLowerCase()) {
        case 'finalizado':
          return 'bg-success';
        case 'em andamento':
          return 'bg-warning text-dark';
        case 'não iniciado':
        default:
          return 'bg-secondary';
      }
    }

    const navigateToUnit = (item) => {
      if (item.id) {
        emit('row-click', item.id);
      }
    };

    return {
      getColumnValue,
      getIconClass,
      getBadgeClass,
      getSituacaoBadgeClass,
      navigateToUnit
    }
  }
}
</script>

<style scoped>
.tree-row {
  vertical-align: middle;
  cursor: pointer;
}

.tree-row:hover {
  background-color: #f8f9fa;
}

.tree-row td {
  vertical-align: middle;
  white-space: nowrap;
  overflow: hidden;
  text-overflow: ellipsis;
}

/* Estilo para linhas filhas */
.child-row {
  background-color: rgba(var(--bs-secondary-rgb), 0.05);
}

/* Estilo para o ícone de expandir/recolher */
.expand-icon {
  cursor: pointer;
  transition: transform 0.2s ease;
  display: inline-block;
  width: 20px;
  text-align: center;
}

.expand-icon.expanded {
  transform: rotate(90deg);
}

.child-row {
  background-color: rgba(var(--bs-secondary-rgb), 0.05);
}

.expand-icon {
  cursor: pointer;
  transition: transform 0.2s ease;
  display: inline-block;
  width: 20px;
  text-align: center;
}

.expand-icon.expanded {
  transform: rotate(90deg);
}
</style>