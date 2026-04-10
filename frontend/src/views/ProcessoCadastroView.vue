  <template>
  <LayoutPadrao>
    <div class="col-lg-8 col-md-9 col-12">
      <PageHeader :title="TEXTOS.processo.cadastro.TITULO">
        <template #actions>
          <BButton
              :disabled="isLoading"
              data-testid="btn-processo-cancelar"
              to="/painel"
              variant="outline-secondary"
          >
            {{ TEXTOS.processo.cadastro.BOTAO_CANCELAR }}
          </BButton>

          <LoadingButton
              v-if="processoEditando"
              :disabled="isLoading"
              data-testid="btn-processo-remover"
              icon="trash"
              :text="TEXTOS.processo.cadastro.BOTAO_REMOVER"
              variant="outline-danger"
              @click="abrirModalRemocao"
          />

          <LoadingButton
              :disabled="isFormInvalid || isLoadingData"
              :loading="isLoading"
              data-testid="btn-processo-salvar"
              icon="save"
              loading-text="Salvando..."
              :text="TEXTOS.processo.cadastro.BOTAO_SALVAR"
              type="button"
              variant="outline-primary"
              @click="salvarProcesso"
          />

          <LoadingButton
              :disabled="isFormInvalid || isLoading || isLoadingData"
              data-testid="btn-processo-iniciar"
              icon="play-fill"
              :text="TEXTOS.processo.cadastro.BOTAO_INICIAR"
              variant="success"
              @click="abrirModalConfirmacao"
          />
        </template>
      </PageHeader>

      <div v-if="exibirAlertaDiagnostico" class="mb-3 pt-2">
        <BAlert
            :model-value="true"
            variant="warning"
        >
          <strong>Pendências organizacionais identificadas.</strong>
          <div class="mt-1">{{ resumoDiagnostico }}</div>
          <ul class="mb-0 mt-2 ps-3">
            <li v-for="grupo in gruposDiagnostico" :key="grupo.tipo">
              {{ grupo.tipo }}: {{ grupo.quantidadeOcorrencias }} ocorrência(s)
            </li>
          </ul>
        </BAlert>
      </div>

      <BForm class="mt-4">
        <AppAlert
            v-if="notificacao"
            :dismissible="notificacao.dismissible ?? true"
            :message="notificacao.message"
            :notification="notificacao.notification"
            :stack-trace="notificacao.stackTrace"
            :variant="notificacao.variant"
            @dismissed="clear()"
        />

        <ProcessoFormFields
            ref="formFieldsRef"
            v-model="formData"
            :field-errors="fieldErrors"
            :is-edit="!!processoEditando"
            :is-loading-unidades="isLoadingUnidades"
            :unidades="unidades"
        />
      </BForm>
    </div>

    <!-- Modal de confirmação CDU-05 -->
    <ModalConfirmacao
        v-model="mostrarModalConfirmacao"
        :auto-close="false"
        :loading="isLoading"
        :cancel-title="TEXTOS.comum.BOTAO_CANCELAR"
        :ok-title="TEXTOS.comum.BOTAO_INICIAR"
        variant="success"
        test-codigo-cancelar="btn-iniciar-processo-cancelar"
        test-codigo-confirmar="btn-iniciar-processo-confirmar"
        :titulo="TEXTOS.processo.cadastro.INICIAR_TITULO"
        @confirmar="confirmarIniciarProcesso"
    >
      <p><strong>Descrição:</strong> {{ descricao }}</p>
      <p><strong>Tipo:</strong> {{ tipo }}</p>
      <p><strong>Unidades selecionadas:</strong> {{ unidadesSelecionadas.length }}</p>
      <hr>
      <p>
        {{ TEXTOS.processo.cadastro.INICIAR_CONFIRMACAO }}
      </p>
    </ModalConfirmacao>

    <!-- Modal de confirmação de remoção -->
    <ModalConfirmacao
        v-model="mostrarModalRemocao"
        :auto-close="false"
        :loading="isLoading"
        :ok-title="TEXTOS.processo.cadastro.BOTAO_REMOVER"
        :titulo="TEXTOS.processo.cadastro.REMOVER_TITULO"
        variant="danger"
        @confirmar="confirmarRemocao"
    >
      <p>{{ TEXTOS.processo.cadastro.REMOVER_CONFIRMACAO(descricao) }}</p>
    </ModalConfirmacao>
  </LayoutPadrao>
