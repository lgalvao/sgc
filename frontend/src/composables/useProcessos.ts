import {ref} from "vue";
import type {Page} from "@/services/painelService";
import * as painelService from "@/services/painelService";
import {useErrorHandler} from "@/composables/useErrorHandler";
import * as processoService from "@/services/processoService";
import type {
    AtualizarProcessoRequest,
    CriarProcessoRequest,
    Processo,
    ProcessoResumo,
    SubprocessoElegivel,
    TipoProcesso,
    UnidadeImportacao,
} from "@/types/tipos";
import {logger} from "@/utils";

type ContextoCompletoProcesso = Processo & {
    elegiveis?: SubprocessoElegivel[];
};

function criarPaginaVazia<T>(): Page<T> {
    return {
        content: [],
        totalPages: 0,
        totalElements: 0,
        number: 0,
        size: 0,
        first: true,
        last: true,
        empty: true,
    };
}

const processosPainel = ref<ProcessoResumo[]>([]);
const processosPainelPage = ref<Page<ProcessoResumo>>(criarPaginaVazia<ProcessoResumo>());
const processoDetalhe = ref<Processo | null>(null);
const processosFinalizados = ref<ProcessoResumo[]>([]);
const processosParaImportacao = ref<ProcessoResumo[]>([]);
const subprocessosElegiveis = ref<SubprocessoElegivel[]>([]);
const carregando = ref(false);
const {lastError, clearError, withErrorHandling} = useErrorHandler();

function setProcessoDetalhe(processo: Processo | null) {
    processoDetalhe.value = processo;
}

async function recarregarProcessoDetalheAtual() {
    if (processoDetalhe.value) {
        await buscarProcessoDetalhe(processoDetalhe.value.codigo);
    }
}

async function buscarProcessosPainel(
    perfil: string,
    unidade: number,
    page: number,
    size: number,
    sort?: keyof ProcessoResumo,
    order?: "asc" | "desc",
) {
    return withErrorHandling(async () => {
        carregando.value = true;
        const response = await painelService.listarProcessos(
            perfil,
            unidade,
            page,
            size,
            sort,
            order,
        );
        processosPainel.value = response?.content ?? [];
        processosPainelPage.value = response ?? criarPaginaVazia<ProcessoResumo>();
    }).finally(() => {
        carregando.value = false;
    });
}

async function buscarProcessosFinalizados() {
    return withErrorHandling(async () => {
        carregando.value = true;
        processosFinalizados.value = await processoService.buscarProcessosFinalizados() ?? [];
    }).finally(() => {
        carregando.value = false;
    });
}

async function buscarProcessosParaImportacao() {
    return withErrorHandling(async () => {
        carregando.value = true;
        processosParaImportacao.value = await processoService.buscarProcessosParaImportacao() ?? [];
    }).finally(() => {
        carregando.value = false;
    });
}

async function buscarUnidadesParaImportacao(codigoProcesso: number): Promise<UnidadeImportacao[]> {
    return withErrorHandling(async () => processoService.buscarUnidadesParaImportacao(codigoProcesso));
}

async function buscarProcessoDetalhe(codigoProcesso: number) {
    return withErrorHandling(async () => {
        carregando.value = true;
        setProcessoDetalhe(null);
        processoDetalhe.value = await processoService.obterDetalhesProcesso(codigoProcesso);
    }, () => {
        setProcessoDetalhe(null);
    }).finally(() => {
        carregando.value = false;
    });
}

async function criarProcesso(payload: CriarProcessoRequest) {
    return withErrorHandling(async () => processoService.criarProcesso(payload));
}

async function atualizarProcesso(codigoProcesso: number, payload: AtualizarProcessoRequest) {
    return withErrorHandling(async () => {
        await processoService.atualizarProcesso(codigoProcesso, payload);
    });
}

async function removerProcesso(codigoProcesso: number) {
    return withErrorHandling(async () => {
        await processoService.excluirProcesso(codigoProcesso);
    });
}

async function iniciarProcesso(codigoProcesso: number, tipo: TipoProcesso, unidadesIds: number[]) {
    return withErrorHandling(async () => {
        await processoService.iniciarProcesso(codigoProcesso, tipo, unidadesIds);
    });
}

async function finalizarProcesso(codigoProcesso: number) {
    return withErrorHandling(async () => {
        await processoService.finalizarProcesso(codigoProcesso);
        await buscarProcessoDetalhe(codigoProcesso);
    });
}

