<template>
  <div class="container mt-4">
    <div class="display-6 mb-3">Impacto no Mapa de Competências</div>

    <div v-if="unidade && processo">
      <p><strong>Unidade:</strong> {{ unidade.sigla }} - {{ unidade.nome }}</p>
      <p><strong>Processo:</strong> {{ processo.descricao }}</p>

      <h3 class="mt-4">Mudanças Registradas</h3>
      <div v-if="mudancasDoQuery.length === 0"> <!-- Usar mudancasDoQuery aqui -->
        <p>Nenhuma mudança foi registrada.</p>
      </div>
      <div v-else>
        <div v-for="mudanca in mudancasDoQuery" :key="mudanca.id" class="card mb-3"> <!-- Usar mudancasDoQuery aqui -->
          <div class="card-body">
            <div :key="mudanca.id">
              <template v-if="mudanca.tipo === TipoMudanca.AtividadeAdicionada">
                Atividade adicionada: <strong>{{ mudanca.descricaoAtividade }}</strong>
              </template>
              <template v-else-if="mudanca.tipo === TipoMudanca.AtividadeRemovida">
                Atividade removida: <strong>{{ mudanca.descricaoAtividade }}</strong>
              </template>
              <template v-else-if="mudanca.tipo === TipoMudanca.AtividadeAlterada">
                Atividade alterada: <strong>{{ mudanca.descricaoAtividade }}</strong> (de "{{ mudanca.valorAntigo }}"
                para "{{ mudanca.valorNovo }}")
              </template>
              <template v-else-if="mudanca.tipo === TipoMudanca.ConhecimentoAdicionado">
                Conhecimento adicionado à atividade "{{ mudanca.descricaoAtividade }}":
                <strong>{{ mudanca.descricaoConhecimento }}</strong>
              </template>
              <template v-else-if="mudanca.tipo === TipoMudanca.ConhecimentoRemovido">
                Conhecimento removido da atividade "{{ mudanca.descricaoAtividade }}":
                <strong>{{ mudanca.descricaoConhecimento }}</strong>
              </template>
              <template v-else-if="mudanca.tipo === TipoMudanca.ConhecimentoAlterado">
                Conhecimento alterado na atividade "{{ mudanca.descricaoAtividade }}":
                <strong>{{ mudanca.descricaoConhecimento }}</strong> (de "{{ mudanca.valorAntigo }}" para
                "{{ mudanca.valorNovo }}")
              </template>
            </div>
          </div>
        </div>
      </div>

      <h3 class="mt-4">Competências Impactadas</h3>
      <div v-if="competenciasImpactadas.size === 0">
        <p>Nenhuma competência foi impactada pelas mudanças nas atividades.</p>
      </div>
      <div v-else>
        <div v-for="[comp_id, { competencia, mudancas }] in competenciasImpactadas" :key="comp_id" class="card mb-3">
          <div class="card-body">
            <h5 class="card-title">{{ competencia.descricao }}</h5>
            <p class="text-info">Esta competência foi impactada pelas seguintes mudanças:</p>
            <ul>
              <li v-for="mudanca in mudancas" :key="mudanca.id">
                <template v-if="mudanca.tipo === TipoMudanca.AtividadeAdicionada">
                  Atividade adicionada: <strong>{{ mudanca.descricaoAtividade }}</strong>
                </template>
                <template v-else-if="mudanca.tipo === TipoMudanca.AtividadeRemovida">
                  Atividade removida: <strong>{{ mudanca.descricaoAtividade }}</strong>
                </template>
                <template v-else-if="mudanca.tipo === TipoMudanca.AtividadeAlterada">
                  Atividade alterada: <strong>{{ mudanca.descricaoAtividade }}</strong> (de "{{ mudanca.valorAntigo }}"
                  para "{{ mudanca.valorNovo }}")
                </template>
                <template v-else-if="mudanca.tipo === TipoMudanca.ConhecimentoAdicionado">
                  Conhecimento adicionado à atividade "{{ mudanca.descricaoAtividade }}":
                  <strong>{{ mudanca.descricaoConhecimento }}</strong>
                </template>
                <template v-else-if="mudanca.tipo === TipoMudanca.ConhecimentoRemovido">
                  Conhecimento removido da atividade "{{ mudanca.descricaoAtividade }}":
                  <strong>{{ mudanca.descricaoConhecimento }}</strong>
                </template>
                <template v-else-if="mudanca.tipo === TipoMudanca.ConhecimentoAlterado">
                  Conhecimento alterado na atividade "{{ mudanca.descricaoAtividade }}":
                  <strong>{{ mudanca.descricaoConhecimento }}</strong> (de "{{ mudanca.valorAntigo }}" para
                  "{{ mudanca.valorNovo }}")
                </template>
              </li>
            </ul>
          </div>
        </div>
      </div>
    </div>
    <div v-else>
      <p>Carregando informações do processo e unidade...</p>
    </div>
  </div>
