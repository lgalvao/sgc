<template>
  <LayoutPadrao>
    <div class="col-lg-8 col-md-9 col-12">
      <PageHeader :title="TEXTOS.processo.cadastro.TITULO"/>

      <ProcessoDiagnosticoAlert
          :carregando="carregandoDiagnosticoOrganizacional"
          :exibir="exibirAlertaDiagnostico"
          :grupos="gruposDiagnostico"
          :resumo="resumoDiagnostico"
          :unidades-sem-responsavel="unidadesSemResponsavel"
          @dismiss="dispensarAlertaDiagnostico"
      />

      <BForm class="mt-4" @submit.prevent>
        <AppAlert
            v-if="notificacao"
            :dispensavel="notificacao.dispensavel"
            :mensagem="notificacao.mensagem"
            :notification="notificacao.notificacao"
            :stack-trace="notificacao.stackTrace"
            :variante="notificacao.variante"
            @dismissed="clear()"
        />

        <ProcessoFormFields
            ref="formFieldsRef"
            v-model="formData"
            :erros-campos="fieldErrors"
            :modo-edicao="!!processoEditando"
            :carregando-unidades="isLoadingUnidades"
            :unidades="unidades"
        />

        <div class="d-flex flex-wrap justify-content-end gap-2 mt-4 pt-3 border-top">
          <BButton
              :disabled="anyLoading"
              data-testid="btn-processo-cancelar-rodape"
              to="/painel"
              variant="outline-secondary"
          >
            {{ TEXTOS.processo.cadastro.BOTAO_CANCELAR }}
          </BButton>

          <LoadingButton
              v-if="processoEditando"
              :disabled="anyLoading"
              :text="TEXTOS.processo.cadastro.BOTAO_REMOVER"
              data-testid="btn-processo-remover-rodape"
              icon="trash"
              variant="outline-danger"
              @click="mostrarModalRemocao = true"
          />

          <LoadingButton
              :disabled="salvarDesabilitado"
              :loading="isSaving"
              :text="TEXTOS.processo.cadastro.BOTAO_SALVAR"
              data-testid="btn-processo-salvar-rodape"
              icon="save"
              loading-text="Salvando..."
              type="button"
              variant="outline-primary"
              @click="salvarProcesso"
          />

          <LoadingButton
              :disabled="iniciarDesabilitado"
              :loading="isStarting"
              :text="TEXTOS.processo.cadastro.BOTAO_INICIAR"
              data-testid="btn-processo-iniciar-rodape"
              icon="play-fill"
              variant="danger"
              @click="mostrarModalConfirmacao = true"
          />
        </div>
      </BForm>
    </div>

    <ProcessoCadastroModais
        v-model:mostrar-confirmacao="mostrarModalConfirmacao"
        v-model:mostrar-remocao="mostrarModalRemocao"
        :descricao="descricao"
        :carregando-confirmacao="isStarting"
        :carregando-remocao="isRemoving"
        :tipo-label="tipo || '-'"
        :total-unidades="unidadesSelecionadas.length"
        @confirmar-iniciar="confirmarIniciarProcesso"
        @confirmar-remocao="confirmarRemocao"
    />

    <ModalAcaoBloco
        id="modal-unidades-com-equipe-propria"
        ref="modalUnidadesComEquipePropriaRef"
        :mostrar-situacao="false"
        :permitir-vazio="true"
        :rotulo-botao="TEXTOS.comum.BOTAO_INICIAR"
        :texto="textoModalUnidadesComEquipePropria"
        :titulo="tituloModalUnidadesComEquipePropria"
        :unidades="unidadesComEquipePropriaSelecionadas"
        :unidades-pre-selecionadas="idsUnidadesComEquipePropriaSelecionadas"
        @confirmar="confirmarSelecaoUnidadesComEquipePropria"
    />
  </LayoutPadrao>
</template>

<script lang="ts" setup>
import {BButton, BForm} from "bootstrap-vue-next";
import LayoutPadrao from '@/components/layout/LayoutPadrao.vue';
import {computed, ref} from "vue";
import PageHeader from "@/components/layout/PageHeader.vue";
import LoadingButton from "@/components/comum/LoadingButton.vue";
import ProcessoFormFields from "@/components/processo/ProcessoFormFields.vue";
import ProcessoDiagnosticoAlert from "@/components/processo/ProcessoDiagnosticoAlert.vue";
import ProcessoCadastroModais from "@/components/processo/ProcessoCadastroModais.vue";
import ModalAcaoBloco from "@/components/processo/ModalAcaoBloco.vue";
import AppAlert from "@/components/comum/AppAlert.vue";
import {useProcessoCadastroTela} from "@/composables/useProcessoCadastroTela";
import {TEXTOS} from "@/constants/textos";

import type {FormFieldsRef} from "@/composables/useProcessoCadastroCarga";
import type {ModalUnidadesComEquipePropriaRef} from "@/composables/useProcessoMutacoes";

const formFieldsRef = ref<FormFieldsRef | null>(null);
const modalUnidadesComEquipePropriaRef = ref<ModalUnidadesComEquipePropriaRef | null>(null);
const {
  anyLoading,
  buscarUnidadesParaProcesso,
  carregandoDiagnosticoOrganizacional,
  clear,
  confirmarIniciarProcesso,
  confirmarRemocao,
  confirmarSelecaoUnidadesComEquipePropria,
  dataLimite,
  descricao,
  dispensarAlertaDiagnostico,
  exibirAlertaDiagnostico,
  fieldErrors,
  formData,
  gruposDiagnostico,
  iniciarDesabilitado,
  isFormInvalid,
  isLoadingData,
  isLoadingUnidades,
  isRemoving,
  isSaving,
  isStarting,
  mostrarModalConfirmacao,
  mostrarModalRemocao,
  notificacao,
  notify,
  notifyStructured,
  processoEditando,
  resumoDiagnostico,
  salvarDesabilitado,
  salvarProcesso,
  tipo,
  unidades,
  unidadesComEquipePropriaSelecionadas,
  unidadesSelecionadas,
  unidadesSemResponsavel,
} = useProcessoCadastroTela({
  formFieldsRef,
  modalUnidadesComEquipePropriaRef
});

const tituloModalUnidadesComEquipePropria = "Selecionar unidades participantes";
const textoModalUnidadesComEquipePropria =
    "A seleção inclui unidades com equipe própria e unidades subordinadas. Indique quais também devem participar deste processo.";

const idsUnidadesComEquipePropriaSelecionadas = computed(() =>
    unidadesComEquipePropriaSelecionadas.value.map((unidade) => unidade.codigo)
);

defineExpose({
  buscarUnidadesParaProcesso,
  dataLimite,
  descricao,
  fieldErrors,
  isFormInvalid,
  isLoadingData,
  isRemoving,
  isSaving,
  isStarting,
  mostrarModalConfirmacao,
  mostrarModalRemocao,
  notificacao,
  notify,
  notifyStructured,
  processoEditando,
  salvarProcesso,
  tipo,
  unidadesSelecionadas,
  confirmarIniciarProcesso,
  confirmarRemocao,
  confirmarSelecaoUnidadesComEquipePropria,
});
</script>
