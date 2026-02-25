import {describe, expect, it, vi} from 'vitest';
import {mount} from '@vue/test-utils';
import { ref } from 'vue';
import SubprocessoCards from '@/components/processo/SubprocessoCards.vue';
import {TipoProcesso} from '@/types/tipos';
import {createTestingPinia} from '@pinia/testing';
import * as useAcessoModule from '@/composables/useAcesso';

const pushMock = vi.fn();
vi.mock('vue-router', () => ({
    useRouter: () => ({ push: pushMock })
}));

describe('SubprocessoCards.vue', () => {
    const defaultProps = {
        tipoProcesso: TipoProcesso.MAPEAMENTO,
        mapa: { codigo: 1 } as any,
        codSubprocesso: 100,
        codProcesso: 1,
        siglaUnidade: 'TESTE'
    };

    const mountComponent = (propsOverrides: any = {}, accessOverrides: any = {}) => {
        vi.spyOn(useAcessoModule, 'useAcesso').mockReturnValue({
            podeEditarMapa: ref(accessOverrides.podeEditarMapa ?? true),
            podeEditarCadastro: ref(accessOverrides.podeEditarCadastro ?? true),
            podeVisualizarMapa: ref(accessOverrides.podeVisualizarMapa ?? true),
            podeVisualizarDiagnostico: ref(accessOverrides.podeVisualizarDiagnostico ?? false),
            podeVerPagina: ref(accessOverrides.podeVerPagina ?? true),
        } as any);

        return mount(SubprocessoCards, {
            props: { ...defaultProps, ...propsOverrides },
            global: {
                plugins: [
                    createTestingPinia({
                        createSpy: vi.fn,
                        initialState: {
                            subprocessos: {
                                subprocessoDetalhe: { codigo: 100 }
                            }
                        }
                    })
                ],
                stubs: {}
            }
        });
    };

    it('renderiza cards de edição para MAPEAMENTO com permissão', async () => {
        const wrapper = mountComponent();

        expect(wrapper.find('[data-testid="card-subprocesso-atividades"]').exists()).toBe(true);
        expect(wrapper.find('[data-testid="card-subprocesso-mapa-edicao"]').exists()).toBe(true);

        // Click action
        await wrapper.find('[data-testid="card-subprocesso-atividades"]').trigger('click');
        expect(pushMock).toHaveBeenCalledWith({
            name: 'SubprocessoCadastro',
            params: { codProcesso: 1, siglaUnidade: 'TESTE' }
        });

        // Keydown Enter action
        pushMock.mockClear();
        await wrapper.find('[data-testid="card-subprocesso-atividades"]').trigger('keydown', { key: 'Enter' });
        expect(pushMock).toHaveBeenCalledWith({
            name: 'SubprocessoCadastro',
            params: { codProcesso: 1, siglaUnidade: 'TESTE' }
        });

        // Keydown Space action
        pushMock.mockClear();
        await wrapper.find('[data-testid="card-subprocesso-atividades"]').trigger('keydown', { key: ' ' });
        expect(pushMock).toHaveBeenCalledWith({
            name: 'SubprocessoCadastro',
            params: { codProcesso: 1, siglaUnidade: 'TESTE' }
        });

        // Mapa Card Actions
        // Click
        pushMock.mockClear();
        await wrapper.find('[data-testid="card-subprocesso-mapa-edicao"]').trigger('click');
        expect(pushMock).toHaveBeenCalledWith({
            name: 'SubprocessoMapa',
            params: { codProcesso: 1, siglaUnidade: 'TESTE' }
        });

        // Enter
        pushMock.mockClear();
        await wrapper.find('[data-testid="card-subprocesso-mapa-edicao"]').trigger('keydown', { key: 'Enter' });
        expect(pushMock).toHaveBeenCalledWith({
            name: 'SubprocessoMapa',
            params: { codProcesso: 1, siglaUnidade: 'TESTE' }
        });

        // Space
        pushMock.mockClear();
        await wrapper.find('[data-testid="card-subprocesso-mapa-edicao"]').trigger('keydown', { key: ' ' });
        expect(pushMock).toHaveBeenCalledWith({
            name: 'SubprocessoMapa',
            params: { codProcesso: 1, siglaUnidade: 'TESTE' }
        });
    });

    it('renderiza cards de visualização se não puder editar', async () => {
        const wrapper = mountComponent({}, { podeEditarMapa: false, podeEditarCadastro: false, podeVerPagina: true });

        expect(wrapper.find('[data-testid="card-subprocesso-atividades-vis"]').exists()).toBe(true);

        // Atividades Vis Actions
        await wrapper.find('[data-testid="card-subprocesso-atividades-vis"]').trigger('click');
        expect(pushMock).toHaveBeenCalledWith({
            name: 'SubprocessoVisCadastro',
            params: { codProcesso: 1, siglaUnidade: 'TESTE' }
        });

        pushMock.mockClear();
        await wrapper.find('[data-testid="card-subprocesso-atividades-vis"]').trigger('keydown', { key: 'Enter' });
        expect(pushMock).toHaveBeenCalledWith({
            name: 'SubprocessoVisCadastro',
            params: { codProcesso: 1, siglaUnidade: 'TESTE' }
        });

        pushMock.mockClear();
        await wrapper.find('[data-testid="card-subprocesso-atividades-vis"]').trigger('keydown', { key: ' ' });
        expect(pushMock).toHaveBeenCalledWith({
            name: 'SubprocessoVisCadastro',
            params: { codProcesso: 1, siglaUnidade: 'TESTE' }
        });

         // Mapa vis Actions
        pushMock.mockClear();
        await wrapper.find('[data-testid="card-subprocesso-mapa-visualizacao"]').trigger('click');
        expect(pushMock).toHaveBeenCalledWith({
            name: 'SubprocessoVisMapa',
            params: { codProcesso: 1, siglaUnidade: 'TESTE' }
        });

        pushMock.mockClear();
        await wrapper.find('[data-testid="card-subprocesso-mapa-visualizacao"]').trigger('keydown', { key: 'Enter' });
        expect(pushMock).toHaveBeenCalledWith({
            name: 'SubprocessoVisMapa',
            params: { codProcesso: 1, siglaUnidade: 'TESTE' }
        });

        pushMock.mockClear();
        await wrapper.find('[data-testid="card-subprocesso-mapa-visualizacao"]').trigger('keydown', { key: ' ' });
        expect(pushMock).toHaveBeenCalledWith({
            name: 'SubprocessoVisMapa',
            params: { codProcesso: 1, siglaUnidade: 'TESTE' }
        });
    });

    it('renderiza cards de diagnostico e navega corretamente', async () => {
        const wrapper = mountComponent({ tipoProcesso: TipoProcesso.DIAGNOSTICO, codSubprocesso: 100 }, { podeVisualizarDiagnostico: true });

        // Diagnostico Card Actions
        await wrapper.find('[data-testid="card-subprocesso-diagnostico"]').trigger('click');
        expect(pushMock).toHaveBeenCalledWith({
            name: 'AutoavaliacaoDiagnostico',
            params: { codSubprocesso: 100, siglaUnidade: 'TESTE' }
        });

        pushMock.mockClear();
        await wrapper.find('[data-testid="card-subprocesso-diagnostico"]').trigger('keydown', { key: 'Enter' });
        expect(pushMock).toHaveBeenCalledWith({
            name: 'AutoavaliacaoDiagnostico',
            params: { codSubprocesso: 100, siglaUnidade: 'TESTE' }
        });

        pushMock.mockClear();
        await wrapper.find('[data-testid="card-subprocesso-diagnostico"]').trigger('keydown', { key: ' ' });
        expect(pushMock).toHaveBeenCalledWith({
            name: 'AutoavaliacaoDiagnostico',
            params: { codSubprocesso: 100, siglaUnidade: 'TESTE' }
        });

        // Ocupacoes Card Actions
        pushMock.mockClear();
        await wrapper.find('[data-testid="card-subprocesso-ocupacoes"]').trigger('click');
        expect(pushMock).toHaveBeenCalledWith({
            name: 'OcupacoesCriticasDiagnostico',
            params: { codSubprocesso: 100, siglaUnidade: 'TESTE' }
        });

        pushMock.mockClear();
        await wrapper.find('[data-testid="card-subprocesso-ocupacoes"]').trigger('keydown', { key: 'Enter' });
        expect(pushMock).toHaveBeenCalledWith({
            name: 'OcupacoesCriticasDiagnostico',
            params: { codSubprocesso: 100, siglaUnidade: 'TESTE' }
        });

        pushMock.mockClear();
        await wrapper.find('[data-testid="card-subprocesso-ocupacoes"]').trigger('keydown', { key: ' ' });
        expect(pushMock).toHaveBeenCalledWith({
            name: 'OcupacoesCriticasDiagnostico',
            params: { codSubprocesso: 100, siglaUnidade: 'TESTE' }
        });

        // Monitoramento Card Actions
        pushMock.mockClear();
        await wrapper.find('[data-testid="card-subprocesso-monitoramento"]').trigger('click');
        expect(pushMock).toHaveBeenCalledWith({
            name: 'MonitoramentoDiagnostico',
            params: { codSubprocesso: 100, siglaUnidade: 'TESTE' }
        });

        pushMock.mockClear();
        await wrapper.find('[data-testid="card-subprocesso-monitoramento"]').trigger('keydown', { key: 'Enter' });
        expect(pushMock).toHaveBeenCalledWith({
            name: 'MonitoramentoDiagnostico',
            params: { codSubprocesso: 100, siglaUnidade: 'TESTE' }
        });

        pushMock.mockClear();
        await wrapper.find('[data-testid="card-subprocesso-monitoramento"]').trigger('keydown', { key: ' ' });
        expect(pushMock).toHaveBeenCalledWith({
            name: 'MonitoramentoDiagnostico',
            params: { codSubprocesso: 100, siglaUnidade: 'TESTE' }
        });
    });

    it('trata estado desabilitado (sem mapa) corretamente', async () => {
        const wrapper = mountComponent({ mapa: null, codSubprocesso: 100 });

        const card = wrapper.find('[data-testid="card-subprocesso-mapa-edicao"]');
        expect(card.exists()).toBe(true);
        expect(card.classes()).toContain('disabled-card');
        expect(card.attributes('aria-disabled')).toBe('true');
        expect(card.attributes('tabindex')).toBe('-1');

        pushMock.mockClear();
        await card.trigger('click');
        expect(pushMock).not.toHaveBeenCalled();

        await card.trigger('keydown', { key: 'Enter' });
        expect(pushMock).not.toHaveBeenCalled();

        await card.trigger('keydown', { key: ' ' });
        expect(pushMock).not.toHaveBeenCalled();
    });

    it('trata estado desabilitado (sem mapa) em modo visualização', async () => {
        const wrapper = mountComponent({ mapa: null }, { podeEditarMapa: false, podeEditarCadastro: false, podeVerPagina: true });

        const card = wrapper.find('[data-testid="card-subprocesso-mapa-visualizacao"]');
        expect(card.classes()).toContain('disabled-card');

        pushMock.mockClear();
        await card.trigger('click');
        expect(pushMock).not.toHaveBeenCalled();

        await card.trigger('keydown', { key: 'Enter' });
        expect(pushMock).not.toHaveBeenCalled();
    });
});
