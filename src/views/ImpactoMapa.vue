<template>
  <div class="container mt-4">
    <div class="display-6 mb-3">Impacto no Mapa de Competências</div>

    <template v-if="unidade && processo">
      <div class="mt-5">
        <!-- Seção de Atividades Inseridas -->
        <div v-if="atividadesInseridas.length > 0">
          <h4 class="mb-3"><i class="bi bi-plus-circle me-2"></i>Atividades inseridas</h4>
          <div class="list-group mb-5">
            <div v-for="atividade in atividadesInseridas" :key="atividade.id"
                 class="list-group-item flex-column align-items-start">
              <div class="d-flex w-100 justify-content-start align-items-start">
                <i class="bi fs-4 me-3 bi-plus-circle-fill text-success" style="margin-top: 2px;"></i>
                <div class="flex-grow-1">
                  <p class="mb-1">
                    <strong>{{ atividade.descricaoAtividade }}</strong>
                  </p>
                  <!-- Conhecimentos da atividade inserida -->
                  <div v-if="conhecimentosAtividade(atividade.idAtividade).length > 0" class="mt-2 ms-3">
                    <small class="text-muted">Conhecimentos adicionados:</small>
                    <ul class="list-unstyled mt-1">
                      <li v-for="conhecimento in conhecimentosAtividade(atividade.idAtividade)" :key="conhecimento.id" class="d-flex align-items-center mb-1">
                        <i class="bi bi-plus-circle-fill text-success me-2" style="font-size: 0.875rem;"></i>
                        <small><strong>{{ conhecimento.descricaoConhecimento }}</strong></small>
                      </li>
                    </ul>
                  </div>
                </div>
              </div>
            </div>
          </div>
        </div>

        <!-- Seção de Competências Impactadas -->
        <h4 class="mb-3"><i class="bi bi-bullseye me-2"></i>Competências impactadas</h4>
        <div v-if="competenciasImpactadas.size === 0">
          <p class="text-muted">Nenhuma competência foi impactada.</p>
        </div>
        <div v-else>
          <div v-for="[comp_id, { competencia, mudancas: mudancasCompetencia }] in competenciasImpactadas"
               :key="comp_id" class="card mb-3">
            <div class="card-header bg-light">
              <h5 class="card-title mb-0">{{ competencia.descricao }}</h5>
            </div>
            <ul class="list-group list-group-flush">
              <li v-for="mudanca in mudancasCompetencia" :key="mudanca.id"
                  class="list-group-item d-flex align-items-center">
                <i class="bi me-3"
                   :class="[changeDetails[mudanca.tipo].icon, changeDetails[mudanca.tipo].color]"></i>
                <small>
                  <template
                      v-if="mudanca.tipo === TipoMudanca.AtividadeAdicionada || mudanca.tipo === TipoMudanca.AtividadeRemovida">
                    {{ changeDetails[mudanca.tipo].text }}: <strong>{{ mudanca.descricaoAtividade }}</strong>
                  </template>
                  <template v-else-if="mudanca.tipo === TipoMudanca.AtividadeAlterada">
                    {{ changeDetails[mudanca.tipo].text }}: <strong>{{ mudanca.descricaoAtividade }}</strong>
                    <small class="d-block text-muted">De "{{ mudanca.valorAntigo }}" para "{{
                        mudanca.valorNovo
                      }}"</small>
                  </template>
                  <template
                      v-else-if="mudanca.tipo === TipoMudanca.ConhecimentoAdicionado || mudanca.tipo === TipoMudanca.ConhecimentoRemovido">
                    {{ changeDetails[mudanca.tipo].text }}: <strong>{{ mudanca.descricaoConhecimento }}</strong>
                  </template>
                  <template v-else-if="mudanca.tipo === TipoMudanca.ConhecimentoAlterado">
                    {{ changeDetails[mudanca.tipo].text }}: <strong>{{ mudanca.descricaoConhecimento }}</strong>
                    <small class="d-block text-muted">De "{{ mudanca.valorAntigo }}" para "{{
                        mudanca.valorNovo
                      }}"</small>
                  </template>
                </small>
              </li>
            </ul>
          </div>
        </div>
      </div>
    </template>
    <div v-else class="text-center">
      <div class="spinner-border" role="status">
        <span class="visually-hidden">Carregando...</span>
      </div>
      <p class="mt-2">Carregando informações do processo e unidade...</p>
    </div>
  </div>
</template>

