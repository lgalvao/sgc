<template>
  <LayoutPadrao>
    <PageHeader title="Atividades e conhecimentos">
      <template #default>
        <div class="unidade-cabecalho mb-0">
          <span class="unidade-sigla">{{ siglaUnidade }}</span>
          <span class="unidade-nome">{{ nomeUnidade }}</span>
        </div>
      </template>
      <template #actions>
        <BButton
            v-if="podeVerImpacto"
            data-testid="cad-atividades__btn-impactos-mapa-visualizacao"
            variant="outline-secondary"
            @click="abrirModalImpacto"
        >
          <i aria-hidden="true" class="bi bi-arrow-right-circle me-2"/>{{
            isRevisao ? 'Ver impactos' : 'Impacto no mapa'
          }}
        </BButton>
        <BButton
            data-testid="btn-vis-atividades-historico"
            variant="outline-info"
            @click="abrirModalHistoricoAnalise"
        >
          Histórico de análise
        </BButton>
        <BButton
            v-if="podeDevolverCadastro"
            data-testid="btn-acao-devolver"
            title="Devolver para ajustes"
            variant="secondary"
            @click="devolverCadastro"
        >
          Devolver para ajustes
        </BButton>
        <BButton
            v-if="podeAceitarCadastro || podeHomologarCadastro"
            data-testid="btn-acao-analisar-principal"
            title="Validar"
            variant="success"
            @click="validarCadastro"
        >
          {{ podeHomologarCadastro ? 'Homologar' : 'Registrar aceite' }}
        </BButton>
      </template>
    </PageHeader>

    <!-- Lista de atividades -->
    <VisAtividadeItem
        v-for="(atividade) in atividades"
        :key="atividade.codigo"
        :atividade="atividade"
    />

    <!-- Modal de Impacto no Mapa -->
    <ImpactoMapaModal
        v-if="codSubprocesso"
        :impacto="impactos"
        :loading="loadingImpacto"
        :mostrar="mostrarModalImpacto"
        @fechar="fecharModalImpacto"
    />

    <!-- Modal de Histórico de Análise -->
    <HistoricoAnaliseModal
        :historico="historicoAnalises"
        :mostrar="mostrarModalHistoricoAnalise"
        @fechar="fecharModalHistoricoAnalise"
    />

    <!-- Modal de Validação -->
    <ModalConfirmacao
        v-model="mostrarModalValidar"
        :auto-close="false"
        :loading="loadingValidacao"
        :titulo="isHomologacao ? 'Homologação do cadastro de atividades e conhecimentos' : (isRevisao ? 'Aceite da revisão do cadastro' : 'Validação do cadastro')"
        ok-title="Confirmar"
        test-id-confirmar="btn-aceite-cadastro-confirmar"
        variant="success"
        @confirmar="confirmarValidacao"
    >
      <p>{{
          isHomologacao ? 'Confirma a homologação do cadastro de atividades e conhecimentos?' : (isRevisao ? 'Confirma o aceite da revisão do cadastro de atividades?' : 'Confirma o aceite do cadastro de atividades?')
        }}</p>
      <BFormGroup class="mb-3" label="Observação" label-for="observacaoValidacao">
        <BFormTextarea
            id="observacaoValidacao"
            v-model="observacaoValidacao"
            data-testid="inp-aceite-cadastro-obs"
            rows="3"
        />
      </BFormGroup>
    </ModalConfirmacao>

    <!-- Modal de Devolução -->
    <ModalConfirmacao
        v-model="mostrarModalDevolver"
        :auto-close="false"
        :loading="loadingDevolucao"
        :ok-disabled="!observacaoDevolucao.trim()"
        :titulo="isRevisao ? 'Devolução da revisão do cadastro' : 'Devolução do cadastro'"
        ok-title="Confirmar"
        test-id-confirmar="btn-devolucao-cadastro-confirmar"
        variant="danger"
        @confirmar="confirmarDevolucao"
    >
      <p>{{
          isRevisao ? 'Confirma a devolução da revisão do cadastro para ajustes?' : 'Confirma a devolução do cadastro para ajustes?'
        }}</p>
      <BFormGroup class="mb-3" label="Observação (obrigatório)" label-for="observacaoDevolucao">
        <BFormTextarea
            id="observacaoDevolucao"
            v-model="observacaoDevolucao"
            :state="estadoObservacaoDevolucao"
            data-testid="inp-devolucao-cadastro-obs"
            rows="3"
        />
        <BFormInvalidFeedback :state="estadoObservacaoDevolucao">
          A justificativa é obrigatória para a devolução.
        </BFormInvalidFeedback>
      </BFormGroup>
    </ModalConfirmacao>
  </LayoutPadrao>
