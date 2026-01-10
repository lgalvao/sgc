<template>
  <BContainer class="mt-4">
    <div class="d-flex justify-content-between align-items-center mb-3">
      <div>
        <h2 class="mb-0">Autoavaliação de Competências</h2>
        <small class="text-muted">{{ siglaUnidade }} - {{ nomeUnidade }}</small>
      </div>
      <div class="d-flex gap-2">
        <BButton
            :disabled="!podeConcluir"
            data-testid="btn-concluir-autoavaliacao"
            variant="success"
            @click="concluirAutoavaliacao"
        >
          <i class="bi bi-check-circle me-2"/>Concluir Autoavaliação
        </BButton>
      </div>
    </div>

    <BAlert :fade="false" :model-value="true" variant="info">
      <i class="bi bi-info-circle me-2"/>
      Avalie a importância e o seu domínio para cada competência da unidade.
      Utilize a escala de 1 a 6 ou NA (Não se aplica).
    </BAlert>

    <div v-if="loading" class="text-center py-5">
      <BSpinner label="Carregando..."/>
    </div>

    <div v-else-if="competencias.length === 0" class="alert alert-warning">
      Nenhuma competência encontrada para esta unidade.
    </div>

    <div v-else class="row">
      <div v-for="comp in competencias" :key="comp.codigo" class="col-12 mb-4">
        <BCard class="h-100 shadow-sm">
          <template #header>
            <h5 class="card-title mb-0">{{ comp.descricao }}</h5>
          </template>

          <div class="row g-3">
            <div class="col-md-6">
              <label :for="`sel-imp-${comp.codigo}`" class="form-label fw-bold">Importância para a função:</label>
              <BFormSelect
                  :id="`sel-imp-${comp.codigo}`"
                  v-model="avaliacoes[comp.codigo].importancia"
                  :data-testid="`sel-importancia-${comp.codigo}`"
                  :options="OPCOES_NIVEL"
                  @change="salvar(comp.codigo, avaliacoes[comp.codigo].importancia, avaliacoes[comp.codigo].dominio)"
              />
            </div>

            <div class="col-md-6">
              <label :for="`sel-dom-${comp.codigo}`" class="form-label fw-bold">Seu domínio da competência:</label>
              <BFormSelect
                  :id="`sel-dom-${comp.codigo}`"
                  v-model="avaliacoes[comp.codigo].dominio"
                  :data-testid="`sel-dominio-${comp.codigo}`"
                  :options="OPCOES_NIVEL"
                  @change="salvar(comp.codigo, avaliacoes[comp.codigo].importancia, avaliacoes[comp.codigo].dominio)"
              />
            </div>

            <div class="col-12">
              <label :for="`txt-obs-${comp.codigo}`" class="form-label text-muted small">Observações (opcional):</label>
              <BFormTextarea
                  :id="`txt-obs-${comp.codigo}`"
                  v-model="avaliacoes[comp.codigo].observacoes"
                  :data-testid="`txt-obs-${comp.codigo}`"
                  placeholder="Comentários..."
                  rows="2"
                  @blur="salvar(comp.codigo, avaliacoes[comp.codigo].importancia, avaliacoes[comp.codigo].dominio, $event.target.value)"
              />
            </div>
          </div>

          <div v-if="avaliacoes[comp.codigo].salvo" class="text-end mt-2">
            <small class="text-success"><i class="bi bi-check"/> Salvo</small>
          </div>
        </BCard>
      </div>
    </div>
  </BContainer>
</template>

<script lang="ts" setup>
import {computed, onMounted, ref} from 'vue';
import {useRoute, useRouter} from 'vue-router';
import {BAlert, BButton, BCard, BContainer, BFormSelect, BFormTextarea, BSpinner} from 'bootstrap-vue-next';
import {logger} from '@/utils';
import {useMapasStore} from '@/stores/mapas';
import {useUnidadesStore} from '@/stores/unidades';
import {useFeedbackStore} from '@/stores/feedback';
import {diagnosticoService} from '@/services/diagnosticoService';
import type {Competencia} from '@/types/tipos';

const OPCOES_NIVEL = [
  {value: 'NA', text: 'NA - Não se aplica'},
  {value: 'N1', text: '1 - Baixo'},
  {value: 'N2', text: '2 - Baixo-Médio'},
  {value: 'N3', text: '3 - Médio'},
  {value: 'N4', text: '4 - Médio-Alto'},
  {value: 'N5', text: '5 - Alto'},
  {value: 'N6', text: '6 - Muito Alto'},
];

const route = useRoute();
const router = useRouter();
const mapasStore = useMapasStore();
const unidadesStore = useUnidadesStore();
const feedbackStore = useFeedbackStore();

const loading = ref(true);
const codSubprocesso = computed(() => Number(route.params.codSubprocesso || route.params.codProcesso)); // Ajuste conforme rota
const siglaUnidade = computed(() => route.params.siglaUnidade as string);

const unidade = computed(() => unidadesStore.unidade);
const nomeUnidade = computed(() => unidade.value?.nome || '');

const competencias = computed<Competencia[]>(() => mapasStore.mapaCompleto?.competencias || []);

interface AvaliacaoLocal {
  importancia: string;
  dominio: string;
  observacoes: string;
  salvo: boolean;
}

const avaliacoes = ref<Record<number, AvaliacaoLocal>>({});

const podeConcluir = computed(() => {
  if (competencias.value.length === 0) return false;
  return competencias.value.every(comp => {
    const av = avaliacoes.value[comp.codigo];
    return av && av.importancia && av.dominio;
  });
});

onMounted(async () => {
  try {
    loading.value = true;
    await unidadesStore.buscarUnidade(siglaUnidade.value);

    // Busca o mapa para ter as competências
    await mapasStore.buscarMapaCompleto(codSubprocesso.value);

    // Busca avaliações existentes
    const existentes = await diagnosticoService.buscarMinhasAvaliacoes(codSubprocesso.value);

    // Inicializa estado local
    competencias.value.forEach(comp => {
      const existente = existentes.find(e => e.competenciaCodigo === comp.codigo);
      avaliacoes.value[comp.codigo] = {
        importancia: existente?.importancia || '',
        dominio: existente?.dominio || '',
        observacoes: existente?.observacoes || '',
        salvo: !!existente
      };
    });
  } catch (error) {
    feedbackStore.show('Erro', 'Erro ao carregar dados do diagnóstico.', 'danger');
    logger.error(error);
  } finally {
    loading.value = false;
  }
});

async function salvar(competenciaCodigo: number, importancia: string, dominio: string, observacoes?: string) {
  if (!importancia || !dominio) return;

  try {
    avaliacoes.value[competenciaCodigo].salvo = false;
    await diagnosticoService.salvarAvaliacao(
        codSubprocesso.value,
        competenciaCodigo,
        importancia,
        dominio,
        observacoes || avaliacoes.value[competenciaCodigo].observacoes
    );
    avaliacoes.value[competenciaCodigo].salvo = true;
  } catch (error: any) {
    const msg = error.response?.data?.message || error.message || 'Erro ao salvar avaliação.';
    feedbackStore.show('Erro', msg, 'danger');
  }
}

async function concluirAutoavaliacao() {
  if (!podeConcluir.value) return;

  try {
    await diagnosticoService.concluirAutoavaliacao(codSubprocesso.value);
    feedbackStore.show('Sucesso', 'Autoavaliação concluída com sucesso!', 'success');
    await router.push('/painel');
  } catch (error: any) {
    feedbackStore.show('Erro', error.response?.data?.message || 'Erro ao concluir.', 'danger');
  }
}
</script>
