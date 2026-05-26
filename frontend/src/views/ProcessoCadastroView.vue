<template>
  <LayoutPadrao>
    <div class="col-lg-8 col-md-9 col-12">
      <PageHeader :title="TEXTOS.processo.cadastro.TITULO"/>

      <ProcessoDiagnosticoAlert
          :carregando="carregandoDiagnosticoOrganizacional"
          :exibir="exibirAlertaDiagnostico"
          :grupos="gruposDiagnostico"
          :resumo="resumoDiagnostico"
          :unidades-sem-responsavel="unidadesSemResponsavel"
          @dismiss="dispensarAlertaDiagnostico"
      />

      <BForm class="mt-4" @submit.prevent>
        <AppAlert
            v-if="notificacao"
            :dispensavel="notificacao.dispensavel"
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
              :disabled="anyLoading"
              data-testid="btn-processo-cancelar-rodape"
              to="/painel"
              variant="outline-secondary"
          >
            {{ TEXTOS.processo.cadastro.BOTAO_CANCELAR }}
          </BButton>

          <LoadingButton
              v-if="processoEditando"
              :disabled="anyLoading"
              :text="TEXTOS.processo.cadastro.BOTAO_REMOVER"
              data-testid="btn-processo-remover-rodape"
              icon="trash"
              variant="outline-danger"
              @click="mostrarModalRemocao = true"
          />

          <LoadingButton
              :disabled="salvarDesabilitado"
              :loading="isSaving"
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
              :loading="isStarting"
              :text="TEXTOS.processo.cadastro.BOTAO_INICIAR"
              data-testid="btn-processo-iniciar-rodape"
              icon="play-fill"
              variant="danger"
              @click="mostrarModalConfirmacao = true"
          />
        </div>
      </BForm>
    </div>

    <ProcessoCadastroModais
        v-model:mostrar-confirmacao="mostrarModalConfirmacao"
        v-model:mostrar-remocao="mostrarModalRemocao"
        :descricao="descricao"
        :is-loading-confirmacao="isStarting"
        :is-loading-remocao="isRemoving"
        :tipo-label="tipo || '-'"
        :total-unidades="unidadesSelecionadas.length"
        @confirmar-iniciar="confirmarIniciarProcesso"
        @confirmar-remocao="confirmarRemocao"
    />

    <ModalAcaoBloco
        id="modal-unidades-com-equipe-propria"
        ref="modalUnidadesComEquipePropriaRef"
        :mostrar-situacao="false"
        :permitir-vazio="true"
        :rotulo-botao="TEXTOS.comum.BOTAO_INICIAR"
        :texto="textoModalUnidadesComEquipePropria"
        :titulo="tituloModalUnidadesComEquipePropria"
        :unidades="unidadesComEquipePropriaSelecionadas"
        :unidades-pre-selecionadas="idsUnidadesComEquipePropriaSelecionadas"
        @confirmar="confirmarSelecaoUnidadesComEquipePropria"
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
import ModalAcaoBloco from "@/components/processo/ModalAcaoBloco.vue";
import AppAlert from "@/components/comum/AppAlert.vue";
import {isErroCanceladoHttp} from "@/axios-setup";
import {logger} from "@/utils";
import {deveNotificarGlobalmente, ehErroAxios, extrairErrosGenericos, normalizarErro} from "@/utils/apiError";
import {useDiagnosticoOrganizacionalAlert} from "@/composables/useDiagnosticoOrganizacionalAlert";
import {useProcessoForm} from "@/composables/useProcessoForm";
import {useNotification} from "@/composables/useNotification";
import {TEXTOS} from "@/constants/textos";
import {TEXTOS_SUCESSO_PROCESSO} from "@/constants/textos-processo";
import {useValidacaoFormulario} from "@/composables/useValidacaoFormulario";

import {useToastStore} from "@/stores/toast";
import {useUnidadeStore} from "@/stores/unidade";
import {useInvalidacaoNavegacao} from "@/composables/useInvalidacaoNavegacao";
import * as processoService from "@/services/processo";
import {Processo, TipoProcesso, type Unidade} from "@/types/tipos";
import {usePerfil} from "@/composables/usePerfil";
import {
  aplicarSelecaoDiretaUnidadesComEquipePropria,
  filtrarSelecionadasPorElegibilidade,
  listarUnidadesComEquipePropriaSelecionadas,
  removerUnidadesSemEquipe
} from "@/views/processoCadastroUnidades";