</template>

<script lang="ts" setup>
import {BButton, BFormGroup, BFormInvalidFeedback, BFormTextarea} from "bootstrap-vue-next";
import {computed, onMounted, ref} from "vue";
import {useRouter} from "vue-router";
import {storeToRefs} from "pinia";
import LayoutPadrao from '@/components/layout/LayoutPadrao.vue';
import HistoricoAnaliseModal from "@/components/processo/HistoricoAnaliseModal.vue";
import ImpactoMapaModal from "@/components/mapa/ImpactoMapaModal.vue";
import ModalConfirmacao from "@/components/comum/ModalConfirmacao.vue";
import PageHeader from "@/components/layout/PageHeader.vue";
import VisAtividadeItem from "@/components/atividades/VisAtividadeItem.vue";
import {useAtividadesStore} from "@/stores/atividades";
import {useUnidadesStore} from "@/stores/unidades";
import {useProcessosStore} from "@/stores/processos";
import {useAnalisesStore} from "@/stores/analises";
import {useMapasStore} from "@/stores/mapas";
import {useSubprocessosStore} from "@/stores/subprocessos";
import type {
  AceitarCadastroRequest,
  DevolverCadastroRequest,
  HomologarCadastroRequest,
  Unidade,
  UnidadeParticipante,
} from "@/types/tipos";
import {TipoProcesso} from "@/types/tipos";
import {useAcesso} from "@/composables/useAcesso";

const props = defineProps<{
  codProcesso: number | string;
  sigla: string;
}>();

const router = useRouter();
const atividadesStore = useAtividadesStore();
const unidadesStore = useUnidadesStore();
const processosStore = useProcessosStore();
const analisesStore = useAnalisesStore();
const mapasStore = useMapasStore();
const subprocessosStore = useSubprocessosStore();
const {impactoMapa: impactos} = storeToRefs(mapasStore);

const unidadeId = computed(() => props.sigla);
const codProcesso = computed(() => Number(props.codProcesso));

const unidade = computed(() => {
  function buscarUnidade(unidades: Unidade[], sigla: string): Unidade | undefined {
    for (const u of unidades) {
      if (u.sigla === sigla) return u;
      if (u.filhas?.length) {
        const encontrada = buscarUnidade(u.filhas, sigla);
        if (encontrada) return encontrada;
      }
    }
  }

  return buscarUnidade(unidadesStore.unidades as Unidade[], unidadeId.value);
});

const siglaUnidade = computed(() => unidade.value?.sigla || unidadeId.value);
const nomeUnidade = computed(() => (unidade.value?.nome ? `${unidade.value.nome}` : ""));

const subprocesso = computed(() => {
  if (!processosStore.processoDetalhe) return null;

  function encontrarUnidade(unidades: UnidadeParticipante[]): UnidadeParticipante | undefined {
    for (const u of unidades) {
      if (u.sigla === unidadeId.value) return u;
      if (u.filhos && u.filhos.length > 0) {
        const encontrada = encontrarUnidade(u.filhos);
        if (encontrada) return encontrada;
      }
    }
    return undefined;
  }

  return encontrarUnidade(processosStore.processoDetalhe.unidades);
});

const {
  podeHomologarCadastro,
  podeAceitarCadastro,
  podeDevolverCadastro,
  podeVisualizarImpacto
} = useAcesso(computed(() => subprocessosStore.subprocessoDetalhe));

const podeVerImpacto = computed(() => podeVisualizarImpacto.value);
const isHomologacao = computed(() => podeHomologarCadastro.value);

const codSubprocesso = computed(() => subprocesso.value?.codSubprocesso);

const atividades = computed(() => {
  if (codSubprocesso.value === undefined) return [];
  return atividadesStore.obterAtividadesPorSubprocesso(codSubprocesso.value) || [];
});

const processoAtual = computed(() => processosStore.processoDetalhe);
const isRevisao = computed(() => processoAtual.value?.tipo === TipoProcesso.REVISAO);

const historicoAnalises = computed(() => {
  return analisesStore.analisesCadastro || [];
});

const loadingImpacto = ref(false);
const mostrarModalImpacto = ref(false);
const mostrarModalValidar = ref(false);
const mostrarModalDevolver = ref(false);
const mostrarModalHistoricoAnalise = ref(false);
const observacaoValidacao = ref("");
const observacaoDevolucao = ref("");
const validacaoDevolucaoSubmetida = ref(false);

