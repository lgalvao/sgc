<template>
  <LayoutPadrao>
    <CarregamentoPagina v-if="carregando"/>

    <template v-else>
      <div class="d-flex align-items-center justify-content-between mb-4 flex-wrap gap-2">
        <div>
          <h1 class="h4 mb-1">
            <i aria-hidden="true" class="bi bi-clipboard-check text-primary me-2"/>
            {{ TEXTOS.diagnostico.TITULO_AUTOAVALIACAO }}
          </h1>
          <div v-if="contexto" class="text-muted small">
            <strong>{{ contexto.unidadeSigla }}</strong> - {{ contexto.unidadeNome }}
            <BBadge :variant="varianteSituacao" class="ms-2">
              {{ contexto.situacaoDiagnostico }}
            </BBadge>
          </div>
        </div>
        <BButton size="sm" variant="outline-secondary" @click="voltar">
          <i aria-hidden="true" class="bi bi-arrow-left me-1"/>
          {{ TEXTOS.diagnostico.BTN_VOLTAR }}
        </BButton>
      </div>

      <AppAlert
          v-if="retornoFluxo"
          :mensagem="retornoFluxo.mensagem"
          :variante="retornoFluxo.variante"
          @dismissed="limparRetornoFluxo"
      />

      <div class="mb-3 text-muted small d-flex align-items-center gap-2">
        <template v-if="salvandoAutomaticamente">
          <BSpinner small variant="secondary"/>
          {{ TEXTOS.diagnostico.LABEL_SALVANDO }}
        </template>
      </div>

      <BAlert
          v-if="ehConsensoCriado && !ehChefe"
          :model-value="true"
          class="mb-4"
          variant="warning"
      >
        <i aria-hidden="true" class="bi bi-exclamation-triangle me-2"/>
        A chefia registrou a avaliação de consenso. Revise e aprove para finalizar.
      </BAlert>

      <BAlert
          v-if="ehConsensoAprovado"
          :model-value="true"
          class="mb-4"
          variant="success"
      >
        <i aria-hidden="true" class="bi bi-check-circle me-2"/>
        Avaliação de consenso aprovada. Fluxo finalizado.
      </BAlert>

      <BCard class="mb-4">
        <BCardHeader>
          <strong>{{ TEXTOS.diagnostico.TITULO_AUTOAVALIACAO }}</strong>
          <span class="text-muted small ms-2">{{ TEXTOS.diagnostico.ESCALA_HINT }}</span>
        </BCardHeader>
        <BTable
            :fields="colunas"
            :items="competenciasComDescricao"
            hover
            responsive
            small
            striped
        >
          <template #cell(descricao)="{ item }">
            <div class="d-flex flex-column gap-2">
              <span>{{ item.descricao }}</span>
              <div v-if="item.atividades.length > 0" class="small">
                <BButton
                    :data-testid="`toggle-atividades-${item.competenciaCodigo}`"
                    size="sm"
                    variant="link"
                    class="p-0 text-decoration-none"
                    @click="alternarDetalhesCompetencia(item.competenciaCodigo)"
                >
                  {{ detalhesCompetenciaAbertos[item.competenciaCodigo] ? 'Ocultar' : 'Atividade e conhecimentos' }}
                </BButton>
                <div v-if="detalhesCompetenciaAbertos[item.competenciaCodigo]" class="mt-2">
                  <ul class="mb-0 ps-3">
                    <li v-for="atividade in item.atividades" :key="atividade.codigo" class="mb-1">
                      <strong>{{ atividade.descricao }}</strong>
                      <div class="text-muted">
                        {{ formatarConhecimentos(atividade.conhecimentos) }}
                      </div>
                    </li>
                  </ul>
                </div>
              </div>
            </div>
          </template>
          <template #cell(importancia)="{ item }">
            <BFormSelect
                v-if="podeEditar"
                :data-testid="`autoavaliacao-importancia-${item.competenciaCodigo}`"
                :disabled="!podeEditar"
                :model-value="item.importancia"
                :options="opcoesNota"
                class="form-select-sm w-auto"
                @update:model-value="(v: unknown) => atualizarNota(item.competenciaCodigo, 'importancia', normalizarValorNota(v))"
            />
            <span v-else>{{ formatarNota(item.importancia) }}</span>
          </template>

          <template #cell(dominio)="{ item }">
            <BFormSelect
                v-if="podeEditar"
                :data-testid="`autoavaliacao-dominio-${item.competenciaCodigo}`"
                :disabled="!podeEditar"
                :model-value="item.dominio"
                :options="opcoesNota"
                class="form-select-sm w-auto"
                @update:model-value="(v: unknown) => atualizarNota(item.competenciaCodigo, 'dominio', normalizarValorNota(v))"
            />
            <span v-else>{{ formatarNota(item.dominio) }}</span>
          </template>
        </BTable>
      </BCard>

      <div v-if="!ehChefe && podeEditar" class="d-flex gap-2 mb-4">
        <BButton
            :disabled="concluindo"
            data-testid="btn-concluir-autoavaliacao"
            variant="outline-success"
            @click="abrirModalConcluir"
        >
          <BSpinner v-if="concluindo" aria-hidden="true" class="me-1" small/>
          {{ TEXTOS.diagnostico.BTN_CONCLUIR_AUTOAVALIACAO }}
        </BButton>
      </div>

      <div v-if="ehConsensoCriado && !ehChefe" class="d-flex gap-2 mb-4">
        <BButton
            :disabled="aprovando"
            data-testid="btn-aprovar-consenso"
            variant="success"
            @click="abrirModalAprovar"
        >
          <BSpinner v-if="aprovando" aria-hidden="true" class="me-1" small/>
          {{ TEXTOS.diagnostico.BTN_APROVAR_CONSENSO }}
        </BButton>
      </div>

      <BCard v-if="ehChefe" class="mb-4">
        <BCardHeader>
          <strong>Equipe</strong>
          <BBadge v-if="pendentes > 0" class="ms-2" variant="warning">
            {{ pendentes }} pendente(s)
          </BBadge>
        </BCardHeader>
        <BListGroup flush>
          <BListGroupItem
              v-for="membro in itensEquipe"
              :key="membro.servidorTitulo"
              class="d-flex align-items-center justify-content-between"
          >
            <div>
              <strong>{{ membro.servidorNome }}</strong>
              <small class="text-muted ms-2">{{ membro.servidorTitulo }}</small>
            </div>
            <div class="d-flex align-items-center gap-2">
              <BBadge :variant="varianteSituacaoServidor(membro.situacaoServidor)">
                {{ formatarSituacaoServidor(membro.situacaoServidor) }}
              </BBadge>
              <BButton
                  v-if="membro.situacaoServidor === 'AUTOAVALIACAO_CONCLUIDA'"
                  :data-testid="`btn-consenso-${membro.servidorTitulo}`"
                  size="sm"
                  variant="outline-primary"
                  @click="navegarParaConsenso(membro.servidorTitulo)"
              >
                Registrar consenso
              </BButton>
              <BButton
                  v-if="podeImpossibilitar(membro.situacaoServidor)"
                  :data-testid="`btn-impossibilitar-${membro.servidorTitulo}`"
                  size="sm"
                  variant="outline-danger"
                  @click="abrirModalImpossibilitar(membro)"
              >
                {{ TEXTOS.diagnostico.BTN_IMPOSSIBILITAR }}
              </BButton>
            </div>
          </BListGroupItem>
        </BListGroup>
      </BCard>
    </template>

    <ModalConfirmacao
        v-model="modalConcluirAberto"
        :loading="concluindo"
        :mensagem="TEXTOS.diagnostico.MODAL_CONCLUIR_MENSAGEM"
        :titulo="TEXTOS.diagnostico.MODAL_CONCLUIR_TITULO"
        ok-title="Concluir"
        variant="success"
        test-id-confirmar="btn-confirmar-concluir"
        @confirmar="confirmarConcluir"
    />

    <ModalConfirmacao
        v-model="modalAprovarAberto"
        :loading="aprovando"
        :mensagem="TEXTOS.diagnostico.MODAL_APROVAR_MENSAGEM"
        :titulo="TEXTOS.diagnostico.MODAL_APROVAR_TITULO"
        ok-title="Aprovar"
        ok-variant="success"
        test-id-confirmar="btn-confirmar-aprovar"
        @confirmar="confirmarAprovar"
    />

    <BModal
        v-model="modalImpossibilitarAberto"
        :title="TEXTOS.diagnostico.MODAL_IMPOSSIBILITAR_TITULO"
        centered
        @hide="fecharModalImpossibilitar"
    >
      <p v-if="servidorParaImpossibilitar" class="mb-3">
        {{ TEXTOS.diagnostico.MODAL_IMPOSSIBILITAR_MENSAGEM(servidorParaImpossibilitar.servidorNome) }}
      </p>
      <BFormTextarea
          v-model="justificativaImpossibilidade"
          :placeholder="TEXTOS.diagnostico.MODAL_IMPOSSIBILITAR_PLACEHOLDER"
          data-testid="textarea-justificativa-impossibilidade"
          rows="3"
      />
      <BFormText v-if="mensagemErroJustificativa" class="text-danger">
        {{ mensagemErroJustificativa }}
      </BFormText>
      <template #footer>
        <BButton class="text-secondary" variant="link" @click="fecharModalImpossibilitar">Cancelar</BButton>
        <BButton
            :disabled="impossibilitando"
            data-testid="btn-confirmar-impossibilitar"
            variant="danger"
            @click="confirmarImpossibilitar"
        >
          <BSpinner v-if="impossibilitando" aria-hidden="true" class="me-1" small/>
          {{ TEXTOS.diagnostico.BTN_IMPOSSIBILITAR }}
        </BButton>
      </template>
    </BModal>
  </LayoutPadrao>
