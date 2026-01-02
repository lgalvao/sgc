<template>
  <div class="container-fluid mt-4">
    <!-- Seção de Administradores -->
    <div v-if="podeGerenciarAdministradores" class="card mb-4">
      <div class="card-header bg-primary text-white d-flex justify-content-between align-items-center">
        <h5 class="mb-0">Administradores do Sistema</h5>
        <button class="btn btn-light btn-sm" @click="abrirModalAdicionarAdmin">
          <i class="bi bi-person-plus"></i> Adicionar Administrador
        </button>
      </div>
      <div class="card-body">
        <div v-if="carregandoAdmins" class="text-center py-4">
          <div class="spinner-border text-primary" role="status">
            <span class="visually-hidden">Carregando...</span>
          </div>
        </div>

        <div v-else-if="erroAdmins" class="alert alert-danger">
          {{ erroAdmins }}
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
                  <button
                    class="btn btn-sm btn-outline-danger"
                    :disabled="removendoAdmin === admin.tituloEleitoral"
                    @click="confirmarRemocao(admin)"
                  >
                    <span v-if="removendoAdmin === admin.tituloEleitoral" class="spinner-border spinner-border-sm me-1" role="status" aria-hidden="true"></span>
                    <i v-else class="bi bi-trash"></i>
                    Remover
                  </button>
                </td>
              </tr>
              <tr v-if="administradores.length === 0">
                <td colspan="5" class="text-center text-muted">Nenhum administrador cadastrado</td>
              </tr>
            </tbody>
          </table>
        </div>
      </div>
    </div>

    <!-- Seção de Configurações de Parâmetros -->
    <div class="card">
      <div class="card-header bg-primary text-white d-flex justify-content-between align-items-center">
        <h5 class="mb-0">Configurações do Sistema</h5>
        <button class="btn btn-light btn-sm" @click="recarregar">
          <i class="bi bi-arrow-clockwise"></i> Recarregar
        </button>
      </div>
      <div class="card-body">
        <div v-if="store.loading" class="text-center py-4">
          <div class="spinner-border text-primary" role="status">
            <span class="visually-hidden">Carregando...</span>
          </div>
        </div>

        <div v-else-if="store.error" class="alert alert-danger">
          {{ store.error }}
        </div>

        <form v-else @submit.prevent="salvar">
          <div class="mb-3">
            <label for="diasInativacao" class="form-label">
              Dias para inativação de processos (DIAS_INATIVACAO_PROCESSO)
            </label>
            <input
              id="diasInativacao"
              v-model="form.diasInativacao"
              type="number"
              class="form-control"
              min="1"
              required
            />
            <div class="form-text">
              Dias depois da finalização de um processo para que seja considerado inativo.
            </div>
          </div>

          <div class="mb-3">
            <label for="diasAlertaNovo" class="form-label">
              Dias para indicação de alerta como novo (DIAS_ALERTA_NOVO)
            </label>
            <input
              id="diasAlertaNovo"
              v-model="form.diasAlertaNovo"
              type="number"
              class="form-control"
              min="1"
              required
            />
            <div class="form-text">
              Dias depois de um alerta ser enviado para que deixe de ser marcado como novo.
            </div>
          </div>

          <div class="d-flex justify-content-end">
            <button type="submit" class="btn btn-success" :disabled="salvando">
              <span v-if="salvando" class="spinner-border spinner-border-sm me-2" role="status" aria-hidden="true"></span>
              Salvar Configurações
            </button>
          </div>
        </form>
      </div>
    </div>

    <!-- Modal: Adicionar Administrador -->
    <BModal v-model="mostrarModalAdicionarAdmin" title="Adicionar Administrador" hide-footer>
      <form @submit.prevent="adicionarAdmin">
        <div class="mb-3">
          <label for="usuarioTitulo" class="form-label">Título Eleitoral do Usuário</label>
          <input
            id="usuarioTitulo"
            v-model="novoAdminTitulo"
            type="text"
            class="form-control"
            placeholder="Digite o título eleitoral"
            required
            maxlength="12"
          />
        </div>
        <div class="d-flex justify-content-end gap-2">
          <button type="button" class="btn btn-secondary" @click="fecharModalAdicionarAdmin">
            Cancelar
          </button>
          <button type="submit" class="btn btn-primary" :disabled="adicionandoAdmin">
            <span v-if="adicionandoAdmin" class="spinner-border spinner-border-sm me-2" role="status" aria-hidden="true"></span>
            Adicionar
          </button>
        </div>
      </form>
    </BModal>

    <!-- Modal: Remover Administrador -->
    <BModal v-model="mostrarModalRemoverAdmin" title="Confirmar Remoção" hide-footer>
      <p v-if="adminParaRemover">
        Deseja realmente remover <strong>{{ adminParaRemover.nome }}</strong> como administrador do sistema?
      </p>
      <div class="d-flex justify-content-end gap-2">
        <button type="button" class="btn btn-secondary" @click="mostrarModalRemoverAdmin = false">
          Cancelar
        </button>
        <button 
          type="button" 
          class="btn btn-danger" 
          :disabled="removendoAdmin !== null"
          @click="removerAdmin"
        >
          <span v-if="removendoAdmin" class="spinner-border spinner-border-sm me-2" role="status" aria-hidden="true"></span>
          Remover
        </button>
      </div>
    </BModal>
  </div>
