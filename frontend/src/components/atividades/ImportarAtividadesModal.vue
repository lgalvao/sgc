<template>
  <ModalPadrao
      v-model="mostrarComputado"
      :loading="importando"
      data-testid="mdl-importacao-atividades"
      size="lg"
      test-codigo-cancelar="importar-atividades-modal__btn-modal-cancelar"
      test-codigo-confirmar="btn-importar"
      texto-acao="Importar"
      texto-acao-carregando="Importando..."
      titulo="Importação de atividades"
      variant-acao="success"
      @confirmar="importar"
      @fechar="fechar"
  >
    <BAlert
        v-if="erroImportacao"
        :fade="false"
        :model-value="true"
        dismissible
        variant="danger"
        @dismissed="limparErroImportacao"
    >
      {{ erroImportacao }}
    </BAlert>
    <fieldset :disabled="importando">
      <div class="mb-3">
        <label
            class="form-label"
            for="processo-select"
        >Processo <span aria-hidden="true" class="text-danger">*</span></label>
        <BFormSelect
            id="processo-select"
            v-model="processoSelecionadoId"
            :options="processosParaImportacao"
            :state="mensagemErroProcesso ? false : null"
            data-testid="select-processo"
            text-field="descricao"
            value-field="codigo"
        >
          <template #first>
            <BFormSelectOption
                :value="null"
                disabled
            >
              Selecione
            </BFormSelectOption>
          </template>
        </BFormSelect>
        <BFormInvalidFeedback :state="mensagemErroProcesso ? false : null">
          {{ mensagemErroProcesso }}
        </BFormInvalidFeedback>
        <div
            v-if="!processosParaImportacao.length"
            class="text-center text-muted mt-3"
        >
          {{ TEXTOS.atividades.importacao.NENHUM_PROCESSO }}
        </div>
      </div>

      <div class="mb-3">
        <label
            class="form-label"
            for="unidade-select"
        >Unidade <span aria-hidden="true" class="text-danger">*</span></label>
        <BFormSelect
            id="unidade-select"
            v-model="unidadeSelecionadaId"
            :disabled="!processoSelecionado"
            :options="unidadesParticipantes"
            :state="mensagemErroUnidade ? false : null"
            data-testid="select-unidade"
            text-field="sigla"
            value-field="codUnidade"
        >
          <template #first>
            <BFormSelectOption
                :value="null"
                disabled
            >
              Selecione
            </BFormSelectOption>
          </template>
        </BFormSelect>
        <BFormInvalidFeedback :state="mensagemErroUnidade ? false : null">
          {{ mensagemErroUnidade }}
        </BFormInvalidFeedback>
      </div>

      <div v-if="unidadeSelecionada">
        <h6>Atividades para importar</h6>
        <div
            id="atividades-container"
            :class="['atividades-container border rounded p-2', { 'border-danger is-invalid': mensagemErroAtividades }]"
            tabindex="-1"
        >
          <div
              v-if="atividadesParaImportar.length"
          >
            <div class="d-flex gap-2 mb-2">
              <BButton
                  aria-label="Selecionar todas as atividades"
                  data-testid="btn-selecionar-todas-atividades"
                  size="sm"
                  variant="outline-secondary"
                  @click="selecionarTodasAtividades"
              >
                <i
                    aria-hidden="true"
                    class="bi bi-check-all me-1"
                />
                {{ TEXTOS.atividades.importacao.BOTAO_SELECIONAR_TODAS }}
              </BButton>
              <BButton
                  aria-label="Desmarcar todas as atividades"
                  data-testid="btn-limpar-selecao-atividades"
                  size="sm"
                  variant="outline-secondary"
                  @click="limparSelecaoAtividades"
              >
                <i
                    aria-hidden="true"
                    class="bi bi-x-lg me-1"
                />
                {{ TEXTOS.atividades.importacao.BOTAO_LIMPAR_SELECAO }}
              </BButton>
            </div>
            <div
                v-for="ativ in atividadesParaImportar"
                :key="ativ.codigo"
                class="form-check"
            >
              <BFormCheckbox
                  :id="`ativ-check-${ativ.codigo}`"
                  v-model="atividadesSelecionadas"
                  :data-testid="`checkbox-atividade-${ativ.codigo}`"
                  :value="ativ"
              >
                {{ ativ.descricao }}
              </BFormCheckbox>
            </div>
          </div>
          <div
              v-else
              class="text-center text-muted mt-3"
          >
            {{ TEXTOS.atividades.importacao.NENHUMA_ATIVIDADE }}
          </div>
        </div>
        <div v-if="mensagemErroAtividades" class="text-danger small mt-1">
          {{ mensagemErroAtividades }}
        </div>
      </div>
    </fieldset>
  </ModalPadrao>
