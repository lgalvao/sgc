<template>
  <LayoutPadrao>
    <PageHeader :title="TEXTOS.administracao.TITULO">
      <template #actions>
        <BButton
            data-testid="btn-abrir-modal-add-admin"
            size="sm"
            variant="outline-primary"
            @click="abrirModalAdicionarAdmin"
        >
          <i aria-hidden="true" class="bi bi-person-plus"></i> {{ TEXTOS.administracao.BOTAO_ADICIONAR }}
        </BButton>
      </template>
    </PageHeader>

    <div v-if="carregandoAdmins" class="text-center py-4">
      <BSpinner :label="TEXTOS.comum.CARREGANDO" variant="primary" />
    </div>

    <BAlert v-else-if="erroAdmins" :model-value="true" variant="danger">
      {{ erroAdmins }}
    </BAlert>

    <div v-else-if="administradores.length === 0">
      <EmptyState
          :description="TEXTOS.administracao.EMPTY_DESCRIPTION"
          icon="bi-people"
          :title="TEXTOS.administracao.EMPTY_TITLE"
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
              :text="TEXTOS.comum.BOTAO_REMOVER"
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
        :ok-title="TEXTOS.comum.BOTAO_CRIAR"
        :titulo="TEXTOS.administracao.MODAL_ADICIONAR_TITULO"
        @confirmar="adicionarAdmin"
        @shown="() => inputTituloRef?.focus()"
    >
      <BFormGroup
          label-for="usuarioTitulo"
          class="mb-3"
      >
        <template #label>
          {{ TEXTOS.administracao.LABEL_TITULO }} <span aria-hidden="true" class="text-danger">*</span>
        </template>
        <BFormInput
            id="usuarioTitulo"
            ref="inputTituloRef"
            v-model="novoAdminTitulo"
            maxlength="12"
            :placeholder="TEXTOS.administracao.PLACEHOLDER_TITULO"
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
        :ok-title="TEXTOS.comum.BOTAO_REMOVER"
        :titulo="TEXTOS.administracao.MODAL_REMOVER_TITULO"
        variant="danger"
        @confirmar="removerAdmin"
    >
      <p v-if="adminParaRemover">
        {{ TEXTOS.administracao.MODAL_REMOVER_PERGUNTA(adminParaRemover.nome) }}
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
import {TEXTOS} from '@/constants/textos';

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
  {key: 'nome', label: TEXTOS.administracao.CAMPO_NOME},
  {key: 'tituloEleitoral', label: TEXTOS.administracao.CAMPO_TITULO},
  {key: 'matricula', label: TEXTOS.administracao.CAMPO_MATRICULA},
  {key: 'unidadeSigla', label: TEXTOS.administracao.CAMPO_UNIDADE},
  {key: 'acoes', label: TEXTOS.administracao.CAMPO_ACOES, thClass: 'text-end'},
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
    notify(TEXTOS.administracao.ERRO_TITULO_INVALIDO, 'warning');
    return;
  }

  adicionandoAdmin.value = true;
  try {
    await adicionarAdministrador(novoAdminTitulo.value.trim());
    fecharModalAdicionarAdmin();
    notify(TEXTOS.administracao.SUCESSO_ADICIONADO, 'success');
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
    notify(TEXTOS.administracao.SUCESSO_REMOVIDO, 'success');
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
