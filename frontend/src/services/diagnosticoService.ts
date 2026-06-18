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

const BASE = '/subprocessos';
const caminhoDiagnostico = (codSubprocesso: number, sufixo = ''): string =>
    `${BASE}/${codSubprocesso}/diagnostico${sufixo}`;

/** GET /subprocessos/{id}/diagnostico/contexto */
export async function obterContextoDiagnostico(codSubprocesso: number): Promise<DiagnosticoContexto> {
    return apiGet(caminhoDiagnostico(codSubprocesso, '/contexto'));
}

/** GET /subprocessos/{id}/diagnostico/autoavaliacao */
export async function obterAutoavaliacao(codSubprocesso: number): Promise<Autoavaliacao> {
    return apiGet(caminhoDiagnostico(codSubprocesso, '/autoavaliacao'));
}

/** POST /subprocessos/{id}/diagnostico/autoavaliacao — salvamento automático */
export async function salvarAutoavaliacao(codSubprocesso: number, request: AutoavaliacaoRequest): Promise<void> {
    return apiPost(caminhoDiagnostico(codSubprocesso, '/autoavaliacao'), request);
}

/** POST /subprocessos/{id}/diagnostico/autoavaliacao/concluir */
export async function concluirAutoavaliacao(codSubprocesso: number): Promise<void> {
    return apiPost(caminhoDiagnostico(codSubprocesso, '/autoavaliacao/concluir'));
}

/** GET /subprocessos/{id}/diagnostico/consenso — consenso do servidor logado */
export async function obterConsenso(codSubprocesso: number): Promise<Consenso> {
    return apiGet(caminhoDiagnostico(codSubprocesso, '/consenso'));
}

/** GET /subprocessos/{id}/diagnostico/consenso/{servidorTitulo} */
export async function obterConsensoServidor(
    codSubprocesso: number,
    servidorTitulo: string,
): Promise<Consenso> {
    return apiGet(caminhoDiagnostico(codSubprocesso, `/consenso/${encodeURIComponent(servidorTitulo)}`));
}

/** POST /subprocessos/{id}/diagnostico/consenso/{servidorTitulo} */
export async function salvarConsenso(
    codSubprocesso: number,
    servidorTitulo: string,
    request: ConsensoRequest,
): Promise<void> {
    return apiPost(caminhoDiagnostico(codSubprocesso, `/consenso/${encodeURIComponent(servidorTitulo)}`), request);
}

/** POST /subprocessos/{id}/diagnostico/consenso/{servidorTitulo}/concluir */
export async function concluirConsenso(
    codSubprocesso: number,
    servidorTitulo: string,
): Promise<void> {
    return apiPost(caminhoDiagnostico(codSubprocesso, `/consenso/${encodeURIComponent(servidorTitulo)}/concluir`));
}

/** POST /subprocessos/{id}/diagnostico/consenso/aprovar */
export async function aprovarConsenso(codSubprocesso: number): Promise<void> {
    return apiPost(caminhoDiagnostico(codSubprocesso, '/consenso/aprovar'));
}

/** POST /subprocessos/{id}/diagnostico/avaliacoes/{servidorTitulo}/impossibilitar */
export async function impossibilitarAvaliacao(
    codSubprocesso: number,
    servidorTitulo: string,
    request: JustificativaRequest,
): Promise<void> {
    return apiPost(
        caminhoDiagnostico(codSubprocesso, `/avaliacoes/${encodeURIComponent(servidorTitulo)}/impossibilitar`),
        request,
    );
}

/** POST /subprocessos/{id}/diagnostico/avaliacoes/{servidorTitulo}/reverter-impossibilidade */
export async function permitirAvaliacao(
    codSubprocesso: number,
    servidorTitulo: string,
): Promise<void> {
    return apiPost(
        caminhoDiagnostico(codSubprocesso, `/avaliacoes/${encodeURIComponent(servidorTitulo)}/reverter-impossibilidade`),
    );
}

/** GET /subprocessos/{id}/diagnostico/equipe */
export async function obterEquipe(codSubprocesso: number): Promise<DiagnosticoEquipe> {
    return apiGet(caminhoDiagnostico(codSubprocesso, '/equipe'));
}

/** POST /subprocessos/{id}/diagnostico/situacoes-capacitacao — salvamento automático */
export async function salvarSituacoesCapacitacao(
    codSubprocesso: number,
    request: SituacoesCapacitacaoRequest,
): Promise<void> {
    return apiPost(caminhoDiagnostico(codSubprocesso, '/situacoes-capacitacao'), request);
}

/** GET /subprocessos/{id}/diagnostico/unidade */
export async function obterDiagnosticoUnidade(codSubprocesso: number): Promise<DiagnosticoUnidade> {
    return apiGet(caminhoDiagnostico(codSubprocesso, '/unidade'));
}

/** POST /subprocessos/{id}/diagnostico/concluir */
export async function concluirDiagnostico(codSubprocesso: number): Promise<void> {
    return apiPost(caminhoDiagnostico(codSubprocesso, '/concluir'));
}

/** GET /subprocessos/{id}/diagnostico/concluir/validacao */
export async function validarConclusaoDiagnostico(codSubprocesso: number): Promise<void> {
    return apiGet(caminhoDiagnostico(codSubprocesso, '/concluir/validacao'));
}

/** POST /subprocessos/{id}/diagnostico/validar */
export async function validarDiagnostico(
    codSubprocesso: number,
    request?: TextoOpcionalRequest,
): Promise<void> {
    return apiPost(caminhoDiagnostico(codSubprocesso, '/validar'), request);
}

/** GET /subprocessos/{id}/diagnostico/validar/validacao */
export async function validarAcaoValidarDiagnostico(codSubprocesso: number): Promise<void> {
    return apiGet(caminhoDiagnostico(codSubprocesso, '/validar/validacao'));
}

/** POST /subprocessos/{id}/diagnostico/devolver */
export async function devolverDiagnostico(
    codSubprocesso: number,
    request: JustificativaRequest,
): Promise<void> {
    return apiPost(caminhoDiagnostico(codSubprocesso, '/devolver'), request);
}

/** GET /subprocessos/{id}/diagnostico/devolver/validacao */
export async function validarAcaoDevolverDiagnostico(codSubprocesso: number): Promise<void> {
    return apiGet(caminhoDiagnostico(codSubprocesso, '/devolver/validacao'));
}

/** POST /subprocessos/{id}/diagnostico/homologar */
export async function homologarDiagnostico(
    codSubprocesso: number,
    request?: TextoOpcionalRequest,
): Promise<void> {
    return apiPost(caminhoDiagnostico(codSubprocesso, '/homologar'), request);
}

/** GET /subprocessos/{id}/diagnostico/homologar/validacao */
export async function validarAcaoHomologarDiagnostico(codSubprocesso: number): Promise<void> {
    return apiGet(caminhoDiagnostico(codSubprocesso, '/homologar/validacao'));
}
