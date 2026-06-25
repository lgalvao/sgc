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

      <AdministradoresFluxoModais
          :adicionando-admin="adicionandoAdmin"
          :admin-para-remover="adminParaRemover"
          :erro-adicionar-admin="erroAdicionarAdmin"
          :erro-remover-admin="erroRemoverAdmin"
          :input-titulo-ref="inputTituloRef"
          :mensagem-erro-novo-admin="mensagemErroNovoAdmin"
          :mostrar-modal-adicionar-admin="mostrarModalAdicionarAdmin"
          :mostrar-modal-remover-admin="mostrarModalRemoverAdmin"
          :removendo-admin="removendoAdmin"
          :termo-usuario="termoUsuario"
          :usuario-selecionado="usuarioSelecionado"
          @adicionar-admin="adicionarAdmin"
          @modal-adicionar-exibido="() => inputTituloRef?.focus()"
          @remover-admin="removerAdmin"
          @update:mostrar-modal-adicionar-admin="mostrarModalAdicionarAdmin = $event"
          @update:mostrar-modal-remover-admin="mostrarModalRemoverAdmin = $event"
          @update:termo-usuario="termoUsuario = String($event ?? '')"
          @update:usuario-selecionado="usuarioSelecionado = ($event as string | null)"
      />
    </template>
  </LayoutPadrao>
</template>

<script lang="ts" setup>
import {BAlert, BButton, BSpinner, BTable} from 'bootstrap-vue-next';

import AdministradoresFluxoModais from '@/components/administracao/AdministradoresFluxoModais.vue';
import LayoutPadrao from '@/components/layout/LayoutPadrao.vue';
import PageHeader from '@/components/layout/PageHeader.vue';
import CarregamentoPagina from '@/components/comum/CarregamentoPagina.vue';
import EmptyState from '@/components/comum/EmptyState.vue';
import LoadingButton from '@/components/comum/LoadingButton.vue';
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
