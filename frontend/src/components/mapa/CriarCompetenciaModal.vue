<template>
  <BModal
      :fade="false"
      :model-value="mostrar"
      :title="competenciaSendoEditada ? 'Edição de competência' : 'Criação de competência'"
      centered
      data-testid="mdl-criar-competencia"
      size="lg"
      @hide="fechar"
  >
    <BAlert v-if="fieldErrors?.generic" :model-value="true" variant="danger" class="mb-4">
      {{ fieldErrors.generic }}
    </BAlert>

    <div class="mb-4">
      <h5>Descrição</h5>
      <div class="mb-2">
        <BFormTextarea
            ref="inputDescricaoRef"
            v-model="novaCompetencia.descricao"
            :state="fieldErrors?.descricao ? false : null"
            data-testid="inp-criar-competencia-descricao"
            placeholder="Descreva a competência"
            rows="3"
        />
        <BFormInvalidFeedback :state="fieldErrors?.descricao ? false : null">
          {{ fieldErrors?.descricao }}
        </BFormInvalidFeedback>
      </div>
    </div>

    <div class="mb-4">
      <h5>Atividades</h5>
      <div
          :class="{ 'border-danger': fieldErrors?.atividades }"
          class="d-flex flex-wrap gap-2 p-2 border rounded"
      >
        <BCard
            v-for="atividade in atividades"
            :key="atividade.codigo"
            :class="atividadesSelecionadas.includes(atividade.codigo) ? 'atividade-card-item checked' : 'atividade-card-item'"
            :data-testid="atividadesSelecionadas.includes(atividade.codigo) ? 'atividade-associada' : 'atividade-nao-associada'"
            no-body
        >
          <BCardBody class="d-flex align-items-center">
            <BFormCheckbox
                :id="`atv-${atividade.codigo}`"
                v-model="atividadesSelecionadas"
                :value="atividade.codigo"
                data-testid="chk-criar-competencia-atividade"
            >
              {{ atividade.descricao }}
              <BBadge
                  v-if="atividade.conhecimentos.length > 0"
                  v-b-tooltip.html.right="getConhecimentosModal(atividade)"
                  variant="secondary"
                  class="ms-2"
                  data-testid="cad-mapa__txt-badge-conhecimentos-2"
              >
                {{ atividade.conhecimentos.length }}
              </BBadge>
            </BFormCheckbox>
          </BCardBody>
        </BCard>
      </div>
      <div v-if="fieldErrors?.atividades" class="text-danger small mt-1">
        {{ fieldErrors.atividades }}
      </div>
    </div>

    <template #footer>
      <div class="d-flex justify-content-end w-100 gap-3 align-items-center">
        <BButton
            class="text-decoration-none text-secondary fw-medium btn-cancelar-link"
            data-testid="btn-criar-competencia-cancelar"
            variant="link"
            @click="fechar"
        >
          Cancelar
        </BButton>
        <BButton
            v-b-tooltip.hover
            :disabled="atividadesSelecionadas.length === 0 || !novaCompetencia.descricao"
            data-testid="btn-criar-competencia-salvar"
            title="Criar Competência"
            variant="primary"
            @click="salvar"
        >
          <i aria-hidden="true" class="bi bi-save"/> Salvar
        </BButton>
      </div>
    </template>
  </BModal>
</template>

<script lang="ts" setup>
import {
  BButton,
  BCard,
  BCardBody,
  BFormCheckbox,
  BFormInvalidFeedback,
  BFormTextarea,
  BModal,
  BAlert,
  BBadge,
} from "bootstrap-vue-next";
import {nextTick, ref, watch} from "vue";
import type {Atividade, Competencia} from "@/types/tipos";

const props = defineProps<{
  mostrar: boolean;
  atividades: Atividade[];
  competenciaParaEditar?: Competencia | null;
  fieldErrors?: {
    descricao?: string;
    atividades?: string;
    generic?: string;
  };
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
const inputDescricaoRef = ref<InstanceType<typeof BFormTextarea> | null>(null);

watch(
    () => props.mostrar,
    (mostrar) => {
      if (mostrar) {
        if (props.competenciaParaEditar) {
          novaCompetencia.value.descricao = props.competenciaParaEditar.descricao;
          atividadesSelecionadas.value = [
            ...(props.competenciaParaEditar.atividades?.map(a => a.codigo) || []),
          ];
          competenciaSendoEditada.value = props.competenciaParaEditar;
        } else {
          novaCompetencia.value.descricao = "";
          atividadesSelecionadas.value = [];
          competenciaSendoEditada.value = null;
        }
        nextTick(() => {
          inputDescricaoRef.value?.$el?.focus();
        });
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
  box-shadow: 0 0 0 0.25rem var(--bs-primary);
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