<template>
  <BContainer class="mt-4">
    <PageHeader title="Atividades e conhecimentos">
      <template #subtitle>
        <div class="d-flex align-items-center gap-2">
          <BButton
              aria-label="Voltar"
              class="p-0 me-2 text-decoration-none text-muted"
              data-testid="btn-cad-atividades-voltar"
              title="Voltar"
              variant="link"
              @click="router.back()"
          >
            <i aria-hidden="true" class="bi bi-arrow-left"/>
          </BButton>
          <span>{{ sigla }} - {{ nomeUnidade }}</span>
          <span
              v-if="subprocesso"
              :class="badgeClass(subprocesso.situacao)"
              class="badge fs-6"
              data-testid="cad-atividades__txt-badge-situacao"
          >{{ subprocesso.situacaoLabel || situacaoLabel(subprocesso.situacao) }}</span>
        </div>
      </template>
      <template #actions>
        <BDropdown
            v-if="codSubprocesso && (podeVerImpacto || isChefe)"
            data-testid="btn-mais-acoes"
            text="Mais ações"
            variant="outline-secondary"
            class="me-2"
        >
          <BDropdownItem
              v-if="podeVerImpacto"
              data-testid="cad-atividades__btn-impactos-mapa"
              @click="abrirModalImpacto"
          >
            <i aria-hidden="true" class="bi bi-arrow-right-circle me-2"/> Impacto no mapa
          </BDropdownItem>
          <BDropdownItem
              v-if="isChefe"
              data-testid="btn-cad-atividades-historico"
              @click="abrirModalHistorico"
          >
            <i aria-hidden="true" class="bi bi-clock-history me-2"/> Histórico de análise
          </BDropdownItem>
          <BDropdownItem
              v-if="isChefe"
              data-testid="btn-cad-atividades-importar"
              @click="mostrarModalImportar = true"
          >
            <i aria-hidden="true" class="bi bi-upload me-2"/> Importar atividades
          </BDropdownItem>
        </BDropdown>

        <LoadingButton
            v-if="!!permissoes?.podeDisponibilizarCadastro"
            :loading="loadingValidacao"
            data-testid="btn-cad-atividades-disponibilizar"
            variant="success"
            icon="check-lg"
            text="Disponibilizar"
            loading-text="Validando..."
            @click="disponibilizarCadastro"
        />
      </template>
    </PageHeader>

    <BAlert
        v-if="erroGlobal"
        :model-value="true"
        class="mb-4"
        dismissible
        variant="danger"
        @dismissed="erroGlobal = null"
    >
      {{ erroGlobal }}
    </BAlert>

    <BForm
        class="row g-2 align-items-center mb-4"
        data-testid="form-nova-atividade"
        @submit.prevent="handleAdicionarAtividade"
    >
      <BCol>
        <BFormInput
            ref="inputNovaAtividadeRef"
            v-model="novaAtividade"
            aria-label="Nova atividade"
            data-testid="inp-nova-atividade"
            :disabled="loadingAdicionar"
            placeholder="Nova atividade"
            type="text"
            required
        />
      </BCol>
      <BCol cols="auto">
        <LoadingButton
            aria-label="Adicionar atividade"
            :disabled="!codSubprocesso || !permissoes?.podeEditarMapa || !novaAtividade.trim()"
            :loading="loadingAdicionar"
            data-testid="btn-adicionar-atividade"
            size="sm"
            type="submit"
            variant="outline-primary"
            icon="plus-lg"
        />
      </BCol>
    </BForm>

    <!-- Empty State -->
    <EmptyState
        v-if="atividades?.length === 0"
        icon="bi-list-check"
        title="Lista de atividades"
        :description="`Não há atividades cadastradas. Utilize o campo acima para adicionar uma nova atividade${isChefe ? ' ou importe de outro processo' : ''}.`"
        data-testid="cad-atividades-empty-state"
    >
      <BButton
          v-if="isChefe"
          variant="outline-primary"
          size="sm"
          data-testid="btn-empty-state-importar"
          @click="mostrarModalImportar = true"
      >
        <i aria-hidden="true" class="bi bi-upload me-2"/> Importar atividades
      </BButton>
    </EmptyState>

    <div
        v-for="(atividade, idx) in atividades"
        :key="atividade.codigo || idx"
        :ref="el => setAtividadeRef(atividade.codigo, el)"
    >
      <AtividadeItem
          :atividade="atividade"
          :pode-editar="!!permissoes?.podeEditarCadastro"
          :erro-validacao="obterErroParaAtividade(atividade.codigo)"
          @atualizar-atividade="(desc) => salvarEdicaoAtividade(atividade.codigo, desc)"
          @remover-atividade="() => removerAtividade(idx)"
          @adicionar-conhecimento="(desc) => adicionarConhecimento(idx, desc)"
          @atualizar-conhecimento="(idC, desc) => salvarEdicaoConhecimento(atividade.codigo, idC, desc)"
          @remover-conhecimento="(idC) => removerConhecimento(idx, idC)"
      />
    </div>

    <ImportarAtividadesModal
        :cod-subprocesso-destino="codSubprocesso"
        :mostrar="mostrarModalImportar"
        @fechar="mostrarModalImportar = false"
        @importar="handleImportAtividades"
    />

    <ImpactoMapaModal
        v-if="codSubprocesso"
        :impacto="impactoMapa"
        :loading="loadingImpacto"
        :mostrar="mostrarModalImpacto"
        @fechar="fecharModalImpacto"
    />

    <ConfirmacaoDisponibilizacaoModal
        :mostrar="mostrarModalConfirmacao"
        :is-revisao="!!isRevisao"
        @fechar="mostrarModalConfirmacao = false"
        @confirmar="confirmarDisponibilizacao"
    />

    <HistoricoAnaliseModal
        :historico="historicoAnalises"
        :mostrar="mostrarModalHistorico"
        @fechar="mostrarModalHistorico = false"
    />

    <ModalConfirmacao
        v-model="mostrarModalConfirmacaoRemocao"
        :titulo="dadosRemocao?.tipo === 'atividade' ? 'Remover Atividade' : 'Remover Conhecimento'"
        :mensagem="dadosRemocao?.tipo === 'atividade' ? 'Confirma a remoção desta atividade e todos os conhecimentos associados?' : 'Confirma a remoção deste conhecimento?'"
        variant="danger"
        @confirmar="confirmarRemocao"
    />

  </BContainer>
