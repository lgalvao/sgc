<template>
  <LayoutPadrao>
    <CarregamentoPagina v-if="carregando"/>

    <template v-else>
      <SubprocessoResumoHeader
          :format-data-simples="formatDataSimples"
          :format-situacao-subprocesso="formatSituacaoSubprocesso"
          :format-tipo-responsabilidade="formatTipoResponsabilidade"
          :sigla-unidade-fallback="subprocessoDetalheObrigatorio.unidade.sigla"
          :subprocesso="subprocessoDetalheObrigatorio"
      >
        <template #acoes-extras>
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
      </SubprocessoResumoHeader>

      <AppAlert
          v-if="retornoFluxo"
          :mensagem="retornoFluxo.mensagem"
          :variante="retornoFluxo.variante"
          @dismissed="limparRetornoFluxo"
      />

      <BCard class="mb-4">
        <div class="p-3 p-md-4">
          <div class="d-flex flex-column flex-md-row justify-content-between gap-2 mb-3">
            <div>
              <h3 class="h5 mb-1">{{ TEXTOS.diagnostico.TITULO_COMPETENCIAS_UNIDADE }}</h3>
              <p class="text-muted mb-0">Selecione um servidor para visualizar as competências avaliadas.</p>
            </div>
            <BBadge :variant="varianteSituacao" class="align-self-start">{{ situacao }}</BBadge>
          </div>

          <EmptyState
              v-if="servidoresExibidos.length === 0"
              :description="TEXTOS.diagnostico.VAZIO_EQUIPE_TEXTO"
              :title="TEXTOS.diagnostico.VAZIO_EQUIPE_TITULO"
              icon="bi-people"
          />

          <template v-else>
            <div class="mb-3">
              <div class="form-label">{{ TEXTOS.diagnostico.LABEL_SERVIDOR_ANALISADO }}</div>
              <div class="scroll-container-servidores">
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
            </div>

            <template v-if="servidorSelecionado">
              <BCard
                  class="mb-3 border-0 bg-body-tertiary"
                  data-testid="detalhes-servidor-diagnostico-unidade"
              >
                <div class="d-flex flex-column gap-2 flex-md-row justify-content-md-between align-items-md-start">
                  <div>
                    <div class="fw-semibold text-primary">{{ servidorSelecionado.servidorNome }}</div>
                    <small class="text-muted">Título {{ servidorSelecionado.servidorTitulo }}</small>
                  </div>
                  <BBadge :variant="varianteSituacaoServidor(servidorSelecionado.situacaoServidor)">
                    {{ formatarSituacaoServidor(servidorSelecionado.situacaoServidor) }}
                  </BBadge>
                </div>
              </BCard>

              <div class="table-responsive scroll-container-competencias">
                <BTable
                    :fields="colunasCompetenciasServidor"
                    :items="competenciasServidorSelecionado"
                    class="mb-0"
                    data-testid="tbl-competencias-servidor-diagnostico-unidade"
                    hover
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
              </div>
            </template>

            <EmptyState
                v-else
                :description="TEXTOS.diagnostico.VAZIO_CAPACITACAO_SELECAO_TEXTO"
                :title="TEXTOS.diagnostico.VAZIO_CAPACITACAO_SELECAO_TITULO"
                class="my-4"
                icon="bi-person-check"
            />
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
  BDropdown,
  BDropdownItemButton,
  BFormText,
  BFormTextarea,
  BListGroup,
  BListGroupItem,
  BModal,
  BSpinner,
  BTable,
} from 'bootstrap-vue-next';
import LayoutPadrao from '@/components/layout/LayoutPadrao.vue';
import CarregamentoPagina from '@/components/comum/CarregamentoPagina.vue';
import AppAlert from '@/components/comum/AppAlert.vue';
import EmptyState from '@/components/comum/EmptyState.vue';
import HistoricoAnaliseModal from '@/components/processo/HistoricoAnaliseModal.vue';
import SubprocessoMovimentacoes from '@/components/processo/SubprocessoMovimentacoes.vue';
import SubprocessoResumoHeader from '@/components/processo/SubprocessoResumoHeader.vue';
import {TEXTOS} from '@/constants/textos';
import {useDiagnosticoUnidadeView} from '@/views/useDiagnosticoUnidadeView';

const props = defineProps<{
  codSubprocesso: number;
  siglaUnidade: string;
}>();
const router = useRouter();
const {
  subprocessoDetalheObrigatorio,
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
  formatDataSimples,
  formatSituacaoSubprocesso,
  formatTipoResponsabilidade,
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
  colunasCompetenciasServidor,
} = useDiagnosticoUnidadeView(props);
</script>

<style scoped>
.scroll-container-servidores {
  max-height: 200px;
  overflow-y: auto;
  border: 1px solid var(--bs-border-color);
  border-radius: var(--bs-border-radius);
}

.scroll-container-competencias {
  max-height: 420px;
  overflow-y: auto;
}

</style>
