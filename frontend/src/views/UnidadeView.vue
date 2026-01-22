<template>
  <BContainer class="mt-4">
    <BAlert
        v-if="unidadesStore.lastError"
        :model-value="true"
        variant="danger"
        dismissible
        @dismissed="unidadesStore.clearError()"
    >
      {{ unidadesStore.lastError.message }}
    </BAlert>

    <div v-if="unidadeComResponsavelDinamico">
      <PageHeader :title="`${unidadeComResponsavelDinamico.sigla} - ${unidadeComResponsavelDinamico.nome}`">
        <template #actions>
          <BButton
              v-if="mapaVigente"
              data-testid="btn-mapa-vigente"
              variant="outline-success"
              @click="visualizarMapa"
          >
            <i
                class="bi bi-file-earmark-spreadsheet me-2"
            />Mapa vigente
          </BButton>
          <BButton
              v-if="perfilStore.perfilSelecionado === 'ADMIN'"
              class="ms-2"
              data-testid="unidade-view__btn-criar-atribuicao"
              variant="outline-primary"
              @click="irParaCriarAtribuicao"
          >
            Criar atribuição
          </BButton>
        </template>
      </PageHeader>

      <BCard class="mb-4">
        <BCardBody>
          <p><strong>Titular:</strong> {{ titularDetalhes?.nome }}</p>
          <p class="ms-3">
            <i class="bi bi-telephone-fill me-2"/>{{ titularDetalhes?.ramal }}
            <i class="bi bi-envelope-fill ms-3 me-2"/>{{ titularDetalhes?.email }}
          </p>
          <template
              v-if="unidadeComResponsavelDinamico.responsavel &&
              unidadeComResponsavelDinamico.responsavel.codigo &&
              unidadeComResponsavelDinamico.responsavel.codigo !== unidadeComResponsavelDinamico.usuarioCodigo"
          >
            <p><strong>Responsável:</strong> {{ unidadeComResponsavelDinamico.responsavel.nome }}</p>
            <p class="ms-3">
              <i class="bi bi-telephone-fill me-2"/>{{ unidadeComResponsavelDinamico.responsavel.ramal }}
              <i class="bi bi-envelope-fill ms-3 me-2"/>{{ unidadeComResponsavelDinamico.responsavel.email }}
            </p>
          </template>
        </BCardBody>
      </BCard>
    </div>
    <div v-else>
      <p>Unidade não encontrada.</p>
    </div>

    <div
        v-if="unidadeComResponsavelDinamico && unidadeComResponsavelDinamico.filhas && unidadeComResponsavelDinamico.filhas.length > 0"
        class="mt-5"
    >
      <TreeTable
          :columns="colunasTabela"
          :data="dadosFormatadosSubordinadas"
          :hide-headers="true"
          title="Unidades Subordinadas"
          @row-click="navegarParaUnidadeSubordinada"
      />
    </div>
  </BContainer>
</template>

<script lang="ts" setup>
import {BAlert, BButton, BCard, BCardBody, BContainer} from "bootstrap-vue-next";
import {computed, onMounted, ref} from "vue";
import {useRouter} from "vue-router";
import {logger} from "@/utils";
import {useAtribuicaoTemporariaStore} from "@/stores/atribuicoes";
import {useMapasStore} from "@/stores/mapas";
import {usePerfilStore} from "@/stores/perfil";
import {useUnidadesStore} from "@/stores/unidades";
import {buscarUsuarioPorTitulo} from "@/services/usuarioService";
import type {MapaCompleto, Responsavel, Unidade, Usuario,} from "@/types/tipos";
import TreeTable from "../components/TreeTableView.vue";
import PageHeader from "@/components/layout/PageHeader.vue";

const props = defineProps<{ codUnidade: number }>();

const router = useRouter();
const codigo = computed(() => props.codUnidade);
const unidadesStore = useUnidadesStore();
const perfilStore = usePerfilStore();
const mapasStore = useMapasStore();
const atribuicaoTemporariaStore = useAtribuicaoTemporariaStore();

const titular = ref<Usuario | null>(null);

