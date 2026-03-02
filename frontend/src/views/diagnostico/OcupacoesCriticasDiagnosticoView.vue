<template>
  <LayoutPadrao>
    <PageHeader
        :subtitle="`${unidade?.sigla || ''} - ${unidade?.nome || ''}`"
        title="Ocupações Críticas"
    >
      <template #actions>
        <BButton to="/painel" variant="primary">Voltar</BButton>
      </template>
    </PageHeader>

    <BAlert :fade="false" :model-value="true" variant="info">
      <i aria-hidden="true" class="bi bi-info-circle me-2"/>
      Identifique a situação de capacitação para as competências com Gap significativo.
    </BAlert>

    <div v-if="loading" class="text-center py-5">
      <BSpinner label="Carregando..."/>
    </div>

    <div v-else>
      <div v-if="servidoresComGap.length === 0" class="alert alert-success">
        Nenhuma ocupação crítica identificada (nenhum Gap >= 2 encontrado).
      </div>

      <div v-for="servidor in servidoresComGap" :key="servidor.tituloEleitoral" class="card mb-4 shadow-sm">
        <div class="card-header bg-light">
          <h5 class="mb-0">{{ servidor.nome }}</h5>
        </div>
        <div class="card-body p-0">
          <div class="table-responsive">
            <table class="table table-hover mb-0">
              <thead class="table-light">
              <tr>
                <th style="width: 40%">Competência</th>
                <th class="text-center" style="width: 10%">Imp.</th>
                <th class="text-center" style="width: 10%">Dom.</th>
                <th class="text-center" style="width: 10%">Gap</th>
                <th style="width: 30%">Situação Cap.</th>
              </tr>
              </thead>
              <tbody>
              <tr v-for="gap in servidor.gaps" :key="gap.avaliacao.codigo">
                <td>{{ gap.avaliacao.competenciaDescricao }}</td>
                <td class="text-center"><span class="badge bg-secondary">{{ gap.avaliacao.importanciaLabel }}</span>
                </td>
                <td class="text-center"><span class="badge bg-info">{{ gap.avaliacao.dominioLabel }}</span></td>
                <td class="text-center"><span class="badge bg-danger">{{ gap.avaliacao.gap }}</span></td>
                <td>
                  <BFormSelect
                      v-model="gap.ocupacao.situacao"
                      :options="OPCOES_SITUACAO"
                      size="sm"
                      @change="gap.ocupacao.situacao && salvar(servidor.tituloEleitoral, gap.avaliacao.competenciaCodigo, gap.ocupacao.situacao)"
                  />
                  <small v-if="gap.salvo" class="text-success ms-2"><i aria-hidden="true" class="bi bi-check"/></small>
                </td>
              </tr>
              </tbody>
            </table>
          </div>
        </div>
      </div>
    </div>
  </LayoutPadrao>
</template>

<script lang="ts" setup>
import {computed, onMounted, ref} from 'vue';
import {useRoute} from 'vue-router';
import {BAlert, BButton, BFormSelect, BSpinner} from 'bootstrap-vue-next';
import LayoutPadrao from '@/components/layout/LayoutPadrao.vue';
import PageHeader from '@/components/layout/PageHeader.vue';
import {useUnidadesStore} from '@/stores/unidades';
import {useFeedbackStore} from '@/stores/feedback';
import {
  type AvaliacaoServidorDto,
  type DiagnosticoDto,
  diagnosticoService,
  type ServidorDiagnosticoDto
} from '@/services/diagnosticoService';

const OPCOES_SITUACAO = [
  {value: null, text: 'Selecione...'},
  {value: 'NA', text: 'Não se aplica'},
  {value: 'AC', text: 'A capacitar'},
  {value: 'EC', text: 'Em capacitação'},
  {value: 'C', text: 'Capacitado'},
  {value: 'I', text: 'Instrutor'},
];

const route = useRoute();
const unidadesStore = useUnidadesStore();
const feedbackStore = useFeedbackStore();

const loading = ref(true);
const codSubprocesso = computed(() => Number(route.params.codSubprocesso));
const unidade = computed(() => unidadesStore.unidade);

const diagnostico = ref<DiagnosticoDto | null>(null);

interface GapItem {
  avaliacao: AvaliacaoServidorDto;
  ocupacao: { situacao: string | null };
  salvo: boolean;
}

interface ServidorGaps {
  tituloEleitoral: string;
  nome: string;
  gaps: GapItem[];
}

const servidoresComGap = ref<ServidorGaps[]>([]);

onMounted(async () => {
  try {
    loading.value = true;
    // Carrega unidade se necessário (via store ou api)
    // Busca diagnóstico completo
    const diag = await diagnosticoService.buscarDiagnostico(codSubprocesso.value);
    diagnostico.value = diag;

    // Processa dados para extrair Gaps >= 2
    servidoresComGap.value = diag.servidores
        .map(servidor => ({
          tituloEleitoral: servidor.tituloEleitoral,
          nome: servidor.nome,
          gaps: filtrarGaps(servidor)
        }))
        .filter(s => s.gaps.length > 0);

  } catch (error) {
    feedbackStore.show('Erro', 'Erro ao carregar ocupações críticas: ' + error, 'danger');
  } finally {
    loading.value = false;
  }
});

function filtrarGaps(servidor: ServidorDiagnosticoDto): GapItem[] {
  return servidor.avaliacoes
      .filter(av => (av.gap !== null && av.gap >= 2)) // Regra de negócio: Gap >= 2 (N5-N3=2)
      .map(av => {
        // Procura ocupação já salva
        const ocupacaoExistente = servidor.ocupacoes.find(o => o.competenciaCodigo === av.competenciaCodigo);
        return {
          avaliacao: av,
          ocupacao: {situacao: ocupacaoExistente?.situacao || null},
          salvo: !!ocupacaoExistente
        };
      });
}

async function salvar(servidorTitulo: string, competenciaCodigo: number, situacao: string) {
  if (!situacao) return;

  try {
    const servidor = servidoresComGap.value.find(s => s.tituloEleitoral === servidorTitulo);
    const gap = servidor?.gaps.find(g => g.avaliacao.competenciaCodigo === competenciaCodigo);
    if (gap) gap.salvo = false;

    await diagnosticoService.salvarOcupacao(
        codSubprocesso.value,
        servidorTitulo,
        competenciaCodigo,
        situacao
    );

    if (gap) gap.salvo = true;
  } catch (error) {
    feedbackStore.show('Erro', 'Erro ao salvar ocupação: ' + error, 'danger');
  }
}
</script>
