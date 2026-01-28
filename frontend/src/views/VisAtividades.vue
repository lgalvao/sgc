<template>
  <BContainer class="mt-4">
    <PageHeader title="Atividades e conhecimentos">
      <template #subtitle>
        <div class="unidade-cabecalho mb-0">
          <span class="unidade-sigla">{{ siglaUnidade }}</span>
          <span class="unidade-nome">{{ nomeUnidade }}</span>
        </div>
      </template>
      <template #actions>
        <BButton
            v-if="podeVerImpacto"
            data-testid="cad-atividades__btn-impactos-mapa"
            variant="outline-secondary"
            @click="abrirModalImpacto"
        >
          <i aria-hidden="true" class="bi bi-arrow-right-circle me-2"/>{{ isRevisao ? 'Ver impactos' : 'Impacto no mapa' }}
        </BButton>
        <BButton
            data-testid="btn-vis-atividades-historico"
            variant="outline-info"
            @click="abrirModalHistoricoAnalise"
        >
          Histórico de análise
        </BButton>
        <BButton
            data-testid="btn-acao-devolver"
            title="Devolver para ajustes"
            variant="secondary"
            @click="devolverCadastro"
        >
          Devolver para ajustes
        </BButton>
        <BButton
            data-testid="btn-acao-analisar-principal"
            title="Validar"
            variant="success"
            @click="validarCadastro"
        >
          {{ perfilSelecionado === Perfil.ADMIN ? 'Homologar' : 'Registrar aceite' }}
        </BButton>
      </template>
    </PageHeader>

    <!-- Lista de atividades -->
    <BCard
        v-for="(atividade) in atividades"
        :key="atividade.codigo"
        class="mb-3 atividade-card"
        no-body
    >
      <BCardBody class="py-2">
        <div
            class="card-title d-flex align-items-center atividade-edicao-row position-relative group-atividade atividade-hover-row atividade-titulo-card"
        >
          <strong
              class="atividade-descricao"
              data-testid="txt-atividade-descricao"
          >{{ atividade.descricao }}</strong>
        </div>

        <!-- Conhecimentos da atividade -->
        <div class="mt-3 ms-3">
          <div
              v-for="(conhecimento) in atividade.conhecimentos"
              :key="conhecimento.codigo"
              class="d-flex align-items-center mb-2 group-conhecimento position-relative conhecimento-hover-row"
          >
            <span data-testid="txt-conhecimento-descricao">{{ conhecimento.descricao }}</span>
          </div>
        </div>
      </BCardBody>
    </BCard>

    <!-- Modal de Impacto no Mapa -->
    <ImpactoMapaModal
        v-if="codSubprocesso"
        :impacto="impactoMapa"
        :loading="loadingImpacto"
        :mostrar="mostrarModalImpacto"
        @fechar="fecharModalImpacto"
    />

    <!-- Modal de Histórico de Análise -->
    <HistoricoAnaliseModal
        :historico="historicoAnalises"
        :mostrar="mostrarModalHistoricoAnalise"
        @fechar="fecharModalHistoricoAnalise"
    />

    <!-- Modal de Validação -->
    <ModalConfirmacao
      v-model="mostrarModalValidar"
      :titulo="isHomologacao ? 'Homologação do cadastro de atividades e conhecimentos' : (isRevisao ? 'Aceite da revisão do cadastro' : 'Validação do cadastro')"
      ok-title="Confirmar"
      variant="success"
      :loading="loadingValidacao"
      :auto-close="false"
      test-id-confirmar="btn-aceite-cadastro-confirmar"
      @confirmar="confirmarValidacao"
    >
      <p>{{
          isHomologacao ? 'Confirma a homologação do cadastro de atividades e conhecimentos?' : (isRevisao ? 'Confirma o aceite da revisão do cadastro de atividades?' : 'Confirma o aceite do cadastro de atividades?')
        }}</p>
      <BFormGroup label="Observação" label-for="observacaoValidacao" class="mb-3">
        <BFormTextarea
            id="observacaoValidacao"
            v-model="observacaoValidacao"
            data-testid="inp-aceite-cadastro-obs"
            rows="3"
        />
      </BFormGroup>
    </ModalConfirmacao>

    <!-- Modal de Devolução -->
    <ModalConfirmacao
      v-model="mostrarModalDevolver"
      :titulo="isRevisao ? 'Devolução da revisão do cadastro' : 'Devolução do cadastro'"
      ok-title="Confirmar"
      variant="danger"
      :loading="loadingDevolucao"
      :auto-close="false"
      test-id-confirmar="btn-devolucao-cadastro-confirmar"
      @confirmar="confirmarDevolucao"
    >
      <p>{{
          isRevisao ? 'Confirma a devolução da revisão do cadastro para ajustes?' : 'Confirma a devolução do cadastro para ajustes?'
        }}</p>
      <BFormGroup label="Observação" label-for="observacaoDevolucao" class="mb-3">
        <BFormTextarea
            id="observacaoDevolucao"
            v-model="observacaoDevolucao"
            data-testid="inp-devolucao-cadastro-obs"
            rows="3"
        />
      </BFormGroup>
    </ModalConfirmacao>
  </BContainer>
