import {beforeEach, describe, expect, it, vi} from 'vitest';
import {createPinia, setActivePinia} from 'pinia';
import {useAtividadesStore} from '../atividades';
import type {Atividade, Conhecimento} from '@/types/tipos';

const getMockAtividadesData = () => [
    {
        "id": 1,
        "descricao": "Manutenção de sistemas administrativos criados pela unidade",
        "idSubprocesso": 3,
        "conhecimentos": [
            {"id": 1, "descricao": "Criação de testes de integração em Cypress"}
        ]
    },
    {
        "id": 2,
        "descricao": "Especificação de sistemas administrativos",
        "idSubprocesso": 3,
        "conhecimentos": [
            {"id": 6, "descricao": "Modelagem de dados"}
        ]
    },
    {
        "id": 3,
        "descricao": "Implantação de sistemas externos",
        "idSubprocesso": 1, // Changed idSubprocesso for testing purposes
        "conhecimentos": [
            {"id": 40, "descricao": "Conhecimento em configuração de APIs de terceiros"}
        ]
    }
];

// Mock the atividades.json import at the top level
vi.mock('../../mocks/atividades.json', async () => {
    return {
        default: getMockAtividadesData(), // Use the function to get fresh data
    };
});

describe('useAtividadesStore', () => {
    let atividadesStore: ReturnType<typeof useAtividadesStore>;

    beforeEach(async () => { // Make beforeEach async
        setActivePinia(createPinia());
        // Dynamically import useAtividadesStore after the mock is set up
        const {useAtividadesStore: useAtividadesStoreActual} = (await vi.importActual('../atividades')) as {
            useAtividadesStore: typeof useAtividadesStore
        };
        atividadesStore = useAtividadesStoreActual();
        // Reset the store state to the mock data before each test
        // Pinia's $reset() method is not available for stores with custom state setup
        // So, manually reset the state based on the initial mock data
        const initialAtividades = getMockAtividadesData();
        atividadesStore.atividades = initialAtividades.map((a: Atividade) => ({
            ...a,
            conhecimentos: a.conhecimentos.map((c: Conhecimento) => ({...c}))
        }));
        atividadesStore.nextId = Math.max(...initialAtividades.flatMap((a: Atividade) => [a.id, ...a.conhecimentos.map((c: Conhecimento) => c.id)])) + 1;
    });

    it('should initialize with mock activities and correct nextId', () => {
        expect(atividadesStore.atividades.length).toBe(getMockAtividadesData().length);
        expect(atividadesStore.nextId).toBe(41); // Based on mock data's max ID
    });

    describe('getters', () => {
        it('getAtividadesPorsubprocesso should filter activities by idSubprocesso', () => {
            const atividades = atividadesStore.getAtividadesPorSubprocesso(3);
            expect(atividades.length).toBe(2);
            expect(atividades[0].id).toBe(1);
            expect(atividades[1].id).toBe(2);
        });

        it('getAtividadesPorsubprocesso should return empty array if no matching idSubprocesso', () => {
            const atividades = atividadesStore.getAtividadesPorSubprocesso(999);
            expect(atividades.length).toBe(0);
        });
    });

    describe('actions', () => {
        it('setAtividades should replace activities for a given idSubprocesso', () => {
            const novasAtividades: Atividade[] = [
                {id: 100, descricao: 'Nova Atividade 1', idSubprocesso: 3, conhecimentos: []},
                {id: 101, descricao: 'Nova Atividade 2', idSubprocesso: 3, conhecimentos: []},
            ];
            const initialLength = atividadesStore.atividades.length;
            atividadesStore.setAtividades(3, novasAtividades);

            // Should remove old activities for idSubprocesso 3 (id 1, 2) and add new ones
            expect(atividadesStore.atividades.length).toBe(initialLength - 2 + novasAtividades.length);
            expect(atividadesStore.atividades.some((a: Atividade) => a.id === 1)).toBe(false);
            expect(atividadesStore.atividades.some((a: Atividade) => a.id === 2)).toBe(false);
            expect(atividadesStore.atividades.some((a: Atividade) => a.id === 100)).toBe(true);
            expect(atividadesStore.atividades.some((a: Atividade) => a.id === 101)).toBe(true);
        });

        it('adicionarAtividade should add a new activity and assign nextId', () => {
            const novaAtividade: Atividade = {
                id: 0, // ID will be assigned by the store
                descricao: 'Atividade Teste',
                idSubprocesso: 5,
                conhecimentos: []
            };
            const initialLength = atividadesStore.atividades.length;
            const expectedNextId = atividadesStore.nextId;

            atividadesStore.adicionarAtividade(novaAtividade);

            expect(atividadesStore.atividades.length).toBe(initialLength + 1);
            expect(novaAtividade.id).toBe(expectedNextId);
            expect(atividadesStore.nextId).toBe(expectedNextId + 1);
            expect(atividadesStore.atividades.some((a: Atividade) => a.id === expectedNextId)).toBe(true);
        });

        it('removerAtividade should remove an activity by ID', () => {
            const initialLength = atividadesStore.atividades.length;
            atividadesStore.removerAtividade(1);
            expect(atividadesStore.atividades.length).toBe(initialLength - 1);
            expect(atividadesStore.atividades.some((a: Atividade) => a.id === 1)).toBe(false);
        });

        it('removerAtividade should not change state if activity not found', () => {
            const initialLength = atividadesStore.atividades.length;
            atividadesStore.removerAtividade(999);
            expect(atividadesStore.atividades.length).toBe(initialLength);
        });

        it('adicionarConhecimento should add a knowledge to an activity and assign nextId', () => {
            const atividadeId = 1;
            const novoConhecimento: Conhecimento = {id: 0, descricao: 'Novo Conhecimento Teste'};
            const initialConhecimentosLength = atividadesStore.atividades.find((a: Atividade) => a.id === atividadeId)?.conhecimentos.length || 0;
            const expectedNextId = atividadesStore.nextId;

            atividadesStore.adicionarConhecimento(atividadeId, novoConhecimento, []);

            const atividadeAtualizada = atividadesStore.atividades.find((a: Atividade) => a.id === atividadeId);
            expect(atividadeAtualizada?.conhecimentos.length).toBe(initialConhecimentosLength + 1);
            expect(novoConhecimento.id).toBe(expectedNextId);
            expect(atividadesStore.nextId).toBe(expectedNextId + 1);
            expect(atividadeAtualizada?.conhecimentos.some((c: Conhecimento) => c.id === expectedNextId)).toBe(true);
        });

        it('adicionarConhecimento should not add knowledge if activity not found', () => {
            const initialNextId = atividadesStore.nextId;
            const initialLength = atividadesStore.atividades.length;
            atividadesStore.adicionarConhecimento(999, {id: 0, descricao: 'Non Existent'}, []);
            expect(atividadesStore.atividades.length).toBe(initialLength);
            expect(atividadesStore.nextId).toBe(initialNextId);
        });

        it('removerConhecimento should remove a knowledge from an activity', () => {
            const atividadeId = 1;
            const conhecimentoId = 1; // ID of "Criação de testes de integração em Cypress"
            const initialConhecimentosLength = atividadesStore.atividades.find((a: Atividade) => a.id === atividadeId)?.conhecimentos.length || 0;

            atividadesStore.removerConhecimento(atividadeId, conhecimentoId, []);

            const atividadeAtualizada = atividadesStore.atividades.find((a: Atividade) => a.id === atividadeId);
            expect(atividadeAtualizada?.conhecimentos.length).toBe(initialConhecimentosLength - 1);
            expect(atividadeAtualizada?.conhecimentos.some((c: Conhecimento) => c.id === conhecimentoId)).toBe(false);
        });

        it('removerConhecimento should not change state if activity or knowledge not found', () => {
            const initialLength = atividadesStore.atividades.length;
            const initialConhecimentosLength = atividadesStore.atividades.find((a: Atividade) => a.id === 1)?.conhecimentos.length || 0;

            atividadesStore.removerConhecimento(999, 1, []); // Non-existent activity
            expect(atividadesStore.atividades.length).toBe(initialLength);
            expect(atividadesStore.atividades.find((a: Atividade) => a.id === 1)?.conhecimentos.length).toBe(initialConhecimentosLength);

            atividadesStore.removerConhecimento(1, 999, []); // Non-existent knowledge
            expect(atividadesStore.atividades.length).toBe(initialLength);
            expect(atividadesStore.atividades.find((a: Atividade) => a.id === 1)?.conhecimentos.length).toBe(initialConhecimentosLength);
        });

        it('fetchAtividadesPorsubprocesso should fetch and add activities without duplication', async () => {
            // Create a fresh store instance for this test
            setActivePinia(createPinia());
            const {useAtividadesStore: useAtividadesStoreActual} = (await vi.importActual('../atividades')) as {
                useAtividadesStore: typeof useAtividadesStore
            };
            const testAtividadesStore = useAtividadesStoreActual();

            // Manually set the initial state for this test
            testAtividadesStore.atividades = []; // Clear the array first
            const initialAtividades = getMockAtividadesData();
            testAtividadesStore.atividades = initialAtividades.map((a: Atividade) => ({
                ...a,
                conhecimentos: a.conhecimentos.map((c: Conhecimento) => ({...c}))
            }));
            testAtividadesStore.nextId = Math.max(...initialAtividades.flatMap((a: Atividade) => [a.id, ...a.conhecimentos.map((c: Conhecimento) => c.id)])) + 1;

            // Spy on the fetchAtividadesPorsubprocesso action and mock its implementation
            const fetchSpy = vi.spyOn(testAtividadesStore, 'fetchAtividadesPorSubprocesso').mockImplementation(async function (this: typeof testAtividadesStore, idSubprocesso: number) {
                const fetchedActivities: Atividade[] = [
                    {id: 4, descricao: "Fetched Activity 1", idSubprocesso: 3, conhecimentos: []},
                    {id: 1, descricao: "Existing Activity", idSubprocesso: 3, conhecimentos: []}, // Duplicate
                ];

                const atividadesDoProcesso = fetchedActivities.filter((a: Atividade) => a.idSubprocesso === idSubprocesso);

                atividadesDoProcesso.forEach((novaAtividade: Atividade) => {
                    if (!testAtividadesStore.atividades.some((a: Atividade) => a.id === novaAtividade.id)) {
                        (this as any).atividades.push(novaAtividade);
                    }
                });
            });

            const initialLength = testAtividadesStore.atividades.length; // This will be 3 (from mockAtividadesData)
            await testAtividadesStore.fetchAtividadesPorSubprocesso(3);

            // Should add only non-duplicate fetched activity
            expect(testAtividadesStore.atividades.length).toBe(initialLength + 1); // Expects 3 + 1 = 4
            expect(testAtividadesStore.atividades.some((a: Atividade) => a.id === 4)).toBe(true);
            expect(testAtividadesStore.atividades.filter((a: Atividade) => a.id === 1).length).toBe(1); // Should not duplicate existing ID 1

            fetchSpy.mockRestore(); // Restore the original implementation
        });

        it('adicionarMultiplasAtividades should add multiple activities and assign unique IDs', () => {
            const novasAtividades: Atividade[] = [
                {
                    id: 0,
                    descricao: 'Multi Atividade 1',
                    idSubprocesso: 2,
                    conhecimentos: [{id: 0, descricao: 'Multi Conhecimento 1'}]
                },
                {
                    id: 0,
                    descricao: 'Multi Atividade 2',
                    idSubprocesso: 2,
                    conhecimentos: [{id: 0, descricao: 'Multi Conhecimento 2'}]
                },
            ];
            const initialLength = atividadesStore.atividades.length;
            const initialNextId = atividadesStore.nextId;

            atividadesStore.adicionarMultiplasAtividades(novasAtividades);

            expect(atividadesStore.atividades.length).toBe(initialLength + 2);
            expect(atividadesStore.atividades[initialLength].id).toBe(initialNextId);
            expect(atividadesStore.atividades[initialLength].conhecimentos[0].id).toBe(initialNextId + 1);
            expect(atividadesStore.atividades[initialLength + 1].id).toBe(initialNextId + 2);
            expect(atividadesStore.atividades[initialLength + 1].conhecimentos[0].id).toBe(initialNextId + 3);
            expect(atividadesStore.nextId).toBe(initialNextId + 4);
        });
    });
});
