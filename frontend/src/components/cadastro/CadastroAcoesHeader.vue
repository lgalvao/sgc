<script lang="ts" setup>
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
      <span v-if="unidade" class="fw-bold" data-testid="subprocesso-header__txt-header-unidade">{{
          unidade.sigla
        }}</span>
    </template>
    <template #alerta>
      <slot name="alerta"/>
    </template>
    <template #actions>
      <div class="d-flex gap-2">
        <BButton
            v-if="codSubprocesso"
            data-testid="btn-cad-atividades-historico"
            variant="light"
            @click="$emit('abrir-historico')"
        >
          <i aria-hidden="true" class="bi bi-clock-history me-1"/> {{ TEXTOS.atividades.BOTAO_HISTORICO_ANALISE }}
        </BButton>
        <BButton
            v-if="codSubprocesso && podeVisualizarImpacto"
            data-testid="cad-atividades__btn-impactos-mapa-edicao"
            variant="light"
            @click="$emit('abrir-impacto')"
        >
          <i aria-hidden="true" class="bi bi-arrow-right-circle me-1"/> {{ TEXTOS.atividades.BOTAO_IMPACTO }}
        </BButton>
        <BDropdown
            v-if="codSubprocesso && usarDropdownAcoes"
            :text="TEXTOS.mapa.BOTAO_ACOES"
            data-testid="btn-cadastro-acoes"
            toggle-class="text-nowrap"
            variant="success"
        >
          <BDropdownItemButton
              v-if="mostrarDevolverCadastro"
              :disabled="!permissoes.habilitarDevolverCadastro"
              data-testid="btn-cadastro-acao-devolver"
              @click="$emit('abrir-devolver')"
          >
            {{ TEXTOS.atividades.BOTAO_DEVOLVER }}
          </BDropdownItemButton>
          <BDropdownItemButton
              v-if="acaoPrincipalCadastro?.mostrar"
              :disabled="!acaoPrincipalCadastro.habilitar"
              data-testid="btn-cadastro-acao-principal"
              @click="$emit('abrir-validar')"
          >
            {{ acaoPrincipalCadastro.rotuloBotao }}
          </BDropdownItemButton>
          <BDropdownItemButton
              v-if="mostrarDisponibilizarCadastro"
              :disabled="loadingValidacao || !permissoes.habilitarDisponibilizarCadastro"
              data-testid="btn-cadastro-acao-disponibilizar"
              @click="$emit('disponibilizar')"
          >
            {{ TEXTOS.atividades.BOTAO_DISPONIBILIZAR }}
          </BDropdownItemButton>
        </BDropdown>
        <BButton
            v-else-if="codSubprocesso && mostrarDevolverCadastro"
            :disabled="!permissoes.habilitarDevolverCadastro"
            :title="TEXTOS.atividades.BOTAO_DEVOLVER"
            data-testid="btn-acao-devolver"
            variant="secondary"
            @click="$emit('abrir-devolver')"
        >
          {{ TEXTOS.atividades.BOTAO_DEVOLVER }}
        </BButton>
        <BButton
            v-else-if="codSubprocesso && acaoPrincipalCadastro?.mostrar"
            :disabled="!acaoPrincipalCadastro.habilitar"
            :title="acaoPrincipalCadastro.rotuloBotao"
            data-testid="btn-acao-analisar-principal"
            variant="success"
            @click="$emit('abrir-validar')"
        >
          {{ acaoPrincipalCadastro.rotuloBotao }}
        </BButton>
        <BButton
            v-if="codSubprocesso && mostrarImportarAtividades"
            :disabled="!permissoes.habilitarEditarCadastro"
            data-testid="btn-cad-atividades-importar"
            variant="light"
            @click="$emit('abrir-importar')"
        >
          <i aria-hidden="true" class="bi bi-arrow-down-circle me-1"/> {{ TEXTOS.atividades.BOTAO_IMPORTAR }}
        </BButton>
        <LoadingButton
            v-if="codSubprocesso && mostrarDisponibilizarCadastro && !usarDropdownAcoes"
            :disabled="loadingValidacao || !permissoes.habilitarDisponibilizarCadastro"
            :loading="loadingValidacao"
            :loading-text="TEXTOS.atividades.BOTAO_DISPONIBILIZANDO"
            :text="TEXTOS.atividades.BOTAO_DISPONIBILIZAR"
            data-testid="btn-cad-atividades-disponibilizar"
            icon="check-lg"
            variant="success"
            @click="$emit('disponibilizar')"
        />
      </div>
    </template>
  </PageHeader>
</template>
