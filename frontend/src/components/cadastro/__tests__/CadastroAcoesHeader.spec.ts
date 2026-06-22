import {describe, expect, it} from 'vitest'
import {mount} from '@vue/test-utils'
import CadastroAcoesHeader from '../CadastroAcoesHeader.vue'
import {TEXTOS} from "@/constants/textos"
import {PERMISSOES_SUBPROCESSO_VAZIAS} from "@/utils/permissoesSubprocesso";

const permissoesBase = {
    ...PERMISSOES_SUBPROCESSO_VAZIAS,
    podeEditarCadastro: true,
    podeDisponibilizarCadastro: true,
    podeDevolverCadastro: true,
    podeAceitarCadastro: true,
    podeHomologarCadastro: true,
    podeEditarMapa: true,
    podeDisponibilizarMapa: true,
    podeValidarMapa: true,
    podeApresentarSugestoes: true,
    podeVerSugestoes: true,
    podeDevolverMapa: true,
    podeAceitarMapa: true,
    podeHomologarMapa: true,
    podeVisualizarImpacto: true,
    podeAlterarDataLimite: true,
    podeReabrirCadastro: true,
    podeReabrirRevisao: true,
    podeEnviarLembrete: true,
    mesmaUnidade: true,
    habilitarAcessoCadastro: true,
    habilitarAcessoMapa: true,
    habilitarEditarCadastro: true,
    habilitarDisponibilizarCadastro: true,
    habilitarDevolverCadastro: true,
    habilitarAceitarCadastro: true,
    habilitarHomologarCadastro: true,
    habilitarEditarMapa: true,
    habilitarDisponibilizarMapa: true,
    habilitarValidarMapa: true,
    habilitarApresentarSugestoes: true,
    habilitarDevolverMapa: true,
    habilitarAceitarMapa: true,
    habilitarHomologarMapa: true,
    habilitarAlterarDataLimite: true,
    habilitarReabrirCadastro: true,
    habilitarReabrirRevisao: true,
    habilitarEnviarLembrete: true,
}

const propsPadrao = {
    codSubprocesso: 123,
    permissoes: permissoesBase,
    unidade: {codigo: 1, sigla: 'TEST', nome: 'Unidade Teste'}
}

