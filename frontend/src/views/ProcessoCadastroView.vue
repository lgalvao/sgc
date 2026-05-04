<template>
  <LayoutPadrao>
    <div class="col-lg-8 col-md-9 col-12">
      <PageHeader :title="TEXTOS.processo.cadastro.TITULO"/>

      <ProcessoDiagnosticoAlert
          :exibir="exibirAlertaDiagnostico"
          :grupos="gruposDiagnostico"
          :resumo="resumoDiagnostico"
          @dismiss="dispensarAlertaDiagnostico"
      />

      <BForm class="mt-4" @submit.prevent>
        <AppAlert
            v-if="notificacao"
            :dispensavel="notificacao.dispensavel ?? true"
            :mensagem="notificacao.mensagem"
            :notification="notificacao.notificacao"
            :stack-trace="notificacao.stackTrace"
            :variante="notificacao.variante"
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

        <div class="d-flex flex-wrap justify-content-end gap-2 mt-4 pt-3 border-top">
          <BButton
              :disabled="isLoading"
              data-testid="btn-processo-cancelar-rodape"
              to="/painel"
              variant="outline-secondary"
          >
            {{ TEXTOS.processo.cadastro.BOTAO_CANCELAR }}
          </BButton>

          <LoadingButton
              v-if="processoEditando"
              :disabled="isLoading"
              :text="TEXTOS.processo.cadastro.BOTAO_REMOVER"
              data-testid="btn-processo-remover-rodape"
              icon="trash"
              variant="outline-danger"
              @click="abrirModalRemocao"
          />

          <LoadingButton
              :disabled="salvarDesabilitado"
              :loading="isLoading"
              :text="TEXTOS.processo.cadastro.BOTAO_SALVAR"
              data-testid="btn-processo-salvar-rodape"
              icon="save"
              loading-text="Salvando..."
              type="button"
              variant="outline-primary"
              @click="salvarProcesso"
          />

          <LoadingButton
              :disabled="iniciarDesabilitado"
              :text="TEXTOS.processo.cadastro.BOTAO_INICIAR"
              data-testid="btn-processo-iniciar-rodape"
              icon="play-fill"
              variant="success"
              @click="abrirModalConfirmacao"
          />
        </div>
      </BForm>
    </div>

    <ProcessoCadastroModais
        v-model:mostrar-confirmacao="mostrarModalConfirmacao"
        v-model:mostrar-remocao="mostrarModalRemocao"
        :descricao="descricao"
        :is-loading="isLoading"
        :tipo-label="tipo || '-'"
        :total-unidades="unidadesSelecionadas.length"
        @confirmar-iniciar="confirmarIniciarProcesso"
        @confirmar-remocao="confirmarRemocao"
    />
  </LayoutPadrao>
</template>

<script lang="ts" setup>
import {BButton, BForm} from "bootstrap-vue-next";
import LayoutPadrao from '@/components/layout/LayoutPadrao.vue';
import {computed, nextTick, onMounted, ref, watch} from "vue";
import {useRoute, useRouter} from "vue-router";
import PageHeader from "@/components/layout/PageHeader.vue";
import LoadingButton from "@/components/comum/LoadingButton.vue";
import ProcessoFormFields from "@/components/processo/ProcessoFormFields.vue";
import ProcessoDiagnosticoAlert from "@/components/processo/ProcessoDiagnosticoAlert.vue";
import ProcessoCadastroModais from "@/components/processo/ProcessoCadastroModais.vue";
import AppAlert from "@/components/comum/AppAlert.vue";
import {logger} from "@/utils";
import {deveNotificarGlobalmente, ehErroAxios, extrairErrosGenericos, normalizarErro} from "@/utils/apiError";
import {useProcessoForm} from "@/composables/useProcessoForm";
import {useNotification} from "@/composables/useNotification";
import {TEXTOS} from "@/constants/textos";
import {useValidacaoFormulario} from "@/composables/useValidacaoFormulario";

import {useToastStore} from "@/stores/toast";
import {useOrganizacaoStore} from "@/stores/organizacao";
import {useUnidadeStore} from "@/stores/unidade";
import {useInvalidacaoNavegacao} from "@/composables/useInvalidacaoNavegacao";
import * as processoService from "@/services/processo";
import {Processo, TipoProcesso, type Unidade} from "@/types/tipos";
import {usePerfil} from "@/composables/usePerfil";

const {
  descricao,
  tipo,
  dataLimite,
  unidadesSelecionadas,
  fieldErrors,
  isFormInvalid,
  setFromErroNormalizado,
  clearErrors,
  hasErrors,
  construirCriarRequest,
  construirAtualizarRequest,
  limpar: limparCampos
} = useProcessoForm();

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
const organizacaoStore = useOrganizacaoStore();
const unidadeStore = useUnidadeStore();
const {invalidarCachesProcesso} = useInvalidacaoNavegacao();
const {notificacao, notify, notifyStructured, clear} = useNotification();
const {mostrarDiagnosticoOrganizacional} = usePerfil();
const {focarPrimeiroErroInvalido} = useValidacaoFormulario();

