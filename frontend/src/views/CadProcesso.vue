<template>
  <BContainer class="mt-4">
    <h2>Cadastro de processo</h2>

    <BAlert
        v-model="alertState.show"
        :fade="false"
        :variant="alertState.variant"
        class="mt-3"
        dismissible
    >
      <h4 v-if="alertState.title" class="alert-heading">{{ alertState.title }}</h4>
      <p class="mb-0">{{ alertState.body }}</p>
      <ul v-if="alertState.errors && alertState.errors.length > 0" class="mb-0 mt-2">
        <li v-for="(error, index) in alertState.errors" :key="index">{{ error }}</li>
      </ul>
    </BAlert>

    <BForm class="mt-4 col-md-6 col-sm-8 col-12">
      <BFormGroup
          class="mb-3"
          label="Descrição"
          label-for="descricao"
      >
        <BFormInput
            id="descricao"
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
        <div v-if="fieldErrors.unidades" class="text-danger small mt-1">
          {{ fieldErrors.unidades }}
        </div>
      </BFormGroup>

      <BFormGroup
          class="mb-3"
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
          <BButton
              :disabled="isFormInvalid || isLoading"
              data-testid="btn-processo-iniciar"
              variant="success"
              @click="abrirModalConfirmacao"
          >
            <i aria-hidden="true" class="bi bi-play-fill me-1"/> Iniciar processo
          </BButton>
          <BButton
              :disabled="isFormInvalid || isLoading"
              class="ms-2"
              data-testid="btn-processo-salvar"
              type="button"
              variant="outline-primary"
              @click="salvarProcesso"
          >
            <BSpinner v-if="isLoading" small class="me-1" />
            <i v-else aria-hidden="true" class="bi bi-save me-1"/> Salvar
          </BButton>
          <BButton
              v-if="processoEditando"
              :disabled="isLoading"
              class="ms-2"
              data-testid="btn-processo-remover"
              variant="outline-danger"
              @click="abrirModalRemocao"
          >
            <i aria-hidden="true" class="bi bi-trash me-1"/> Remover
          </BButton>
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
    <BModal
        v-model="mostrarModalConfirmacao"
        :fade="false"
        centered
        hide-footer
        title="Iniciar processo"
    >
      <template #default>
        <p><strong>Descrição:</strong> {{ descricao }}</p>
        <p><strong>Tipo:</strong> {{ tipo }}</p>
        <p><strong>Unidades selecionadas:</strong> {{ unidadesSelecionadas.length }}</p>
        <hr>
        <p>
          Ao iniciar o processo, não será mais possível editá-lo ou removê-lo e todas as unidades participantes
          serão notificadas por e-mail.
        </p>
      </template>
      <template #footer>
        <BButton
            :disabled="isLoading"
            data-testid="btn-iniciar-processo-cancelar"
            variant="secondary"
            @click="fecharModalConfirmacao"
        >
          Cancelar
        </BButton>
        <BButton
            :disabled="isLoading"
            data-testid="btn-iniciar-processo-confirmar"
            variant="primary"
            @click="confirmarIniciarProcesso"
        >
          <BSpinner v-if="isLoading" small class="me-1" />
          <span v-else>Confirmar</span>
        </BButton>
      </template>
    </BModal>

    <!-- Modal de confirmação de remoção -->
    <BModal
        v-model="mostrarModalRemocao"
        :fade="false"
        centered
        hide-footer
        title="Remover processo"
    >
      <template #default>
        <p>Remover o processo '{{ descricao }}'? Esta ação não poderá ser desfeita.</p>
      </template>
      <template #footer>
        <BButton
            :disabled="isLoading"
            variant="secondary"
            @click="fecharModalRemocao"
        >
          Cancelar
        </BButton>
        <BButton
            :disabled="isLoading"
            variant="danger"
            @click="confirmarRemocao"
        >
          <BSpinner v-if="isLoading" small class="me-1" />
          <span v-else>Remover</span>
        </BButton>
      </template>
    </BModal>
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
  BModal,
  BSpinner
} from "bootstrap-vue-next";
import {computed, nextTick, onMounted, ref, watch} from "vue";
import {useRoute, useRouter} from "vue-router";
import ArvoreUnidades from "@/components/ArvoreUnidades.vue";
import * as processoService from "@/services/processoService";
import {useFormErrors} from '@/composables/useFormErrors';

