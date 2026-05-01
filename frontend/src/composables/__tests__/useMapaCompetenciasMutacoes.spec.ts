import {describe, it, expect, beforeEach, vi} from "vitest";
import {ref, computed} from "vue";
import {useMapaCompetenciasMutacoes} from "../useMapaCompetenciasMutacoes";

describe("useMapaCompetenciasMutacoes", () => {
    const codigoSubprocesso = ref<number | null>(null);
    const competencias = computed(() => []);
    const fluxoMapa = {
        lastError: ref<any>(null),
        clearError: vi.fn(),
        adicionarCompetencia: vi.fn(),
        atualizarCompetencia: vi.fn(),
        removerCompetencia: vi.fn(),
        removerAtividadeDaCompetencia: vi.fn(),
    };
    const notify = vi.fn();
    const clearErrors = vi.fn();
    const aplicarErroNormalizado = vi.fn();
    const sincronizarMapa = vi.fn();

    const setup = () => useMapaCompetenciasMutacoes({
        codigoSubprocesso,
        competencias: competencias as any,
        fluxoMapa,
        notify,
        clearErrors,
        aplicarErroNormalizado,
        sincronizarMapa,
    });

    beforeEach(() => {
        vi.clearAllMocks();
        codigoSubprocesso.value = 123;
    });

    it("deve abrir modal de criação", () => {
        const {abrirModalCriarNovaCompetencia, mostrarModalCriarNovaCompetencia, competenciaSendoEditada} = setup();
        
        abrirModalCriarNovaCompetencia();
        
        expect(mostrarModalCriarNovaCompetencia.value).toBe(true);
        expect(competenciaSendoEditada.value).toBeNull();
        expect(clearErrors).toHaveBeenCalled();
        expect(fluxoMapa.clearError).toHaveBeenCalled();
    });

    it("deve abrir modal de edição", () => {
        const {iniciarEdicaoCompetencia, competenciaSendoEditada} = setup();
        const comp = {codigo: 1} as any;
        
        iniciarEdicaoCompetencia(comp);
        
        expect(competenciaSendoEditada.value).toEqual(comp);
    });

    it("deve adicionar nova competência com sucesso", async () => {
        const {adicionarCompetenciaEFecharModal, loadingCompetencia} = setup();
        const mapaNovo = {codigo: 10} as any;
        fluxoMapa.adicionarCompetencia.mockResolvedValue(mapaNovo);

        await adicionarCompetenciaEFecharModal({descricao: "Nova", atividadesSelecionadas: [1]});

        expect(fluxoMapa.adicionarCompetencia).toHaveBeenCalledWith(123, {
            descricao: "Nova",
            atividadesCodigos: [1]
        });
        expect(sincronizarMapa).toHaveBeenCalledWith(mapaNovo);
        expect(loadingCompetencia.value).toBe(false);
    });

    it("deve atualizar competência existente", async () => {
        const {iniciarEdicaoCompetencia, adicionarCompetenciaEFecharModal} = setup();
        iniciarEdicaoCompetencia({codigo: 50} as any);
        fluxoMapa.atualizarCompetencia.mockResolvedValue({} as any);

        await adicionarCompetenciaEFecharModal({descricao: "Editada", atividadesSelecionadas: []});

        expect(fluxoMapa.atualizarCompetencia).toHaveBeenCalledWith(123, 50, expect.any(Object));
    });

    it("deve remover atividade associada", async () => {
        const {removerAtividadeAssociada} = setup();
        fluxoMapa.removerAtividadeDaCompetencia.mockResolvedValue({} as any);

        await removerAtividadeAssociada(50, 10);

        expect(fluxoMapa.removerAtividadeDaCompetencia).toHaveBeenCalledWith(123, 50, 10);
    });
});
