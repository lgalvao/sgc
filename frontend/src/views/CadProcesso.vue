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
        data-testid="btn-salvar"
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
        data-testid="btn-remover"
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
          data-testid="btn-modal-cancelar"
          @click="fecharModalConfirmacao"
        >
          Cancelar
        </BButton>
        <BButton
          variant="primary"
          data-testid="btn-modal-confirmar"
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
import {BButton, BContainer, BForm, BFormGroup, BFormInput, BFormSelect, BModal, useToast,} from "bootstrap-vue-next";
import {nextTick, onMounted, ref, watch} from "vue";
import {useRoute, useRouter} from "vue-router";
import ArvoreUnidades from "@/components/ArvoreUnidades.vue";
// import {TEXTOS} from "@/constants"; // Removed
import * as processoService from "@/services/processoService";

import {useProcessosStore} from "@/stores/processos";
import {useUnidadesStore} from "@/stores/unidades";
import {AtualizarProcessoRequest, CriarProcessoRequest, Processo as ProcessoModel, TipoProcesso, Unidade} from "@/types/tipos";

const TipoProcessoEnum = TipoProcesso;

const unidadesSelecionadas = ref<number[]>([]);
const descricao = ref<string>("");
const tipo = ref<string>("MAPEAMENTO");
const dataLimite = ref<string>("");
const router = useRouter();
const route = useRoute();
const processosStore = useProcessosStore();
const unidadesStore = useUnidadesStore();
const toast = useToast(); // Instantiate toast

const mostrarModalConfirmacao = ref(false);
const mostrarModalRemocao = ref(false);
const processoEditando = ref<ProcessoModel | null>(null);

onMounted(async () => {
  const codProcesso = route.query.codProcesso;
  if (codProcesso) {
    try {
      await processosStore.buscarProcessoDetalhe(Number(codProcesso));
      const processo = processosStore.processoDetalhe;
      if (processo) {
        // Redirect if process is not in CRIADO state (cannot be edited)
        if (processo.situacao !== 'CRIADO') {
          toast.show({
            title: "Processo em andamento",
            body: "Este processo já foi iniciado e não pode ser editado.",
            props: { variant: 'warning', value: true },
          });
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
      toast.show({
        title: "Erro ao carregar processo",
        body: "Não foi possível carregar os detalhes do processo.",
        props: { variant: 'danger', value: true },
      });
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


// Helper para buscar unidade na árvore
function findUnidadeById(codigo: number, nodes: Unidade[]): Unidade | null {
  for (const node of nodes) {
    if (node.codigo === codigo) return node;
    if (node.filhas) {
      const found = findUnidadeById(codigo, node.filhas);
      if (found) return found;
    }
  }
  return null;
}

async function salvarProcesso() {
  if (!descricao.value) {
    toast.show({
      title: "Dados incompletos",
      body: "Preencha a descrição.",
      props: { variant: 'danger', value: true },
    });
    console.log("Validation error in salvarProcesso: Preencha a descrição.");
    return;
  }

  // Filtrar apenas unidades elegíveis
  const unidadesFiltradas = unidadesSelecionadas.value.filter(id => {
    const unidade = findUnidadeById(id, unidadesStore.unidades);
    return unidade && unidade.isElegivel;
  });

  if (unidadesFiltradas.length === 0) {
    toast.show({
      title: "Dados incompletos",
      body: "Pelo menos uma unidade participante elegível deve ser incluída.",
      props: { variant: 'danger', value: true },
    });
    return;
  }

  if (!dataLimite.value) {
    toast.show({
      title: "Dados incompletos",
      body: "Preencha a data limite.",
      props: { variant: 'danger', value: true },
    });
    return;
  }

  try {
    if (processoEditando.value) {
      const request: AtualizarProcessoRequest = {
        codigo: processoEditando.value.codigo,
        descricao: descricao.value,
        tipo: tipo.value as TipoProcesso,
        dataLimiteEtapa1: `${dataLimite.value}T00:00:00`,
        unidades: unidadesFiltradas,
      };
      await processosStore.atualizarProcesso(
        processoEditando.value.codigo,
        request,
      );
      toast.show({
        title: "Processo alterado",
        body: "O processo foi alterado!",
        props: { variant: 'success', value: true },
      });
      await router.push("/painel");
    } else {
      const request: CriarProcessoRequest = {
        descricao: descricao.value,
        tipo: tipo.value as TipoProcesso,
        dataLimiteEtapa1: `${dataLimite.value}T00:00:00`,
        unidades: unidadesFiltradas,
      };
      await processosStore.criarProcesso(request);
      toast.show({
        title: "Processo criado",
        body: "O processo foi criado!",
        props: { variant: 'success', value: true },
      });
      await router.push("/painel");
    }
    limparCampos();
  } catch (error) {
    toast.show({
      title: "Erro ao salvar processo",
      body: "Não foi possível salvar o processo. Verifique os dados e tente novamente.",
      props: { variant: 'danger', value: true },
    });
    console.error("Erro ao salvar processo:", error);
  }
}

async function abrirModalConfirmacao() {
  if (!descricao.value) {
    toast.show({
      title: "Dados incompletos",
      body: "Preencha a descrição.",
      props: { variant: 'danger', value: true },
    });
    return;
  }
  if (unidadesSelecionadas.value.length === 0) {
    toast.show({
      title: "Dados incompletos",
      body: "Pelo menos uma unidade participante deve ser incluída.",
      props: { variant: 'danger', value: true },
    });
    return;
  }
  if (!dataLimite.value) {
    toast.show({
      title: "Dados incompletos",
      body: "Preencha a data limite.",
      props: { variant: 'danger', value: true },
    });
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
    toast.show({
      title: "Salve o processo",
      body: "Você precisa salvar o processo antes de poder iniciá-lo.",
      props: { variant: 'danger', value: true },
    });
    return;
  }

  try {
    await processosStore.iniciarProcesso(
      processoEditando.value.codigo,
      tipo.value as TipoProcesso,
      unidadesSelecionadas.value,
    );
    toast.show({
      title: "Processo iniciado!",
      body: "O processo foi iniciado! Notificações enviadas às unidades.",
      props: { variant: 'success', value: true },
    });
    await router.push("/painel");
    if (!processoEditando.value) {
      // Only clear fields if it was a new process
      limparCampos();
    }
  } catch (error) {
    toast.show({
      title: "Erro ao iniciar processo",
      body: "Não foi possível iniciar o processo. Tente novamente.",
      props: { variant: 'danger', value: true },
    });
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
      toast.show({
        title: "Processo removido",
        body: `Processo ${descricao.value} removido com sucesso.`,
        props: { variant: 'success', value: true },
      });
      await router.push("/painel");
      if (!processoEditando.value) {
        // Only clear fields if it was a new process
        limparCampos();
      }
    } catch (error) {
      toast.show({
        title: "Erro ao remover processo",
        body: "Não foi possível remover o processo. Tente novamente.",
        props: { variant: 'danger', value: true },
      });
      console.error("Erro ao remover processo:", error);
    }
  }
  fecharModalRemocao();
}
</script>
