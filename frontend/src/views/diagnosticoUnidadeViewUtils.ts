import type {ColorVariant} from 'bootstrap-vue-next';
import {formatarDataBR} from '@/utils';
import {TEXTOS} from '@/constants/textos';
import type {
    SituacaoAvaliacaoServidor,
    ValorSituacaoCapacitacao,
} from '@/types/diagnostico-competencias';
import type {ResponsavelDto} from '@/types/tipos';

export function formatDataSimples(dataStr: string | null): string {
    return dataStr ? formatarDataBR(dataStr) : '';
}

export function formatTipoResponsabilidade(resp: ResponsavelDto | null): string {
    if (!resp?.tipo) return '';
    if (resp.tipo === 'Substituição' && resp.dataFim) {
        return `Substituição (até ${formatDataSimples(resp.dataFim)})`;
    }
    if (resp.tipo === 'Atribuição temporária' && resp.dataFim) {
        return `Atrib. temporária (até ${formatDataSimples(resp.dataFim)})`;
    }
    return resp.tipo;
}

export function formatarNota(valor: number | null): string {
    if (valor === null) return TEXTOS.diagnostico.NOTA_NAO_INFORMADA;
    if (valor === 0) return TEXTOS.diagnostico.NOTA_NA;
    return String(valor);
}

export function formatarSituacaoCapacitacaoResumida(situacaoCapacitacao: ValorSituacaoCapacitacao | null): string {
    if (!situacaoCapacitacao) {
        return TEXTOS.diagnostico.NOTA_NAO_INFORMADA;
    }
    return `${situacaoCapacitacao} - ${formatarSituacaoCapacitacao(situacaoCapacitacao)}`;
}

export function formatarSituacaoCapacitacao(situacaoCapacitacao: ValorSituacaoCapacitacao | null): string {
    switch (situacaoCapacitacao) {
        case 'NA':
            return TEXTOS.diagnostico.CAPACITACAO_NA;
        case 'AC':
            return TEXTOS.diagnostico.CAPACITACAO_AC;
        case 'EC':
            return TEXTOS.diagnostico.CAPACITACAO_EC;
        case 'C':
            return TEXTOS.diagnostico.CAPACITACAO_C;
        case 'I':
            return TEXTOS.diagnostico.CAPACITACAO_I;
        default:
            return TEXTOS.diagnostico.NOTA_NAO_INFORMADA;
    }
}

export function varianteSituacaoServidor(situacaoServidor: SituacaoAvaliacaoServidor): ColorVariant {
    switch (situacaoServidor) {
        case 'CONSENSO_APROVADO':
            return 'success';
        case 'AVALIACAO_IMPOSSIBILITADA':
            return 'secondary';
        case 'CONSENSO_CRIADO':
            return 'warning';
        case 'AUTOAVALIACAO_CONCLUIDA':
            return 'info';
        default:
            return 'light';
    }
}

export function formatarSituacaoServidor(situacaoServidor: SituacaoAvaliacaoServidor): string {
    const mapa: Record<SituacaoAvaliacaoServidor, string> = {
        AUTOAVALIACAO_NAO_INICIADA: TEXTOS.diagnostico.SITUACAO_NAO_REALIZADA,
        AUTOAVALIACAO_CONCLUIDA: TEXTOS.diagnostico.SITUACAO_AUTOAVALIACAO_CONCLUIDA,
        CONSENSO_CRIADO: TEXTOS.diagnostico.SITUACAO_CONSENSO_CRIADO,
        CONSENSO_APROVADO: TEXTOS.diagnostico.SITUACAO_CONSENSO_APROVADO,
        AVALIACAO_IMPOSSIBILITADA: TEXTOS.diagnostico.SITUACAO_IMPOSSIBILITADA,
    };
    return mapa[situacaoServidor] ?? situacaoServidor;
}