</template>

<script lang="ts" setup>
import {
  BAlert,
  BButton,
  BCol,
  BContainer,
  BDropdown,
  BDropdownItem,
  BForm,
  BFormInput
} from "bootstrap-vue-next";
import {ref, watch, nextTick} from "vue";
import {badgeClass, situacaoLabel} from "@/utils";
import ImpactoMapaModal from "@/components/ImpactoMapaModal.vue";
import ImportarAtividadesModal from "@/components/ImportarAtividadesModal.vue";
import HistoricoAnaliseModal from "@/components/HistoricoAnaliseModal.vue";
import ConfirmacaoDisponibilizacaoModal from "@/components/ConfirmacaoDisponibilizacaoModal.vue";
import ModalConfirmacao from "@/components/ModalConfirmacao.vue";
import PageHeader from "@/components/layout/PageHeader.vue";
import LoadingButton from "@/components/ui/LoadingButton.vue";
import EmptyState from "@/components/EmptyState.vue";
import AtividadeItem from "@/components/AtividadeItem.vue";
import {useCadAtividadesLogic} from "@/composables/useCadAtividadesLogic";

const props = defineProps<{
  codProcesso: number | string;
  sigla: string;
}>();

const {
  router,
  isChefe,
  codSubprocesso,
  subprocesso,
  nomeUnidade,
  permissoes,
  atividades,
  isRevisao,
  historicoAnalises,
  novaAtividade,
  loadingAdicionar,
  loadingValidacao,
  loadingImpacto,
  impactoMapa,
  mostrarModalImpacto,
  mostrarModalImportar,
  mostrarModalConfirmacao,
  mostrarModalHistorico,
  mostrarModalConfirmacaoRemocao,
  dadosRemocao,
  // eslint-disable-next-line @typescript-eslint/no-unused-vars
  errosValidacao,
  erroGlobal,
  podeVerImpacto,
  adicionarAtividade,
  removerAtividade,
  confirmarRemocao,
  adicionarConhecimento,
  removerConhecimento,
  salvarEdicaoConhecimento,
  salvarEdicaoAtividade,
  handleImportAtividades,
  obterErroParaAtividade,
  setAtividadeRef,
  abrirModalHistorico,
  abrirModalImpacto,
  disponibilizarCadastro,
  confirmarDisponibilizacao,
  fecharModalImpacto
} = useCadAtividadesLogic(props);

const inputNovaAtividadeRef = ref<InstanceType<typeof BFormInput> | null>(null);

async function handleAdicionarAtividade() {
  const sucesso = await adicionarAtividade();
  if (sucesso) {
    await nextTick();
    inputNovaAtividadeRef.value?.$el?.focus();
  }
}

// Auto-focus if empty on load
watch(() => atividades.value?.length, (newLen, oldLen) => {
  if (newLen === 0 && oldLen === undefined) {
    nextTick(() => inputNovaAtividadeRef.value?.$el?.focus());
  }
}, { immediate: true });

</script>