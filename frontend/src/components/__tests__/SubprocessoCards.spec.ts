import {describe, expect, it, vi} from 'vitest';
import {mount} from '@vue/test-utils';
import {ref} from 'vue';
import SubprocessoCards from '@/components/processo/SubprocessoCards.vue';
import {TipoProcesso} from '@/types/tipos';
import {createTestingPinia} from '@pinia/testing';
import * as useAcessoModule from '@/composables/useAcesso';

const pushMock = vi.fn();
vi.mock('vue-router', () => ({
    useRouter: () => ({push: pushMock})
}));

const processosMock = {
    processoDetalhe: ref<any>(null),
};

vi.mock('@/composables/useProcessos', () => ({
    useProcessos: () => processosMock,
}));

describe('SubprocessoCards.vue', () => {
    const defaultProps = {
        tipoProcesso: TipoProcesso.MAPEAMENTO,
        mapa: {codigo: 1} as any,
        codSubprocesso: 100,
        codProcesso: 1,
        siglaUnidade: 'TESTE'
    };

    const mountComponent = (propsOverrides: any = {}, accessOverrides: any = {}) => {
        processosMock.processoDetalhe.value = {
            situacao: accessOverrides.situacaoProcesso ?? 'EM_ANDAMENTO',
        };

        vi.spyOn(useAcessoModule, 'useAcesso').mockReturnValue({
            podeEditarMapa: ref(accessOverrides.podeEditarMapa ?? true),
            podeEditarCadastro: ref(accessOverrides.podeEditarCadastro ?? true),
            habilitarAcessoMapa: ref(accessOverrides.habilitarAcessoMapa ?? true),
            habilitarAcessoCadastro: ref(accessOverrides.habilitarAcessoCadastro ?? true),
            mesmaUnidade: ref(accessOverrides.mesmaUnidade ?? true),
            podeVisualizarMapa: ref(accessOverrides.podeVisualizarMapa ?? true),
            podeVisualizarDiagnostico: ref(accessOverrides.podeVisualizarDiagnostico ?? false),
            podeVerPagina: ref(accessOverrides.podeVerPagina ?? true),
        } as any);

        return mount(SubprocessoCards, {
            props: {...defaultProps, ...propsOverrides},
            global: {
                plugins: [
                    createTestingPinia({
                        createSpy: vi.fn,
                        initialState: {
                            subprocessos: {
                                subprocessoDetalhe: {
                                    codigo: 100,
                                    permissoes: {
                                        habilitarAcessoCadastro: true,
                                        habilitarAcessoMapa: true
                                    }
                                }
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
            params: {codProcesso: 1, siglaUnidade: 'TESTE'}
        });

        // Keydown enter action
        pushMock.mockClear();
        await wrapper.find('[data-testid="card-subprocesso-atividades"]').trigger('keydown', {key: 'Enter'});
        expect(pushMock).toHaveBeenCalledWith({
            name: 'SubprocessoCadastro',
            params: {codProcesso: 1, siglaUnidade: 'TESTE'}
        });

        // Keydown space action
        pushMock.mockClear();
        await wrapper.find('[data-testid="card-subprocesso-atividades"]').trigger('keydown', {key: ' '});
        expect(pushMock).toHaveBeenCalledWith({
            name: 'SubprocessoCadastro',
            params: {codProcesso: 1, siglaUnidade: 'TESTE'}
        });

        // Mapa card actions
        // Click
        pushMock.mockClear();
        await wrapper.find('[data-testid="card-subprocesso-mapa-edicao"]').trigger('click');
        expect(pushMock).toHaveBeenCalledWith({
            name: 'SubprocessoMapa',
            params: {codProcesso: 1, siglaUnidade: 'TESTE'}
        });

        // Enter
        pushMock.mockClear();
        await wrapper.find('[data-testid="card-subprocesso-mapa-edicao"]').trigger('keydown', {key: 'Enter'});
        expect(pushMock).toHaveBeenCalledWith({
            name: 'SubprocessoMapa',
            params: {codProcesso: 1, siglaUnidade: 'TESTE'}
        });

        // Space
        pushMock.mockClear();
        await wrapper.find('[data-testid="card-subprocesso-mapa-edicao"]').trigger('keydown', {key: ' '});
        expect(pushMock).toHaveBeenCalledWith({
            name: 'SubprocessoMapa',
            params: {codProcesso: 1, siglaUnidade: 'TESTE'}
        });
    });

    it('renderiza cards de visualização se não puder editar', async () => {
        const wrapper = mountComponent({}, {podeEditarMapa: false, podeEditarCadastro: false, podeVerPagina: true});

        expect(wrapper.find('[data-testid="card-subprocesso-atividades-vis"]').exists()).toBe(true);

        // Atividades vis actions
        await wrapper.find('[data-testid="card-subprocesso-atividades-vis"]').trigger('click');
        expect(pushMock).toHaveBeenCalledWith({
            name: 'SubprocessoVisCadastro',
            params: {codProcesso: 1, siglaUnidade: 'TESTE'}
        });

        pushMock.mockClear();
        await wrapper.find('[data-testid="card-subprocesso-atividades-vis"]').trigger('keydown', {key: 'Enter'});
        expect(pushMock).toHaveBeenCalledWith({
            name: 'SubprocessoVisCadastro',
            params: {codProcesso: 1, siglaUnidade: 'TESTE'}
        });

        pushMock.mockClear();
        await wrapper.find('[data-testid="card-subprocesso-atividades-vis"]').trigger('keydown', {key: ' '});
        expect(pushMock).toHaveBeenCalledWith({
            name: 'SubprocessoVisCadastro',
            params: {codProcesso: 1, siglaUnidade: 'TESTE'}
        });

        // Mapa vis Actions
        pushMock.mockClear();
        await wrapper.find('[data-testid="card-subprocesso-mapa-visualizacao"]').trigger('click');
        expect(pushMock).toHaveBeenCalledWith({
            name: 'SubprocessoVisMapa',
            params: {codProcesso: 1, siglaUnidade: 'TESTE'}
        });

        pushMock.mockClear();
        await wrapper.find('[data-testid="card-subprocesso-mapa-visualizacao"]').trigger('keydown', {key: 'Enter'});
        expect(pushMock).toHaveBeenCalledWith({
            name: 'SubprocessoVisMapa',
            params: {codProcesso: 1, siglaUnidade: 'TESTE'}
        });

        pushMock.mockClear();
        await wrapper.find('[data-testid="card-subprocesso-mapa-visualizacao"]').trigger('keydown', {key: ' '});
        expect(pushMock).toHaveBeenCalledWith({
            name: 'SubprocessoVisMapa',
            params: {codProcesso: 1, siglaUnidade: 'TESTE'}
        });
    });

    it('renderiza card de mapa desabilitado quando acesso ao mapa não está habilitado', async () => {
        pushMock.mockClear();
        const wrapper = mountComponent({}, {podeEditarMapa: false, habilitarAcessoMapa: false});

        expect(wrapper.find('[data-testid="card-subprocesso-mapa-desabilitado"]').exists()).toBe(true);
        expect(wrapper.find('[data-testid="card-subprocesso-mapa-edicao"]').exists()).toBe(false);
        expect(wrapper.find('[data-testid="card-subprocesso-mapa-visualizacao"]').exists()).toBe(false);

        // Card desabilitado não deve navegar ao clicar
        await wrapper.find('[data-testid="card-subprocesso-mapa-desabilitado"]').trigger('click');
        expect(pushMock).not.toHaveBeenCalled();
    });

    it('renderiza cards de diagnostico e navega corretamente', async () => {
        const wrapper = mountComponent({
            tipoProcesso: TipoProcesso.DIAGNOSTICO,
            codSubprocesso: 100
        }, {podeVisualizarDiagnostico: true});

        // Diagnostico card actions
        await wrapper.find('[data-testid="card-subprocesso-diagnostico"]').trigger('click');
        expect(pushMock).toHaveBeenCalledWith({
            name: 'AutoavaliacaoDiagnostico',
            params: {codSubprocesso: 100, siglaUnidade: 'TESTE'}
        });

        pushMock.mockClear();
        await wrapper.find('[data-testid="card-subprocesso-diagnostico"]').trigger('keydown', {key: 'Enter'});
        expect(pushMock).toHaveBeenCalledWith({
            name: 'AutoavaliacaoDiagnostico',
            params: {codSubprocesso: 100, siglaUnidade: 'TESTE'}
        });

        pushMock.mockClear();
        await wrapper.find('[data-testid="card-subprocesso-diagnostico"]').trigger('keydown', {key: ' '});
        expect(pushMock).toHaveBeenCalledWith({
            name: 'AutoavaliacaoDiagnostico',
            params: {codSubprocesso: 100, siglaUnidade: 'TESTE'}
        });

        // Ocupacoes card actions
        pushMock.mockClear();
        await wrapper.find('[data-testid="card-subprocesso-ocupacoes"]').trigger('click');
        expect(pushMock).toHaveBeenCalledWith({
            name: 'OcupacoesCriticasDiagnostico',
            params: {codSubprocesso: 100, siglaUnidade: 'TESTE'}
        });

        pushMock.mockClear();
        await wrapper.find('[data-testid="card-subprocesso-ocupacoes"]').trigger('keydown', {key: 'Enter'});
        expect(pushMock).toHaveBeenCalledWith({
            name: 'OcupacoesCriticasDiagnostico',
            params: {codSubprocesso: 100, siglaUnidade: 'TESTE'}
        });

        pushMock.mockClear();
        await wrapper.find('[data-testid="card-subprocesso-ocupacoes"]').trigger('keydown', {key: ' '});
        expect(pushMock).toHaveBeenCalledWith({
            name: 'OcupacoesCriticasDiagnostico',
            params: {codSubprocesso: 100, siglaUnidade: 'TESTE'}
        });

        // Monitoramento card actions
        pushMock.mockClear();
        await wrapper.find('[data-testid="card-subprocesso-monitoramento"]').trigger('click');
        expect(pushMock).toHaveBeenCalledWith({
            name: 'MonitoramentoDiagnostico',
            params: {codSubprocesso: 100, siglaUnidade: 'TESTE'}
        });

        pushMock.mockClear();
        await wrapper.find('[data-testid="card-subprocesso-monitoramento"]').trigger('keydown', {key: 'Enter'});
        expect(pushMock).toHaveBeenCalledWith({
            name: 'MonitoramentoDiagnostico',
            params: {codSubprocesso: 100, siglaUnidade: 'TESTE'}
        });

        pushMock.mockClear();
        await wrapper.find('[data-testid="card-subprocesso-monitoramento"]').trigger('keydown', {key: ' '});
        expect(pushMock).toHaveBeenCalledWith({
            name: 'MonitoramentoDiagnostico',
            params: {codSubprocesso: 100, siglaUnidade: 'TESTE'}
        });
    });

});
