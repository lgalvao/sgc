import {mount} from "@vue/test-utils";
import {describe, expect, it} from "vitest";
import AtividadeItem from "@/components/atividades/AtividadeItem.vue";
import {Atividade} from "@/types/tipos";
import {getCommonMountOptions, setupComponentTest} from "@/test-utils/componentTestHelpers";

describe("AtividadeItem.vue", () => {
    const context = setupComponentTest();

    const atividadeMock: Atividade = {
        codigo: 1,
        descricao: "Atividade Teste",
        conhecimentos: [
            {codigo: 10, descricao: "Conhecimento 1"}
        ],
    };

    const commonStubs = {
        BButton: {
            template: '<button class="btn" v-bind="$attrs"><slot /></button>'
        },
        BCard: {template: '<div class="card"><slot /></div>'},
        BCardBody: {template: '<div class="card-body"><slot /></div>'},
        BFormInput: {
            template: '<input class="form-control" :value="modelValue" @input="$emit(\'update:modelValue\', $event.target.value)" />',
            props: ['modelValue', 'size']
        },
        BForm: {template: '<form @submit.prevent="$emit(\'submit\', $event)"><slot /></form>'},
        BCol: {template: '<div class="col"><slot /></div>'},
    };


    it("deve entrar em modo de edição de atividade e salvar", async () => {
        const mountOptions = getCommonMountOptions({}, commonStubs);
        context.wrapper = mount(AtividadeItem, {
            ...mountOptions,
            props: {atividade: atividadeMock, podeEditar: true},
        });

        await context.wrapper.find('[data-testid="btn-editar-atividade"]').trigger('click');

        // Check if input appeared (meaning we are in edit mode)
        const input = context.wrapper.find('[data-testid="inp-editar-atividade"]');
        expect(input.exists()).toBe(true);

        await input.setValue('Atividade Editada');

        const saveButton = context.wrapper.find('[data-testid="btn-salvar-edicao-atividade"]');
        expect(saveButton.exists()).toBe(true);
        await saveButton.trigger('click');

        expect(context.wrapper!.emitted('atualizar-atividade')).toBeTruthy();
        expect(context.wrapper!.emitted('atualizar-atividade')![0]).toEqual(['Atividade Editada']);
    });

    it("deve adicionar novo conhecimento", async () => {
        const mountOptions = getCommonMountOptions({}, commonStubs);
        context.wrapper = mount(AtividadeItem, {
            ...mountOptions,
            props: {atividade: atividadeMock, podeEditar: true},
        });

        const input = context.wrapper.find('[data-testid="inp-novo-conhecimento"]');
        expect(input.exists()).toBe(true);
        await input.setValue('Novo Conhecimento');

        // Trigger submit on the form directly
        await context.wrapper.find('[data-testid="form-novo-conhecimento"]').trigger('submit');

        expect(context.wrapper!.emitted('adicionar-conhecimento')).toBeTruthy();
        expect(context.wrapper!.emitted('adicionar-conhecimento')![0]).toEqual(['Novo Conhecimento']);
    });

    it("não deve salvar edição de atividade se descrição vazia", async () => {
        const mountOptions = getCommonMountOptions({}, commonStubs);
        context.wrapper = mount(AtividadeItem, {
            ...mountOptions,
            props: {atividade: atividadeMock, podeEditar: true},
        });

        await context.wrapper.find('[data-testid="btn-editar-atividade"]').trigger('click');
        const input = context.wrapper.find('[data-testid="inp-editar-atividade"]');

        await input.setValue('');
        await context.wrapper.find('[data-testid="btn-salvar-edicao-atividade"]').trigger('click');
        expect(context.wrapper.emitted('atualizar-atividade')).toBeFalsy();
    });

    it("não deve salvar edição de atividade se descrição igual", async () => {
        const mountOptions = getCommonMountOptions({}, commonStubs);
        context.wrapper = mount(AtividadeItem, {
            ...mountOptions,
            props: {atividade: atividadeMock, podeEditar: true},
        });

        await context.wrapper.find('[data-testid="btn-editar-atividade"]').trigger('click');
        const input = context.wrapper.find('[data-testid="inp-editar-atividade"]');

        await input.setValue('Atividade Teste'); // Same as initial
        await context.wrapper.find('[data-testid="btn-salvar-edicao-atividade"]').trigger('click');
        expect(context.wrapper.emitted('atualizar-atividade')).toBeFalsy();
    });

    it("não deve adicionar conhecimento vazio", async () => {
        const mountOptions = getCommonMountOptions({}, commonStubs);
        context.wrapper = mount(AtividadeItem, {
            ...mountOptions,
            props: {atividade: atividadeMock, podeEditar: true},
        });

        const input = context.wrapper.find('[data-testid="inp-novo-conhecimento"]');
        await input.setValue('');
        await context.wrapper.find('[data-testid="form-novo-conhecimento"]').trigger('submit');

        expect(context.wrapper.emitted('adicionar-conhecimento')).toBeFalsy();
    });

    it("deve emitir 'remover-atividade'", async () => {
        const mountOptions = getCommonMountOptions({}, commonStubs);
        context.wrapper = mount(AtividadeItem, {
            ...mountOptions,
            props: {atividade: atividadeMock, podeEditar: true},
        });

        await context.wrapper.find('[data-testid="btn-remover-atividade"]').trigger('click');
        expect(context.wrapper.emitted('remover-atividade')).toBeTruthy();
    });

    it("deve emitir 'remover-conhecimento'", async () => {
        const mountOptions = getCommonMountOptions({}, commonStubs);
        context.wrapper = mount(AtividadeItem, {
            ...mountOptions,
            props: {atividade: atividadeMock, podeEditar: true},
        });

        await context.wrapper.find('[data-testid="btn-remover-conhecimento"]').trigger('click');
        expect(context.wrapper!.emitted('remover-conhecimento')).toBeTruthy();
        expect(context.wrapper!.emitted('remover-conhecimento')![0]).toEqual([10]);
    });

    it("deve salvar edição de conhecimento", async () => {
        const mountOptions = getCommonMountOptions({}, commonStubs);
        context.wrapper = mount(AtividadeItem, {
            ...mountOptions,
            props: {atividade: atividadeMock, podeEditar: true},
        });

        await context.wrapper.find('[data-testid="btn-editar-conhecimento"]').trigger('click');
        const input = context.wrapper.find('[data-testid="inp-editar-conhecimento"]');
        await input.setValue('Conhecimento Editado');
        await context.wrapper.find('[data-testid="btn-salvar-edicao-conhecimento"]').trigger('click');

        expect(context.wrapper!.emitted('atualizar-conhecimento')).toBeTruthy();
        expect(context.wrapper!.emitted('atualizar-conhecimento')![0]).toEqual([10, 'Conhecimento Editado']);
    });

    it("não deve salvar edição de conhecimento vazio", async () => {
        const mountOptions = getCommonMountOptions({}, commonStubs);
        context.wrapper = mount(AtividadeItem, {
            ...mountOptions,
            props: {atividade: atividadeMock, podeEditar: true},
        });

        await context.wrapper.find('[data-testid="btn-editar-conhecimento"]').trigger('click');
        const input = context.wrapper.find('[data-testid="inp-editar-conhecimento"]');
        await input.setValue('');
        await context.wrapper.find('[data-testid="btn-salvar-edicao-conhecimento"]').trigger('click');

        expect(context.wrapper.emitted('atualizar-conhecimento')).toBeFalsy();
    });

    it("deve cancelar edição de conhecimento", async () => {
        const mountOptions = getCommonMountOptions({}, commonStubs);
        context.wrapper = mount(AtividadeItem, {
            ...mountOptions,
            props: {atividade: atividadeMock, podeEditar: true},
        });

        await context.wrapper.find('[data-testid="btn-editar-conhecimento"]').trigger('click');
        expect(context.wrapper.find('[data-testid="inp-editar-conhecimento"]').exists()).toBe(true);

        await context.wrapper.find('[data-testid="btn-cancelar-edicao-conhecimento"]').trigger('click');
        expect(context.wrapper.find('[data-testid="inp-editar-conhecimento"]').exists()).toBe(false);
    });
});
