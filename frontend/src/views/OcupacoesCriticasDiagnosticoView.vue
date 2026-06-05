<template>
  <LayoutPadrao>
    <CarregamentoPagina v-if="carregando"/>

    <template v-else>
      <!-- Cabeçalho -->
      <div class="d-flex align-items-center justify-content-between mb-4 flex-wrap gap-2">
        <div>
          <h1 class="h4 mb-1">
            {{ TEXTOS.diagnostico.TITULO_SITUACAO_CAPACITACAO }}
          </h1>
          <div v-if="unidade" class="text-muted small">
            <strong>{{ unidade.unidadeSigla }}</strong>
          </div>
        </div>
        <BButton size="sm" variant="outline-secondary" @click="void router.back()">
          <i aria-hidden="true" class="bi bi-arrow-left me-1"/>
          {{ TEXTOS.diagnostico.BTN_VOLTAR }}
        </BButton>
      </div>

      <!-- Alertas -->
      <AppAlert
          v-if="erroMensagem"
          :mensagem="erroMensagem"
          variante="danger"
          @dismissed="erroMensagem = ''"
      />

      <!-- Badge de autosave -->
      <div class="mb-3 text-muted small d-flex align-items-center gap-2">
        <template v-if="salvandoAutomaticamente">
          <BSpinner small variant="secondary"/>
          {{ TEXTOS.diagnostico.LABEL_SALVANDO }}
        </template>
        <template v-else-if="autoguardado">
          <i aria-hidden="true" class="bi bi-check-circle text-success"/>
          {{ TEXTOS.diagnostico.LABEL_AUTOGUARDADO }}
        </template>
      </div>

      <!-- Tabela de situação de capacitação -->
      <BCard class="mb-4">
        <EmptyState
            v-if="ocupacoesLocais.length === 0"
            :description="TEXTOS.diagnostico.VAZIO_CAPACITACAO_TEXTO"
            :title="TEXTOS.diagnostico.VAZIO_CAPACITACAO_TITULO"
            icon="bi-award"
        />

        <div v-else class="table-responsive">
          <table class="table table-sm table-hover align-middle mb-0 tabela-capacitacao">
            <thead>
            <tr>
              <th class="coluna-competencia">{{ TEXTOS.diagnostico.COLUNA_COMPETENCIA }}</th>
              <th
                  v-for="servidor in servidoresCabecalho"
                  :key="servidor.servidorTitulo"
                  :title="servidor.servidorNome"
                  class="text-center coluna-servidor"
              >
                <div class="cabecalho-servidor">
                  <span class="cabecalho-servidor-nome">{{ abreviarNomeServidor(servidor.servidorNome) }}</span>
                  <small
                      v-if="servidor.exibirTituloSecundario"
                      class="cabecalho-servidor-titulo"
                  >
                    {{ servidor.servidorTitulo }}
                  </small>
                </div>
              </th>
            </tr>
            </thead>
            <tbody>
            <tr
                v-for="linha in matrizCapacitacao"
                :key="linha.competenciaCodigo"
            >
              <th class="coluna-competencia celula-competencia" scope="row">
                {{ linha.competenciaDescricao }}
              </th>
              <td
                  v-for="celula in linha.celulas"
                  :key="`${linha.competenciaCodigo}-${celula.servidorTitulo}`"
                  class="text-center coluna-servidor"
              >
                <BFormSelect
                    :data-testid="`ocupacao-${celula.servidorTitulo}-${linha.competenciaCodigo}`"
                    :model-value="celula.situacaoCapacitacao"
                    :options="opcoesCapacitacao"
                    class="form-select-sm seletor-capacitacao"
                    @update:model-value="(v: unknown) => atualizarCapacitacao(celula.servidorTitulo, linha.competenciaCodigo, v as SituacaoCapacitacao)"
                />
              </td>
            </tr>
            </tbody>
          </table>
        </div>
      </BCard>
    </template>
  </LayoutPadrao>
</template>

