<template>
  <div>
    <div
      v-if="mostrar"
      class="modal fade show"
      style="display: block;"
      tabindex="-1"
    >
      <div class="modal-dialog modal-lg">
        <div class="modal-content">
          <div class="modal-header">
            <h5 class="modal-title">
              <i :class="tipo === 'aceitar' ? 'bi bi-check-circle text-primary' : 'bi bi-check-all text-success'" />
              {{ tipo === 'aceitar' ? 'Aceitar cadastros em bloco' : 'Homologar cadastros em bloco' }}
            </h5>
            <button
              type="button"
              class="btn-close"
              @click="emit('fechar')"
            />
          </div>
          <div class="modal-body">
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
          </div>
          <div class="modal-footer">
            <button
              type="button"
              class="btn btn-secondary"
              @click="emit('fechar')"
            >
              <i class="bi bi-x-circle" /> Cancelar
            </button>
            <button
              type="button"
              class="btn"
              :class="tipo === 'aceitar' ? 'btn-primary' : 'btn-success'"
              @click="emit('confirmar', unidades)"
            >
              <i :class="tipo === 'aceitar' ? 'bi bi-check-circle' : 'bi bi-check-all'" />
              {{ tipo === 'aceitar' ? 'Aceitar' : 'Homologar' }}
            </button>
          </div>
        </div>
      </div>
    </div>
    <div
      v-if="mostrar"
      class="modal-backdrop fade show"
    />
  </div>
</template>

<script lang="ts" setup>
export interface UnidadeSelecao {
  sigla: string;
  nome: string;
  situacao: string;
  selecionada: boolean;
}

defineProps<{
  mostrar: boolean;
  tipo: 'aceitar' | 'homologar';
  unidades: UnidadeSelecao[];
}>();

const emit = defineEmits<{
  (e: 'fechar'): void
  (e: 'confirmar', unidades: UnidadeSelecao[]): void
}>();
</script>
