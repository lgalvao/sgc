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
    descricao: 'Teste',
    tipo: TipoProcesso.MAPEAMENTO,
    situacao: SituacaoProcesso.EM_ANDAMENTO,
    dataLimite: '2025-12-31',
    dataCriacao: '2024-01-01',
    unidades: [],
    resumoSubprocessos: [],
    podeFinalizar: false,
    podeHomologarCadastro: false,
    podeHomologarMapa: false,
};