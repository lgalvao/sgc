<template>
  <BModal
      :fade="false"
      :model-value="mostrar"
      :title="tipo === 'aceitar' ? 'Aceitar cadastros em bloco' : 'Homologar cadastros em bloco'"
      centered
      hide-footer
      size="lg"
      @hide="emit('fechar')"
  >
    <BAlert
        :fade="false"
        :model-value="true"
        variant="info"
    >
      <i class="bi bi-info-circle"/>
      Selecione as unidades que terão seus cadastros {{
        tipo === 'aceitar' ? 'aceitos' : 'homologados'
      }}:
    </BAlert>

    <div class="table-responsive">
      <BTable
          :fields="fields"
          :items="unidades"
          bordered
          hover
          striped
      >
        <template #cell(selecionada)="{ item }">
          <BFormCheckbox
              :id="'chk-' + item.sigla"
              v-model="item.selecionada"
              :data-testid="'chk-unidade-' + item.sigla"
          />
        </template>
        <template #cell(sigla)="{ item }">
          <strong>{{ item.sigla }}</strong>
        </template>
      </BTable>
    </div>

    <template #footer>
      <BButton
          data-testid="modal-acao-bloco__btn-modal-cancelar"
          variant="secondary"
          @click="emit('fechar')"
      >
        <i class="bi bi-x-circle"/> Cancelar
      </BButton>
      <BButton
          :variant="tipo === 'aceitar' ? 'primary' : 'success'"
          data-testid="modal-acao-bloco__btn-confirmar-acao-bloco"
          @click="emit('confirmar', unidades)"
      >
        <i :class="tipo === 'aceitar' ? 'bi bi-check-circle' : 'bi bi-check-all'"/>
        {{ tipo === 'aceitar' ? 'Aceitar' : 'Homologar' }}
      </BButton>
    </template>
  </BModal>
</template>

<script lang="ts" setup>
import {BAlert, BButton, BFormCheckbox, BModal, BTable} from "bootstrap-vue-next";

export interface UnidadeSelecao {
  sigla: string;
  nome: string;
  situacao: string;
  selecionada: boolean;
}

defineProps<{
  mostrar: boolean;
  tipo: "aceitar" | "homologar";
  unidades: UnidadeSelecao[];
}>();

const emit = defineEmits<{
  (e: "fechar"): void;
  (e: "confirmar", unidades: UnidadeSelecao[]): void;
}>();

const fields = [
  {key: "selecionada", label: "Selecionar"},
  {key: "sigla", label: "Sigla"},
  {key: "nome", label: "Nome"},
  {key: "situacao", label: "Situação Atual"},
];
</script>
