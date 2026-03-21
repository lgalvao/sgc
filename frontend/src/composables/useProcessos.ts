import {computed, ref} from "vue";
import type {
    AtualizarProcessoRequest,
    CriarProcessoRequest,
    Processo,
    ProcessoResumo,
    SubprocessoElegivel,
    UnidadeImportacao
} from "@/types/tipos";
import * as processoService from "@/services/processoService";
import * as painelService from "@/services/painelService";
import type {Page} from "@/services/painelService";
import {useErrorHandler} from "@/composables/useErrorHandler";

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
    unidade: number,
    page: number,
    size: number,
    sort?: keyof ProcessoResumo,
    order?: "asc" | "desc",
) {
    return withErrorHandling(async () => {
        carregando.value = true;
        const response = await painelService.listarProcessos(
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

async function criarProcesso(request: CriarProcessoRequest) {
    return withErrorHandling(async () => {
        carregando.value = true;
        return await processoService.criarProcesso(request);
    }).finally(() => {
        carregando.value = false;
    });
}

async function atualizarProcesso(codigo: number, request: AtualizarProcessoRequest) {
    return withErrorHandling(async () => {
        carregando.value = true;
        const p = await processoService.atualizarProcesso(codigo, request);
        await recarregarProcessoDetalheAtual();
        return p;
    }).finally(() => {
        carregando.value = false;
    });
}

async function removerProcesso(codigo: number) {
    return withErrorHandling(async () => {
        carregando.value = true;
        await processoService.excluirProcesso(codigo);
    }).finally(() => {
        carregando.value = false;
    });
}

async function iniciarProcesso(codigo: number, tipo: string, unidades: number[]) {
    return withErrorHandling(async () => {
        carregando.value = true;
        await processoService.iniciarProcesso(codigo, tipo, unidades);
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

async function buscarSubprocessosElegiveis(codigoProcesso: number) {
    return withErrorHandling(async () => {
        subprocessosElegiveis.value = await processoService.buscarSubprocessosElegiveis(codigoProcesso) ?? [];
    });
}

async function finalizarProcesso(codigoProcesso: number) {
    return withErrorHandling(async () => {
        carregando.value = true;
        await processoService.finalizarProcesso(codigoProcesso);
        await recarregarProcessoDetalheAtual();
    }).finally(() => {
        carregando.value = false;
    });
}

async function executarAcaoBloco(acao: "aceitar" | "homologar" | "disponibilizar", unidadeCodigos: number[], dataLimite?: string) {
    if (!processoDetalhe.value) throw new Error("Detalhes do processo não carregados.");
    return withErrorHandling(async () => {
        carregando.value = true;
        await processoService.executarAcaoEmBloco(processoDetalhe.value!.codigo, {
            unidadeCodigos,
            acao,
            dataLimite
        });
        await recarregarProcessoDetalheAtual();
    }).finally(() => {
        carregando.value = false;
    });
}

async function alterarDataLimiteSubprocesso(codSubprocesso: number, dados: { novaData: string }) {
    return withErrorHandling(async () => {
        await processoService.alterarDataLimiteSubprocesso(codSubprocesso, dados);
        await recarregarProcessoDetalheAtual();
    });
}

async function apresentarSugestoes(codSubprocesso: number, dados: { sugestoes: string }) {
    return withErrorHandling(async () => {
        await processoService.apresentarSugestoes(codSubprocesso, dados);
        await recarregarProcessoDetalheAtual();
    });
}

async function validarMapa(codSubprocesso: number) {
    return withErrorHandling(async () => {
        await processoService.validarMapa(codSubprocesso);
        await recarregarProcessoDetalheAtual();
    });
}

async function homologarValidacao(codSubprocesso: number, dados: { texto: string }) {
    return withErrorHandling(async () => {
        await processoService.homologarValidacao(codSubprocesso, dados);
        await recarregarProcessoDetalheAtual();
    });
}

async function aceitarValidacao(codSubprocesso: number, dados: { texto: string }) {
    return withErrorHandling(async () => {
        await processoService.aceitarValidacao(codSubprocesso, dados);
        await recarregarProcessoDetalheAtual();
    });
}

async function devolverValidacao(codSubprocesso: number, dados: { justificativa: string }) {
    return withErrorHandling(async () => {
        await processoService.devolverValidacao(codSubprocesso, dados);
        await recarregarProcessoDetalheAtual();
    });
}

async function enviarLembrete(codProcesso: number, unidadeCodigo: number) {
    return withErrorHandling(async () => {
        await processoService.enviarLembrete(codProcesso, unidadeCodigo);
    });
}

async function buscarContextoCompleto(codProcesso: number) {
    return withErrorHandling(async () => {
        setProcessoDetalhe(null);
        const data = await processoService.buscarContextoCompleto(codProcesso);
        if (data) {
            processoDetalhe.value = data;
            subprocessosElegiveis.value = data.elegiveis ?? [];
        }
        return data;
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
        await recarregarProcessoDetalheAtual();
    });
}

function criarPaginaVazia<T>(): Page<T> {
    return {
        content: [],
        totalPages: 0,
        totalElements: 0,
        size: 20,
        number: 0,
        first: true,
        last: true,
        empty: true,
    };
}

export function useProcessos() {
    return {
        processosPainel,
        processosPainelPage,
        processoDetalhe,
        processosFinalizados,
        processosParaImportacao,
        subprocessosElegiveis,
        carregando,
        lastError,
        clearError,
        buscarProcessosPainel,
        buscarProcessosFinalizados,
        buscarProcessosParaImportacao,
        buscarUnidadesParaImportacao,
        buscarProcessoDetalhe,
        recarregarProcessoDetalheAtual,
        criarProcesso,
        atualizarProcesso,
        removerProcesso,
        iniciarProcesso,
        finalizarProcesso,
        executarAcaoBloco,
        buscarSubprocessosElegiveis,
        alterarDataLimiteSubprocesso,
        apresentarSugestoes,
        validarMapa,
        homologarValidacao,
        aceitarValidacao,
        devolverValidacao,
        enviarLembrete,
        buscarContextoCompleto,
        processarCadastroBloco
    };
}
