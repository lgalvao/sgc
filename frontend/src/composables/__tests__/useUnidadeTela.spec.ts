import {beforeEach, describe, expect, it, vi} from "vitest";
import {ref, watch, nextTick} from "vue";
import {useUnidadeTela} from "../useUnidadeTela";
import {relatoriosService} from "@/services/relatoriosService";
import {TEXTOS} from "@/constants/textos";
import {TEXTOS_RELATORIOS} from "@/constants/textos-relatorios";
import {Perfil} from "@/types/comum";

const pushMock = vi.fn();
vi.mock("vue-router", () => ({
    useRouter: () => ({
        push: pushMock,
    }),
}));

const mockMostrarCriarAtribuicaoTemporaria = ref(true);
vi.mock("@/composables/usePerfil", () => ({
    usePerfil: () => ({
        mostrarCriarAtribuicaoTemporaria: mockMostrarCriarAtribuicaoTemporaria,
    }),
}));

const mockNotify = vi.fn();
vi.mock("@/composables/useNotification", () => ({
    useNotification: () => ({
        notify: mockNotify,
    }),
}));

const mockDefinirUnidadeAtual = vi.fn();
vi.mock("@/composables/useUnidadeAtual", () => ({
    useUnidadeAtual: () => ({
        definirUnidadeAtual: mockDefinirUnidadeAtual,
    }),
}));

const mockPerfilStore = {
    perfilSelecionado: Perfil.SERVIDOR,
};
vi.mock("@/stores/perfil", () => ({
    usePerfilStore: () => mockPerfilStore,
}));

vi.mock("@/services/relatoriosService", () => ({
    relatoriosService: {
        downloadRelatorioMapaVigenteUnidadePdf: vi.fn(),
        downloadRelatorioMapaVigenteUnidadeCsv: vi.fn(),
    },
}));

const mockQueryData = ref<any>(null);
const mockQueryPending = ref(false);
const mockQueryLoading = ref(false);
const mockQueryError = ref<Error | null>(null);

vi.mock("@/composables/useUnidadeQuery", () => ({
    useDadosTelaUnidadeQuery: vi.fn(() => ({
        data: mockQueryData,
        isPending: mockQueryPending,
        isLoading: mockQueryLoading,
        error: mockQueryError,
    })),
}));

