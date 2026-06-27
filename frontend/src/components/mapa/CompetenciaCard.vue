<template>
  <BCard
      class="mb-2 competencia-card"
      data-testid="cad-mapa__card-competencia"
      no-body
  >
    <BCardHeader class="d-flex align-items-center">
      <BCardTitle class="fs-5 mb-0 me-3">
        <strong
            class="competencia-descricao"
            data-testid="cad-mapa__txt-competencia-descricao"
        > {{ competencia.descricao }}</strong>
      </BCardTitle>
      <div class="d-flex gap-1 actions-container">
        <BButton
            v-if="podeEditar !== false"
            :aria-label="`Editar competência ${competencia.descricao}`"
            class="botao-acao"
            data-testid="btn-editar-competencia"
            size="sm"
            title="Editar"
            @click="emit('editar', competencia)"
        >
          <i aria-hidden="true" class="bi bi-pencil"/>
        </BButton>
        <BButton
            v-if="podeEditar !== false"
            :aria-label="`Excluir competência ${competencia.descricao}`"
            class="botao-acao"
            data-testid="btn-excluir-competencia"
            size="sm"
            title="Excluir"
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
            class="atividade-associada-card-item d-flex group-atividade-associada"
            no-body
        >
          <BCardBody class="p-2">
            <div class="d-flex justify-content-between align-items-start mb-1">
              <span class="atividade-associada-descricao fw-bold text-break me-2">
                {{ atividade.descricao }}
              </span>
              <BButton
                  v-if="podeEditar !== false"
                  :aria-label="`Remover atividade ${atividade.descricao}`"
                  class="botao-acao-inline"
                  data-testid="btn-remover-atividade-associada"
                  size="sm"
                  title="Excluir"
                  @click="emit('removerAtividade', competencia.codigo, atividade.codigo)"
              >
                <i aria-hidden="true" class="bi bi-trash"/>
              </BButton>
            </div>

            <div v-if="(atividade.conhecimentos?.length ?? 0) > 0" class="conhecimentos-inline mt-1">
              <ul class="conhecimentos-lista mb-0 small text-muted border-top pt-1">
                <li
                    v-for="c in (getAtividadeCompleta(atividade.codigo)?.conhecimentos ?? [])"
                    :key="c.codigo"
                    class="conhecimento-item text-break"
                >
                  {{ c.descricao }}
                </li>
              </ul>
            </div>
          </BCardBody>
        </BCard>
      </div>
    </BCardBody>
  </BCard>
</template>

<script lang="ts" setup>
import {BButton, BCard, BCardBody, BCardHeader, BCardTitle} from "bootstrap-vue-next";
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
</script>

<style scoped>
.competencia-card {
  transition: box-shadow 0.2s;
}

.competencia-card:hover {
  box-shadow: 0 2px 12px 0 rgba(0, 0, 0, 0.07);
}

.competencia-descricao {
  overflow-wrap: anywhere;
  word-break: break-word;
}

.actions-container {
  opacity: 0.8;
  transition: opacity 0.2s;
}

.competencia-card:hover .actions-container,
.actions-container:focus-within {
  opacity: 1;
}

.botao-acao {
  width: 2.2rem;
  height: 2.2rem;
  display: flex;
  align-items: center;
  justify-content: center;
  padding: 0;
  font-size: 1rem;
  border: none;
  background-color: #e0e0e0;
  color: var(--bs-secondary);
  transition: all 0.2s;
}

.botao-acao:hover {
  background-color: var(--bs-primary-bg-subtle);
  color: var(--bs-primary);
  transform: translateY(-1px);
}

.botao-acao[data-testid="btn-excluir-competencia"]:hover {
  background-color: var(--bs-danger-bg-subtle);
  color: var(--bs-danger);
}

.atividade-associada-card-item {
  border: 1px solid rgba(0, 0, 0, 0.1);
  border-radius: 0.5rem;
  background-color: transparent;
  min-width: 150px;
  max-width: 300px;
  flex: 1 1 auto;
  transition: all 0.2s ease-in-out;
}

.atividade-associada-card-item:hover {
  border-color: var(--bs-primary-border-subtle);
  background-color: rgba(var(--bs-primary-rgb), 0.02);
  transform: translateY(-2px);
  box-shadow: 0 4px 8px rgba(0, 0, 0, 0.05);
}

.atividade-associada-descricao {
  font-size: 0.875rem;
  font-weight: 600;
  color: var(--bs-emphasis-color);
  overflow-wrap: anywhere;
  word-break: break-word;
}

.conhecimentos-lista {
  padding-left: 1rem;
  list-style-type: disc;
}

.conhecimento-item {
  line-height: 1.2;
  overflow-wrap: anywhere;
  word-break: break-word;
}

.botao-acao-inline {
  width: 1.6rem;
  height: 1.6rem;
  display: flex;
  align-items: center;
  justify-content: center;
  padding: 0;
  font-size: 0.8rem;
  border: none;
  background-color: #f0f0f0;
  color: var(--bs-secondary);
  transition: all 0.2s;
  flex-shrink: 0;
  margin-left: 0.15rem;
}

.botao-acao-inline:hover {
  background-color: var(--bs-danger-bg-subtle);
  color: var(--bs-danger);
}
</style>
