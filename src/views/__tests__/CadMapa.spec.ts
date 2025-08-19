import {mount} from '@vue/test-utils';
import {createPinia, setActivePinia} from 'pinia';
import CadMapa from '../CadMapa.vue';
import {useMapasStore} from '@/stores/mapas';
import {useUnidadesStore} from '@/stores/unidades';
import {useAtividadesStore} from '@/stores/atividades';
import {useProcessosStore} from '@/stores/processos';
import {beforeEach, describe, expect, it, vi} from 'vitest';

describe('CadMapa.vue', () => {
    let mapasStore: ReturnType<typeof useMapasStore>;
    let unidadesStore: ReturnType<typeof useUnidadesStore>;
    let atividadesStore: ReturnType<typeof useAtividadesStore>;
    let processosStore: ReturnType<typeof useProcessosStore>;

    beforeEach(() => {
        setActivePinia(createPinia());
        mapasStore = useMapasStore();
        unidadesStore = useUnidadesStore();
        atividadesStore = useAtividadesStore();
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
            }
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
        ];
        atividadesStore.atividades = [
            {id: 10, descricao: 'Atividade A', idSubprocesso: 101, conhecimentos: []},
            {id: 11, descricao: 'Atividade B', idSubprocesso: 101, conhecimentos: []},
        ];
        mapasStore.mapas = [];

        // Espionar métodos das stores
        vi.spyOn(mapasStore, 'editarMapa');
        vi.spyOn(mapasStore, 'adicionarMapa');

        // Resetar o estado das stores para garantir isolamento entre os testes
        mapasStore.$reset();
        unidadesStore.$reset();
        atividadesStore.$reset();
        processosStore.$reset();
    });

    it('deve renderizar corretamente com os dados iniciais da unidade', async () => {
        const wrapper = mount(CadMapa, {
            props: {
                sigla: 'UN1',
                idProcesso: 1,
            },
            global: {
                plugins: [createPinia()],
            },
        });

        expect(wrapper.text()).toContain('UN1 - Unidade Teste 1');
        expect(wrapper.find('h2').text()).toBe('Mapa de competências técnicas');
        expect(wrapper.find('[data-testid="btn-abrir-criar-competencia"]').exists()).toBe(true);
        expect(wrapper.find('button').text()).toContain('Disponibilizar');
    });

    it('deve abrir e fechar o modal de criação de competência', async () => {
        const wrapper = mount(CadMapa, {
            props: {sigla: 'UN1', idProcesso: 1},
            global: {plugins: [createPinia()]},
        });

        // Abrir modal
        await wrapper.find('[data-testid="btn-abrir-criar-competencia"]').trigger('click');
        expect(wrapper.find('.modal-dialog').exists()).toBe(true);
        expect(wrapper.find('h5.modal-title').text()).toBe('Criação de competência');

        // Fechar modal
        await wrapper.find('.modal-header button.btn-close').trigger('click');
        expect(wrapper.find('.modal-dialog').exists()).toBe(false);
    });

    it('deve adicionar uma nova competência', async () => {
        const wrapper = mount(CadMapa, {
            props: {sigla: 'UN1', idProcesso: 1},
            global: {plugins: [createPinia()]},
        });

        await wrapper.find('[data-testid="btn-abrir-criar-competencia"]').trigger('click');
        await wrapper.find('[data-testid="input-nova-competencia"]').setValue('Nova Competência');
        await wrapper.find('[data-testid="atividade-checkbox"]').trigger('click'); // Seleciona uma atividade

        await wrapper.find('[data-testid="btn-criar-competencia"]').trigger('click');

        expect(mapasStore.adicionarMapa).toHaveBeenCalled();
        expect(wrapper.findAll('[data-testid="competencia-item"]').length).toBe(1);
        expect(wrapper.find('[data-testid="competencia-descricao"]').text()).toBe('Nova Competência');
    });

    it('deve editar uma competência existente', async () => {
        mapasStore.mapas = [
            {
                id: 1,
                unidade: 'UN1',
                idProcesso: 1,
                competencias: [{id: 100, descricao: 'Competência Original', atividadesAssociadas: [10]}],
                situacao: 'em_andamento',
                dataCriacao: new Date(),
                dataDisponibilizacao: null,
                dataFinalizacao: null,
            },
        ];

        const wrapper = mount(CadMapa, {
            props: {
                sigla: 'UN1',
                idProcesso: 1,
            },
            global: {
                plugins: [createPinia()],
            },
        });

        await wrapper.vm.$nextTick();

        // Iniciar edição
        await wrapper.find('[data-testid="btn-editar-competencia"]').trigger('click');
        expect(wrapper.find('h5.modal-title').text()).toBe('Edição de competência');

        // Editar e salvar
        await wrapper.find('[data-testid="input-nova-competencia"]').setValue('Competência Editada');
        await wrapper.find('[data-testid="btn-criar-competencia"]').trigger('click');

        expect(mapasStore.editarMapa).toHaveBeenCalledWith(
            1,
            expect.objectContaining({
                competencias: expect.arrayContaining([
                    expect.objectContaining({
                        id: 100,
                        descricao: 'Competência Editada',
                    }),
                ]),
            })
        );
        expect(wrapper.find('[data-testid="competencia-descricao"]').text()).toBe('Competência Editada');
    });

    it('deve excluir uma competência', async () => {
        mapasStore.mapas = [
            {
                id: 1,
                unidade: 'UN1',
                idProcesso: 1,
                competencias: [{id: 100, descricao: 'Competência a Excluir', atividadesAssociadas: []}],
                situacao: 'em_andamento',
                dataCriacao: new Date(),
                dataDisponibilizacao: null,
                dataFinalizacao: null,
            },
        ];

        const wrapper = mount(CadMapa, {
            props: {sigla: 'UN1', idProcesso: 1},
            global: {plugins: [createPinia()]},
        });

        await wrapper.vm.$nextTick();

        await wrapper.find('[data-testid="btn-excluir-competencia"]').trigger('click');

        expect(mapasStore.editarMapa).toHaveBeenCalledWith(
            1,
            expect.objectContaining({
                competencias: [],
            })
        );
        expect(wrapper.findAll('[data-testid="competencia-item"]').length).toBe(0);
    });

    it('deve remover uma atividade associada a uma competência', async () => {
        mapasStore.mapas = [
            {
                id: 1,
                unidade: 'UN1',
                idProcesso: 1,
                competencias: [{id: 100, descricao: 'Competência', atividadesAssociadas: [10, 11]}],
                situacao: 'em_andamento',
                dataCriacao: new Date(),
                dataDisponibilizacao: null,
                dataFinalizacao: null,
            },
        ];

        const wrapper = mount(CadMapa, {
            props: {sigla: 'UN1', idProcesso: 1},
            global: {plugins: [createPinia()]},
        });

        await wrapper.vm.$nextTick();

        await wrapper.find('.botao-acao-inline').trigger('click');

        expect(mapasStore.editarMapa).toHaveBeenCalledWith(
            1,
            expect.objectContaining({
                competencias: expect.arrayContaining([
                    expect.objectContaining({
                        id: 100,
                        atividadesAssociadas: [11],
                    }),
                ]),
            })
        );
    });

    it('deve abrir o modal de disponibilização e preencher a data limite', async () => {
        mapasStore.mapas = [
            {
                id: 1,
                unidade: 'UN1',
                idProcesso: 1,
                competencias: [{id: 100, descricao: 'Competência', atividadesAssociadas: [10]}],
                situacao: 'em_andamento',
                dataCriacao: new Date(),
                dataDisponibilizacao: null,
                dataFinalizacao: null,
            },
        ];

        const wrapper = mount(CadMapa, {
            props: {sigla: 'UN1', idProcesso: 1},
            global: {plugins: [createPinia()]},
        });

        await wrapper.find('button.btn-outline-success').trigger('click');
        expect(wrapper.find('.modal-dialog').exists()).toBe(true);
        expect(wrapper.find('h5.modal-title').text()).toBe('Disponibilizar Mapa');

        const inputDataLimite = wrapper.find('#dataLimite');
        await inputDataLimite.setValue('2025-12-31');
        expect((inputDataLimite.element as HTMLInputElement).value).toBe('2025-12-31');
    });

    it('deve disponibilizar o mapa e exibir notificação', async () => {
        mapasStore.mapas = [
            {
                id: 1,
                unidade: 'UN1',
                idProcesso: 1,
                competencias: [{id: 100, descricao: 'Competência', atividadesAssociadas: [10]}],
                situacao: 'em_andamento',
                dataCriacao: new Date(),
                dataDisponibilizacao: null,
                dataFinalizacao: null,
            },
        ];

        const wrapper = mount(CadMapa, {
            props: {sigla: 'UN1', idProcesso: 1},
            global: {
                plugins: [createPinia()],
            },
        });

        await wrapper.find('button.btn-outline-success').trigger('click');
        await wrapper.find('#dataLimite').setValue('2025-12-31');
        await wrapper.find('button.btn-success').trigger('click');

        expect(mapasStore.editarMapa).toHaveBeenCalledWith(
            1,
            expect.objectContaining({
                situacao: 'disponivel_validacao',
            })
        );
        expect(wrapper.find('.alert-info').text()).toContain('O mapa de competências da unidade UN1 foi disponibilizado para validação até 31/12/2025. (Simulação)');
    });
});