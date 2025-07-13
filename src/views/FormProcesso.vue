<template>
  <div class="container mt-4">
    <h2>Novo Processo</h2>
    <form class="mt-4 col-md-6 col-sm-8 col-12 p-0">
      <div class="mb-3">
        <label for="descricao" class="form-label">Descrição</label>
        <input type="text" class="form-control" id="descricao" placeholder="Descreva o processo" />
      </div>
      <div class="mb-3">
        <label for="tipo" class="form-label">Tipo</label>
        <select class="form-select" id="tipo">
          <option>Mapeamento</option>
          <option>Revisão</option>
          <option>Diagnóstico</option>
        </select>
      </div>
      <div class="mb-3">
        <label class="form-label">Unidades participantes</label>
        <div class="border rounded p-3">
          <div class="form-check">
            <input type="checkbox" id="sec-geral" v-model="unidadesSelecionadas" value="Secretaria Geral" class="form-check-input" />
            <label for="sec-geral" class="form-check-label ms-2"><strong>STIC</strong></label>
          </div>
          <div class="ms-3 form-check">
            <input type="checkbox" id="coord-ti" v-model="unidadesSelecionadas" value="Coordenação de TI" class="form-check-input" />
            <label for="coord-ti" class="form-check-label ms-2">COSIS</label>
            <div class="ms-3 form-check">
              <input type="checkbox" id="setor-desenvolvimento" v-model="unidadesSelecionadas" value="Setor de Desenvolvimento" class="form-check-input" />
              <label for="setor-desenvolvimento" class="form-check-label ms-2">SEDESENV</label>
            </div>
            <div class="ms-3 form-check">
              <input type="checkbox" id="setor-suporte" v-model="unidadesSelecionadas" value="Setor de Suporte" class="form-check-input" />
              <label for="setor-suporte" class="form-check-label ms-2">SESEL</label>
            </div>
          </div>
          <div class="ms-3 form-check">
            <input type="checkbox" id="coord-financeiro" v-model="unidadesSelecionadas" value="Coordenação Financeira" class="form-check-input" />
            <label for="coord-financeiro" class="form-check-label ms-2">COINF</label>
          </div>
        </div>
      </div>
      <div class="mb-3">
        <label for="dataLimite" class="form-label">Data limite</label>
        <input type="date" class="form-control" id="dataLimite" />
      </div>
      <button type="button" class="btn btn-primary" @click="voltarParaLista">Salvar</button>
      <router-link to="/processos" class="btn btn-secondary ms-2">Cancelar</router-link>
    </form>
  </div>
</template>

<script setup>
// Formulário fictício para criação de processo
import { ref } from 'vue'
import { useRouter } from 'vue-router'
const unidadesSelecionadas = ref([])
const router = useRouter()
function voltarParaLista() {
  router.push('/processos')
}
</script> 