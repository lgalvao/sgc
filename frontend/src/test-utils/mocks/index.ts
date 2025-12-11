import type {SubprocessoDetalhe, Unidade} from "@/types/tipos";
import {SituacaoSubprocesso, TipoProcesso} from "@/types/tipos";

export const mockUnidade: Unidade = {
    codigo: 1,
    nome: "Unidade Mock",
    sigla: "UNID",
};

export const mockProcessoDetalhe: SubprocessoDetalhe = {
    unidade: {codigo: 1, nome: "Teste", sigla: "TST"},
    titular: {
        codigo: 1,
        nome: "Titular Teste",
        tituloEleitoral: "123",
        email: "t@t.com",
        ramal: "123",
        unidade: {codigo: 1, nome: "Teste", sigla: "TST"},
    },
    responsavel: {
        codigo: 1,
        nome: "Responsável Teste",
        tituloEleitoral: "456",
        email: "r@r.com",
        ramal: "456",
        unidade: {codigo: 1, nome: "Teste", sigla: "TST"},
    },
    situacao: SituacaoSubprocesso.CADASTRO_EM_ANDAMENTO,
    situacaoLabel: "Em andamento",
    localizacaoAtual: "Na unidade",
    processoDescricao: "Processo Teste",
    tipoProcesso: TipoProcesso.MAPEAMENTO,
    prazoEtapaAtual: "2025-12-31",
    isEmAndamento: true,
    etapaAtual: 1,
    movimentacoes: [],
    elementosProcesso: [],
    permissoes: {
        podeVerPagina: true,
        podeEditarMapa: true,
        podeVisualizarMapa: true,
        podeDisponibilizarCadastro: true,
        podeDevolverCadastro: true,
        podeAceitarCadastro: true,
        podeVisualizarDiagnostico: true,
        podeAlterarDataLimite: true,
        podeVisualizarImpacto: true,
        podeRealizarAutoavaliacao: true,
    },
};
