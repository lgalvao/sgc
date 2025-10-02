import {parseDate} from '@/utils';
import type {Atividade, Competencia, Conhecimento, Mapa} from '@/types/tipos';

export function mapConhecimento(obj: any): Conhecimento {
    return {
        id: obj.id ?? obj.codigo ?? 0,
        descricao: obj.descricao ?? obj.desc ?? ''
    };
}

export function mapCompetencia(obj: any): Competencia {
    return {
        id: obj.id ?? obj.codigo ?? 0,
        descricao: obj.descricao ?? obj.desc ?? '',
        atividadesAssociadas: obj.atividadesAssociadas ?? obj.atividades ?? obj.atividades_ids ?? []
    };
}

export function mapAtividade(obj: any): Atividade {
    return {
        id: obj.id ?? obj.codigo ?? 0,
        descricao: obj.descricao ?? obj.desc ?? '',
        idSubprocesso: obj.idSubprocesso ?? obj.atividade_subprocesso ?? 0,
        conhecimentos: Array.isArray(obj.conhecimentos) ? obj.conhecimentos.map(mapConhecimento) : []
    };
}

export function mapMapa(obj: any): Mapa {
    return {
        id: obj.id ?? obj.codigo ?? 0,
        unidade: obj.unidade ?? obj.unidade_sigla ?? '',
        situacao: obj.situacao ?? obj.situacao_codigo ?? '',
        idProcesso: obj.idProcesso ?? obj.processo_codigo ?? 0,
        competencias: Array.isArray(obj.competencias) ? obj.competencias.map(mapCompetencia) : [],
        dataCriacao: obj.dataCriacao ? parseDate(obj.dataCriacao) : (obj.data_criacao ? parseDate(obj.data_criacao) : new Date()),
        dataDisponibilizacao: obj.dataDisponibilizacao ? parseDate(obj.dataDisponibilizacao) : (obj.data_hora_disponibilizado ? parseDate(obj.data_hora_disponibilizado) : null),
        dataFinalizacao: obj.dataFinalizacao ? parseDate(obj.dataFinalizacao) : (obj.data_hora_homologado ? parseDate(obj.data_hora_homologado) : null),
        vigente: obj.vigente ?? obj.mapa_vigente ?? undefined,
        dataInicio: obj.dataInicio ? parseDate(obj.dataInicio) : (obj.data_inicio ? parseDate(obj.data_inicio) : undefined),
        dataFim: obj.dataFim ? parseDate(obj.dataFim) : (obj.data_fim ? parseDate(obj.data_fim) : null),
        descricao: obj.descricao ?? obj.observacoes_disponibilizacao ?? obj.sugestoes_apresentadas ?? undefined,
        tipo: obj.tipo ?? null,
        subprocessos: Array.isArray(obj.subprocessos) ? obj.subprocessos : undefined
    };
}

export function mapAtividadesArray(arr: any[] = []): Atividade[] {
    return arr.map(mapAtividade);
}

export function mapMapasArray(arr: any[] = []): Mapa[] {
    return arr.map(mapMapa);
}

