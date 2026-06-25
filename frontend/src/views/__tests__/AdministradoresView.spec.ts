import {beforeEach, describe, expect, it, vi} from "vitest";
import {flushPromises, mount} from "@vue/test-utils";
import AdministradoresView from "@/views/AdministradoresView.vue";
import * as administradorService from "@/services/administradorService";
import {createTestingPinia} from "@pinia/testing";
import {TEXTOS} from "@/constants/textos";

vi.mock("@/services/administradorService", () => ({
    listarAdministradores: vi.fn(),
    adicionarAdministrador: vi.fn(),
    removerAdministrador: vi.fn()
}));

describe("AdministradoresView.vue", () => {
    const mockAdmins = [
        {nome: "Admin 1", tituloEleitoral: "111", matricula: "M1", unidadeSigla: "U1", unidadeCodigo: 1},
        {nome: "Admin 2", tituloEleitoral: "222", matricula: "M2", unidadeSigla: "U2", unidadeCodigo: 2}
    ];

    beforeEach(() => {
        vi.clearAllMocks();
        vi.mocked(administradorService.listarAdministradores).mockResolvedValue(mockAdmins);
    });

    const createWrapper = () => {
        return mount(AdministradoresView, {
            global: {
                plugins: [createTestingPinia({createSpy: vi.fn})],
                stubs: {
                    LayoutPadrao: {template: '<div><slot /></div>'},
                    PageHeader: {
                        props: ['title'],
                        template: '<div><h1>{{ title }}</h1><slot name="actions" /></div>'
                    },
                    CarregamentoPagina: {template: '<div data-testid="pagina-carregando"></div>'},
                    BButton: {template: `<button @click="$emit('click')"><slot /></button>`},
                    BAlert: {template: '<div><slot /></div>'},
                    EmptyState: true,
                    BTable: {
                        props: ['items'],
                        template: `
                            <table>
                                <tr v-for="item in items" :key="item.tituloEleitoral">
                                    <td>{{ item.nome }}</td>
                                    <td>{{ item.tituloEleitoral }}</td>
                                    <td><slot name="cell(acoes)" :item="item" /></td>
                                </tr>
                            </table>
                        `
                    },
                    AdministradoresFluxoModais: {
                        name: 'AdministradoresFluxoModais',
                        props: ['mostrarModalAdicionarAdmin', 'mostrarModalRemoverAdmin', 'termoUsuario'],
                        template: `
                          <div>
                            <div v-if="mostrarModalAdicionarAdmin">
                              <input class="buscador-input" :value="termoUsuario" @input="$emit('update:termoUsuario', $event.target.value)" />
                              <button class="confirm" @click="$emit('adicionarAdmin')">OK</button>
                            </div>
                            <div v-if="mostrarModalRemoverAdmin">
                              <button class="confirm" @click="$emit('removerAdmin')">OK</button>
                            </div>
                          </div>`,
                        emits: ['adicionarAdmin', 'modalAdicionarExibido', 'removerAdmin', 'update:mostrarModalAdicionarAdmin', 'update:mostrarModalRemoverAdmin', 'update:termoUsuario', 'update:usuarioSelecionado']
                    },
                    LoadingButton: {
                        props: ['loading', 'text', 'icon'],
                        template: `<button class="loading-btn" @click="$emit('click')">{{ text }}<i v-if="icon" :class="'bi bi-' + icon"></i><slot /></button>`,
                        emits: ['click']
                    },
                }
            }
        });
    };

    it("deve carregar e exibir administradores", async () => {
        const wrapper = createWrapper();

        expect(wrapper.find('[data-testid="pagina-carregando"]').exists()).toBe(true);
        await flushPromises();

        expect(wrapper.find('h1').text()).toBe(TEXTOS.administracao.TITULO);
        expect(administradorService.listarAdministradores).toHaveBeenCalled();
        expect(wrapper.text()).toContain("Admin 1");
        expect(wrapper.text()).toContain("Admin 2");
    });

    it("deve tratar erro ao carregar", async () => {
        vi.mocked(administradorService.listarAdministradores).mockRejectedValue(new Error("Erro ao carregar"));
        const wrapper = createWrapper();
        await flushPromises();

        expect(wrapper.text()).toContain("Erro ao carregar");
    });

    it("deve abrir modal e adicionar administrador", async () => {
        const wrapper = createWrapper();
        await flushPromises();

        const addButton = wrapper.find('button[data-testid="btn-abrir-modal-add-admin"]');
        await addButton.trigger("click");

        const input = wrapper.find('input.buscador-input');
        await input.setValue("333");

        vi.mocked(administradorService.adicionarAdministrador).mockResolvedValue({} as any);

        await wrapper.find('button.confirm').trigger("click");
        await flushPromises();

        expect(administradorService.adicionarAdministrador).toHaveBeenCalledWith("333");
    });

    it("deve remover administrador", async () => {
        const wrapper = createWrapper();
        await flushPromises();

        // Encontra o botão de lixeira (LoadingButton com icon="trash")
        const removeBtn = wrapper.find('button.loading-btn i.bi-trash').element.parentElement;
        await (removeBtn as HTMLElement).click();

        vi.mocked(administradorService.removerAdministrador).mockResolvedValue({} as any);

        await wrapper.find('button.confirm').trigger("click");
        await flushPromises();

        expect(administradorService.removerAdministrador).toHaveBeenCalledWith("111");
    });

    it("deve gerenciar estados de modais e fluxos de erro", async () => {
        const wrapper = createWrapper();
        await flushPromises();
        const vm = wrapper.vm as any;

        // Abre modal
        await wrapper.find('button[data-testid="btn-abrir-modal-add-admin"]').trigger("click");
        expect(vm.mostrarModalAdicionarAdmin).toBe(true);

        // Tentativa de adicionar administrador com termo vazio
        vm.termoUsuario = "";
        await vm.adicionarAdmin();

        // Tratamento de erro na adição de administrador
        vm.termoUsuario = "123";
        vi.mocked(administradorService.adicionarAdministrador).mockRejectedValue(new Error("Erro add"));
        await vm.adicionarAdmin();
        expect(vm.erroAdicionarAdmin).toBe("Erro add");

        // Tratamento de erro na remoção de administrador
        vm.adminParaRemover = {tituloEleitoral: "111", nome: "Admin"};
        vi.mocked(administradorService.removerAdministrador).mockRejectedValue(new Error("Erro rem"));
        await vm.removerAdmin();
        expect(vm.erroRemoverAdmin).toBe("Erro rem");
    });
});