import {useProcessosStore} from "@/stores/processos";
import {useUnidadesStore} from "@/stores/unidades";
import {useFeedbackStore} from "@/stores/feedback";
import {AtualizarProcessoRequest, CriarProcessoRequest, Processo as ProcessoModel, TipoProcesso} from "@/types/tipos";

const tipoOptions = [
  { value: TipoProcesso.MAPEAMENTO, text: 'Mapeamento' },
  { value: TipoProcesso.REVISAO, text: 'Revisão' },
  { value: TipoProcesso.DIAGNOSTICO, text: 'Diagnóstico' }
];

const unidadesSelecionadas = ref<number[]>([]);
const descricao = ref<string>("");
const tipo = ref<string>("MAPEAMENTO");
const dataLimite = ref<string>("");
const isLoading = ref(false);
const router = useRouter();
const route = useRoute();
const processosStore = useProcessosStore();
const unidadesStore = useUnidadesStore();
const feedbackStore = useFeedbackStore();

const {errors: fieldErrors, setFromNormalizedError, clearErrors, hasErrors} = useFormErrors([
  'descricao',
  'tipo',
  'dataLimite',
  'unidades',
  'dataLimiteEtapa1'
]);

watch(descricao, () => {
  fieldErrors.value.descricao = '';
});
watch(tipo, () => {
  fieldErrors.value.tipo = '';
});
watch(dataLimite, () => {
  fieldErrors.value.dataLimite = '';
});
watch(unidadesSelecionadas, () => {
  fieldErrors.value.unidades = '';
});

const mostrarModalConfirmacao = ref(false);
const mostrarModalRemocao = ref(false);
const processoEditando = ref<ProcessoModel | null>(null);

const isFormInvalid = computed(() => {
  return !descricao.value.trim() ||
      !tipo.value ||
      !dataLimite.value ||
      unidadesSelecionadas.value.length === 0;
});

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
        tipo.value = processo.tipo;
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
      console.error("Erro ao carregar processo:", error);
    }
  } else {
    await unidadesStore.buscarUnidadesParaProcesso(tipo.value);
  }
});

watch(tipo, async (novoTipo) => {
  const codProcesso = processoEditando.value
      ? processoEditando.value.codigo
      : undefined;
  await unidadesStore.buscarUnidadesParaProcesso(novoTipo, codProcesso);
});

function limparCampos() {
  descricao.value = "";
  tipo.value = "MAPEAMENTO";
  dataLimite.value = "";
  unidadesSelecionadas.value = [];
  clearErrors();
}

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
    }
  } else {
    mostrarAlerta('danger', title, defaultMsg, []);
    console.error(title + ":", error);
  }
  console.error(title + ":", error);
}

async function salvarProcesso() {
  clearErrors();
  isLoading.value = true;
  
  // Validações agora são feitas no backend via Bean Validation
  try {
    if (processoEditando.value) {
      const request: AtualizarProcessoRequest = {
        codigo: processoEditando.value.codigo,
        descricao: descricao.value,
        tipo: tipo.value as TipoProcesso,
        dataLimiteEtapa1: dataLimite.value ? `${dataLimite.value}T00:00:00` : null,
        unidades: unidadesSelecionadas.value, // Backend valida elegibilidade
      };
      await processosStore.atualizarProcesso(
          processoEditando.value.codigo,
          request,
      );
      await router.push("/painel");
    } else {
      const request: CriarProcessoRequest = {
        descricao: descricao.value,
        tipo: tipo.value as TipoProcesso,
        dataLimiteEtapa1: dataLimite.value ? `${dataLimite.value}T00:00:00` : null,
        unidades: unidadesSelecionadas.value, // Backend valida elegibilidade
      };
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

function fecharModalConfirmacao() {
  mostrarModalConfirmacao.value = false;
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
      const request: CriarProcessoRequest = {
        descricao: descricao.value,
        tipo: tipo.value as TipoProcesso,
        dataLimiteEtapa1: dataLimite.value ? `${dataLimite.value}T00:00:00` : null,
        unidades: unidadesSelecionadas.value,
      };
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
