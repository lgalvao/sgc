<template>
  <BContainer class="mt-4">
    <h2>Cadastro de processo</h2>

    <BAlert
      v-model="alertState.show"
      :variant="alertState.variant"
      dismissible
      :fade="false"
      class="mt-3"
    >
      <h4 v-if="alertState.title" class="alert-heading">{{ alertState.title }}</h4>
      <p class="mb-0">{{ alertState.body }}</p>
    </BAlert>

    <BForm class="mt-4 col-md-6 col-sm-8 col-12">
      <BFormGroup
        label="Descrição"
        label-for="descricao"
        class="mb-3"
      >
        <BFormInput
          id="descricao"
          v-model="descricao"
          placeholder="Descreva o processo"
          type="text"
          data-testid="inp-processo-descricao"
        />
      </BFormGroup>

      <BFormGroup
        label="Tipo"
        label-for="tipo"
        class="mb-3"
      >
        <BFormSelect
          id="tipo"
          v-model="tipo"
          data-testid="sel-processo-tipo"
          :options="Object.values(TipoProcessoEnum)"
        />
      </BFormGroup>

      <BFormGroup
        label="Unidades participantes"
        class="mb-3"
      >
        <div class="border rounded p-3">
          <ArvoreUnidades
            v-if="!unidadesStore.isLoading"
            v-model="unidadesSelecionadas"
            :unidades="unidadesStore.unidades"
          />
          <div
            v-else
            class="text-center py-3"
          >
            <span class="spinner-border spinner-border-sm me-2" />
            Carregando unidades...
          </div>
        </div>
      </BFormGroup>

      <BFormGroup
        label="Data limite"
        label-for="dataLimite"
        class="mb-3"
      >
        <BFormInput
          id="dataLimite"
          v-model="dataLimite"
          type="date"
          data-testid="inp-processo-data-limite"
        />
      </BFormGroup>

      <BButton
        variant="primary"
        type="button"
        data-testid="btn-processo-salvar"
        @click="salvarProcesso"
      >
        Salvar
      </BButton>
      <BButton
        variant="success"
        class="ms-2"
        data-testid="btn-processo-iniciar"
        @click="abrirModalConfirmacao"
      >
        Iniciar processo
      </BButton>
      <BButton
        v-if="processoEditando"
        variant="danger"
        class="ms-2"
        data-testid="btn-processo-remover"
        @click="abrirModalRemocao"
      >
        Remover
      </BButton>
      <BButton
        variant="secondary"
        class="ms-2"
        to="/painel"
      >
        Cancelar
      </BButton>
    </BForm>

    <!-- Modal de confirmação CDU-05 -->
    <BModal
      v-model="mostrarModalConfirmacao"
      :fade="false"
      title="Iniciar processo"
      centered
      hide-footer
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
          variant="secondary"
          data-testid="btn-iniciar-processo-cancelar"
          @click="fecharModalConfirmacao"
        >
          Cancelar
        </BButton>
        <BButton
          variant="primary"
          data-testid="btn-iniciar-processo-confirmar"
          @click="confirmarIniciarProcesso"
        >
          Confirmar
        </BButton>
      </template>
    </BModal>

    <!-- Modal de confirmação de remoção -->
    <BModal
      v-model="mostrarModalRemocao"
      :fade="false"
      title="Remover processo"
      centered
      hide-footer
    >
      <template #default>
        <p>Remover o processo '{{ descricao }}'? Esta ação não poderá ser desfeita.</p>
      </template>
      <template #footer>
        <BButton
          variant="secondary"
          @click="fecharModalRemocao"
        >
          Cancelar
        </BButton>
        <BButton
          variant="danger"
          @click="confirmarRemocao"
        >
          Remover
        </BButton>
      </template>
    </BModal>
  </BContainer>
</template>