</template>

<script lang="ts" setup>
import {BAlert, BButton, BForm} from "bootstrap-vue-next";
import LayoutPadrao from '@/components/layout/LayoutPadrao.vue';
import {computed, nextTick, onMounted, ref, watch} from "vue";
import {useRoute, useRouter} from "vue-router";
import ModalConfirmacao from "@/components/comum/ModalConfirmacao.vue";
import PageHeader from "@/components/layout/PageHeader.vue";
import LoadingButton from "@/components/comum/LoadingButton.vue";
import ProcessoFormFields from "@/components/processo/ProcessoFormFields.vue";
import AppAlert from "@/components/comum/AppAlert.vue";
import {logger} from "@/utils";
import {isAxiosError, normalizeError, shouldNotifyGlobally} from "@/utils/apiError";
import {useProcessoForm} from "@/composables/useProcessoForm";
import {useNotification} from "@/composables/useNotification";
import {TEXTOS} from "@/constants/textos";

import {useToastStore} from "@/stores/toast";
import {usePainelStore} from "@/stores/painel";
import {useOrganizacaoStore} from "@/stores/organizacao";
import {useUnidadeStore} from "@/stores/unidade";
import {
  mapUnidadesArray
} from "@/services/unidadeService";
import * as processoService from "@/services/processoService";
import {Processo as ProcessoModel, TipoProcesso, type Unidade} from "@/types/tipos";
import {usePerfil} from "@/composables/usePerfil";

const {
  descricao,
  tipo,
  dataLimite,
  unidadesSelecionadas,
  fieldErrors,
  isFormInvalid,
  setFromNormalizedError,
  clearErrors,
  hasErrors,
  construirCriarRequest,
  construirAtualizarRequest,
  limpar: limparCampos
} = useProcessoForm();

// Computed form data for the extracted component
const formData = computed({
  get: () => ({
    descricao: descricao.value,
    tipo: tipo.value,
    unidadesSelecionadas: unidadesSelecionadas.value,
    dataLimite: dataLimite.value
  }),
  set: (value) => {
    descricao.value = value.descricao;
    tipo.value = value.tipo;
    unidadesSelecionadas.value = value.unidadesSelecionadas;
    dataLimite.value = value.dataLimite;
  }
});

const formFieldsRef = ref<InstanceType<typeof ProcessoFormFields> | null>(null);

const isLoading = ref(false);
const router = useRouter();
const route = useRoute();
const toastStore = useToastStore();
const painelStore = usePainelStore();
const organizacaoStore = useOrganizacaoStore();
const unidadeStore = useUnidadeStore();
const {notificacao, notify, notifyStructured, clear} = useNotification();
const {mostrarDiagnosticoOrganizacional} = usePerfil();

const unidades = ref<Unidade[]>([]);
const isLoadingUnidades = ref(false);
const ultimaBuscaUnidades = ref<{ tipoProcesso: TipoProcesso; codProcesso?: number } | null>(null);
const diagnosticoOrganizacional = computed(() => organizacaoStore.diagnostico);
const erroDiagnosticoOrganizacional = computed(() => organizacaoStore.erroDiagnostico);

const gruposDiagnostico = computed(() => diagnosticoOrganizacional.value?.grupos ?? []);
const resumoDiagnostico = computed(() =>
    erroDiagnosticoOrganizacional.value
        ?? diagnosticoOrganizacional.value?.resumo
        ?? ""
);
const exibirAlertaDiagnostico = computed(() =>
    mostrarDiagnosticoOrganizacional.value
    && (!!erroDiagnosticoOrganizacional.value || diagnosticoOrganizacional.value?.possuiViolacoes === true)
);

function extrairErrosGenericos(error: ReturnType<typeof normalizeError>): string[] {
  return error.subErrors
      ?.filter(subError => !subError.field)
      .map(subError => subError.message ?? "")
      .filter(Boolean) ?? [];
}

