import {beforeEach, describe, expect, it, vi} from "vitest";
import {ref} from "vue";
import {useCadastroOrquestracao} from "../useCadastroOrquestracao";
import {PERMISSOES_SUBPROCESSO_VAZIAS} from "@/utils/permissoesSubprocesso";

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
        const {carregarContextoInicial, codigoSubprocesso} = useCadastroOrquestracao(props, atividades);
        vi.mocked(storeMock.garantirContextoCadastroAtividadesPorProcessoEUnidade).mockResolvedValue({
            detalhes: {codigo: 123, situacao: "S", permissoes: PERMISSOES_SUBPROCESSO_VAZIAS},
            atividadesDisponiveis: [],
            unidade: {sigla: "U"},
            mapa: {codigo: 50}
        } as any);

        const success = await carregarContextoInicial();

        expect(success).toBe(true);
        expect(codigoSubprocesso.value).toBe(123);
        expect(storeMock.garantirContextoCadastroAtividadesPorProcessoEUnidade).toHaveBeenCalledWith(1, "U", false);
    });

    it("deve retornar false quando contexto não for encontrado", async () => {
        const {carregarContextoInicial} = useCadastroOrquestracao(props, atividades);
        vi.mocked(storeMock.garantirContextoCadastroAtividadesPorProcessoEUnidade).mockResolvedValue(null);

        const success = await carregarContextoInicial();

        expect(success).toBe(false);
    });
});
