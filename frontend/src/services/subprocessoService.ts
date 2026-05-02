import type {
    Analise,
    Atividade,
    AtividadeImpactada,
    AtividadeOperacaoResponse,
    CompetenciaImpactada,
    ContextoCadastroAtividadesSubprocesso,
    ContextoEdicaoSubprocesso,
    DisponibilizarMapaRequest,
    ImpactoMapa,
    MapaAjuste,
    MapaCompleto,
    SalvarAjustesRequest,
    SalvarCompetenciaRequest,
    SalvarMapaRequest,
    SubprocessoDetalhe,
    SubprocessoDetalheResponse,
    SubprocessoStatus,
    ValidacaoCadastro
} from "@/types/tipos";
import {normalizarPermissoesSubprocesso} from "@/utils/permissoesSubprocesso";
import apiClient from "../axios-setup";

const CAMINHO_SUBPROCESSOS = "/subprocessos";

interface ImportarAtividadesRequest {
    codSubprocessoOrigem: number;
    codigosAtividades?: number[];
}

interface BuscarSubprocessoPorProcessoEUnidadeResponse {
    codigo: number;
}

interface ContextoComDetalhesResponse {
    detalhes: SubprocessoDetalheResponse;
}

interface ImpactoMapaResponse {
    temImpactos: boolean;
    inseridas: AtividadeImpactada[];
    removidas: AtividadeImpactada[];
    alteradas: AtividadeImpactada[];
    competenciasImpactadas: CompetenciaImpactada[];
    totalInseridas: number;
    totalRemovidas: number;
    totalAlteradas: number;
    totalCompetenciasImpactadas: number;
}

function caminhoSubprocesso(codSubprocesso: number, sufixo = ""): string {
    return `${CAMINHO_SUBPROCESSOS}/${codSubprocesso}${sufixo}`;
}

function mapearDetalheSubprocesso(dto: SubprocessoDetalheResponse): SubprocessoDetalhe {
    const subprocesso = dto.subprocesso;
    return {
        codigo: subprocesso.codigo,
        unidade: subprocesso.unidade,
        titular: dto.titular,
        responsavel: dto.responsavel,
        situacao: subprocesso.situacao,
        localizacaoAtual: dto.localizacaoAtual,
        processoDescricao: subprocesso.processoDescricao,
        dataCriacaoProcesso: subprocesso.dataCriacaoProcesso,
        ultimaDataLimiteSubprocesso: subprocesso.ultimaDataLimite,
        tipoProcesso: subprocesso.tipoProcesso,
        prazoEtapaAtual: subprocesso.dataLimiteEtapa2 ?? subprocesso.dataLimiteEtapa1,
        isEmAndamento: subprocesso.isEmAndamento,
        etapaAtual: subprocesso.etapaAtual,
        movimentacoes: dto.movimentacoes,
        elementosProcesso: [],
        permissoes: normalizarPermissoesSubprocesso(dto.permissoes),
    };
}

function mapearContextoComDetalhes<T extends ContextoComDetalhesResponse>(contexto: T): Omit<T, "detalhes"> & { detalhes: SubprocessoDetalhe } {
    return {
        ...contexto,
        detalhes: mapearDetalheSubprocesso(contexto.detalhes),
    };
}

function mapearPayloadCompetencia(competencia: SalvarCompetenciaRequest) {
    return {
        descricao: competencia.descricao,
        atividadesCodigos: competencia.atividadesCodigos,
    };
}

async function postarAcaoEmBloco(
    acao: string,
    payload: { unidadeCodigos: number[]; dataLimite?: string }
): Promise<void> {
    await apiClient.post(`${CAMINHO_SUBPROCESSOS}/${acao}`, {
        subprocessos: payload.unidadeCodigos,
        ...(payload.dataLimite ? {dataLimite: payload.dataLimite} : {}),
    });
}

export async function importarAtividades(
    codSubprocessoDestino: number,
    codSubprocessoOrigem: number,
    codigosAtividades?: number[],
): Promise<AtividadeOperacaoResponse> {
    const request: ImportarAtividadesRequest = {
        codSubprocessoOrigem: codSubprocessoOrigem,
        ...(codigosAtividades && codigosAtividades.length > 0 ? {codigosAtividades} : {}),
    };
    const response = await apiClient.post<AtividadeOperacaoResponse>(
        caminhoSubprocesso(codSubprocessoDestino, "/importar-atividades"),
        request,
    );
    return response.data;
}

export async function listarAtividades(codSubprocesso: number): Promise<Atividade[]> {
    const response = await apiClient.get<ContextoEdicaoSubprocesso>(caminhoSubprocesso(codSubprocesso, "/contexto-edicao"));
    return response.data.mapa.atividades;
}

export async function listarAtividadesParaImportacao(codSubprocesso: number): Promise<Atividade[]> {
    const response = await apiClient.get<Atividade[]>(caminhoSubprocesso(codSubprocesso, "/atividades-importacao"));
    return response.data;
}

