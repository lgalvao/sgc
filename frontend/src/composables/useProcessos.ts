import {ref} from "vue";
import type {
    AtualizarProcessoRequest,
    CriarProcessoRequest,
    Processo,
    ProcessoResumo,
    SubprocessoElegivel
} from "@/types/tipos";
import * as processoService from "@/services/processoService";
import type {Page} from "@/services/painelService";
import * as painelService from "@/services/painelService";
import {useErrorHandler} from "@/composables/useErrorHandler";

const processosPainel = ref<ProcessoResumo[]>([]);
const processosPainelPage = ref<Page<ProcessoResumo>>(criarPaginaVazia<ProcessoResumo>());
const processoDetalhe = ref<Processo | null>(null);
const subprocessosElegiveis = ref<SubprocessoElegivel[]>([]);
const {lastError, clearError, withErrorHandling} = useErrorHandler();

function setProcessoDetalhe(processo: Processo | null) {
    processoDetalhe.value = processo;
}

async function recarregarProcessoDetalheAtual() {
    if (processoDetalhe.value) {
        await buscarProcessoDetalhe(processoDetalhe.value.codigo);
    }
}

async function executarComTratamentoECarregamento<T>(
    acao: () => Promise<T>,
    onError?: () => void,
) {
    return withErrorHandling(acao, onError);
}

async function buscarProcessosPainel(
    unidade: number,
    page: number,
    size: number,
    sort?: keyof ProcessoResumo,
    order?: "asc" | "desc",
) {
    return executarComTratamentoECarregamento(async () => {
        const response = await painelService.listarProcessos(
            unidade,
            page,
            size,
            sort,
            order,
        );
        processosPainel.value = response?.content ?? [];
        processosPainelPage.value = response ?? criarPaginaVazia<ProcessoResumo>();
    });
}

async function criarProcesso(request: CriarProcessoRequest) {
    return executarComTratamentoECarregamento(async () => {
        return await processoService.criarProcesso(request);
    });
}

async function atualizarProcesso(codigo: number, request: AtualizarProcessoRequest) {
    return executarComTratamentoECarregamento(async () => {
        const p = await processoService.atualizarProcesso(codigo, request);
        await recarregarProcessoDetalheAtual();
        return p;
    });
}

async function removerProcesso(codigo: number) {
    return executarComTratamentoECarregamento(async () => {
        await processoService.excluirProcesso(codigo);
    });
}

async function iniciarProcesso(codigo: number, tipo: string, unidades: number[]) {
    return executarComTratamentoECarregamento(async () => {
        await processoService.iniciarProcesso(codigo, tipo, unidades);
    });
}

async function buscarProcessoDetalhe(codigoProcesso: number) {
    return executarComTratamentoECarregamento(async () => {
        setProcessoDetalhe(null);
        const processo = await processoService.obterDetalhesProcesso(codigoProcesso);
        processoDetalhe.value = processo;
        return processo;
    }, () => {
        setProcessoDetalhe(null);
    });
}

async function finalizarProcesso(codigoProcesso: number) {
    return executarComTratamentoECarregamento(async () => {
        await processoService.finalizarProcesso(codigoProcesso);
        await recarregarProcessoDetalheAtual();
    });
}

async function executarAcaoBloco(acao: "aceitar" | "homologar" | "disponibilizar", unidadeCodigos: number[], dataLimite?: string) {
    if (!processoDetalhe.value) throw new Error("Detalhes do processo não carregados.");
    return executarComTratamentoECarregamento(async () => {
        await processoService.executarAcaoEmBloco(processoDetalhe.value!.codigo, {
            unidadeCodigos,
            acao,
            dataLimite
        });
        await recarregarProcessoDetalheAtual();
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
        subprocessosElegiveis,
        lastError,
        clearError,
        buscarProcessosPainel,
        buscarProcessoDetalhe,
        criarProcesso,
        atualizarProcesso,
        removerProcesso,
        iniciarProcesso,
        finalizarProcesso,
        executarAcaoBloco,
        buscarContextoCompleto,
    };
}