function coletarCodigosElegiveis(unidadesArvore: Unidade[]): Set<number> {
  const codigosElegiveis = new Set<number>();

  const visitar = (unidade: Unidade) => {
    if (unidade.isElegivel === true) {
      codigosElegiveis.add(unidade.codigo);
    }
    (unidade.filhas ?? []).forEach(visitar);
  };

  unidadesArvore.forEach(visitar);
  return codigosElegiveis;
}

function sincronizarUnidadesSelecionadasElegiveis(unidadesArvore: Unidade[]) {
  const codigosElegiveis = coletarCodigosElegiveis(unidadesArvore);
  const selecionadasFiltradas = unidadesSelecionadas.value.filter(codigo =>
      codigosElegiveis.has(codigo),
  );

  if (selecionadasFiltradas.length !== unidadesSelecionadas.value.length) {
    unidadesSelecionadas.value = selecionadasFiltradas;
  }
}

async function buscarUnidadesParaProcesso(tipoProcesso: TipoProcesso, codProcesso?: number) {
  if (
      ultimaBuscaUnidades.value?.tipoProcesso === tipoProcesso
      && ultimaBuscaUnidades.value?.codProcesso === codProcesso
  ) {
    return;
  }
  ultimaBuscaUnidades.value = {tipoProcesso, codProcesso};
  isLoadingUnidades.value = true;
  try {
    const unidadesMapeadas = await unidadeStore.garantirArvoreElegibilidade(tipoProcesso, codProcesso);
    unidades.value = unidadesMapeadas;
    sincronizarUnidadesSelecionadasElegiveis(unidadesMapeadas);
  } catch (error) {
    logger.error("Erro ao buscar unidades:", error);
  } finally {
    isLoadingUnidades.value = false;
  }
}

async function carregarDiagnosticoOrganizacional() {
  await organizacaoStore.garantirDiagnostico(mostrarDiagnosticoOrganizacional.value);
}

const mostrarModalConfirmacao = ref(false);
const mostrarModalRemocao = ref(false);
const processoEditando = ref<ProcessoModel | null>(null);

const isLoadingData = ref(false);

onMounted(async () => {
  void carregarDiagnosticoOrganizacional();

  const codProcesso = route.query.codProcesso;
  if (codProcesso) {
    await carregarProcessoParaEdicao(Number(codProcesso));
  } else if (tipo.value) {
    await buscarUnidadesParaProcesso(tipo.value);
  }

  // Auto-focus na descrição ao carregar
  if (!processoEditando.value) {
    await nextTick();
    formFieldsRef.value?.inputDescricaoRef?.$el?.focus();
  }
});

async function carregarProcessoParaEdicao(codProcesso: number) {
  isLoadingData.value = true;
  try {
    const processo = await processoService.obterDetalhesProcesso(codProcesso);
    if (!processo) {
      return;
    }
    if (processo.situacao !== 'CRIADO') {
      await router.push(`/processo/${processo.codigo}`);
      return;
    }

    processoEditando.value = processo;
    descricao.value = processo.descricao;
    tipo.value = processo.tipo;
    dataLimite.value = processo.dataLimite.split("T")[0];
    unidadesSelecionadas.value = processo.unidades.map((unidade) => unidade.codUnidade);
    await buscarUnidadesParaProcesso(processo.tipo, processo.codigo);
    await nextTick();
  } catch (error) {
    notify(TEXTOS.processo.cadastro.ERRO_CARREGAR_DETALHES, 'danger');
    logger.error("Erro ao carregar processo:", error);
  } finally {
    isLoadingData.value = false;
  }
}

watch(tipo, async (novoTipo) => {
  const codProcesso = processoEditando.value
      ? processoEditando.value.codigo
      : undefined;
  if (novoTipo) {
    await buscarUnidadesParaProcesso(novoTipo, codProcesso);
  }
});

