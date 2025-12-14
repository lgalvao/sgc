import { describe, it, expect } from 'vitest';
import processoRoutes from '@/router/processo.routes';

describe('processo.routes.ts', () => {
  it('should define routes correctly', () => {
    expect(processoRoutes).toHaveLength(7);
    const paths = processoRoutes.map(r => r.path);
    expect(paths).toContain('/processo/cadastro');
    expect(paths).toContain('/processo/:codProcesso');
    expect(paths).toContain('/processo/:codProcesso/:siglaUnidade');
  });

  it('should handle route props correctly for Subprocesso', () => {
    const route = processoRoutes.find(r => r.name === 'Subprocesso');
    expect(route).toBeDefined();

    if (route && typeof route.props === 'function') {
      const props = route.props({
        params: { codProcesso: '123', siglaUnidade: 'TEST' }
      } as any);
      expect(props).toEqual({ codProcesso: 123, siglaUnidade: 'TEST' });
    } else {
        throw new Error('Props should be a function');
    }
  });

  it('should handle route props correctly for SubprocessoMapa', () => {
    const route = processoRoutes.find(r => r.name === 'SubprocessoMapa');

    if (route && typeof route.props === 'function') {
      const props = route.props({
        params: { codProcesso: '456', siglaUnidade: 'ABC' }
      } as any);
      expect(props).toEqual({ codProcesso: 456, sigla: 'ABC' });
    } else {
        throw new Error('Props should be a function');
    }
  });

  it('should handle route props correctly for SubprocessoVisMapa', () => {
    const route = processoRoutes.find(r => r.name === 'SubprocessoVisMapa');
    if (route && typeof route.props === 'function') {
      const props = route.props({
        params: { codProcesso: '789', siglaUnidade: 'XYZ' }
      } as any);
      expect(props).toEqual({ codProcesso: 789, sigla: 'XYZ' });
    } else {
        throw new Error('Props should be a function');
    }
  });

  it('should handle route props correctly for SubprocessoCadastro', () => {
    const route = processoRoutes.find(r => r.name === 'SubprocessoCadastro');
    if (route && typeof route.props === 'function') {
      const props = route.props({
        params: { codProcesso: '101', siglaUnidade: 'CAD' }
      } as any);
      expect(props).toEqual({ codProcesso: 101, sigla: 'CAD' });
    } else {
        throw new Error('Props should be a function');
    }
  });

  it('should handle route props correctly for SubprocessoVisCadastro', () => {
    const route = processoRoutes.find(r => r.name === 'SubprocessoVisCadastro');
    if (route && typeof route.props === 'function') {
      const props = route.props({
        params: { codProcesso: '102', siglaUnidade: 'VIS' }
      } as any);
      expect(props).toEqual({ codProcesso: 102, sigla: 'VIS' });
    } else {
        throw new Error('Props should be a function');
    }
  });

  it('should lazy load components', async () => {
    const cadProcesso = processoRoutes.find(r => r.name === 'CadProcesso');
    // @ts-expect-error - testing internal property
    const component = await cadProcesso?.component();
    expect(component.default).toBeDefined();
  });
});
