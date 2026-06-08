<template>
  <LayoutPadrao>
    <CarregamentoPagina v-if="carregando"/>

    <template v-else>
      <!-- Cabeçalho -->
      <div class="d-flex align-items-center justify-content-between mb-4 flex-wrap gap-2">
        <div>
          <h1 class="h4 mb-1">
            <i aria-hidden="true" class="bi bi-building text-primary me-2"/>
            {{ TEXTOS.diagnostico.TITULO_UNIDADE }}
          </h1>
          <div v-if="unidade" class="text-muted small">
            <strong>{{ unidade.unidadeSigla }}</strong> — {{ unidade.unidadeNome }}
            <BBadge :variant="varianteSituacao" class="ms-2">{{ situacao }}</BBadge>
          </div>
        </div>
        <BButton size="sm" variant="outline-secondary" @click="void router.back()">
          <i aria-hidden="true" class="bi bi-arrow-left me-1"/>
          {{ TEXTOS.diagnostico.BTN_VOLTAR }}
        </BButton>
      </div>

      <!-- Alertas -->
      <AppAlert
          v-if="retornoFluxo"
          :mensagem="retornoFluxo.mensagem"
          :variante="retornoFluxo.variante"
          @dismissed="limparRetornoFluxo"
      />

      <!-- Cards de métricas -->
      <BRow class="mb-4 g-3">
        <BCol md="4">
          <BCard class="text-center h-100">
            <div class="display-6 fw-bold text-primary">{{ servidores.length }}</div>
            <div class="text-muted small mt-1">Servidores</div>
          </BCard>
        </BCol>
        <BCol md="4">
          <BCard class="text-center h-100">
            <div class="display-6 fw-bold" :class="totalPendentes > 0 ? 'text-warning' : 'text-success'">
              {{ totalPendentes }}
            </div>
            <div class="text-muted small mt-1">Pendentes</div>
          </BCard>
        </BCol>
        <BCol md="4">
          <BCard class="text-center h-100">
            <div class="display-6 fw-bold text-info">{{ ocupacoesCriticas.length }}</div>
            <div class="text-muted small mt-1">Ocupações Críticas</div>
          </BCard>
        </BCol>
      </BRow>

      <!-- Tabela de servidores com detalhamento do consenso -->
      <BCard class="mb-4">
        <BCardHeader class="d-flex justify-content-between align-items-center">
          <strong>Servidores e Consenso</strong>
          <BBadge v-if="totalPendentes > 0" variant="warning">{{ totalPendentes }} pendente(s)</BBadge>
        </BCardHeader>

        <EmptyState
            v-if="servidores.length === 0"
            :description="TEXTOS.diagnostico.VAZIO_EQUIPE_TEXTO"
            :title="TEXTOS.diagnostico.VAZIO_EQUIPE_TITULO"
            icon="bi-people"
        />

        <template v-else>
          <BAccordion v-for="servidor in servidores" :key="servidor.servidorTitulo" class="mb-1">
            <BAccordionItem>
              <template #title>
                <div class="d-flex align-items-center gap-2 w-100 justify-content-between pe-3">
                  <span>
                    <strong>{{ servidor.servidorNome }}</strong>
                    <small class="text-muted ms-2">{{ servidor.servidorTitulo }}</small>
                  </span>
                  <BBadge :variant="varianteSituacaoServidor(servidor.situacaoServidor)">
                    {{ formatarSituacaoServidor(servidor.situacaoServidor) }}
                  </BBadge>
                </div>
              </template>
              <BTable
                  :fields="colunasCompetencias"
                  :items="servidor.consenso"
                  bordered
                  responsive
                  small
              >
                <template #cell(importancia)="{ item }">
                  {{ formatarNota(item.importancia) }}
                </template>
                <template #cell(dominio)="{ item }">
                  {{ formatarNota(item.dominio) }}
                </template>
                <template #cell(gap)="{ item }">
                  <span
                    v-if="obterGapInfo(item)"
                    :class="obterGapInfo(item)?.variante"
                  >
                    {{ obterGapInfo(item)?.texto }}
                  </span>
                  <span v-else>{{ TEXTOS.diagnostico.NOTA_NAO_INFORMADA }}</span>
                </template>
              </BTable>
            </BAccordionItem>
          </BAccordion>
        </template>
      </BCard>

      <!-- Ocupações críticas -->
      <BCard v-if="ocupacoesCriticas.length > 0" class="mb-4">
        <BCardHeader><strong>Ocupações Críticas</strong></BCardHeader>
        <BTable
            :fields="colunasOcupacoes"
            :items="ocupacoesComDescricao"
            bordered
            responsive
            small
        >
          <template #cell(situacaoCapacitacao)="{ item }">
            <BBadge :variant="varianteCapacitacao(item.situacaoCapacitacao)">
              {{ formatarCapacitacao(item.situacaoCapacitacao) }}
            </BBadge>
          </template>
        </BTable>
      </BCard>

      <!-- Ações do gestor/admin -->
      <div class="d-flex gap-2 flex-wrap mb-4">
        <BButton
            v-if="podeValidar"
            :disabled="validando"
            data-testid="btn-validar-diagnostico-unidade"
            variant="success"
            @click="abrirModalValidar"
        >
          <BSpinner v-if="validando" aria-hidden="true" class="me-1" small/>
          {{ TEXTOS.diagnostico.BTN_VALIDAR }}
        </BButton>

        <BButton
            v-if="podeDevolver"
            :disabled="devolvendo"
            data-testid="btn-devolver-diagnostico-unidade"
            variant="warning"
            @click="abrirModalDevolver"
        >
          <BSpinner v-if="devolvendo" aria-hidden="true" class="me-1" small/>
          {{ TEXTOS.diagnostico.BTN_DEVOLVER }}
        </BButton>

        <BButton
            v-if="podeHomologar"
            :disabled="homologando"
            data-testid="btn-homologar-diagnostico-unidade"
            variant="primary"
            @click="abrirModalHomologar"
        >
          <BSpinner v-if="homologando" aria-hidden="true" class="me-1" small/>
          {{ TEXTOS.diagnostico.BTN_HOMOLOGAR }}
        </BButton>
      </div>

      <!-- Histórico de movimentações -->
      <BCard v-if="movimentacoes.length > 0" class="mb-4">
        <BCardHeader><strong>Histórico de Movimentações</strong></BCardHeader>
        <BListGroup flush>
          <BListGroupItem
              v-for="(mov, idx) in movimentacoes"
              :key="idx"
              class="small"
          >
            <div class="d-flex justify-content-between">
              <span><strong>{{ mov.descricao }}</strong></span>
              <span class="text-muted">{{ mov.dataHora }}</span>
            </div>
            <div class="text-muted">{{ mov.unidadeOrigem }} → {{ mov.unidadeDestino }}</div>
          </BListGroupItem>
        </BListGroup>
      </BCard>
    </template>

    <!-- Modal: Validar -->
    <BModal v-model="modalValidarAberto" :title="TEXTOS.diagnostico.MODAL_VALIDAR_TITULO" centered>
      <BFormTextarea v-model="observacoesValidar" :placeholder="TEXTOS.diagnostico.LABEL_OBSERVACOES" rows="3"/>
      <template #footer>
        <BButton class="text-secondary" variant="link" @click="modalValidarAberto = false">Cancelar</BButton>
        <BButton :disabled="validando" data-testid="btn-confirmar-validar-unidade" variant="success" @click="confirmarValidar">
          <BSpinner v-if="validando" aria-hidden="true" class="me-1" small/>
          Validar
        </BButton>
      </template>
    </BModal>

    <!-- Modal: Devolver -->
    <BModal v-model="modalDevolverAberto" :title="TEXTOS.diagnostico.MODAL_DEVOLVER_TITULO" centered>
      <BFormTextarea v-model="justificativaDevolver" :placeholder="TEXTOS.diagnostico.MODAL_DEVOLVER_PLACEHOLDER" rows="3"/>
      <BFormText v-if="mensagemErroJustificativaDevolver" class="text-danger">{{ mensagemErroJustificativaDevolver }}</BFormText>
      <template #footer>
        <BButton class="text-secondary" variant="link" @click="modalDevolverAberto = false">Cancelar</BButton>
        <BButton :disabled="devolvendo" data-testid="btn-confirmar-devolver-unidade" variant="warning" @click="confirmarDevolver">
          <BSpinner v-if="devolvendo" aria-hidden="true" class="me-1" small/>
          Devolver
        </BButton>
      </template>
    </BModal>

    <!-- Modal: Homologar -->
    <BModal v-model="modalHomologarAberto" :title="TEXTOS.diagnostico.MODAL_HOMOLOGAR_TITULO" centered>
      <BFormTextarea v-model="observacoesHomologar" :placeholder="TEXTOS.diagnostico.LABEL_OBSERVACOES" rows="3"/>
      <template #footer>
        <BButton class="text-secondary" variant="link" @click="modalHomologarAberto = false">Cancelar</BButton>
        <BButton :disabled="homologando" data-testid="btn-confirmar-homologar-unidade" variant="primary" @click="confirmarHomologar">
          <BSpinner v-if="homologando" aria-hidden="true" class="me-1" small/>
          Homologar
        </BButton>
      </template>
    </BModal>
  </LayoutPadrao>
