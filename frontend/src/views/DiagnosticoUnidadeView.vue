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
              variant="outline-secondary"
              @click="abrirHistoricoAnalise"
          >
            {{ TEXTOS.diagnostico.BTN_HISTORICO_ANALISE }}
          </BButton>

          <BDropdown
              v-if="podeValidar || podeDevolver || podeHomologar"
              text="Ações"
              data-testid="dropdown-acoes-diagnostico-unidade"
              toggle-class="text-nowrap"
              variant="outline-secondary"
          >
            <BDropdownItemButton
                v-if="podeDevolver"
                :disabled="devolvendo || !habilitarDevolver"
                data-testid="btn-devolver-diagnostico-unidade"
                @click="abrirModalDevolver"
            >
              {{ TEXTOS.diagnostico.BTN_DEVOLVER }}
            </BDropdownItemButton>
            <BDropdownItemButton
                v-if="podeValidar"
                :disabled="validando || !habilitarValidar"
                data-testid="btn-validar-diagnostico-unidade"
                @click="abrirModalValidar"
            >
              Registrar aceite
            </BDropdownItemButton>
            <BDropdownItemButton
                v-if="podeHomologar"
                :disabled="homologando || !habilitarHomologar"
                data-testid="btn-homologar-diagnostico-unidade"
                @click="abrirModalHomologar"
            >
              {{ TEXTOS.diagnostico.BTN_HOMOLOGAR }}
            </BDropdownItemButton>
          </BDropdown>

          <BButton variant="outline-secondary" @click="void router.back()">
            <i aria-hidden="true" class="bi bi-arrow-left me-1"/>
            {{ TEXTOS.diagnostico.BTN_VOLTAR }}
          </BButton>
        </template>
      </SubprocessoResumoHeader>

      <Alerta
          v-if="retornoFluxo"
          :mensagem="retornoFluxo.mensagem"
          :variante="retornoFluxo.variante"
          data-testid="alert-diagnostico-unidade-feedback"
          @dismissed="limparRetornoFluxo"
      />

      <BCard class="mb-4">
        <div class="p-2 p-md-3">
          <div class="mb-3">
            <div>
              <h3 class="h4 mb-1">{{ TEXTOS.diagnostico.TITULO_COMPETENCIAS_UNIDADE }}</h3>
              <p class="text-muted mb-0">Selecione um servidor para visualizar as avaliações correspondentes.</p>
            </div>
          </div>

          <EmptyState
              v-if="servidoresExibidos.length === 0"
              :description="TEXTOS.diagnostico.VAZIO_EQUIPE_TEXTO"
              :title="TEXTOS.diagnostico.VAZIO_EQUIPE_TITULO"
              icon="bi-people"
          />

          <template v-else>
            <div class="mb-3">
              <div class="scroll-container-servidores">
                <BListGroup data-testid="lista-servidores-diagnostico-unidade">
                  <BListGroupItem
                      v-for="item in servidoresExibidos"
                      :key="item.servidorTitulo"
                      button
                      :class="[
                        'd-flex align-items-center justify-content-between gap-3',
                        item.servidorTitulo === servidorSelecionadoTitulo ? 'bg-secondary border-secondary text-white' : '',
                      ]"
                      :data-testid="`btn-selecionar-servidor-diagnostico-unidade-${item.servidorTitulo}`"
                      @click="servidorSelecionadoTitulo = item.servidorTitulo"
                  >
                    <div>
                      <div class="fw-semibold">{{ item.servidorNome }}</div>
                    </div>
                    <BBadge :variant="varianteSituacaoServidor(item.situacaoServidor)">
                      {{ formatarSituacaoServidor(item.situacaoServidor) }}
                    </BBadge>
                  </BListGroupItem>
                </BListGroup>
              </div>
            </div>

            <template v-if="servidorSelecionado">
              <EmptyState
                  v-if="!possuiDadosCompetenciasServidorSelecionado"
                  :description="TEXTOS.diagnostico.VAZIO_COMPETENCIAS_AUTOAVALIACAO_TEXTO"
                  :title="TEXTOS.diagnostico.VAZIO_COMPETENCIAS_AUTOAVALIACAO_TITULO"
                  class="my-4"
                  icon="bi-clipboard-x"
              />

              <BCard
                  v-else
                  class="mb-3 border-1"
                  data-testid="detalhes-servidor-diagnostico-unidade"
              >
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
                      <div class="text-center">{{ formatarNota(item.importancia) }}</div>
                    </template>
                    <template #cell(dominio)="{ item }">
                      <div class="text-center">{{ formatarNota(item.dominio) }}</div>
                    </template>
                    <template #cell(situacaoCapacitacao)="{ item }">
                      <span :title="formatarSituacaoCapacitacao(item.situacaoCapacitacao)">
                        {{ formatarSituacaoCapacitacaoResumida(item.situacaoCapacitacao) }}
                      </span>
                    </template>
                  </BTable>
                </div>
              </BCard>
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

    <DiagnosticoFluxoModais
        :carregando-historico="carregandoHistorico"
        :devolvendo="devolvendo"
        :erro-devolver="retornoFluxo?.variante === 'danger' ? retornoFluxo.mensagem : null"
        :erro-homologar="retornoFluxo?.variante === 'danger' ? retornoFluxo.mensagem : null"
        :erro-validar="retornoFluxo?.variante === 'danger' ? retornoFluxo.mensagem : null"
        :feedback-justificativa-devolver="mensagemErroJustificativaDevolver"
        :homologando="homologando"
        :historico-analises="historicoAnalises"
        :justificativa-devolver="justificativaDevolver"
        :modal-devolver-aberto="modalDevolverAberto"
        :modal-historico-aberto="modalHistoricoAberto"
        :modal-homologar-aberto="modalHomologarAberto"
        :modal-validar-aberto="modalValidarAberto"
        :observacoes-homologar="observacoesHomologar"
        :observacoes-validar="observacoesValidar"
        :test-id-confirmar-devolver="'btn-confirmar-devolver-unidade'"
        :test-id-confirmar-homologar="'btn-confirmar-homologar-unidade'"
        :test-id-confirmar-validar="'btn-confirmar-validar-unidade'"
        :validando="validando"
        @confirmar-devolver="confirmarDevolver"
        @confirmar-homologar="confirmarHomologar"
        @confirmar-validar="confirmarValidar"
        @update:justificativa-devolver="justificativaDevolver = $event"
        @update:modal-devolver-aberto="modalDevolverAberto = $event"
        @update:modal-historico-aberto="modalHistoricoAberto = $event"
        @update:modal-homologar-aberto="modalHomologarAberto = $event"
        @update:modal-validar-aberto="modalValidarAberto = $event"
        @update:observacoes-homologar="observacoesHomologar = $event"
        @update:observacoes-validar="observacoesValidar = $event"
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
  BListGroup,
  BListGroupItem,
  BTable,
} from 'bootstrap-vue-next';
import LayoutPadrao from '@/components/layout/LayoutPadrao.vue';
import CarregamentoPagina from '@/components/comum/CarregamentoPagina.vue';
import Alerta from '@/components/comum/Alerta.vue';
import EmptyState from '@/components/comum/EmptyState.vue';
import DiagnosticoFluxoModais from '@/components/diagnostico/DiagnosticoFluxoModais.vue';
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
  habilitarValidar,
  habilitarDevolver,
  habilitarHomologar,
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
  possuiDadosCompetenciasServidorSelecionado,
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
