import {beforeEach, describe, expect, it, vi} from "vitest";
import {createPinia, setActivePinia} from "pinia";
import {useSubprocessoStore} from "../subprocesso";
import * as subprocessoService from "@/services/subprocessoService";
import {PERMISSOES_SUBPROCESSO_VAZIAS} from "@/utils/permissoesSubprocesso";

vi.mock("@/services/subprocessoService", () => ({
    buscarSubprocessoDetalhe: vi.fn(),
    buscarContextoEdicao: vi.fn(),
    buscarContextoCadastroAtividades: vi.fn(),
    buscarSubprocessoPorProcessoEUnidade: vi.fn(),
    buscarContextoEdicaoPorProcessoEUnidade: vi.fn(),
    buscarContextoCadastroAtividadesPorProcessoEUnidade: vi.fn(),
}));

describe("subprocesso store (cache e dedupe)", () => {
    beforeEach(() => {
        setActivePinia(createPinia());
        vi.clearAllMocks();
    });

    it("deve inicializar com estado vazio", () => {
        const store = useSubprocessoStore();
        expect(store.contextoEdicao).toBeNull();
        expect(store.contextoCadastro).toBeNull();
        expect(store.erroIntegracaoContexto).toBeNull();
    });

    describe("limpeza de contexto", () => {
        it("deve limpar contexto atual", () => {
            const store = useSubprocessoStore();
            store.contextoEdicao = {detalhes: {codigo: 1}} as any;
            store.contextoCadastro = {detalhes: {codigo: 2}} as any;
            store.limparContextoAtual();
            expect(store.contextoEdicao).toBeNull();
            expect(store.contextoCadastro).toBeNull();
        });
    });

    describe("deduplicação e sincronização", () => {
        it("deve deduplicar requisições em paralelo para o mesmo código", async () => {
            const store = useSubprocessoStore();
            let resolver!: (valor: any) => void;
            const promessa = new Promise<any>((resolve) => {
                resolver = resolve;
            });
            vi.mocked(subprocessoService.buscarContextoEdicao).mockReturnValue(promessa);

            const req1 = store.garantirContextoEdicao(10);
            const req2 = store.garantirContextoEdicao(10);

            expect(subprocessoService.buscarContextoEdicao).toHaveBeenCalledTimes(1);
            resolver({detalhes: {codigo: 10, situacao: "TESTE"}, mapa: null});

            const res1 = await req1;
            const res2 = await req2;
            expect(res1).toBe(res2);
            expect(store.contextoEdicao?.detalhes.codigo).toBe(10);
        });

        it("deve sincronizar detalhes ao carregar contexto de cadastro", async () => {
            const store = useSubprocessoStore();
            const mockCadastro = {detalhes: {codigo: 10, situacao: "CADASTRO"}, atividades: []};
            vi.mocked(subprocessoService.buscarContextoCadastroAtividades).mockResolvedValue(mockCadastro as any);

            await store.garantirContextoCadastroAtividades(10);

            expect(store.contextoCadastro?.detalhes.codigo).toBe(10);
            expect(store.contextoCadastro?.detalhes.situacao).toBe("CADASTRO");
            expect(store.contextoCadastro).toEqual(mockCadastro);
        });

        it("deve sincronizar detalhes ao carregar contexto de edicao", async () => {
            const store = useSubprocessoStore();
            const mockMapa = {detalhes: {codigo: 10, situacao: "MAPA"}, mapa: {}};
            vi.mocked(subprocessoService.buscarContextoEdicao).mockResolvedValue(mockMapa as any);

            await store.garantirContextoEdicao(10);

            expect(store.contextoEdicao?.detalhes.codigo).toBe(10);
            expect(store.contextoEdicao?.detalhes.situacao).toBe("MAPA");
            expect(store.contextoEdicao).toEqual(mockMapa);
        });

        it("deve garantir contexto de edicao por processo e unidade", async () => {
            const store = useSubprocessoStore();
            const mockMapa = {detalhes: {codigo: 10, situacao: "MAPA"}, mapa: {}};
            vi.mocked(subprocessoService.buscarContextoEdicaoPorProcessoEUnidade).mockResolvedValue(mockMapa as any);

            const result = await store.garantirContextoEdicaoPorProcessoEUnidade(1, "UNIDADE");

            expect(result?.codigo).toBe(10);
            expect(result?.contexto).toEqual(mockMapa);
            expect(store.contextoEdicao).toEqual(mockMapa);
            expect(subprocessoService.buscarContextoEdicaoPorProcessoEUnidade).toHaveBeenCalledWith(1, "UNIDADE");

            // Cache hit no mapeamento, mas contextoEdicao já é o correto
            await store.garantirContextoEdicaoPorProcessoEUnidade(1, "UNIDADE");
            expect(subprocessoService.buscarContextoEdicao).not.toHaveBeenCalled();

            // Cache hit no mapeamento, mas contextoEdicao é nulo ou diferente
            store.limparContextoAtual();
            vi.mocked(subprocessoService.buscarContextoEdicao).mockResolvedValue(mockMapa as any);
            await store.garantirContextoEdicaoPorProcessoEUnidade(1, "UNIDADE");
            expect(subprocessoService.buscarContextoEdicao).toHaveBeenCalledWith(10);
        });

        it("deve garantir contexto de cadastro por processo e unidade", async () => {
            const store = useSubprocessoStore();
            const mockCadastro = {detalhes: {codigo: 10, situacao: "CADASTRO"}, atividades: []};
            vi.mocked(subprocessoService.buscarContextoCadastroAtividadesPorProcessoEUnidade).mockResolvedValue(mockCadastro as any);

            const result = await store.garantirContextoCadastroAtividadesPorProcessoEUnidade(1, "UNIDADE");

            expect(result).toEqual(mockCadastro);
            expect(store.contextoCadastro).toEqual(mockCadastro);
            expect(subprocessoService.buscarContextoCadastroAtividadesPorProcessoEUnidade).toHaveBeenCalledWith(1, "UNIDADE");

            // Cache hit no mapeamento, mas contextoCadastro já é o correto
            await store.garantirContextoCadastroAtividadesPorProcessoEUnidade(1, "UNIDADE");
            expect(subprocessoService.buscarContextoCadastroAtividades).not.toHaveBeenCalled();

            // Cache hit no mapeamento, mas contextoCadastro é nulo ou diferente
            store.limparContextoAtual();
            vi.mocked(subprocessoService.buscarContextoCadastroAtividades).mockResolvedValue(mockCadastro as any);
            await store.garantirContextoCadastroAtividadesPorProcessoEUnidade(1, "UNIDADE");
            expect(subprocessoService.buscarContextoCadastroAtividades).toHaveBeenCalledWith(10);
        });
    });

    describe("atualização de status", () => {
        it("deve atualizar status local de edicao", () => {
            const store = useSubprocessoStore();
            store.contextoEdicao = {detalhes: {codigo: 10, situacao: "ANTIGA"}} as any;

            store.atualizarStatusLocal({codigo: 10, situacao: "NOVA" as any});

            expect(store.contextoEdicao?.detalhes.situacao).toBe("NOVA");
        });

        it("deve atualizar status local de cadastro", () => {
            const store = useSubprocessoStore();
            store.contextoCadastro = {detalhes: {codigo: 10, situacao: "ANTIGA"}} as any;

            store.atualizarStatusLocal({codigo: 10, situacao: "NOVA" as any});

            expect(store.contextoCadastro?.detalhes.situacao).toBe("NOVA");
        });

        it("deve atualizar permissoes se fornecidas", () => {
            const store = useSubprocessoStore();
            store.contextoEdicao = {
                detalhes: {
                    codigo: 10,
                    situacao: "ANTIGA",
                    permissoes: PERMISSOES_SUBPROCESSO_VAZIAS
                }
            } as any;
            const novasPermissoes = {habilitarEditar: true} as any;

            store.atualizarStatusLocal({codigo: 10, situacao: "NOVA" as any, permissoes: novasPermissoes});

            expect(store.contextoEdicao?.detalhes.permissoes).toEqual(novasPermissoes);
        });

        it("deve atualizar permissoes no contexto de cadastro se fornecidas", () => {
            const store = useSubprocessoStore();
            store.contextoCadastro = {
                detalhes: {
                    codigo: 10,
                    situacao: "ANTIGA",
                    permissoes: PERMISSOES_SUBPROCESSO_VAZIAS
                }
            } as any;
            const novasPermissoes = {habilitarEditar: true} as any;

            store.atualizarStatusLocal({codigo: 10, situacao: "NOVA" as any, permissoes: novasPermissoes});

            expect(store.contextoCadastro?.detalhes.permissoes).toEqual(novasPermissoes);
        });
    });

    describe("validação de dados", () => {
        it("deve validar dados de edicao", () => {
            const store = useSubprocessoStore();
            store.contextoEdicao = {detalhes: {codigo: 10}} as any;
            store.invalidar();
            expect(store.contextoEdicao?.detalhes.codigo).toBe(10);
        });

        it("deve validar dados de cadastro", () => {
            const store = useSubprocessoStore();
            store.contextoCadastro = {detalhes: {codigo: 10}} as any;
            expect(store.contextoCadastro?.detalhes.codigo).toBe(10);
        });
    });

    describe("tratamento de erros", () => {
        it("deve registrar erro de integração ao falhar carregar edicao", async () => {
            const store = useSubprocessoStore();
            vi.mocked(subprocessoService.buscarContextoEdicao).mockRejectedValue(new Error("Erro de rede"));

            const result = await store.garantirContextoEdicao(10);

            expect(result).toBeNull();
            expect(store.erroIntegracaoContexto).not.toBeNull();
        });

        it("deve tratar erro 404 como erro inesperado com mensagem customizada", async () => {
            const store = useSubprocessoStore();
            const erro404 = {response: {status: 404, data: {mensagem: "Não achei"}}, isAxiosError: true};
            vi.mocked(subprocessoService.buscarContextoEdicao).mockRejectedValue(erro404);

            await store.garantirContextoEdicao(10);

            expect(store.erroIntegracaoContexto?.tipo).toBe("inesperado");
            expect(store.erroIntegracaoContexto?.mensagem).toContain("indica inconsistência interna");
        });

        it("deve tratar erro de request cancelada", async () => {
            const store = useSubprocessoStore();
            const erroCancelado = {code: "ERR_CANCELED", isAxiosError: true};
            vi.mocked(subprocessoService.buscarContextoEdicao).mockRejectedValue(erroCancelado);

            await store.garantirContextoEdicao(10);

            expect(store.erroIntegracaoContexto?.codigo).toBe("REQUEST_CANCELADA");
        });

        it("deve propagar erro genérico", async () => {
            const store = useSubprocessoStore();
            vi.mocked(subprocessoService.buscarContextoEdicao).mockRejectedValue(new Error("Erro genérico"));

            await store.garantirContextoEdicao(10);

            expect(store.erroIntegracaoContexto?.mensagem).toBe("Erro genérico");
        });

        it("deve limpar erro de integração", () => {
            const store = useSubprocessoStore();
            store.erroIntegracaoContexto = {mensagem: "Erro"} as any;
            store.limparErroIntegracao();
            expect(store.erroIntegracaoContexto).toBeNull();
        });

        it("deve tratar erro ao buscar por processo e unidade (edicao)", async () => {
            const store = useSubprocessoStore();
            vi.mocked(subprocessoService.buscarContextoEdicaoPorProcessoEUnidade).mockRejectedValue(new Error("Erro busca"));

            const result = await store.garantirContextoEdicaoPorProcessoEUnidade(1, "UN");

            expect(result).toBeNull();
            expect(store.erroIntegracaoContexto).not.toBeNull();
        });

        it("deve tratar erro ao buscar por processo e unidade (cadastro)", async () => {
            const store = useSubprocessoStore();
            vi.mocked(subprocessoService.buscarContextoCadastroAtividadesPorProcessoEUnidade).mockRejectedValue(new Error("Erro busca"));

            const result = await store.garantirContextoCadastroAtividadesPorProcessoEUnidade(1, "UN");

            expect(result).toBeNull();
            expect(store.erroIntegracaoContexto).not.toBeNull();
        });
    });

    describe("invalidação", () => {
        it("deve manter o último snapshot, mas marcá-lo como inválido", () => {
            const store = useSubprocessoStore();
            store.contextoEdicao = {detalhes: {codigo: 1}} as any;
            store.contextoCadastro = {detalhes: {codigo: 2}, atividades: []} as any;

            store.invalidar();

            expect(store.contextoEdicao?.detalhes.codigo).toBe(1);
            expect(store.contextoCadastro?.detalhes.codigo).toBe(2);
            expect(store.erroIntegracaoContexto).toBeNull();
        });

        it("deve resetar o estado completo", () => {
            const store = useSubprocessoStore();
            store.contextoEdicao = {detalhes: {codigo: 1}} as any;
            store.erroIntegracaoContexto = {mensagem: "Erro"} as any;

            store.resetar();

            expect(store.contextoEdicao).toBeNull();
            expect(store.erroIntegracaoContexto).toBeNull();
        });
    });
});
