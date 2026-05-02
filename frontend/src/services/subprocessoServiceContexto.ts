import type {
    Analise,
    Atividade,
    AtividadeOperacaoResponse,
    ContextoCadastroAtividadesSubprocesso,
    ContextoCadastroAtividadesSubprocessoResponse,
    ContextoEdicaoSubprocesso,
    ContextoEdicaoSubprocessoResponse,
    SubprocessoDetalhe,
    SubprocessoDetalheResponse,
    SubprocessoStatus,
    ValidacaoCadastro,
} from "@/types/tipos";
import apiClient from "../axios-setup";
import {
    type BuscarSubprocessoPorProcessoEUnidadeResponse,
    CAMINHO_SUBPROCESSOS,
    caminhoSubprocesso,
    type ImportarAtividadesRequest,
    mapearContextoComDetalhes,
    mapearDetalheSubprocesso,
} from "./subprocessoServiceBase";

const obter = async <T>(caminho: string, params?: object): Promise<T> =>
    (await (params ? apiClient.get<T>(caminho, {params}) : apiClient.get<T>(caminho))).data;

const postar = async (caminho: string, payload: object): Promise<void> => { await apiClient.post(caminho, payload); };

export async function importarAtividades(
    codSubprocessoDestino: number,
    codSubprocessoOrigem: number,
    codigosAtividades?: number[],
): Promise<AtividadeOperacaoResponse> {
    const request: ImportarAtividadesRequest = {
        codSubprocessoOrigem,
        ...(codigosAtividades && codigosAtividades.length > 0 ? {codigosAtividades} : {}),
    };
    return (await apiClient.post<AtividadeOperacaoResponse>(caminhoSubprocesso(codSubprocessoDestino, "/importar-atividades"), request)).data;
}

export async function listarAtividades(codSubprocesso: number): Promise<Atividade[]> { return (await obter<ContextoEdicaoSubprocessoResponse>(caminhoSubprocesso(codSubprocesso, "/contexto-edicao"))).mapa.atividades; }

export async function listarAtividadesParaImportacao(codSubprocesso: number): Promise<Atividade[]> { return obter<Atividade[]>(caminhoSubprocesso(codSubprocesso, "/atividades-importacao")); }

export async function validarCadastro(codSubprocesso: number): Promise<ValidacaoCadastro> { return obter<ValidacaoCadastro>(caminhoSubprocesso(codSubprocesso, "/validar-cadastro")); }

export async function alterarDataLimiteSubprocesso(
    codSubprocesso: number,
    dados: { novaData: string },
): Promise<void> {
    await postar(caminhoSubprocesso(codSubprocesso, "/data-limite"), {data: dados.novaData});
}

export async function reabrirCadastro(codSubprocesso: number, justificativa: string): Promise<void> { await postar(caminhoSubprocesso(codSubprocesso, "/reabrir-cadastro"), {justificativa}); }

export async function reabrirRevisaoCadastro(codSubprocesso: number, justificativa: string): Promise<void> { await postar(caminhoSubprocesso(codSubprocesso, "/reabrir-revisao-cadastro"), {justificativa}); }

export async function obterStatus(codSubprocesso: number): Promise<SubprocessoStatus> { return obter<SubprocessoStatus>(caminhoSubprocesso(codSubprocesso, "/status")); }

export async function buscarSubprocessoDetalhe(codSubprocesso: number): Promise<SubprocessoDetalhe> {
    return mapearDetalheSubprocesso(await obter<SubprocessoDetalheResponse>(caminhoSubprocesso(codSubprocesso)));
}

export async function buscarContextoEdicao(codSubprocesso: number): Promise<ContextoEdicaoSubprocesso> {
    return mapearContextoComDetalhes(await obter<ContextoEdicaoSubprocessoResponse>(caminhoSubprocesso(codSubprocesso, "/contexto-edicao")));
}

export async function buscarContextoEdicaoPorProcessoEUnidade(
    codProcesso: number,
    siglaUnidade: string,
): Promise<ContextoEdicaoSubprocesso> {
    return mapearContextoComDetalhes(await obter<ContextoEdicaoSubprocessoResponse>(`${CAMINHO_SUBPROCESSOS}/contexto-edicao/buscar`, {codProcesso, siglaUnidade}));
}

export async function buscarContextoCadastroAtividades(
    codSubprocesso: number,
): Promise<ContextoCadastroAtividadesSubprocesso> {
    return mapearContextoComDetalhes(await obter<ContextoCadastroAtividadesSubprocessoResponse>(caminhoSubprocesso(codSubprocesso, "/contexto-cadastro-atividades")));
}

export async function buscarContextoCadastroAtividadesPorProcessoEUnidade(
    codProcesso: number,
    siglaUnidade: string,
): Promise<ContextoCadastroAtividadesSubprocesso> {
    return mapearContextoComDetalhes(await obter<ContextoCadastroAtividadesSubprocessoResponse>(`${CAMINHO_SUBPROCESSOS}/contexto-cadastro-atividades/buscar`, {codProcesso, siglaUnidade}));
}

export async function buscarSubprocessoPorProcessoEUnidade(
    codProcesso: number,
    siglaUnidade: string,
): Promise<BuscarSubprocessoPorProcessoEUnidadeResponse> {
    return obter<BuscarSubprocessoPorProcessoEUnidadeResponse>(`${CAMINHO_SUBPROCESSOS}/buscar`, {codProcesso, siglaUnidade});
}

export const listarAnalisesCadastro = async (codSubprocesso: number): Promise<Analise[]> => obter<Analise[]>(caminhoSubprocesso(codSubprocesso, "/historico-cadastro"));
export const listarAnalisesValidacao = async (codSubprocesso: number): Promise<Analise[]> => obter<Analise[]>(caminhoSubprocesso(codSubprocesso, "/historico-validacao"));