</template>

<script lang="ts" setup>
import {BAlert, BButton, BFormCheckbox, BFormInvalidFeedback, BFormSelect, BFormSelectOption,} from "bootstrap-vue-next";
import {computed, ref, watch} from "vue";
import ModalPadrao from "@/components/comum/ModalPadrao.vue";
import * as processoService from "@/services/processo";
import * as subprocessoService from "@/services/subprocessoService";
import {
  type Atividade,
  type AtividadeOperacaoResponse,
  type ProcessoResumo,
  type UnidadeImportacao,
} from "@/types/tipos";
import {TEXTOS} from "@/constants/textos";
import {useValidacaoFormulario} from "@/composables/useValidacaoFormulario";
import {useErrorHandler} from "@/composables/useErrorHandler";
import {logger} from "@/utils/logger";

const props = defineProps<{
  mostrar: boolean;
  codSubprocessoDestino: number | null | undefined;
}>();

const emit = defineEmits<{
  fechar: [];
  importar: [resultado: AtividadeOperacaoResponse];
}>();

const {
  validarSubmissao,
  resetarValidacao,
  deveExibirErro,
  focarPrimeiroErroInvalido
} = useValidacaoFormulario();
const {withErrorHandling} = useErrorHandler();

const resultadoImportacao = ref<AtividadeOperacaoResponse | null>(null);
const erroImportacao = ref<string | null>(null);
const importando = ref(false);
const processosParaImportacao = ref<ProcessoResumo[]>([]);

const processoSelecionado = ref<ProcessoResumo | null>(null);
const processoSelecionadoId = ref<number | null>(null);
const unidadesParticipantes = ref<UnidadeImportacao[]>([]);
const unidadeSelecionada = ref<UnidadeImportacao | null>(null);
const unidadeSelecionadaId = ref<number | null>(null);
const atividadesParaImportar = ref<Atividade[]>([]);
const atividadesSelecionadas = ref<Atividade[]>([]);

const mostrarComputado = computed({
  get: () => props.mostrar,
  set: (mostrar: boolean) => {
    if (!mostrar) emit("fechar");
  }
});

const mensagemErroProcesso = computed(() => {
  return deveExibirErro(!processoSelecionadoId.value) ? "Selecione o processo de origem." : "";
});

const mensagemErroUnidade = computed(() => {
  return deveExibirErro(!unidadeSelecionadaId.value) ? "Selecione a unidade de origem." : "";
});

const mensagemErroAtividades = computed(() => {
  return deveExibirErro(atividadesSelecionadas.value.length === 0) ? TEXTOS.atividades.importacao.SELECIONE_ATIVIDADE : "";
});

const isFormularioValido = computed(() => {
  return Boolean(
      processoSelecionadoId.value &&
      unidadeSelecionadaId.value &&
      atividadesSelecionadas.value.length > 0
  );
});

watch(
    () => props.mostrar,
    async (mostrar) => {
      if (mostrar) {
        resetModal();
        await carregarProcessosParaImportacao();
      }
    },
    {immediate: true},
);

