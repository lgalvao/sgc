<template>
  <LayoutPadrao>
    <PageHeader :title="TEXTOS.administracao.TITULO">
      <template #actions>
        <div class="d-flex gap-2">
          <BButton
              data-testid="btn-abrir-modal-add-admin"
              variant="outline-primary"
              @click="abrirModalAdicionarAdmin"
          >
            <i aria-hidden="true" class="bi bi-person-plus me-1"></i> {{ TEXTOS.administracao.BOTAO_ADICIONAR }}
          </BButton>
        </div>
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
              variant="link"
              class="text-secondary"
              :title="TEXTOS.comum.BOTAO_REMOVER"
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
        :ok-title="TEXTOS.comum.BOTAO_CRIAR"
        :titulo="TEXTOS.administracao.MODAL_ADICIONAR_TITULO"
        variant="success"
        @confirmar="adicionarAdmin"
        @shown="() => inputTituloRef?.focus()"
    >
      <BAlert v-if="erroAdicionarAdmin" :model-value="true" class="mb-3" variant="danger">
        {{ erroAdicionarAdmin }}
      </BAlert>
      <BFormGroup
          label-for="tituloEleitoral"
          class="mb-3"
      >
        <template #label>
          {{ TEXTOS.administracao.LABEL_TITULO }} <span aria-hidden="true" class="text-danger">*</span>
        </template>
        <BuscadorUsuarios
            id="tituloEleitoral"
            ref="inputTituloRef"
            v-model:termo="termoUsuario"
            v-model:selecionado="usuarioSelecionado"
            :state="mensagemErroNovoAdmin ? false : null"
            :placeholder="TEXTOS.administracao.PLACEHOLDER_TITULO"
            @keydown.enter.prevent="adicionarAdmin"
        />
        <BFormInvalidFeedback :state="mensagemErroNovoAdmin ? false : null">
          {{ mensagemErroNovoAdmin }}
        </BFormInvalidFeedback>
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
      <BAlert v-if="erroRemoverAdmin" :model-value="true" class="mb-3" variant="danger">
        {{ erroRemoverAdmin }}
      </BAlert>
      <p v-if="adminParaRemover">
        {{ TEXTOS.administracao.MODAL_REMOVER_PERGUNTA(adminParaRemover.nome) }}
      </p>
    </ModalConfirmacao>
  </LayoutPadrao>
</template>

<script lang="ts" setup>
import {computed, onMounted, ref} from 'vue';
import {BAlert, BButton, BFormGroup, BFormInvalidFeedback, BSpinner, BTable} from 'bootstrap-vue-next';

import LayoutPadrao from '@/components/layout/LayoutPadrao.vue';
import PageHeader from '@/components/layout/PageHeader.vue';
import EmptyState from '@/components/comum/EmptyState.vue';
import ModalConfirmacao from '@/components/comum/ModalConfirmacao.vue';
import LoadingButton from '@/components/comum/LoadingButton.vue';
import BuscadorUsuarios from '@/components/comum/BuscadorUsuarios.vue';
import {
  adicionarAdministrador,
  type AdministradorDto,
  listarAdministradores,
  removerAdministrador
} from '@/services/administradorService';
import {normalizarErro} from '@/utils/apiError';
import {useNotification} from '@/composables/useNotification';
import {useValidacaoFormulario} from '@/composables/useValidacaoFormulario';
import {TEXTOS} from '@/constants/textos';
import {useAsyncAction} from '@/composables/useAsyncAction';

const {notify} = useNotification();
const {carregando: carregandoAdmins, erro: erroAdmins, executarSilencioso} = useAsyncAction();


const administradores = ref<AdministradorDto[]>([]);
const removendoAdmin = ref<string | null>(null);
const mostrarModalAdicionarAdmin = ref(false);
const mostrarModalRemoverAdmin = ref(false);
const adminParaRemover = ref<AdministradorDto | null>(null);
const usuarioSelecionado = ref<string | null>(null);
const termoUsuario = ref('');
const {
  validarSubmissao,
  resetarValidacao,
  deveExibirErro,
  focarPrimeiroErroInvalido
} = useValidacaoFormulario();

const erroAdicionarAdmin = ref('');
const erroRemoverAdmin = ref('');
const adicionandoAdmin = ref(false);
const inputTituloRef = ref<InstanceType<typeof BuscadorUsuarios> | null>(null);

const camposAdmins = [
  {key: 'nome', label: TEXTOS.administracao.CAMPO_NOME},
  {key: 'tituloEleitoral', label: TEXTOS.administracao.CAMPO_TITULO},
  {key: 'matricula', label: TEXTOS.administracao.CAMPO_MATRICULA},
  {key: 'unidadeSigla', label: TEXTOS.administracao.CAMPO_UNIDADE},
  {key: 'acoes', label: TEXTOS.administracao.CAMPO_ACOES, thClass: 'text-end'},
];

const mensagemErroNovoAdmin = computed(() => {
  return deveExibirErro(!termoUsuario.value.trim()) ? TEXTOS.administracao.ERRO_TITULO_INVALIDO : '';
});

async function carregarAdministradores() {
  await executarSilencioso(async () => {
    administradores.value = await listarAdministradores();
  }, TEXTOS.comum.ERRO_OPERACAO);
}

function abrirModalAdicionarAdmin() {
  termoUsuario.value = '';
  usuarioSelecionado.value = null;
  resetarValidacao();
  erroAdicionarAdmin.value = '';
  mostrarModalAdicionarAdmin.value = true;
}

function fecharModalAdicionarAdmin() {
  mostrarModalAdicionarAdmin.value = false;
  termoUsuario.value = '';
  usuarioSelecionado.value = null;
  resetarValidacao();
  erroAdicionarAdmin.value = '';

}

async function adicionarAdmin() {
  const adminId = usuarioSelecionado.value || termoUsuario.value.trim();

  if (!validarSubmissao(!!adminId)) {
    await focarPrimeiroErroInvalido();
    return;
  }

  erroAdicionarAdmin.value = '';
  adicionandoAdmin.value = true;
  try {
    await adicionarAdministrador(adminId);
    fecharModalAdicionarAdmin();
    notify(TEXTOS.administracao.SUCESSO_ADICIONADO, 'success');
    await carregarAdministradores();
  } catch (error) {
    erroAdicionarAdmin.value = normalizarErro(error).mensagem;
  } finally {
    adicionandoAdmin.value = false;
  }
}

async function confirmarRemocao(admin: AdministradorDto) {
  adminParaRemover.value = admin;
  erroRemoverAdmin.value = '';
  mostrarModalRemoverAdmin.value = true;
}

async function removerAdmin() {
  if (!adminParaRemover.value) return;

  erroRemoverAdmin.value = '';
  removendoAdmin.value = adminParaRemover.value.tituloEleitoral;
  try {
    await removerAdministrador(adminParaRemover.value.tituloEleitoral);
    notify(TEXTOS.administracao.SUCESSO_REMOVIDO, 'success');
    await carregarAdministradores();
    mostrarModalRemoverAdmin.value = false;
    adminParaRemover.value = null;
  } catch (error) {
    erroRemoverAdmin.value = normalizarErro(error).mensagem;
  } finally {
    removendoAdmin.value = null;
  }
}

onMounted(async () => {
  await carregarAdministradores();
});
</script>
