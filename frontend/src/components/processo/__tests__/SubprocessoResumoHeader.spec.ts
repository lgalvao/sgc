import {describe, expect, it} from 'vitest';
import {mount} from '@vue/test-utils';
import SubprocessoResumoHeader from '../SubprocessoResumoHeader.vue';

describe('SubprocessoResumoHeader.vue', () => {
    const defaultProps = {
        subprocesso: {
            unidade: {sigla: 'SIGLA', nome: 'Nome Unidade'},
            processoDescricao: 'Desc Processo',
            situacao: 'SIT_TEST',
            localizacaoAtual: 'Local Teste',
            prazoEtapaAtual: '2026-01-01',
            titular: {nome: 'Titular', ramal: '123', email: 'titular@test.com'},
            responsavel: null
        } as any,
        siglaUnidadeFallback: 'FALLBACK',
        mostrarAcoesCabecalho: false,
        mostrarAlterarDataLimite: false,
        habilitarAlterarDataLimite: false,
        mostrarReabrirCadastro: false,
        habilitarReabrirCadastro: false,
        mostrarReabrirRevisao: false,
        habilitarReabrirRevisao: false,
        mostrarEnviarLembrete: false,
        habilitarEnviarLembrete: false,
        formatSituacaoSubprocesso: (s: string) => `F_${s}`,
        formatDataSimples: (d: string | null) => d ? `D_${d}` : '',
        formatTipoResponsabilidade: () => 'Substituto'
    };

    const stubs = {
        PageHeader: {
            props: ['title', 'subtitle'],
            template: `<div><h1>{{ title }}</h1><h2>{{ subtitle }}</h2><slot name="actions"></slot></div>`
        },
        BDropdown: {template: `<div><slot></slot></div>`},
        BDropdownItemButton: {template: `<button @click="$emit('click')"><slot></slot></button>`},
        BCard: {template: `<div><slot></slot></div>`},
        BRow: {template: `<div><slot></slot></div>`},
        BCol: {template: `<div><slot></slot></div>`},
    };

    it('renders with minimal props and falls back to siglaUnidadeFallback', () => {
        const props = {
            ...defaultProps,
            subprocesso: {
                ...defaultProps.subprocesso,
                unidade: null,
                prazoEtapaAtual: null,
                titular: null,
            }
        };
        const wrapper = mount(SubprocessoResumoHeader, {props, global: {stubs}});
        expect(wrapper.text()).toContain('FALLBACK');
        expect(wrapper.find('[data-testid="subprocesso-header__txt-prazo"]').exists()).toBe(false);
    });

    it('renders all actions when flags are true', async () => {
        const props = {
            ...defaultProps,
            mostrarAcoesCabecalho: true,
            mostrarAlterarDataLimite: true,
            habilitarAlterarDataLimite: true,
            mostrarReabrirCadastro: true,
            habilitarReabrirCadastro: true,
            mostrarReabrirRevisao: true,
            habilitarReabrirRevisao: true,
            mostrarEnviarLembrete: true,
            habilitarEnviarLembrete: true,
        };
        const wrapper = mount(SubprocessoResumoHeader, {props, global: {stubs}});

        expect(wrapper.find('[data-testid="btn-alterar-data-limite"]').exists()).toBe(true);
        expect(wrapper.find('[data-testid="btn-reabrir-cadastro"]').exists()).toBe(true);
        expect(wrapper.find('[data-testid="btn-reabrir-revisao"]').exists()).toBe(true);
        expect(wrapper.find('[data-testid="btn-enviar-lembrete"]').exists()).toBe(true);

        await wrapper.find('[data-testid="btn-alterar-data-limite"]').trigger('click');
        expect(wrapper.emitted('abrir-alterar-data-limite')).toBeTruthy();

        await wrapper.find('[data-testid="btn-reabrir-cadastro"]').trigger('click');
        expect(wrapper.emitted('abrir-reabrir-cadastro')).toBeTruthy();

        await wrapper.find('[data-testid="btn-reabrir-revisao"]').trigger('click');
        expect(wrapper.emitted('abrir-reabrir-revisao')).toBeTruthy();

        await wrapper.find('[data-testid="btn-enviar-lembrete"]').trigger('click');
        expect(wrapper.emitted('confirmar-enviar-lembrete')).toBeTruthy();
    });

    it('renders titular with all contact info', () => {
        const wrapper = mount(SubprocessoResumoHeader, {props: defaultProps, global: {stubs}});
        expect(wrapper.text()).toContain('123');
        expect(wrapper.text()).toContain('titular@test.com');
    });

    it('renders titular without contact info', () => {
        const props = {
            ...defaultProps,
            subprocesso: {
                ...defaultProps.subprocesso,
                titular: {nome: 'Titular'}
            }
        };
        const wrapper = mount(SubprocessoResumoHeader, {props, global: {stubs}});
        expect(wrapper.text()).toContain('Titular');
        expect(wrapper.text()).not.toContain('123');
    });

    it('renders responsavel differently from titular with all contact info', () => {
        const props = {
            ...defaultProps,
            subprocesso: {
                ...defaultProps.subprocesso,
                responsavel: {
                    tipo: 'SUBSTITUTO',
                    usuario: {nome: 'Resp', ramal: '456', email: 'resp@test.com'}
                }
            }
        };
        const wrapper = mount(SubprocessoResumoHeader, {props, global: {stubs}});
        expect(wrapper.text()).toContain('Resp');
        expect(wrapper.text()).toContain('Substituto');
        expect(wrapper.text()).toContain('456');
        expect(wrapper.text()).toContain('resp@test.com');
    });

    it('renders responsavel without contact info and without tipo', () => {
        const props = {
            ...defaultProps,
            subprocesso: {
                ...defaultProps.subprocesso,
                responsavel: {
                    usuario: {nome: 'Resp'}
                }
            }
        };
        const wrapper = mount(SubprocessoResumoHeader, {props, global: {stubs}});
        expect(wrapper.text()).toContain('Resp');
    });

    it('does not render responsavel if it is the same as titular', () => {
        const props = {
            ...defaultProps,
            subprocesso: {
                ...defaultProps.subprocesso,
                titular: {nome: 'Titular'},
                responsavel: {
                    usuario: {nome: 'Titular'}
                }
            }
        };
        const wrapper = mount(SubprocessoResumoHeader, {props, global: {stubs}});
        const html = wrapper.html();
        // Since both are 'Titular', the responsavel section shouldn't appear
        // The word "Responsável:" should not be present
        expect(html).not.toContain('Responsável:');
    });
});