export async function validarCadastro(codSubprocesso: number): Promise<ValidacaoCadastro> {
    const response = await apiClient.get<ValidacaoCadastro>(caminhoSubprocesso(codSubprocesso, "/validar-cadastro"));
    return response.data;
}

export async function alterarDataLimiteSubprocesso(
    codSubprocesso: number,
    dados: { novaData: string },
): Promise<void> {
    await apiClient.post(caminhoSubprocesso(codSubprocesso, "/data-limite"), {
        data: dados.novaData,
    });
}

export async function reabrirCadastro(
    codSubprocesso: number,
    justificativa: string,
): Promise<void> {
    await apiClient.post(caminhoSubprocesso(codSubprocesso, "/reabrir-cadastro"), {justificativa});
}

export async function reabrirRevisaoCadastro(
    codSubprocesso: number,
    justificativa: string,
): Promise<void> {
    await apiClient.post(caminhoSubprocesso(codSubprocesso, "/reabrir-revisao-cadastro"), {justificativa});
}

export async function obterStatus(codSubprocesso: number): Promise<SubprocessoStatus> {
    const response = await apiClient.get<SubprocessoStatus>(caminhoSubprocesso(codSubprocesso, "/status"));
    return response.data;
}

export async function buscarSubprocessoDetalhe(
    codSubprocesso: number,
): Promise<SubprocessoDetalheResponse> {
    const response = await apiClient.get<SubprocessoDetalheResponse>(caminhoSubprocesso(codSubprocesso));
    return response.data;
}

export async function buscarContextoEdicao(
    codSubprocesso: number,
): Promise<ContextoEdicaoSubprocesso> {
    const response = await apiClient.get<ContextoEdicaoSubprocesso & ContextoComDetalhesResponse>(caminhoSubprocesso(codSubprocesso, "/contexto-edicao"));
    return mapearContextoComDetalhes(response.data);
}

export async function buscarContextoEdicaoPorProcessoEUnidade(
    codProcesso: number,
    siglaUnidade: string,
): Promise<ContextoEdicaoSubprocesso> {
    const response = await apiClient.get<ContextoEdicaoSubprocesso & ContextoComDetalhesResponse>(`${CAMINHO_SUBPROCESSOS}/contexto-edicao/buscar`, {
        params: {codProcesso, siglaUnidade},
    });
    return mapearContextoComDetalhes(response.data);
}

export async function buscarContextoCadastroAtividades(
    codSubprocesso: number,
): Promise<ContextoCadastroAtividadesSubprocesso> {
    const response = await apiClient.get<ContextoCadastroAtividadesSubprocesso & ContextoComDetalhesResponse>(
        caminhoSubprocesso(codSubprocesso, "/contexto-cadastro-atividades"),
    );
    return mapearContextoComDetalhes(response.data);
}

export async function buscarContextoCadastroAtividadesPorProcessoEUnidade(
    codProcesso: number,
    siglaUnidade: string,
): Promise<ContextoCadastroAtividadesSubprocesso> {
    const response = await apiClient.get<ContextoCadastroAtividadesSubprocesso & ContextoComDetalhesResponse>(
        `${CAMINHO_SUBPROCESSOS}/contexto-cadastro-atividades/buscar`,
        {
            params: {codProcesso, siglaUnidade},
        },
    );
    return mapearContextoComDetalhes(response.data);
}

export async function buscarSubprocessoPorProcessoEUnidade(
    codProcesso: number,
    siglaUnidade: string,
): Promise<BuscarSubprocessoPorProcessoEUnidadeResponse> {
    const response = await apiClient.get<BuscarSubprocessoPorProcessoEUnidadeResponse>(`${CAMINHO_SUBPROCESSOS}/buscar`, {
        params: {codProcesso, siglaUnidade},
    });
    return response.data;
}

// Mapa / Competencias

export async function obterSugestoesMapa(
    codSubprocesso: number,
): Promise<string> {
    const response = await apiClient.get<{sugestoes: string}>(caminhoSubprocesso(codSubprocesso, "/sugestoes"));
    return response.data.sugestoes;
}

export async function apresentarSugestoes(
    codSubprocesso: number,
    dados: { sugestoes: string },
): Promise<void> {
    await apiClient.post(caminhoSubprocesso(codSubprocesso, "/apresentar-sugestoes"), {
        texto: dados.sugestoes,
    });
}

export async function verificarImpactosMapa(codSubprocesso: number): Promise<ImpactoMapa> {
    const response = await apiClient.get<ImpactoMapaResponse>(caminhoSubprocesso(codSubprocesso, "/impactos-mapa"));
    const data = response.data;

    return {
        temImpactos: data.temImpactos,
        atividadesInseridas: data.inseridas,
        atividadesRemovidas: data.removidas,
        atividadesAlteradas: data.alteradas,
        competenciasImpactadas: data.competenciasImpactadas,
        totalAtividadesInseridas: data.totalInseridas,
        totalAtividadesRemovidas: data.totalRemovidas,
        totalAtividadesAlteradas: data.totalAlteradas,
        totalCompetenciasImpactadas: data.totalCompetenciasImpactadas
    };
}

