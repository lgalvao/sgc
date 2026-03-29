<template>
  <BModal
      :fade="false"
      :model-value="mostrar"
      centered
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
            :options="processosParaImportacao"
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
            v-if="!processosParaImportacao.length"
            class="text-center text-muted mt-3"
        >
          {{ TEXTOS.atividades.importacao.NENHUM_PROCESSO }}
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
          {{ TEXTOS.atividades.importacao.NENHUMA_ATIVIDADE }}
        </div>
      </div>
    </fieldset>
    <template #footer>
      <div class="d-flex justify-content-end w-100 gap-3 align-items-center">
        <BButton
            :disabled="importando"
            class="text-decoration-none text-secondary fw-medium btn-cancelar-link"
            data-testid="importar-atividades-modal__btn-modal-cancelar"
            type="button"
            variant="link"
            @click="fechar"
        >
          <i aria-hidden="true" class="bi bi-x-circle me-1"/>
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
              class="me-1"
              small
          />
          <i v-else aria-hidden="true" class="bi bi-box-arrow-in-down me-1"/>
          {{ importando ? 'Importando...' : 'Importar' }}
        </BButton>
      </div>
    </template>
  </BModal>
</template>

<script lang="ts" setup>
import {BAlert, BButton, BFormCheckbox, BFormSelect, BFormSelectOption, BModal, BSpinner} from "bootstrap-vue-next";
import {ref, watch} from "vue";
import * as processoService from "@/services/processoService";
import * as subprocessoService from "@/services/subprocessoService";
import {type Atividade, type ProcessoResumo, type UnidadeImportacao,} from "@/types/tipos";
import {TEXTOS} from "@/constants/textos";
import {normalizeError} from "@/utils/apiError";

const props = defineProps<{
  mostrar: boolean;
  codSubprocessoDestino: number | null | undefined;
}>();

const emit = defineEmits<{
  fechar: [];
  importar: [aviso?: string];
}>();

const resultadoImportacao = ref<{aviso?: string} | null>(null);
const erroImportacao = ref<string | null>(null);
const importando = ref(false);
const processosParaImportacao = ref<ProcessoResumo[]>([]);

const processoSelecionado = ref<ProcessoResumo | null>(null);
const processoSelecionadoId = ref<number | null>(null);
const unidadesParticipantes = ref<UnidadeImportacao[]>([]);
const unidadeSelecionada = ref<UnidadeImportacao | null>(null);
const unidadeSelecionadaId = ref<number | null>(null);
const atividadesParaImportar = ref<Atividade[]>([]);
const atividadesSelecionadas = ref<Atividade[]>([]);

watch(
    () => props.mostrar,
    async (mostrar) => {
      if (mostrar) {
        resetModal();
        processosParaImportacao.value = await processoService.buscarProcessosParaImportacao() ?? [];
      }
    },
    {immediate: true},
);

watch(processoSelecionadoId, async (newId) => {
  if (newId) {
    const processo = processosParaImportacao.value.find(
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

function limparErroImportacao() {
  erroImportacao.value = null;
}

async function selecionarProcesso(processo: ProcessoResumo | null) {
  processoSelecionado.value = processo;
  atividadesSelecionadas.value = [];
  if (processo) {
    unidadesParticipantes.value = await processoService.buscarUnidadesParaImportacao(processo.codigo);
  } else {
    unidadesParticipantes.value = [];
  }
  unidadeSelecionada.value = null;
  unidadeSelecionadaId.value = null;
}

async function selecionarUnidade(unidadePu: UnidadeImportacao | null) {
  atividadesSelecionadas.value = [];
  unidadeSelecionada.value = unidadePu;
  if (unidadePu) {
    try {
      const atividadesDaOutraUnidade = await subprocessoService.listarAtividadesParaImportacao(unidadePu.codSubprocesso);
      atividadesParaImportar.value = atividadesDaOutraUnidade ? [...atividadesDaOutraUnidade] : [];
    } catch {
      atividadesParaImportar.value = [];
    }
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
    erroImportacao.value = TEXTOS.atividades.importacao.SELECIONE_ATIVIDADE;
    return;
  }

  const idsAtividades = atividadesSelecionadas.value.map((a) => a.codigo);

  try {
    importando.value = true;
    resultadoImportacao.value = await subprocessoService.importarAtividades(
      props.codSubprocessoDestino,
      unidadeSelecionada.value.codSubprocesso,
      idsAtividades,
    );
    emit("importar", resultadoImportacao.value?.aviso);
    fechar();
  } catch (erro) {
    erroImportacao.value = normalizeError(erro).message;
  } finally {
    importando.value = false;
  }
}
</script>

<style scoped>
.atividades-container {
  max-height: 250px;
  overflow-y: auto;
}

.btn-cancelar-link {
  padding: 0.375rem 0.75rem;
  transition: all 0.2s;
  border-radius: 0.375rem;
}

.btn-cancelar-link:hover {
  color: var(--bs-emphasis-color) !important;
  background-color: var(--bs-secondary-bg);
}
</style>
