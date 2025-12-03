<template>
  <BModal
    :fade="false"
    :model-value="mostrar"
    title="Importação de atividades"
    size="lg"
    centered
    hide-footer
    @hide="fechar"
  >
    <BAlert
      v-if="erroImportacao"
      variant="danger"
      dismissible
      :model-value="true"
      :fade="false"
      @dismissed="limparErroImportacao"
    >
      {{ erroImportacao }}
    </BAlert>
    <fieldset :disabled="importando">
      <div class="mb-3">
        <label
          class="form-label"
          for="processo-select"
        >Processo</label>
        <BFormSelect
          id="processo-select"
          v-model="processoSelecionadoId"
          data-testid="select-processo"
          :options="processosDisponiveis"
          value-field="codigo"
          text-field="descricao"
        >
          <template #first>
            <BFormSelectOption
              disabled
              value=""
            >
              Selecione
            </BFormSelectOption>
          </template>
        </BFormSelect>
        <div
          v-if="!processosDisponiveis.length"
          class="text-center text-muted mt-3"
        >
          Nenhum processo disponível para importação.
        </div>
      </div>

      <div class="mb-3">
        <label
          class="form-label"
          for="unidade-select"
        >Unidade</label>
        <BFormSelect
          id="unidade-select"
          v-model="unidadeSelecionadaId"
          :disabled="!processoSelecionado"
          data-testid="select-unidade"
          :options="unidadesParticipantes"
          value-field="codUnidade"
          text-field="sigla"
        >
          <template #first>
            <BFormSelectOption
              disabled
              value=""
            >
              Selecione
            </BFormSelectOption>
          </template>
        </BFormSelect>
      </div>

      <div v-if="unidadeSelecionada">
        <h6>Atividades para importar</h6>
        <div
          v-if="atividadesParaImportar.length"
          class="atividades-container border rounded p-2"
        >
          <div
            v-for="ativ in atividadesParaImportar"
            :key="ativ.codigo"
            class="form-check"
          >
            <BFormCheckbox
              :id="`ativ-check-${ativ.codigo}`"
              v-model="atividadesSelecionadas"
              :value="ativ"
              :data-testid="`checkbox-atividade-${ativ.codigo}`"
            >
              {{ ativ.descricao }}
            </BFormCheckbox>
          </div>
        </div>
        <div
          v-else
          class="text-center text-muted mt-3"
        >
          Nenhuma atividade encontrada para esta unidade/processo.
        </div>
      </div>
    </fieldset>
    <template #footer>
      <BButton
        variant="outline-secondary"
        type="button"
        data-testid="importar-atividades-modal__btn-modal-cancelar"
        @click="fechar"
      >
        Cancelar
      </BButton>
      <BButton
        :disabled="!atividadesSelecionadas.length || importando"
        variant="outline-primary"
        type="button"
        data-testid="btn-importar"
        @click="importar"
      >
        <span
          v-if="importando"
          class="spinner-border spinner-border-sm"
          role="status"
          aria-hidden="true"
        />
        {{ importando ? 'Importando...' : 'Importar' }}
      </BButton>
    </template>
  </BModal>
</template>

<script lang="ts" setup>
import {BAlert, BButton, BFormCheckbox, BFormSelect, BFormSelectOption, BModal,} from "bootstrap-vue-next";
import {computed, onMounted, ref, watch} from "vue";
import {useApi} from "@/composables/useApi";
import {useAtividadesStore} from "@/stores/atividades";
import {useProcessosStore} from "@/stores/processos";
import {type Atividade, type ProcessoResumo, TipoProcesso, type UnidadeParticipante,} from "@/types/tipos";

const props = defineProps<{
  mostrar: boolean;
  codSubrocessoDestino: number | undefined;
}>();

const emit = defineEmits<{
  fechar: [];
  importar: [];
}>();

const processosStore = useProcessosStore();
const atividadesStore = useAtividadesStore();

const {
  execute: executarImportacao,
  error: erroImportacao,
  isLoading: importando,
  clearError: limparErroImportacao,
} = useApi(atividadesStore.importarAtividades);

