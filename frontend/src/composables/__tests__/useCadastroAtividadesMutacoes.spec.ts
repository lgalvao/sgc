import {describe, it, expect, beforeEach, vi} from "vitest";
import {ref} from "vue";
import {useCadastroAtividadesMutacoes} from "../useCadastroAtividadesMutacoes";
import * as atividadeService from "@/services/atividadeService";

vi.mock("@/services/atividadeService", () => ({
    excluirAtividade: vi.fn(),
    excluirConhecimento: vi.fn(),
    atualizarAtividade: vi.fn(),
    criarConhecimento: vi.fn(),
    atualizarConhecimento: vi.fn(),
}));

describe("useCadastroAtividadesMutacoes", () => {
    const atividades = ref<any[]>([]);
    const codigoSubprocesso = ref<number | null>(null);
    const codMapa = ref<number | null>(null);
    const withErrorHandling = vi.fn((cb) => cb());
    const lastError = ref<any>(null);
    const notify = vi.fn();
    const processarRespostaLocal = vi.fn();
    const adicionarAtividadeAction = vi.fn();

    const setup = () => useCadastroAtividadesMutacoes({
        atividades,
        codigoSubprocesso,
        codMapa,
        withErrorHandling,
        lastError,
        notify,
        processarRespostaLocal,
        adicionarAtividadeAction,
    });

    beforeEach(() => {
        vi.clearAllMocks();
        atividades.value = [];
        codigoSubprocesso.value = 100;
        codMapa.value = 200;
    });

    it("deve adicionar atividade com sucesso", async () => {
        const {adicionarAtividade} = setup();
        const response = {codigo: 1} as any;
        adicionarAtividadeAction.mockResolvedValue(response);

        const result = await adicionarAtividade();

        expect(result).toBe(true);
        expect(adicionarAtividadeAction).toHaveBeenCalledWith(100, 200);
        expect(processarRespostaLocal).toHaveBeenCalledWith(response);
    });

    it("deve retornar falso se codMapa ou codigoSubprocesso forem nulos", async () => {
        codMapa.value = null;
        const {adicionarAtividade} = setup();
        
        const result = await adicionarAtividade();
        
        expect(result).toBe(false);
        expect(adicionarAtividadeAction).not.toHaveBeenCalled();
    });

    it("deve lidar com erro ao adicionar atividade", async () => {
        const {adicionarAtividade, erroNovaAtividade} = setup();
        adicionarAtividadeAction.mockRejectedValue(new Error("Erro"));
        lastError.value = {message: "Erro customizado"};

        const result = await adicionarAtividade();

        expect(result).toBe(false);
        expect(erroNovaAtividade.value).toBe("Erro customizado");
    });

    it("deve preparar remoção de atividade", () => {
        const {removerAtividade, dadosRemocao, mostrarModalConfirmacaoRemocao} = setup();
        
        removerAtividade(1);
        
        expect(dadosRemocao.value).toEqual({tipo: "atividade", atividadeCodigo: 1});
        expect(mostrarModalConfirmacaoRemocao.value).toBe(true);
    });

    it("deve confirmar remoção de atividade com sucesso", async () => {
        const {removerAtividade, confirmarRemocao, mostrarModalConfirmacaoRemocao} = setup();
        removerAtividade(1);
        vi.mocked(atividadeService.excluirAtividade).mockResolvedValue({} as any);

        await confirmarRemocao();

        expect(atividadeService.excluirAtividade).toHaveBeenCalledWith(1);
        expect(processarRespostaLocal).toHaveBeenCalled();
        expect(mostrarModalConfirmacaoRemocao.value).toBe(false);
    });

    it("deve salvar edição de atividade", async () => {
        const {salvarEdicaoAtividade} = setup();
        atividades.value = [{codigo: 1, descricao: "Antiga"}];
        vi.mocked(atividadeService.atualizarAtividade).mockResolvedValue({} as any);

        await salvarEdicaoAtividade(1, "Nova");

        expect(atividadeService.atualizarAtividade).toHaveBeenCalledWith(1, expect.objectContaining({
            descricao: "Nova"
        }));
    });

    it("deve adicionar conhecimento", async () => {
        const {adicionarConhecimento} = setup();
        vi.mocked(atividadeService.criarConhecimento).mockResolvedValue({} as any);

        await adicionarConhecimento(1, "Novo Conhecimento");

        expect(atividadeService.criarConhecimento).toHaveBeenCalledWith(1, {
            descricao: "Novo Conhecimento"
        });
    });

    it("deve preparar e confirmar remoção de conhecimento", async () => {
        const {removerConhecimento, confirmarRemocao} = setup();
        removerConhecimento(1, 50);
        vi.mocked(atividadeService.excluirConhecimento).mockResolvedValue({} as any);

        await confirmarRemocao();

        expect(atividadeService.excluirConhecimento).toHaveBeenCalledWith(1, 50);
    });

    it("deve salvar edição de conhecimento", async () => {
        const {salvarEdicaoConhecimento} = setup();
        vi.mocked(atividadeService.atualizarConhecimento).mockResolvedValue({} as any);

        await salvarEdicaoConhecimento(1, 50, "Desc Atualizada");

        expect(atividadeService.atualizarConhecimento).toHaveBeenCalledWith(1, 50, {
            codigo: 50,
            descricao: "Desc Atualizada"
        });
    });
});
