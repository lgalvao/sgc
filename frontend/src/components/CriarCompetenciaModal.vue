<template>
  <BModal
      :fade="false"
      :model-value="mostrar"
      :title="competenciaSendoEditada ? 'Edição de competência' : 'Criação de competência'"
      centered
      hide-footer
      size="lg"
      @hide="fechar"
  >
    <div class="mb-4">
      <h5>Descrição</h5>
      <div class="mb-2">
        <BFormTextarea
            v-model="novaCompetencia.descricao"
            data-testid="input-descricao-competencia"
            placeholder="Descreva a competência"
            rows="3"
        />
      </div>
    </div>

    <div class="mb-4">
      <h5>Atividades</h5>
      <div class="d-flex flex-wrap gap-2">
        <BCard
            v-for="atividade in atividades"
            :key="atividade.codigo"
            :class="atividadesSelecionadas.includes(atividade.codigo) ? 'atividade-card-item checked' : 'atividade-card-item'"
            no-body
        >
          <BCardBody class="d-flex align-items-center py-2">
            <BFormCheckbox
                :id="`atv-${atividade.codigo}`"
                v-model="atividadesSelecionadas"
                :data-testid="`chk-atividade-${atividade.codigo}`"
                :value="atividade.codigo"
                class="form-check-input me-2"
            >
              {{ atividade.descricao }}
              <span
                  v-if="atividade.conhecimentos.length > 0"
                  :data-bs-html="true"
                  :data-bs-title="getConhecimentosModal(atividade)"
                  class="badge bg-secondary ms-2"
                  data-bs-custom-class="conhecimentos-tooltip"
                  data-bs-placement="right"
                  data-bs-toggle="tooltip"
              >
                {{ atividade.conhecimentos.length }}
              </span>
            </BFormCheckbox>
          </BCardBody>
        </BCard>
      </div>
    </div>

    <template #footer>
      <BButton
          data-testid="criar-competencia-modal__btn-modal-cancelar"
          variant="secondary"
          @click="fechar"
      >
        Cancelar
      </BButton>
      <BButton
          :disabled="atividadesSelecionadas.length === 0 || !novaCompetencia.descricao"
          data-testid="criar-competencia-modal__btn-modal-confirmar"
          variant="primary"
          @click="salvar"
      >
        <i class="bi bi-save"/> Salvar
      </BButton>
    </template>
  </BModal>
</template>

<script lang="ts" setup>
import {BButton, BCard, BCardBody, BFormCheckbox, BFormTextarea, BModal,} from "bootstrap-vue-next";
import {ref, watch} from "vue";
import type {Atividade, Competencia} from "@/types/tipos";

const props = defineProps<{
  mostrar: boolean;
  atividades: Atividade[];
  competenciaParaEditar?: Competencia | null;
}>();

const emit = defineEmits<{
  fechar: [];
  salvar: [
    competencia: { descricao: string; atividadesSelecionadas: number[] },
  ];
}>();

const novaCompetencia = ref({descricao: ""});
const atividadesSelecionadas = ref<number[]>([]);
const competenciaSendoEditada = ref<Competencia | null>(null);

watch(
    () => props.mostrar,
    (mostrar) => {
      if (mostrar) {
        if (props.competenciaParaEditar) {
          novaCompetencia.value.descricao = props.competenciaParaEditar.descricao;
          atividadesSelecionadas.value = [
            ...((props.competenciaParaEditar.atividadesAssociadas) || []),
          ];
          competenciaSendoEditada.value = props.competenciaParaEditar;
        } else {
          novaCompetencia.value.descricao = "";
          atividadesSelecionadas.value = [];
          competenciaSendoEditada.value = null;
        }
      }
    },
    {immediate: true},
);

function getConhecimentosModal(atividade: Atividade): string {
  if (!atividade.conhecimentos.length) {
    return "Nenhum conhecimento";
  }

  const conhecimentosHtml = atividade.conhecimentos
      .map((c) => `<div class="mb-1">• ${c.descricao}</div>`)
      .join("");

  return `<div class="text-start"><strong>Conhecimentos:</strong><br>${conhecimentosHtml}</div>`;
}

function fechar() {
  emit("fechar");
}

function salvar() {
  emit("salvar", {
    descricao: novaCompetencia.value.descricao,
    atividadesSelecionadas: atividadesSelecionadas.value,
  });
}
</script>

<style scoped>
.atividade-card-item {
  cursor: pointer;
  border: 1px solid var(--bs-border-color);
  border-radius: 0.375rem;
  transition: all 0.2s ease-in-out;
  background-color: var(--bs-body-bg);
}

.atividade-card-item:hover {
  border-color: var(--bs-primary);
  box-shadow: 0 0 0 0.25rem rgba(var(--bs-primary-rgb), 0.25);
}

.atividade-card-item.checked {
  background-color: var(--bs-primary-bg-subtle);
  border-color: var(--bs-primary);
}

.atividade-card-item .form-check-label {
  cursor: pointer;
  padding: 0.25rem 0;
}

.atividade-card-item.checked .form-check-label {
  font-weight: bold;
  color: var(--bs-primary);
}

.atividade-card-item .card-body {
  padding: 0.5rem 0.75rem;
}
</style>
