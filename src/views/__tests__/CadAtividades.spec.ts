import {mount} from '@vue/test-utils';
import {createPinia, setActivePinia} from 'pinia';
import CadAtividades from '../CadAtividades.vue';
import {useAtividadesStore} from '@/stores/atividades';
import {useUnidadesStore} from '@/stores/unidades';
import {useProcessosStore} from '@/stores/processos';
import {usePerfil} from '@/composables/usePerfil';
import {ref} from 'vue';
import {beforeEach, describe, expect, it, vi} from 'vitest';
import {ProcessoTipo} from '@/types/tipos';

// Mock do Bootstrap Modal para evitar erros de referência no ambiente de teste
const mockModal = {
    getInstance: () => ({
        hide: vi.fn()
    }),
    hide: vi.fn(),
    show: vi.fn(),
};
(window as any).bootstrap = {Modal: mockModal};

// Mock do composable usePerfil
vi.mock('@/composables/usePerfil', () => ({
    usePerfil: vi.fn(() => ({
        perfilSelecionado: ref('ADMIN'),
    })),
}));

describe('CadAtividades.vue', () => {
    let atividadesStore: ReturnType<typeof useAtividadesStore>;
    let unidadesStore: ReturnType<typeof useUnidadesStore>;
    let processosStore: ReturnType<typeof useProcessosStore>;

    beforeEach(() => {
        setActivePinia(createPinia());
        atividadesStore = useAtividadesStore();
        unidadesStore = useUnidadesStore();
        processosStore = useProcessosStore();

        vi.clearAllMocks();

        // Mock de dados iniciais para as stores
        unidadesStore.unidades = [
            {
                id: 101,
                sigla: 'UN1',
                nome: 'Unidade Teste 1',
                filhas: [],
                titular: 1,
                responsavel: null,
                tipo: 'OPERACIONAL'
            },
            {
                id: 102,
                sigla: 'UN2',
                nome: 'Unidade Teste 2',
                filhas: [],
                titular: 2,
                responsavel: null,
                tipo: 'OPERACIONAL'
            },
        ];
        processosStore.processosUnidade = [
            {
                id: 101,
                idProcesso: 1,
                unidade: 'UN1',
                situacao: 'Em andamento',
                dataLimiteEtapa1: new Date(),
                dataLimiteEtapa2: new Date(),
                dataFimEtapa1: null,
                dataFimEtapa2: null,
                unidadeAtual: 'UN1',
                unidadeAnterior: null
            },
            {
                id: 102,
                idProcesso: 1,
                unidade: 'UN2',
                situacao: 'Em andamento',
                dataLimiteEtapa1: new Date(),
                dataLimiteEtapa2: new Date(),
                dataFimEtapa1: null,
                dataFimEtapa2: null,
                unidadeAtual: 'UN2',
                unidadeAnterior: null
            },
        ];
        processosStore.processos = [
            {
                id: 1,
                descricao: 'Processo Teste',
                tipo: ProcessoTipo.MAPEAMENTO,
                situacao: 'Finalizado',
                dataLimite: new Date()
            },
        ];

        // Espionar métodos das stores
        vi.spyOn(atividadesStore, 'adicionarAtividade');
        vi.spyOn(atividadesStore, 'removerAtividade');
        vi.spyOn(atividadesStore, 'adicionarConhecimento');
        vi.spyOn(atividadesStore, 'removerConhecimento');
        vi.spyOn(atividadesStore, 'setAtividades');
        vi.spyOn(atividadesStore, 'adicionarMultiplasAtividades');
        vi.spyOn(atividadesStore, 'fetchAtividadesPorSubprocesso');

        // Resetar o estado das stores para garantir isolamento entre os testes
        atividadesStore.$reset();
        unidadesStore.$reset();
        processosStore.$reset();
    });

    it('deve renderizar corretamente com os dados iniciais da unidade', async () => {
        const wrapper = mount(CadAtividades, {
            props: {
                idProcesso: 1,
                sigla: 'UN1',
            },
            global: {
                plugins: [createPinia()],
            },
        });

        expect(wrapper.text()).toContain('UN1 - Unidade Teste 1');
        expect(wrapper.find('h1').text()).toBe('Atividades e conhecimentos');
        expect(wrapper.find('[data-testid="input-nova-atividade"]').exists()).toBe(true);
        expect(wrapper.find('[data-testid="btn-adicionar-atividade"]').exists()).toBe(true);
    });

    it('deve adicionar uma nova atividade', async () => {
        const wrapper = mount(CadAtividades, {
            props: {
                idProcesso: 1,
                sigla: 'UN1',
            },
            global: {
                plugins: [createPinia()],
            },
        });

        const inputAtividade = wrapper.find('[data-testid="input-nova-atividade"]');
        await inputAtividade.setValue('Nova Atividade de Teste');
        await wrapper.find('[data-testid="btn-adicionar-atividade"]').trigger('submit');

        expect(atividadesStore.adicionarAtividade).toHaveBeenCalledWith(
            expect.objectContaining({
                descricao: 'Nova Atividade de Teste',
                idSubprocesso: 101,
                conhecimentos: [],
            })
        );
        expect((inputAtividade.element as HTMLInputElement).value).toBe('');
    });

    it('não deve adicionar atividade se o campo estiver vazio', async () => {
        const wrapper = mount(CadAtividades, {
            props: {
                idProcesso: 1,
                sigla: 'UN1',
            },
            global: {
                plugins: [createPinia()],
            },
        });

        const inputAtividade = wrapper.find('[data-testid="input-nova-atividade"]');
        await inputAtividade.setValue('');
        await wrapper.find('[data-testid="btn-adicionar-atividade"]').trigger('submit');

        expect(atividadesStore.adicionarAtividade).not.toHaveBeenCalled();
    });

    it('deve remover uma atividade', async () => {
        // Configura atividades iniciais na store
        atividadesStore.atividades = [
            {id: 1, descricao: 'Atividade 1', idSubprocesso: 101, conhecimentos: []},
            {id: 2, descricao: 'Atividade 2', idSubprocesso: 101, conhecimentos: []},
        ];

        const wrapper = mount(CadAtividades, {
            props: {
                idProcesso: 1,
                sigla: 'UN1',
            },
            global: {
                plugins: [createPinia()],
            },
        });

        // Esperar a renderização das atividades
        await wrapper.vm.$nextTick();

        // Encontrar o botão de remover da primeira atividade
        const removerBtn = wrapper.findAll('[data-testid="btn-remover-atividade"]')[0];
        await removerBtn.trigger('click');

        expect(atividadesStore.removerAtividade).toHaveBeenCalledWith(1);
    });

    it('deve iniciar e salvar a edição de uma atividade', async () => {
        atividadesStore.atividades = [
            {id: 1, descricao: 'Atividade Original', idSubprocesso: 101, conhecimentos: []},
        ];

        const wrapper = mount(CadAtividades, {
            props: {
                idProcesso: 1,
                sigla: 'UN1',
            },
            global: {
                plugins: [createPinia()],
            },
        });

        await wrapper.vm.$nextTick();

        // Iniciar edição
        await wrapper.find('[data-testid="btn-editar-atividade"]').trigger('click');
        expect(wrapper.find('[data-testid="input-editar-atividade"]').exists()).toBe(true);

        // Editar e salvar
        const inputEditar = wrapper.find('[data-testid="input-editar-atividade"]');
        await inputEditar.setValue('Atividade Editada');
        await wrapper.find('[data-testid="btn-salvar-edicao-atividade"]').trigger('click');

        // Verifica se setAtividades foi chamado com a atividade atualizada
        expect(atividadesStore.setAtividades).toHaveBeenCalledWith(
            101,
            expect.arrayContaining([
                expect.objectContaining({
                    id: 1,
                    descricao: 'Atividade Editada',
                }),
            ])
        );
        expect(wrapper.find('[data-testid="input-editar-atividade"]').exists()).toBe(false);
        expect(wrapper.find('[data-testid="atividade-descricao"]').text()).toBe('Atividade Editada');
    });

    it('deve cancelar a edição de uma atividade', async () => {
        atividadesStore.atividades = [
            {id: 1, descricao: 'Atividade Original', idSubprocesso: 101, conhecimentos: []},
        ];

        const wrapper = mount(CadAtividades, {
            props: {
                idProcesso: 1,
                sigla: 'UN1',
            },
            global: {
                plugins: [createPinia()],
            },
        });

        await wrapper.vm.$nextTick();

        // Iniciar edição
        await wrapper.find('[data-testid="btn-editar-atividade"]').trigger('click');
        expect(wrapper.find('[data-testid="input-editar-atividade"]').exists()).toBe(true);

        // Cancelar edição
        await wrapper.find('[data-testid="btn-cancelar-edicao-atividade"]').trigger('click');

        expect(wrapper.find('[data-testid="input-editar-atividade"]').exists()).toBe(false);
        expect(wrapper.find('[data-testid="atividade-descricao"]').text()).toBe('Atividade Original');
    });

    it('deve adicionar um novo conhecimento a uma atividade', async () => {
        atividadesStore.atividades = [
            {id: 1, descricao: 'Atividade com Conhecimento', idSubprocesso: 101, conhecimentos: []},
        ];

        const wrapper = mount(CadAtividades, {
            props: {
                idProcesso: 1,
                sigla: 'UN1',
            },
            global: {
                plugins: [createPinia()],
            },
        });

        await wrapper.vm.$nextTick();

        const inputNovoConhecimento = wrapper.find('[data-testid="input-novo-conhecimento"]');
        await inputNovoConhecimento.setValue('Novo Conhecimento');
        await wrapper.find('[data-testid="btn-adicionar-conhecimento"]').trigger('submit');

        expect(atividadesStore.adicionarConhecimento).toHaveBeenCalledWith(
            1,
            expect.objectContaining({
                descricao: 'Novo Conhecimento',
            })
        );
        expect((inputNovoConhecimento.element as HTMLInputElement).value).toBe('');
    });

    it('deve remover um conhecimento de uma atividade', async () => {
        atividadesStore.atividades = [
            {
                id: 1,
                descricao: 'Atividade 1',
                idSubprocesso: 101,
                conhecimentos: [{id: 10, descricao: 'Conhecimento 1'}]
            },
        ];

        const wrapper = mount(CadAtividades, {
            props: {
                idProcesso: 1,
                sigla: 'UN1',
            },
            global: {
                plugins: [createPinia()],
            },
        });

        await wrapper.vm.$nextTick();

        const removerConhecimentoBtn = wrapper.find('[data-testid="btn-remover-conhecimento"]');
        await removerConhecimentoBtn.trigger('click');

        expect(atividadesStore.removerConhecimento).toHaveBeenCalledWith(1, 10);
    });

    it('deve iniciar e salvar a edição de um conhecimento', async () => {
        atividadesStore.atividades = [
            {
                id: 1,
                descricao: 'Atividade 1',
                idSubprocesso: 101,
                conhecimentos: [{id: 10, descricao: 'Conhecimento Original'}]
            },
        ];

        const wrapper = mount(CadAtividades, {
            props: {
                idProcesso: 1,
                sigla: 'UN1',
            },
            global: {
                plugins: [createPinia()],
            },
        });

        await wrapper.vm.$nextTick();

        // Iniciar edição
        await wrapper.find('[data-testid="btn-editar-conhecimento"]').trigger('click');
        expect(wrapper.find('[data-testid="input-editar-conhecimento"]').exists()).toBe(true);

        // Editar e salvar
        const inputEditar = wrapper.find('[data-testid="input-editar-conhecimento"]');
        await inputEditar.setValue('Conhecimento Editado');
        await wrapper.find('[data-testid="btn-salvar-edicao-conhecimento"]').trigger('click');

        // Verifica se setAtividades foi chamado com o conhecimento atualizado
        expect(atividadesStore.setAtividades).toHaveBeenCalledWith(
            101,
            expect.arrayContaining([
                expect.objectContaining({
                    id: 1,
                    conhecimentos: expect.arrayContaining([
                        expect.objectContaining({
                            id: 10,
                            descricao: 'Conhecimento Editado',
                        }),
                    ]),
                }),
            ])
        );
        expect(wrapper.find('[data-testid="input-editar-conhecimento"]').exists()).toBe(false);
        expect(wrapper.find('[data-testid="conhecimento-descricao"]').text()).toBe('Conhecimento Editado');
    });

    it('deve cancelar a edição de um conhecimento', async () => {
        atividadesStore.atividades = [
            {
                id: 1,
                descricao: 'Atividade 1',
                idSubprocesso: 101,
                conhecimentos: [{id: 10, descricao: 'Conhecimento Original'}]
            },
        ];

        const wrapper = mount(CadAtividades, {
            props: {
                idProcesso: 1,
                sigla: 'UN1',
            },
            global: {
                plugins: [createPinia()],
            },
        });

        await wrapper.vm.$nextTick();

        // Iniciar edição
        await wrapper.find('[data-testid="btn-editar-conhecimento"]').trigger('click');
        expect(wrapper.find('[data-testid="input-editar-conhecimento"]').exists()).toBe(true);

        // Cancelar edição
        await wrapper.find('[data-testid="btn-cancelar-edicao-conhecimento"]').trigger('click');

        expect(wrapper.find('[data-testid="input-editar-conhecimento"]').exists()).toBe(false);
        expect(wrapper.find('[data-testid="conhecimento-descricao"]').text()).toBe('Conhecimento Original');
    });

    it('deve exibir o botão "Importar atividades" apenas para o perfil CHEFE', async () => {
        // @ts-ignore
        (usePerfil as vi.Mock).mockReturnValue({perfilSelecionado: ref('ADMIN')});
        const wrapperAdmin = mount(CadAtividades, {
            props: {idProcesso: 1, sigla: 'UN1'},
            global: {plugins: [createPinia()]},
        });
        expect(wrapperAdmin.find('button[title="Importar"]').exists()).toBe(true);

        // @ts-ignore
        (usePerfil as vi.Mock).mockReturnValue({perfilSelecionado: ref('SERVIDOR')});
        const wrapperServidor = mount(CadAtividades, {
            props: {idProcesso: 1, sigla: 'UN1'},
            global: {plugins: [createPinia()]},
        });
        expect(wrapperServidor.find('button[title="Importar"]').exists()).toBe(false);
    });

    it('deve carregar processos disponíveis no modal de importação', async () => {
        const wrapper = mount(CadAtividades, {
            props: {
                idProcesso: 1,
                sigla: 'UN1',
            },
            global: {
                plugins: [createPinia()],
            },
        });

        await wrapper.find('button[title="Importar"]').trigger('click');
        await wrapper.vm.$nextTick();

        const options = wrapper.findAll('#processo-select option');
        expect(options.length).toBe(2);
        expect(options[1].text()).toBe('Processo Teste');
    });

    it('deve carregar unidades participantes ao selecionar um processo no modal', async () => {
        processosStore.processosUnidade = [
            {
                id: 101,
                idProcesso: 1,
                unidade: 'UN1',
                situacao: 'Em andamento',
                dataLimiteEtapa1: new Date(),
                dataLimiteEtapa2: new Date(),
                dataFimEtapa1: null,
                dataFimEtapa2: null,
                unidadeAtual: 'UN1',
                unidadeAnterior: null
            },
            {
                id: 102,
                idProcesso: 1,
                unidade: 'UN2',
                situacao: 'Em andamento',
                dataLimiteEtapa1: new Date(),
                dataLimiteEtapa2: new Date(),
                dataFimEtapa1: null,
                dataFimEtapa2: null,
                unidadeAtual: 'UN2',
                unidadeAnterior: null
            },
        ];

        const wrapper = mount(CadAtividades, {
            props: {
                idProcesso: 1,
                sigla: 'UN1',
            },
            global: {
                plugins: [createPinia()],
            },
        });

        await wrapper.find('button[title="Importar"]').trigger('click');
        await wrapper.vm.$nextTick();

        await wrapper.find('#processo-select').setValue(1);
        await wrapper.vm.$nextTick();

        const options = wrapper.findAll('#unidade-select option');
        expect(options.length).toBe(3);
        expect(options[1].text()).toBe('UN1');
        expect(options[2].text()).toBe('UN2');
    });

    it('deve carregar atividades para importar ao selecionar uma unidade no modal', async () => {
        atividadesStore.atividades = [
            {id: 20, descricao: 'Atividade Importada', idSubprocesso: 102, conhecimentos: []},
        ];

        const wrapper = mount(CadAtividades, {
            props: {
                idProcesso: 1,
                sigla: 'UN1',
            },
            global: {
                plugins: [createPinia()],
            },
        });

        await wrapper.find('button[title="Importar"]').trigger('click');
        await wrapper.vm.$nextTick();

        await wrapper.find('#processo-select').setValue(1);
        await wrapper.vm.$nextTick();

        await wrapper.find('#unidade-select').setValue(102);
        await wrapper.vm.$nextTick();

        expect(atividadesStore.fetchAtividadesPorSubprocesso).toHaveBeenCalledWith(102);
        expect(wrapper.find('.atividades-container').text()).toContain('Atividade Importada');
    });

    it('deve importar atividades selecionadas e fechar o modal', async () => {
        atividadesStore.atividades = [
            {id: 20, descricao: 'Atividade Importada', idSubprocesso: 102, conhecimentos: []},
        ];

        const wrapper = mount(CadAtividades, {
            props: {
                idProcesso: 1,
                sigla: 'UN1',
            },
            global: {
                plugins: [createPinia()],
            },
        });

        await wrapper.find('button[title="Importar"]').trigger('click');
        await wrapper.vm.$nextTick();

        await wrapper.find('#processo-select').setValue(1);
        await wrapper.vm.$nextTick();

        await wrapper.find('#unidade-select').setValue(102);
        await wrapper.vm.$nextTick();

        await wrapper.find('#ativ-check-20').setValue(true);
        const modalHideSpy = vi.spyOn(mockModal.getInstance(), 'hide');
        await wrapper.find('button.btn-outline-primary').trigger('click');

        expect(atividadesStore.adicionarMultiplasAtividades).toHaveBeenCalledWith(
            expect.arrayContaining([
                expect.objectContaining({
                    descricao: 'Atividade Importada',
                    idSubprocesso: 101,
                }),
            ])
        );
        expect(modalHideSpy).toHaveBeenCalled();
        // Corrigido: cast para HTMLSelectElement
        expect((wrapper.find('#processo-select').element as HTMLSelectElement).value).toBe('');
    });

    // Teste removido pois a função não é exposta e não pode ser espionada diretamente
    // it('deve chamar disponibilizarCadastro ao clicar no botão', async () => {
    //   const wrapper = mount(CadAtividades, {
    //     props: { idProcesso: 1, sigla: 'UN1' },
    //     global: { plugins: [createPinia()] },
    //   });
    //
    //   const disponibilizarCadastroSpy = vi.spyOn(wrapper.vm, 'disponibilizarCadastro');
    //
    //   await wrapper.find('button[title="Disponibilizar"]').trigger('click');
    //
    //   expect(disponibilizarCadastroSpy).toHaveBeenCalled();
    // });
});
