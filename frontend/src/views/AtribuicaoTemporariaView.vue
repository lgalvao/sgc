<template>
  <LayoutPadrao>
    <CarregamentoPagina v-if="carregandoInicial && !unidade"/>

    <div v-else class="col-lg-8 col-md-9 col-12">
      <PageHeader
          :title="tituloPagina"
          actions-test-id="atribuicao-view__acoes"
          title-test-id="atribuicao-view__titulo"
      >
        <template v-if="unidade" #default>
          <span data-testid="atribuicao-view__sigla">{{ unidade.sigla }}</span>
        </template>
        <template #alerta>
          <Alerta
              v-if="notificacao"
              :chave="notificacao.chave"
              :dispensavel="notificacao.dispensavel ?? true"
              :mensagem="notificacao.mensagem"
              :variante="notificacao.variante"
              @dismissed="clear()"
          />

          <Alerta
              v-if="erroFormulario"
              :mensagem="erroFormulario"
              class="mt-3"
              variante="danger"
              @dismissed="erroFormulario = ''"
          />
        </template>
        <template #actions>
          <BButton :to="`/unidade/${props.codUnidade}`" variant="outline-secondary">
            <i class="bi bi-arrow-left me-1"/> {{ TEXTOS.comum.BOTAO_VOLTAR }}
          </BButton>
        </template>
      </PageHeader>

      <BForm class="mt-4" @submit.prevent="salvarAtribuicao">
        <BFormGroup
            class="mb-3"
            label-for="usuario"
        >
          <template #label>
            {{ TEXTOS.atribuicaoTemporaria.LABEL_USUARIO }} <span aria-hidden="true" class="text-danger">*</span>
          </template>
          <template #description>
            {{ TEXTOS.atribuicaoTemporaria.AJUDA_PESQUISA_USUARIO }}
          </template>
          <BuscadorUsuarios
              id="usuario"
              ref="inputUsuarioRef"
              v-model:selecionado="usuarioSelecionado"
              v-model:termo="termoUsuario"
              :placeholder="TEXTOS.atribuicaoTemporaria.SELECIONE_USUARIO"
              :state="mensagemErroUsuario ? false : null"
          />
          <BFormInvalidFeedback :state="mensagemErroUsuario ? false : null">
            {{ mensagemErroUsuario }}
          </BFormInvalidFeedback>
        </BFormGroup>

        <BRow>
          <BCol class="mb-3" md="6">
            <BFormGroup label-for="dataInicio">
              <template #label>
                {{ TEXTOS.atribuicaoTemporaria.LABEL_DATA_INICIO }} <span
                  aria-hidden="true"
                  class="text-danger">*</span>
              </template>
              <InputData
                  id="dataInicio"
                  v-model="dataInicio"
                  :min="obterHojeFormatado()"
                  :state="mensagemErroDataInicio ? false : null"
                  data-testid="input-data-inicio"
                  max="2099-12-31"
              />
              <BFormInvalidFeedback :state="mensagemErroDataInicio ? false : null">
                {{ mensagemErroDataInicio }}
              </BFormInvalidFeedback>
            </BFormGroup>
          </BCol>

          <BCol class="mb-3" md="6">
            <BFormGroup label-for="dataTermino">
              <template #label>
                {{ TEXTOS.atribuicaoTemporaria.LABEL_DATA_TERMINO }} <span
                  aria-hidden="true"
                  class="text-danger">*</span>
              </template>
              <InputData
                  id="dataTermino"
                  v-model="dataTermino"
                  :min="dataMinimaTermino"
                  :state="mensagemErroDataTermino ? false : null"
                  data-testid="input-data-termino"
                  max="2099-12-31"
              />
              <BFormInvalidFeedback :state="mensagemErroDataTermino ? false : null">
                {{ mensagemErroDataTermino }}
              </BFormInvalidFeedback>
            </BFormGroup>
          </BCol>
        </BRow>

        <BFormGroup
            class="mb-3"
            label-for="justificativa"
        >
          <template #label>
            {{ TEXTOS.atribuicaoTemporaria.LABEL_JUSTIFICATIVA }} <span aria-hidden="true" class="text-danger">*</span>
          </template>
          <EditorTextoRico
              id="justificativa"
              v-model="justificativa"
              data-testid="textarea-justificativa"
              minimo-altura="10rem"
              rotulo="Justificativa"
          />
          <BFormInvalidFeedback :state="mensagemErroJustificativa ? false : null">
            {{ mensagemErroJustificativa }}
          </BFormInvalidFeedback>
        </BFormGroup>

        <div class="d-flex justify-content-end gap-2 mt-4">
          <BButton
              v-if="modoEdicao"
              :disabled="carregando"
              class="btn-acao-footer"
              data-testid="btn-remover-atribuicao"
              variant="outline-danger"
              @click="mostrarModalRemocao = true"
          >
            {{ TEXTOS.atribuicaoTemporaria.BOTAO_REMOVER }}
          </BButton>
          <BButton
              :disabled="carregando"
              class="btn-acao-footer"
              data-testid="btn-cancelar-atribuicao"
              variant="outline-secondary"
              @click="irParaUnidade"
          >
            {{ TEXTOS.comum.BOTAO_CANCELAR }}
          </BButton>
          <LoadingButton
              :disabled="carregando"
              :loading="carregando"
              :loading-text="textoBotaoSalvando"
              :text="textoBotaoSalvar"
              class="btn-acao-footer"
              data-testid="cad-atribuicao__btn-salvar-atribuicao"
              variant="success"
              @click="salvarAtribuicao"
          />
        </div>
      </BForm>

      <AtribuicaoTemporariaFluxoModais
          :carregando="carregando"
          :mostrar-modal-remocao="mostrarModalRemocao"
          @confirmar-remocao="removerAtribuicao"
          @update:mostrar-modal-remocao="mostrarModalRemocao = $event"
      />
    </div>
  </LayoutPadrao>
