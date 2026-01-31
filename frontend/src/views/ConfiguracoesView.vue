<template>
  <div class="container-fluid mt-4">
    <PageHeader title="Configurações" />

    <!-- Seção de Administradores -->
    <div v-if="podeGerenciarAdministradores" class="card mb-4">
      <div class="card-header bg-primary text-white d-flex justify-content-between align-items-center">
        <h5 class="mb-0">Administradores do Sistema</h5>
        <BButton variant="light" size="sm" @click="abrirModalAdicionarAdmin">
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
            icon="bi-people"
            title="Nenhum administrador cadastrado"
            description="Utilize o botão 'Adicionar administrador' para cadastrar novos administradores."
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
                      variant="outline-danger"
                      size="sm"
                      icon="trash"
                      text="Remover"
                      @click="confirmarRemocao(admin)"
                  />
                </td>
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
        <LoadingButton
            :loading="store.loading"
            variant="light"
            size="sm"
            icon="arrow-clockwise"
            text="Recarregar"
            @click="recarregar"
        />
      </div>
      <div class="card-body">
        <div v-if="store.loading" class="text-center py-4">
          <div class="spinner-border text-primary" role="status">
            <span class="visually-hidden">Carregando...</span>
          </div>
        </div>

        <BAlert v-else-if="store.error" :model-value="true" variant="danger">
          {{ store.error }}
        </BAlert>

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
              aria-describedby="diasInativacaoHelp"
            />
            <div id="diasInativacaoHelp" class="form-text">
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
              aria-describedby="diasAlertaNovoHelp"
            />
            <div id="diasAlertaNovoHelp" class="form-text">
              Dias depois de um alerta ser enviado para que deixe de ser marcado como novo.
            </div>
          </div>

          <div class="d-flex justify-content-end">
            <LoadingButton
                type="submit"
                variant="success"
                :loading="salvando"
                icon="check-lg"
                text="Salvar Configurações"
            />
          </div>
        </form>
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
        <label for="usuarioTitulo" class="form-label">Título</label>
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

<script setup lang="ts">
import {computed, onMounted, reactive, ref} from 'vue';
import EmptyState from '@/components/EmptyState.vue';
import ModalConfirmacao from '@/components/ModalConfirmacao.vue';
import PageHeader from '@/components/layout/PageHeader.vue';
import LoadingButton from '@/components/ui/LoadingButton.vue';
import {BAlert, BButton} from 'bootstrap-vue-next';
import {type Parametro, useConfiguracoesStore} from '@/stores/configuracoes';
import {useNotificacoesStore} from '@/stores/feedback';
import {usePerfilStore} from '@/stores/perfil';
import {
  adicionarAdministrador,
  type AdministradorDto,
  listarAdministradores,
  removerAdministrador
} from '@/services/administradorService';
import {normalizeError} from '@/utils/apiError';

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
const inputTituloRef = ref<HTMLInputElement | null>(null);

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

  const findCodigo = (chave: string) => store.parametros.find(p => p.chave === chave)?.codigo;

  paramsToSave.push({
    codigo: findCodigo('DIAS_INATIVACAO_PROCESSO'),
    chave: 'DIAS_INATIVACAO_PROCESSO',
    descricao: 'Dias para inativação de processos',
    valor: form.diasInativacao.toString()
  });

  paramsToSave.push({
    codigo: findCodigo('DIAS_ALERTA_NOVO'),
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
