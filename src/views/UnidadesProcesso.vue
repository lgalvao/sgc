<template>
  <div class="container mt-4">
    <h2>Unidades participantes do processo</h2>
    <div class="list-group mt-4">
      <TreeNode
        v-for="unidade in unidades"
        :key="unidade.sigla"
        :unidade="unidade"
        :abertas="abertas"
        @abrir="abrirAtividadesConhecimentos"
      />
    </div>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import TreeNode from '../components/TreeNode.vue'
import unidadesJson from '../mocks/unidades.json'
const router = useRouter()
const unidades = ref(unidadesJson)
const abertas = ref({})
function abrirAtividadesConhecimentos(sigla) {
  router.push(`/processos/1/unidade/${sigla}/atividades`)
}
function expandirTodos(unidadesArr) {
  for (const unidade of unidadesArr) {
    if (unidade.filhas && unidade.filhas.length) {
      abertas.value[unidade.sigla] = true
      expandirTodos(unidade.filhas)
    }
  }
}
onMounted(() => {
  expandirTodos(unidades.value)
})
</script>

<style scoped>
.unidade-folha {
  cursor: pointer;
  transition: background 0.2s;
}
.unidade-folha:hover {
  background: #e9ecef;
}
</style> 