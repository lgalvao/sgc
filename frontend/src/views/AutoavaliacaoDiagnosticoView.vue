<template>
  <LayoutPadrao>
    <CarregamentoPagina v-if="carregando"/>

    <template v-else>
      <div :class="{'cursor-salvando': salvandoAutomaticamente}">
      <PageHeader
          :subtitle="contexto ? `${contexto.unidadeSigla} - ${contexto.unidadeNome}` : undefined"
          :title="TEXTOS.diagnostico.TITULO_AUTOAVALIACAO"
      >
        <template #actions>
          <BButton
              v-if="!ehChefe && podeConcluirAutoavaliacao"
              :disabled="concluindo || !habilitarConcluirAutoavaliacao"
              data-testid="btn-concluir-autoavaliacao"
              size="sm"
              variant="success"
              @click="abrirModalConcluir"
          >
            <BSpinner v-if="concluindo" aria-hidden="true" class="me-1" small/>
            {{ TEXTOS.diagnostico.BTN_CONCLUIR_AUTOAVALIACAO }}
          </BButton>
          <BButton size="sm" variant="outline-secondary" @click="voltar">
            <i aria-hidden="true" class="bi bi-arrow-left me-1"/>
            {{ TEXTOS.diagnostico.BTN_VOLTAR }}
          </BButton>
        </template>
      </PageHeader>

      <AppAlert
          v-if="retornoFluxo"
          :mensagem="retornoFluxo.mensagem"
          :variante="retornoFluxo.variante"
          @dismissed="limparRetornoFluxo"
      />

      <BAlert
          v-if="ehConsensoAprovado"
          :model-value="true"
          class="mb-4"
          variant="success"
      >
        <i aria-hidden="true" class="bi bi-check-circle me-2"/>
        A avaliação de consenso já foi aprovada.
      </BAlert>

      <BCard class="mb-4" style="max-width: 750px;">
        <BTable
            :fields="colunas"
            :items="competenciasComDescricao"
            hover
            responsive
            small
        >
          <template #cell(descricao)="{ item }">
            <div class="d-flex flex-column gap-2">
              <span>{{ item.descricao }}</span>
              <div v-if="item.atividades.length > 0" class="small">
                <BButton
                    :data-testid="`toggle-atividades-${item.competenciaCodigo}`"
                    size="sm"
                    variant="link-dark"
                    class="p-0 text-decoration-none d-flex align-items-center"
                    @click="alternarDetalhesCompetencia(item.competenciaCodigo)"
                >
                  <i
                      :class="['bi me-1', detalhesCompetenciaAbertos[item.competenciaCodigo] ? 'bi-chevron-down' : 'bi-chevron-right']"
                      aria-hidden="true"
                  />
                  Atividade
                </BButton>
                <BCollapse :id="`collapse-atividades-${item.competenciaCodigo}`" :model-value="detalhesCompetenciaAbertos[item.competenciaCodigo]">
                  <div class="mt-2">
                    <ul class="mb-0 ps-3">
                      <li v-for="atividade in item.atividades" :key="atividade.codigo" class="mb-1">
                        <strong>{{ atividade.descricao }}</strong>
                        <div class="text-muted">
                          {{ formatarConhecimentos(atividade.conhecimentos) }}
                        </div>
                      </li>
                    </ul>
                  </div>
                </BCollapse>
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
                class="form-select-sm w-auto mx-auto"
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
                class="form-select-sm w-auto mx-auto"
                @update:model-value="(v: unknown) => atualizarNota(item.competenciaCodigo, 'dominio', normalizarValorNota(v))"
            />
            <span v-else>{{ formatarNota(item.dominio) }}</span>
          </template>
        </BTable>
      </BCard>

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
                  v-if="membro.podeManterConsenso"
                  :data-testid="`btn-consenso-${membro.servidorTitulo}`"
                  size="sm"
                  variant="primary"
                  @click="navegarParaConsenso(membro.servidorTitulo)"
              >
                Registrar consenso
              </BButton>
              <BButton
                  v-if="membro.podeImpossibilitar"
                  :data-testid="`btn-impossibilitar-${membro.servidorTitulo}`"
                  size="sm"
                  variant="danger"
                  @click="abrirModalImpossibilitar(membro)"
              >
                {{ TEXTOS.diagnostico.BTN_IMPOSSIBILITAR }}
              </BButton>
            </div>
          </BListGroupItem>
        </BListGroup>
      </BCard>
      </div>
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

    <ModalObservacaoAcao
        :feedback-observacao="mensagemErroJustificativa"
        :loading="impossibilitando"
        :model-value="modalImpossibilitarAberto"
        :observacao="justificativaImpossibilidade"
        :obrigatoria="true"
        :placeholder="TEXTOS.diagnostico.MODAL_IMPOSSIBILITAR_PLACEHOLDER"
        :texto="servidorParaImpossibilitar ? TEXTOS.diagnostico.MODAL_IMPOSSIBILITAR_MENSAGEM(servidorParaImpossibilitar.servidorNome) : ''"
        :texto-acao="TEXTOS.diagnostico.BTN_IMPOSSIBILITAR"
        :titulo="TEXTOS.diagnostico.MODAL_IMPOSSIBILITAR_TITULO"
        input-data-testid="textarea-justificativa-impossibilidade"
        label="Justificativa"
        test-id-confirmar="btn-confirmar-impossibilitar"
        variant-acao="danger"
        @confirmar="confirmarImpossibilitar"
        @fechar="fecharModalImpossibilitar"
        @update:model-value="modalImpossibilitarAberto = $event"
        @update:observacao="justificativaImpossibilidade = $event"
    />
  </LayoutPadrao>
</template>

<script lang="ts" setup>
import {
  BAlert,
  BBadge,
  BButton,
  BCard,
  BCardHeader,
  BCollapse,
  BFormSelect,
  BListGroup,
  BListGroupItem,
  BSpinner,
  BTable,
} from 'bootstrap-vue-next';
import LayoutPadrao from '@/components/layout/LayoutPadrao.vue';
import PageHeader from '@/components/layout/PageHeader.vue';
import CarregamentoPagina from '@/components/comum/CarregamentoPagina.vue';
import AppAlert from '@/components/comum/AppAlert.vue';
import ModalConfirmacao from '@/components/comum/ModalConfirmacao.vue';
import ModalObservacaoAcao from '@/components/comum/ModalObservacaoAcao.vue';
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
  ehConsensoAprovado,
  ehChefe,
  podeConcluirAutoavaliacao,
  habilitarConcluirAutoavaliacao,
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
  itensEquipe,
  pendentes,
  varianteSituacaoServidor,
  formatarSituacaoServidor,
  navegarParaConsenso,
  abrirModalImpossibilitar,
  modalImpossibilitarAberto,
  fecharModalImpossibilitar,
  servidorParaImpossibilitar,
  justificativaImpossibilidade,
  mensagemErroJustificativa,
  confirmarImpossibilitar,
  impossibilitando,
  voltar,
  formatarNota,
} = useAutoavaliacaoDiagnosticoView(props);
</script>

<style scoped>
.cursor-salvando,
.cursor-salvando * {
  cursor: wait !important;
}
</style>
