<script lang="ts" setup>
import {computed, type Ref, unref} from "vue";
import {BCard, BCol, BDropdown, BDropdownItemButton, BRow} from "bootstrap-vue-next";
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
        title-test-id="subprocesso-header__txt-header-unidade"
    >
      <template #actions>
        <BDropdown
            v-if="mostrarAcoesCabecalhoNormalizado"
            :text="TEXTOS.mapa.BOTAO_ACOES"
            data-testid="btn-subprocesso-acoes"
            toggle-class="text-nowrap"
            variant="outline-secondary"
        >
          <BDropdownItemButton
              v-if="mostrarAlterarDataLimiteNormalizado"
              :disabled="!habilitarAlterarDataLimiteNormalizado"
              data-testid="btn-alterar-data-limite"
              @click="$emit('abrir-alterar-data-limite')"
          >
            <i aria-hidden="true" class="bi bi-calendar me-1"/>
            {{ TEXTOS.subprocesso.BOTAO_ALTERAR_DATA_LIMITE }}
          </BDropdownItemButton>
          <BDropdownItemButton
              v-if="mostrarReabrirCadastroNormalizado"
              :disabled="!habilitarReabrirCadastroNormalizado"
              data-testid="btn-reabrir-cadastro"
              @click="$emit('abrir-reabrir-cadastro')"
          >
            <i aria-hidden="true" class="bi bi-arrow-counterclockwise me-1"/>
            {{ TEXTOS.subprocesso.BOTAO_REABRIR_CADASTRO }}
          </BDropdownItemButton>
          <BDropdownItemButton
              v-if="mostrarReabrirRevisaoNormalizado"
              :disabled="!habilitarReabrirRevisaoNormalizado"
              data-testid="btn-reabrir-revisao"
              @click="$emit('abrir-reabrir-revisao')"
          >
            <i aria-hidden="true" class="bi bi-arrow-counterclockwise me-1"/>
            {{ TEXTOS.subprocesso.BOTAO_REABRIR_REVISAO }}
          </BDropdownItemButton>
          <BDropdownItemButton
              v-if="mostrarEnviarLembreteNormalizado"
              :disabled="!habilitarEnviarLembreteNormalizado"
              data-testid="btn-enviar-lembrete"
              @click="$emit('confirmar-enviar-lembrete')"
          >
            <i aria-hidden="true" class="bi bi-bell me-1"/>
            {{ TEXTOS.subprocesso.BOTAO_ENVIAR_LEMBRETE }}
          </BDropdownItemButton>
        </BDropdown>
      </template>
    </PageHeader>

    <BRow class="mb-4">
      <BCol class="mb-3 mb-md-0" md="6">
        <BCard class="h-100" data-testid="header-subprocesso-details-info">
          <p class="mb-2" data-testid="txt-header-processo">
            <strong>{{ TEXTOS.subprocesso.LABEL_PROCESSO }}:</strong> {{ subprocesso.processoDescricao }}
          </p>
          <p class="mb-2">
            <span class="fw-bold me-1">{{ TEXTOS.subprocesso.LABEL_SITUACAO }}:</span>
            <span data-testid="subprocesso-header__txt-situacao">{{
                formatSituacaoSubprocesso(subprocesso.situacao)
              }}</span>
          </p>
          <p class="mb-2">
            <span class="fw-bold me-1">{{ TEXTOS.subprocesso.LABEL_LOCALIZACAO }}:</span>
            <span data-testid="subprocesso-header__txt-localizacao">{{ subprocesso.localizacaoAtual }}</span>
          </p>
          <p v-if="subprocesso.prazoEtapaAtual" class="mb-0">
            <span class="fw-bold me-1">{{ TEXTOS.subprocesso.LABEL_PRAZO_ETAPA }}:</span>
            <span data-testid="subprocesso-header__txt-prazo">{{ formatDataSimples(subprocesso.prazoEtapaAtual) }}</span>
          </p>
        </BCard>
      </BCol>

      <BCol md="6">
        <BCard class="h-100" data-testid="header-subprocesso-details-resp">
          <p class="mb-2"><strong>{{ TEXTOS.subprocesso.LABEL_TITULAR }}:</strong> {{
              subprocesso.titular?.nome || ''
            }}</p>
          <p class="ms-3 mb-3">
            <span v-if="subprocesso.titular?.ramal" class="me-3">
              <i aria-hidden="true" class="bi bi-telephone-fill me-1 text-muted"/>
              {{ subprocesso.titular.ramal }}
            </span>
            <span v-if="subprocesso.titular?.email">
              <i aria-hidden="true" class="bi bi-envelope-fill me-1 text-muted"/>
              <a :href="`mailto:${subprocesso.titular.email}`" class="link-discreto">{{ subprocesso.titular.email }}</a>
            </span>
          </p>

          <template
              v-if="subprocesso.responsavel?.usuario?.nome && subprocesso.responsavel.usuario.nome !== subprocesso.titular?.nome">
            <p class="mb-2">
              <strong>{{ TEXTOS.subprocesso.LABEL_RESPONSAVEL }}:</strong> {{
                subprocesso.responsavel.usuario.nome || ''
              }}
              <span v-if="subprocesso.responsavel.tipo" class="ms-1 small text-muted italic">
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
                <a :href="`mailto:${subprocesso.responsavel.usuario.email}`" class="link-discreto">{{
                    subprocesso.responsavel.usuario.email
                  }}</a>
              </span>
            </p>
          </template>
        </BCard>
      </BCol>
    </BRow>
  </div>
</template>
