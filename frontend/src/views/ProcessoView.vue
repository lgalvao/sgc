<template>
  <BContainer class="mt-4">
    <ErrorAlert
        :error="processosStore.lastError"
        @dismiss="processosStore.clearError()"/>

    <div v-if="processo">
      <PageHeader :title="processo.descricao" title-test-id="processo-info">
        <template #default>
          <BBadge class="mb-2" style="border-radius: 0" variant="secondary">
            Detalhes do processo
          </BBadge>

          <ProcessoInfo
              :tipo="processo.tipo"
              :tipo-label="processo.tipoLabel"
              :situacao="processo.situacao"
              :situacao-label="processo.situacaoLabel"
              :show-data-limite="false"/>
        </template>

        <template #actions>
          <BButton v-if="mostrarBotoesBloco && podeAceitarBloco" variant="success" @click="abrirModalBloco('aceitar')">
            Aceitar em bloco
          </BButton>

          <BButton v-if="mostrarBotoesBloco && podeHomologarBloco" variant="warning" class="text-white"
                   @click="abrirModalBloco('homologar')">
            Homologar em bloco
          </BButton>

          <BButton v-if="mostrarBotoesBloco && podeDisponibilizarBloco" variant="info" class="text-white"
                   @click="abrirModalBloco('disponibilizar')">
            Disponibilizar mapas em bloco
          </BButton>
        </template>
      </PageHeader>

      <ProcessoSubprocessosTable
          :participantes-hierarquia="participantesHierarquia"
          @row-click="abrirDetalhesUnidade"/>

      <ProcessoAcoes
          :mostrar-botoes-bloco="false"
          :perfil="perfilStore.perfilSelecionado"
          :situacao-processo="processo.situacao"
          @finalizar="finalizarProcesso"/>
    </div>

    <div v-else class="text-center py-5">
      <BSpinner label="Carregando detalhes do processo..." variant="primary"/>
      <p class="mt-2 text-muted">Carregando detalhes do processo...</p>
    </div>

    <!-- Modal de Ação em Bloco -->
    <ModalAcaoBloco
        :id="'modal-acao-bloco'"
        ref="modalBlocoRef"
        :titulo="tituloModalBloco"
        :texto="textoModalBloco"
        :rotulo-botao="rotuloBotaoBloco"
        :unidades="unidadesElegiveis"
        :unidades-pre-selecionadas="idsElegiveis"
        :mostrar-data-limite="acaoBlocoAtual === 'disponibilizar'"
        @confirmar="executarAcaoBloco"/>

    <ModalConfirmacao
        v-model="mostrarModalFinalizacao"
        test-id-cancelar="btn-finalizar-processo-cancelar"
        test-id-confirmar="btn-finalizar-processo-confirmar"
        titulo="Finalização de processo"
        variant="success"
        @confirmar="confirmarFinalizacao">

      <BAlert
          :fade="false"
          :model-value="true"
          variant="info">

        <i aria-hidden="true" class="bi bi-info-circle"/>
        Confirma a finalização do processo <strong>{{ processo?.descricao || '' }}</strong>?<br>
        Essa ação tornará vigentes os mapas de competências homologados e notificará todas as unidades
        participantes do processo.
      </BAlert>
    </ModalConfirmacao>
  </BContainer>
</template>

<script lang="ts" setup>
import {BAlert, BBadge, BButton, BContainer, BSpinner} from "bootstrap-vue-next";
import ModalAcaoBloco from "@/components/ModalAcaoBloco.vue";
import ModalConfirmacao from "@/components/ModalConfirmacao.vue";
import PageHeader from "@/components/layout/PageHeader.vue";
import ProcessoAcoes from "@/components/ProcessoAcoes.vue";
import ErrorAlert from "@/components/common/ErrorAlert.vue";
import ProcessoInfo from "@/components/processo/ProcessoInfo.vue";
import ProcessoSubprocessosTable from "@/components/processo/ProcessoSubprocessosTable.vue";

import {useProcessoView} from "@/composables/useProcessoView";

const {
  processosStore,
  perfilStore,
  processo,
  participantesHierarquia,
  mostrarModalFinalizacao,
  acaoBlocoAtual,
  unidadesElegiveis,
  idsElegiveis,
  mostrarBotoesBloco,
  podeAceitarBloco,
  podeHomologarBloco,
  podeDisponibilizarBloco,
  tituloModalBloco,
  textoModalBloco,
  rotuloBotaoBloco,
  abrirDetalhesUnidade,
  finalizarProcesso,
  confirmarFinalizacao,
  abrirModalBloco,
  executarAcaoBloco
} = useProcessoView();
</script>
