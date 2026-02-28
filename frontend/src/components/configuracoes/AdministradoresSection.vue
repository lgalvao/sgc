<template>
  <div class="card mb-4">
    <div class="card-header bg-primary text-white d-flex justify-content-between align-items-center">
      <h5 class="mb-0">Administradores do Sistema</h5>
      <BButton size="sm" variant="light" @click="abrirModalAdicionarAdmin">
        <i aria-hidden="true" class="bi bi-person-plus"></i> Adicionar administrador
      </BButton>
    </div>
    <div class="card-body">
      <div v-if="carregandoAdmins" class="text-center py-4">
        <div class="spinner-border text-primary" role="status">
          <span class="visually-hidden">Carregando...</span>
        </div>
      </div>

      <BAlert v-else-if="erroAdmins" :model-value="true" variant="danger">
        {{ erroAdmins }}
      </BAlert>

      <div v-else-if="administradores.length === 0">
        <EmptyState
            description="Utilize o botão 'Adicionar administrador' para cadastrar novos administradores."
            icon="bi-people"
            title="Nenhum administrador cadastrado"
        />
      </div>

      <div v-else class="table-responsive">
        <table class="table table-striped table-hover">
          <thead>
          <tr>
            <th>Nome</th>
            <th>Título Eleitoral</th>
            <th>Matrícula</th>
            <th>Unidade</th>
            <th class="text-end">Ações</th>
          </tr>
          </thead>
          <tbody>
          <tr v-for="admin in administradores" :key="admin.tituloEleitoral">
            <td>{{ admin.nome }}</td>
            <td>{{ admin.tituloEleitoral }}</td>
            <td>{{ admin.matricula }}</td>
            <td>{{ admin.unidadeSigla }}</td>
            <td class="text-end">
              <LoadingButton
                  :loading="removendoAdmin === admin.tituloEleitoral"
                  icon="trash"
                  size="sm"
                  text="Remover"
                  variant="outline-danger"
                  @click="confirmarRemocao(admin)"
              />
            </td>
          </tr>
          </tbody>
        </table>
      </div>
    </div>

    <!-- Modal: Adicionar administrador -->
    <ModalConfirmacao
        v-model="mostrarModalAdicionarAdmin"
        :auto-close="false"
        :loading="adicionandoAdmin"
        :ok-disabled="!novoAdminTitulo"
        ok-title="Adicionar"
        titulo="Adicionar administrador"
        @confirmar="adicionarAdmin"
        @shown="() => inputTituloRef?.focus()"
    >
      <div class="mb-3">
        <label class="form-label" for="usuarioTitulo">Título <span aria-hidden="true"
                                                                   class="text-danger">*</span></label>
        <input
            id="usuarioTitulo"
            ref="inputTituloRef"
            v-model="novoAdminTitulo"
            class="form-control"
            maxlength="12"
            placeholder="Digite o título eleitoral"
            required
            type="text"
            @keydown.enter.prevent="adicionarAdmin"
        />
      </div>
    </ModalConfirmacao>

    <!-- Modal: Remover Administrador -->
    <ModalConfirmacao
        v-model="mostrarModalRemoverAdmin"
        :auto-close="false"
        :loading="removendoAdmin !== null"
        ok-title="Remover"
        titulo="Confirmar Remoção"
        variant="danger"
        @confirmar="removerAdmin"
    >
      <p v-if="adminParaRemover">
        Deseja realmente remover <strong>{{ adminParaRemover.nome }}</strong> como administrador do sistema?
      </p>
    </ModalConfirmacao>
  </div>
</template>

<script lang="ts" setup>
import {onMounted, ref} from 'vue';
import EmptyState from '@/components/comum/EmptyState.vue';
import ModalConfirmacao from '@/components/comum/ModalConfirmacao.vue';
import LoadingButton from '@/components/comum/LoadingButton.vue';
import {BAlert, BButton} from 'bootstrap-vue-next';
import {
  adicionarAdministrador,
  type AdministradorDto,
  listarAdministradores,
  removerAdministrador
} from '@/services/administradorService';
import {normalizeError} from '@/utils/apiError';
import {useNotificacoesStore} from '@/stores/feedback';

const notificacoes = useNotificacoesStore();

const administradores = ref<AdministradorDto[]>([]);
const carregandoAdmins = ref(false);
const erroAdmins = ref<string | null>(null);
const removendoAdmin = ref<string | null>(null);
const mostrarModalAdicionarAdmin = ref(false);
const mostrarModalRemoverAdmin = ref(false);
const adminParaRemover = ref<AdministradorDto | null>(null);
const novoAdminTitulo = ref('');
const adicionandoAdmin = ref(false);
const inputTituloRef = ref<HTMLInputElement | null>(null);

async function carregarAdministradores() {
  carregandoAdmins.value = true;
  erroAdmins.value = null;
  try {
    administradores.value = await listarAdministradores();
  } catch (error) {
    erroAdmins.value = normalizeError(error).message;
  } finally {
    carregandoAdmins.value = false;
  }
}

function abrirModalAdicionarAdmin() {
  novoAdminTitulo.value = '';
  mostrarModalAdicionarAdmin.value = true;
}

function fecharModalAdicionarAdmin() {
  mostrarModalAdicionarAdmin.value = false;
  novoAdminTitulo.value = '';
}

async function adicionarAdmin() {
  if (!novoAdminTitulo.value.trim()) {
    notificacoes.show('Erro', 'Digite um título eleitoral válido', 'warning');
    return;
  }

  adicionandoAdmin.value = true;
  try {
    await adicionarAdministrador(novoAdminTitulo.value.trim());
    notificacoes.show('Sucesso', 'Administrador adicionado com sucesso!', 'success');
    fecharModalAdicionarAdmin();
    await carregarAdministradores();
  } catch (error) {
    const erro = normalizeError(error);
    notificacoes.show('Erro', erro.message, 'danger');
  } finally {
    adicionandoAdmin.value = false;
  }
}

async function confirmarRemocao(admin: AdministradorDto) {
  adminParaRemover.value = admin;
  mostrarModalRemoverAdmin.value = true;
}

async function removerAdmin() {
  if (!adminParaRemover.value) return;

  removendoAdmin.value = adminParaRemover.value.tituloEleitoral;
  try {
    await removerAdministrador(adminParaRemover.value.tituloEleitoral);
    notificacoes.show('Sucesso', 'Administrador removido com sucesso!', 'success');
    await carregarAdministradores();
    mostrarModalRemoverAdmin.value = false;
    adminParaRemover.value = null;
  } catch (error) {
    const erro = normalizeError(error);
    notificacoes.show('Erro', erro.message, 'danger');
  } finally {
    removendoAdmin.value = null;
  }
}

onMounted(async () => {
  await carregarAdministradores();
});
</script>