<script lang="ts" setup>
import {computed, onUnmounted} from 'vue';
import {useRoute} from 'vue-router';
import {useUnidadesStore} from '@/stores/unidades';
import {useProcessosStore} from '@/stores/processos';
import {useMapasStore} from '@/stores/mapas';
import {Mudanca, TipoMudanca, useRevisaoStore} from '@/stores/revisao';
import {Competencia, Processo} from '@/types/tipos';

const route = useRoute();
const idProcesso = computed(() => Number(route.params.idProcesso));
const siglaUnidade = computed(() => route.params.siglaUnidade as string);

const unidadesStore = useUnidadesStore();
const processosStore = useProcessosStore();
const mapasStore = useMapasStore();
const revisaoStore = useRevisaoStore();

const changeDetails = {
  [TipoMudanca.AtividadeAdicionada]: {icon: 'bi-plus-circle-fill', color: 'text-success', text: 'Atividade adicionada'},
  [TipoMudanca.AtividadeRemovida]: {icon: 'bi-dash-circle-fill', color: 'text-danger', text: 'Atividade removida'},
  [TipoMudanca.AtividadeAlterada]: {
    icon: 'bi-arrow-right-circle-fill',
    color: 'text-primary',
    text: 'Atividade alterada'
  },
  [TipoMudanca.ConhecimentoAdicionado]: {
    icon: 'bi-plus-circle-fill',
    color: 'text-success',
    text: 'Conhecimento adicionado'
  },
  [TipoMudanca.ConhecimentoRemovido]: {
    icon: 'bi-dash-circle-fill',
    color: 'text-danger',
    text: 'Conhecimento removido'
  },
  [TipoMudanca.ConhecimentoAlterado]: {
    icon: 'bi-arrow-right-circle-fill',
    color: 'text-primary',
    text: 'Conhecimento alterado'
  },
};

const unidade = computed(() => unidadesStore.pesquisarUnidade(siglaUnidade.value));
const processo = computed<Processo | undefined>(() => processosStore.processos.find(p => p.id === idProcesso.value));

const mapa = computed(() => {
  const unidade = siglaUnidade.value;
  const processo = idProcesso.value;
  return mapasStore.getMapaByUnidadeId(unidade, processo);
});

const mudancas = computed(() => {
  return revisaoStore.mudancasParaImpacto;
});

const atividadesInseridas = computed(() => {
  return mudancas.value.filter(m => m.tipo === TipoMudanca.AtividadeAdicionada);
});

const conhecimentosAtividade = (idAtividade: number | undefined) => {
  if (!idAtividade) return [];
  return mudancas.value.filter(m => 
    m.tipo === TipoMudanca.ConhecimentoAdicionado && 
    m.idAtividade === idAtividade
  );
};

onUnmounted(() => {
  revisaoStore.setMudancasParaImpacto([]);
});

const competenciasImpactadas = computed<Map<number, { competencia: Competencia, mudancas: Mudanca[] }>>(() => {
  const competenciasImpactadasMap = new Map<number, { competencia: Competencia, mudancas: Mudanca[] }>();

  if (!mapa.value) {
    return competenciasImpactadasMap;
  }
  const mapaAtual = mapa.value;

  mudancas.value
      .filter(m => m.tipo !== TipoMudanca.AtividadeAdicionada)
      .forEach(mudanca => {
        // Verifica competências explicitamente marcadas como impactadas
        if (mudanca.competenciasImpactadasIds && mudanca.competenciasImpactadasIds.length > 0) {
          mudanca.competenciasImpactadasIds.forEach(idCompetenciaImpactada => {
            const competencia = mapaAtual.competencias.find(c => c.id === idCompetenciaImpactada);
            if (competencia) {
              if (!competenciasImpactadasMap.has(competencia.id)) {
                competenciasImpactadasMap.set(competencia.id, {competencia, mudancas: []});
              }
              competenciasImpactadasMap.get(competencia.id)?.mudancas.push(mudanca);
            }
          });
        }

        // Verifica competências que têm a atividade associada
        if (mudanca.idAtividade) {
          mapaAtual.competencias.forEach(competencia => {
            if (competencia.atividadesAssociadas.includes(mudanca.idAtividade as number)) {
              if (!competenciasImpactadasMap.has(competencia.id)) {
                competenciasImpactadasMap.set(competencia.id, {competencia, mudancas: []});
              }
              const mudancasExistentes = competenciasImpactadasMap.get(competencia.id)?.mudancas || [];
              if (!mudancasExistentes.some(m => m.id === mudanca.id)) {
                competenciasImpactadasMap.get(competencia.id)?.mudancas.push(mudanca);
              }
            }
          });
        }
      });
  return competenciasImpactadasMap;
});
</script>