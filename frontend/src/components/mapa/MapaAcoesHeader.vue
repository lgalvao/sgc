<script setup lang="ts">
import {computed} from "vue";
import {BButton, BDropdown, BDropdownItemButton} from "bootstrap-vue-next";
import PageHeader from "@/components/layout/PageHeader.vue";
import LoadingButton from "@/components/comum/LoadingButton.vue";
import {TEXTOS} from "@/constants/textos";
import type {Unidade} from "@/types/tipos";

interface Props {
  unidade?: Unidade | null;
  codigoSubprocesso?: number | null;
  podeVerSugestoes?: boolean;
  loadingSugestoesVisualizacao?: boolean;
  podeVisualizarImpacto?: boolean;
  loadingImpacto?: boolean;
  usarMenuAcoesMapa?: boolean;
  mostrarApresentarSugestoes?: boolean;
  habilitarApresentarSugestoes?: boolean;
  mostrarValidarMapa?: boolean;
  habilitarValidarMapa?: boolean;
  mostrarDisponibilizarMapa?: boolean;
  habilitarDisponibilizarMapa?: boolean;
  loadingDisponibilizacao?: boolean;
  mostrarDevolverMapa?: boolean;
  habilitarDevolverMapa?: boolean;
  mostrarAcaoPrincipalMapa?: boolean;
  habilitarAcaoPrincipalMapa?: boolean;
  rotuloAcaoPrincipalMapa?: string;
}

const props = withDefaults(defineProps<Props>(), {
  podeVerSugestoes: false,
  loadingSugestoesVisualizacao: false,
  podeVisualizarImpacto: false,
  loadingImpacto: false,
  usarMenuAcoesMapa: false,
  mostrarApresentarSugestoes: false,
  habilitarApresentarSugestoes: false,
  mostrarValidarMapa: false,
  habilitarValidarMapa: false,
  mostrarDisponibilizarMapa: false,
  habilitarDisponibilizarMapa: false,
  loadingDisponibilizacao: false,
  mostrarDevolverMapa: false,
  habilitarDevolverMapa: false,
  mostrarAcaoPrincipalMapa: false,
  habilitarAcaoPrincipalMapa: false,
  rotuloAcaoPrincipalMapa: TEXTOS.mapa.LABEL_HOMOLOGAR,
});

defineEmits<{
  (e: "ver-sugestoes"): void;
  (e: "abrir-historico"): void;
  (e: "abrir-impacto"): void;
  (e: "abrir-sugestoes"): void;
  (e: "abrir-validar"): void;
  (e: "abrir-disponibilizar"): void;
  (e: "abrir-devolver"): void;
  (e: "abrir-acao-principal"): void;
}>();

const exibirAcoes = computed(() => Boolean(props.codigoSubprocesso));
</script>

<template>
  <PageHeader :title="TEXTOS.mapa.TITULO_TECNICO">
    <template #default>
      <div v-if="unidade" class="fs-5" data-testid="mapa-header__txt-header-unidade">
        {{ unidade.sigla }}
      </div>
    </template>

    <template #actions>
      <div class="d-flex gap-2">
        <LoadingButton
            v-if="podeVerSugestoes"
            :loading="loadingSugestoesVisualizacao"
            data-testid="btn-mapa-ver-sugestoes"
            loading-text="Carregando..."
            text="Ver sugestões"
            variant="outline-secondary"
            @click="$emit('ver-sugestoes')"
        >
          {{ TEXTOS.mapa.BOTAO_VER_SUGESTOES }}
        </LoadingButton>

        <BButton
            v-if="exibirAcoes"
            data-testid="btn-mapa-historico"
            variant="outline-secondary"
            @click="$emit('abrir-historico')"
        >
          {{ TEXTOS.mapa.BOTAO_HISTORICO_ANALISE }}
        </BButton>

        <LoadingButton
            v-if="podeVisualizarImpacto"
            :loading="loadingImpacto"
            data-testid="cad-mapa__btn-impactos-mapa"
            icon="arrow-right-circle"
            :text="TEXTOS.mapa.BOTAO_IMPACTO"
            variant="outline-secondary"
            @click="$emit('abrir-impacto')"
        />

        <BDropdown
            v-if="usarMenuAcoesMapa"
            data-testid="btn-mapa-acoes"
            :text="TEXTOS.mapa.BOTAO_ACOES"
            toggle-class="text-nowrap"
            variant="success"
        >
          <BDropdownItemButton
              v-if="mostrarApresentarSugestoes"
              data-testid="btn-mapa-acao-sugestoes"
              :disabled="!habilitarApresentarSugestoes"
              @click="$emit('abrir-sugestoes')"
          >
            {{ TEXTOS.mapa.BOTAO_SUGESTOES }}
          </BDropdownItemButton>
          <BDropdownItemButton
              v-if="mostrarValidarMapa"
              data-testid="btn-mapa-acao-validar"
              :disabled="!habilitarValidarMapa"
              @click="$emit('abrir-validar')"
          >
            {{ TEXTOS.mapa.BOTAO_VALIDAR }}
          </BDropdownItemButton>
          <BDropdownItemButton
              v-if="mostrarDisponibilizarMapa"
              data-testid="btn-mapa-acao-disponibilizar"
              :disabled="!habilitarDisponibilizarMapa || loadingDisponibilizacao"
              @click="$emit('abrir-disponibilizar')"
          >
            {{ TEXTOS.mapa.BOTAO_DISPONIBILIZAR }}
          </BDropdownItemButton>
          <BDropdownItemButton
              v-if="mostrarDevolverMapa"
              data-testid="btn-mapa-acao-devolver"
              :disabled="!habilitarDevolverMapa"
              @click="$emit('abrir-devolver')"
          >
            {{ TEXTOS.mapa.BOTAO_DEVOLVER }}
          </BDropdownItemButton>
          <BDropdownItemButton
              v-if="mostrarAcaoPrincipalMapa"
              data-testid="btn-mapa-acao-homologar-aceite"
              :disabled="!habilitarAcaoPrincipalMapa"
              @click="$emit('abrir-acao-principal')"
          >
            {{ rotuloAcaoPrincipalMapa }}
          </BDropdownItemButton>
        </BDropdown>
      </div>
    </template>
  </PageHeader>
</template>