</template>

<script lang="ts" setup>
import {
  BAlert,
  BBadge,
  BButton,
  BCard,
  BCardHeader,
  BFormSelect,
  BFormText,
  BFormTextarea,
  BListGroup,
  BListGroupItem,
  BModal,
  BSpinner,
  BTable,
} from 'bootstrap-vue-next';
import LayoutPadrao from '@/components/layout/LayoutPadrao.vue';
import CarregamentoPagina from '@/components/comum/CarregamentoPagina.vue';
import AppAlert from '@/components/comum/AppAlert.vue';
import ModalConfirmacao from '@/components/comum/ModalConfirmacao.vue';
import {TEXTOS} from '@/constants/textos';
import {useAutoavaliacaoDiagnosticoView} from '@/views/useAutoavaliacaoDiagnosticoView';

const props = defineProps<{
  codSubprocesso: number;
  siglaUnidade: string;
}>();
const {
  contexto,
  carregando,
  retornoFluxo,
  limparRetornoFluxo,
  salvandoAutomaticamente,
  ehAutoavaliacaoConcluida,
  ehConsensoCriado,
  ehConsensoAprovado,
  ehChefe,
  podeEditar,
  colunas,
  competenciasComDescricao,
  detalhesCompetenciaAbertos,
  alternarDetalhesCompetencia,
  formatarConhecimentos,
  opcoesNota,
  atualizarNota,
  normalizarValorNota,
  confirmarConcluir,
  modalConcluirAberto,
  abrirModalConcluir,
  concluindo,
  modalAprovarAberto,
  abrirModalAprovar,
  confirmarAprovar,
  aprovando,
  itensEquipe,
  pendentes,
  varianteSituacaoServidor,
  formatarSituacaoServidor,
  navegarParaConsenso,
  podeImpossibilitar,
  abrirModalImpossibilitar,
  modalImpossibilitarAberto,
  fecharModalImpossibilitar,
  servidorParaImpossibilitar,
  justificativaImpossibilidade,
  mensagemErroJustificativa,
  confirmarImpossibilitar,
  impossibilitando,
  voltar,
  varianteSituacao,
  formatarNota,
} = useAutoavaliacaoDiagnosticoView(props);
</script>
