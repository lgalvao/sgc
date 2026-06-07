<template>
  <LayoutPadrao>
    <CarregamentoPagina
        v-if="carregandoInicial"
        :mensagem="TEXTOS.comum.CARREGANDO_DADOS"
    />

    <template v-else>
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

      <BAlert v-if="erroAdmins" :model-value="true" dismissible variant="danger">
        {{ erroAdmins.mensagem }}
      </BAlert>

      <div v-else-if="administradores.length === 0">
        <EmptyState
            :description="TEXTOS.administracao.EMPTY_DESCRIPTION"
            :title="TEXTOS.administracao.EMPTY_TITLE"
            icon="bi-people"
        />
      </div>

      <BTable
          v-else
          data-testid="tbl-administradores"
          :fields="camposAdmins"
          :items="administradores"
          :busy="carregandoAdmins"
          hover
          responsive
      >
        <template #table-busy>
          <div class="text-center text-primary my-2">
            <BSpinner class="align-middle me-2" small/>
            <strong>{{ TEXTOS.comum.CARREGANDO }}</strong>
          </div>
        </template>

        <template #cell(acoes)="{ item }">
          <div class="text-end">
            <LoadingButton
                :loading="removendoAdmin === item.tituloEleitoral"
                :title="TEXTOS.comum.BOTAO_REMOVER"
                class="text-secondary"
                icon="trash"
                size="sm"
                variant="link"
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
        <BAlert v-if="erroAdicionarAdmin" :model-value="true" class="mb-3" dismissible variant="danger">
          {{ erroAdicionarAdmin }}
        </BAlert>
        <BFormGroup
            class="mb-3"
        >
          <BuscadorUsuarios
              id="tituloEleitoral"
              ref="inputTituloRef"
              v-model:selecionado="usuarioSelecionado"
              v-model:termo="termoUsuario"
              :placeholder="TEXTOS.administracao.PLACEHOLDER_TITULO"
              :state="mensagemErroNovoAdmin ? false : null"
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
        <BAlert v-if="erroRemoverAdmin" :model-value="true" class="mb-3" dismissible variant="danger">
          {{ erroRemoverAdmin }}
        </BAlert>
        <p v-if="adminParaRemover">
          {{ TEXTOS.administracao.MODAL_REMOVER_PERGUNTA(adminParaRemover.nome) }}
        </p>
      </ModalConfirmacao>
    </template>
  </LayoutPadrao>
</template>

<script lang="ts" setup>
import {BAlert, BButton, BFormGroup, BFormInvalidFeedback, BSpinner, BTable} from 'bootstrap-vue-next';

import LayoutPadrao from '@/components/layout/LayoutPadrao.vue';
import PageHeader from '@/components/layout/PageHeader.vue';
import CarregamentoPagina from '@/components/comum/CarregamentoPagina.vue';
import EmptyState from '@/components/comum/EmptyState.vue';
import ModalConfirmacao from '@/components/comum/ModalConfirmacao.vue';
import LoadingButton from '@/components/comum/LoadingButton.vue';
import BuscadorUsuarios from '@/components/comum/BuscadorUsuarios.vue';
import {useAdministradoresTela} from '@/composables/useAdministradoresTela';
import {TEXTOS} from '@/constants/textos';

const {
  administradores,
  carregandoInicial,
  carregandoAdmins,
  erroAdmins,
  removendoAdmin,
  mostrarModalAdicionarAdmin,
  mostrarModalRemoverAdmin,
  adminParaRemover,
  usuarioSelecionado,
  termoUsuario,
  erroAdicionarAdmin,
  erroRemoverAdmin,
  adicionandoAdmin,
  inputTituloRef,
  mensagemErroNovoAdmin,
  abrirModalAdicionarAdmin,
  adicionarAdmin,
  confirmarRemocao,
  removerAdmin,
} = useAdministradoresTela();

const camposAdmins = [
  {key: 'nome', label: TEXTOS.administracao.CAMPO_NOME},
  {key: 'tituloEleitoral', label: TEXTOS.administracao.CAMPO_TITULO},
  {key: 'matricula', label: TEXTOS.administracao.CAMPO_MATRICULA},
  {key: 'unidadeSigla', label: TEXTOS.administracao.CAMPO_UNIDADE},
  {key: 'acoes', label: TEXTOS.administracao.CAMPO_ACOES, thClass: 'text-end'},
];
</script>