<script lang="ts" setup>
import {computed, ref} from 'vue';
import {useRouter} from 'vue-router';
import {
  BButton,
  BCard,
  BFormSelect,
  BSpinner,
} from 'bootstrap-vue-next';
import LayoutPadrao from '@/components/layout/LayoutPadrao.vue';
import CarregamentoPagina from '@/components/comum/CarregamentoPagina.vue';
import AppAlert from '@/components/comum/AppAlert.vue';
import EmptyState from '@/components/comum/EmptyState.vue';
import {useDiagnosticoContexto} from '@/composables/useDiagnosticoContexto';
import {useOcupacoesCriticasDiagnostico} from '@/composables/useOcupacoesCriticasDiagnostico';
import {TEXTOS} from '@/constants/textos';
import type {SituacaoCapacitacao} from '@/types/diagnostico-competencias';

const props = defineProps<{
  codSubprocesso: number;
  siglaUnidade: string;
}>();

const router = useRouter();
const {data: contexto} = useDiagnosticoContexto(props.codSubprocesso);

const {
  ocupacoesLocais,
  unidade,
  servidores,
  carregando,
  salvandoAutomaticamente,
  autoguardado,
  atualizarCapacitacao,
} = useOcupacoesCriticasDiagnostico(props.codSubprocesso);

// « Alertas »
const erroMensagem = ref('');

const servidoresCabecalho = computed(() => {
  const nomesAbreviados = new Map<string, number>();
  const lista = servidores.value.map((servidor) => {
    const nomeAbreviado = abreviarNomeServidor(servidor.servidorNome);
    nomesAbreviados.set(nomeAbreviado, (nomesAbreviados.get(nomeAbreviado) ?? 0) + 1);
    return {
      ...servidor,
      nomeAbreviado,
      exibirTituloSecundario: false,
    };
  });

  return lista.map((servidor) => ({
    ...servidor,
    exibirTituloSecundario: (nomesAbreviados.get(servidor.nomeAbreviado) ?? 0) > 1,
  }));
});

const matrizCapacitacao = computed(() => {
  const ocupacoesPorChave = new Map(
    ocupacoesLocais.value.map((ocupacao) => [`${ocupacao.competenciaCodigo}-${ocupacao.servidorTitulo}`, ocupacao]),
  );

  return (contexto.value?.competencias ?? []).map((competencia) => ({
    competenciaCodigo: competencia.competenciaCodigo,
    competenciaDescricao: competencia.descricao,
    celulas: servidoresCabecalho.value.map((servidor) => ({
      servidorTitulo: servidor.servidorTitulo,
      servidorNome: servidor.servidorNome,
      situacaoCapacitacao: ocupacoesPorChave.get(`${competencia.competenciaCodigo}-${servidor.servidorTitulo}`)?.situacaoCapacitacao ?? null,
    })),
  }));
});

// « Opções de capacitação »
const opcoesCapacitacao = [
  {value: null, text: '-'},
  {value: 'NA', text: TEXTOS.diagnostico.CAPACITACAO_NA},
  {value: 'AC', text: TEXTOS.diagnostico.CAPACITACAO_AC},
  {value: 'EC', text: TEXTOS.diagnostico.CAPACITACAO_EC},
  {value: 'C', text: TEXTOS.diagnostico.CAPACITACAO_C},
  {value: 'I', text: TEXTOS.diagnostico.CAPACITACAO_I},
];

function abreviarNomeServidor(nome: string): string {
  const partes = nome.trim().split(/\s+/).filter(Boolean);
  if (partes.length <= 1) {
    return nome;
  }
  const primeiroNome = partes[0];
  const segundoNome = partes[1];
  if (primeiroNome.length >= 10) {
    return primeiroNome;
  }
  return `${primeiroNome} ${segundoNome[0]}.`;
}
</script>

<style scoped>
.tabela-capacitacao {
  width: 100%;
  min-width: 52rem;
}

.coluna-competencia {
  min-width: 16rem;
  width: 16rem;
  position: sticky;
  left: 0;
  background: var(--bs-body-bg, #fff);
  z-index: 1;
}

.celula-competencia {
  white-space: normal;
}

.coluna-servidor {
  min-width: 8rem;
  width: 8rem;
}

.cabecalho-servidor {
  display: flex;
  flex-direction: column;
  align-items: center;
  gap: 0.1rem;
}

.cabecalho-servidor-nome {
  font-weight: 600;
}

.cabecalho-servidor-titulo {
  color: var(--bs-secondary-color, #6c757d);
  font-weight: 400;
}

.seletor-capacitacao {
  min-width: 0;
  width: 100%;
}
</style>
