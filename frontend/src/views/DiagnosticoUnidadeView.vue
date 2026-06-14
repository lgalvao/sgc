<template>
  <LayoutPadrao>
    <CarregamentoPagina v-if="carregando"/>

    <template v-else>
      <PageHeader
          :subtitle="unidade?.unidadeNome"
          :title="unidade?.unidadeSigla ?? siglaUnidade"
          title-test-id="diagnostico-unidade-titulo"
      >
        <template #actions>
          <BButton
              data-testid="btn-historico-analise-unidade"
              size="sm"
              variant="outline-secondary"
              @click="abrirHistoricoAnalise"
          >
            {{ TEXTOS.diagnostico.BTN_HISTORICO_ANALISE }}
          </BButton>

          <BDropdown
              v-if="podeValidar || podeDevolver || podeHomologar"
              size="sm"
              text="Ações"
              toggle-class="text-nowrap"
              variant="outline-secondary"
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

          <BButton size="sm" variant="outline-secondary" @click="void router.back()">
            <i aria-hidden="true" class="bi bi-arrow-left me-1"/>
            {{ TEXTOS.diagnostico.BTN_VOLTAR }}
          </BButton>
        </template>
      </PageHeader>

      <AppAlert
          v-if="retornoFluxo"
          :mensagem="retornoFluxo.mensagem"
          :variante="retornoFluxo.variante"
          @dismissed="limparRetornoFluxo"
      />

      <BRow class="mb-4">
        <BCol class="mb-3 mb-md-0" md="6">
          <BCard class="h-100">
            <p class="mb-2">
              <strong>Processo:</strong> {{ contexto?.processoCodigo ?? '-' }}
            </p>
            <p class="mb-2">
              <strong>Subprocesso:</strong> {{ contexto?.subprocessoCodigo ?? '-' }}
            </p>
            <p class="mb-0">
              <strong>Situação:</strong>
              <BBadge :variant="varianteSituacao" class="ms-1">{{ situacao }}</BBadge>
            </p>
          </BCard>
        </BCol>

        <BCol md="6">
          <BCard class="h-100">
            <p class="mb-2">
              <strong>Unidade:</strong> {{ unidade?.unidadeSigla ?? siglaUnidade }}
            </p>
            <p class="mb-2">
              <strong>Nome:</strong> {{ unidade?.unidadeNome ?? '-' }}
            </p>
            <p class="mb-0">
              <strong>Responsável:</strong> {{ unidade?.responsavelTitulo ?? '-' }}
            </p>
          </BCard>
        </BCol>
      </BRow>

      <BCard class="mb-4">
        <BCardHeader><strong>Servidores participantes</strong></BCardHeader>
        <EmptyState
            v-if="servidoresExibidos.length === 0"
            :description="TEXTOS.diagnostico.VAZIO_EQUIPE_TEXTO"
            :title="TEXTOS.diagnostico.VAZIO_EQUIPE_TITULO"
            icon="bi-people"
        />
        <BTable
            v-else
            :fields="colunasServidoresParticipantes"
            :items="servidoresExibidos"
            responsive
            small
        />
      </BCard>

      <BCard class="mb-4">
        <BCardHeader><strong>{{ TEXTOS.diagnostico.TITULO_COMPETENCIAS_UNIDADE }}</strong></BCardHeader>
        <div class="p-3">
          <EmptyState
              v-if="servidoresExibidos.length === 0"
              :description="TEXTOS.diagnostico.VAZIO_EQUIPE_TEXTO"
              :title="TEXTOS.diagnostico.VAZIO_EQUIPE_TITULO"
              icon="bi-people"
          />

          <template v-else>
            <div class="mb-3">
              <div class="form-label">{{ TEXTOS.diagnostico.LABEL_SERVIDOR_ANALISADO }}</div>
              <BListGroup data-testid="lista-servidores-diagnostico-unidade">
                <BListGroupItem
                    v-for="item in servidoresExibidos"
                    :key="item.servidorTitulo"
                    :active="item.servidorTitulo === servidorSelecionadoTitulo"
                    button
                    class="d-flex align-items-center justify-content-between gap-3"
                    :data-testid="`btn-selecionar-servidor-diagnostico-unidade-${item.servidorTitulo}`"
                    @click="servidorSelecionadoTitulo = item.servidorTitulo"
                >
                  <div>
                    <div class="fw-semibold">{{ item.servidorNome }}</div>
                    <small class="text-muted">Título {{ item.servidorTitulo }}</small>
                  </div>
                  <BBadge :variant="varianteSituacaoServidor(item.situacaoServidor)">
                    {{ formatarSituacaoServidor(item.situacaoServidor) }}
                  </BBadge>
                </BListGroupItem>
              </BListGroup>
            </div>

            <BCard
                v-if="servidorSelecionado"
                class="mb-3 border-0 bg-body-tertiary"
                data-testid="detalhes-servidor-diagnostico-unidade"
            >
              <div class="d-flex flex-column gap-2 flex-md-row justify-content-md-between align-items-md-start">
                <div>
                  <div class="fw-semibold">{{ servidorSelecionado.servidorNome }}</div>
                  <small class="text-muted">Título {{ servidorSelecionado.servidorTitulo }}</small>
                </div>
                <BBadge :variant="varianteSituacaoServidor(servidorSelecionado.situacaoServidor)">
                  {{ formatarSituacaoServidor(servidorSelecionado.situacaoServidor) }}
                </BBadge>
              </div>
            </BCard>

            <BTable
                :fields="colunasCompetenciasServidor"
                :items="competenciasServidorSelecionado"
                data-testid="tbl-competencias-servidor-diagnostico-unidade"
                responsive
                small
            >
              <template #cell(importancia)="{ item }">
                {{ formatarNota(item.importancia) }}
              </template>
              <template #cell(dominio)="{ item }">
                {{ formatarNota(item.dominio) }}
              </template>
              <template #cell(situacaoCapacitacao)="{ item }">
                <span :title="formatarSituacaoCapacitacao(item.situacaoCapacitacao)">
                  {{ formatarSituacaoCapacitacaoResumida(item.situacaoCapacitacao) }}
                </span>
              </template>
            </BTable>
          </template>
        </div>
      </BCard>

      <SubprocessoMovimentacoes :movimentacoes="movimentacoesFormatadas"/>
    </template>

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
import {useRouter} from 'vue-router';
import {
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
import PageHeader from '@/components/layout/PageHeader.vue';
import CarregamentoPagina from '@/components/comum/CarregamentoPagina.vue';
import AppAlert from '@/components/comum/AppAlert.vue';
import EmptyState from '@/components/comum/EmptyState.vue';
import HistoricoAnaliseModal from '@/components/processo/HistoricoAnaliseModal.vue';
import SubprocessoMovimentacoes from '@/components/processo/SubprocessoMovimentacoes.vue';
import {TEXTOS} from '@/constants/textos';
import {useDiagnosticoUnidadeView} from '@/views/useDiagnosticoUnidadeView';

const props = defineProps<{
  codSubprocesso: number;
  siglaUnidade: string;
}>();
const router = useRouter();
const {
  contexto,
  unidade,
  carregando,
  situacao,
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
  formatarSituacaoCapacitacaoResumida,
  formatarSituacaoCapacitacao,
  formatarSituacaoServidor,
  formatarNota,
  varianteSituacaoServidor,
  servidoresExibidos,
  servidorSelecionado,
  servidorSelecionadoTitulo,
  competenciasServidorSelecionado,
  movimentacoesFormatadas,
  colunasServidoresParticipantes,
  colunasCompetenciasServidor,
} = useDiagnosticoUnidadeView(props);
</script>
