<template>
  <div
    class="list-group-item"
    :class="{'unidade-folha': !unidade.filhas.length, 'unidade-folha-hover': folhaHover, 'participante': isParticipante()}"
    @mouseenter="onMouseEnter"
    @mouseleave="onMouseLeave"
    @click.stop="onClickNode"
  >
    <div class="d-flex align-items-center">
      <button
        v-if="unidade.filhas.length"
        class="btn btn-sm btn-link p-0 me-2"
        type="button"
        tabindex="-1"
      >
        <span v-if="abertas[unidade.sigla]">–</span>
        <span v-else>+</span>
      </button>
      <span v-else class="me-4"></span>
      <strong style="user-select: none;">
        {{ unidade.sigla }}
      </strong>
      <span class="badge bg-secondary ms-2">{{ unidade.situacao }}</span>
      <span v-if="isParticipante()" class="badge bg-success ms-2">Participante</span>
    </div>
    <div v-if="unidade.filhas.length && abertas[unidade.sigla]" class="ms-4 mt-2">
      <TreeNode
        v-for="filha in unidade.filhas"
        :key="filha.sigla"
        :unidade="filha"
        :abertas="abertas"
        :participantes="participantes"
        @abrir="$emit('abrir', $event)"
      />
    </div>
  </div>
</template>

<script setup>
import { ref } from 'vue'
const props = defineProps({
  unidade: Object,
  abertas: Object,
  participantes: Array // nova prop
})
const emit = defineEmits(['abrir'])
const folhaHover = ref(false)
function toggle() {
  props.abertas[props.unidade.sigla] = !props.abertas[props.unidade.sigla]
}
function abrirFolha() {
  if (!props.unidade.filhas.length) emit('abrir', props.unidade.sigla)
}
function onMouseEnter() {
  if (!props.unidade.filhas.length) folhaHover.value = true
}
function onMouseLeave() {
  folhaHover.value = false
}
function onClickNode() {
  if (props.unidade.filhas.length) {
    toggle()
  } else {
    abrirFolha()
  }
}
const isParticipante = () => props.participantes && props.participantes.includes(props.unidade.sigla)
</script>

<style scoped>
.unidade-folha {
  cursor: pointer;
  transition: background 0.2s, color 0.2s;
}
.unidade-folha-hover {
  background: #0d6efd !important; /* Bootstrap primary */
  color: #fff !important;
}
.unidade-folha-hover strong,
.unidade-folha-hover .badge {
  color: #fff !important;
}
.participante {
  background: #e6ffe6 !important;
}
</style> 