function handleApiErrors(error: unknown, title: string, defaultMsg: string) {
  clearErrors();
  clear();

  const erroNormalizado = normalizeError(error);
  const usarErroEstruturado = isAxiosError(error) || (erroNormalizado.subErrors?.length ?? 0) > 0;

  if (usarErroEstruturado) {
    setFromNormalizedError(erroNormalizado);
    if (fieldErrors.value.dataLimiteEtapa1) fieldErrors.value.dataLimite = fieldErrors.value.dataLimiteEtapa1;

    const hasFieldErrors = hasErrors();
    const genericErrors = extrairErrosGenericos(erroNormalizado);

    if (!hasFieldErrors || genericErrors.length > 0) {
      notifyStructured(
          erroNormalizado.message || defaultMsg,
          genericErrors,
          {
            variant: 'danger',
            stackTrace: erroNormalizado.stackTrace || undefined,
          }
      );
      window.scrollTo(0, 0);
    } else if (hasFieldErrors) {
      nextTick(() => {
        const firstInvalid = document.querySelector('.is-invalid') as HTMLElement;
        if (firstInvalid) {
          firstInvalid.focus();
        }
      });
    }
  } else {
    notify(defaultMsg, 'danger');
  }

  if (shouldNotifyGlobally(erroNormalizado)) {
    logger.error(title + ":", error);
  }
}

async function salvarProcesso() {
  clearErrors();
  isLoading.value = true;

  // Validações agora são feitas no backend via Bean validation
  try {
    if (processoEditando.value) {
      const request = construirAtualizarRequest(processoEditando.value.codigo);
      await processoService.atualizarProcesso(
          processoEditando.value.codigo,
          request,
      );
      toastStore.setPending(TEXTOS.sucesso.PROCESSO_ALTERADO);
      painelStore.invalidar();
      await router.push("/painel");
    } else {
      const request = construirCriarRequest();
      await processoService.criarProcesso(request);
      toastStore.setPending(TEXTOS.sucesso.PROCESSO_CRIADO);
      painelStore.invalidar();
      await router.push("/painel");
    }
    limparCampos();
  } catch (error) {
    handleApiErrors(error, "Erro ao salvar processo", "Não foi possível salvar o processo. Verifique os dados e tente novamente.");
  } finally {
    isLoading.value = false;
  }
}

function abrirModalConfirmacao() {
  mostrarModalConfirmacao.value = true;
}

async function confirmarIniciarProcesso() {
  clearErrors();
  isLoading.value = true;

  let codigoProcesso = processoEditando.value?.codigo;

  if (!codigoProcesso) {
    try {
      const request = construirCriarRequest();
      const novoProcesso = await processoService.criarProcesso(request);
      codigoProcesso = novoProcesso.codigo;
    } catch (error) {
      mostrarModalConfirmacao.value = false;
      handleApiErrors(error, "Erro ao criar processo", TEXTOS.processo.cadastro.ERRO_CRIAR_PARA_INICIAR);
      isLoading.value = false;
      return;
    }
  }

  try {
    if (!tipo.value) {
      notify(TEXTOS.processo.cadastro.ERRO_INICIAR_PROCESSO, "danger");
      mostrarModalConfirmacao.value = false;
      isLoading.value = false;
      return;
    }

    await processoService.iniciarProcesso(
        codigoProcesso,
        tipo.value,
        unidadesSelecionadas.value,
    );

    toastStore.setPending(TEXTOS.sucesso.PROCESSO_INICIADO);
    painelStore.invalidar();
    await router.push("/painel");
    mostrarModalConfirmacao.value = false;
  } catch (error) {
    mostrarModalConfirmacao.value = false;
    handleApiErrors(error, "Erro ao iniciar processo", TEXTOS.processo.cadastro.ERRO_INICIAR_PROCESSO);
  } finally {
    isLoading.value = false;
  }
}

function abrirModalRemocao() {
  mostrarModalRemocao.value = true;
}

function fecharModalRemocao() {
  mostrarModalRemocao.value = false;
}

async function confirmarRemocao() {
  if (processoEditando.value) {
    isLoading.value = true;
    const descRemovida = processoEditando.value.descricao;
    try {
      await processoService.excluirProcesso(processoEditando.value.codigo);
      toastStore.setPending(TEXTOS.sucesso.PROCESSO_REMOVIDO(descRemovida));
      painelStore.invalidar();
      await router.push("/painel");
      if (!processoEditando.value) {
        limparCampos();
      }
      fecharModalRemocao();
    } catch (error) {
      fecharModalRemocao();
      handleApiErrors(error, "Erro ao remover processo", TEXTOS.processo.cadastro.ERRO_REMOVER_PROCESSO);
    } finally {
      isLoading.value = false;
    }
  } else {
    fecharModalRemocao();
  }
}
</script>
