<script setup lang="ts">
import {computed, type Ref, unref} from "vue";
import {BCard, BCardBody, BDropdown, BDropdownItemButton} from "bootstrap-vue-next";
import PageHeader from "@/components/layout/PageHeader.vue";
import type {ResponsavelDto, SubprocessoDetalhe} from "@/types/tipos";
import {TEXTOS} from "@/constants/textos";

type PropBooleana = boolean | Ref<boolean> | undefined;

interface Props {
  subprocesso: SubprocessoDetalhe;
  siglaUnidadeFallback: string;
  mostrarAcoesCabecalho?: PropBooleana;
  mostrarAlterarDataLimite?: PropBooleana;
  habilitarAlterarDataLimite?: PropBooleana;
  mostrarReabrirCadastro?: PropBooleana;
  habilitarReabrirCadastro?: PropBooleana;
  mostrarReabrirRevisao?: PropBooleana;
  habilitarReabrirRevisao?: PropBooleana;
  mostrarEnviarLembrete?: PropBooleana;
  habilitarEnviarLembrete?: PropBooleana;
  formatSituacaoSubprocesso: (situacao: string) => string;
  formatDataSimples: (data: string | null) => string;
  formatTipoResponsabilidade: (responsavel: ResponsavelDto | null) => string;
}

const props = defineProps<Props>();

defineEmits<{
  (e: "abrir-alterar-data-limite"): void;
  (e: "abrir-reabrir-cadastro"): void;
  (e: "abrir-reabrir-revisao"): void;
  (e: "confirmar-enviar-lembrete"): void;
}>();

function normalizarFlag(valor: PropBooleana) {
  return Boolean(unref(valor));
}

const mostrarAcoesCabecalhoNormalizado = computed(() => normalizarFlag(props.mostrarAcoesCabecalho));
const mostrarAlterarDataLimiteNormalizado = computed(() => normalizarFlag(props.mostrarAlterarDataLimite));
const habilitarAlterarDataLimiteNormalizado = computed(() => normalizarFlag(props.habilitarAlterarDataLimite));
const mostrarReabrirCadastroNormalizado = computed(() => normalizarFlag(props.mostrarReabrirCadastro));
const habilitarReabrirCadastroNormalizado = computed(() => normalizarFlag(props.habilitarReabrirCadastro));
const mostrarReabrirRevisaoNormalizado = computed(() => normalizarFlag(props.mostrarReabrirRevisao));
const habilitarReabrirRevisaoNormalizado = computed(() => normalizarFlag(props.habilitarReabrirRevisao));
const mostrarEnviarLembreteNormalizado = computed(() => normalizarFlag(props.mostrarEnviarLembrete));
const habilitarEnviarLembreteNormalizado = computed(() => normalizarFlag(props.habilitarEnviarLembrete));
</script>

