import {describe, expect, it, vi} from 'vitest';
import {mount} from '@vue/test-utils';
import SubprocessoCards from '@/components/SubprocessoCards.vue';
import {TipoProcesso} from '@/types/tipos';
import {checkA11y} from "@/test-utils/a11yTestHelpers";

const pushMock = vi.fn();
vi.mock('vue-router', () => ({
    useRouter: () => ({ push: pushMock })
}));

describe('SubprocessoCards.vue', () => {
    const defaultProps = {
        tipoProcesso: TipoProcesso.MAPEAMENTO,
        mapa: { codigo: 1 } as any,
        permissoes: {
            podeEditarMapa: true,
            podeVisualizarMapa: true,
            podeVisualizarDiagnostico: false,
            podeVerPagina: true,
            podeDisponibilizarCadastro: false,
            podeDevolverCadastro: false,
            podeAceitarCadastro: false,
            podeVisualizarImpacto: false,
            podeAlterarDataLimite: false,
            podeRealizarAutoavaliacao: false,
            podeDisponibilizarMapa: false
        },
        codSubprocesso: 100,
        codProcesso: 1,
        siglaUnidade: 'TESTE'
    };

    it('renderiza cards de edição para MAPEAMENTO com permissão', async () => {
        const wrapper = mount(SubprocessoCards, {
            props: defaultProps,
            global: {
                stubs: {
                    BCard: { template: '<div class="card" @click="$emit(\'click\')" @keydown.enter.prevent="$emit(\'keydown.enter.prevent\')" @keydown.space.prevent="$emit(\'keydown.space.prevent\')"><slot /></div>' },
                    BCardTitle: { template: '<div><slot /></div>' },
                    BCardText: { template: '<div><slot /></div>' },
                    BRow: { template: '<div><slot /></div>' },
                    BCol: { template: '<div><slot /></div>' }
                }
            }
        });

        expect(wrapper.find('[data-testid="card-subprocesso-atividades"]').exists()).toBe(true);
        expect(wrapper.find('[data-testid="card-subprocesso-mapa"]').exists()).toBe(true);

        // Click action
        await wrapper.find('[data-testid="card-subprocesso-atividades"]').trigger('click');
        expect(pushMock).toHaveBeenCalledWith({
            name: 'SubprocessoCadastro',
            params: { codProcesso: 1, siglaUnidade: 'TESTE' }
        });

        // Keydown Enter action
        pushMock.mockClear();
        await wrapper.find('[data-testid="card-subprocesso-atividades"]').trigger('keydown.enter');
        expect(pushMock).toHaveBeenCalledWith({
            name: 'SubprocessoCadastro',
            params: { codProcesso: 1, siglaUnidade: 'TESTE' }
        });

        // Keydown Space action
        pushMock.mockClear();
        await wrapper.find('[data-testid="card-subprocesso-atividades"]').trigger('keydown.space');
        expect(pushMock).toHaveBeenCalledWith({
            name: 'SubprocessoCadastro',
            params: { codProcesso: 1, siglaUnidade: 'TESTE' }
        });
    });

    it('renderiza cards de visualização se não puder editar', async () => {
        const wrapper = mount(SubprocessoCards, {
            props: {
                ...defaultProps,
                permissoes: { ...defaultProps.permissoes, podeEditarMapa: false }
            },
            global: {
                stubs: {
                    BCard: { template: '<div class="card" @click="$emit(\'click\')" @keydown.enter.prevent="$emit(\'keydown.enter.prevent\')" @keydown.space.prevent="$emit(\'keydown.space.prevent\')"><slot /></div>' },
                    BCardTitle: { template: '<div><slot /></div>' },
                    BCardText: { template: '<div><slot /></div>' },
                    BRow: { template: '<div><slot /></div>' },
                    BCol: { template: '<div><slot /></div>' }
                }
            }
        });

        expect(wrapper.find('[data-testid="card-subprocesso-atividades-vis"]').exists()).toBe(true);

        await wrapper.find('[data-testid="card-subprocesso-atividades-vis"]').trigger('click');
        expect(pushMock).toHaveBeenCalledWith({
            name: 'SubprocessoVisCadastro',
            params: { codProcesso: 1, siglaUnidade: 'TESTE' }
        });

         // Mapa vis click
        pushMock.mockClear();
        await wrapper.find('[data-testid="card-subprocesso-mapa"]').trigger('click');
        expect(pushMock).toHaveBeenCalledWith({
            name: 'SubprocessoVisMapa',
            params: { codProcesso: 1, siglaUnidade: 'TESTE' }
        });

        // Mapa vis keydown
        pushMock.mockClear();
        await wrapper.find('[data-testid="card-subprocesso-mapa"]').trigger('keydown.enter');
        expect(pushMock).toHaveBeenCalledWith({
            name: 'SubprocessoVisMapa',
            params: { codProcesso: 1, siglaUnidade: 'TESTE' }
        });
    });

    it('renderiza cards de diagnostico e navega corretamente', async () => {
        const wrapper = mount(SubprocessoCards, {
            props: {
                ...defaultProps,
                tipoProcesso: TipoProcesso.DIAGNOSTICO,
                permissoes: { ...defaultProps.permissoes, podeVisualizarDiagnostico: true }
            },
            global: {
                stubs: {
                    BCard: { template: '<div class="card" @click="$emit(\'click\')" @keydown.enter.prevent="$emit(\'keydown.enter.prevent\')" @keydown.space.prevent="$emit(\'keydown.space.prevent\')"><slot /></div>' },
                    BCardTitle: { template: '<div><slot /></div>' },
                    BCardText: { template: '<div><slot /></div>' },
                    BRow: { template: '<div><slot /></div>' },
                    BCol: { template: '<div><slot /></div>' }
                }
            }
        });

        // Click
        await wrapper.find('[data-testid="card-subprocesso-diagnostico"]').trigger('click');
        expect(pushMock).toHaveBeenCalledWith({
            name: 'AutoavaliacaoDiagnostico',
            params: { codSubprocesso: 100, siglaUnidade: 'TESTE' }
        });

        // Keydown Enter
        pushMock.mockClear();
        await wrapper.find('[data-testid="card-subprocesso-ocupacoes"]').trigger('keydown.enter');
        expect(pushMock).toHaveBeenCalledWith({
            name: 'OcupacoesCriticasDiagnostico',
            params: { codSubprocesso: 100, siglaUnidade: 'TESTE' }
        });

        // Keydown Space
        pushMock.mockClear();
        await wrapper.find('[data-testid="card-subprocesso-monitoramento"]').trigger('keydown.space');
        expect(pushMock).toHaveBeenCalledWith({
            name: 'MonitoramentoDiagnostico',
            params: { codSubprocesso: 100, siglaUnidade: 'TESTE' }
        });
    });

    it('trata estado desabilitado (sem mapa) corretamente', async () => {
        const wrapper = mount(SubprocessoCards, {
            props: {
                ...defaultProps,
                mapa: null
            },
            global: {
                stubs: {
                    BCard: {
                        template: '<div class="card" @click="$emit(\'click\')" @keydown.enter.prevent="$emit(\'keydown.enter.prevent\')" @keydown.space.prevent="$emit(\'keydown.space.prevent\')"><slot /></div>'
                    },
                    BCardTitle: { template: '<div><slot /></div>' },
                    BCardText: { template: '<div><slot /></div>' },
                    BRow: { template: '<div><slot /></div>' },
                    BCol: { template: '<div><slot /></div>' }
                }
            }
        });

        const card = wrapper.find('[data-testid="card-subprocesso-mapa"]');
        expect(card.exists()).toBe(true);
        expect(card.classes()).toContain('disabled-card');
        expect(card.attributes('aria-disabled')).toBe('true');
        expect(card.attributes('tabindex')).toBe('-1');

        pushMock.mockClear();
        await card.trigger('click');
        expect(pushMock).not.toHaveBeenCalled();

        await card.trigger('keydown.enter');
        expect(pushMock).not.toHaveBeenCalled();

        await card.trigger('keydown.space');
        expect(pushMock).not.toHaveBeenCalled();
    });

    it('trata estado desabilitado (sem mapa) em modo visualização', async () => {
        const wrapper = mount(SubprocessoCards, {
            props: {
                ...defaultProps,
                permissoes: { ...defaultProps.permissoes, podeEditarMapa: false },
                mapa: null
            },
            global: {
                stubs: {
                    BCard: {
                        template: '<div class="card" @click="$emit(\'click\')" @keydown.enter.prevent="$emit(\'keydown.enter.prevent\')"><slot /></div>'
                    },
                    BCardTitle: { template: '<div><slot /></div>' },
                    BCardText: { template: '<div><slot /></div>' },
                    BRow: { template: '<div><slot /></div>' },
                    BCol: { template: '<div><slot /></div>' }
                }
            }
        });

        const card = wrapper.find('[data-testid="card-subprocesso-mapa"]');
        expect(card.classes()).toContain('disabled-card');

        pushMock.mockClear();
        await card.trigger('click');
        expect(pushMock).not.toHaveBeenCalled();

        await card.trigger('keydown.enter');
        expect(pushMock).not.toHaveBeenCalled();
    });

    it('deve ser acessível', async () => {
        const wrapper = mount(SubprocessoCards, {
            props: defaultProps,
            global: {
                stubs: {
                    BCard: { template: '<div class="card"><slot /></div>' },
                    BCardTitle: { template: '<div><slot /></div>' },
                    BCardText: { template: '<div><slot /></div>' },
                    BRow: { template: '<div><slot /></div>' },
                    BCol: { template: '<div><slot /></div>' }
                }
            }
        });
        await checkA11y(wrapper.element as HTMLElement);
    });
});
