import {computed, ref, watch} from "vue";
import {useRouter} from "vue-router";
import type {Responsavel, Unidade, Usuario} from "@/types/tipos";
import type {TreeItem} from "@/components/comum/TreeTable.vue";
import {usePerfil} from "@/composables/usePerfil";
import {useUnidadeAtual} from "@/composables/useUnidadeAtual";
import {formatarDataBR, logger} from "@/utils";
import {TEXTOS} from "@/constants/textos";
import {TEXTOS_RELATORIOS} from "@/constants/textos-relatorios";
import {useNotification} from "@/composables/useNotification";
import {usePerfilStore} from "@/stores/perfil";
import {Perfil} from "@/types/comum";
import {relatoriosService} from "@/services/relatoriosService";
import {useDadosTelaUnidadeQuery} from "@/composables/useUnidadeQuery";

interface UnidadeTelaProps {
  codUnidade: number;
}

export function useUnidadeTela(props: UnidadeTelaProps) {
  const router = useRouter();
  const {mostrarCriarAtribuicaoTemporaria} = usePerfil();
  const {notify} = useNotification();
  const {definirUnidadeAtual} = useUnidadeAtual();
  const perfilStore = usePerfilStore();

  const dadosTelaQuery = useDadosTelaUnidadeQuery(() => props.codUnidade);
  const loadingExportacaoPdf = ref(false);
  const loadingExportacaoCsv = ref(false);
  const erroDispensado = ref(false);

  const unidade = computed(() => dadosTelaQuery.data.value?.unidade ?? null);
  const mapaVigente = computed(() => dadosTelaQuery.data.value?.mapaVigente ?? null);
  const carregandoPagina = computed(() => dadosTelaQuery.isPending.value || dadosTelaQuery.isLoading.value);

  const ultimoErro = computed(() => {
    if (erroDispensado.value || !dadosTelaQuery.error.value) {
      return null;
    }
    return dadosTelaQuery.error.value.message;
  });

  watch(unidade, (novaUnidade) => {
    definirUnidadeAtual(novaUnidade);
  }, { immediate: true });

  watch(() => dadosTelaQuery.error.value, (novoErro) => {
    if (novoErro) {
      erroDispensado.value = false;
    }
  });

  function irParaCriarAtribuicao() {
    void router.push({path: `/unidade/${props.codUnidade}/atribuicao`});
  }

  function navegarParaUnidadeSubordinada(row: TreeItem) {
    void router.push({path: `/unidade/${row.codigo}`});
  }

  async function exportarMapaVigentePdf() {
    if (!mapaVigente.value) {
      return;
    }

    loadingExportacaoPdf.value = true;
    try {
      await relatoriosService.downloadRelatorioMapaVigenteUnidadePdf(props.codUnidade);
    } catch (error) {
      logger.error("Falha ao exportar PDF do mapa vigente:", error);
      notify(TEXTOS_RELATORIOS.ERRO_EXPORTAR, "danger");
    } finally {
      loadingExportacaoPdf.value = false;
    }
  }

  async function exportarMapaVigenteCsv() {
    if (!mapaVigente.value) {
      return;
    }

    loadingExportacaoCsv.value = true;
    try {
      await relatoriosService.downloadRelatorioMapaVigenteUnidadeCsv(props.codUnidade);
    } catch (error) {
      logger.error("Falha ao exportar CSV do mapa vigente:", error);
      notify(TEXTOS_RELATORIOS.ERRO_EXPORTAR_CSV, "danger");
    } finally {
      loadingExportacaoCsv.value = false;
    }
  }

  const colunasTabela = [{key: "nome", label: TEXTOS.unidade.CAMPO_UNIDADE}];
  const subordinadas = computed(() => unidade.value?.filhas ?? []);
  const temSubordinadas = computed(() => subordinadas.value.length > 0);
  const podeExportarMapaVigente = computed(() => perfilStore.perfilSelecionado === Perfil.CHEFE && Boolean(mapaVigente.value));

  const dadosFormatadosSubordinadas = computed(() => formatarDadosParaArvore(subordinadas.value));

  const responsavelExibivel = computed<Usuario | Responsavel | null>(() => {
    return unidade.value?.responsavel ?? unidade.value?.titular ?? null;
  });

  const responsavelEhTitular = computed(() => {
    const titular = unidade.value?.titular;
    const responsavel = responsavelExibivel.value;
    if (!titular || !responsavel) {
      return false;
    }
    return titular.tituloEleitoral === responsavel.tituloEleitoral;
  });

  const titularExibivel = computed(() => {
    const titular = unidade.value?.titular;
    if (!titular || !responsavelExibivel.value) {
      return Boolean(titular);
    }
    return !responsavelEhTitular.value;
  });

  const textoBotaoAtribuicao = computed(() =>
      unidade.value?.tipoResponsabilidade === "ATRIBUICAO_TEMPORARIA"
          ? TEXTOS.unidade.BOTAO_EDITAR_ATRIBUICAO
          : TEXTOS.unidade.BOTAO_CRIAR_ATRIBUICAO
  );

  const descricaoResponsabilidade = computed(() => {
    const tipoResponsabilidade = unidade.value?.tipoResponsabilidade ?? "TITULAR";
    const dataFim = unidade.value?.dataFimResponsabilidade;

    if (tipoResponsabilidade === "SUBSTITUTO") {
      return dataFim
          ? `Substituição (até ${formatarDataBR(dataFim)})`
          : "Substituição";
    }

    if (tipoResponsabilidade === "ATRIBUICAO_TEMPORARIA") {
      return dataFim
          ? `Atrib. temporária (até ${formatarDataBR(dataFim)})`
          : "Atrib. temporária";
    }

    return "Titular";
  });

  const labelContatoPrincipal = computed(() =>
      responsavelEhTitular.value
          ? TEXTOS.unidade.LABEL_TITULAR
          : TEXTOS.unidade.LABEL_RESPONSAVEL
  );

  const descricaoContatoPrincipal = computed(() =>
      responsavelEhTitular.value
          ? ""
          : descricaoResponsabilidade.value
  );

  interface UnidadeFormatada {
    codigo: number;
    nome: string;
    expanded: boolean;
    children?: UnidadeFormatada[];
    [key: string]: unknown;
  }

  function formatarDadosParaArvore(dados: Unidade[]): UnidadeFormatada[] {
    return dados.map((item) => {
      const children = formatarDadosParaArvore(item.filhas || []);
      return {
        codigo: item.codigo,
        nome: item.nome,
        sigla: item.sigla,
        expanded: false,
        ...(children.length > 0 && {children})
      };
    });
  }

  return {
    unidade,
    mapaVigente,
    carregandoPagina,
    ultimoErro,
    erroDispensado,
    loadingExportacaoPdf,
    loadingExportacaoCsv,
    podeExportarMapaVigente,
    mostrarCriarAtribuicaoTemporaria,
    textoBotaoAtribuicao,
    titularExibivel,
    responsavelExibivel,
    labelContatoPrincipal,
    descricaoContatoPrincipal,
    temSubordinadas,
    colunasTabela,
    dadosFormatadosSubordinadas,
    irParaCriarAtribuicao,
    navegarParaUnidadeSubordinada,
    exportarMapaVigentePdf,
    exportarMapaVigenteCsv
  };
}
