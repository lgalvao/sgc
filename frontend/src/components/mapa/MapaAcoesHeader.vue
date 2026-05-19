<script lang="ts" setup>
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
  mostrarExportacaoMapa?: boolean;
  loadingExportacaoPdf?: boolean;
  loadingExportacaoCsv?: boolean;
  mostrarDevolverMapa?: boolean;
  habilitarDevolverMapa?: boolean;
  mostrarAcaoPrincipalMapa?: boolean;
  habilitarAcaoPrincipalMapa?: boolean;
  rotuloAcaoPrincipalMapa?: string;
}

const props = withDefaults(defineProps<Props>(), {
  unidade: null,
  codigoSubprocesso: null,
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
  mostrarExportacaoMapa: false,
  loadingExportacaoPdf: false,
  loadingExportacaoCsv: false,
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
  (e: "exportar-pdf"): void;
  (e: "exportar-csv"): void;
}>();

const exibirAcoes = computed(() => Boolean(props.codigoSubprocesso));
</script>

<template>
  <PageHeader :title="TEXTOS.mapa.TITULO_TECNICO">
    <template #default>
      <div v-if="unidade" class="fs-5" data-testid="subprocesso-header__txt-header-unidade">
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
          <i aria-hidden="true" class="bi bi-clock-history me-1"/> {{ TEXTOS.mapa.BOTAO_HISTORICO_ANALISE }}
        </BButton>

        <LoadingButton
            v-if="podeVisualizarImpacto"
            :loading="loadingImpacto"
            :text="TEXTOS.mapa.BOTAO_IMPACTO"
            data-testid="cad-mapa__btn-impactos-mapa"
            icon="arrow-right-circle"
            variant="outline-secondary"
            @click="$emit('abrir-impacto')"
        />

        <BDropdown
            v-if="mostrarExportacaoMapa"
            :text="TEXTOS.mapa.BOTAO_EXPORTAR"
            data-testid="btn-mapa-exportar"
            toggle-class="text-nowrap"
            variant="outline-secondary"
        >
          <BDropdownItemButton
              :disabled="loadingExportacaoPdf"
              data-testid="btn-mapa-exportar-pdf"
              @click="$emit('exportar-pdf')"
          >
            PDF
          </BDropdownItemButton>
          <BDropdownItemButton
              :disabled="loadingExportacaoCsv"
              data-testid="btn-mapa-exportar-csv"
              @click="$emit('exportar-csv')"
          >
            {{ TEXTOS.relatorios.BOTAO_CSV }}
          </BDropdownItemButton>
        </BDropdown>

        <BDropdown
            v-if="usarMenuAcoesMapa"
            :text="TEXTOS.mapa.BOTAO_ACOES"
            data-testid="btn-mapa-acoes"
            toggle-class="text-nowrap"
            variant="success"
        >
          <BDropdownItemButton
              v-if="mostrarApresentarSugestoes"
              :disabled="!habilitarApresentarSugestoes"
              data-testid="btn-mapa-acao-sugestoes"
              @click="$emit('abrir-sugestoes')"
          >
            {{ TEXTOS.mapa.BOTAO_SUGESTOES }}
          </BDropdownItemButton>
          <BDropdownItemButton
              v-if="mostrarValidarMapa"
              :disabled="!habilitarValidarMapa"
              data-testid="btn-mapa-acao-validar"
              @click="$emit('abrir-validar')"
          >
            {{ TEXTOS.mapa.BOTAO_VALIDAR }}
          </BDropdownItemButton>
          <BDropdownItemButton
              v-if="mostrarDisponibilizarMapa"
              :disabled="!habilitarDisponibilizarMapa || loadingDisponibilizacao"
              data-testid="btn-mapa-acao-disponibilizar"
              @click="$emit('abrir-disponibilizar')"
          >
            {{ TEXTOS.mapa.BOTAO_DISPONIBILIZAR }}
          </BDropdownItemButton>
          <BDropdownItemButton
              v-if="mostrarDevolverMapa"
              :disabled="!habilitarDevolverMapa"
              data-testid="btn-mapa-acao-devolver"
              @click="$emit('abrir-devolver')"
          >
            {{ TEXTOS.mapa.BOTAO_DEVOLVER }}
          </BDropdownItemButton>
          <BDropdownItemButton
              v-if="mostrarAcaoPrincipalMapa"
              :disabled="!habilitarAcaoPrincipalMapa"
              data-testid="btn-mapa-acao-homologar-aceite"
              @click="$emit('abrir-acao-principal')"
          >
            {{ rotuloAcaoPrincipalMapa }}
          </BDropdownItemButton>
        </BDropdown>
      </div>
    </template>
  </PageHeader>
</template>
