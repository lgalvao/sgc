<template>
  <BContainer class="mt-4">
    <h2>Cadastro de processo</h2>

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
          data-testid="input-descricao"
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
          data-testid="select-tipo"
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
          data-testid="input-dataLimite"
        />
      </BFormGroup>

      <BButton
        variant="primary"
        @click="salvarProcesso"
      >
        Salvar
      </BButton>
      <BButton
        variant="success"
        class="ms-2"
        data-testid="btn-iniciar-processo"
        @click="abrirModalConfirmacao"
      >
        Iniciar processo
      </BButton>
      <BButton
        v-if="processoEditando"
        variant="danger"
        class="ms-2"
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
          @click="fecharModalConfirmacao"
        >
          Cancelar
        </BButton>
        <BButton
          variant="primary"
          @click="confirmarIniciarProcesso"
        >
          Confirmar
        </BButton>
      </template>
    </BModal>

    <!-- Modal de confirmação de remoção -->
    <BModal
      v-model="mostrarModalRemocao"
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
import {
  BButton,
  BContainer,
  BForm,
  BFormGroup,
  BFormInput,
  BFormSelect,
  BModal,
} from "bootstrap-vue-next";
import { nextTick, onMounted, ref, watch } from "vue";
import { useRoute, useRouter } from "vue-router";
import ArvoreUnidades from "@/components/ArvoreUnidades.vue";
import { TEXTOS } from "@/constants";
import * as processoService from "@/services/processoService";
import { useNotificacoesStore } from "@/stores/notificacoes";
import { useProcessosStore } from "@/stores/processos";
import { useUnidadesStore } from "@/stores/unidades";
import {
  AtualizarProcessoRequest,
  CriarProcessoRequest,
  Processo as ProcessoModel,
  TipoProcesso,
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
const notificacoesStore = useNotificacoesStore();
const mostrarModalConfirmacao = ref(false);
const mostrarModalRemocao = ref(false);
const processoEditando = ref<ProcessoModel | null>(null);

onMounted(async () => {
  const codProcesso = route.query.codProcesso;
  if (codProcesso) {
    try {
      await processosStore.fetchProcessoDetalhe(Number(codProcesso));
      const processo = processosStore.processoDetalhe;
      if (processo) {
        processoEditando.value = processo;
        descricao.value = processo.descricao;
        tipo.value = processo.tipo;
        dataLimite.value = processo.dataLimite.split("T")[0];
        unidadesSelecionadas.value = processo.unidades.map((u) => u.codUnidade);
        await unidadesStore.fetchUnidadesParaProcesso(
          processo.tipo,
          processo.codigo,
        );
        await nextTick();
      }
    } catch (error) {
      notificacoesStore.erro(
        "Erro ao carregar processo",
        "Não foi possível carregar os detalhes do processo.",
      );
      console.error("Erro ao carregar processo:", error);
    }
  } else {
    await unidadesStore.fetchUnidadesParaProcesso(tipo.value);
  }
});

watch(tipo, async (novoTipo) => {
  const codProcesso = processoEditando.value
    ? processoEditando.value.codigo
    : undefined;
  await unidadesStore.fetchUnidadesParaProcesso(novoTipo, codProcesso);
});

function limparCampos() {
  descricao.value = "";
  tipo.value = "MAPEAMENTO";
  dataLimite.value = "";
  unidadesSelecionadas.value = [];
}

async function salvarProcesso() {
  if (
    !descricao.value ||
    !dataLimite.value ||
    unidadesSelecionadas.value.length === 0
  ) {
    notificacoesStore.erro(
      "Dados incompletos",
      "Preencha todos os campos e selecione ao menos uma unidade.",
    );
    return;
  }

  try {
    if (processoEditando.value) {
      const request: AtualizarProcessoRequest = {
        codigo: processoEditando.value.codigo,
        descricao: descricao.value,
        tipo: tipo.value as TipoProcesso,
        dataLimiteEtapa1: `${dataLimite.value}T00:00:00`,
        unidades: unidadesSelecionadas.value,
      };
      await processosStore.atualizarProcesso(
        processoEditando.value.codigo,
        request,
      );
      notificacoesStore.sucesso(
        "Processo alterado",
        "O processo foi alterado!",
      );
      await router.push("/painel");
    } else {
      const request: CriarProcessoRequest = {
        descricao: descricao.value,
        tipo: tipo.value as TipoProcesso,
        dataLimiteEtapa1: `${dataLimite.value}T00:00:00`,
        unidades: unidadesSelecionadas.value,
      };
      await processosStore.criarProcesso(request);
      notificacoesStore.sucesso("Processo criado", "O processo foi criado!");
      await router.push("/painel");
    }
    limparCampos();
  } catch (error) {
    notificacoesStore.erro(
      "Erro ao salvar processo",
      "Não foi possível salvar o processo. Verifique os dados e tente novamente.",
    );
    console.error("Erro ao salvar processo:", error);
  }
}

async function abrirModalConfirmacao() {
  if (
    !descricao.value ||
    !dataLimite.value ||
    unidadesSelecionadas.value.length === 0
  ) {
    notificacoesStore.erro(
      "Dados incompletos",
      "Preencha todos os campos e selecione ao menos uma unidade.",
    );
    return;
  }

  // A validação de unidades agora é feita no backend,
  // mas uma verificação simples de seleção pode ser mantida.
  if (unidadesSelecionadas.value.length === 0) {
    notificacoesStore.erro(
      "Nenhuma unidade selecionada",
      "Selecione ao menos uma unidade para iniciar o processo.",
    );
    return;
  }

  mostrarModalConfirmacao.value = true;
}

function fecharModalConfirmacao() {
  mostrarModalConfirmacao.value = false;
}

async function confirmarIniciarProcesso() {
  mostrarModalConfirmacao.value = false;
  if (!processoEditando.value) {
    notificacoesStore.erro(
      "Salve o processo",
      "Você precisa salvar o processo antes de poder iniciá-lo.",
    );
    return;
  }

  try {
    await processosStore.iniciarProcesso(
      processoEditando.value.codigo,
      tipo.value as TipoProcesso,
      unidadesSelecionadas.value,
    );
    notificacoesStore.sucesso(
      "Processo iniciado",
      "O processo foi iniciado! Notificações enviadas às unidades.",
    );
    await router.push("/painel");
    if (!processoEditando.value) {
      // Only clear fields if it was a new process
      limparCampos();
    }
  } catch (error) {
    notificacoesStore.erro(
      "Erro ao iniciar processo",
      "Não foi possível iniciar o processo. Tente novamente.",
    );
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
      notificacoesStore.adicionarNotificacao({
        tipo: "success",
        titulo: "Processo removido",
        mensagem: `${TEXTOS.PROCESSO_REMOVIDO_INICIO}${descricao.value}${TEXTOS.PROCESSO_REMOVIDO_FIM}`,
        testId: "notificacao-remocao",
      });
      await router.push("/painel");
      if (!processoEditando.value) {
        // Only clear fields if it was a new process
        limparCampos();
      }
    } catch (error) {
      notificacoesStore.erro(
        "Erro ao remover processo",
        "Não foi possível remover o processo. Tente novamente.",
      );
      console.error("Erro ao remover processo:", error);
    }
  }
  fecharModalRemocao();
}
</script>