const processoSelecionado = ref<ProcessoResumo | null>(null);
const processoSelecionadoId = ref<number | null>(null);
const unidadesParticipantes = ref<UnidadeParticipante[]>([]);
const unidadeSelecionada = ref<UnidadeParticipante | null>(null);
const unidadeSelecionadaId = ref<number | null>(null);
const atividadesParaImportar = ref<Atividade[]>([]);
const atividadesSelecionadas = ref<Atividade[]>([]);

const processosDisponiveis = computed<ProcessoResumo[]>(() => {
  return processosStore.processosPainel.filter(
      (p) =>
          (p.tipo === TipoProcesso.MAPEAMENTO || p.tipo === TipoProcesso.REVISAO) &&
          p.situacao === "FINALIZADO",
  );
});

onMounted(() => {
  processosStore.buscarProcessosPainel("ADMIN", 0, 0, 1000); // Usar um perfil e unidade genéricos para obter todos os processos
});

watch(
    () => props.mostrar,
    (mostrar) => {
      if (mostrar) {
        resetModal();
      }
    },
);

watch(processoSelecionadoId, async (newId) => {
  if (newId) {
    const processo = processosDisponiveis.value.find(
        (p) => p.codigo === Number(newId),
    );
    if (processo) {
      await selecionarProcesso(processo);
    }
  } else {
    await selecionarProcesso(null);
  }
});

watch(unidadeSelecionadaId, (newId) => {
  if (newId) {
    const unidade = unidadesParticipantes.value.find(
        (u) => u.codUnidade === Number(newId),
    );
    if (unidade) {
      selecionarUnidade(unidade);
    }
  } else {
    selecionarUnidade(null);
  }
});

function resetModal() {
  processoSelecionado.value = null;
  processoSelecionadoId.value = null;
  unidadesParticipantes.value = [];
  unidadeSelecionada.value = null;
  unidadeSelecionadaId.value = null;
  atividadesParaImportar.value = [];
  atividadesSelecionadas.value = [];
}

async function selecionarProcesso(processo: ProcessoResumo | null) {
  processoSelecionado.value = processo;
  if (processo) {
    await processosStore.buscarProcessoDetalhe(processo.codigo);
    unidadesParticipantes.value =
        processosStore.processoDetalhe?.unidades || [];
  } else {
    unidadesParticipantes.value = [];
  }
  unidadeSelecionada.value = null;
  unidadeSelecionadaId.value = null;
}

async function selecionarUnidade(unidadePu: UnidadeParticipante | null) {
  unidadeSelecionada.value = unidadePu;
  if (unidadePu) {
    await atividadesStore.buscarAtividadesParaSubprocesso(unidadePu.codSubprocesso);
    const atividadesDaOutraUnidade =
        atividadesStore.obterAtividadesPorSubprocesso(unidadePu.codSubprocesso);
    atividadesParaImportar.value = atividadesDaOutraUnidade
        ? [...atividadesDaOutraUnidade]
        : [];
  } else {
    atividadesParaImportar.value = [];
  }
}

function fechar() {
  emit("fechar");
}

async function importar() {
  limparErroImportacao();
  if (!props.codSubrocessoDestino || !unidadeSelecionada.value) {
    return;
  }
  if (atividadesSelecionadas.value.length === 0) {
    erroImportacao.value = "Selecione ao menos uma atividade para importar.";
    return;
  }

  const idsAtividades = atividadesSelecionadas.value.map((a) => a.codigo);

  try {
    await executarImportacao(
        props.codSubrocessoDestino,
        unidadeSelecionada.value.codSubprocesso,
        idsAtividades,
    );
    emit("importar");
    fechar();
  } catch {
    // O erro já está sendo tratado pelo useApi, não é necessário fazer nada aqui
    // a não ser que queira algum comportamento adicional no erro.
  }
}
</script>

<style scoped>
.atividades-container {
  max-height: 250px;
  overflow-y: auto;
}
</style>
