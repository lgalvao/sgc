<template>
  <div v-if="exibir" class="mb-3 pt-2">
    <BAlert
        :model-value="true"
        data-testid="alert-diagnostico-organizacional"
        dismissible
        :variant="carregando ? 'info' : 'warning'"
        @dismissed="$emit('dismiss')"
    >
      <div class="d-flex align-items-start gap-2">
        <BSpinner v-if="carregando" class="mt-1" small/>
        <i v-else class="bi bi-exclamation-triangle-fill fs-5 mt-1"></i>
        <div>
          <strong v-if="carregando">Validando informações organizacionais...</strong>
          <template v-else-if="unidadesSemResponsavel.length > 0">
            <div class="mt-1">
              {{ prefixoMensagemUnidadesSemResponsavel }}
              <template v-for="(unidade, indice) in unidadesSemResponsavel" :key="unidade.sigla">
                <span v-if="indice > 0">{{ separadorListaUnidades(indice, unidadesSemResponsavel.length) }}</span>
                <RouterLink
                    v-if="unidade.codigo !== null"
                    :to="`/unidade/${unidade.codigo}`"
                    :data-testid="`link-unidade-sem-responsavel-${indice}`"
                >
                  <strong>{{ unidade.sigla }}</strong>
                </RouterLink>
                <strong v-else>{{ unidade.sigla }}</strong>
              </template>
              {{ sufixoMensagemUnidadesSemResponsavel }} A responsabilidade
              deve ser definida externamente, no SGRH, ou por atribuição temporária no próprio sistema.
            </div>
          </template>
          <template v-else>
            <strong>Há unidades sem responsável atual.</strong>
            <div class="mt-1">{{ resumo }}</div>
          </template>
          <ul v-if="!carregando && unidadesSemResponsavel.length === 0" class="mb-0 mt-2 ps-3 small">
            <li v-for="grupo in grupos" :key="grupo.tipo">
              {{ grupo.tipo }}: {{ grupo.quantidadeOcorrencias }} ocorrência(s)
            </li>
          </ul>
        </div>
      </div>
    </BAlert>
  </div>
</template>

<script lang="ts" setup>
import {BAlert, BSpinner} from "bootstrap-vue-next";
import {computed} from "vue";
import {RouterLink} from "vue-router";

interface GrupoDiagnostico {
  tipo: string;
  quantidadeOcorrencias: number;
}

interface UnidadeSemResponsavel {
  codigo: number | null;
  sigla: string;
}

const props = defineProps<{
  carregando?: boolean;
  exibir: boolean;
  resumo: string;
  grupos: GrupoDiagnostico[];
  unidadesSemResponsavel?: UnidadeSemResponsavel[];
}>();

defineEmits<{
  dismiss: [];
}>();

const unidadesSemResponsavel = computed(() => props.unidadesSemResponsavel ?? []);
const prefixoMensagemUnidadesSemResponsavel = computed(() =>
    unidadesSemResponsavel.value.length > 1 ? "As unidades " : "A unidade "
);
const sufixoMensagemUnidadesSemResponsavel = computed(() =>
    unidadesSemResponsavel.value.length > 1
        ? " estão atualmente sem responsável. Enquanto isso, não poderão participar de processos."
        : " está atualmente sem responsável. Enquanto isso, não poderá participar de processos."
);

function separadorListaUnidades(indice: number, total: number): string {
  if (indice === total - 1) {
    return total === 2 ? " e " : ", e ";
  }

  return ", ";
}
</script>

<style scoped>
a {
  color: #0056b3;
  text-decoration: underline;
}
a:hover {
  color: #004085;
}
</style>
