<template>
  <div class="container mt-4">
    <button class="btn btn-secondary mb-3" @click="voltar">Voltar</button>
    <h2>Edição de Mapa de Competências</h2>
    <div v-if="unidade">
      <div class="mb-3">
        <strong>Unidade:</strong> {{ unidade.sigla }} - {{ unidade.nome || unidade.sigla }}
      </div>

      <div class="mb-4">
        <h5>Atividades</h5>
        <div v-for="atividade in atividades" :key="atividade.id" class="form-check">
          <input :id="`atv-${atividade.id}`" v-model="atividadesSelecionadas"
                 :value="atividade.id"
                 class="form-check-input"
                 type="checkbox">
          <label :for="`atv-${atividade.id}`" class="form-check-label">
            {{ atividade.descricao }}
          </label>
        </div>
      </div>

      <div class="mb-4">
        <h5>Descrição da Competência</h5>
        <form @submit.prevent="adicionarCompetencia">
          <div class="mb-2">
            <textarea v-model="novaCompetencia.descricao"
                      class="form-control"
                      placeholder="Descreva a competência"
                      rows="3">
            </textarea>
          </div>
          <button :disabled="atividadesSelecionadas.length === 0 || !novaCompetencia.descricao"
                  class="btn btn-primary"
                  type="submit">
            Adicionar competência
          </button>
        </form>
      </div>

      <div class="mb-4">
        <h5>Competências cadastradas</h5>
        <div v-if="competencias.length === 0" class="text-muted">Nenhuma competência cadastrada ainda.</div>
        <div v-for="comp in competencias" :key="comp.id" class="card mb-2">
          <div class="card-body">
            <strong> {{ comp.descricao }}</strong><br>
            <ul>
              <li v-for="atvId in comp.atividadesAssociadas" :key="atvId">
                {{ descricaoAtividade(atvId) }}
              </li>
            </ul>
          </div>
        </div>
      </div>
      <button :disabled="competencias.length === 0" class="btn btn-success" @click="finalizarEdicao">Gerar Mapa</button>
    </div>
    <div v-else>
      <p>Unidade não encontrada.</p>
    </div>
  </div>
</template>

<script setup lang="ts">
import {computed, ref} from 'vue'
import {useRoute, useRouter} from 'vue-router'
import {storeToRefs} from 'pinia'

import {useMapasStore} from '@/stores/mapas'
import {useUnidadesStore} from '@/stores/unidades'
import {useAtividadesStore} from "@/stores/atividades";
import {useProcessosStore} from "@/stores/processos";
import {Atividade, Competencia, Unidade} from '@/types/tipos';

const route = useRoute()
const router = useRouter()
const sigla = computed(() => route.params.sigla as string)
const processoId = computed(() => Number(route.query.processoId))
const unidadesStore = useUnidadesStore()
const mapaStore = useMapasStore()
const atividadesStore = useAtividadesStore()
const processosStore = useProcessosStore()

const {unidades} = storeToRefs(unidadesStore)

function buscarUnidade(unidades: Unidade[], sigla: string): Unidade | null {
  for (const unidade of unidades) {
    if (unidade.sigla === sigla) return unidade
    if (unidade.filhas && unidade.filhas.length) {
      const encontrada = buscarUnidade(unidade.filhas, sigla)
      if (encontrada) return encontrada
    }
  }
  return null
}

const unidade = computed<Unidade | null>(() => buscarUnidade(unidades.value as Unidade[], sigla.value))
const processoUnidadeId = computed(() => {
  const processoUnidade = processosStore.processosUnidade.find(
      (pu: any) => pu.processoId === processoId.value && pu.unidade === sigla.value
  );
  return processoUnidade?.id;
});

const atividades = computed<Atividade[]>(() => {
  if (typeof processoUnidadeId.value !== 'number') {
    return []
  }
  return atividadesStore.getAtividadesPorProcessoUnidade(processoUnidadeId.value) || []
})
const mapa = computed(() => mapaStore.getMapaVigentePorUnidade(sigla.value))
const competencias = ref<Competencia[]>(mapa.value ? [...mapa.value.competencias] : [])
const atividadesSelecionadas = ref<number[]>([])
const novaCompetencia = ref({descricao: ''})

function descricaoAtividade(id: number): string {
  const atv = atividades.value.find(a => a.id === id)
  return atv ? atv.descricao : 'Atividade não encontrada'
}

function adicionarCompetencia() {
  if (!novaCompetencia.value.descricao || atividadesSelecionadas.value.length === 0) return
  competencias.value.push({
    id: Date.now(),
    descricao: novaCompetencia.value.descricao,
    atividadesAssociadas: [...atividadesSelecionadas.value]
  })
  novaCompetencia.value.descricao = ''
  atividadesSelecionadas.value = []
}

function finalizarEdicao() {
  if (mapa.value) {
    mapaStore.editarMapa(mapa.value.id, {competencias: competencias.value})
    router.push({path: '/finalizacao-mapa', query: {sigla: sigla.value}})
  }
}

function voltar() {
  router.back()
}
</script> 