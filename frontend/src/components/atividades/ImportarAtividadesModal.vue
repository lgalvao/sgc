<template>
  <ModalPadrao
      v-model="mostrarComputado"
      :loading="importando"
      data-testid="mdl-importacao-atividades"
      size="lg"
      test-id-cancelar="importar-atividades-modal__btn-modal-cancelar"
      test-id-confirmar="btn-importar"
      texto-acao="Importar"
      texto-acao-carregando="Importando..."
      titulo="Importação de atividades"
      variant-acao="success"
      @confirmar="importar"
      @fechar="fechar"
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
        >Processo <span aria-hidden="true" class="text-danger">*</span></label>
        <BFormSelect
            id="processo-select"
            v-model="processoSelecionadoId"
            :options="processosParaImportacao"
            :state="mensagemErroProcesso ? false : null"
            data-testid="select-processo"
            text-field="descricao"
            value-field="codigo"
        >
          <template #first>
            <BFormSelectOption
                :value="null"
                disabled
            >
              Selecione
            </BFormSelectOption>
          </template>
        </BFormSelect>
        <BFormInvalidFeedback :state="mensagemErroProcesso ? false : null">
          {{ mensagemErroProcesso }}
        </BFormInvalidFeedback>
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
        >Unidade <span aria-hidden="true" class="text-danger">*</span></label>
        <BFormSelect
            id="unidade-select"
            v-model="unidadeSelecionadaId"
            :disabled="!processoSelecionado"
            :options="unidadesParticipantes"
            :state="mensagemErroUnidade ? false : null"
            data-testid="select-unidade"
            text-field="sigla"
            value-field="codUnidade"
        >
          <template #first>
            <BFormSelectOption
                :value="null"
                disabled
            >
              Selecione
            </BFormSelectOption>
          </template>
        </BFormSelect>
        <BFormInvalidFeedback :state="mensagemErroUnidade ? false : null">
          {{ mensagemErroUnidade }}
        </BFormInvalidFeedback>
      </div>

      <div v-if="unidadeSelecionada">
        <h6>Atividades para importar</h6>
        <div
            id="atividades-container"
            :class="['atividades-container border rounded p-2', { 'border-danger is-invalid': mensagemErroAtividades }]"
            tabindex="-1"
        >
          <div
              v-if="atividadesParaImportar.length"
          >
            <div class="d-flex gap-2 mb-2">
              <BButton
                  aria-label="Selecionar todas as atividades"
                  data-testid="btn-selecionar-todas-atividades"
                  size="sm"
                  variant="outline-secondary"
                  @click="selecionarTodasAtividades"
              >
                <i
                    aria-hidden="true"
                    class="bi bi-check-all me-1"
                />
                {{ TEXTOS.atividades.importacao.BOTAO_SELECIONAR_TODAS }}
              </BButton>
              <BButton
                  aria-label="Desmarcar todas as atividades"
                  data-testid="btn-limpar-selecao-atividades"
                  size="sm"
                  variant="outline-secondary"
                  @click="limparSelecaoAtividades"
              >
                <i
                    aria-hidden="true"
                    class="bi bi-x-lg me-1"
                />
                {{ TEXTOS.atividades.importacao.BOTAO_LIMPAR_SELECAO }}
              </BButton>
            </div>
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
        <div v-if="mensagemErroAtividades" class="text-danger small mt-1">
          {{ mensagemErroAtividades }}
        </div>
      </div>
    </fieldset>
  </ModalPadrao>
</template>

<script lang="ts" setup>
import {BAlert, BButton, BFormCheckbox, BFormInvalidFeedback, BFormSelect, BFormSelectOption} from "bootstrap-vue-next";
import {computed, toRef} from "vue";
import ModalPadrao from "@/components/comum/ModalPadrao.vue";
import {TEXTOS} from "@/constants/textos";
import {useImportarAtividadesTela} from "@/composables/useImportarAtividadesTela";
import type {AtividadeOperacaoResponse} from "@/types/tipos";

const props = defineProps<{
  mostrar: boolean;
  codSubprocessoDestino: number | null | undefined;
}>();

const emit = defineEmits<{
  fechar: [];
  importar: [resultado: AtividadeOperacaoResponse];
}>();

const {
  erroImportacao,
  importando,
  processosParaImportacao,
  processoSelecionado,
  processoSelecionadoId,
  unidadesParticipantes,
  unidadeSelecionada,
  unidadeSelecionadaId,
  atividadesParaImportar,
  atividadesSelecionadas,
  mensagemErroProcesso,
  mensagemErroUnidade,
  mensagemErroAtividades,
  limparErroImportacao,
  selecionarTodasAtividades,
  limparSelecaoAtividades,
  importar,
} = useImportarAtividadesTela(
    toRef(props, "mostrar"),
    toRef(props, "codSubprocessoDestino"),
    () => emit("fechar"),
    (resultado) => emit("importar", resultado),
);

const mostrarComputado = computed({
  get: () => props.mostrar,
  set: (mostrar: boolean) => {
    if (!mostrar) emit("fechar");
  }
});

function fechar() {
  emit("fechar");
}
</script>

<style scoped>
.atividades-container {
  max-height: 250px;
  overflow-y: auto;
}
</style>