<script lang="ts" setup>
import {BAlert, BButton, BContainer, BForm, BFormGroup, BFormInput, BFormSelect, BModal} from "bootstrap-vue-next";
import {nextTick, onMounted, ref, watch} from "vue";
import {useRoute, useRouter} from "vue-router";
import ArvoreUnidades from "@/components/ArvoreUnidades.vue";
// import {TEXTOS} from "@/constants"; // Removed
import * as processoService from "@/services/processoService";

import {useProcessosStore} from "@/stores/processos";
import {useUnidadesStore} from "@/stores/unidades";
import {
  AtualizarProcessoRequest,
  CriarProcessoRequest,
  Processo as ProcessoModel,
  TipoProcesso
} from "@/types/tipos";

const TipoProcessoEnum = TipoProcesso;

const unidadesSelecionadas = ref<number[]>([]);
const descricao = ref<string>("");
const tipo = ref<string>("MAPEAMENTO");
const dataLimite = ref<string>("");
const router = useRouter();
const route = useRoute();
const processosStore = useProcessosStore();
const unidadesStore = useUnidadesStore();

const mostrarModalConfirmacao = ref(false);
const mostrarModalRemocao = ref(false);
const processoEditando = ref<ProcessoModel | null>(null);

interface AlertState {
  show: boolean;
  variant: 'primary' | 'secondary' | 'success' | 'danger' | 'warning' | 'info' | 'light' | 'dark';
  title: string;
  body: string;
}

const alertState = ref<AlertState>({
  show: false,
  variant: 'info',
  title: '',
  body: ''
});

function mostrarAlerta(variant: AlertState['variant'], title: string, body: string) {
  alertState.value = {
    show: true,
    variant,
    title,
    body
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
}

async function salvarProcesso() {
  // Validações agora são feitas no backend via Bean Validation
  try {
    if (processoEditando.value) {
      const request: AtualizarProcessoRequest = {
        codigo: processoEditando.value.codigo,
        descricao: descricao.value,
        tipo: tipo.value as TipoProcesso,
        dataLimiteEtapa1: `${dataLimite.value}T00:00:00`,
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
        dataLimiteEtapa1: `${dataLimite.value}T00:00:00`,
        unidades: unidadesSelecionadas.value, // Backend valida elegibilidade
      };
      await processosStore.criarProcesso(request);
      await router.push("/painel");
    }
    limparCampos();
  } catch (error) {
    mostrarAlerta('danger', "Erro ao salvar processo", "Não foi possível salvar o processo. Verifique os dados e tente novamente.");
    console.error("Erro ao salvar processo:", error);
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
  mostrarModalConfirmacao.value = false;

  let codigoProcesso = processoEditando.value?.codigo;

  if (!codigoProcesso) {
    // Se não houver processo salvo, cria antes de iniciar
    // Backend valida elegibilidade das unidades
    try {
      const request: CriarProcessoRequest = {
        descricao: descricao.value,
        tipo: tipo.value as TipoProcesso,
        dataLimiteEtapa1: `${dataLimite.value}T00:00:00`,
        unidades: unidadesSelecionadas.value,
      };
      const novoProcesso = await processosStore.criarProcesso(request);
      codigoProcesso = novoProcesso.codigo;
    } catch (error) {
      mostrarAlerta('danger', "Erro ao criar processo", "Não foi possível criar o processo para iniciá-lo.");
      console.error("Erro ao criar processo:", error);
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
    limparCampos();
  } catch (error) {
    mostrarAlerta('danger', "Erro ao iniciar processo", "Não foi possível iniciar o processo. Tente novamente.");
    console.error("Erro ao iniciar processo:", error);
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
    try {
      await processoService.excluirProcesso(processoEditando.value.codigo);
      await router.push("/painel");
      if (!processoEditando.value) {
        // Only clear fields if it was a new process
        limparCampos();
      }
    } catch (error) {
      mostrarAlerta('danger', "Erro ao remover processo", "Não foi possível remover o processo. Tente novamente.");
      console.error("Erro ao remover processo:", error);
    }
  }
  fecharModalRemocao();
}
</script>