describe('CadastroAcoesHeader.vue', () => {
    it('deve renderizar o título e a sigla da unidade', () => {
        const wrapper = mount(CadastroAcoesHeader, {
            props: propsPadrao
        })
        expect(wrapper.text()).toContain(TEXTOS.atividades.TITULO)
        expect(wrapper.find('[data-testid="subprocesso-header__txt-header-unidade"]').text()).toBe('TEST')
    })

    it('deve emitir "abrir-historico" ao clicar no botão de histórico', async () => {
        const wrapper = mount(CadastroAcoesHeader, {
            props: propsPadrao
        })
        const btn = wrapper.find('[data-testid="btn-cad-atividades-historico"]')
        await btn.trigger('click')
        expect(wrapper.emitted('abrir-historico')).toBeDefined()
    })

    describe('Lógica de Ações Únicas vs Dropdown', () => {
        it('deve mostrar botão direto se houver apenas uma ação de workflow (Devolver)', () => {
            const wrapper = mount(CadastroAcoesHeader, {
                props: {
                    ...propsPadrao,
                    mostrarDevolverCadastro: true,
                    acaoPrincipalCadastro: null,
                    mostrarDisponibilizarCadastro: false
                }
            })
            expect(wrapper.find('[data-testid="btn-acao-devolver"]').exists()).toBe(true)
            expect(wrapper.find('[data-testid="btn-cadastro-acoes"]').exists()).toBe(false)
        })

        it('deve mostrar botão direto se houver apenas uma ação de workflow (Principal)', () => {
            const wrapper = mount(CadastroAcoesHeader, {
                props: {
                    ...propsPadrao,
                    mostrarDevolverCadastro: false,
                    acaoPrincipalCadastro: {
                        mostrar: true,
                        habilitar: true,
                        rotuloBotao: 'Validar'
                    },
                    mostrarDisponibilizarCadastro: false
                }
            })
            expect(wrapper.find('[data-testid="btn-acao-analisar-principal"]').exists()).toBe(true)
            expect(wrapper.find('[data-testid="btn-acao-analisar-principal"]').text()).toBe('Validar')
        })

        it('deve usar dropdown se houver mais de uma ação de workflow', () => {
            const wrapper = mount(CadastroAcoesHeader, {
                props: {
                    ...propsPadrao,
                    mostrarDevolverCadastro: true,
                    acaoPrincipalCadastro: {
                        mostrar: true,
                        habilitar: true,
                        rotuloBotao: 'Validar'
                    }
                }
            })
            expect(wrapper.find('[data-testid="btn-cadastro-acoes"]').exists()).toBe(true)
        })
    })

    describe('Interações e Permissões', () => {
        it('deve desabilitar botão de devolver se não houver permissão', () => {
            const wrapper = mount(CadastroAcoesHeader, {
                props: {
                    ...propsPadrao,
                    mostrarDevolverCadastro: true,
                    permissoes: {...permissoesBase, habilitarDevolverCadastro: false}
                }
            })
            expect((wrapper.find('[data-testid="btn-acao-devolver"]').element as HTMLButtonElement).disabled).toBe(true)
        })

        it('deve desabilitar botão principal se a ação estiver desabilitada no objeto', () => {
            const wrapper = mount(CadastroAcoesHeader, {
                props: {
                    ...propsPadrao,
                    acaoPrincipalCadastro: {
                        mostrar: true,
                        habilitar: false,
                        rotuloBotao: 'Validar'
                    }
                }
            })
            expect((wrapper.find('[data-testid="btn-acao-analisar-principal"]').element as HTMLButtonElement).disabled).toBe(true)
        })

        it('deve mostrar e habilitar botão de impacto se permitido', async () => {
            const wrapper = mount(CadastroAcoesHeader, {
                props: {
                    ...propsPadrao,
                    podeVisualizarImpacto: true
                }
            })
            const btn = wrapper.find('[data-testid="cad-atividades__btn-impactos-mapa-edicao"]')
            expect(btn.exists()).toBe(true)
            await btn.trigger('click')
            expect(wrapper.emitted('abrir-impacto')).toBeDefined()
        })

        it('deve mostrar e habilitar botão de importar se permitido', async () => {
            const wrapper = mount(CadastroAcoesHeader, {
                props: {
                    ...propsPadrao,
                    mostrarImportarAtividades: true,
                    permissoes: {...permissoesBase, habilitarEditarCadastro: true}
                }
            })
            const btn = wrapper.find('[data-testid="btn-cad-atividades-importar"]')
            expect(btn.exists()).toBe(true)
            await btn.trigger('click')
            expect(wrapper.emitted('abrir-importar')).toBeDefined()
        })

        it('deve desabilitar botão de importar se não puder editar cadastro', () => {
            const wrapper = mount(CadastroAcoesHeader, {
                props: {
                    ...propsPadrao,
                    mostrarImportarAtividades: true,
                    permissoes: {...permissoesBase, habilitarEditarCadastro: false}
                }
            })
            expect((wrapper.find('[data-testid="btn-cad-atividades-importar"]').element as HTMLButtonElement).disabled).toBe(true)
        })
    })

    describe('Ações de Disponibilização', () => {
        it('deve emitir "disponibilizar" ao clicar no botão de disponibilizar direto', async () => {
            const wrapper = mount(CadastroAcoesHeader, {
                props: {
                    ...propsPadrao,
                    mostrarDisponibilizarCadastro: true
                }
            })
            const btn = wrapper.find('[data-testid="btn-cad-atividades-disponibilizar"]')
            await btn.trigger('click')
            expect(wrapper.emitted('disponibilizar')).toBeDefined()
        })

        it('deve mostrar estado de loading no botão de disponibilizar', () => {
            const wrapper = mount(CadastroAcoesHeader, {
                props: {
                    ...propsPadrao,
                    mostrarDisponibilizarCadastro: true,
                    loadingValidacao: true
                }
            })
            const btn = wrapper.findComponent({name: 'LoadingButton'})
            expect(btn.props('loading')).toBe(true)
            expect(btn.props('disabled')).toBe(true)
        })
    })
})
