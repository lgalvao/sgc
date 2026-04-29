<script setup lang="ts">
import {BButton} from 'bootstrap-vue-next';
import PageHeader from "@/components/layout/PageHeader.vue";
import LoadingButton from "@/components/comum/LoadingButton.vue";
import {TEXTOS} from "@/constants/textos";
import type {PermissoesSubprocesso, Unidade} from "@/types/tipos";

interface Props {
  unidade?: Unidade | null;
  codSubprocesso?: number | null;
  permissoes: PermissoesSubprocesso;
  mostrarDevolverCadastro?: boolean;
  mostrarImportarAtividades?: boolean;
  mostrarDisponibilizarCadastro?: boolean;
  acaoPrincipalCadastro?: {
    mostrar: boolean;
    habilitar: boolean;
    rotuloBotao: string;
  } | null;
  loadingValidacao?: boolean;
  podeVisualizarImpacto?: boolean;
}

defineProps<Props>();

defineEmits<{
  (e: 'abrir-historico'): void;
  (e: 'abrir-devolver'): void;
  (e: 'abrir-validar'): void;
  (e: 'abrir-impacto'): void;
  (e: 'abrir-importar'): void;
  (e: 'disponibilizar'): void;
}>();
</script>

<template>
  <PageHeader :title="TEXTOS.atividades.TITULO">
    <template #default>
      <span v-if="unidade" class="fw-bold" data-testid="subprocesso-header__txt-header-unidade">{{ unidade.sigla }}</span>
    </template>
    <template #actions>
      <div class="d-flex gap-2">
        <BButton
            v-if="codSubprocesso"
            data-testid="btn-cad-atividades-historico"
            variant="outline-secondary"
            @click="$emit('abrir-historico')"
        >
          <i aria-hidden="true" class="bi bi-clock-history me-1"/> {{ TEXTOS.atividades.BOTAO_HISTORICO_ANALISE }}
        </BButton>
        <BButton
            v-if="codSubprocesso && mostrarDevolverCadastro"
            data-testid="btn-acao-devolver"
            :disabled="!permissoes.habilitarDevolverCadastro"
            :title="TEXTOS.atividades.BOTAO_DEVOLVER"
            variant="secondary"
            @click="$emit('abrir-devolver')"
        >
          {{ TEXTOS.atividades.BOTAO_DEVOLVER }}
        </BButton>
        <BButton
            v-if="codSubprocesso && acaoPrincipalCadastro?.mostrar"
            data-testid="btn-acao-analisar-principal"
            :disabled="!acaoPrincipalCadastro.habilitar"
            :title="acaoPrincipalCadastro.rotuloBotao"
            variant="success"
            @click="$emit('abrir-validar')"
        >
          {{ acaoPrincipalCadastro.rotuloBotao }}
        </BButton>
      </div>

      <div v-if="podeVisualizarImpacto || mostrarImportarAtividades || mostrarDisponibilizarCadastro" class="d-flex gap-2">
        <BButton
            v-if="codSubprocesso && podeVisualizarImpacto"
            data-testid="cad-atividades__btn-impactos-mapa-edicao"
            variant="outline-secondary"
            @click="$emit('abrir-impacto')"
        >
          <i aria-hidden="true" class="bi bi-arrow-right-circle me-1"/> {{ TEXTOS.atividades.BOTAO_IMPACTO }}
        </BButton>
        <BButton
            v-if="codSubprocesso && mostrarImportarAtividades"
            data-testid="btn-cad-atividades-importar"
            :disabled="!permissoes.habilitarEditarCadastro"
            variant="outline-secondary"
            @click="$emit('abrir-importar')"
        >
          <i aria-hidden="true" class="bi bi-arrow-down-circle me-1"/> {{ TEXTOS.atividades.BOTAO_IMPORTAR }}
        </BButton>
        <LoadingButton
            v-if="codSubprocesso && mostrarDisponibilizarCadastro"
            :disabled="loadingValidacao || !permissoes.habilitarDisponibilizarCadastro"
            :loading="loadingValidacao"
            data-testid="btn-cad-atividades-disponibilizar"
            icon="check-lg"
            :loading-text="TEXTOS.atividades.BOTAO_DISPONIBILIZANDO"
            :text="TEXTOS.atividades.BOTAO_DISPONIBILIZAR"
            variant="success"
            @click="$emit('disponibilizar')"
        />
      </div>
    </template>
  </PageHeader>
</template>
