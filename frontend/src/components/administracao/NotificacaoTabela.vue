<template>
  <section class="notificacao-tabela" data-testid="sec-notificacoes">
    <EmptyState
        v-if="items.length === 0"
        :title="TEXTOS.administracao.NOTIFICACOES_SEM_REGISTROS"
        data-testid="alert-notificacoes-sem-registros"
        icon="bi-info-circle"
    />
    <BTable
        v-else
        :fields="camposTabela"
        :items="items"
        data-testid="tbl-notificacoes"
        hover
        responsive
        small
        sort-icon-left
    >
      <template #cell(destinatario)="{ item }">
        <div class="fw-semibold" :title="item.destinatario">
          {{ formatarDestinatario(item) }}
        </div>
      </template>

      <template #cell(tipoNotificacao)="{ item }">
        <span>{{ formatarTipoNotificacao(item.tipoNotificacao) }}</span>
      </template>

      <template #cell(assunto)="{ item }">
        <div class="linha-assunto" :title="item.assunto">
          <div class="fw-semibold">{{ formatarAssunto(item.assunto) }}</div>
          <div class="text-muted small">
            {{ resumirContexto(item) }}
          </div>
        </div>
      </template>

      <template #cell(situacao)="{ item }">
        <BBadge :variant="(obterStatusNotificacao(item.situacao).variant as ColorVariant)">
          {{ obterStatusNotificacao(item.situacao).label }}
        </BBadge>
      </template>

      <template #cell(quando)="{ item }">
        {{ formatarQuando(item) }}
      </template>

      <template #cell(acoes)="{ item }">
        <div class="text-end d-flex justify-content-end align-items-center gap-2">
          <BButton
              size="sm"
              variant="outline-secondary"
              class="btn-acao"
              :data-testid="`btn-detalhes-${item.codigo}`"
              :title="TEXTOS.administracao.NOTIFICACOES_DETALHES"
              @click="$emit('detalhes', item)"
          >
            <i aria-hidden="true" class="bi bi-info-circle"></i>
          </BButton>
          <BButton
              v-if="item.corpoHtml"
              size="sm"
              variant="outline-secondary"
              class="btn-acao"
              :data-testid="`btn-preview-${item.codigo}`"
              title="Ver conteúdo do e-mail"
              @click="$emit('preview', item)"
          >
            <i aria-hidden="true" class="bi bi-eye"></i>
          </BButton>
          <BButton
              v-if="item.situacao === 'FALHA_DEFINITIVA'"
              :data-testid="`btn-notificacoes-reenviar-${item.codigo}`"
              size="sm"
              variant="outline-dark"
              class="btn-acao"
              title="Tentar reenviar e-mail"
              @click="$emit('reenviar', item)"
          >
            <i aria-hidden="true" class="bi bi-send"></i>
          </BButton>
        </div>
      </template>
    </BTable>
  </section>
</template>

<script lang="ts" setup>
import {BBadge, BButton, BTable, type ColorVariant} from "bootstrap-vue-next";
import EmptyState from "@/components/comum/EmptyState.vue";
import {TEXTOS} from "@/constants/textos";
import type {Notificacao} from "@/services/notificacaoService";
import {obterStatusNotificacao} from "@/services/notificacaoService";
import {
  formatarAssunto,
  formatarDestinatario,
  formatarQuando,
  formatarTipoNotificacao,
  resumirContexto
} from "@/utils/notificacaoFormatters";

defineProps<{
  items: Notificacao[];
}>();

defineEmits<{
  detalhes: [item: Notificacao];
  preview: [item: Notificacao];
  reenviar: [item: Notificacao];
}>();

const camposTabela = [
  {key: "destinatario", label: TEXTOS.administracao.NOTIFICACOES_CAMPOS.DESTINATARIO, thClass: "col-destinatario", tdClass: "col-destinatario", sortable: true},
  {key: "tipoNotificacao", label: TEXTOS.administracao.NOTIFICACOES_CAMPOS.TIPO, thClass: "col-tipo", tdClass: "col-tipo", sortable: true, formatter: ({value, item}: {value: unknown, item: Notificacao}) => formatarTipoNotificacao(typeof value === "string" ? value : item?.tipoNotificacao)},
  {key: "assunto", label: "Assunto", sortable: true, formatter: ({value}: {value: unknown}) => formatarAssunto(typeof value === "string" ? value : undefined)},
  {key: "situacao", label: TEXTOS.administracao.NOTIFICACOES_CAMPOS.STATUS, thClass: "col-status", tdClass: "col-status", sortable: true},
  {key: "quando", label: TEXTOS.administracao.NOTIFICACOES_CAMPOS.QUANDO, thClass: "col-data", tdClass: "col-data", sortable: true, formatter: ({item}: {item: Notificacao}) => item ? formatarQuando(item) : "-"},
  {key: "acoes", label: "", thClass: "text-end col-acoes", tdClass: "text-end col-acoes"},
];
</script>

<style scoped>
:deep(.col-destinatario) { width: 12rem; }
:deep(.col-tipo) { width: 14rem; }
:deep(.col-status) { width: 10rem; }
:deep(.col-data) { width: 10rem; }
:deep(.col-acoes) { width: 8rem; }

.linha-assunto {
  min-width: 0;
}

.btn-acao {
  display: inline-flex;
  align-items: center;
  justify-content: center;
  padding: 0.25rem 0.55rem;
  border-color: var(--bs-border-color);
  color: var(--bs-secondary-color);
}

.btn-acao:hover,
.btn-acao:focus {
  background: var(--bs-secondary-bg);
  border-color: var(--bs-secondary-color);
  color: var(--bs-body-color);
}
</style>