</template>

<script lang="ts" setup>
import {BButton, BCol, BForm, BFormGroup, BFormInvalidFeedback, BRow} from "bootstrap-vue-next";
import {ref} from "vue";
import PageHeader from "@/components/layout/PageHeader.vue";
import LoadingButton from "@/components/comum/LoadingButton.vue";
import Alerta from "@/components/comum/Alerta.vue";
import AtribuicaoTemporariaFluxoModais from "@/components/unidade/AtribuicaoTemporariaFluxoModais.vue";
import InputData from "@/components/comum/InputData.vue";
import CarregamentoPagina from "@/components/comum/CarregamentoPagina.vue";
import BuscadorUsuarios from "@/components/comum/BuscadorUsuarios.vue";
import EditorTextoRico from "@/components/comum/EditorTextoRico.vue";
import {useAtribuicaoTemporariaTela} from "@/composables/useAtribuicaoTemporariaTela";
import {TEXTOS} from "@/constants/textos";
import {obterHojeFormatado} from "@/utils/date";
import LayoutPadrao from "@/components/layout/LayoutPadrao.vue";

const props = defineProps<{ codUnidade: number }>();
const inputUsuarioRef = ref<InstanceType<typeof BuscadorUsuarios> | null>(null);
const {
  atribuicoes,
  atribuicaoVigente,
  carregandoInicial,
  clear,
  dataInicio,
  dataMinimaTermino,
  dataTermino,
  erroFormulario,
  formularioValido,
  irParaUnidade,
  carregando,
  justificativa,
  mensagemErroDataInicio,
  mensagemErroDataTermino,
  mensagemErroJustificativa,
  mensagemErroUsuario,
  modoEdicao,
  mostrarModalRemocao,
  notificacao,
  removerAtribuicao,
  salvarAtribuicao,
  termoUsuario,
  textoBotaoSalvar,
  textoBotaoSalvando,
  tituloPagina,
  unidade,
  usuarioSelecionado,
} = useAtribuicaoTemporariaTela(props.codUnidade);

defineExpose({
  atribuicoes,
  atribuicaoVigente,
  dataInicio,
  dataTermino,
  erroFormulario,
  formularioValido,
  carregando,
  justificativa,
  mensagemErroDataInicio,
  mensagemErroDataTermino,
  mensagemErroJustificativa,
  mensagemErroUsuario,
  modoEdicao,
  mostrarModalRemocao,
  removerAtribuicao,
  salvarAtribuicao,
  termoUsuario,
  unidade,
  usuarioSelecionado,
});
</script>

<style scoped>
.btn-acao-cabecalho,
.btn-acao-footer {
  min-width: 8rem;
  justify-content: center;
}

.usuario-resultados {
  max-height: 16rem;
}
</style>
