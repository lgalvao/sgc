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
            <div class="display-6 fw-bold text-info">{{ situacoesCapacitacao.length }}</div>
            <div class="text-muted small mt-1">Situações de Capacitação</div>
          </BCard>
        </BCol>
      </BRow>

      <div class="d-flex justify-content-between align-items-center flex-wrap gap-2 mb-3">
        <BButton
            data-testid="btn-historico-analise-unidade"
            variant="outline-secondary"
            @click="abrirHistoricoAnalise"
        >
          {{ TEXTOS.diagnostico.BTN_HISTORICO_ANALISE }}
        </BButton>

        <BDropdown
            v-if="podeValidar || podeDevolver || podeHomologar"
            text="Ações"
            variant="outline-primary"
        >
          <BDropdownItemButton
              v-if="podeDevolver"
              :disabled="devolvendo"
              data-testid="btn-devolver-diagnostico-unidade"
              @click="abrirModalDevolver"
          >
            {{ TEXTOS.diagnostico.BTN_DEVOLVER }}
          </BDropdownItemButton>
          <BDropdownItemButton
              v-if="podeValidar"
              :disabled="validando"
              data-testid="btn-validar-diagnostico-unidade"
              @click="abrirModalValidar"
          >
            Registrar aceite
          </BDropdownItemButton>
          <BDropdownItemButton
              v-if="podeHomologar"
              :disabled="homologando"
              data-testid="btn-homologar-diagnostico-unidade"
              @click="abrirModalHomologar"
          >
            {{ TEXTOS.diagnostico.BTN_HOMOLOGAR }}
          </BDropdownItemButton>
        </BDropdown>
      </div>

      <BCard class="mb-4">
        <BCardHeader><strong>Servidores participantes</strong></BCardHeader>
        <EmptyState
            v-if="servidoresExibidos.length === 0"
            :description="TEXTOS.diagnostico.VAZIO_EQUIPE_TEXTO"
            :title="TEXTOS.diagnostico.VAZIO_EQUIPE_TITULO"
            icon="bi-people"
        />
        <ul v-else class="mb-0">
          <li v-for="servidor in servidoresExibidos" :key="servidor.servidorTitulo">
            {{ servidor.servidorNome }}
          </li>
        </ul>
      </BCard>

      <BCard class="mb-4">
        <BCardHeader><strong>Competência x Servidor</strong></BCardHeader>
        <div class="table-responsive" data-testid="matriz-diagnostico-unidade">
          <table class="table table-bordered table-sm align-middle mb-0">
            <thead>
            <tr>
              <th rowspan="2">Competência</th>
              <th
                  v-for="servidor in servidoresExibidos"
                  :key="`servidor-${servidor.servidorTitulo}`"
                  class="text-center"
                  colspan="3"
              >
                {{ servidor.servidorNome }}
              </th>
            </tr>
            <tr>
              <template v-for="servidor in servidoresExibidos" :key="`colunas-${servidor.servidorTitulo}`">
                <th class="text-center">I</th>
                <th class="text-center">D</th>
                <th class="text-center">C</th>
              </template>
            </tr>
            </thead>
            <tbody>
            <tr v-for="linha in matrizCompetencias" :key="linha.competenciaCodigo">
              <td>{{ linha.competenciaDescricao }}</td>
              <template v-for="avaliacao in linha.avaliacoesPorServidor" :key="`${linha.competenciaCodigo}-${avaliacao.servidorTitulo}`">
                <td class="text-center">{{ formatarNota(avaliacao.importancia) }}</td>
                <td class="text-center">{{ formatarNota(avaliacao.dominio) }}</td>
                <td class="text-center">{{ formatarSituacaoCapacitacaoResumida(avaliacao.situacaoCapacitacao) }}</td>
              </template>
            </tr>
            </tbody>
          </table>
        </div>
      </BCard>

      <!-- Tabela de servidores com detalhamento do consenso -->
      <BCard class="mb-4">
        <BCardHeader class="d-flex justify-content-between align-items-center">
          <strong>Servidores e Consenso</strong>
          <BBadge v-if="totalPendentes > 0" variant="warning">{{ totalPendentes }} pendente(s)</BBadge>
        </BCardHeader>

        <EmptyState
            v-if="servidoresExibidos.length === 0"
            :description="TEXTOS.diagnostico.VAZIO_EQUIPE_TEXTO"
            :title="TEXTOS.diagnostico.VAZIO_EQUIPE_TITULO"
            icon="bi-people"
        />

        <template v-else>
          <BAccordion v-for="servidor in servidoresExibidos" :key="servidor.servidorTitulo" class="mb-1">
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
          Aceitar
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

    <HistoricoAnaliseModal
        :historico="historicoAnalises"
        :loading="carregandoHistorico"
        :mostrar="modalHistoricoAberto"
        @fechar="modalHistoricoAberto = false"
    />
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
  BDropdown,
  BDropdownItemButton,
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
import HistoricoAnaliseModal from '@/components/processo/HistoricoAnaliseModal.vue';
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
  situacoesCapacitacao,
  movimentacoes,
  carregando,
  situacao,
  totalPendentes,
  retornoFluxo,
  limparRetornoFluxo,
  modalHistoricoAberto,
  carregandoHistorico,
  historicoAnalises,
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
  abrirHistoricoAnalise,
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
  formatarSituacaoCapacitacaoResumida,
  formatarNota,
  obterGapInfo,
  servidoresExibidos,
  matrizCompetencias,
  colunasCompetencias,
} = useDiagnosticoUnidadeView(props);
</script>
