import {describe, expect, it} from 'vitest';
import processoRoutes from '@/router/processo.routes';

describe('processo.routes.ts', () => {
  it('deve definir as rotas corretamente', () => {
    expect(processoRoutes).toHaveLength(7);
    const paths = processoRoutes.map(r => r.path);
    expect(paths).toContain('/processo/cadastro');
    expect(paths).toContain('/processo/:codProcesso');
    expect(paths).toContain('/processo/:codProcesso/:siglaUnidade');
  });

  it('deve tratar corretamente as props da rota para Subprocesso', () => {
    const route = processoRoutes.find(r => r.name === 'Subprocesso');
    expect(route).toBeDefined();

    if (route && typeof route.props === 'function') {
      const props = route.props({
        params: { codProcesso: '123', siglaUnidade: 'TEST' }
      } as any);
      expect(props).toEqual({ codProcesso: 123, siglaUnidade: 'TEST' });
    } else {
        throw new Error('Props deve ser uma função');
    }
  });

  it('deve tratar corretamente as props da rota para SubprocessoMapa', () => {
    const route = processoRoutes.find(r => r.name === 'SubprocessoMapa');

    if (route && typeof route.props === 'function') {
      const props = route.props({
        params: { codProcesso: '456', siglaUnidade: 'ABC' }
      } as any);
      expect(props).toEqual({ codProcesso: 456, sigla: 'ABC' });
    } else {
        throw new Error('Props deve ser uma função');
    }
  });

  it('deve tratar corretamente as props da rota para SubprocessoVisMapa', () => {
    const route = processoRoutes.find(r => r.name === 'SubprocessoVisMapa');
    if (route && typeof route.props === 'function') {
      const props = route.props({
        params: { codProcesso: '789', siglaUnidade: 'XYZ' }
      } as any);
      expect(props).toEqual({ codProcesso: 789, sigla: 'XYZ' });
    } else {
        throw new Error('Props deve ser uma função');
    }
  });

  it('deve tratar corretamente as props da rota para SubprocessoCadastro', () => {
    const route = processoRoutes.find(r => r.name === 'SubprocessoCadastro');
    if (route && typeof route.props === 'function') {
      const props = route.props({
        params: { codProcesso: '101', siglaUnidade: 'CAD' }
      } as any);
      expect(props).toEqual({ codProcesso: 101, sigla: 'CAD' });
    } else {
        throw new Error('Props deve ser uma função');
    }
  });

  it('deve tratar corretamente as props da rota para SubprocessoVisCadastro', () => {
    const route = processoRoutes.find(r => r.name === 'SubprocessoVisCadastro');
    if (route && typeof route.props === 'function') {
      const props = route.props({
        params: { codProcesso: '102', siglaUnidade: 'VIS' }
      } as any);
      expect(props).toEqual({ codProcesso: 102, sigla: 'VIS' });
    } else {
        throw new Error('Props deve ser uma função');
    }
  });

  it('deve carregar componentes sob demanda (lazy load)', async () => {
    const cadProcesso = processoRoutes.find(r => r.name === 'CadProcesso');
    // @ts-expect-error - testing internal property
    const component = await cadProcesso?.component();
    expect(component.default).toBeDefined();
  });
});
