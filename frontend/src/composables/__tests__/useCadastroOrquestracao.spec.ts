import {beforeEach, describe, expect, it, vi} from "vitest";
import {defineComponent, h, ref} from "vue";
import {flushPromises, mount} from "@vue/test-utils";
import {useCadastroOrquestracao} from "../useCadastroOrquestracao";
import {PERMISSOES_SUBPROCESSO_VAZIAS} from "@/utils/permissoesSubprocesso";
import * as formatters from "@/utils/formatters";
import logger from "@/utils/logger";

const storeMock = {
    garantirContextoCadastroAtividades: vi.fn(),
    garantirContextoCadastroAtividadesPorProcessoEUnidade: vi.fn(),
    atualizarStatusLocal: vi.fn(),
    invalidarContextoEdicao: vi.fn(),
    dadosCadastroValidos: vi.fn(),
    erroIntegracaoContexto: null as {codigo?: string} | null,
};

const mapasStoreMock = {
    invalidar: vi.fn(),
};

const painelStoreMock = {
    invalidar: vi.fn(),
};

vi.mock("@/stores/subprocesso", () => ({
    useSubprocessoStore: () => storeMock
}));

vi.mock("@/stores/mapas", () => ({
    useMapasStore: () => mapasStoreMock
}));

vi.mock("@/stores/painel", () => ({
    usePainelStore: () => painelStoreMock
}));

