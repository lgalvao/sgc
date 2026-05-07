import {describe, expect, it} from 'vitest';
import {mount} from '@vue/test-utils';
import CompetenciaEdicaoModal from '../modais/CompetenciaEdicaoModal.vue';
import type {Atividade, Competencia} from '@/types/tipos';

const atividadeBase = (codigo: number, descricao: string): Atividade => ({
    codigo,
    descricao,
    conhecimentos: [],
});

const competenciaBase = (codigo: number, atv: Atividade[] = []): Competencia => ({
    codigo,
    descricao: `Competência ${codigo}`,
    atividades: atv,
});

describe('CompetenciaEdicaoModal.vue', () => {
    describe('modo criação', () => {
        it('botão confirmar exibe texto "Criar" quando não há competencia para editar', () => {
            const wrapper = mount(CompetenciaEdicaoModal, {
                props: {mostrar: true, atividades: [], competenciaParaEditar: null},
            });
            // No modo criação o botão confirmar deve exibir "Criar"
            const btn = wrapper.find('[data-testid="btn-criar-competencia-salvar"]');
            expect(btn.text()).toBe('Criar');
        });

        it('exibe erro de descrição ao tentar salvar sem preencher', async () => {
            const wrapper = mount(CompetenciaEdicaoModal, {
                props: {mostrar: true, atividades: []},
            });
            const btn = wrapper.find('[data-testid="btn-criar-competencia-salvar"]');
            await btn.trigger('click');
            await wrapper.vm.$nextTick();
            expect(wrapper.text()).toContain('A descrição é obrigatória.');
        });

        it('exige ao menos uma atividade ao tentar salvar sem selecionar', async () => {
            const wrapper = mount(CompetenciaEdicaoModal, {
                props: {mostrar: true, atividades: [atividadeBase(1, 'Atv 1')]},
            });
            const textarea = wrapper.find('[data-testid="inp-criar-competencia-descricao"]');
            await textarea.setValue('Minha competência');
            const btn = wrapper.find('[data-testid="btn-criar-competencia-salvar"]');
            await btn.trigger('click');
            await wrapper.vm.$nextTick();
            expect(wrapper.text()).toContain('Selecione ao menos uma atividade.');
        });

        it('permite marcar e desmarcar todas as atividades', async () => {
            const atividades = [atividadeBase(1, 'Atv 1'), atividadeBase(2, 'Atv 2')];
            const wrapper = mount(CompetenciaEdicaoModal, {
                props: {mostrar: true, atividades},
            });

            await wrapper.find('[data-testid="btn-competencia-selecionar-todas-atividades"]').trigger('click');
            await wrapper.find('[data-testid="btn-criar-competencia-salvar"]').trigger('click');

            expect(wrapper.emitted('salvar')).toBeTruthy();
            expect(wrapper.emitted('salvar')![0][0]).toMatchObject({
                atividadesSelecionadas: [1, 2],
            });

            await wrapper.find('[data-testid="btn-competencia-limpar-selecao-atividades"]').trigger('click');
            await wrapper.find('[data-testid="btn-criar-competencia-salvar"]').trigger('click');

            expect(wrapper.text()).toContain('Selecione ao menos uma atividade.');
        });

        it('emite fechar ao clicar em cancelar', async () => {
            const wrapper = mount(CompetenciaEdicaoModal, {
                props: {mostrar: true, atividades: []},
            });
            const btn = wrapper.find('[data-testid="btn-criar-competencia-cancelar"]');
            await btn.trigger('click');
            expect(wrapper.emitted('fechar')).toBeTruthy();
        });

        it('exibe fieldError.generic quando fornecido', () => {
            const wrapper = mount(CompetenciaEdicaoModal, {
                props: {
                    mostrar: true,
                    atividades: [],
                    fieldErrors: {generic: 'Erro de servidor'},
                },
            });
            expect(wrapper.text()).toContain('Erro de servidor');
        });

        it('exibe fieldError.descricao quando fornecido', () => {
            const wrapper = mount(CompetenciaEdicaoModal, {
                props: {
                    mostrar: true,
                    atividades: [],
                    fieldErrors: {descricao: 'Descrição muito longa'},
                },
            });
            expect(wrapper.text()).toContain('Descrição muito longa');
        });
    });

    describe('modo edição', () => {
        it('botão confirmar exibe texto "Salvar" quando há competencia para editar', () => {
            const atv = atividadeBase(1, 'Atv 1');
            const wrapper = mount(CompetenciaEdicaoModal, {
                props: {
                    mostrar: true,
                    atividades: [atv],
                    competenciaParaEditar: competenciaBase(10, [atv]),
                },
            });
            // No modo edição o botão confirmar deve exibir "Salvar"
            const btn = wrapper.find('[data-testid="btn-criar-competencia-salvar"]');
            expect(btn.text()).toBe('Salvar');
        });

        it('pré-preenche a descrição com a competência existente', () => {
            const atv = atividadeBase(1, 'Atv 1');
            const comp = competenciaBase(10, [atv]);
            comp.descricao = 'Competência pré-preenchida';
            const wrapper = mount(CompetenciaEdicaoModal, {
                props: {mostrar: true, atividades: [atv], competenciaParaEditar: comp},
            });
            const textarea = wrapper.find('[data-testid="inp-criar-competencia-descricao"]');
            expect((textarea.element as HTMLTextAreaElement).value).toBe('Competência pré-preenchida');
        });

        it('emite salvar com descricao e atividadesSelecionadas ao confirmar em modo edicao', async () => {
            const atv = atividadeBase(1, 'Atv 1');
            const comp = competenciaBase(10, [atv]);
            const wrapper = mount(CompetenciaEdicaoModal, {
                props: {mostrar: true, atividades: [atv], competenciaParaEditar: comp},
            });
            const btn = wrapper.find('[data-testid="btn-criar-competencia-salvar"]');
            await btn.trigger('click');
            const emitido = wrapper.emitted('salvar');
            expect(emitido).toBeTruthy();
            expect(emitido![0][0]).toMatchObject({
                descricao: comp.descricao,
                atividadesSelecionadas: [atv.codigo],
            });
        });

        it('não emite salvar quando loading é true', async () => {
            const atv = atividadeBase(1, 'Atv 1');
            const comp = competenciaBase(10, [atv]);
            const wrapper = mount(CompetenciaEdicaoModal, {
                props: {mostrar: true, atividades: [atv], competenciaParaEditar: comp, loading: true},
            });
            const btn = wrapper.find('[data-testid="btn-criar-competencia-salvar"]');
            await btn.trigger('click');
            expect(wrapper.emitted('salvar')).toBeFalsy();
        });
    });

    describe('visibilidade do modal', () => {
        it('renderiza o conteúdo quando mostrar é true', () => {
            const wrapper = mount(CompetenciaEdicaoModal, {
                props: {mostrar: true, atividades: []},
            });
            expect(wrapper.find('[data-testid="mdl-criar-competencia"]').exists()).toBe(true);
        });
    });
});
