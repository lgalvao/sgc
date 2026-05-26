import {beforeEach, describe, expect, it, vi} from "vitest";
import {computed, ref} from "vue";
import {useMapaCompetenciasMutacoes} from "../useMapaCompetenciasMutacoes";

describe("useMapaCompetenciasMutacoes", () => {
    const codigoSubprocesso = ref<number | null>(null);
    const obterCodigoSubprocessoObrigatorio = vi.fn(() => {
        if (codigoSubprocesso.value === null) {
            throw new Error("Invariante violada: codigoSubprocesso não carregado");
        }
        return codigoSubprocesso.value;
    });
    const competencias = computed(() => []);
    const fluxoMapa = {
        adicionarCompetencia: vi.fn(),
        atualizarCompetencia: vi.fn(),
        removerCompetencia: vi.fn(),
        removerAtividadeDaCompetencia: vi.fn(),
    };
    const notify = vi.fn();
    const limparErros = vi.fn();
    const aplicarErroNormalizado = vi.fn();
    const sincronizarMapa = vi.fn();

    const setup = () => useMapaCompetenciasMutacoes({
        obterCodigoSubprocessoObrigatorio,
        competencias: competencias as any,
        fluxoMapa,
        notify,
        limparErros,
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
        expect(limparErros).toHaveBeenCalled();
    });

    it("deve falhar com invariante quando nao ha subprocesso", async () => {
        codigoSubprocesso.value = null;
        const {adicionarCompetenciaEFecharModal} = setup();

        await expect(adicionarCompetenciaEFecharModal({descricao: "Nova", atividadesSelecionadas: [1]}))
            .rejects.toThrow("Invariante violada: codigoSubprocesso não carregado");

        expect(fluxoMapa.adicionarCompetencia).not.toHaveBeenCalled();
        expect(fluxoMapa.atualizarCompetencia).not.toHaveBeenCalled();
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
        const localCompetencias = ref([{codigo: 50, descricao: "C", atividades: [{codigo: 10}]}] as any);
        const {removerAtividadeAssociada} = useMapaCompetenciasMutacoes({
            obterCodigoSubprocessoObrigatorio,
            competencias: localCompetencias as any,
            fluxoMapa,
            notify,
            limparErros,
            aplicarErroNormalizado,
            sincronizarMapa,
        });
        fluxoMapa.removerAtividadeDaCompetencia.mockResolvedValue({} as any);

        await removerAtividadeAssociada(50, 10);

        expect(fluxoMapa.removerAtividadeDaCompetencia).toHaveBeenCalledWith(123, 50, 10);
    });

    it("deve ignorar exclusao quando competencia nao existe", () => {
        const {excluirCompetencia, mostrarModalExcluirCompetencia} = setup();

        excluirCompetencia(999);

        expect(mostrarModalExcluirCompetencia.value).toBe(false);
    });

    it("deve ignorar confirmacao sem competencia selecionada", async () => {
        const {confirmarExclusaoCompetencia} = setup();

        await confirmarExclusaoCompetencia();

        expect(fluxoMapa.removerCompetencia).not.toHaveBeenCalled();
    });

    it("deve fechar modal de criação", () => {
        const {fecharModalCriarNovaCompetencia, mostrarModalCriarNovaCompetencia} = setup();
        mostrarModalCriarNovaCompetencia.value = true;

        fecharModalCriarNovaCompetencia();

        expect(mostrarModalCriarNovaCompetencia.value).toBe(false);
        expect(limparErros).toHaveBeenCalled();
    });

    it("deve iniciar exclusão de competência", () => {
        const localCompetencias = ref([{codigo: 50, descricao: "C"}] as any);
        const {
            excluirCompetencia,
            mostrarModalExcluirCompetencia,
            competenciaParaExcluir
        } = useMapaCompetenciasMutacoes({
            obterCodigoSubprocessoObrigatorio,
            competencias: localCompetencias as any,
            fluxoMapa,
            notify,
            limparErros,
            aplicarErroNormalizado,
            sincronizarMapa,
        });

        excluirCompetencia(50);

        expect(mostrarModalExcluirCompetencia.value).toBe(true);
        expect(competenciaParaExcluir.value?.codigo).toBe(50);
    });

    it("deve confirmar exclusão de competência com sucesso", async () => {
        const localCompetencias = ref([{codigo: 50, descricao: "C"}] as any);
        const {
            excluirCompetencia,
            confirmarExclusaoCompetencia,
            mostrarModalExcluirCompetencia
        } = useMapaCompetenciasMutacoes({
            obterCodigoSubprocessoObrigatorio,
            competencias: localCompetencias as any,
            fluxoMapa,
            notify,
            limparErros,
            aplicarErroNormalizado,
            sincronizarMapa,
        });
        excluirCompetencia(50);
        fluxoMapa.removerCompetencia.mockResolvedValue({} as any);

        await confirmarExclusaoCompetencia();

        expect(fluxoMapa.removerCompetencia).toHaveBeenCalledWith(123, 50);
        expect(mostrarModalExcluirCompetencia.value).toBe(false);
    });

    it("deve lidar com erro ao adicionar competência", async () => {
        const {adicionarCompetenciaEFecharModal} = setup();
        fluxoMapa.adicionarCompetencia.mockRejectedValue(new Error("Erro"));

        await adicionarCompetenciaEFecharModal({descricao: "Nova", atividadesSelecionadas: []});

        expect(aplicarErroNormalizado).toHaveBeenCalled();
    });

    it("deve lidar com erro ao confirmar exclusão", async () => {
        const localCompetencias = ref([{codigo: 50, descricao: "C"}] as any);
        const {excluirCompetencia, confirmarExclusaoCompetencia} = useMapaCompetenciasMutacoes({
            obterCodigoSubprocessoObrigatorio,
            competencias: localCompetencias as any,
            fluxoMapa,
            notify,
            limparErros,
            aplicarErroNormalizado,
            sincronizarMapa,
        });
        excluirCompetencia(50);
        fluxoMapa.removerCompetencia.mockRejectedValue(new Error("Erro de exclusão"));

        await confirmarExclusaoCompetencia();

        expect(notify).toHaveBeenCalledWith(expect.stringContaining("Erro de exclusão"), "danger");
    });

    it("deve lidar com erro ao remover atividade associada", async () => {
        const localCompetencias = ref([{codigo: 50, descricao: "C", atividades: [{codigo: 10}]}] as any);
        const {removerAtividadeAssociada} = useMapaCompetenciasMutacoes({
            obterCodigoSubprocessoObrigatorio,
            competencias: localCompetencias as any,
            fluxoMapa,
            notify,
            limparErros,
            aplicarErroNormalizado,
            sincronizarMapa,
        });
        fluxoMapa.removerAtividadeDaCompetencia.mockRejectedValue(new Error("Erro de remoção"));

        await removerAtividadeAssociada(50, 10);

        expect(notify).toHaveBeenCalledWith(expect.stringContaining("Erro de remoção"), "danger");
    });
});
