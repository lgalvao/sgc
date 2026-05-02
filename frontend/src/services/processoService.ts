import {
    type AtualizarProcessoRequest,
    type CriarProcessoRequest,
    type Processo,
    type ProcessoResumo,
    SituacaoSubprocesso,
    type Subprocesso,
    type SubprocessoElegivel,
    type UnidadeImportacao,
    type UnidadeParticipante
} from "@/types/tipos";
import type {ProcessoDetalheDto, UnidadeParticipanteDto} from "@/types/dtos";
import apiClient from "../axios-setup";

const CAMINHO_PROCESSOS = "/processos";

interface ProcessoDetalheResponseBackend extends ProcessoDetalheDto {
    unidades: UnidadeParticipanteDto[];
    resumoSubprocessos: ProcessoResumo[];
}

function caminhoProcesso(codProcesso?: number, sufixo = ""): string {
    return codProcesso === undefined ? `${CAMINHO_PROCESSOS}${sufixo}` : `${CAMINHO_PROCESSOS}/${codProcesso}${sufixo}`;
}

function mapearUnidadeParticipante(dto: UnidadeParticipanteDto): UnidadeParticipante {
    return {
        ...dto,
        codSubprocesso: dto.codSubprocesso ?? 0,
        situacaoSubprocesso: (dto.situacaoSubprocesso as SituacaoSubprocesso | undefined) ?? SituacaoSubprocesso.NAO_INICIADO,
        dataLimite: dto.dataLimite ?? "",
        filhos: (dto.filhos ?? []).map(mapearUnidadeParticipante),
    };
}

function mapearProcessoDetalhe(dto: ProcessoDetalheResponseBackend): Processo {
    return {
        ...dto,
        unidades: dto.unidades.map(mapearUnidadeParticipante),
        resumoSubprocessos: dto.resumoSubprocessos,
    };
}

export async function criarProcesso(
    request: CriarProcessoRequest,
): Promise<Processo> {
    const response = await apiClient.post<Processo>(CAMINHO_PROCESSOS, request);
    return response.data;
}

export async function buscarProcessosFinalizados(): Promise<ProcessoResumo[]> {
    const response = await apiClient.get<ProcessoResumo[]>(
        caminhoProcesso(undefined, "/finalizados"),
    );
    return response.data;
}

export async function buscarProcessosParaImportacao(): Promise<ProcessoResumo[]> {
    const response = await apiClient.get<ProcessoResumo[]>(
        caminhoProcesso(undefined, "/para-importacao"),
    );
    return response.data;
}

export async function buscarUnidadesParaImportacao(codProcesso: number): Promise<UnidadeImportacao[]> {
    const response = await apiClient.get<UnidadeParticipanteDto[]>(
        caminhoProcesso(codProcesso, "/unidades-importacao"),
    );
    return response.data.map((dto) => ({
        nome: dto.nome!,
        sigla: dto.sigla!,
        codUnidade: dto.codUnidade,
        codSubprocesso: dto.codSubprocesso ?? 0,
        codUnidadeSuperior: dto.codUnidadeSuperior,
        situacaoSubprocesso: dto.situacaoSubprocesso as UnidadeImportacao["situacaoSubprocesso"],
        dataLimite: dto.dataLimite,
        mapaCodigo: dto.mapaCodigo,
        localizacaoAtualCodigo: dto.localizacaoAtualCodigo,
    }));
}

export async function iniciarProcesso(
    codProcesso: number,
    tipo: string,
    codigosUnidades: number[],
): Promise<void> {
    await apiClient.post(caminhoProcesso(codProcesso, "/iniciar"), {
        tipo,
        unidades: codigosUnidades
    });
}

export async function finalizarProcesso(codProcesso: number): Promise<void> {
    await apiClient.post(caminhoProcesso(codProcesso, "/finalizar"));
}

export async function obterProcessoPorCodigo(codProcesso: number): Promise<Processo> {
    const response = await apiClient.get<Processo>(caminhoProcesso(codProcesso));
    return response.data;
}

export async function atualizarProcesso(
    codProcesso: number,
    request: AtualizarProcessoRequest,
): Promise<Processo> {
    const response = await apiClient.post<Processo>(
        caminhoProcesso(codProcesso, "/atualizar"),
        request,
    );
    return response.data;
}

export async function excluirProcesso(codProcesso: number): Promise<void> {
    await apiClient.post(caminhoProcesso(codProcesso, "/excluir"));
}

export async function excluirProcessoCompleto(codProcesso: number): Promise<void> {
    await apiClient.post(caminhoProcesso(codProcesso, "/excluir-completo"));
}

export async function obterDetalhesProcesso(codProcesso: number): Promise<Processo> {
    const response = await apiClient.get<ProcessoDetalheResponseBackend>(caminhoProcesso(codProcesso, "/detalhes"));
    return mapearProcessoDetalhe(response.data);
}

export async function executarAcaoEmBloco(
    codProcesso: number,
    payload: { unidadeCodigos: number[]; acao: "ACEITAR" | "HOMOLOGAR" | "DISPONIBILIZAR"; dataLimite?: string },
): Promise<void> {
    await apiClient.post(caminhoProcesso(codProcesso, "/acao-em-bloco"), payload);
}

export async function buscarSubprocessosElegiveis(
    codProcesso: number,
): Promise<SubprocessoElegivel[]> {
    const response = await apiClient.get<SubprocessoElegivel[]>(
        caminhoProcesso(codProcesso, "/subprocessos-elegiveis"),
    );
    return response.data;
}

export async function buscarSubprocessos(
    codProcesso: number,
): Promise<Subprocesso[]> {
    const response = await apiClient.get<Subprocesso[]>(caminhoProcesso(codProcesso, "/subprocessos"));
    return response.data;
}

export async function buscarContextoCompleto(codProcesso: number): Promise<Processo> {
    const response = await apiClient.get<ProcessoDetalheResponseBackend>(caminhoProcesso(codProcesso, "/contexto-completo"));
    return mapearProcessoDetalhe(response.data);
}

// CDU-34: Enviar lembrete de prazo
export async function enviarLembrete(
    codProcesso: number,
    unidadeCodigo: number,
): Promise<void> {
    await apiClient.post(caminhoProcesso(codProcesso, "/enviar-lembrete"), {unidadeCodigo});
}
