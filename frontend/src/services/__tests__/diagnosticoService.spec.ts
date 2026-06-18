import {beforeEach, describe, expect, it, vi} from 'vitest';
import {
    aprovarConsenso,
    concluirAutoavaliacao,
    concluirConsenso,
    concluirDiagnostico,
    devolverDiagnostico,
    homologarDiagnostico,
    impossibilitarAvaliacao,
    obterAutoavaliacao,
    obterConsenso,
    obterConsensoServidor,
    obterContextoDiagnostico,
    obterDiagnosticoUnidade,
    obterEquipe,
    salvarAutoavaliacao,
    salvarConsenso,
    salvarSituacoesCapacitacao,
    validarDiagnostico,
} from '../diagnosticoService';

const {apiGetMock, apiPostMock} = vi.hoisted(() => ({
    apiGetMock: vi.fn(),
    apiPostMock: vi.fn(),
}));

vi.mock('@/utils/apiUtils', () => ({
    apiGet: apiGetMock,
    apiPost: apiPostMock,
}));

describe('diagnosticoService', () => {
    const BASE = '/subprocessos';
    const caminhoDiagnostico = (codSubprocesso: number, sufixo = '') =>
        `${BASE}/${codSubprocesso}/diagnostico${sufixo}`;

    beforeEach(() => {
        vi.clearAllMocks();
    });

    it('deve chamar todos os endpoints GET corretos', async () => {
        apiGetMock.mockResolvedValue(undefined);

        await obterContextoDiagnostico(10);
        await obterAutoavaliacao(11);
        await obterConsenso(12);
        await obterConsensoServidor(13, '242426');
        await obterEquipe(14);
        await obterDiagnosticoUnidade(15);

        expect(apiGetMock).toHaveBeenNthCalledWith(1, caminhoDiagnostico(10, '/contexto'));
        expect(apiGetMock).toHaveBeenNthCalledWith(2, caminhoDiagnostico(11, '/autoavaliacao'));
        expect(apiGetMock).toHaveBeenNthCalledWith(3, caminhoDiagnostico(12, '/consenso'));
        expect(apiGetMock).toHaveBeenNthCalledWith(4, caminhoDiagnostico(13, '/consenso/242426'));
        expect(apiGetMock).toHaveBeenNthCalledWith(5, caminhoDiagnostico(14, '/equipe'));
        expect(apiGetMock).toHaveBeenNthCalledWith(6, caminhoDiagnostico(15, '/unidade'));
    });

    it('deve chamar endpoints POST de autoavaliação e consenso', async () => {
        apiPostMock.mockResolvedValue(undefined);

        await salvarAutoavaliacao(20, {competencias: []});
        await concluirAutoavaliacao(21);
        await salvarConsenso(22, '242426', {competencias: []});
        await concluirConsenso(23, '242426');
        await aprovarConsenso(24);

        expect(apiPostMock).toHaveBeenNthCalledWith(1, caminhoDiagnostico(20, '/autoavaliacao'), {competencias: []});
        expect(apiPostMock).toHaveBeenNthCalledWith(2, caminhoDiagnostico(21, '/autoavaliacao/concluir'));
        expect(apiPostMock).toHaveBeenNthCalledWith(3, caminhoDiagnostico(22, '/consenso/242426'), {competencias: []});
        expect(apiPostMock).toHaveBeenNthCalledWith(4, caminhoDiagnostico(23, '/consenso/242426/concluir'));
        expect(apiPostMock).toHaveBeenNthCalledWith(5, caminhoDiagnostico(24, '/consenso/aprovar'));
    });

    it('deve chamar endpoints POST de impossibilidade, situações e fluxo da unidade', async () => {
        apiPostMock.mockResolvedValue(undefined);

        await impossibilitarAvaliacao(30, '242426', {justificativa: 'Servidor afastado'});
        await salvarSituacoesCapacitacao(31, {situacoes: []} as any);
        await concluirDiagnostico(32);
        await validarDiagnostico(33, {texto: 'Observações'});
        await devolverDiagnostico(34, {justificativa: 'Ajustes'});
        await homologarDiagnostico(35, {texto: 'Homologado'});

        expect(apiPostMock).toHaveBeenNthCalledWith(
            1,
            caminhoDiagnostico(30, '/avaliacoes/242426/impossibilitar'),
            {justificativa: 'Servidor afastado'},
        );
        expect(apiPostMock).toHaveBeenNthCalledWith(2, caminhoDiagnostico(31, '/situacoes-capacitacao'), {situacoes: []});
        expect(apiPostMock).toHaveBeenNthCalledWith(3, caminhoDiagnostico(32, '/concluir'));
        expect(apiPostMock).toHaveBeenNthCalledWith(4, caminhoDiagnostico(33, '/validar'), {texto: 'Observações'});
        expect(apiPostMock).toHaveBeenNthCalledWith(5, caminhoDiagnostico(34, '/devolver'), {justificativa: 'Ajustes'});
        expect(apiPostMock).toHaveBeenNthCalledWith(6, caminhoDiagnostico(35, '/homologar'), {texto: 'Homologado'});
    });

    it('deve aceitar payload opcional ausente em validar e homologar', async () => {
        apiPostMock.mockResolvedValue(undefined);

        await validarDiagnostico(40);
        await homologarDiagnostico(41);

        expect(apiPostMock).toHaveBeenNthCalledWith(1, caminhoDiagnostico(40, '/validar'), undefined);
        expect(apiPostMock).toHaveBeenNthCalledWith(2, caminhoDiagnostico(41, '/homologar'), undefined);
    });
});
