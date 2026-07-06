<template>
  <section class="notificacao-tabela" data-testid="sec-notificacoes">
    <EmptyState
        v-if="items.length === 0"
        :title="TEXTOS.administracao.NOTIFICACOES_SEM_REGISTROS"
        description="Notificações por e-mail enviadas durante processos ativos aparecem aqui."
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
        <div :title="item.destinatario" class="fw-semibold">
          {{ formatarDestinatario(item) }}
        </div>
      </template>

      <template #cell(processoDescricao)="{ item }">
        <span>{{ item.processoDescricao || "-" }}</span>
      </template>

      <template #cell(unidadeOrigemSigla)="{ item }">
        <span>{{ item.unidadeOrigemSigla || "-" }}</span>
      </template>

      <template #cell(unidadeDestino)="{ item }">
        <span>{{ item.unidadeDestinoSigla || item.unidadeSigla || "-" }}</span>
      </template>

      <template #cell(assunto)="{ item }">
        <div :title="item.assunto" class="linha-assunto">
          <div class="fw-semibold">{{ formatarAssunto(item.assunto) }}</div>
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
              :data-testid="`btn-detalhes-${item.codigo}`"
              :title="TEXTOS.administracao.NOTIFICACOES_DETALHES"
              class="btn-acao-sutil"
              size="sm"
              variant="outline-secondary"
              @click="$emit('detalhes', item)"
          >
            <i aria-hidden="true" class="bi bi-info-circle"></i>
          </BButton>
          <BButton
              v-if="item.corpoHtml"
              :data-testid="`btn-preview-${item.codigo}`"
              class="btn-acao-sutil"
              size="sm"
              title="Ver conteúdo do e-mail"
              variant="outline-secondary"
              @click="$emit('preview', item)"
          >
            <i aria-hidden="true" class="bi bi-eye"></i>
          </BButton>
          <BButton
              v-if="item.situacao === 'FALHA_DEFINITIVA'"
              :data-testid="`btn-notificacoes-reenviar-${item.codigo}`"
              class="btn-acao-sutil"
              size="sm"
              title="Tentar reenviar e-mail"
              variant="outline-dark"
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
import {formatarAssunto, formatarDestinatario, formatarQuando} from "@/utils/notificacaoFormatters";

defineProps<{
  items: Notificacao[];
}>();

defineEmits<{
  detalhes: [item: Notificacao];
  preview: [item: Notificacao];
  reenviar: [item: Notificacao];
}>();

const camposTabela = [
  {
    key: "destinatario",
    label: TEXTOS.administracao.NOTIFICACOES_CAMPOS.DESTINATARIO,
    thClass: "col-destinatario",
    tdClass: "col-destinatario",
    sortable: true
  },
  {
    key: "unidadeOrigemSigla",
    label: "Origem",
    thClass: "col-origem",
    tdClass: "col-origem",
    sortable: true,
  },
  {
    key: "unidadeDestino",
    label: "Destino",
    thClass: "col-destino",
    tdClass: "col-destino",
    sortable: false,
  },
  {
    key: "processoDescricao",
    label: TEXTOS.administracao.NOTIFICACOES_CAMPOS.PROCESSO,
    thClass: "col-processo",
    tdClass: "col-processo",
    sortable: true,
  },
  {
    key: "assunto",
    label: "Assunto",
    sortable: true,
    formatter: ({value}: { value: unknown }) => formatarAssunto(typeof value === "string" ? value : undefined)
  },
  {
    key: "situacao",
    label: TEXTOS.administracao.NOTIFICACOES_CAMPOS.STATUS,
    thClass: "col-status",
    tdClass: "col-status",
    sortable: true
  },
  {
    key: "quando",
    label: TEXTOS.administracao.NOTIFICACOES_CAMPOS.QUANDO,
    thClass: "col-data",
    tdClass: "col-data",
    sortable: true,
    formatter: ({item}: { item: Notificacao }) => item ? formatarQuando(item) : "-"
  },
  {key: "acoes", label: "", thClass: "text-end col-acoes", tdClass: "text-end col-acoes"},
];
</script>

<style scoped>
:deep(.col-destinatario) {
  width: 12rem;
}

:deep(.col-processo) {
  width: 14rem;
}

:deep(.col-origem) {
  width: 9rem;
}

:deep(.col-destino) {
  width: 9rem;
}

:deep(.col-status) {
  width: 10rem;
}

:deep(.col-data) {
  width: 10rem;
}

:deep(.col-acoes) {
  width: 8rem;
}

.linha-assunto {
  min-width: 0;
}
</style>
