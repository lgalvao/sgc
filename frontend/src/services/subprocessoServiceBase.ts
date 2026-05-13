import type {
    AtividadeImpactada,
    CompetenciaImpactada,
    SalvarCompetenciaRequest,
    SubprocessoDetalhe,
    SubprocessoDetalheResponse,
} from "@/types/tipos";
import {normalizarPermissoesSubprocesso} from "@/utils/permissoesSubprocesso";

export const CAMINHO_SUBPROCESSOS = "/subprocessos";

export interface ImportarAtividadesRequest {
    codSubprocessoOrigem: number;
    codigosAtividades?: number[];
}

export interface ImpactoMapaResponse {
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

export function caminhoSubprocesso(codSubprocesso: number, sufixo = ""): string {
    return `${CAMINHO_SUBPROCESSOS}/${codSubprocesso}${sufixo}`;
}

export function mapearDetalheSubprocesso(dto: SubprocessoDetalheResponse): SubprocessoDetalhe {
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
        dataFimEtapa1: subprocesso.dataFimEtapa1,
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

export function mapearContextoComDetalhes<T extends { detalhes: SubprocessoDetalheResponse }>(
    contexto: T,
): Omit<T, "detalhes"> & { detalhes: SubprocessoDetalhe } {
    return {
        ...contexto,
        detalhes: mapearDetalheSubprocesso(contexto.detalhes),
    };
}

export function mapearPayloadCompetencia(competencia: SalvarCompetenciaRequest) {
    return {
        descricao: competencia.descricao,
        atividadesCodigos: competencia.atividadesCodigos,
    };
}
