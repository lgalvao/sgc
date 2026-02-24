<template>
  <LayoutPadrao>
    <PageHeader title="Cadastro de processo" />

    <BForm class="mt-4 col-md-6 col-sm-8 col-12">
      <FormErrorAlert
          v-model:show="alertState.show"
          :variant="alertState.variant"
          :title="alertState.title"
          :body="alertState.body"
          :errors="alertState.errors"
          :stack-trace="alertState.stackTrace"
      />

      <ProcessoFormFields
          ref="formFieldsRef"
          v-model="formData"
          :field-errors="fieldErrors"
          :unidades="unidadesStore.unidades"
          :is-loading-unidades="unidadesStore.isLoading"
      />

      <div class="d-flex justify-content-between">
        <div>
          <LoadingButton
              :disabled="isFormInvalid || isLoading"
              data-testid="btn-processo-iniciar"
              variant="success"
              icon="play-fill"
              text="Iniciar processo"
              @click="abrirModalConfirmacao"
          />
          <LoadingButton
              :loading="isLoading"
              :disabled="isFormInvalid"
              class="ms-2"
              data-testid="btn-processo-salvar"
              type="button"
              variant="outline-primary"
              icon="save"
              text="Salvar"
              loading-text="Salvando..."
              @click="salvarProcesso"
          />
          <LoadingButton
              v-if="processoEditando"
              :disabled="isLoading"
              class="ms-2"
              data-testid="btn-processo-remover"
              variant="outline-danger"
              icon="trash"
              text="Remover"
              @click="abrirModalRemocao"
          />
        </div>
        <BButton
            :disabled="isLoading"
            class="text-secondary"
            to="/painel"
            variant="link"
        >
          Cancelar
        </BButton>
      </div>
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
        titulo="Iniciar processo"
        @confirmar="confirmarIniciarProcesso"
    >
      <p><strong>Descrição:</strong> {{ descricao }}</p>
      <p><strong>Tipo:</strong> {{ tipo }}</p>
      <p><strong>Unidades selecionadas:</strong> {{ unidadesSelecionadas.length }}</p>
      <hr>
      <p>
        Ao iniciar o processo, não será mais possível editá-lo ou removê-lo e todas as unidades participantes
        serão notificadas por e-mail.
      </p>
    </ModalConfirmacao>

    <!-- Modal de confirmação de remoção -->
    <ModalConfirmacao
        v-model="mostrarModalRemocao"
        :auto-close="false"
        :loading="isLoading"
        ok-title="Remover"
        titulo="Remover processo"
        variant="danger"
        @confirmar="confirmarRemocao"
    >
      <p>Remover o processo '{{ descricao }}'? Esta ação não poderá ser desfeita.</p>
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
import FormErrorAlert from "@/components/comum/FormErrorAlert.vue";
import {logger} from "@/utils";
import {useProcessoForm} from "@/composables/useProcessoForm";

import {useProcessosStore} from "@/stores/processos";
import {useUnidadesStore} from "@/stores/unidades";
import {useFeedbackStore} from "@/stores/feedback";
import {Processo as ProcessoModel, TipoProcesso} from "@/types/tipos";

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
const unidadesStore = useUnidadesStore();
const feedbackStore = useFeedbackStore();

const mostrarModalConfirmacao = ref(false);
const mostrarModalRemocao = ref(false);
const processoEditando = ref<ProcessoModel | null>(null);

interface AlertState {
  show: boolean;
  variant: 'primary' | 'secondary' | 'success' | 'danger' | 'warning' | 'info' | 'light' | 'dark';
  title: string;
  body: string;
  errors?: string[];
  stackTrace?: string;
}

const alertState = ref<AlertState>({
  show: false,
  variant: 'info',
  title: '',
  body: '',
  errors: [],
  stackTrace: ''
});

function mostrarAlerta(variant: AlertState['variant'], title: string, body: string, errors: string[] = [], stackTrace: string = '') {
  alertState.value = {
    show: true,
    variant,
    title,
    body,
    errors,
    stackTrace
  };
  window.scrollTo(0, 0);
}

