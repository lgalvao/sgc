import { SituacaoProcesso, TipoProcesso } from "@/types/tipos";
import type { ProcessoDetalhe, Unidade } from "@/types/tipos";

export const mockUnidade: Unidade = {
    codigo: 1,
    nome: 'Unidade Mock',
    sigla: 'UNID',
};

import { SituacaoSubprocesso } from "@/types/tipos";

export const mockProcessoDetalhe: ProcessoDetalhe = {
    codigo: 1,
    tipo: TipoProcesso.MAPEAMENTO,
    situacao: SituacaoProcesso.EM_ANDAMENTO,
    unidades: [{...mockUnidade, codUnidade: 1, situacaoSubprocesso: SituacaoSubprocesso.NAO_INICIADO, dataLimite: '2025-12-31', filhos: []}],
    unidadesParticipantes: [{...mockUnidade, codUnidade: 1, situacaoSubprocesso: SituacaoSubprocesso.NAO_INICIADO, dataLimite: '2025-12-31', filhos: []}],
    dataCriacao: '2024-01-01',
    dataFinalizacao: null,
    subprocessos: [],
};