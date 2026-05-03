import type {CriarProcessoRequest, AtualizarProcessoRequest, Processo} from "./types";
import apiClient from "@/axios-setup";

const CAMINHO_PROCESSOS = "/processos";

function caminhoProcesso(codProcesso?: number, sufixo = ""): string {
    return codProcesso === undefined ? `${CAMINHO_PROCESSOS}${sufixo}` : `${CAMINHO_PROCESSOS}/${codProcesso}${sufixo}`;
}

export async function criarProcesso(
    request: CriarProcessoRequest,
): Promise<Processo> {
    const response = await apiClient.post<Processo>(CAMINHO_PROCESSOS, request);
    return response.data;
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

export async function executarAcaoEmBloco(
    codProcesso: number,
    payload: { unidadeCodigos: number[]; acao: "ACEITAR" | "HOMOLOGAR" | "DISPONIBILIZAR"; dataLimite?: string },
): Promise<void> {
    await apiClient.post(caminhoProcesso(codProcesso, "/acao-em-bloco"), payload);
}

export async function enviarLembrete(
    codProcesso: number,
    unidadeCodigo: number,
): Promise<void> {
    await apiClient.post(caminhoProcesso(codProcesso, "/enviar-lembrete"), {unidadeCodigo});
}