export async function obterMapaCompleto(codSubprocesso: number): Promise<MapaCompleto> {
    const response = await apiClient.get<MapaCompleto>(caminhoSubprocesso(codSubprocesso, "/mapa-completo"));
    return response.data;
}

export async function salvarMapaCompleto(
    codSubprocesso: number,
    data: SalvarMapaRequest,
): Promise<MapaCompleto> {
    const response = await apiClient.post<MapaCompleto>(
        caminhoSubprocesso(codSubprocesso, "/mapa-completo"),
        data,
    );
    return response.data;
}

export async function obterMapaAjuste(codSubprocesso: number): Promise<MapaAjuste> {
    const response = await apiClient.get<MapaAjuste>(caminhoSubprocesso(codSubprocesso, "/mapa-ajuste"));
    return response.data;
}

export async function salvarMapaAjuste(
    codSubprocesso: number,
    data: SalvarAjustesRequest,
): Promise<void> {
    await apiClient.post(
        caminhoSubprocesso(codSubprocesso, "/mapa-ajuste/atualizar"),
        data,
    );
}

export async function disponibilizarMapa(
    codSubprocesso: number,
    data: DisponibilizarMapaRequest,
): Promise<void> {
    await apiClient.post(caminhoSubprocesso(codSubprocesso, "/disponibilizar-mapa"), data);
}

export async function validarMapa(codSubprocesso: number): Promise<void> {
    await apiClient.post(caminhoSubprocesso(codSubprocesso, "/validar-mapa"));
}

export async function homologarValidacao(
    codSubprocesso: number,
    dados: { texto: string },
): Promise<void> {
    await apiClient.post(caminhoSubprocesso(codSubprocesso, "/homologar-validacao"), dados);
}

export async function aceitarValidacao(
    codSubprocesso: number,
    dados: { texto: string },
): Promise<void> {
    await apiClient.post(caminhoSubprocesso(codSubprocesso, "/aceitar-validacao"), dados);
}

export async function devolverValidacao(
    codSubprocesso: number,
    dados: { justificativa: string },
): Promise<void> {
    await apiClient.post(caminhoSubprocesso(codSubprocesso, "/devolver-validacao"), dados);
}

export async function adicionarCompetencia(
    codSubprocesso: number,
    competencia: SalvarCompetenciaRequest,
): Promise<MapaCompleto> {
    const response = await apiClient.post<MapaCompleto>(
        caminhoSubprocesso(codSubprocesso, "/competencia"),
        mapearPayloadCompetencia(competencia),
    );
    return response.data;
}

export async function atualizarCompetencia(
    codSubprocesso: number,
    codCompetencia: number,
    competencia: SalvarCompetenciaRequest,
): Promise<MapaCompleto> {
    const response = await apiClient.post<MapaCompleto>(
        caminhoSubprocesso(codSubprocesso, `/competencia/${codCompetencia}`),
        mapearPayloadCompetencia(competencia),
    );
    return response.data;
}

export async function removerCompetencia(
    codSubprocesso: number,
    codCompetencia: number,
): Promise<MapaCompleto> {
    const response = await apiClient.post<MapaCompleto>(
        caminhoSubprocesso(codSubprocesso, `/competencia/${codCompetencia}/remover`),
    );
    return response.data;
}

// Acoes em Bloco

export async function aceitarCadastroEmBloco(
    payload: { unidadeCodigos: number[] }
): Promise<void> {
    await postarAcaoEmBloco("aceitar-cadastro-bloco", payload);
}

export async function homologarCadastroEmBloco(
    payload: { unidadeCodigos: number[] }
): Promise<void> {
    await postarAcaoEmBloco("homologar-cadastro-bloco", payload);
}

export async function aceitarValidacaoEmBloco(
    payload: { unidadeCodigos: number[] }
): Promise<void> {
    await postarAcaoEmBloco("aceitar-validacao-bloco", payload);
}

export async function homologarValidacaoEmBloco(
    payload: { unidadeCodigos: number[] }
): Promise<void> {
    await postarAcaoEmBloco("homologar-validacao-bloco", payload);
}

export async function disponibilizarMapaEmBloco(
    payload: { unidadeCodigos: number[]; dataLimite?: string }
): Promise<void> {
    await postarAcaoEmBloco("disponibilizar-mapa-bloco", payload);
}

// Analises / Historico

export const listarAnalisesCadastro = async (
    codSubprocesso: number,
): Promise<Analise[]> => {
    const response = await apiClient.get<Analise[]>(
        caminhoSubprocesso(codSubprocesso, "/historico-cadastro"),
    );
    return response.data;
};

export const listarAnalisesValidacao = async (
    codSubprocesso: number,
): Promise<Analise[]> => {
    const response = await apiClient.get<Analise[]>(
        caminhoSubprocesso(codSubprocesso, "/historico-validacao"),
    );
    return response.data;
};
