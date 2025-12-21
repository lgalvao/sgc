<template>
  <BCard
      class="mb-2 competencia-card"
      data-testid="cad-mapa__card-competencia"
      no-body
  >
    <BCardBody class="position-relative">
      <div
          class="card-title fs-5 d-flex align-items-center competencia-edicao-row competencia-titulo-card"
      >
        <strong
            class="competencia-descricao"
            data-testid="cad-mapa__txt-competencia-descricao"
        > {{ competencia.descricao }}</strong>
      </div>
      <div class="botoes-acao-competencia position-absolute">
        <BButton
            v-b-tooltip.hover
            class="botao-acao"
            data-testid="btn-editar-competencia"
            size="sm"
            title="Editar"
            :aria-label="`Editar competência ${competencia.descricao}`"
            variant="outline-primary"
            @click="emit('editar', competencia)"
        >
          <i class="bi bi-pencil" aria-hidden="true"/>
        </BButton>
        <BButton
            v-b-tooltip.hover
            class="botao-acao ms-1"
            data-testid="btn-excluir-competencia"
            size="sm"
            title="Excluir"
            :aria-label="`Excluir competência ${competencia.descricao}`"
            variant="outline-danger"
            @click="emit('excluir', competencia.codigo)"
        >
          <i class="bi bi-trash" aria-hidden="true"/>
        </BButton>
      </div>
      <div class="d-flex flex-wrap gap-2 mt-2">
        <BCard
            v-for="atvCodigo in competencia.atividadesAssociadas"
            :key="atvCodigo"
            class="atividade-associada-card-item d-flex align-items-center group-atividade-associada"
            no-body
        >
          <BCardBody class="d-flex align-items-center">
                  <span class="atividade-associada-descricao me-2 d-flex align-items-center">
                    {{ getDescricaoAtividade(atvCodigo) }}
                    <span
                        v-if="(getAtividadeCompleta(atvCodigo)?.conhecimentos.length ?? 0) > 0"
                        v-b-tooltip.html.top="getConhecimentosTooltip(atvCodigo)"
                        class="badge bg-secondary ms-2"
                        data-testid="cad-mapa__txt-badge-conhecimentos-1"
                    >
                      {{ getAtividadeCompleta(atvCodigo)?.conhecimentos.length }}
                    </span>
                  </span>
            <BButton
                v-b-tooltip.hover
                class="botao-acao-inline"
                data-testid="btn-remover-atividade-associada"
                size="sm"
                title="Remover Atividade"
                :aria-label="`Remover atividade ${getDescricaoAtividade(atvCodigo)}`"
                variant="outline-secondary"
                @click="emit('removerAtividade', competencia.codigo, atvCodigo)"
            >
              <i class="bi bi-trash" aria-hidden="true"/>
            </BButton>
          </BCardBody>
        </BCard>
      </div>
    </BCardBody>
  </BCard>
</template>

<script lang="ts" setup>
import {BButton, BCard, BCardBody} from "bootstrap-vue-next";
import type {Atividade, Competencia} from "@/types/tipos";

const props = defineProps<{
  competencia: Competencia;
  atividades: Atividade[];
}>();

const emit = defineEmits<{
  editar: [competencia: Competencia];
  excluir: [codigo: number];
  removerAtividade: [competenciaCodigo: number, atividadeCodigo: number];
}>();

function getAtividadeCompleta(codigo: number): Atividade | undefined {
  return props.atividades.find((a) => a.codigo === codigo);
}

function getDescricaoAtividade(codigo: number): string {
  const atv = getAtividadeCompleta(codigo);
  return atv ? atv.descricao : "Atividade não encontrada";
}

function getConhecimentosTooltip(atividadeCodigo: number): string {
  const atividade = getAtividadeCompleta(atividadeCodigo);
  if (!atividade || !atividade.conhecimentos.length) {
    return "Nenhum conhecimento cadastrado";
  }

  const conhecimentosHtml = atividade.conhecimentos
      .map((c) => `<div class="mb-1">• ${c.descricao}</div>`)
      .join("");

  return `<div class="text-start"><strong>Conhecimentos:</strong><br>${conhecimentosHtml}</div>`;
}
</script>

<style scoped>
.competencia-card {
  transition: box-shadow 0.2s;
}

.competencia-card:hover {
  box-shadow: 0 2px 12px 0 rgba(0, 0, 0, 0.07);
}

.botoes-acao-competencia {
  opacity: 0;
  pointer-events: none;
  transition: opacity 0.2s;
  top: 0.5rem;
  right: 0.5rem;
  z-index: 10;
}

/* Show buttons when card is hovered */
.competencia-card:hover .botoes-acao-competencia {
  opacity: 1;
  pointer-events: auto;
}

.botao-acao {
  width: 2rem;
  height: 2rem;
  display: flex;
  align-items: center;
  justify-content: center;
  padding: 0;
  font-size: 1.1rem;
  border-width: 2px;
  transition: background 0.15s, border-color 0.15s, color 0.15s;
  margin-left: 0;
  margin-right: 0;
  position: relative;
  z-index: 2;
}

.botao-acao:focus,
.botao-acao:hover {
  background: var(--bs-primary-bg-subtle);
  box-shadow: 0 0 0 2px var(--bs-primary);
}

.competencia-descricao {
  word-break: break-word;
  max-width: 100%;
  display: inline-block;
}

.competencia-hover-row:hover .competencia-descricao {
  font-weight: bold;
}

.competencia-edicao-row {
  width: 100%;
  justify-content: flex-start;
}

.competencia-titulo-card {
  background: var(--bs-light);
  border-bottom: 1px solid var(--bs-border-color);
  padding: 0.5rem 0.75rem;
  margin-left: -0.75rem;
  margin-right: -0.75rem;
  margin-top: -0.5rem;
  border-top-left-radius: 0.375rem;
  border-top-right-radius: 0.375rem;
  /* Ajuste para preencher a largura total do card */
  width: calc(100% + 1.5rem); /* 100% + 2 * 0.75rem (padding horizontal) */
}

.competencia-titulo-card .competencia-descricao {
  font-size: 1.1rem;
}

.atividade-associada-card-item {
  border: 1px solid var(--bs-border-color);
  border-radius: 0.375rem;
  background-color: var(--bs-secondary-bg);
}

.atividade-associada-descricao {
  font-size: 0.85rem;
  color: var(--bs-body-color);
}

.botao-acao-inline {
  width: 1.5rem;
  height: 1.5rem;
  display: flex;
  align-items: center;
  justify-content: center;
  padding: 0;
  font-size: 0.8rem;
  border-width: 1px;
  transition: background 0.15s, border-color 0.15s, color 0.15s;
}
</style>
