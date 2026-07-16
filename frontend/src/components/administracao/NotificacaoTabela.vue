<template>
  <section class="notificacao-tabela" data-testid="sec-notificacoes">
    <EmptyState
        v-if="items.length === 0"
        :title="TEXTOS.administracao.NOTIFICACOES_SEM_REGISTROS"
        description="Notificações por e-mail enviadas durante processos ativos aparecem aqui."
        data-testid="alert-notificacoes-sem-registros"
        icon="bi-info-circle"
    />
    <div v-else class="processos-notificacoes">
      <details
          v-for="grupo in gruposPorProcesso"
          :key="grupo.chave"
          class="processo-secao"
          open
      >
        <summary class="processo-titulo">{{ grupo.titulo }}</summary>
        <BTable
            :fields="camposTabela"
            :items="grupo.itens"
            data-testid="tbl-notificacoes"
            hover
            responsive
            small
        >
      <template #cell(destino)="{ item }">
        <div :title="item.destinatario">
          {{ formatarDestinatario(item) }}
        </div>
      </template>

      <template #cell(unidadeOrigemSigla)="{ item }">
        <span>{{ item.unidadeOrigemSigla || "-" }}</span>
      </template>

      <template #cell(assunto)="{ item }">
        <div :title="item.assunto" class="linha-assunto">{{ formatarAssunto(item.assunto) }}</div>
      </template>

      <template #cell(situacao)="{ item }">
        <div class="situacao-com-detalhes">
          <BButton :data-testid="`btn-detalhes-${item.codigo}`" :title="TEXTOS.administracao.NOTIFICACOES_DETALHES" aria-label="Detalhes da notificação" class="situacao-badge-botao" variant="link" @click="$emit('detalhes', item)">
            <BBadge :variant="(obterStatusNotificacao(item.situacao).variant as ColorVariant)">{{ obterStatusNotificacao(item.situacao).label }}</BBadge>
          </BButton>
        </div>
      </template>

      <template #cell(quando)="{ item }">
        {{ formatarQuando(item) }}
      </template>

      <template #cell(acoes)="{ item }">
        <div class="text-end d-flex justify-content-end align-items-center gap-2">
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
      </details>
    </div>
  </section>
</template>

<script lang="ts" setup>
import {BBadge, BButton, BTable, type ColorVariant} from "bootstrap-vue-next";
import {computed} from "vue";
import EmptyState from "@/components/comum/EmptyState.vue";
import {TEXTOS} from "@/constants/textos";
import type {Notificacao} from "@/services/notificacaoService";
import {obterStatusNotificacao} from "@/services/notificacaoService";
import {formatarAssunto, formatarDestinatario, formatarQuando} from "@/utils/notificacaoFormatters";

defineEmits<{
  detalhes: [item: Notificacao];
  preview: [item: Notificacao];
  reenviar: [item: Notificacao];
}>();

const props = defineProps<{
  items: Notificacao[];
}>();

const gruposPorProcesso = computed(() => {
  const grupos = new Map<string, {chave: string; titulo: string; itens: Notificacao[]}>();
  for (const item of props.items) {
    const titulo = item.processoDescricao?.trim() || "Processo não informado";
    const chave = titulo;
    const grupo = grupos.get(chave) ?? {chave, titulo, itens: []};
    grupo.itens.push(item);
    grupos.set(chave, grupo);
  }
  return [...grupos.values()];
});

const camposTabela = [
  {
    key: "unidadeOrigemSigla",
    label: "Origem",
    thClass: "col-origem",
    tdClass: "col-origem",
  },
  {
    key: "destino",
    label: "Destino",
    thClass: "col-destino-principal",
    tdClass: "col-destino-principal",
  },
  {
    key: "assunto",
    label: "Assunto",
    formatter: ({value}: { value: unknown }) => formatarAssunto(typeof value === "string" ? value : undefined)
  },
  {
    key: "situacao",
    label: TEXTOS.administracao.NOTIFICACOES_CAMPOS.STATUS,
    thClass: "col-status",
    tdClass: "col-status",
  },
  {
    key: "quando",
    label: TEXTOS.administracao.NOTIFICACOES_CAMPOS.QUANDO,
    thClass: "col-data",
    tdClass: "col-data",
    formatter: ({item}: { item: Notificacao }) => item ? formatarQuando(item) : "-"
  },
  {key: "acoes", label: "", thClass: "text-end col-acoes", tdClass: "text-end col-acoes"},
];
</script>

<style scoped>
:deep(.col-destino-principal) {
  width: 8rem;
}

:deep(.col-origem) {
  width: 6rem;
}

:deep(.col-status) {
  width: 11rem;
}

:deep(th:nth-child(3)),
:deep(td:nth-child(3)) {
  min-width: 24rem;
}

.situacao-com-detalhes {
  display: inline-flex;
  align-items: center;
  gap: 0.5rem;
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

.processo-secao {
  margin-bottom: 0.75rem;
  border: 1px solid var(--bs-border-color);
  border-radius: 0.375rem;
  overflow: hidden;
}

.processo-titulo {
  padding: 0.7rem 0.85rem;
  cursor: pointer;
  font-weight: 600;
  background-color: var(--bs-tertiary-bg);
}

.processo-secao > :deep(.table-responsive) {
  padding: 0.75rem 0.85rem;
}

.processo-secao :deep(.table) {
  margin-bottom: 0;
}

.situacao-badge-botao {
  padding: 0;
  margin: 0;
  border: 0;
  line-height: 1;
  text-decoration: none;
}
</style>