</template>

<script lang="ts" setup>
import {computed} from 'vue';
import {useRoute} from 'vue-router';
import {useUnidadesStore} from '@/stores/unidades';
import {useProcessosStore} from '@/stores/processos';
import {useMapasStore} from '@/stores/mapas';
import {useAtividadesStore} from '@/stores/atividades';
import {Mudanca, TipoMudanca} from '@/stores/revisao'; // Manter import para TipoMudanca e Mudanca
import {Competencia, Processo} from '@/types/tipos';

const route = useRoute();
const idProcesso = computed(() => Number(route.params.idProcesso));
const siglaUnidade = computed(() => route.params.siglaUnidade as string);

const unidadesStore = useUnidadesStore();
const processosStore = useProcessosStore();
const mapasStore = useMapasStore();
const atividadesStore = useAtividadesStore();
// const revisaoStore = useRevisaoStore(); // Não usar mais diretamente para ler mudanças

const unidade = computed(() => unidadesStore.pesquisarUnidade(siglaUnidade.value));
const processo = computed<Processo | undefined>(() => processosStore.processos.find(p => p.id === idProcesso.value));

const mapa = computed(() => mapasStore.getMapaByUnidadeId(siglaUnidade.value, idProcesso.value));

// Ler as mudanças do query parameter
const mudancasDoQuery = computed<Mudanca[]>(() => {
  const mudancasJson = route.query.mudancas as string;
  return mudancasJson ? JSON.parse(mudancasJson) : [];
});

const competenciasImpactadas = computed<Map<number, { competencia: Competencia, mudancas: Mudanca[] }>>(() => {
  const impactedCompetencias = new Map<number, { competencia: Competencia, mudancas: Mudanca[] }>();

  if (!mapa.value) return impactedCompetencias;
  const mapaAtual = mapa.value;

  // Usar mudancasDoQuery em vez de revisaoStore.mudancasRegistradas
  mudancasDoQuery.value.forEach(mudanca => {
    let atividadeId: number | undefined;
    if (mudanca.idAtividade) {
      atividadeId = mudanca.idAtividade;
    } else if (mudanca.idConhecimento) {
      // Find the activity associated with the knowledge
      const atividade = atividadesStore.atividades.find(a => a.conhecimentos.some(c => c.id === mudanca.idConhecimento));
      if (atividade) {
        atividadeId = atividade.id;
      }
    }

    if (atividadeId) {
      mapaAtual.competencias.forEach(comp => {
        if (comp.atividadesAssociadas.includes(atividadeId as number)) {
          if (!impactedCompetencias.has(comp.id)) {
            impactedCompetencias.set(comp.id, {competencia: comp, mudancas: []});
          }
          impactedCompetencias.get(comp.id)?.mudancas.push(mudanca);
        }
      });
    }
  });

  return impactedCompetencias;
});
</script>