</template>

<script lang="ts" setup>
import {
  BAccordion,
  BAccordionItem,
  BBadge,
  BButton,
  BCard,
  BCardHeader,
  BCol,
  BFormText,
  BFormTextarea,
  BListGroup,
  BListGroupItem,
  BModal,
  BRow,
  BSpinner,
  BTable,
} from 'bootstrap-vue-next';
import LayoutPadrao from '@/components/layout/LayoutPadrao.vue';
import CarregamentoPagina from '@/components/comum/CarregamentoPagina.vue';
import AppAlert from '@/components/comum/AppAlert.vue';
import EmptyState from '@/components/comum/EmptyState.vue';
import {TEXTOS} from '@/constants/textos';
import {useDiagnosticoUnidadeView} from '@/views/useDiagnosticoUnidadeView';

const props = defineProps<{
  codSubprocesso: number;
  siglaUnidade: string;
}>();
const {
  router,
  unidade,
  servidores,
  ocupacoesCriticas,
  movimentacoes,
  carregando,
  situacao,
  totalPendentes,
  retornoFluxo,
  limparRetornoFluxo,
  modalValidarAberto,
  modalDevolverAberto,
  modalHomologarAberto,
  observacoesValidar,
  justificativaDevolver,
  observacoesHomologar,
  mensagemErroJustificativaDevolver,
  podeValidar,
  podeDevolver,
  podeHomologar,
  abrirModalValidar,
  abrirModalDevolver,
  abrirModalHomologar,
  confirmarValidar,
  confirmarDevolver,
  confirmarHomologar,
  validando,
  devolvendo,
  homologando,
  varianteSituacao,
  varianteSituacaoServidor,
  formatarSituacaoServidor,
  varianteCapacitacao,
  formatarCapacitacao,
  formatarNota,
  obterGapInfo,
  ocupacoesComDescricao,
  colunasCompetencias,
  colunasOcupacoes,
} = useDiagnosticoUnidadeView(props);
</script>
