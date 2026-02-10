<template>
  <BModal
      :fade="false"
      :model-value="mostrar"
      centered
      hide-footer
      size="lg"
      title="Importação de atividades"
      @hide="fechar"
  >
    <BAlert
        v-if="erroImportacao"
        :fade="false"
        :model-value="true"
        dismissible
        variant="danger"
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
            :options="processosStore.processosFinalizados"
            data-testid="select-processo"
            text-field="descricao"
            value-field="codigo"
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
            v-if="!processosStore.processosFinalizados.length"
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
            :options="unidadesParticipantes"
            data-testid="select-unidade"
            text-field="sigla"
            value-field="codUnidade"
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
                :data-testid="`checkbox-atividade-${ativ.codigo}`"
                :value="ativ"
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
          data-testid="importar-atividades-modal__btn-modal-cancelar"
          type="button"
          variant="secondary"
          :disabled="importando"
          @click="fechar"
      >
        <i class="bi bi-x-circle me-1" aria-hidden="true"/>
        Cancelar
      </BButton>
      <BButton
          :disabled="!atividadesSelecionadas.length || importando"
          data-testid="btn-importar"
          type="button"
          variant="primary"
          @click="importar"
      >
        <BSpinner
            v-if="importando"
            small
            class="me-1"
        />
        <i v-else class="bi bi-box-arrow-in-down me-1" aria-hidden="true"/>
        {{ importando ? 'Importando...' : 'Importar' }}
      </BButton>
    </template>
  </BModal>
</template>

<script lang="ts" setup>
import {BAlert, BButton, BFormCheckbox, BFormSelect, BFormSelectOption, BModal, BSpinner} from "bootstrap-vue-next";
import {onMounted, ref, watch} from "vue";
import {useApi} from "@/composables/useApi";
import {useAtividadesStore} from "@/stores/atividades";
import {useProcessosStore} from "@/stores/processos";
import {type Atividade, type ProcessoResumo, type UnidadeParticipante,} from "@/types/tipos";

const props = defineProps<{
  mostrar: boolean;
  codSubprocessoDestino: number | null | undefined;
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

onMounted(() => {
  // A busca é feita ao abrir o modal
});

watch(
    () => props.mostrar,
    (mostrar) => {
      if (mostrar) {
        resetModal();
        processosStore.buscarProcessosFinalizados();
      }
    },
);

watch(processoSelecionadoId, async (newId) => {
  if (newId) {
    const processo = processosStore.processosFinalizados.find(
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
  if (!props.codSubprocessoDestino || !unidadeSelecionada.value) {
    return;
  }
  if (atividadesSelecionadas.value.length === 0) {
    erroImportacao.value = "Selecione ao menos uma atividade para importar.";
    return;
  }

  const idsAtividades = atividadesSelecionadas.value.map((a) => a.codigo);

  try {
    await executarImportacao(
        props.codSubprocessoDestino,
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