</template>

<script setup lang="ts">
import { onMounted, reactive, ref, computed } from 'vue';
import { BModal } from 'bootstrap-vue-next';
import { useConfiguracoesStore, type Parametro } from '@/stores/configuracoes';
import { useNotificacoesStore } from '@/stores/feedback';
import { usePerfilStore } from '@/stores/perfil';
import { 
  listarAdministradores, 
  adicionarAdministrador, 
  removerAdministrador,
  type AdministradorDto 
} from '@/services/administradorService';
import { normalizeError } from '@/utils/apiError';

const store = useConfiguracoesStore();
const notificacoes = useNotificacoesStore();
const perfilStore = usePerfilStore();
const salvando = ref(false);

// Administradores
const administradores = ref<AdministradorDto[]>([]);
const carregandoAdmins = ref(false);
const erroAdmins = ref<string | null>(null);
const removendoAdmin = ref<string | null>(null);
const mostrarModalAdicionarAdmin = ref(false);
const mostrarModalRemoverAdmin = ref(false);
const adminParaRemover = ref<AdministradorDto | null>(null);
const novoAdminTitulo = ref('');
const adicionandoAdmin = ref(false);

const podeGerenciarAdministradores = computed(() => {
  return perfilStore.isAdmin;
});

const form = reactive({
  diasInativacao: 30,
  diasAlertaNovo: 3
});

function atualizarFormulario() {
  form.diasInativacao = store.getDiasInativacaoProcesso();
  form.diasAlertaNovo = store.getDiasAlertaNovo();
}

async function recarregar() {
  await store.carregarConfiguracoes();
  atualizarFormulario();
}

async function salvar() {
  salvando.value = true;

  const paramsToSave: Parametro[] = [];

  const findId = (chave: string) => store.parametros.find(p => p.chave === chave)?.id;

  paramsToSave.push({
    id: findId('DIAS_INATIVACAO_PROCESSO'),
    chave: 'DIAS_INATIVACAO_PROCESSO',
    descricao: 'Dias para inativação de processos',
    valor: form.diasInativacao.toString()
  });

  paramsToSave.push({
    id: findId('DIAS_ALERTA_NOVO'),
    chave: 'DIAS_ALERTA_NOVO',
    descricao: 'Dias para indicação de alerta como novo',
    valor: form.diasAlertaNovo.toString()
  });

  const sucesso = await store.salvarConfiguracoes(paramsToSave);

  if (sucesso) {
    notificacoes.show('Sucesso', 'Configurações salvas com sucesso!', 'success');
  } else {
    notificacoes.show('Erro', 'Erro ao salvar configurações.', 'danger');
  }

  salvando.value = false;
}

// Funções de Administradores
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
  if (store.parametros.length === 0) {
    await store.carregarConfiguracoes();
  }
  atualizarFormulario();
  
  if (podeGerenciarAdministradores.value) {
    await carregarAdministradores();
  }
});
</script>
