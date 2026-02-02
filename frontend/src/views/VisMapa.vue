<template>
  <BContainer class="mt-4">
    <PageHeader title="Mapa de competências técnicas">
      <template #actions>
        <BButton
            v-if="podeValidar"
            data-testid="btn-mapa-sugestoes"
            title="Apresentar sugestões"
            variant="outline-warning"
            @click="abrirModalSugestoes"
        >
          Apresentar sugestões
        </BButton>
        <BButton
            v-if="podeValidar"
            data-testid="btn-mapa-validar"
            title="Validar mapa"
            variant="outline-success"
            @click="abrirModalValidar"
        >
          Validar
        </BButton>

        <BButton
            v-if="podeValidar && temHistoricoAnalise"
            data-testid="btn-mapa-historico"
            title="Histórico de análise"
            variant="outline-secondary"
            @click="verHistorico"
        >
          Histórico de análise
        </BButton>

        <BButton
            v-if="podeAnalisar"
            v-show="podeVerSugestoes"
            data-testid="btn-mapa-ver-sugestoes"
            title="Ver sugestões"
            variant="outline-info"
            @click="verSugestoes"
        >
          Ver sugestões
        </BButton>
        <BButton
            v-if="podeAnalisar"
            data-testid="btn-mapa-historico-gestor"
            title="Histórico de análise"
            variant="outline-secondary"
            @click="verHistorico"
        >
          Histórico de análise
        </BButton>
        <BButton
            v-if="podeAnalisar"
            data-testid="btn-mapa-devolver"
            title="Devolver para ajustes"
            variant="outline-danger"
            @click="abrirModalDevolucao"
        >
          Devolver para ajustes
        </BButton>
        <BButton
            v-if="podeAnalisar"
            data-testid="btn-mapa-homologar-aceite"
            title="Aceitar"
            variant="outline-success"
            @click="abrirModalAceitar"
        >
          {{ perfilSelecionado === 'ADMIN' ? 'Homologar' : 'Registrar aceite' }}
        </BButton>
      </template>
    </PageHeader>

    <div v-if="unidade">
      <div class="mb-5 d-flex align-items-center">
        <div
            class="fs-5"
            data-testid="txt-header-unidade"
        >
          {{ unidade.sigla }} - {{ unidade.nome }}
        </div>
      </div>

      <div class="mb-4 mt-3">
        <EmptyState
            v-if="!mapa || mapa.competencias.length === 0"
            icon="bi-journal-x"
            title="Nenhuma competência cadastrada"
            description="Este mapa ainda não possui competências registradas."
        />
        <BCard
            v-for="comp in mapa?.competencias"
            :key="comp.codigo"
            class="mb-3 competencia-card"
            data-testid="vis-mapa__card-competencia"
            no-body
        >
          <BCardBody class="py-2">
            <div
                class="card-title fs-5 d-flex align-items-center position-relative competencia-titulo-card"
            >
              <strong
                  class="competencia-descricao"
                  data-testid="vis-mapa__txt-competencia-descricao"
              > {{ comp.descricao }}</strong>
            </div>
            <div class="d-flex flex-wrap gap-2 mt-2 ps-3">
              <div
                  v-for="atv in comp.atividades"
                  :key="atv.codigo"
              >
                <BCard
                    class="atividade-associada-card-item d-flex flex-column group-atividade-associada"
                    data-testid="card-atividade-associada"
                    no-body
                >
                  <BCardBody class="d-flex align-items-center py-1 px-2">
                    <span class="atividade-associada-descricao me-2">{{ atv.descricao }}</span>
                  </BCardBody>
                  <div class="conhecimentos-atividade px-2 pb-2 ps-3">
                    <span
                        v-for="conhecimento in atv.conhecimentos"
                        :key="conhecimento.descricao"
                        class="me-3 mb-1"
                        data-testid="txt-conhecimento-item"
                    >
                      {{ conhecimento.descricao }}
                    </span>
                  </div>
                </BCard>
              </div>
            </div>
          </BCardBody>
        </BCard>
      </div>
    </div>
    <div v-else>
      <p>Unidade não encontrada.</p>
    </div>

    <AceitarMapaModal
        :loading="isLoading"
        :mostrar-modal="mostrarModalAceitar"
        :perfil="perfilSelecionado || undefined"
        @fechar-modal="fecharModalAceitar"
        @confirmar-aceitacao="confirmarAceitacao"
    />

    <ModalConfirmacao
        v-model="mostrarModalSugestoes"
        :loading="isLoading"
        ok-title="Confirmar"
        titulo="Apresentar Sugestões"
        test-id-confirmar="btn-sugestoes-mapa-confirmar"
        test-id-cancelar="btn-sugestoes-mapa-cancelar"
        @confirmar="confirmarSugestoes"
        @shown="() => sugestoesTextareaRef?.$el?.focus()"
    >
      <div class="mb-3">
        <label
            class="form-label"
            for="sugestoesTextarea"
        >Sugestões para o mapa de competências:</label>
        <BFormTextarea
            id="sugestoesTextarea"
            ref="sugestoesTextareaRef"
            v-model="sugestoes"
            data-testid="inp-sugestoes-mapa-texto"
            placeholder="Digite suas sugestões para o mapa de competências..."
            rows="5"
        />
      </div>
    </ModalConfirmacao>

    <BModal
        v-model="mostrarModalVerSugestoes"
        :fade="false"
        centered
        hide-footer
        title="Sugestões"
    >
      <div class="mb-3">
        <label
            class="form-label"
            for="sugestoesVisualizacao"
        >Sugestões registradas para o mapa de competências:</label>
        <BFormTextarea
            id="sugestoesVisualizacao"
            v-model="sugestoesVisualizacao"
            data-testid="txt-ver-sugestoes-mapa"
            readonly
            rows="5"
        />
      </div>
      <template #footer>
        <BButton
            data-testid="btn-ver-sugestoes-mapa-fechar"
            variant="secondary"
            @click="fecharModalVerSugestoes"
        >
          Fechar
        </BButton>
      </template>
    </BModal>

    <ModalConfirmacao
        v-model="mostrarModalValidar"
        :loading="isLoading"
        ok-title="Validar"
        test-id-confirmar="btn-validar-mapa-confirmar"
        test-id-cancelar="btn-validar-mapa-cancelar"
        titulo="Validar Mapa de Competências"
        variant="success"
        @confirmar="confirmarValidacao"
    >
      <p>Confirma a validação do mapa de competências? Essa ação habilitará a análise por unidades superiores.</p>
    </ModalConfirmacao>

    <ModalConfirmacao
        v-model="mostrarModalDevolucao"
        :loading="isLoading"
        ok-title="Confirmar"
        test-id-confirmar="btn-devolucao-mapa-confirmar"
        test-id-cancelar="btn-devolucao-mapa-cancelar"
        titulo="Devolução"
        variant="danger"
        @confirmar="confirmarDevolucao"
        @shown="() => observacaoDevolucaoRef?.$el?.focus()"
    >
      <p>Confirma a devolução da validação do mapa para ajustes?</p>
      <div class="mb-3">
        <label
            class="form-label"
            for="observacaoDevolucao"
        >Observação:</label>
        <BFormTextarea
            id="observacaoDevolucao"
            ref="observacaoDevolucaoRef"
            v-model="observacaoDevolucao"
            data-testid="inp-devolucao-mapa-obs"
            placeholder="Digite observações sobre a devolução..."
            rows="3"
        />
      </div>
    </ModalConfirmacao>

    <HistoricoAnaliseModal
        :historico="historicoAnalise"
        :mostrar="mostrarModalHistorico"
        @fechar="fecharModalHistorico"
    />
  </BContainer>
