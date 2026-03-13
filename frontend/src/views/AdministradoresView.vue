<template>
  <LayoutPadrao>
    <PageHeader title="Administradores">
      <template #actions>
        <BButton
            data-testid="btn-abrir-modal-add-admin"
            size="sm"
            variant="outline-primary"
            @click="abrirModalAdicionarAdmin"
        >
          <i aria-hidden="true" class="bi bi-person-plus"></i> Adicionar administrador
        </BButton>
      </template>
    </PageHeader>

    <div v-if="carregandoAdmins" class="text-center py-4">
      <BSpinner label="Carregando..." variant="primary" />
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

    <BTable
        v-else
        :fields="camposAdmins"
        :items="administradores"
        striped
        hover
        responsive
    >
      <template #cell(acoes)="{ item }">
        <div class="text-end">
          <LoadingButton
              :loading="removendoAdmin === item.tituloEleitoral"
              icon="trash"
              size="sm"
              text="Remover"
              variant="outline-danger"
              @click="confirmarRemocao(item)"
          />
        </div>
      </template>
    </BTable>

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
      <BFormGroup
          label-for="usuarioTitulo"
          class="mb-3"
      >
        <template #label>
          Título <span aria-hidden="true" class="text-danger">*</span>
        </template>
        <BFormInput
            id="usuarioTitulo"
            ref="inputTituloRef"
            v-model="novoAdminTitulo"
            maxlength="12"
            placeholder="Digite o título eleitoral"
            required
            type="text"
            @keydown.enter.prevent="adicionarAdmin"
        />
      </BFormGroup>
    </ModalConfirmacao>

    <!-- Modal: Remover administrador -->
    <ModalConfirmacao
        v-model="mostrarModalRemoverAdmin"
        :auto-close="false"
        :loading="removendoAdmin !== null"
        ok-title="Remover"
        titulo="Confirmar remoção"
        variant="danger"
        @confirmar="removerAdmin"
    >
      <p v-if="adminParaRemover">
        Deseja realmente remover <strong>{{ adminParaRemover.nome }}</strong> como administrador do sistema?
      </p>
    </ModalConfirmacao>
  </LayoutPadrao>
</template>

<script lang="ts" setup>
import {onMounted, ref} from 'vue';
import {BAlert, BButton, BFormGroup, BFormInput, BSpinner, BTable} from 'bootstrap-vue-next';
import LayoutPadrao from '@/components/layout/LayoutPadrao.vue';
import PageHeader from '@/components/layout/PageHeader.vue';
import EmptyState from '@/components/comum/EmptyState.vue';
import ModalConfirmacao from '@/components/comum/ModalConfirmacao.vue';
import LoadingButton from '@/components/comum/LoadingButton.vue';
import {
  adicionarAdministrador,
  type AdministradorDto,
  listarAdministradores,
  removerAdministrador
} from '@/services/administradorService';
import {normalizeError} from '@/utils/apiError';
import {useNotification} from '@/composables/useNotification';

const {notify} = useNotification();

const administradores = ref<AdministradorDto[]>([]);
const carregandoAdmins = ref(false);
const erroAdmins = ref<string | null>(null);
const removendoAdmin = ref<string | null>(null);
const mostrarModalAdicionarAdmin = ref(false);
const mostrarModalRemoverAdmin = ref(false);
const adminParaRemover = ref<AdministradorDto | null>(null);
const novoAdminTitulo = ref('');
const adicionandoAdmin = ref(false);
const inputTituloRef = ref<InstanceType<typeof BFormInput> | null>(null);

const camposAdmins = [
  {key: 'nome', label: 'Nome'},
  {key: 'tituloEleitoral', label: 'Título eleitoral'},
  {key: 'matricula', label: 'Matrícula'},
  {key: 'unidadeSigla', label: 'Unidade'},
  {key: 'acoes', label: 'Ações', thClass: 'text-end'},
];

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
    notify('Digite um título eleitoral válido', 'warning');
    return;
  }

  adicionandoAdmin.value = true;
  try {
    await adicionarAdministrador(novoAdminTitulo.value.trim());
    fecharModalAdicionarAdmin();
    notify('Administrador adicionado', 'success');
    await carregarAdministradores();
  } catch (error) {
    const erro = normalizeError(error);
    notify(erro.message, 'danger');
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
    notify('Administrador removido', 'success');
    await carregarAdministradores();
    mostrarModalRemoverAdmin.value = false;
    adminParaRemover.value = null;
  } catch (error) {
    const erro = normalizeError(error);
    notify(erro.message, 'danger');
  } finally {
    removendoAdmin.value = null;
  }
}

onMounted(async () => {
  await carregarAdministradores();
});
</script>
