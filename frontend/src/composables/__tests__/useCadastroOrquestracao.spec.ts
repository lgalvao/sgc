import {beforeEach, describe, expect, it, vi} from "vitest";
import {ref} from "vue";
import {useCadastroOrquestracao} from "../useCadastroOrquestracao";
import {PERMISSOES_SUBPROCESSO_VAZIAS} from "@/utils/permissoesSubprocesso";
import * as formatters from "@/utils/formatters";

const storeMock = {
    garantirContextoCadastroAtividades: vi.fn(),
    garantirContextoCadastroAtividadesPorProcessoEUnidade: vi.fn(),
    atualizarStatusLocal: vi.fn(),
};

vi.mock("@/stores/subprocesso", () => ({
    useSubprocessoStore: () => storeMock
}));

describe("useCadastroOrquestracao", () => {
    const atividades = ref<any[]>([]);
    const props = {codProcesso: 1, sigla: "U"};

    beforeEach(() => {
        vi.clearAllMocks();
        atividades.value = [];
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

        const success = await carregarContextoInicial();

        expect(success).toBe(false);
    });
});
