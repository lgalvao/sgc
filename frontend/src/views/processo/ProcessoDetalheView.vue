<template>
  <LayoutPadrao>
    <ErrorAlert
        :error="processosStore.lastError"
        @dismiss="processosStore.clearError()"/>

    <div v-if="processo">
      <PageHeader
          :title="processo.descricao"
          :etapa="`Etapa atual: ${processo.situacaoLabel || processo.situacao}`"
          :proxima-acao="proximaAcaoProcesso"
          title-test-id="processo-info"
      >
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

          <BButton
v-if="mostrarBotoesBloco && podeHomologarBloco" variant="warning" class="text-white"
                   @click="abrirModalBloco('homologar')">
            Homologar em bloco
          </BButton>

          <BButton
v-if="mostrarBotoesBloco && podeDisponibilizarBloco" variant="info" class="text-white"
                   @click="abrirModalBloco('disponibilizar')">
            Disponibilizar mapas em bloco
          </BButton>
        </template>
      </PageHeader>

      <ProcessoSubprocessosTable
          :participantes-hierarquia="participantesHierarquia"
          @row-click="abrirDetalhesUnidade"/>

      <ProcessoAcoes
          :pode-aceitar-bloco="podeAceitarBloco"
          :pode-homologar-bloco="podeHomologarBloco"
          :pode-finalizar="podeFinalizar"
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
  </LayoutPadrao>
</template>

<script lang="ts" setup>
import {BAlert, BBadge, BButton, BSpinner} from "bootstrap-vue-next";
import {computed} from "vue";
import ModalAcaoBloco from "@/components/processo/ModalAcaoBloco.vue";
import ModalConfirmacao from "@/components/comum/ModalConfirmacao.vue";
import LayoutPadrao from "@/components/layout/LayoutPadrao.vue";
import PageHeader from "@/components/layout/PageHeader.vue";
import ProcessoAcoes from "@/components/processo/ProcessoAcoes.vue";
import ErrorAlert from "@/components/comum/ErrorAlert.vue";
import ProcessoInfo from "@/components/processo/ProcessoInfo.vue";
import ProcessoSubprocessosTable from "@/components/processo/ProcessoSubprocessosTable.vue";
import {useProximaAcao} from "@/composables/useProximaAcao";

import {useProcessoView} from "@/composables/useProcessoView";

const {
  processosStore,
  perfilStore,
  processo,
  participantesHierarquia,
  modalBlocoRef,
  mostrarModalFinalizacao,
  acaoBlocoAtual,
  unidadesElegiveis,
  idsElegiveis,
  mostrarBotoesBloco,
  podeAceitarBloco,
  podeHomologarBloco,
  podeDisponibilizarBloco,
  podeFinalizar,
  tituloModalBloco,
  textoModalBloco,
  rotuloBotaoBloco,
  mensagemSucessoAcaoBloco,
  abrirDetalhesUnidade,
  finalizarProcesso,
  confirmarFinalizacao,
  abrirModalBloco,
  executarAcaoBloco
} = useProcessoView();
const {obterProximaAcao} = useProximaAcao();

const proximaAcaoProcesso = computed(() => obterProximaAcao({
  perfil: perfilStore.perfilSelecionado,
  situacao: processo.value?.situacaoLabel || processo.value?.situacao,
  podeFinalizar: podeFinalizar.value,
}));

defineExpose({
  abrirDetalhesUnidade,
  executarAcaoBloco,
  acaoBlocoAtual,
  unidadesElegiveis,
  perfilStore,
  tituloModalBloco,
  textoModalBloco,
  rotuloBotaoBloco,
  mensagemSucessoAcaoBloco,
});
</script>