onMounted(async () => {
  const codProcesso = route.query.codProcesso;
  if (codProcesso) {
    try {
      await processosStore.buscarProcessoDetalhe(Number(codProcesso));
      const processo = processosStore.processoDetalhe;
      if (processo) {
        // Redirect if process is not in CRIADO state (cannot be edited)
        if (processo.situacao !== 'CRIADO') {
          await router.push(`/processo/${processo.codigo}`);
          return;
        }

        processoEditando.value = processo;
        descricao.value = processo.descricao;
        tipo.value = processo.tipo as any;
        dataLimite.value = processo.dataLimite.split("T")[0];
        unidadesSelecionadas.value = processo.unidades.map((u) => u.codUnidade);
        await unidadesStore.buscarUnidadesParaProcesso(
            processo.tipo,
            processo.codigo,
        );
        await nextTick();
      }
    } catch (error) {
      mostrarAlerta('danger', "Erro ao carregar processo", "Não foi possível carregar os detalhes do processo.");
      logger.error("Erro ao carregar processo:", error);
    }
  } else if (tipo.value) {
    await unidadesStore.buscarUnidadesParaProcesso(tipo.value);
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
    await unidadesStore.buscarUnidadesParaProcesso(novoTipo, codProcesso);
  }
});

function handleApiErrors(error: any, title: string, defaultMsg: string) {
  clearErrors();
  alertState.value.show = false;

  const lastError = processosStore.lastError;

  if (lastError) {
    setFromNormalizedError(lastError);
    if (fieldErrors.value.dataLimiteEtapa1) fieldErrors.value.dataLimite = fieldErrors.value.dataLimiteEtapa1;

    const hasFieldErrors = hasErrors();
    const genericErrors = lastError.subErrors?.filter(e => !e.field).map(e => e.message || '') || [];

    if (!hasFieldErrors || genericErrors.length > 0) {
      mostrarAlerta('danger', title, lastError.message || defaultMsg, genericErrors as string[], lastError.stackTrace);
    } else if (hasFieldErrors) {
      // Focus on first invalid field
      nextTick(() => {
        const firstInvalid = document.querySelector('.is-invalid') as HTMLElement;
        if (firstInvalid) {
          firstInvalid.focus();
        }
      });
    }
  } else {
    mostrarAlerta('danger', title, defaultMsg, [], (error as any)?.stack || '');
    logger.error(title + ":", error);
  }
  logger.error(title + ":", error);
}

async function salvarProcesso() {
  clearErrors();
  isLoading.value = true;
  
  // Validações agora são feitas no backend via Bean Validation
  try {
    if (processoEditando.value) {
      const request = construirAtualizarRequest(processoEditando.value.codigo);
      await processosStore.atualizarProcesso(
          processoEditando.value.codigo,
          request,
      );
      feedbackStore.show("Processo alterado", "O processo foi alterado com sucesso.", "success");
      await router.push("/painel");
    } else {
      const request = construirCriarRequest();
      await processosStore.criarProcesso(request);
      feedbackStore.show("Processo criado", "O processo foi criado com sucesso.", "success");
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
  // Validações serão feitas no backend ao criar/iniciar o processo
  mostrarModalConfirmacao.value = true;
}

async function confirmarIniciarProcesso() {
  // Limpa erros
  clearErrors();
  isLoading.value = true;

  let codigoProcesso = processoEditando.value?.codigo;

  if (!codigoProcesso) {
    // Se não houver processo salvo, cria antes de iniciar
    // Backend valida elegibilidade das unidades
    try {
      const request = construirCriarRequest();
      const novoProcesso = await processosStore.criarProcesso(request);
      codigoProcesso = novoProcesso.codigo;
    } catch (error) {
      mostrarModalConfirmacao.value = false;
      handleApiErrors(error, "Erro ao criar processo", "Não foi possível criar o processo para iniciá-lo.");
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
    feedbackStore.show("Processo iniciado", "O processo foi iniciado com sucesso.", "success");
    await router.push("/painel");
    mostrarModalConfirmacao.value = false;
  } catch (error) {
    console.error("Erro ao iniciar processo:", error);
    mostrarModalConfirmacao.value = false;
    handleApiErrors(error, "Erro ao iniciar processo", "Não foi possível iniciar o processo. Tente novamente.");
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
      feedbackStore.show("Processo removido", `Processo ${descRemovida} removido`, "success");
      await router.push("/painel");
      if (!processoEditando.value) {
        limparCampos();
      }
      fecharModalRemocao();
    } catch (error) {
      fecharModalRemocao();
      handleApiErrors(error, "Erro ao remover processo", "Não foi possível remover o processo. Tente novamente.");
    } finally {
      isLoading.value = false;
    }
  } else {
    fecharModalRemocao();
  }
}
</script>