</template>

<script lang="ts" setup>
import {
  BButton,
  BCard,
  BCardBody,
  BContainer,
  BFormGroup,
  BFormTextarea,
} from "bootstrap-vue-next";
import HistoricoAnaliseModal from "@/components/HistoricoAnaliseModal.vue";
import ImpactoMapaModal from "@/components/ImpactoMapaModal.vue";
import ModalConfirmacao from "@/components/ModalConfirmacao.vue";
import PageHeader from "@/components/layout/PageHeader.vue";
import {useVisAtividadesLogic} from "@/composables/useVisAtividadesLogic";

const props = defineProps<{
  codProcesso: number | string;
  sigla: string;
}>();

const {
  atividades,
  siglaUnidade,
  nomeUnidade,
  isRevisao,
  isHomologacao,
  podeVerImpacto,
  codSubprocesso,
  impactoMapa,
  loadingImpacto,
  mostrarModalImpacto,
  historicoAnalises,
  mostrarModalHistoricoAnalise,
  mostrarModalValidar,
  loadingValidacao,
  observacaoValidacao,
  mostrarModalDevolver,
  loadingDevolucao,
  observacaoDevolucao,
  perfilSelecionado,
  Perfil,
  abrirModalImpacto,
  fecharModalImpacto,
  abrirModalHistoricoAnalise,
  fecharModalHistoricoAnalise,
  validarCadastro,
  devolverCadastro,
  confirmarValidacao,
  confirmarDevolucao,
} = useVisAtividadesLogic(props);

defineExpose({
  mostrarModalHistoricoAnalise,
  mostrarModalValidar,
  mostrarModalDevolver,
});
</script>

<style>
.unidade-nome {
  color: var(--bs-body-color);
  opacity: 0.85;
  padding-right: 1rem;
}

.atividade-card {
  transition: box-shadow 0.2s;
}

.atividade-descricao {
  word-break: break-word;
  max-width: 100%;
  display: inline-block;
}

.atividade-titulo-card {
  background: var(--bs-light);
  border-bottom: 1px solid var(--bs-border-color);
  padding: 0.5rem 0.75rem;
  margin-left: -0.75rem;
  margin-right: -0.75rem;
  margin-top: -0.5rem;
  border-top-left-radius: 0.375rem;
  border-top-right-radius: 0.375rem;
}

.atividade-titulo-card .atividade-descricao {
  font-size: 1.1rem;
}

.unidade-cabecalho {
  font-size: 1.1rem;
  font-weight: 500;
  display: flex;
  gap: 0.5rem;
  align-items: center;
}

.unidade-sigla {
  background: var(--bs-light);
  color: var(--bs-dark);
  font-weight: bold;
  border-radius: 0.5rem;
  letter-spacing: 1px;
}
</style>
