import {describe, expect, it, vi} from 'vitest';
import {mount} from '@vue/test-utils';
import ModalAcaoBloco from '../ModalAcaoBloco.vue';

vi.mock("@/utils/date", async () => {
    const actual = await vi.importActual("@/utils/date") as any;
    return {
        ...actual,
        obterAmanhaFormatado: () => '2026-03-25'
    };
});

describe('ModalAcaoBloco.vue', () => {
    const propsPadrao = {
        id: 'modal-teste',
        titulo: 'Título Teste',
        texto: 'Texto Teste',
        rotuloBotao: 'Confirmar',
        unidades: [{codigo: 1, sigla: 'U1', nome: 'Unidade 1', situacao: 'OK'}],
        unidadesPreSelecionadas: [1],
        mostrarDataLimite: true,
        modelValue: true // Forçar exibição para o teste encontrar o conteúdo
    };

    it('deve ter o atributo min correto no campo de data', async () => {
        const wrapper = mount(ModalAcaoBloco, {
            props: propsPadrao
        });
        const input = wrapper.find('[data-testid="inp-data-limite-bloco"]');
        expect(input.attributes('min')).toBe('2026-03-25');
    });

    it('deve exibir erro se a data não for futura', async () => {
        const wrapper = mount(ModalAcaoBloco, {
            props: propsPadrao
        });
        const input = wrapper.find('[data-testid="inp-data-limite-bloco"]');

        await input.setValue('2026-03-24'); // hoje no mock

        expect(wrapper.text()).toContain('A data limite para validação deve ser uma data futura.');
    });

    it("deve emitir confirmar com unidades selecionadas e data válida", async () => {
        const wrapper = mount(ModalAcaoBloco, {
            props: propsPadrao
        });
        (wrapper.vm as unknown as { abrir: () => void }).abrir();
        await wrapper.vm.$nextTick();

        const input = wrapper.find('[data-testid="inp-data-limite-bloco"]');
        await input.setValue("2099-01-01");
        await (wrapper.vm as unknown as { confirmar: () => Promise<void> }).confirmar();

        expect(wrapper.emitted("confirmar")).toBeDefined();
        expect(wrapper.emitted("confirmar")?.[0]).toEqual([{ids: [1], dataLimite: "2099-01-01"}]);
    });

    it('deve usar a maior última data limite selecionada como mínimo', () => {
        const wrapper = mount(ModalAcaoBloco, {
            props: {
                ...propsPadrao,
                unidades: [
                    {
                        codigo: 1,
                        sigla: 'U1',
                        nome: 'Unidade 1',
                        situacao: 'OK',
                        ultimaDataLimite: '2026-03-28T00:00:00'
                    },
                    {codigo: 2, sigla: 'U2', nome: 'Unidade 2', situacao: 'OK', ultimaDataLimite: '2026-03-30T00:00:00'}
                ],
                unidadesPreSelecionadas: [1, 2]
            }
        });
        const input = wrapper.find('[data-testid="inp-data-limite-bloco"]');
        expect(input.attributes('min')).toBe('2026-03-30');
    });

    it("não deve emitir confirmar quando nenhuma unidade está selecionada", async () => {
        const wrapper = mount(ModalAcaoBloco, {
            props: {
                ...propsPadrao,
                unidadesPreSelecionadas: [],
            }
        });
        (wrapper.vm as unknown as { abrir: () => void }).abrir();
        await wrapper.vm.$nextTick();

        await (wrapper.vm as unknown as { confirmar: () => Promise<void> }).confirmar();

        expect(wrapper.emitted("confirmar")).toBeUndefined();
        expect(wrapper.text()).toContain("Selecione ao menos uma unidade.");
    });

    it("deve emitir confirmar sem data quando mostrarDataLimite for falso", async () => {
        const wrapper = mount(ModalAcaoBloco, {
            props: {
                ...propsPadrao,
                mostrarDataLimite: false,
            }
        });
        (wrapper.vm as unknown as { abrir: () => void }).abrir();
        await wrapper.vm.$nextTick();
        await (wrapper.vm as unknown as { confirmar: () => Promise<void> }).confirmar();

        expect(wrapper.emitted("confirmar")).toBeDefined();
        expect(wrapper.emitted("confirmar")?.[0]).toEqual([{ids: [1], dataLimite: undefined}]);
    });

    it("deve exibir erro quando data for menor que a última data limite selecionada", async () => {
        const wrapper = mount(ModalAcaoBloco, {
            props: {
                ...propsPadrao,
                unidades: [
                    {
                        codigo: 1,
                        sigla: 'U1',
                        nome: 'Unidade 1',
                        situacao: 'OK',
                        ultimaDataLimite: '2099-01-10T00:00:00'
                    }
                ],
                unidadesPreSelecionadas: [1]
            }
        });

        const input = wrapper.find('[data-testid="inp-data-limite-bloco"]');
        await input.setValue("2099-01-09");

        expect(wrapper.text()).toContain("A data limite deve ser maior ou igual à última data limite das unidades selecionadas.");
    });

    it('exercita o computed todosSelecionados', async () => {
        const wrapper = mount(ModalAcaoBloco, {
            props: {
                ...propsPadrao,
                unidades: [
                    {codigo: 1, sigla: 'U1', nome: 'Unidade 1', situacao: 'ATIVO' as any},
                    {codigo: 2, sigla: 'U2', nome: 'Unidade 2', situacao: 'ATIVO' as any}
                ],
                unidadesPreSelecionadas: [1]
            }
        });
        
        const vm = wrapper.vm as any;
        expect(vm.todosSelecionados).toBe(false);

        // Define para true
        vm.todosSelecionados = true;
        expect(vm.selecionadosLocal).toEqual([1, 2]);
        expect(vm.todosSelecionados).toBe(true);

        // Define para false
        vm.todosSelecionados = false;
        expect(vm.selecionadosLocal).toEqual([]);
        expect(vm.todosSelecionados).toBe(false);
    });

    it('exercita o computed campos com mostrarSituacao false', () => {
        const wrapper = mount(ModalAcaoBloco, {
            props: {
                ...propsPadrao,
                mostrarSituacao: false
            }
        });
        const vm = wrapper.vm as any;
        expect(vm.campos).toHaveLength(2); // selecao, sigla
    });

    it('exercita permitirVazio true e metodos expostos', async () => {
        const wrapper = mount(ModalAcaoBloco, {
            props: {
                ...propsPadrao,
                unidadesPreSelecionadas: [],
                permitirVazio: true,
                mostrarDataLimite: false
            }
        });
        const vm = wrapper.vm as any;
        
        vm.abrir();
        await wrapper.vm.$nextTick();

        // Como permitirVazio é true, o formulário deve ser válido e confirmar
        await vm.confirmar();
        expect(wrapper.emitted("confirmar")?.[0]).toEqual([{ids: [], dataLimite: undefined}]);

        // Testa metodos expostos
        vm.setProcessando(true);
        expect(vm.processando).toBe(true);

        vm.setErro('Erro customizado');
        expect(vm.erro).toBe('Erro customizado');

        vm.fechar();
        expect(vm.mostrar).toBe(false);
        expect(vm.processando).toBe(false);
        expect(vm.erro).toBeNull();
    });

    it('exercita extrairData com falhas e datas curtas', () => {
        const wrapper = mount(ModalAcaoBloco, {
            props: {
                ...propsPadrao,
                unidades: [
                    {codigo: 1, sigla: 'U1', nome: 'Unidade 1', ultimaDataLimite: undefined, situacao: 'ATIVO' as any},
                    {codigo: 2, sigla: 'U2', nome: 'Unidade 2', ultimaDataLimite: '', situacao: 'ATIVO' as any}
                ],
                unidadesPreSelecionadas: [1, 2]
            }
        });
        // ultimaDataLimiteSelecionada deve ignorar e retornar '' se nao tiver data valida
        const vm = wrapper.vm as any;
        expect(vm.ultimaDataLimiteSelecionada).toBe("");
        
        // extrairData com length invalida (menos de 10) testa watch short circuit
        vm.dataLimite = "202";
        expect(vm.erroLocalDataLimite).toBe(""); // Retorna cedo
    });
});
