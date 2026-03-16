<template>
  <LayoutPadrao>
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

    <BForm class="mt-4 col-md-6 col-sm-8 col-12">
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

    <!-- Modal de confirmação CDU-05 -->
    <ModalConfirmacao
        v-model="mostrarModalConfirmacao"
        :auto-close="false"
        :loading="isLoading"
        cancel-title="Cancelar"
        ok-title="Confirmar"
        test-id-cancelar="btn-iniciar-processo-cancelar"
        test-id-confirmar="btn-iniciar-processo-confirmar"
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
import {BButton, BForm} from "bootstrap-vue-next";
import LayoutPadrao from '@/components/layout/LayoutPadrao.vue';
import {computed, nextTick, onMounted, ref, watch} from "vue";
import {useRoute, useRouter} from "vue-router";
import ModalConfirmacao from "@/components/comum/ModalConfirmacao.vue";
import PageHeader from "@/components/layout/PageHeader.vue";
import LoadingButton from "@/components/comum/LoadingButton.vue";
import ProcessoFormFields from "@/components/processo/ProcessoFormFields.vue";
import AppAlert from "@/components/comum/AppAlert.vue";
import {logger} from "@/utils";
import {normalizeError, shouldNotifyGlobally} from "@/utils/apiError";
import {useProcessoForm} from "@/composables/useProcessoForm";
import {useNotification} from "@/composables/useNotification";
import {TEXTOS} from "@/constants/textos";

import {useProcessosStore} from "@/stores/processos";
import {useToastStore} from "@/stores/toast";
import {buscarArvoreComElegibilidade, mapUnidadesArray} from "@/services/unidadeService";
import {Processo as ProcessoModel, TipoProcesso, type Unidade} from "@/types/tipos";

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
const processosStore = useProcessosStore();
const toastStore = useToastStore();
const {notificacao, notify, notifyStructured, clear} = useNotification();

const unidades = ref<Unidade[]>([]);
const isLoadingUnidades = ref(false);

async function buscarUnidadesParaProcesso(tipoProcesso: string, codProcesso?: number) {
  isLoadingUnidades.value = true;
  try {
    const response = await buscarArvoreComElegibilidade(tipoProcesso, codProcesso);
    unidades.value = mapUnidadesArray(response as any);
  } catch (err: any) {
    logger.error("Erro ao buscar unidades:", err);
  } finally {
    isLoadingUnidades.value = false;
  }
}

const mostrarModalConfirmacao = ref(false);
const mostrarModalRemocao = ref(false);
const processoEditando = ref<ProcessoModel | null>(null);

const isLoadingData = ref(false);

onMounted(async () => {
  const codProcesso = route.query.codProcesso;
  if (codProcesso) {
    isLoadingData.value = true;
    try {
      await processosStore.buscarProcessoDetalhe(Number(codProcesso));
      const processo = processosStore.processoDetalhe;
      if (processo) {
        // Redireciona se o processo não está em situação CRIADO (não pode ser editado)
        if (processo.situacao !== 'CRIADO') {
          await router.push(`/processo/${processo.codigo}`);
          return;
        }

        processoEditando.value = processo;
        descricao.value = processo.descricao;
        tipo.value = processo.tipo as any;
        dataLimite.value = processo.dataLimite.split("T")[0];
        unidadesSelecionadas.value = processo.unidades.map((u) => u.codUnidade);
        await buscarUnidadesParaProcesso(
            processo.tipo,
            processo.codigo,
        );
        await nextTick();
      }
    } catch (error) {
      notify(TEXTOS.processo.cadastro.ERRO_CARREGAR_DETALHES, 'danger');
      logger.error("Erro ao carregar processo:", error);
    } finally {
      isLoadingData.value = false;
    }
  } else if (tipo.value) {
    await buscarUnidadesParaProcesso(tipo.value);
  }

  // Auto-focus na descrição ao carregar
  if (!processoEditando.value) {
    nextTick(() => {
      formFieldsRef.value?.inputDescricaoRef?.$el?.focus();
    });
  }
});

watch(tipo, async (novoTipo) => {
  const codProcesso = processoEditando.value
      ? processoEditando.value.codigo
      : undefined;
  if (novoTipo) {
    await buscarUnidadesParaProcesso(novoTipo, codProcesso);
  }
});

function handleApiErrors(error: any, title: string, defaultMsg: string) {
  clearErrors();
  clear();

  const lastError = processosStore.lastError;

  if (lastError) {
    setFromNormalizedError(lastError);
    if (fieldErrors.value.dataLimiteEtapa1) fieldErrors.value.dataLimite = fieldErrors.value.dataLimiteEtapa1;

    const hasFieldErrors = hasErrors();
    const genericErrors = lastError.subErrors?.filter(e => !e.field).map(e => e.message || '') || [];

    if (!hasFieldErrors || genericErrors.length > 0) {
      notifyStructured(
          lastError.message || defaultMsg,
          genericErrors as string[],
          'danger',
          lastError.stackTrace || undefined,
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

  const erroNormalizado = normalizeError(error);
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
      await processosStore.atualizarProcesso(
          processoEditando.value.codigo,
          request,
      );
      toastStore.setPending(TEXTOS.sucesso.PROCESSO_ALTERADO);
      await router.push("/painel");
    } else {
      const request = construirCriarRequest();
      await processosStore.criarProcesso(request);
      toastStore.setPending(TEXTOS.sucesso.PROCESSO_CRIADO);
      await router.push("/painel");
    }
    limparCampos();
  } catch (error) {
    handleApiErrors(error, "Erro ao salvar processo", "Não foi possível salvar o processo. Verifique os dados e tente novamente.");
  } finally {
    isLoading.value = false;
  }
}

async function abrirModalConfirmacao() {
  mostrarModalConfirmacao.value = true;
}

async function confirmarIniciarProcesso() {
  clearErrors();
  isLoading.value = true;

  let codigoProcesso = processoEditando.value?.codigo;

  if (!codigoProcesso) {
    try {
      const request = construirCriarRequest();
      const novoProcesso = await processosStore.criarProcesso(request);
      codigoProcesso = novoProcesso.codigo;
    } catch (error) {
      mostrarModalConfirmacao.value = false;
      handleApiErrors(error, "Erro ao criar processo", TEXTOS.processo.cadastro.ERRO_CRIAR_PARA_INICIAR);
      isLoading.value = false;
      return;
    }
  }

  try {
    await processosStore.iniciarProcesso(
        codigoProcesso,
        tipo.value as TipoProcesso,
        unidadesSelecionadas.value,
    );

    toastStore.setPending(TEXTOS.sucesso.PROCESSO_INICIADO);
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
      await processosStore.removerProcesso(processoEditando.value.codigo);
      toastStore.setPending(TEXTOS.sucesso.PROCESSO_REMOVIDO(descRemovida));
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