watch(processoSelecionadoId, async (newId) => {
  if (newId) {
    const processo = processosParaImportacao.value.find(
        (p) => p.codigo === Number(newId),
    );
    if (processo) {
      await selecionarProcesso(processo);
    }
  } else {
    await selecionarProcesso(null);
  }
});

watch(unidadeSelecionadaId, (newId) => {
  if (newId) {
    const unidade = unidadesParticipantes.value.find(
        (u) => u.codUnidade === Number(newId),
    );
    if (unidade) {
      selecionarUnidade(unidade);
    }
  } else {
    selecionarUnidade(null);
  }
});

function resetModal() {
  processoSelecionado.value = null;
  processoSelecionadoId.value = null;
  unidadesParticipantes.value = [];
  unidadeSelecionada.value = null;
  unidadeSelecionadaId.value = null;
  atividadesParaImportar.value = [];
  atividadesSelecionadas.value = [];
  resetarValidacao();
}

function limparErroImportacao() {
  erroImportacao.value = null;
}

function registrarErroImportacao(mensagem: string) {
  erroImportacao.value = mensagem;
}

async function executarComErroImportacao<T>(
    acao: () => Promise<T>,
    aplicarResultado: (resultado: T) => void,
) {
  try {
    aplicarResultado(await withErrorHandling(acao, (erro) => {
      registrarErroImportacao(erro.mensagem);
    }));
  } catch (error) {
    logger.error("Erro ao carregar dados de importação de atividades", error);
  }
}

async function carregarProcessosParaImportacao() {
  processosParaImportacao.value = [];
  await executarComErroImportacao(
      () => processoService.buscarProcessosParaImportacao(),
      (processos) => {
        processosParaImportacao.value = processos;
      },
  );
}

function selecionarTodasAtividades() {
  atividadesSelecionadas.value = [...atividadesParaImportar.value];
}

function limparSelecaoAtividades() {
  atividadesSelecionadas.value = [];
}

async function selecionarProcesso(processo: ProcessoResumo | null) {
  processoSelecionado.value = processo;
  atividadesSelecionadas.value = [];
  limparErroImportacao();
  unidadesParticipantes.value = [];
  if (processo) {
    await executarComErroImportacao(
        () => processoService.buscarUnidadesParaImportacao(processo.codigo),
        (unidades) => {
          unidadesParticipantes.value = unidades;
        },
    );
  }
  unidadeSelecionada.value = null;
  unidadeSelecionadaId.value = null;
}

async function selecionarUnidade(unidadePu: UnidadeImportacao | null) {
  atividadesSelecionadas.value = [];
  unidadeSelecionada.value = unidadePu;
  limparErroImportacao();
  atividadesParaImportar.value = [];
  if (unidadePu) {
    await executarComErroImportacao(
        () => subprocessoService.listarAtividadesParaImportacao(unidadePu.codSubprocesso),
        (atividadesDaOutraUnidade) => {
          atividadesParaImportar.value = [...atividadesDaOutraUnidade];
        },
    );
  }
}

function fechar() {
  emit("fechar");
}

async function importar() {
  if (!validarSubmissao(isFormularioValido.value)) {
    await focarPrimeiroErroInvalido();
    return;
  }

  limparErroImportacao();
  if (!props.codSubprocessoDestino || !unidadeSelecionada.value) {
    return;
  }

  const idsAtividades = atividadesSelecionadas.value.map((a) => a.codigo);

  importando.value = true;
  try {
    resultadoImportacao.value = await withErrorHandling(
        () => subprocessoService.importarAtividades(
            props.codSubprocessoDestino!,
            unidadeSelecionada.value!.codSubprocesso,
            idsAtividades,
        ),
        (erro) => {
          registrarErroImportacao(erro.mensagem);
        },
    );
    emit("importar", resultadoImportacao.value);
    fechar();
  } catch (error) {
    resultadoImportacao.value = null;
    logger.error("Erro ao importar atividades", error);
  } finally {
    importando.value = false;
  }
}
</script>

<style scoped>
.atividades-container {
  max-height: 250px;
  overflow-y: auto;
}
</style>
