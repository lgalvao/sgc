<template>
  <BModal
      :fade="false"
      :model-value="mostrar"
      centered
      data-testid="mdl-historico-analise"
      size="lg"
      title="Histórico de análise"
      @hide="fechar"
  >
    <div data-testid="modal-historico-body">
      <div v-if="loading" class="text-center py-4">
        <BSpinner label="Carregando dados" variant="primary"/>
      </div>
      <template v-else>
        <BAlert
            v-if="historico.length === 0"
            :fade="false"
            :model-value="true"
            data-testid="alert-historico-vazio"
            variant="secondary"
        >
          Nenhuma análise registrada para este mapa.
        </BAlert>
        <div v-else>
          <BTable
              :fields="fields"
              :items="historico"
              hover
              responsive
              striped
          >
            <template #head(dataHora)>
              <span data-testid="header-historico-dataHora">Data/Hora</span>
            </template>
            <template #head(unidadeSigla)>
              <span data-testid="header-historico-unidade">Unidade</span>
            </template>
            <template #head(acao)>
              <span data-testid="header-historico-acao">Ação</span>
            </template>
            <template #head(usuarioNome)>
              <span data-testid="header-historico-usuario">Usuário</span>
            </template>
            <template #head(observacoes)>
              <span data-testid="header-historico-observacao">Observação</span>
            </template>
            <template #cell(dataHora)="{ item, index }">
              <span :data-testid="`cell-dataHora-${index}`">
                {{ formatarDataHoraBR((item as Analise).dataHora) }}
              </span>
            </template>
            <template #cell(unidadeSigla)="{ item, index }">
              <span :data-testid="`cell-unidade-${index}`" :title="(item as Analise).unidadeNome">
                {{ (item as Analise).unidadeSigla }}
              </span>
            </template>
            <template #cell(acao)="{ item, index }">
              <span :data-testid="`cell-resultado-${index}`">
                {{ (item as Analise).acaoDescricao || formatarAcaoAnalise((item as Analise).acao) }}
              </span>
            </template>
            <template #cell(usuarioNome)="{ item, index }">
              <span
                  :data-testid="`cell-usuario-${index}`"
                  :title="(item as Analise).usuarioNome || (item as Analise).analistaUsuarioTitulo"
                  class="texto-truncado-usuario"
              >
                {{ (item as Analise).usuarioNome || (item as Analise).analistaUsuarioTitulo || "-" }}
              </span>
            </template>
            <template #cell(observacoes)="{ item, index }">
              <span :data-testid="`cell-observacao-${index}`">
                {{ (item as Analise).observacoes || '-' }}
              </span>
            </template>
          </BTable>
        </div>
      </template>
    </div>
    <template #footer>
      <div class="d-flex justify-content-end w-100 gap-3 align-items-center">
        <BButton
            data-testid="btn-modal-fechar"
            variant="secondary"
            @click="fechar"
        >
          Fechar
        </BButton>
      </div>
    </template>
  </BModal>
</template>

<script lang="ts" setup>
import {BAlert, BButton, BModal, BSpinner, BTable} from "bootstrap-vue-next";
import type {Analise} from "@/types/tipos";
import {formatarDataHoraBR} from "@/utils/date";


const __ = defineProps<{
  mostrar: boolean;
  historico: Analise[];
  loading?: boolean;
}>();

const emit = defineEmits(["fechar"]);

const fields = [
  {key: "dataHora", label: "Data/Hora"},
  {key: "unidadeSigla", label: "Unidade"},
  {key: "acao", label: "Ação"},
  {key: "usuarioNome", label: "Usuário"},
  {key: "observacoes", label: "Observação"},
];

/**
 * Fecha o modal
 */
function fechar() {
  emit("fechar");
}

function formatarAcaoAnalise(acao: string | null | undefined): string {
  switch (acao) {
    case "ACEITE_MAPEAMENTO":
    case "ACEITE_REVISAO":
      return "Aceite";
    case "DEVOLUCAO_MAPEAMENTO":
    case "DEVOLUCAO_REVISAO":
      return "Devolução";
    default:
      if (!acao) {
        return "-";
      }
      return acao
          .toLowerCase()
          .split("_")
          .map((parte) => parte.charAt(0).toUpperCase() + parte.slice(1))
          .join(" ");
  }
}
</script>

<style scoped>
.texto-truncado-usuario {
  display: inline-block;
  max-width: 16rem;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
  vertical-align: bottom;
}
</style>
