<template>
  <div class="container mt-4">
    <button class="btn btn-secondary mb-3" @click="voltar">Voltar</button>
    <h2>Finalização de Mapa de Competências</h2>
    <div v-if="unidade && mapa">
      <div class="mb-3">
        <strong>Unidade:</strong> {{ unidade.sigla }} - {{ unidade.nome }}
      </div>

      <div class="mb-4">
        <h5>Competências cadastradas</h5>
        <div v-for="comp in mapa.competencias" :key="comp.id" class="card mb-2">
          <div class="card-body">
            <strong>{{ comp.descricao }}</strong> <br>
            <div v-if="incluirAtividades">
              <ul>
                <li v-for="atvId in comp.atividadesAssociadas" :key="atvId">
                  {{ descricaoAtividade(atvId) }}
                </li>
              </ul>
            </div>
          </div>
        </div>
      </div>

      <div class="form-check mb-3">
        <input id="incluirAtividades" v-model="incluirAtividades" class="form-check-input" type="checkbox">
        <label class="form-check-label" for="incluirAtividades">
          Incluir descrição das atividades no mapa gerado
        </label>
      </div>

      <div class="mb-3">
        <label class="form-label" for="dataLimite">Data limite para validação</label>
        <input id="dataLimite" v-model="dataLimite" class="form-control" type="date"/>
      </div>

      <button :disabled="!dataLimite" class="btn btn-success" @click="disponibilizarMapa">Disponibilizar</button>
      <div v-if="notificacao" class="alert alert-info mt-4">
        {{ notificacao }}
      </div>
    </div>
    <div v-else>
      <p>Unidade ou mapa não encontrado.</p>
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
import {useProcessosStore} from '@/stores/processos'
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
  if (processoUnidadeId.value !== undefined) {
    return (atividadesStore.getAtividadesPorProcessoUnidade(processoUnidadeId.value) as Atividade[]) || [];
  }
  return [];
});
const incluirAtividades = ref(true)
const dataLimite = ref('')
const notificacao = ref('')

function descricaoAtividade(id: number): string {
  const atv = atividades.value.find(a => a.id === id)
  return atv ? atv.descricao : 'Atividade não encontrada'
}

function formatarData(data: string): string {
  if (!data) return ''
  const [ano, mes, dia] = data.split('-')
  return `${dia}/${mes}/${ano}`
}

function disponibilizarMapa() {
  if (!mapa.value || !unidade.value) {
    notificacao.value = 'Erro: Mapa ou unidade não encontrados.'
    return
  }

  const currentMapa = mapa.value;
  const currentUnidade = unidade.value;

  mapaStore.editarMapa(currentMapa.id, {
    situacao: 'disponivel_validacao',
    dataDisponibilizacao: new Date(),
  })

  notificacao.value = `Notificação: O mapa de competências da unidade ${currentUnidade.sigla} foi disponibilizado para validação até ${formatarData(dataLimite.value)}. (Simulação)`
}

function voltar() {
  router.back()
}
</script>