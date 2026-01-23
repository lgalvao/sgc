<template>
  <BContainer class="mt-4">
    <PageHeader title="Cadastro de processo" />

    <BForm class="mt-4 col-md-6 col-sm-8 col-12">
      <BAlert
          v-model="alertState.show"
          :fade="false"
          :variant="alertState.variant"
          class="mb-3"
          dismissible
      >
        <h4 v-if="alertState.title" class="alert-heading">{{ alertState.title }}</h4>
        <p class="mb-0">{{ alertState.body }}</p>
        <ul v-if="alertState.errors && alertState.errors.length > 0" class="mb-0 mt-2">
          <li v-for="(error, index) in alertState.errors" :key="index">{{ error }}</li>
        </ul>
      </BAlert>

      <BFormGroup
          class="mb-3"
          label="Descrição"
          label-for="descricao"
      >
        <BFormInput
            id="descricao"
            ref="inputDescricaoRef"
            v-model="descricao"
            :state="fieldErrors.descricao ? false : null"
            data-testid="inp-processo-descricao"
            placeholder="Descreva o processo"
            type="text"
        />
        <BFormInvalidFeedback :state="fieldErrors.descricao ? false : null">
          {{ fieldErrors.descricao }}
        </BFormInvalidFeedback>
      </BFormGroup>

      <BFormGroup
          class="mb-3"
          label="Tipo"
          label-for="tipo"
      >
        <BFormSelect
            id="tipo"
            v-model="tipo"
            :options="tipoOptions"
            :state="fieldErrors.tipo ? false : null"
            data-testid="sel-processo-tipo"
        />
        <BFormInvalidFeedback :state="fieldErrors.tipo ? false : null">
          {{ fieldErrors.tipo }}
        </BFormInvalidFeedback>
      </BFormGroup>

      <BFormGroup
          class="mb-3"
          label="Unidades participantes"
      >
        <div class="border rounded p-3" :class="{ 'border-danger': fieldErrors.unidades }">
          <ArvoreUnidades
              v-if="!unidadesStore.isLoading"
              v-model="unidadesSelecionadas"
              :unidades="unidadesStore.unidades"
          />
          <div
              v-else
              class="text-center py-3"
          >
            <span class="spinner-border spinner-border-sm me-2"/>
            Carregando unidades...
          </div>
        </div>
        <BFormInvalidFeedback :state="fieldErrors.unidades ? false : null" class="d-block">
          {{ fieldErrors.unidades }}
        </BFormInvalidFeedback>
      </BFormGroup>

      <BFormGroup
          class="mb-3"
          description="Prazo para conclusão da primeira etapa (Mapeamento/Revisão)."
          label="Data limite"
          label-for="dataLimite"
      >
        <BFormInput
            id="dataLimite"
            v-model="dataLimite"
            :state="fieldErrors.dataLimite ? false : null"
            data-testid="inp-processo-data-limite"
            type="date"
        />
        <BFormInvalidFeedback :state="fieldErrors.dataLimite ? false : null">
          {{ fieldErrors.dataLimite }}
        </BFormInvalidFeedback>
      </BFormGroup>

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
  </BContainer>
</template>

<script lang="ts" setup>
import {
  BAlert,
  BButton,
  BContainer,
  BForm,
  BFormGroup,
  BFormInput,
  BFormInvalidFeedback,
  BFormSelect,
} from "bootstrap-vue-next";
import {nextTick, onMounted, ref, watch} from "vue";
import {useRoute, useRouter} from "vue-router";
import ModalConfirmacao from "@/components/ModalConfirmacao.vue";
import PageHeader from "@/components/layout/PageHeader.vue";
import LoadingButton from "@/components/ui/LoadingButton.vue";
import ArvoreUnidades from "@/components/ArvoreUnidades.vue";
import * as processoService from "@/services/processoService";
import {logger} from "@/utils";
import {useProcessoForm} from "@/composables/useProcessoForm";

import {useProcessosStore} from "@/stores/processos";
import {useUnidadesStore} from "@/stores/unidades";
import {useFeedbackStore} from "@/stores/feedback";
import {Processo as ProcessoModel, TipoProcesso} from "@/types/tipos";

const tipoOptions = [
  { value: TipoProcesso.MAPEAMENTO, text: 'Mapeamento' },
  { value: TipoProcesso.REVISAO, text: 'Revisão' },
  { value: TipoProcesso.DIAGNOSTICO, text: 'Diagnóstico' }
];

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

const inputDescricaoRef = ref<InstanceType<typeof BFormInput> | null>(null);

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
}

const alertState = ref<AlertState>({
  show: false,
  variant: 'info',
  title: '',
  body: '',
  errors: []
});

function mostrarAlerta(variant: AlertState['variant'], title: string, body: string, errors: string[] = []) {
  alertState.value = {
    show: true,
    variant,
    title,
    body,
    errors
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
          // Como vamos redirecionar, não adianta mostrar alerta local.
          // Idealmente usaríamos um store global, mas aqui apenas logamos/redirecionamos
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
  } else {
    await unidadesStore.buscarUnidadesParaProcesso(tipo.value);
  }

  // Auto-focus na descrição ao carregar
  if (!processoEditando.value) {
    nextTick(() => {
      inputDescricaoRef.value?.$el?.focus();
    });
  }
});

watch(tipo, async (novoTipo) => {
  const codProcesso = processoEditando.value
      ? processoEditando.value.codigo
      : undefined;
  await unidadesStore.buscarUnidadesParaProcesso(novoTipo, codProcesso);
});

function handleApiErrors(error: any, title: string, defaultMsg: string) {
  clearErrors();
  alertState.value.show = false;

  const lastError = processosStore.lastError;

  if (lastError) {
    setFromNormalizedError(lastError);
    if (fieldErrors.value.dataLimiteEtapa1) fieldErrors.value.dataLimite = fieldErrors.value.dataLimiteEtapa1;

    const hasFieldErrors = hasErrors();
    const genericErrors = lastError.subErrors?.filter(e => !e.field).map(e => e.message) || [];

    if (!hasFieldErrors || genericErrors.length > 0) {
      mostrarAlerta('danger', title, lastError.message || defaultMsg, genericErrors);
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
    mostrarAlerta('danger', title, defaultMsg, []);
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
      await router.push("/painel");
    } else {
      const request = construirCriarRequest();
      await processosStore.criarProcesso(request);
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
    await router.push("/painel");
    mostrarModalConfirmacao.value = false;
    feedbackStore.show("Processo iniciado", "O processo foi iniciado com sucesso.", "success");
  } catch (error) {
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
    try {
      await processoService.excluirProcesso(processoEditando.value.codigo);
      await router.push("/painel");
      if (!processoEditando.value) {
        // Only clear fields if it was a new process
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
