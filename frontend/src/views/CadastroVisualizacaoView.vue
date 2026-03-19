<template>
  <LayoutPadrao>
    <PageHeader
        :title="TEXTOS.atividades.TITULO"
        :subtitle="nomeUnidade"
    >
      <template #default>
        <span class="fw-bold" data-testid="subprocesso-header__txt-header-unidade">{{ siglaUnidade }}</span>
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
            variant="outline-secondary"
            @click="abrirModalHistoricoAnalise"
        >
          {{ TEXTOS.atividades.BOTAO_HISTORICO_ANALISE }}
        </BButton>
        <BButton
            v-if="podeDevolverCadastro"
            data-testid="btn-acao-devolver"
            :disabled="!habilitarDevolverCadastro"
            :title="TEXTOS.atividades.BOTAO_DEVOLVER"
            variant="secondary"
            @click="devolverCadastro"
        >
          {{ TEXTOS.atividades.BOTAO_DEVOLVER }}
        </BButton>
        <BButton
            v-if="podeAceitarCadastro || podeHomologarCadastro"
            data-testid="btn-acao-analisar-principal"
            :disabled="!habilitarAnalisarCadastro"
            :title="podeHomologarCadastro ? TEXTOS.atividades.BOTAO_HOMOLOGAR : TEXTOS.atividades.BOTAO_ACEITAR"
            variant="success"
            @click="validarCadastro"
        >
          {{ podeHomologarCadastro ? TEXTOS.atividades.BOTAO_HOMOLOGAR : TEXTOS.atividades.BOTAO_ACEITAR }}
        </BButton>
      </template>
    </PageHeader>

    <!-- Lista de atividades -->
    <BCard
        v-for="(atividade) in atividades"
        :key="atividade.codigo"
        class="mb-3 atividade-card"
        no-body
    >
      <BCardBody class="py-2">
        <BCardTitle class="d-flex align-items-center atividade-edicao-row position-relative group-atividade atividade-hover-row atividade-titulo-card">
          <strong class="atividade-descricao" data-testid="txt-atividade-descricao">{{ atividade.descricao }}</strong>
        </BCardTitle>
        <div class="mt-3 ms-3">
          <div
              v-for="(conhecimento) in atividade.conhecimentos"
              :key="conhecimento.codigo"
              class="d-flex align-items-center mb-2 group-conhecimento position-relative conhecimento-hover-row"
          >
            <span data-testid="txt-conhecimento-descricao">{{ conhecimento.descricao }}</span>
          </div>
        </div>
      </BCardBody>
    </BCard>

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
        :titulo="isHomologacao ? TEXTOS.atividades.MODAL_HOMOLOGAR_TITULO : (isRevisao ? TEXTOS.atividades.MODAL_ACEITE_REVISAO_TITULO : TEXTOS.atividades.MODAL_VALIDAR_TITULO)"
        :ok-title="TEXTOS.comum.BOTAO_CONFIRMAR"
        test-id-confirmar="btn-aceite-cadastro-confirmar"
        variant="success"
        @confirmar="confirmarValidacao"
    >
      <p>{{
          isHomologacao ? TEXTOS.atividades.MODAL_HOMOLOGAR_TEXTO : (isRevisao ? TEXTOS.atividades.MODAL_ACEITE_REVISAO_TEXTO : TEXTOS.atividades.MODAL_VALIDAR_TEXTO)
        }}</p>
      <BFormGroup class="mb-3" :label="TEXTOS.comum.OBSERVACAO" label-for="observacaoValidacao">
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
        :titulo="isRevisao ? TEXTOS.atividades.MODAL_DEVOLVER_REVISAO_TITULO : TEXTOS.atividades.MODAL_DEVOLVER_TITULO"
        :ok-title="TEXTOS.comum.BOTAO_CONFIRMAR"
        test-id-confirmar="btn-devolucao-cadastro-confirmar"
        variant="secondary"
        @confirmar="confirmarDevolucao"
    >
      <p>{{
          isRevisao ? TEXTOS.atividades.MODAL_DEVOLVER_REVISAO_TEXTO : TEXTOS.atividades.MODAL_DEVOLVER_TEXTO
        }}</p>
      <BFormGroup class="mb-3" label-for="observacaoDevolucao">
        <template #label>
          Observação <span aria-hidden="true" class="text-danger">*</span>
        </template>
        <BFormTextarea
            id="observacaoDevolucao"
            v-model="observacaoDevolucao"
            :state="estadoObservacaoDevolucao"
            data-testid="inp-devolucao-cadastro-obs"
            rows="3"
        />
        <BFormInvalidFeedback :state="estadoObservacaoDevolucao">
          {{ TEXTOS.atividades.ERRO_DEVOLUCAO_JUSTIFICATIVA }}
        </BFormInvalidFeedback>
      </BFormGroup>
    </ModalConfirmacao>
  </LayoutPadrao>
</template>