<template>
  <div data-testid="header-subprocesso">
    <PageHeader
        :subtitle="subprocesso.unidade?.nome ?? ''"
        :title="subprocesso.unidade?.sigla ?? siglaUnidadeFallback"
        title-test-codigo="subprocesso-header__txt-header-unidade"
    >
      <template #actions>
        <BDropdown
            v-if="mostrarAcoesCabecalhoNormalizado"
            data-testid="btn-subprocesso-acoes"
            :text="TEXTOS.mapa.BOTAO_ACOES"
            toggle-class="text-nowrap"
            variant="outline-secondary"
        >
          <BDropdownItemButton
              v-if="mostrarAlterarDataLimiteNormalizado"
              data-testid="btn-alterar-data-limite"
              :disabled="!habilitarAlterarDataLimiteNormalizado"
              @click="$emit('abrir-alterar-data-limite')"
          >
            <i aria-hidden="true" class="bi bi-calendar me-1"/>
            {{ TEXTOS.subprocesso.BOTAO_ALTERAR_DATA_LIMITE }}
          </BDropdownItemButton>
          <BDropdownItemButton
              v-if="mostrarReabrirCadastroNormalizado"
              data-testid="btn-reabrir-cadastro"
              :disabled="!habilitarReabrirCadastroNormalizado"
              @click="$emit('abrir-reabrir-cadastro')"
          >
            <i aria-hidden="true" class="bi bi-arrow-counterclockwise me-1"/>
            {{ TEXTOS.subprocesso.BOTAO_REABRIR_CADASTRO }}
          </BDropdownItemButton>
          <BDropdownItemButton
              v-if="mostrarReabrirRevisaoNormalizado"
              data-testid="btn-reabrir-revisao"
              :disabled="!habilitarReabrirRevisaoNormalizado"
              @click="$emit('abrir-reabrir-revisao')"
          >
            <i aria-hidden="true" class="bi bi-arrow-counterclockwise me-1"/>
            {{ TEXTOS.subprocesso.BOTAO_REABRIR_REVISAO }}
          </BDropdownItemButton>
          <BDropdownItemButton
              v-if="mostrarEnviarLembreteNormalizado"
              data-testid="btn-enviar-lembrete"
              :disabled="!habilitarEnviarLembreteNormalizado"
              @click="$emit('confirmar-enviar-lembrete')"
          >
            <i aria-hidden="true" class="bi bi-bell me-1"/>
            {{ TEXTOS.subprocesso.BOTAO_ENVIAR_LEMBRETE }}
          </BDropdownItemButton>
        </BDropdown>
      </template>
    </PageHeader>

    <BCard class="mb-4" data-testid="header-subprocesso-details" no-body>
      <BCardBody>
        <p data-testid="txt-header-processo">
          <strong>{{ TEXTOS.subprocesso.LABEL_PROCESSO }}:</strong> {{ subprocesso.processoDescricao }}
        </p>
        <p>
          <span class="fw-bold me-1">{{ TEXTOS.subprocesso.LABEL_SITUACAO }}:</span>
          <span data-testid="subprocesso-header__txt-situacao">{{ formatSituacaoSubprocesso(subprocesso.situacao) }}</span>
        </p>
        <p>
          <span class="fw-bold me-1">{{ TEXTOS.subprocesso.LABEL_LOCALIZACAO }}:</span>
          <span data-testid="subprocesso-header__txt-localizacao">{{ subprocesso.localizacaoAtual }}</span>
        </p>
        <p v-if="subprocesso.prazoEtapaAtual">
          <span class="fw-bold me-1">{{ TEXTOS.subprocesso.LABEL_PRAZO_ETAPA }}:</span>
          <span data-testid="subprocesso-header__txt-prazo">{{ formatDataSimples(subprocesso.prazoEtapaAtual) }}</span>
        </p>
        <p class="mt-2"><strong>{{ TEXTOS.subprocesso.LABEL_TITULAR }}:</strong> {{ subprocesso.titular?.nome || '' }}</p>
        <p class="ms-3 mb-2">
          <span v-if="subprocesso.titular?.ramal" class="me-3">
            <i aria-hidden="true" class="bi bi-telephone-fill me-1 text-muted"/>
            {{ subprocesso.titular.ramal }}
          </span>
          <span v-if="subprocesso.titular?.email">
            <i aria-hidden="true" class="bi bi-envelope-fill me-1 text-muted"/>
            <a :href="`mailto:${subprocesso.titular.email}`">{{ subprocesso.titular.email }}</a>
          </span>
        </p>
        <template v-if="subprocesso.responsavel?.usuario?.nome && subprocesso.responsavel.usuario.nome !== subprocesso.titular?.nome">
          <p class="mt-2">
            <strong>{{ TEXTOS.subprocesso.LABEL_RESPONSAVEL }}:</strong> {{ subprocesso.responsavel.usuario.nome || '' }}
            <span v-if="subprocesso.responsavel.tipo" class="ms-1">
              - {{ formatTipoResponsabilidade(subprocesso.responsavel) }}
            </span>
          </p>
          <p class="ms-3 mb-0">
            <span v-if="subprocesso.responsavel.usuario.ramal" class="me-3">
              <i aria-hidden="true" class="bi bi-telephone-fill me-1 text-muted"/>
              {{ subprocesso.responsavel.usuario.ramal }}
            </span>
            <span v-if="subprocesso.responsavel.usuario.email">
              <i aria-hidden="true" class="bi bi-envelope-fill me-1 text-muted"/>
              <a :href="`mailto:${subprocesso.responsavel.usuario.email}`">{{ subprocesso.responsavel.usuario.email }}</a>
            </span>
          </p>
        </template>
      </BCardBody>
    </BCard>
  </div>
</template>
