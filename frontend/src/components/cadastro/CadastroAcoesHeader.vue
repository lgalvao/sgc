<script setup lang="ts">
import {computed} from 'vue';
import {BButton, BDropdown, BDropdownItemButton} from 'bootstrap-vue-next';
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

defineEmits<{
  (e: 'abrir-historico'): void;
  (e: 'abrir-devolver'): void;
  (e: 'abrir-validar'): void;
  (e: 'abrir-impacto'): void;
  (e: 'abrir-importar'): void;
  (e: 'disponibilizar'): void;
}>();

const props = defineProps<Props>();

const quantidadeAcoesWorkflow = computed(() => {
  let total = 0;
  if (props.codSubprocesso && props.mostrarDevolverCadastro) {
    total += 1;
  }
  if (props.codSubprocesso && props.acaoPrincipalCadastro?.mostrar) {
    total += 1;
  }
  if (props.codSubprocesso && props.mostrarDisponibilizarCadastro) {
    total += 1;
  }
  return total;
});

const usarDropdownAcoes = computed(() => quantidadeAcoesWorkflow.value > 1);
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
        <BDropdown
            v-if="codSubprocesso && usarDropdownAcoes"
            data-testid="btn-cadastro-acoes"
            :text="TEXTOS.mapa.BOTAO_ACOES"
            toggle-class="text-nowrap"
            variant="success"
        >
          <BDropdownItemButton
              v-if="mostrarDevolverCadastro"
              data-testid="btn-cadastro-acao-devolver"
              :disabled="!permissoes.habilitarDevolverCadastro"
              @click="$emit('abrir-devolver')"
          >
            {{ TEXTOS.atividades.BOTAO_DEVOLVER }}
          </BDropdownItemButton>
          <BDropdownItemButton
              v-if="acaoPrincipalCadastro?.mostrar"
              data-testid="btn-cadastro-acao-principal"
              :disabled="!acaoPrincipalCadastro.habilitar"
              @click="$emit('abrir-validar')"
          >
            {{ acaoPrincipalCadastro.rotuloBotao }}
          </BDropdownItemButton>
          <BDropdownItemButton
              v-if="mostrarDisponibilizarCadastro"
              data-testid="btn-cadastro-acao-disponibilizar"
              :disabled="loadingValidacao || !permissoes.habilitarDisponibilizarCadastro"
              @click="$emit('disponibilizar')"
          >
            {{ TEXTOS.atividades.BOTAO_DISPONIBILIZAR }}
          </BDropdownItemButton>
        </BDropdown>
        <BButton
            v-else-if="codSubprocesso && mostrarDevolverCadastro"
            data-testid="btn-acao-devolver"
            :disabled="!permissoes.habilitarDevolverCadastro"
            :title="TEXTOS.atividades.BOTAO_DEVOLVER"
            variant="secondary"
            @click="$emit('abrir-devolver')"
        >
          {{ TEXTOS.atividades.BOTAO_DEVOLVER }}
        </BButton>
        <BButton
            v-else-if="codSubprocesso && acaoPrincipalCadastro?.mostrar"
            data-testid="btn-acao-analisar-principal"
            :disabled="!acaoPrincipalCadastro.habilitar"
            :title="acaoPrincipalCadastro.rotuloBotao"
            variant="success"
            @click="$emit('abrir-validar')"
        >
          {{ acaoPrincipalCadastro.rotuloBotao }}
        </BButton>
        <LoadingButton
            v-else-if="codSubprocesso && mostrarDisponibilizarCadastro"
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

      <div v-if="podeVisualizarImpacto || mostrarImportarAtividades" class="d-flex gap-2">
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
      </div>
    </template>
  </PageHeader>
</template>