type ModalAcaoBlocoRef = {
  abrir: () => void;
  fechar: () => void;
  setProcessando: (valor: boolean) => void;
  setErro: (mensagem: string | null) => void;
};

const {
  descricao,
  tipo,
  dataLimite,
  unidadesSelecionadas,
  fieldErrors,
  isFormInvalid,
  aplicarErroNormalizado,
  limparErros,
  temErros,
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
const modalUnidadesComEquipePropriaRef = ref<ModalAcaoBlocoRef | null>(null);

const isSaving = ref(false);
const isStarting = ref(false);
const isRemoving = ref(false);
const router = useRouter();
const route = useRoute();
const toastStore = useToastStore();
const unidadeStore = useUnidadeStore();
const {invalidarCachesProcesso} = useInvalidacaoNavegacao();
const {notificacao, notify, notifyStructured, clear} = useNotification();
const {mostrarDiagnosticoOrganizacional} = usePerfil();
const {focarPrimeiroErroInvalido} = useValidacaoFormulario();

const unidades = ref<Unidade[]>([]);
const isLoadingUnidades = ref(false);
const {
  carregandoDiagnosticoOrganizacional,
  gruposDiagnostico,
  resumoDiagnostico,
  unidadesSemResponsavel,
  exibirAlertaDiagnostico,
  dispensarAlertaDiagnostico
} = useDiagnosticoOrganizacionalAlert(unidades, mostrarDiagnosticoOrganizacional);
const anyLoading = computed(() => isSaving.value || isStarting.value || isRemoving.value);
const salvarDesabilitado = computed(() => isFormInvalid.value || isLoadingData.value || anyLoading.value);
const iniciarDesabilitado = computed(() => isFormInvalid.value || isLoadingData.value || anyLoading.value);
const tituloModalUnidadesComEquipePropria = "Selecionar unidades participantes";
const textoModalUnidadesComEquipePropria =
    "A seleção inclui unidades com equipe própria e unidades subordinadas. Indique quais também devem participar deste processo.";

const unidadesComEquipePropriaSelecionadas = computed(() =>
    listarUnidadesComEquipePropriaSelecionadas(unidades.value, unidadesSelecionadas.value)
);

const idsUnidadesComEquipePropriaSelecionadas = computed(() =>
    unidadesComEquipePropriaSelecionadas.value.map((unidade) => unidade.codigo)
);

function sincronizarUnidadesSelecionadasElegiveis(unidadesArvore: Unidade[]) {
  const selecionadasFiltradas = filtrarSelecionadasPorElegibilidade(
      unidadesSelecionadas.value,
      unidadesArvore,
  );

  if (selecionadasFiltradas.length !== unidadesSelecionadas.value.length) {
    unidadesSelecionadas.value = selecionadasFiltradas;
  }
}

async function buscarUnidadesParaProcesso(tipoProcesso: TipoProcesso, codProcesso?: number) {
  isLoadingUnidades.value = true;
  try {
    const unidadesMapeadas = await unidadeStore.garantirArvoreElegibilidade(tipoProcesso, codProcesso);
    const unidadesSemSemEquipe = removerUnidadesSemEquipe(unidadesMapeadas);
    unidades.value = unidadesSemSemEquipe;
    sincronizarUnidadesSelecionadasElegiveis(unidadesSemSemEquipe);
  } catch (error) {
    logger.error("Erro ao buscar unidades:", error);
    notify(TEXTOS.processo.cadastro.ERRO_CARREGAR_UNIDADES, 'danger');
  } finally {
    isLoadingUnidades.value = false;
  }
}

const mostrarModalConfirmacao = ref(false);
const mostrarModalRemocao = ref(false);
const processoEditando = ref<Processo | null>(null);
const isLoadingData = ref(false);
let inicializando = false;

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
  inicializando = true;
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
    if (isErroCanceladoHttp(error)) {
      return;
    }
    notify(TEXTOS.processo.cadastro.ERRO_CARREGAR_DETALHES, 'danger');
    logger.error("Erro ao carregar processo:", error);
  } finally {
    isLoadingData.value = false;
    inicializando = false;
  }
}

watch(tipo, async (novoTipo) => {
  if (inicializando) return;
  const codProcesso = processoEditando.value ? processoEditando.value.codigo : undefined;
  if (novoTipo) {
    await buscarUnidadesParaProcesso(novoTipo, codProcesso);
  }
});