describe("useCadastroOrquestracao", () => {
    const atividades = ref<any[]>([]);
    const props = {codProcesso: 1, sigla: "U"};
    const criarComponenteTeste = (entradaProps = props) => defineComponent({
        setup() {
            return useCadastroOrquestracao(entradaProps, atividades);
        },
        render() {
            return h("div");
        },
    });

    beforeEach(() => {
        vi.clearAllMocks();
        atividades.value = [];
        storeMock.erroIntegracaoContexto = null;
        storeMock.dadosCadastroValidos.mockReturnValue(false);
    });

    it("deve carregar contexto inicial por processo e unidade", async () => {
        const {carregarContextoInicial, codigoSubprocesso, unidade, codMapa, atividadesSnapshotInicial} = useCadastroOrquestracao(props, atividades);
        vi.mocked(storeMock.garantirContextoCadastroAtividadesPorProcessoEUnidade).mockResolvedValue({
            detalhes: {codigo: 123, situacao: "S", permissoes: PERMISSOES_SUBPROCESSO_VAZIAS},
            atividadesDisponiveis: [{codigo: 1, descricao: "Atividade"}],
            unidade: {sigla: "U", nome: "Unidade U"},
            mapa: {codigo: 50}
        } as any);
        vi.spyOn(formatters, "calcularAssinaturaCadastro").mockReturnValue("assinatura-calculada");

        const success = await carregarContextoInicial();

        expect(success).toBe(true);
        expect(codigoSubprocesso.value).toBe(123);
        expect(unidade.value).toEqual(expect.objectContaining({sigla: "U"}));
        expect(codMapa.value).toBe(50);
        expect(atividades.value).toEqual([{codigo: 1, descricao: "Atividade"}]);
        expect(atividadesSnapshotInicial.value).toBe("assinatura-calculada");
        expect(storeMock.atualizarStatusLocal).toHaveBeenCalledWith({
            codigo: 123,
            situacao: "S",
            permissoes: PERMISSOES_SUBPROCESSO_VAZIAS,
        });
        expect(storeMock.garantirContextoCadastroAtividadesPorProcessoEUnidade).toHaveBeenCalledWith(1, "U", false);
    });

    it("deve invalidar caches de mapa e painel relacionados ao processar resposta local", () => {
        const {processarRespostaLocal} = useCadastroOrquestracao(props, atividades);

        processarRespostaLocal({
            subprocesso: {codigo: 123, situacao: "S"} as any,
            permissoes: PERMISSOES_SUBPROCESSO_VAZIAS,
            atividadesAtualizadas: [{codigo: 9, descricao: "Atualizada"}] as any,
        });

        expect(atividades.value).toEqual([{codigo: 9, descricao: "Atualizada"}]);
        expect(mapasStoreMock.invalidar).toHaveBeenCalledWith(123);
        expect(storeMock.invalidarContextoEdicao).toHaveBeenCalledWith(123);
        expect(painelStoreMock.invalidar).toHaveBeenCalledOnce();
    });

    it("deve reaproveitar a assinatura de referência quando ela vier do backend", async () => {
        const {carregarContextoInicial, atividadesSnapshotInicial} = useCadastroOrquestracao(props, atividades);
        const spyAssinatura = vi.spyOn(formatters, "calcularAssinaturaCadastro");
        vi.mocked(storeMock.garantirContextoCadastroAtividadesPorProcessoEUnidade).mockResolvedValue({
            detalhes: {codigo: 123, situacao: "S", permissoes: PERMISSOES_SUBPROCESSO_VAZIAS},
            atividadesDisponiveis: [{codigo: 1, descricao: "Atividade"}],
            unidade: {sigla: "U", nome: "Unidade U"},
            mapa: {codigo: 50},
            assinaturaCadastroReferencia: "assinatura-backend",
        } as any);

        const success = await carregarContextoInicial();

        expect(success).toBe(true);
        expect(atividadesSnapshotInicial.value).toBe("assinatura-backend");
        expect(spyAssinatura).not.toHaveBeenCalled();
    });

    it("deve carregar contexto inicial por codSubprocesso em refresh direto", async () => {
        const {carregarContextoInicial, codigoSubprocesso} = useCadastroOrquestracao({
            codProcesso: 1,
            sigla: "U",
            codSubprocesso: 999,
        }, atividades);
        vi.mocked(storeMock.garantirContextoCadastroAtividades).mockResolvedValue({
            detalhes: {codigo: 999, situacao: "S", permissoes: PERMISSOES_SUBPROCESSO_VAZIAS},
            atividadesDisponiveis: [],
            unidade: {sigla: "U", nome: "Unidade U"},
            mapa: {codigo: 50},
        } as any);

        const success = await carregarContextoInicial();

        expect(success).toBe(true);
        expect(codigoSubprocesso.value).toBe(999);
        expect(storeMock.garantirContextoCadastroAtividades).toHaveBeenCalledWith(999, false);
        expect(storeMock.garantirContextoCadastroAtividadesPorProcessoEUnidade).not.toHaveBeenCalled();
    });

    it("deve retornar false quando contexto não for encontrado", async () => {
        const {carregarContextoInicial} = useCadastroOrquestracao(props, atividades);
        vi.mocked(storeMock.garantirContextoCadastroAtividadesPorProcessoEUnidade).mockResolvedValue(null);
        const loggerSpy = vi.spyOn(logger, "error").mockImplementation(() => logger as never);

        const success = await carregarContextoInicial();

        expect(success).toBe(false);
        expect(loggerSpy).toHaveBeenCalledWith("ERRO: Subprocesso não encontrado!");
    });

    it("deve repetir a carga quando a primeira tentativa for cancelada na transição de sessão", async () => {
        const {carregarContextoInicial, codigoSubprocesso} = useCadastroOrquestracao(props, atividades);
        const loggerSpy = vi.spyOn(logger, "error").mockImplementation(() => logger as never);
        vi.mocked(storeMock.garantirContextoCadastroAtividadesPorProcessoEUnidade)
            .mockImplementationOnce(async () => {
                storeMock.erroIntegracaoContexto = {codigo: "REQUEST_CANCELADA"};
                return null;
            })
            .mockImplementationOnce(async () => {
                storeMock.erroIntegracaoContexto = null;
                return {
                    detalhes: {codigo: 123, situacao: "S", permissoes: PERMISSOES_SUBPROCESSO_VAZIAS},
                    atividadesDisponiveis: [],
                    unidade: {sigla: "U", nome: "Unidade U"},
                    mapa: {codigo: 50},
                } as any;
            });

        const success = await carregarContextoInicial();

        expect(success).toBe(true);
        expect(codigoSubprocesso.value).toBe(123);
        expect(storeMock.garantirContextoCadastroAtividadesPorProcessoEUnidade).toHaveBeenNthCalledWith(1, 1, "U", false);
        expect(storeMock.garantirContextoCadastroAtividadesPorProcessoEUnidade).toHaveBeenNthCalledWith(2, 1, "U", true);
        expect(loggerSpy).not.toHaveBeenCalled();
    });

    it("deve recarregar ao reativar quando o contexto de cadastro estiver inválido", async () => {
        vi.mocked(storeMock.garantirContextoCadastroAtividadesPorProcessoEUnidade).mockResolvedValue({
            detalhes: {codigo: 123, situacao: "S", permissoes: PERMISSOES_SUBPROCESSO_VAZIAS},
            atividadesDisponiveis: [],
            unidade: {sigla: "U", nome: "Unidade U"},
            mapa: {codigo: 50},
        } as any);

        const wrapper = mount(criarComponenteTeste());
        await wrapper.vm.carregarContextoInicial();
        await flushPromises();

        storeMock.garantirContextoCadastroAtividadesPorProcessoEUnidade.mockClear();
        storeMock.dadosCadastroValidos.mockReturnValue(false);

        // @ts-expect-error acesso interno para simular ativação keepAlive
        await wrapper.vm.$.a?.[0]();

        expect(storeMock.garantirContextoCadastroAtividadesPorProcessoEUnidade).toHaveBeenCalledWith(1, "U", false);
    });

    it("não deve recarregar ao reativar quando o contexto de cadastro ainda estiver válido", async () => {
        vi.mocked(storeMock.garantirContextoCadastroAtividadesPorProcessoEUnidade).mockResolvedValue({
            detalhes: {codigo: 123, situacao: "S", permissoes: PERMISSOES_SUBPROCESSO_VAZIAS},
            atividadesDisponiveis: [],
            unidade: {sigla: "U", nome: "Unidade U"},
            mapa: {codigo: 50},
        } as any);

        const wrapper = mount(criarComponenteTeste());
        await wrapper.vm.carregarContextoInicial();
        await flushPromises();

        storeMock.garantirContextoCadastroAtividadesPorProcessoEUnidade.mockClear();
        storeMock.dadosCadastroValidos.mockReturnValue(true);

        // @ts-expect-error acesso interno para simular ativação keepAlive
        await wrapper.vm.$.a?.[0]();

        expect(storeMock.garantirContextoCadastroAtividadesPorProcessoEUnidade).not.toHaveBeenCalled();
    });
});