<script lang="ts" setup>
import {BButton, BCard, BCardBody, BCardTitle, BFormGroup, BFormInvalidFeedback, BFormTextarea} from "bootstrap-vue-next";
import {computed, onMounted, ref} from "vue";
import {useRouter} from "vue-router";
import {storeToRefs} from "pinia";
import LayoutPadrao from '@/components/layout/LayoutPadrao.vue';
import HistoricoAnaliseModal from "@/components/processo/HistoricoAnaliseModal.vue";
import ImpactoMapaModal from "@/components/mapa/ImpactoMapaModal.vue";
import ModalConfirmacao from "@/components/comum/ModalConfirmacao.vue";
import PageHeader from "@/components/layout/PageHeader.vue";
import {useProcessosStore} from "@/stores/processos";
import {useMapasStore} from "@/stores/mapas";
import {useSubprocessosStore} from "@/stores/subprocessos";
import {useToastStore} from "@/stores/toast";
import type {
  AceitarCadastroRequest,
  Analise,
  Atividade,
  DevolverCadastroRequest,
  HomologarCadastroRequest,
  Unidade,
  UnidadeParticipante
} from "@/types/tipos";
import {TipoProcesso} from "@/types/tipos";
import {useAcesso} from "@/composables/useAcesso";
import {listarAnalisesCadastro} from "@/services/analiseService";
import {TEXTOS} from "@/constants/textos";

const props = defineProps<{
  codProcesso: number | string;
  sigla: string;
}>();

const router = useRouter();
const processosStore = useProcessosStore();
const mapasStore = useMapasStore();
const subprocessosStore = useSubprocessosStore();
const toastStore = useToastStore();
const {impactoMapa: impactos} = storeToRefs(mapasStore);

const unidadeId = computed(() => props.sigla);
const codProcesso = computed(() => Number(props.codProcesso));

const unidade = ref<Unidade | null>(null);

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
  podeVisualizarImpacto,
  habilitarAceitarCadastro,
  habilitarDevolverCadastro,
  habilitarHomologarCadastro
} = useAcesso(computed(() => subprocessosStore.subprocessoDetalhe));

const podeVerImpacto = computed(() => podeVisualizarImpacto.value);
const isHomologacao = computed(() => podeHomologarCadastro.value);
const habilitarAnalisarCadastro = computed(() => habilitarAceitarCadastro.value || habilitarHomologarCadastro.value);

const codSubprocesso = computed(() => subprocesso.value?.codSubprocesso);

const atividades = ref<Atividade[]>([]);

const processoAtual = computed(() => processosStore.processoDetalhe);
const isRevisao = computed(() => processoAtual.value?.tipo === TipoProcesso.REVISAO);

const analisesCadastro = ref<Analise[]>([]);

const historicoAnalises = computed(() => {
  return analisesCadastro.value || [];
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
    analisesCadastro.value = await listarAnalisesCadastro(codSubprocesso.value);
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
        toastStore.setPending(TEXTOS.sucesso.HOMOLOGACAO_EFETIVADA);
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
        toastStore.setPending(TEXTOS.sucesso.ACEITE_REGISTRADO);
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
      toastStore.setPending(TEXTOS.sucesso.DEVOLUCAO_REALIZADA);
      await router.push("/painel");
    }
  } finally {
    loadingDevolucao.value = false;
  }
}

onMounted(async () => {
  await processosStore.buscarProcessoDetalhe(codProcesso.value);
  
  // Tenta obter o ID do subprocesso de forma robusta
  let id: number | null | undefined = codSubprocesso.value;
  if (!id && subprocesso.value) {
    id = await subprocessosStore.buscarSubprocessoPorProcessoEUnidade(
        codProcesso.value,
        subprocesso.value.sigla,
    );
  }

  if (id) {
    const data = await subprocessosStore.buscarContextoEdicao(id);
    if (data) {
      if (data.atividadesDisponiveis) {
        atividades.value = data.atividadesDisponiveis;
      }
      if (data.unidade) {
        unidade.value = data.unidade as Unidade;
      }
    }
  }
});

defineExpose({
  mostrarModalHistoricoAnalise,
  mostrarModalValidar,
  mostrarModalDevolver
});
</script>

<style scoped>
.atividade-card {
  transition: box-shadow 0.2s;
}

.atividade-card:hover {
  box-shadow: 0 2px 12px 0 rgba(0, 0, 0, 0.07);
}

.atividade-descricao {
  word-break: break-word;
  max-width: 100%;
  display: inline-block;
}

.atividade-titulo-card {
  background: var(--bs-light);
  border-bottom: 1px solid var(--bs-border-color);
  padding: 0.5rem 0.75rem;
  margin-left: -0.75rem;
  margin-right: -0.75rem;
  margin-top: -0.5rem;
  border-top-left-radius: 0.375rem;
  border-top-right-radius: 0.375rem;
}

.atividade-titulo-card .atividade-descricao {
  font-size: 1.1rem;
}
</style>
