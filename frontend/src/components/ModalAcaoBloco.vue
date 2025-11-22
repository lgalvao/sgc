<template>
  <BModal
    :model-value="mostrar"
    :title="tipo === 'aceitar' ? 'Aceitar cadastros em bloco' : 'Homologar cadastros em bloco'"
    size="lg"
    centered
    hide-footer
    @hide="emit('fechar')"
  >
    <BAlert
      variant="info"
      :model-value="true"
    >
      <i class="bi bi-info-circle" />
      Selecione as unidades que terão seus cadastros {{
        tipo === 'aceitar' ? 'aceitos' : 'homologados'
      }}:
    </BAlert>

    <div class="table-responsive">
      <table class="table table-bordered">
        <thead class="table-light">
          <tr>
            <th>Selecionar</th>
            <th>Sigla</th>
            <th>Nome</th>
            <th>Situação Atual</th>
          </tr>
        </thead>
        <tbody>
          <tr
            v-for="unidade in unidades"
            :key="unidade.sigla"
          >
            <td>
              <BFormCheckbox
                :id="'chk-' + unidade.sigla"
                v-model="unidade.selecionada"
              />
            </td>
            <td><strong>{{ unidade.sigla }}</strong></td>
            <td>{{ unidade.nome }}</td>
            <td>{{ unidade.situacao }}</td>
          </tr>
        </tbody>
      </table>
    </div>

    <template #footer>
      <BButton
        variant="secondary"
        @click="emit('fechar')"
      >
        <i class="bi bi-x-circle" /> Cancelar
      </BButton>
      <BButton
        :variant="tipo === 'aceitar' ? 'primary' : 'success'"
        @click="emit('confirmar', unidades)"
      >
        <i :class="tipo === 'aceitar' ? 'bi bi-check-circle' : 'bi bi-check-all'" />
        {{ tipo === 'aceitar' ? 'Aceitar' : 'Homologar' }}
      </BButton>
    </template>
  </BModal>
</template>

<script lang="ts" setup>
import {BAlert, BButton, BFormCheckbox, BModal} from "bootstrap-vue-next";

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
</script>
