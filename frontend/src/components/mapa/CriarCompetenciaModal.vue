<template>
  <ModalPadrao
      v-model="mostrarComputado"
      :acao-desabilitada="salvamentoDesabilitado"
      data-testid="mdl-criar-competencia"
      tamanho="lg"
      test-id-cancelar="btn-criar-competencia-cancelar"
      test-id-confirmar="btn-criar-competencia-salvar"
      texto-acao="Salvar"
      :titulo="competenciaSendoEditada ? 'Edição de competência' : 'Criação de competência'"
      @confirmar="salvar"
      @fechar="fechar"
      @shown="focarDescricao"
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

  </ModalPadrao>
</template>

<script lang="ts" setup>
import {
  BCard,
  BCardBody,
  BFormCheckbox,
  BFormInvalidFeedback,
  BFormTextarea,
  BAlert,
  BBadge,
} from "bootstrap-vue-next";
import {computed, nextTick, ref, watch} from "vue";
import ModalPadrao from "@/components/comum/ModalPadrao.vue";
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

const mostrarComputado = computed({
  get: () => props.mostrar,
  set: (mostrar: boolean) => {
    if (!mostrar) emit("fechar");
  }
});

const salvamentoDesabilitado = computed(() => {
  return atividadesSelecionadas.value.length === 0 || !novaCompetencia.value.descricao;
});

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
      }
    },
    {immediate: true},
);

function focarDescricao() {
  nextTick(() => {
    inputDescricaoRef.value?.$el?.focus();
  });
}

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

</style>
