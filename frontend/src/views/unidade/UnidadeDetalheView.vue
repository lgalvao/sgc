<template>
  <LayoutPadrao>
    <ErrorAlert
        :error="unidadesStore.lastError"
        @dismiss="unidadesStore.clearError()"
    />

    <div v-if="unidadeComResponsavelDinamico">
      <PageHeader :title="`${unidadeComResponsavelDinamico.sigla} - ${unidadeComResponsavelDinamico.nome}`">
        <template #actions>
          <BButton
              v-if="mapaVigente"
              data-testid="btn-mapa-vigente"
              variant="outline-success"
              @click="visualizarMapa"
          >
            <i
                class="bi bi-file-earmark-spreadsheet me-2"
            />Mapa vigente
          </BButton>
          <BButton
              v-if="perfilStore.perfilSelecionado === 'ADMIN'"
              class="ms-2"
              data-testid="unidade-view__btn-criar-atribuicao"
              variant="outline-primary"
              @click="irParaCriarAtribuicao"
          >
            Criar atribuição
          </BButton>
        </template>
      </PageHeader>

      <UnidadeInfoCard
          :unidade="unidadeComResponsavelDinamico"
          :titular-detalhes="titularDetalhes"
      />
    </div>
    <EmptyState
        v-else
        icon="bi-building"
        title="Unidade não encontrada"
        description="Não foi possível localizar os dados da unidade solicitada."
    />

    <div
        v-if="unidadeComResponsavelDinamico && unidadeComResponsavelDinamico.filhas && unidadeComResponsavelDinamico.filhas.length > 0"
        class="mt-5"
    >
      <TreeTable
          :columns="colunasTabela"
          :data="dadosFormatadosSubordinadas"
          :hide-headers="true"
          title="Unidades Subordinadas"
          @row-click="navegarParaUnidadeSubordinada"
      />
    </div>
  </LayoutPadrao>
</template>

<script lang="ts" setup>
import {BButton} from "bootstrap-vue-next";
import LayoutPadrao from '@/components/layout/LayoutPadrao.vue';
import {computed} from "vue";
import type {Unidade} from "@/types/tipos";
import TreeTable from "@/components/comum/TreeTable.vue";
import PageHeader from "@/components/layout/PageHeader.vue";
import UnidadeInfoCard from "@/components/unidade/UnidadeInfoCard.vue";
import ErrorAlert from "@/components/comum/ErrorAlert.vue";
import EmptyState from "@/components/comum/EmptyState.vue";
import {useUnidadeView} from "@/composables/useUnidadeView";

const props = defineProps<{ codUnidade: number }>();

const {
  unidadesStore,
  perfilStore,
  unidadeComResponsavelDinamico,
  titularDetalhes,
  mapaVigente,
  irParaCriarAtribuicao,
  navegarParaUnidadeSubordinada,
  visualizarMapa
} = useUnidadeView(props.codUnidade);

const colunasTabela = [{key: "nome", label: "Unidade"}];

const dadosFormatadosSubordinadas = computed(() => {
  if (
      !unidadeComResponsavelDinamico.value ||
      !unidadeComResponsavelDinamico.value.filhas ||
      unidadeComResponsavelDinamico.value.filhas.length === 0
  )
    return [];
  return formatarDadosParaArvore(unidadeComResponsavelDinamico.value.filhas);
});

interface UnidadeFormatada {
  codigo: number;
  nome: string;
  expanded: boolean;
  children?: UnidadeFormatada[];
}

function formatarDadosParaArvore(dados: Unidade[]): UnidadeFormatada[] {
  if (!dados) return [];

  return dados.map((item) => {
    const children = item.filhas ? formatarDadosParaArvore(item.filhas) : [];
    return {
      codigo: item.codigo,
      nome: item.sigla + " - " + item.nome,
      expanded: true,
      ...(children.length > 0 && {children})};
  });
}
</script>