const estadoObservacaoDevolucao = computed(() => {
  return validacaoDevolucaoSubmetida.value && !observacaoDevolucao.value.trim() ? false : null;
});

// Ações de validação/devolução
const loadingValidacao = ref(false);
const loadingDevolucao = ref(false);

async function abrirModalImpacto() {
  mostrarModalImpacto.value = true;
  if (codSubprocesso.value) {
    loadingImpacto.value = true;
    try {
      await mapasStore.buscarImpactoMapa(codSubprocesso.value);
    } finally {
      loadingImpacto.value = false;
    }
  }
}

function fecharModalImpacto() {
  mostrarModalImpacto.value = false;
}

async function abrirModalHistoricoAnalise() {
  if (codSubprocesso.value) {
    await analisesStore.carregarHistorico(codSubprocesso.value);
  }
  mostrarModalHistoricoAnalise.value = true;
}

function fecharModalHistoricoAnalise() {
  mostrarModalHistoricoAnalise.value = false;
}

function validarCadastro() {
  mostrarModalValidar.value = true;
}

function devolverCadastro() {
  mostrarModalDevolver.value = true;
}

function fecharModalValidar() {
  mostrarModalValidar.value = false;
  observacaoValidacao.value = "";
}

function fecharModalDevolver() {
  mostrarModalDevolver.value = false;
  observacaoDevolucao.value = "";
  validacaoDevolucaoSubmetida.value = false;
}

async function confirmarValidacao() {
  if (!codSubprocesso.value) return;

  loadingValidacao.value = true;
  try {
    let sucesso: boolean;

    if (isHomologacao.value) {
      const req: HomologarCadastroRequest = {
        observacoes: observacaoValidacao.value,
      };
      if (isRevisao.value) {
        sucesso = await subprocessosStore.homologarRevisaoCadastro(codSubprocesso.value, req);
      } else {
        sucesso = await subprocessosStore.homologarCadastro(codSubprocesso.value, req);
      }

      if (sucesso) {
        fecharModalValidar();
        await router.push({
          name: "Subprocesso",
          params: {
            codProcesso: props.codProcesso,
            siglaUnidade: props.sigla,
          },
        });
      }
    } else {
      const req: AceitarCadastroRequest = {
        observacoes: observacaoValidacao.value,
      };
      if (isRevisao.value) {
        sucesso = await subprocessosStore.aceitarRevisaoCadastro(codSubprocesso.value, req);
      } else {
        sucesso = await subprocessosStore.aceitarCadastro(codSubprocesso.value, req);
      }

      if (sucesso) {
        fecharModalValidar();
        await router.push({name: "Painel"});
      }
    }
  } finally {
    loadingValidacao.value = false;
  }
}

async function confirmarDevolucao() {
  validacaoDevolucaoSubmetida.value = true;
  if (!codSubprocesso.value || !observacaoDevolucao.value.trim()) return;

  loadingDevolucao.value = true;

  try {
    const req: DevolverCadastroRequest = {
      observacoes: observacaoDevolucao.value,
    };

    let sucesso: boolean;
    if (isRevisao.value) {
      sucesso = await subprocessosStore.devolverRevisaoCadastro(codSubprocesso.value, req);
    } else {
      sucesso = await subprocessosStore.devolverCadastro(codSubprocesso.value, req);
    }

    if (sucesso) {
      fecharModalDevolver();
      await router.push("/painel");
    }
  } finally {
    loadingDevolucao.value = false;
  }
}

onMounted(async () => {
  await processosStore.buscarProcessoDetalhe(codProcesso.value);
  if (codSubprocesso.value) {
    await Promise.all([
      atividadesStore.buscarAtividadesParaSubprocesso(codSubprocesso.value),
      subprocessosStore.buscarSubprocessoDetalhe(codSubprocesso.value)
    ]);
  }
});

defineExpose({
  mostrarModalHistoricoAnalise,
  mostrarModalValidar,
  mostrarModalDevolver
});
</script>

<style>
.unidade-nome {
  color: var(--bs-body-color);
  opacity: 0.85;
  padding-right: 1rem;
}

.unidade-cabecalho {
  font-size: 1.1rem;
  font-weight: 500;
  display: flex;
  gap: 0.5rem;
  align-items: center;
}

.unidade-sigla {
  background: var(--bs-light);
  color: var(--bs-dark);
  font-weight: bold;
  border-radius: 0.5rem;
  letter-spacing: 1px;
}
</style>
