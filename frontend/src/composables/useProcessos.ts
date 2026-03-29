import {ref} from "vue";
import type {
    AtualizarProcessoRequest,
    CriarProcessoRequest,
    Processo,
    ProcessoResumo,
    SubprocessoElegivel,
    UnidadeImportacao
} from "@/types/tipos";
import * as processoService from "@/services/processoService";
import type {Page} from "@/services/painelService";
import * as painelService from "@/services/painelService";
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

async function executarComCarregamento<T>(acao: () => Promise<T>) {
    carregando.value = true;
    try {
        return await acao();
    } finally {
        carregando.value = false;
    }
}

async function executarComTratamentoECarregamento<T>(
    acao: () => Promise<T>,
    onError?: () => void,
) {
    return withErrorHandling(() => executarComCarregamento(acao), onError);
}

async function executarAcaoComRecarga(acao: () => Promise<void>) {
    return withErrorHandling(async () => {
        await acao();
        await recarregarProcessoDetalheAtual();
    });
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

async function buscarProcessosFinalizados() {
    return executarComTratamentoECarregamento(async () => {
        processosFinalizados.value = await processoService.buscarProcessosFinalizados() ?? [];
    });
}

async function buscarProcessosParaImportacao() {
    return executarComTratamentoECarregamento(async () => {
        processosParaImportacao.value = await processoService.buscarProcessosParaImportacao() ?? [];
    });
}

async function buscarUnidadesParaImportacao(codigoProcesso: number): Promise<UnidadeImportacao[]> {
    return withErrorHandling(async () => processoService.buscarUnidadesParaImportacao(codigoProcesso));
}

async function buscarProcessoDetalhe(codigoProcesso: number) {
    return executarComTratamentoECarregamento(async () => {
        setProcessoDetalhe(null);
        processoDetalhe.value = await processoService.obterDetalhesProcesso(codigoProcesso);
    }, () => {
        setProcessoDetalhe(null);
    });
}

async function buscarSubprocessosElegiveis(codigoProcesso: number) {
    return withErrorHandling(async () => {
        subprocessosElegiveis.value = await processoService.buscarSubprocessosElegiveis(codigoProcesso) ?? [];
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

async function alterarDataLimiteSubprocesso(codSubprocesso: number, dados: { novaData: string }) {
    return executarAcaoComRecarga(async () => {
        await processoService.alterarDataLimiteSubprocesso(codSubprocesso, dados);
    });
}

async function apresentarSugestoes(codSubprocesso: number, dados: { sugestoes: string }) {
    return executarAcaoComRecarga(async () => {
        await processoService.apresentarSugestoes(codSubprocesso, dados);
    });
}

async function validarMapa(codSubprocesso: number) {
    return executarAcaoComRecarga(async () => {
        await processoService.validarMapa(codSubprocesso);
    });
}

async function homologarValidacao(codSubprocesso: number, dados: { texto: string }) {
    return executarAcaoComRecarga(async () => {
        await processoService.homologarValidacao(codSubprocesso, dados);
    });
}

async function aceitarValidacao(codSubprocesso: number, dados: { texto: string }) {
    return executarAcaoComRecarga(async () => {
        await processoService.aceitarValidacao(codSubprocesso, dados);
    });
}

async function devolverValidacao(codSubprocesso: number, dados: { justificativa: string }) {
    return executarAcaoComRecarga(async () => {
        await processoService.devolverValidacao(codSubprocesso, dados);
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
    return executarAcaoComRecarga(async () => {
        await processoService.processarAcaoEmBloco(payload);
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
