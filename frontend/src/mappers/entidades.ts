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
        codigo: obj.id ?? obj.codigo ?? 0,
        descricao: obj.descricao ?? obj.desc ?? '',
        atividadesAssociadas: obj.atividadesAssociadas ?? obj.atividades ?? obj.atividades_ids ?? []
    };
}

export function mapAtividade(obj: any): Atividade {
    return {
        codigo: obj.id ?? obj.codigo ?? 0,
        descricao: obj.descricao ?? obj.desc ?? '',
        conhecimentos: Array.isArray(obj.conhecimentos) ? obj.conhecimentos.map(mapConhecimento) : []
    };
}

export function mapMapa(obj: any): Mapa {
    return {
        codigo: obj.id ?? obj.codigo ?? 0,
        unidade: obj.unidade ?? obj.unidade_sigla ?? '',
        situacao: obj.situacao ?? obj.situacao_codigo ?? '',
        idProcesso: obj.idProcesso ?? obj.processo_codigo ?? 0,
        competencias: Array.isArray(obj.competencias) ? obj.competencias.map(mapCompetencia) : [],
        dataCriacao: (obj.dataCriacao ? parseDate(obj.dataCriacao) : (obj.data_criacao ? parseDate(obj.data_criacao) : new Date())).toISOString(),
        dataDisponibilizacao: (obj.dataDisponibilizacao ? parseDate(obj.dataDisponibilizacao) : (obj.data_hora_disponibilizado ? parseDate(obj.data_hora_disponibilizado) : null))?.toISOString(),
        dataFinalizacao: (obj.dataFinalizacao ? parseDate(obj.dataFinalizacao) : (obj.data_hora_homologado ? parseDate(obj.data_hora_homologado) : null))?.toISOString(),
        descricao: obj.descricao ?? obj.observacoes_disponibilizacao ?? obj.sugestoes_apresentadas ?? undefined
    };
}

export function mapAtividadesArray(arr: any[] = []): Atividade[] {
    return arr.map(mapAtividade);
}

export function mapMapasArray(arr: any[] = []): Mapa[] {
    return arr.map(mapMapa);
}