onMounted(async () => {
  await Promise.all([
    unidadesStore.buscarArvoreUnidade(codigo.value),
    atribuicaoTemporariaStore.buscarAtribuicoes(),
  ]);

  if (unidadesStore.unidade?.tituloTitular) {
    try {
      titular.value = await buscarUsuarioPorTitulo(unidadesStore.unidade.tituloTitular);
    } catch (e) {
      logger.error("Erro ao buscar titular:", e);
    }
  }
});

const unidadeOriginal = computed<Unidade | null>(
    () => unidadesStore.unidade,
);

const unidadeComResponsavelDinamico = computed<Unidade | null>(() => {
  const unidade = unidadeOriginal.value;
  if (!unidade) return null;

  const atribuicoes = atribuicaoTemporariaStore.obterAtribuicoesPorUnidade(
      unidade.sigla,
  );
  const hoje = new Date();

  // Encontrar a atribuição temporária vigente
  const atribuicaoVigente = atribuicoes.find((atrb) => {
    const dataInicio = new Date(atrb.dataInicio);
    const dataTermino = new Date(atrb.dataTermino);
    return hoje >= dataInicio && hoje <= dataTermino;
  });

  if (atribuicaoVigente) {
    const responsavel: Responsavel = {
      codigo: atribuicaoVigente.usuario.codigo,
      nome: atribuicaoVigente.usuario.nome,
      tituloEleitoral: atribuicaoVigente.usuario.tituloEleitoral,
      unidade: atribuicaoVigente.usuario.unidade,
      email: atribuicaoVigente.usuario.email,
      ramal: atribuicaoVigente.usuario.ramal,
      usuarioTitulo: atribuicaoVigente.usuario.nome,
      unidadeCodigo: atribuicaoVigente.unidade.codigo,
      usuarioCodigo: atribuicaoVigente.usuario.codigo,
      tipo: "TEMPORARIO",
      dataInicio: atribuicaoVigente.dataInicio,
      dataFim: atribuicaoVigente.dataFim,
    };
    // Retorna uma nova unidade com o responsável da atribuição temporária
    return {
      ...unidade,
      responsavel,
    };
  }

  // Se não houver atribuição temporária vigente, retorna a unidade original
  return unidade;
});
const titularDetalhes = computed<Usuario | null>(() => {
  return titular.value;
});

const mapaVigente = computed<MapaCompleto | null>(() => {
  return mapasStore.mapaCompleto;
});

function irParaCriarAtribuicao() {
  router.push({path: `/unidade/${codigo.value}/atribuicao`});
}

const dadosFormatadosSubordinadas = computed(() => {
  if (
      !unidadeComResponsavelDinamico.value ||
      !unidadeComResponsavelDinamico.value.filhas ||
      unidadeComResponsavelDinamico.value.filhas.length === 0
  )
    return [];
  return formatarDadosParaArvore(unidadeComResponsavelDinamico.value.filhas);
});

const colunasTabela = [{key: "nome", label: "Unidade"}];

interface UnidadeFormatada {
  codigo: number;
  nome: string;
  expanded: boolean;
  children?: UnidadeFormatada[];
}

function formatarDadosParaArvore(dados: Unidade[]): UnidadeFormatada[] {
  if (!dados) return [];

  return dados.map((item) => {
    const children = item.filhas ? formatarDadosParaArvore(item.filhas) : [];
    return {
      codigo: item.codigo,
      nome: item.sigla + " - " + item.nome,
      expanded: true,
      ...(children.length > 0 && {children}),
    };
  });
}

function navegarParaUnidadeSubordinada(item: { codigo: any }) {
  if (item && typeof item.codigo === "number")
    router.push({path: `/unidade/${item.codigo}`});
}

function visualizarMapa() {
  if (mapaVigente.value && unidadeOriginal.value) {
    router.push({
      name: "SubprocessoVisMapa",
      params: {
        codProcesso: mapaVigente.value.subprocessoCodigo,
        siglaUnidade: unidadeOriginal.value.sigla,
      },
    });
  }
}
</script>