</template>

<script lang="ts" setup>
import {
  BButton,
  BCard,
  BCardBody,
  BContainer,
  BFormTextarea,
  BModal,
} from "bootstrap-vue-next";
import EmptyState from "@/components/EmptyState.vue";
import ModalConfirmacao from "@/components/ModalConfirmacao.vue";
import PageHeader from "@/components/layout/PageHeader.vue";
import AceitarMapaModal from "@/components/AceitarMapaModal.vue";
import HistoricoAnaliseModal from "@/components/HistoricoAnaliseModal.vue";
import {ref} from "vue";
import {useVisMapa} from "@/composables/useVisMapa";

const {
  perfilSelecionado,
  mapa,
  unidade,
  podeValidar,
  podeAnalisar,
  podeVerSugestoes,
  temHistoricoAnalise,
  historicoAnalise,
  mostrarModalAceitar,
  mostrarModalSugestoes,
  mostrarModalVerSugestoes,
  mostrarModalValidar,
  mostrarModalDevolucao,
  mostrarModalHistorico,
  sugestoes,
  sugestoesVisualizacao,
  observacaoDevolucao,
  isLoading,
  confirmarSugestoes,
  confirmarValidacao,
  confirmarAceitacao,
  confirmarDevolucao,
  abrirModalAceitar,
  fecharModalAceitar,
  abrirModalSugestoes,
  verSugestoes,
  fecharModalVerSugestoes,
  abrirModalValidar,
  abrirModalDevolucao,
  abrirModalHistorico,
  fecharModalHistorico
} = useVisMapa();

const verHistorico = abrirModalHistorico;

// Refs para foco
const sugestoesTextareaRef = ref<InstanceType<typeof BFormTextarea> | null>(null);
const observacaoDevolucaoRef = ref<InstanceType<typeof BFormTextarea> | null>(null);

</script>