const unidades = ref<Unidade[]>([]);
const isLoadingUnidades = ref(false);
const diagnosticoOrganizacional = computed(() => organizacaoStore.diagnostico);
const erroDiagnosticoOrganizacional = computed(() => organizacaoStore.erroDiagnostico);

const gruposDiagnostico = computed(() => diagnosticoOrganizacional.value?.grupos ?? []);
const resumoDiagnostico = computed(() =>
    erroDiagnosticoOrganizacional.value
    ?? diagnosticoOrganizacional.value?.resumo
    ?? ""
);
const alertaDiagnosticoDispensado = ref(false);
const exibirAlertaDiagnostico = computed(() =>
    mostrarDiagnosticoOrganizacional.value
    && (!!erroDiagnosticoOrganizacional.value || diagnosticoOrganizacional.value?.possuiViolacoes === true)
    && !alertaDiagnosticoDispensado.value
);
const salvarDesabilitado = computed(() => isFormInvalid.value || isLoadingData.value);
const iniciarDesabilitado = computed(() => isFormInvalid.value || isLoading.value || isLoadingData.value);

function dispensarAlertaDiagnostico() {
  alertaDiagnosticoDispensado.value = true;
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

const mostrarModalConfirmacao = ref(false);
const mostrarModalRemocao = ref(false);
const processoEditando = ref<Processo | null>(null);
const isLoadingData = ref(false);

onMounted(async () => {
  const codProcesso = route.query.codProcesso;
  if (codProcesso) {
    await carregarProcessoParaEdicao(Number(codProcesso));
  } else if (tipo.value) {
    await buscarUnidadesParaProcesso(tipo.value);
  }

  if (!processoEditando.value) {
    await nextTick();
    formFieldsRef.value?.focarDescricao();
  }
});

async function carregarProcessoParaEdicao(codProcesso: number) {
  isLoadingData.value = true;
  try {
    const processo = await processoService.obterDetalhesProcesso(codProcesso);
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
  const codProcesso = processoEditando.value ? processoEditando.value.codigo : undefined;
  if (novoTipo) {
    await buscarUnidadesParaProcesso(novoTipo, codProcesso);
  }
});

function handleApiErrors(error: unknown, title: string, defaultMsg: string) {
  clearErrors();
  clear();

  const erroNormalizado = normalizarErro(error);
  const usarErroEstruturado = ehErroAxios(error) || (erroNormalizado.erros?.length ?? 0) > 0;

  if (usarErroEstruturado) {
    setFromErroNormalizado(erroNormalizado);
    const genericErrors = extrairErrosGenericos(erroNormalizado);

    if (!hasErrors() || genericErrors.length > 0) {
      notifyStructured(erroNormalizado.mensagem || defaultMsg, genericErrors, {
        variante: 'danger',
        stackTrace: erroNormalizado.stackTrace || undefined,
      });
      globalThis.scrollTo(0, 0);
    } else {
      void focarPrimeiroErroInvalido();
    }
  } else {
    notify(defaultMsg, 'danger');
  }

  if (deveNotificarGlobalmente(erroNormalizado)) {
    logger.error(title + ":", error);
  }
}

async function salvarProcesso() {
  clearErrors();
  isLoading.value = true;

  try {
    if (processoEditando.value) {
      const request = construirAtualizarRequest(processoEditando.value.codigo);
      await processoService.atualizarProcesso(processoEditando.value.codigo, request);
      toastStore.setPending(TEXTOS.sucesso.PROCESSO_ALTERADO);
    } else {
      const request = construirCriarRequest();
      await processoService.criarProcesso(request);
      toastStore.setPending(TEXTOS.sucesso.PROCESSO_CRIADO);
    }
    invalidarCachesProcesso();
    await router.push("/painel");
    limparCampos();
  } catch (error) {
    handleApiErrors(error, "Erro ao salvar processo", "Não foi possível salvar o processo.");
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
    if (!tipo.value) throw new Error("Tipo não definido");
    await processoService.iniciarProcesso(codigoProcesso, tipo.value, unidadesSelecionadas.value);
    toastStore.setPending(TEXTOS.sucesso.PROCESSO_INICIADO);
    invalidarCachesProcesso();
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
  if (!processoEditando.value) {
    fecharModalRemocao();
    return;
  }
  isLoading.value = true;
  const descRemovida = processoEditando.value.descricao;
  try {
    await processoService.excluirProcesso(processoEditando.value.codigo);
    toastStore.setPending(TEXTOS.sucesso.PROCESSO_REMOVIDO(descRemovida));
    invalidarCachesProcesso();
    await router.push("/painel");
    limparCampos();
    fecharModalRemocao();
  } catch (error) {
    fecharModalRemocao();
    handleApiErrors(error, "Erro ao remover processo", TEXTOS.processo.cadastro.ERRO_REMOVER_PROCESSO);
  } finally {
    isLoading.value = false;
  }
}
</script>
