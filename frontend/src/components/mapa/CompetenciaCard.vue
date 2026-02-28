<template>
  <BCard
      class="mb-2 competencia-card"
      data-testid="cad-mapa__card-competencia"
      no-body
  >
    <BCardHeader class="d-flex justify-content-between align-items-center">
      <div class="card-title fs-5 mb-0">
        <strong
            class="competencia-descricao"
            data-testid="cad-mapa__txt-competencia-descricao"
        > {{ competencia.descricao }}</strong>
      </div>
      <div class="d-flex gap-1">
        <BButton
            v-if="podeEditar !== false"
            v-b-tooltip.hover
            :aria-label="`Editar competência ${competencia.descricao}`"
            class="botao-acao"
            data-testid="btn-editar-competencia"
            size="sm"
            title="Editar"
            variant="outline-primary"
            @click="emit('editar', competencia)"
        >
          <i aria-hidden="true" class="bi bi-pencil"/>
        </BButton>
        <BButton
            v-if="podeEditar !== false"
            v-b-tooltip.hover
            :aria-label="`Excluir competência ${competencia.descricao}`"
            class="botao-acao"
            data-testid="btn-excluir-competencia"
            size="sm"
            title="Excluir"
            variant="outline-danger"
            @click="emit('excluir', competencia.codigo)"
        >
          <i aria-hidden="true" class="bi bi-trash"/>
        </BButton>
      </div>
    </BCardHeader>
    <BCardBody class="position-relative">
      <div class="d-flex flex-wrap gap-2">
        <BCard
            v-for="atividade in competencia.atividades"
            :key="atividade.codigo"
            class="atividade-associada-card-item d-flex align-items-center group-atividade-associada"
            no-body
        >
          <BCardBody class="d-flex align-items-center">
                  <span class="atividade-associada-descricao me-2 d-flex align-items-center">
                    {{ atividade.descricao }}
                    <span
                        v-if="(atividade.conhecimentos?.length ?? 0) > 0"
                        v-b-tooltip.html.top="getConhecimentosTooltip(atividade.codigo)"
                        class="badge bg-secondary ms-2"
                        data-testid="cad-mapa__txt-badge-conhecimentos-1"
                    >
                      {{ atividade.conhecimentos.length }}
                    </span>
                  </span>
            <BButton
                v-if="podeEditar !== false"
                v-b-tooltip.hover
                :aria-label="`Remover atividade ${atividade.descricao}`"
                class="botao-acao-inline"
                data-testid="btn-remover-atividade-associada"
                size="sm"
                title="Remover Atividade"
                variant="outline-secondary"
                @click="emit('removerAtividade', competencia.codigo, atividade.codigo)"
            >
              <i aria-hidden="true" class="bi bi-trash"/>
            </BButton>
          </BCardBody>
        </BCard>
      </div>
    </BCardBody>
  </BCard>
</template>

<script lang="ts" setup>
import {BButton, BCard, BCardBody, BCardHeader} from "bootstrap-vue-next";
import type {Atividade, Competencia} from "@/types/tipos";

const props = defineProps<{
  competencia: Competencia;
  atividades: Atividade[];
  podeEditar?: boolean;
}>();

const emit = defineEmits<{
  editar: [competencia: Competencia];
  excluir: [codigo: number];
  removerAtividade: [competenciaCodigo: number, atividadeCodigo: number];
}>();

function getAtividadeCompleta(codigo: number): Atividade | undefined {
  return props.atividades.find((a) => a.codigo === codigo);
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

.competencia-descricao {
  word-break: break-word;
  max-width: 100%;
  display: inline-block;
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
}

.botao-acao:focus,
.botao-acao:hover {
  background: var(--bs-primary-bg-subtle);
  box-shadow: 0 0 0 2px var(--bs-primary);
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
