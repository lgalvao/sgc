import {apiGet, apiPost} from '@/utils/apiUtils';
import type {
    Autoavaliacao,
    AutoavaliacaoRequest,
    Consenso,
    ConsensoRequest,
    DiagnosticoContexto,
    DiagnosticoEquipe,
    DiagnosticoUnidade,
    JustificativaRequest,
    SituacoesCapacitacaoRequest,
    TextoOpcionalRequest,
} from '@/types/diagnostico-competencias';

const BASE = '/diagnosticos/subprocessos';

/** GET /diagnosticos/subprocessos/{id}/contexto */
export async function obterContextoDiagnostico(codSubprocesso: number): Promise<DiagnosticoContexto> {
    return apiGet(`${BASE}/${codSubprocesso}/contexto`);
}

/** GET /diagnosticos/subprocessos/{id}/autoavaliacao */
export async function obterAutoavaliacao(codSubprocesso: number): Promise<Autoavaliacao> {
    return apiGet(`${BASE}/${codSubprocesso}/autoavaliacao`);
}

/** POST /diagnosticos/subprocessos/{id}/autoavaliacao — salvamento automático */
export async function salvarAutoavaliacao(codSubprocesso: number, request: AutoavaliacaoRequest): Promise<void> {
    return apiPost(`${BASE}/${codSubprocesso}/autoavaliacao`, request);
}

/** POST /diagnosticos/subprocessos/{id}/autoavaliacao/concluir */
export async function concluirAutoavaliacao(codSubprocesso: number): Promise<void> {
    return apiPost(`${BASE}/${codSubprocesso}/autoavaliacao/concluir`);
}

/** GET /diagnosticos/subprocessos/{id}/consenso — consenso do servidor logado */
export async function obterConsenso(codSubprocesso: number): Promise<Consenso> {
    return apiGet(`${BASE}/${codSubprocesso}/consenso`);
}

/** GET /diagnosticos/subprocessos/{id}/consenso/{servidorTitulo} */
export async function obterConsensoServidor(
    codSubprocesso: number,
    servidorTitulo: string,
): Promise<Consenso> {
    return apiGet(`${BASE}/${codSubprocesso}/consenso/${encodeURIComponent(servidorTitulo)}`);
}

/** POST /diagnosticos/subprocessos/{id}/consenso/{servidorTitulo} */
export async function salvarConsenso(
    codSubprocesso: number,
    servidorTitulo: string,
    request: ConsensoRequest,
): Promise<void> {
    return apiPost(`${BASE}/${codSubprocesso}/consenso/${encodeURIComponent(servidorTitulo)}`, request);
}

/** POST /diagnosticos/subprocessos/{id}/consenso/aprovar */
export async function aprovarConsenso(codSubprocesso: number): Promise<void> {
    return apiPost(`${BASE}/${codSubprocesso}/consenso/aprovar`);
}

/** POST /diagnosticos/subprocessos/{id}/avaliacoes/{servidorTitulo}/impossibilitar */
export async function impossibilitarAvaliacao(
    codSubprocesso: number,
    servidorTitulo: string,
    request: JustificativaRequest,
): Promise<void> {
    return apiPost(
        `${BASE}/${codSubprocesso}/avaliacoes/${encodeURIComponent(servidorTitulo)}/impossibilitar`,
        request,
    );
}

/** GET /diagnosticos/subprocessos/{id}/equipe */
export async function obterEquipe(codSubprocesso: number): Promise<DiagnosticoEquipe> {
    return apiGet(`${BASE}/${codSubprocesso}/equipe`);
}

/** POST /diagnosticos/subprocessos/{id}/situacoes-capacitacao — salvamento automático */
export async function salvarSituacoesCapacitacao(
    codSubprocesso: number,
    request: SituacoesCapacitacaoRequest,
): Promise<void> {
    return apiPost(`${BASE}/${codSubprocesso}/situacoes-capacitacao`, request);
}

/** GET /diagnosticos/subprocessos/{id}/unidade */
export async function obterDiagnosticoUnidade(codSubprocesso: number): Promise<DiagnosticoUnidade> {
    return apiGet(`${BASE}/${codSubprocesso}/unidade`);
}

/** POST /diagnosticos/subprocessos/{id}/concluir */
export async function concluirDiagnostico(codSubprocesso: number): Promise<void> {
    return apiPost(`${BASE}/${codSubprocesso}/concluir`);
}

/** POST /diagnosticos/subprocessos/{id}/validar */
export async function validarDiagnostico(
    codSubprocesso: number,
    request?: TextoOpcionalRequest,
): Promise<void> {
    return apiPost(`${BASE}/${codSubprocesso}/validar`, request);
}

/** POST /diagnosticos/subprocessos/{id}/devolver */
export async function devolverDiagnostico(
    codSubprocesso: number,
    request: JustificativaRequest,
): Promise<void> {
    return apiPost(`${BASE}/${codSubprocesso}/devolver`, request);
}

/** POST /diagnosticos/subprocessos/{id}/homologar */
export async function homologarDiagnostico(
    codSubprocesso: number,
    request?: TextoOpcionalRequest,
): Promise<void> {
    return apiPost(`${BASE}/${codSubprocesso}/homologar`, request);
}