async function processarCadastroBloco(payload: {
    codProcesso: number;
    unidades: string[];
    tipoAcao: "aceitar" | "homologar";
    unidadeUsuario: string;
}) {
    return withErrorHandling(async () => {
        await processoService.processarAcaoEmBloco(payload);
        await buscarProcessoDetalhe(payload.codProcesso);
    });
}

async function alterarDataLimiteSubprocesso(codigo: number, dados: { novaData: string }) {
    return withErrorHandling(async () => {
        await processoService.alterarDataLimiteSubprocesso(codigo, dados);
        await recarregarProcessoDetalheAtual();
    });
}

async function apresentarSugestoes(codigo: number, dados: { sugestoes: string }) {
    return withErrorHandling(async () => {
        await processoService.apresentarSugestoes(codigo, dados);
        await recarregarProcessoDetalheAtual();
    });
}

async function validarMapa(codigo: number) {
    return withErrorHandling(async () => {
        await processoService.validarMapa(codigo);
        await recarregarProcessoDetalheAtual();
    });
}

async function homologarValidacao(codigo: number, dados: { texto: string }) {
    return withErrorHandling(async () => {
        await processoService.homologarValidacao(codigo, dados);
        await recarregarProcessoDetalheAtual();
    });
}

async function aceitarValidacao(codigo: number, dados: { texto: string }) {
    return withErrorHandling(async () => {
        await processoService.aceitarValidacao(codigo, dados);
        await recarregarProcessoDetalheAtual();
    });
}

async function devolverValidacao(codigo: number, dados: { justificativa: string }) {
    return withErrorHandling(async () => {
        await processoService.devolverValidacao(codigo, dados);
        await recarregarProcessoDetalheAtual();
    });
}

async function executarAcaoBloco(
    acao: "aceitar" | "homologar" | "disponibilizar",
    ids: number[],
    dataLimite?: string
) {
    return withErrorHandling(async () => {
        if (!processoDetalhe.value) {
            throw new Error("Detalhes do processo não carregados.");
        }

        await processoService.executarAcaoEmBloco(processoDetalhe.value.codigo, {
            unidadeCodigos: ids,
            acao,
            dataLimite,
        });
        await buscarProcessoDetalhe(processoDetalhe.value.codigo);
    });
}

async function enviarLembrete(codProcesso: number, unidadeCodigo: number) {
    return withErrorHandling(async () => {
        await processoService.enviarLembrete(codProcesso, unidadeCodigo);
    });
}

async function buscarContextoCompleto(codigoProcesso: number) {
    return withErrorHandling(async () => {
        carregando.value = true;
        setProcessoDetalhe(null);

        const data = await processoService.buscarContextoCompleto(codigoProcesso) as ContextoCompletoProcesso | undefined;

        setProcessoDetalhe(data ?? null);
        subprocessosElegiveis.value = data?.elegiveis ?? [];
    }, (normalized) => {
        if (normalized.kind !== "unauthorized") {
            logger.error(`Erro ao buscar contexto completo para processo ${codigoProcesso}:`, normalized);
        }
    }).finally(() => {
        carregando.value = false;
    });
}

async function buscarSubprocessosElegiveis(codigoProcesso: number) {
    return withErrorHandling(async () => {
        subprocessosElegiveis.value = await processoService.buscarSubprocessosElegiveis(codigoProcesso) ?? [];
    });
}

export function useProcessos() {
    return {
        carregando,
        processosPainel,
        processosPainelPage,
        processoDetalhe,
        processosFinalizados,
        processosParaImportacao,
        subprocessosElegiveis,
        lastError,
        clearError,
        buscarProcessosPainel,
        buscarProcessosFinalizados,
        buscarProcessosParaImportacao,
        buscarUnidadesParaImportacao,
        buscarProcessoDetalhe,
        criarProcesso,
        atualizarProcesso,
        removerProcesso,
        iniciarProcesso,
        finalizarProcesso,
        processarCadastroBloco,
        alterarDataLimiteSubprocesso,
        apresentarSugestoes,
        validarMapa,
        homologarValidacao,
        aceitarValidacao,
        devolverValidacao,
        executarAcaoBloco,
        enviarLembrete,
        buscarContextoCompleto,
        buscarSubprocessosElegiveis,
        withErrorHandling,
    };
}
