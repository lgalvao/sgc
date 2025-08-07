<template>
  <div class="container mt-4">
    <button class="btn btn-secondary mb-3" @click="voltar">Voltar</button>
    <h2>Visualização de Mapa de Competências</h2>
    <div v-if="unidade && mapa">
      <div class="mb-3">
        <strong>Unidade:</strong> {{ unidade.sigla }} - {{ unidade.nome || unidade.sigla }}
      </div>
      <div class="mb-4">
        <h5>Competências cadastradas</h5>
        <div v-if="mapa.competencias.length === 0" class="text-muted">Nenhuma competência cadastrada ainda.</div>
        <div v-for="comp in mapa.competencias" :key="comp.id" class="card mb-2">
          <div class="card-body">
            <strong>Descrição:</strong> {{ comp.descricao }}<br>
            <strong>Atividades associadas:</strong>
            <ul>
              <li v-for="atvId in comp.atividadesAssociadas" :key="atvId">
                {{ descricaoAtividade(atvId) }}
              </li>
            </ul>
          </div>
        </div>
      </div>
    </div>
    <div v-else>
      <p>Unidade ou mapa não encontrado.</p>
    </div>
  </div>
</template>

<script setup lang="ts">
import {computed} from 'vue'
import {useRoute, useRouter} from 'vue-router'
import {storeToRefs} from 'pinia'
import {useMapasStore} from '@/stores/mapas'
import {useUnidadesStore} from '@/stores/unidades'
import {useAtividadesStore} from "@/stores/atividades";
import {useProcessosStore} from "@/stores/processos";
import {Atividade, Mapa, ProcessoUnidade, Unidade} from '@/types/tipos';

const route = useRoute()
const router = useRouter()
const sigla = computed(() => route.params.sigla as string)
const processoId = computed(() => Number(route.query.processoId))
const unidadesStore = useUnidadesStore()
const {unidades} = storeToRefs(unidadesStore)
const mapaStore = useMapasStore()
const atividadesStore = useAtividadesStore()
const processosStore = useProcessosStore()

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
const mapa = computed<Mapa | null>(() => mapaStore.getMapaVigentePorUnidade(sigla.value) as Mapa | null)
const processoUnidadeId = computed<number | undefined>(() => {
  const processoUnidade = (processosStore.processosUnidade as ProcessoUnidade[]).find(
      pu => pu.processoId === processoId.value && pu.unidade === sigla.value
  );
  return processoUnidade?.id;
});

const atividades = computed<Atividade[]>(() => {
  if (typeof processoUnidadeId.value === 'number') {
    return atividadesStore.getAtividadesPorProcessoUnidade(processoUnidadeId.value) as Atividade[]
  }
  return []
})

function descricaoAtividade(id: number): string {
  const atv = atividades.value.find(a => a.id === id)
  return atv ? atv.descricao : 'Atividade não encontrada'
}

function voltar() {
  router.back()
}
</script> 