describe("useUnidadeTela", () => {
    beforeEach(() => {
        vi.clearAllMocks();
        mockQueryData.value = null;
        mockQueryPending.value = false;
        mockQueryLoading.value = false;
        mockQueryError.value = null;
        mockPerfilStore.perfilSelecionado = Perfil.SERVIDOR;
    });

    it("deve inicializar com valores padrão corretos", () => {
        const {
            unidade,
            mapaVigente,
            carregandoPagina,
            ultimoErro,
            podeExportarMapaVigente,
            textoBotaoAtribuicao,
            titularExibivel,
            responsavelExibivel,
            labelContatoPrincipal,
            descricaoContatoPrincipal,
            temSubordinadas,
        } = useUnidadeTela({codUnidade: 10});

        expect(unidade.value).toBeNull();
        expect(mapaVigente.value).toBeNull();
        expect(carregandoPagina.value).toBe(false);
        expect(ultimoErro.value).toBeNull();
        expect(podeExportarMapaVigente.value).toBe(false);
        expect(textoBotaoAtribuicao.value).toBe(TEXTOS.unidade.BOTAO_CRIAR_ATRIBUICAO);
        expect(titularExibivel.value).toBe(false);
        expect(responsavelExibivel.value).toBeNull();
        expect(labelContatoPrincipal.value).toBe(TEXTOS.unidade.LABEL_RESPONSAVEL);
        expect(descricaoContatoPrincipal.value).toBe("Titular");
        expect(temSubordinadas.value).toBe(false);
    });

    it("deve mapear os dados da unidade e do mapa vigente quando a query resolve com sucesso", () => {
        mockQueryData.value = {
            unidade: {
                codigo: 10,
                nome: "Unidade Teste",
                sigla: "UT",
                filhas: [{codigo: 11, nome: "Filha 1", sigla: "F1", filhas: []}],
                responsavel: {tituloEleitoral: "111", nome: "Responsavel"},
                titular: {tituloEleitoral: "222", nome: "Titular"},
                tipoResponsabilidade: "SUBSTITUTO",
                dataFimResponsabilidade: "2026-12-31",
            },
            mapaVigente: {codigo: 99, dataInicio: "2026-01-01"},
        };

        const {
            unidade,
            mapaVigente,
            temSubordinadas,
            dadosFormatadosSubordinadas,
            responsavelExibivel,
            titularExibivel,
            labelContatoPrincipal,
            descricaoContatoPrincipal,
        } = useUnidadeTela({codUnidade: 10});

        expect(unidade.value).toEqual(mockQueryData.value.unidade);
        expect(mapaVigente.value).toEqual(mockQueryData.value.mapaVigente);
        expect(mockDefinirUnidadeAtual).toHaveBeenCalledWith(mockQueryData.value.unidade);
        expect(temSubordinadas.value).toBe(true);
        expect(dadosFormatadosSubordinadas.value).toEqual([
            {codigo: 11, nome: "Filha 1", sigla: "F1", expanded: false},
        ]);
        expect(responsavelExibivel.value).toEqual(mockQueryData.value.unidade.responsavel);
        expect(titularExibivel.value).toBe(true);
        expect(labelContatoPrincipal.value).toBe(TEXTOS.unidade.LABEL_RESPONSAVEL);
        expect(descricaoContatoPrincipal.value).toBe("Substituição (até 31/12/2026)");
    });

    it("deve lidar com substituto sem data de fim de responsabilidade", () => {
        mockQueryData.value = {
            unidade: {
                codigo: 10,
                responsavel: {tituloEleitoral: "111", nome: "Responsavel"},
                titular: {tituloEleitoral: "222", nome: "Titular"},
                tipoResponsabilidade: "SUBSTITUTO",
                dataFimResponsabilidade: undefined,
            },
        };

        const {descricaoContatoPrincipal} = useUnidadeTela({codUnidade: 10});
        expect(descricaoContatoPrincipal.value).toBe("Substituição");
    });

    it("deve lidar com atribuicao temporaria com e sem data de fim de responsabilidade", () => {
        mockQueryData.value = {
            unidade: {
                codigo: 10,
                responsavel: {tituloEleitoral: "111", nome: "Responsavel"},
                titular: {tituloEleitoral: "222", nome: "Titular"},
                tipoResponsabilidade: "ATRIBUICAO_TEMPORARIA",
                dataFimResponsabilidade: "2026-12-31",
            },
        };

        const {descricaoContatoPrincipal, textoBotaoAtribuicao} = useUnidadeTela({codUnidade: 10});
        expect(descricaoContatoPrincipal.value).toBe("Atrib. temporária (até 31/12/2026)");
        expect(textoBotaoAtribuicao.value).toBe(TEXTOS.unidade.BOTAO_EDITAR_ATRIBUICAO);

        mockQueryData.value.unidade.dataFimResponsabilidade = undefined;
        const {descricaoContatoPrincipal: desc2} = useUnidadeTela({codUnidade: 10});
        expect(desc2.value).toBe("Atrib. temporária");
    });

    it("deve formatar contato principal quando responsavel e titular são a mesma pessoa", () => {
        mockQueryData.value = {
            unidade: {
                codigo: 10,
                responsavel: {tituloEleitoral: "111", nome: "Mesmo"},
                titular: {tituloEleitoral: "111", nome: "Mesmo"},
                tipoResponsabilidade: "TITULAR",
            },
        };

        const {titularExibivel, labelContatoPrincipal, descricaoContatoPrincipal} = useUnidadeTela({codUnidade: 10});
        expect(titularExibivel.value).toBe(false);
        expect(labelContatoPrincipal.value).toBe(TEXTOS.unidade.LABEL_TITULAR);
        expect(descricaoContatoPrincipal.value).toBe("");
    });

    it("deve expor titularExibivel false se responsavel estiver ausente mas titular presente (titular vira responsavelPrincipal)", () => {
        mockQueryData.value = {
            unidade: {
                codigo: 10,
                responsavel: null,
                titular: {tituloEleitoral: "222", nome: "Titular"},
            },
        };

        const {titularExibivel} = useUnidadeTela({codUnidade: 10});
        expect(titularExibivel.value).toBe(false);
    });

    it("deve exportar mapa pdf com sucesso", async () => {
        mockQueryData.value = {
            mapaVigente: {codigo: 99},
        };
        vi.mocked(relatoriosService.downloadRelatorioMapaVigenteUnidadePdf).mockResolvedValue(undefined);

        const {exportarMapaVigentePdf, loadingExportacaoPdf} = useUnidadeTela({codUnidade: 10});
        
        const promessa = exportarMapaVigentePdf();
        expect(loadingExportacaoPdf.value).toBe(true);
        await promessa;
        expect(loadingExportacaoPdf.value).toBe(false);
        expect(relatoriosService.downloadRelatorioMapaVigenteUnidadePdf).toHaveBeenCalledWith(10);
    });

    it("deve lidar com erro na exportacao pdf", async () => {
        mockQueryData.value = {
            mapaVigente: {codigo: 99},
        };
        vi.mocked(relatoriosService.downloadRelatorioMapaVigenteUnidadePdf).mockRejectedValue(new Error("Erro PDF"));

        const {exportarMapaVigentePdf, loadingExportacaoPdf} = useUnidadeTela({codUnidade: 10});
        
        await exportarMapaVigentePdf();
        expect(loadingExportacaoPdf.value).toBe(false);
        expect(mockNotify).toHaveBeenCalledWith(TEXTOS_RELATORIOS.ERRO_EXPORTAR, "danger");
    });

    it("deve ignorar exportacao pdf se nao houver mapa vigente", async () => {
        const {exportarMapaVigentePdf} = useUnidadeTela({codUnidade: 10});
        await exportarMapaVigentePdf();
        expect(relatoriosService.downloadRelatorioMapaVigenteUnidadePdf).not.toHaveBeenCalled();
    });

    it("deve exportar mapa csv com sucesso", async () => {
        mockQueryData.value = {
            mapaVigente: {codigo: 99},
        };
        vi.mocked(relatoriosService.downloadRelatorioMapaVigenteUnidadeCsv).mockResolvedValue(undefined);

        const {exportarMapaVigenteCsv, loadingExportacaoCsv} = useUnidadeTela({codUnidade: 10});
        
        const promessa = exportarMapaVigenteCsv();
        expect(loadingExportacaoCsv.value).toBe(true);
        await promessa;
        expect(loadingExportacaoCsv.value).toBe(false);
        expect(relatoriosService.downloadRelatorioMapaVigenteUnidadeCsv).toHaveBeenCalledWith(10);
    });

    it("deve lidar com erro na exportacao csv", async () => {
        mockQueryData.value = {
            mapaVigente: {codigo: 99},
        };
        vi.mocked(relatoriosService.downloadRelatorioMapaVigenteUnidadeCsv).mockRejectedValue(new Error("Erro CSV"));

        const {exportarMapaVigenteCsv, loadingExportacaoCsv} = useUnidadeTela({codUnidade: 10});
        
        await exportarMapaVigenteCsv();
        expect(loadingExportacaoCsv.value).toBe(false);
        expect(mockNotify).toHaveBeenCalledWith(TEXTOS_RELATORIOS.ERRO_EXPORTAR_CSV, "danger");
    });

    it("deve ignorar exportacao csv se nao houver mapa vigente", async () => {
        const {exportarMapaVigenteCsv} = useUnidadeTela({codUnidade: 10});
        await exportarMapaVigenteCsv();
        expect(relatoriosService.downloadRelatorioMapaVigenteUnidadeCsv).not.toHaveBeenCalled();
    });

    it("podeExportarMapaVigente deve ser true se perfil selecionado for CHEFE e houver mapa vigente", () => {
        mockQueryData.value = {
            mapaVigente: {codigo: 99},
        };
        mockPerfilStore.perfilSelecionado = Perfil.CHEFE;

        const {podeExportarMapaVigente} = useUnidadeTela({codUnidade: 10});
        expect(podeExportarMapaVigente.value).toBe(true);
    });

    it("deve navegar para criar atribuicao e para subordinada", () => {
        const {irParaCriarAtribuicao, navegarParaUnidadeSubordinada} = useUnidadeTela({codUnidade: 10});
        
        irParaCriarAtribuicao();
        expect(pushMock).toHaveBeenCalledWith({path: "/unidade/10/atribuicao"});

        navegarParaUnidadeSubordinada({codigo: 15, nome: "Sub 15", expanded: false});
        expect(pushMock).toHaveBeenCalledWith({path: "/unidade/15"});
    });

    it("deve tratar o erro da query e dispensar", () => {
        const error = new Error("Erro de rede");
        mockQueryError.value = error;

        const {ultimoErro, erroDispensado} = useUnidadeTela({codUnidade: 10});
        expect(ultimoErro.value).toBe("Erro de rede");
        expect(erroDispensado.value).toBe(false);

        erroDispensado.value = true;
        expect(ultimoErro.value).toBeNull();
    });

    it("deve monitorar e redefinir erroDispensado se houver novo erro", async () => {
        const error1 = new Error("Erro 1");
        mockQueryError.value = error1;

        const {ultimoErro, erroDispensado} = useUnidadeTela({codUnidade: 10});
        expect(ultimoErro.value).toBe("Erro 1");

        erroDispensado.value = true;
        expect(ultimoErro.value).toBeNull();

        // Novo erro deve redefinir erroDispensado para false
        mockQueryError.value = new Error("Erro 2");
        await nextTick();

        expect(erroDispensado.value).toBe(false);
        expect(ultimoErro.value).toBe("Erro 2");
    });
});
