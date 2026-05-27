import {beforeEach, describe, expect, it, vi} from "vitest";
import {createPinia, setActivePinia} from "pinia";
import {createApp} from "vue";
import {PiniaColada} from "@pinia/colada";
import {useRelatorioUnidadesComMapaQuery} from "../useRelatorioMapasQuery";
import * as unidadeService from "@/services/unidadeService";
import {Perfil} from "@/types/tipos";

vi.mock("@/services/unidadeService", () => ({
    buscarTodasUnidades: vi.fn(),
    buscarCodigosUnidadesComMapaVigente: vi.fn(),
}));

const mockPerfilStore = {
    usuarioCodigo: "123",
    perfilSelecionado: Perfil.ADMIN as Perfil | null,
    unidadeSelecionada: null as number | null,
};

vi.mock("@/stores/perfil", () => ({
    usePerfilStore: () => mockPerfilStore,
}));

function withSetup<T>(composable: () => T) {
    let result: T;
    const app = createApp({
        setup() {
            result = composable();
            return () => {};
        },
    });
    const pinia = createPinia();
    app.use(pinia);
    app.use(PiniaColada);
    app.mount(document.createElement("div"));
    return [result!, app] as const;
}

describe("useRelatorioMapasQuery", () => {
    const mockUnidades = [
        {
            codigo: 999,
            sigla: "ADMIN",
            nome: "Administração",
            filhas: [
                {
                    codigo: 1,
                    sigla: "SA",
                    nome: "Secretaria A",
                    filhas: [
                        {
                            codigo: 10,
                            sigla: "COSIS",
                            nome: "Coordenadoria de Sistemas",
                            filhas: []
                        },
                        {
                            codigo: 11,
                            sigla: "COAUD",
                            nome: "Coordenadoria de Auditoria",
                            filhas: []
                        }
                    ]
                }
            ]
        }
    ];

    beforeEach(() => {
        vi.clearAllMocks();
        const pinia = createPinia();
        setActivePinia(pinia);
        mockPerfilStore.usuarioCodigo = "123";
        mockPerfilStore.perfilSelecionado = Perfil.ADMIN;
        mockPerfilStore.unidadeSelecionada = null;
    });

    it("deve carregar unidades elegíveis e aplicar filtros de mapa vigente", async () => {
        vi.mocked(unidadeService.buscarTodasUnidades).mockResolvedValue(mockUnidades as any);
        vi.mocked(unidadeService.buscarCodigosUnidadesComMapaVigente).mockResolvedValue([10]);

        const [query, app] = withSetup(() => useRelatorioUnidadesComMapaQuery());

        const res = await query.refetch();

        expect(unidadeService.buscarTodasUnidades).toHaveBeenCalled();
        expect(unidadeService.buscarCodigosUnidadesComMapaVigente).toHaveBeenCalled();

        const data = res.data!;
        expect(data).toHaveLength(1);
        expect(data[0].sigla).toBe("ADMIN");
        expect(data[0].isElegivel).toBe(false);
        expect(data[0].filhas?.[0].sigla).toBe("SA");
        expect(data[0].filhas?.[0].filhas).toHaveLength(1);
        expect(data[0].filhas?.[0].filhas?.[0].sigla).toBe("COSIS");
        expect(data[0].filhas?.[0].filhas?.[0].isElegivel).toBe(true);

        app.unmount();
    });

    it("deve restringir a árvore à unidade ativa e subordinadas quando perfil for GESTOR", async () => {
        mockPerfilStore.perfilSelecionado = Perfil.GESTOR;
        mockPerfilStore.unidadeSelecionada = 1;

        vi.mocked(unidadeService.buscarTodasUnidades).mockResolvedValue(mockUnidades as any);
        vi.mocked(unidadeService.buscarCodigosUnidadesComMapaVigente).mockResolvedValue([10]);

        const [query, app] = withSetup(() => useRelatorioUnidadesComMapaQuery());

        const res = await query.refetch();

        const data = res.data!;
        expect(data).toHaveLength(1);
        expect(data[0].sigla).toBe("SA");
        expect(data[0].filhas).toHaveLength(1);
        expect(data[0].filhas?.[0].sigla).toBe("COSIS");

        app.unmount();
    });

    it("deve retornar vazio quando nenhuma unidade possui mapa vigente", async () => {
        vi.mocked(unidadeService.buscarTodasUnidades).mockResolvedValue(mockUnidades as any);
        vi.mocked(unidadeService.buscarCodigosUnidadesComMapaVigente).mockResolvedValue([]);

        const [query, app] = withSetup(() => useRelatorioUnidadesComMapaQuery());

        const res = await query.refetch();

        expect(res.data).toEqual([]);
        app.unmount();
    });
});
