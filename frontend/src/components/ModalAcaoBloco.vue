<template>
  <b-modal
    v-model="show"
    :title="tipo === 'aceitar' ? 'Aceitar cadastros em bloco' : 'Homologar cadastros em bloco'"
    size="lg"
    centered
    @hidden="fechar"
  >
    <div class="alert alert-info">
      <i class="bi bi-info-circle" />
      Selecione as unidades que terão seus cadastros {{
        tipo === 'aceitar' ? 'aceitos' : 'homologados'
      }}:
    </div>

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
              <input
                :id="'chk-' + unidade.sigla"
                v-model="unidade.selecionada"
                type="checkbox"
                class="form-check-input"
              >
            </td>
            <td><strong>{{ unidade.sigla }}</strong></td>
            <td>{{ unidade.nome }}</td>
            <td>{{ unidade.situacao }}</td>
          </tr>
        </tbody>
      </table>
    </div>

    <template #footer>
      <b-button
        variant="secondary"
        @click="fechar"
      >
        <i class="bi bi-x-circle" /> Cancelar
      </b-button>
      <b-button
        :variant="tipo === 'aceitar' ? 'primary' : 'success'"
        @click="emit('confirmar', unidades)"
      >
        <i :class="tipo === 'aceitar' ? 'bi bi-check-circle' : 'bi bi-check-all'" />
        {{ tipo === 'aceitar' ? 'Aceitar' : 'Homologar' }}
      </b-button>
    </template>
  </b-modal>
</template>

<script lang="ts" setup>
import { computed } from 'vue'

export interface UnidadeSelecao {
  sigla: string;
  nome: string;
  situacao: string;
  selecionada: boolean;
}

const props = defineProps<{
  mostrar: boolean;
  tipo: 'aceitar' | 'homologar';
  unidades: UnidadeSelecao[];
}>();

const emit = defineEmits<{
  (e: 'update:mostrar', value: boolean): void
  (e: 'confirmar', unidades: UnidadeSelecao[]): void
}>();

const show = computed({
  get: () => props.mostrar,
  set: (value) => emit('update:mostrar', value)
})

function fechar() {
  emit('update:mostrar', false)
}
</script>