function handleApiErrors(error: unknown, title: string, defaultMsg: string) {
  limparErros();
  clear();

  const erroNormalizado = normalizarErro(error);
  const usarErroEstruturado = ehErroAxios(error) || !!erroNormalizado.erros?.length;

  if (usarErroEstruturado) {
    aplicarErroNormalizado(erroNormalizado);
    const genericErrors = extrairErrosGenericos(erroNormalizado);

    if (!temErros() || genericErrors.length > 0) {
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
  limparErros();
  isSaving.value = true;

  try {
    if (processoEditando.value) {
      const request = construirAtualizarRequest(processoEditando.value.codigo);
      await processoService.atualizarProcesso(processoEditando.value.codigo, request);
      toastStore.setPending(TEXTOS_SUCESSO_PROCESSO.PROCESSO_ALTERADO);
    } else {
      const request = construirCriarRequest();
      await processoService.criarProcesso(request);
      toastStore.setPending(TEXTOS_SUCESSO_PROCESSO.PROCESSO_CRIADO);
    }
    invalidarCachesProcesso();
    await router.push("/painel");
    limparCampos();
  } catch (error) {
    handleApiErrors(error, "Erro ao salvar processo", "Não foi possível salvar o processo.");
  } finally {
    isSaving.value = false;
  }
}

async function garantirCodigoProcessoParaInicio(): Promise<number> {
  if (processoEditando.value?.codigo) {
    return processoEditando.value.codigo;
  }
  const request = construirCriarRequest();
  const novoProcesso = await processoService.criarProcesso(request);
  return novoProcesso.codigo;
}

async function confirmarIniciarProcesso() {
  if (unidadesComEquipePropriaSelecionadas.value.length > 0) {
    mostrarModalConfirmacao.value = false;
    modalUnidadesComEquipePropriaRef.value?.abrir();
    return;
  }

  await iniciarProcessoComSelecaoDireta(unidadesSelecionadas.value);
}

async function confirmarSelecaoUnidadesComEquipePropria(dados: { ids: number[] }) {
  const codigosDiretos = aplicarSelecaoDiretaUnidadesComEquipePropria(
      unidadesSelecionadas.value,
      idsUnidadesComEquipePropriaSelecionadas.value,
      dados.ids,
  );
  await iniciarProcessoComSelecaoDireta(codigosDiretos);
}

async function iniciarProcessoComSelecaoDireta(codigosDiretos: number[]) {
  limparErros();
  isStarting.value = true;
  modalUnidadesComEquipePropriaRef.value?.setErro(null);
  modalUnidadesComEquipePropriaRef.value?.setProcessando(true);

  try {
    let codigoProcesso: number;
    try {
      codigoProcesso = await garantirCodigoProcessoParaInicio();
    } catch (error) {
      mostrarModalConfirmacao.value = false;
      modalUnidadesComEquipePropriaRef.value?.setProcessando(false);
      handleApiErrors(error, "Erro ao criar processo", TEXTOS.processo.cadastro.ERRO_CRIAR_PARA_INICIAR);
      return;
    }

    if (!tipo.value) throw new Error("Tipo não definido");
    await processoService.iniciarProcesso(codigoProcesso, tipo.value, codigosDiretos);
    
    toastStore.setPending(TEXTOS_SUCESSO_PROCESSO.PROCESSO_INICIADO);
    invalidarCachesProcesso();
    await router.push("/painel");
    
    mostrarModalConfirmacao.value = false;
    modalUnidadesComEquipePropriaRef.value?.fechar();
  } catch (error) {
    mostrarModalConfirmacao.value = false;
    modalUnidadesComEquipePropriaRef.value?.fechar();
    handleApiErrors(error, "Erro ao iniciar processo", TEXTOS.processo.cadastro.ERRO_INICIAR_PROCESSO);
  } finally {
    modalUnidadesComEquipePropriaRef.value?.setProcessando(false);
    isStarting.value = false;
  }
}

async function confirmarRemocao() {
  if (!processoEditando.value) {
    mostrarModalRemocao.value = false;
    return;
  }
  isRemoving.value = true;
  const descRemovida = processoEditando.value.descricao;
  try {
    await processoService.excluirProcesso(processoEditando.value.codigo);
    toastStore.setPending(TEXTOS_SUCESSO_PROCESSO.PROCESSO_REMOVIDO(descRemovida));
    invalidarCachesProcesso();
    await router.push("/painel");
    limparCampos();
    mostrarModalRemocao.value = false;
  } catch (error) {
    mostrarModalRemocao.value = false;
    handleApiErrors(error, "Erro ao remover processo", TEXTOS.processo.cadastro.ERRO_REMOVER_PROCESSO);
  } finally {
    isRemoving.value = false;
  }
}
</script>
