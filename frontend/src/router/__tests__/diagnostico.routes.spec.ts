import {describe, expect, it} from 'vitest';
import diagnosticoRoutes from '../diagnostico.routes';

describe('diagnostico.routes', () => {
    it('deve expor as rotas do fluxo de diagnóstico com props normalizadas', async () => {
        expect(diagnosticoRoutes).toHaveLength(5);

        const autoavaliacao = diagnosticoRoutes[0];
        expect(autoavaliacao.name).toBe('AutoavaliacaoDiagnostico');
        expect((autoavaliacao.props as any)({
            params: {codSubprocesso: '41', siglaUnidade: 'ASSESSORIA_12'},
        } as any)).toEqual({
            codSubprocesso: 41,
            siglaUnidade: 'ASSESSORIA_12',
        });
        await expect((autoavaliacao.component as any)()).resolves.toBeDefined();

        const ocupacoes = diagnosticoRoutes[1];
        expect(ocupacoes.name).toBe('OcupacoesCriticasDiagnostico');
        expect((ocupacoes.props as any)({
            params: {codSubprocesso: '42', siglaUnidade: 'ASSESSORIA_13'},
        } as any)).toEqual({
            codSubprocesso: 42,
            siglaUnidade: 'ASSESSORIA_13',
        });
        await expect((ocupacoes.component as any)()).resolves.toBeDefined();

        const monitoramento = diagnosticoRoutes[2];
        expect(monitoramento.name).toBe('MonitoramentoDiagnostico');
        expect((monitoramento.props as any)({
            params: {codSubprocesso: '43', siglaUnidade: 'ASSESSORIA_14'},
        } as any)).toEqual({
            codSubprocesso: 43,
            siglaUnidade: 'ASSESSORIA_14',
        });
        await expect((monitoramento.component as any)()).resolves.toBeDefined();

        const consenso = diagnosticoRoutes[3];
        expect(consenso.name).toBe('ConsensoDiagnostico');
        expect((consenso.props as any)({
            params: {codSubprocesso: '44', siglaUnidade: 'ASSESSORIA_15', servidorTitulo: '242426'},
        } as any)).toEqual({
            codSubprocesso: 44,
            siglaUnidade: 'ASSESSORIA_15',
            servidorTitulo: '242426',
        });
        await expect((consenso.component as any)()).resolves.toBeDefined();

        const unidade = diagnosticoRoutes[4];
        expect(unidade.name).toBe('DiagnosticoUnidade');
        expect((unidade.props as any)({
            params: {codSubprocesso: '45', siglaUnidade: 'ASSESSORIA_16'},
        } as any)).toEqual({
            codSubprocesso: 45,
            siglaUnidade: 'ASSESSORIA_16',
        });
        await expect((unidade.component as any)()).resolves.toBeDefined();
    });
});
