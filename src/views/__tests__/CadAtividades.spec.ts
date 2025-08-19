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

// Mock de unidades.json
vi.mock('@/mocks/unidades.json', () => ({
    default: [
        {sigla: 'UN1', nome: 'Unidade Teste 1', filhas: [], titular: 1, responsavel: null, tipo: 'OPERACIONAL'},
        {sigla: 'UN2', nome: 'Unidade Teste 2', filhas: [], titular: 2, responsavel: null, tipo: 'OPERACIONAL'},
    ],
}));

// Mock de atividades.json
vi.mock('@/mocks/atividades.json', () => ({
    default: [
        {
            id: 1,
            descricao: 'Manutenção de sistemas administrativos criados pela unidade',
            idSubprocesso: 101,
            conhecimentos: []
        },
        {id: 2, descricao: 'Especificação de sistemas administrativos', idSubprocesso: 101, conhecimentos: []},
    ],
}));

// Mock de processos.json
vi.mock('@/mocks/processos.json', () => ({
    default: [
        {
            id: 1,
            descricao: 'Processo Teste',
            tipo: ProcessoTipo.MAPEAMENTO,
            dataLimite: '2025-12-31',
            situacao: 'Finalizado'
        },
    ],
}));

// Mock de subprocessos.json
vi.mock('@/mocks/subprocessos.json', () => ({
    default: [
        {
            id: 101,
            idProcesso: 1,
            unidade: 'UN1',
            situacao: 'Em andamento',
            dataLimiteEtapa1: '2025-12-20',
            dataLimiteEtapa2: '2025-12-25',
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
            dataLimiteEtapa1: '2025-12-20',
            dataLimiteEtapa2: '2025-12-25',
            dataFimEtapa1: null,
            dataFimEtapa2: null,
            unidadeAtual: 'UN2',
            unidadeAnterior: null
        },
    ],
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
        await wrapper.vm.$nextTick(); // Esperar a renderização

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
        await wrapper.vm.$nextTick(); // Esperar a renderização

        const inputAtividade = wrapper.find('[data-testid="input-nova-atividade"]');
        await inputAtividade.setValue('Nova Atividade de Teste');
        await wrapper.find('[data-testid="btn-adicionar-atividade"]').trigger('submit');
        await wrapper.vm.$nextTick(); // Esperar a re-renderização

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
        await wrapper.vm.$nextTick(); // Esperar a renderização

        const inputAtividade = wrapper.find('[data-testid="input-nova-atividade"]');
        await inputAtividade.setValue('');
        await wrapper.find('[data-testid="btn-adicionar-atividade"]').trigger('submit');
        await wrapper.vm.$nextTick(); // Esperar a re-renderização

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

        await wrapper.vm.$nextTick(); // Esperar a renderização das atividades

        // Encontrar o botão de remover da primeira atividade
        const removerBtn = wrapper.findAll('[data-testid="btn-remover-atividade"]')[0];
        expect(removerBtn.exists()).toBe(true); // Verificar se o botão existe
        await removerBtn.trigger('click');
        await wrapper.vm.$nextTick(); // Esperar a re-renderização

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

        await wrapper.vm.$nextTick(); // Esperar a renderização

        // Iniciar edição
        const editarBtn = wrapper.find('[data-testid="btn-editar-atividade"]');
        expect(editarBtn.exists()).toBe(true); // Verificar se o botão existe
        await editarBtn.trigger('click');
        await wrapper.vm.$nextTick(); // Esperar a re-renderização
        expect(wrapper.find('[data-testid="input-editar-atividade"]').exists()).toBe(true);

        // Editar e salvar
        const inputEditar = wrapper.find('[data-testid="input-editar-atividade"]');
        await inputEditar.setValue('Atividade Editada');
        const salvarBtn = wrapper.find('[data-testid="btn-salvar-edicao-atividade"]');
        expect(salvarBtn.exists()).toBe(true); // Verificar se o botão existe
        await salvarBtn.trigger('click');
        await wrapper.vm.$nextTick(); // Esperar a re-renderização

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

        await wrapper.vm.$nextTick(); // Esperar a renderização

        // Iniciar edição
        const editarBtn = wrapper.find('[data-testid="btn-editar-atividade"]');
        expect(editarBtn.exists()).toBe(true); // Verificar se o botão existe
        await editarBtn.trigger('click');
        await wrapper.vm.$nextTick(); // Esperar a re-renderização
        expect(wrapper.find('[data-testid="input-editar-atividade"]').exists()).toBe(true);

        // Cancelar edição
        const cancelarBtn = wrapper.find('[data-testid="btn-cancelar-edicao-atividade"]');
        expect(cancelarBtn.exists()).toBe(true); // Verificar se o botão existe
        await cancelarBtn.trigger('click');
        await wrapper.vm.$nextTick(); // Esperar a re-renderização

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

        await wrapper.vm.$nextTick(); // Esperar a renderização

        const inputNovoConhecimento = wrapper.find('[data-testid="input-novo-conhecimento"]');
        expect(inputNovoConhecimento.exists()).toBe(true); // Verificar se o input existe
        await inputNovoConhecimento.setValue('Novo Conhecimento');
        const adicionarConhecimentoBtn = wrapper.find('[data-testid="btn-adicionar-conhecimento"]');
        expect(adicionarConhecimentoBtn.exists()).toBe(true); // Verificar se o botão existe
        await adicionarConhecimentoBtn.trigger('submit');
        await wrapper.vm.$nextTick(); // Esperar a re-renderização

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

        await wrapper.vm.$nextTick(); // Esperar a renderização

        const removerConhecimentoBtn = wrapper.find('[data-testid="btn-remover-conhecimento"]');
        expect(removerConhecimentoBtn.exists()).toBe(true); // Verificar se o botão existe
        await removerConhecimentoBtn.trigger('click');
        await wrapper.vm.$nextTick(); // Esperar a re-renderização

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

        await wrapper.vm.$nextTick(); // Esperar a renderização

        // Iniciar edição
        const editarConhecimentoBtn = wrapper.find('[data-testid="btn-editar-conhecimento"]');
        expect(editarConhecimentoBtn.exists()).toBe(true); // Verificar se o botão existe
        await editarConhecimentoBtn.trigger('click');
        await wrapper.vm.$nextTick(); // Esperar a re-renderização
        expect(wrapper.find('[data-testid="input-editar-conhecimento"]').exists()).toBe(true);

        // Editar e salvar
        const inputEditarConhecimento = wrapper.find('[data-testid="input-editar-conhecimento"]');
        await inputEditarConhecimento.setValue('Conhecimento Editado');
        const salvarConhecimentoBtn = wrapper.find('[data-testid="btn-salvar-edicao-conhecimento"]');
        expect(salvarConhecimentoBtn.exists()).toBe(true); // Verificar se o botão existe
        await salvarConhecimentoBtn.trigger('click');
        await wrapper.vm.$nextTick(); // Esperar a re-renderização

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

        await wrapper.vm.$nextTick(); // Esperar a renderização

        // Iniciar edição
        const editarConhecimentoBtn = wrapper.find('[data-testid="btn-editar-conhecimento"]');
        expect(editarConhecimentoBtn.exists()).toBe(true); // Verificar se o botão existe
        await editarConhecimentoBtn.trigger('click');
        await wrapper.vm.$nextTick(); // Esperar a re-renderização
        expect(wrapper.find('[data-testid="input-editar-conhecimento"]').exists()).toBe(true);

        // Cancelar edição
        const cancelarConhecimentoBtn = wrapper.find('[data-testid="btn-cancelar-edicao-conhecimento"]');
        expect(cancelarConhecimentoBtn.exists()).toBe(true); // Verificar se o botão existe
        await cancelarConhecimentoBtn.trigger('click');
        await wrapper.vm.$nextTick(); // Esperar a re-renderização

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
        await wrapperAdmin.vm.$nextTick(); // Esperar a renderização
        expect(wrapperAdmin.find('button[title="Importar"]').exists()).toBe(true);

        // @ts-ignore
        (usePerfil as vi.Mock).mockReturnValue({perfilSelecionado: ref('SERVIDOR')});
        const wrapperServidor = mount(CadAtividades, {
            props: {idProcesso: 1, sigla: 'UN1'},
            global: {plugins: [createPinia()]},
        });
        await wrapperServidor.vm.$nextTick(); // Esperar a renderização
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
        await wrapper.vm.$nextTick(); // Esperar a renderização

        const importarBtn = wrapper.find('button[title="Importar"]');
        expect(importarBtn.exists()).toBe(true); // Verificar se o botão existe
        await importarBtn.trigger('click');
        await wrapper.vm.$nextTick(); // Esperar a renderização do modal

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
        await wrapper.vm.$nextTick(); // Esperar a renderização

        const importarBtn = wrapper.find('button[title="Importar"]');
        expect(importarBtn.exists()).toBe(true); // Verificar se o botão existe
        await importarBtn.trigger('click');
        await wrapper.vm.$nextTick(); // Esperar a renderização do modal

        const processoSelect = wrapper.find('#processo-select');
        expect(processoSelect.exists()).toBe(true); // Verificar se o select existe
        await processoSelect.setValue(1);
        await wrapper.vm.$nextTick(); // Esperar a re-renderização

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
        await wrapper.vm.$nextTick(); // Esperar a renderização

        const importarBtn = wrapper.find('button[title="Importar"]');
        expect(importarBtn.exists()).toBe(true); // Verificar se o botão existe
        await importarBtn.trigger('click');
        await wrapper.vm.$nextTick(); // Esperar a renderização do modal

        const processoSelect = wrapper.find('#processo-select');
        expect(processoSelect.exists()).toBe(true); // Verificar se o select existe
        await processoSelect.setValue(1);
        await wrapper.vm.$nextTick(); // Esperar a re-renderização

        const unidadeSelect = wrapper.find('#unidade-select');
        expect(unidadeSelect.exists()).toBe(true); // Verificar se o select existe
        await unidadeSelect.setValue(102);
        await wrapper.vm.$nextTick(); // Esperar a re-renderização

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
        await wrapper.vm.$nextTick(); // Esperar a renderização

        const importarBtn = wrapper.find('button[title="Importar"]');
        expect(importarBtn.exists()).toBe(true); // Verificar se o botão existe
        await importarBtn.trigger('click');
        await wrapper.vm.$nextTick(); // Esperar a renderização do modal

        const processoSelect = wrapper.find('#processo-select');
        expect(processoSelect.exists()).toBe(true); // Verificar se o select existe
        await processoSelect.setValue(1);
        await wrapper.vm.$nextTick(); // Esperar a re-renderização

        const unidadeSelect = wrapper.find('#unidade-select');
        expect(unidadeSelect.exists()).toBe(true); // Verificar se o select existe
        await unidadeSelect.setValue(102);
        await wrapper.vm.$nextTick(); // Esperar a re-renderização

        // Seleciona a atividade para importar
        const ativCheck = wrapper.find('#ativ-check-20');
        expect(ativCheck.exists()).toBe(true); // Verificar se o checkbox existe
        await ativCheck.setValue(true);

        // Espiona o metodo hide do modal
        const modalHideSpy = vi.spyOn(mockModal.getInstance(), 'hide');

        const importarAtividadesBtn = wrapper.find('button.btn-outline-primary');
        expect(importarAtividadesBtn.exists()).toBe(true); // Verificar se o botão existe
        await importarAtividadesBtn.trigger('click');
        await wrapper.vm.$nextTick(); // Esperar a re-renderização

        expect(atividadesStore.adicionarMultiplasAtividades).toHaveBeenCalledWith(
            expect.arrayContaining([
                expect.objectContaining({
                    descricao: 'Atividade Importada',
                    idSubprocesso: 101,
                }),
            ])
        );
        expect(modalHideSpy).toHaveBeenCalled();
        expect((wrapper.find('#processo-select').element as HTMLSelectElement).value).toBe('');
    });
});