import {beforeEach, describe, expect, it, vi} from "vitest";
import {computed, ref} from "vue";
import {useCadastroRevisaoSemMudancas} from "../useCadastroRevisaoSemMudancas";
import {SituacaoSubprocesso} from "@/types/tipos";

describe("useCadastroRevisaoSemMudancas", () => {
    const codigoSubprocesso = ref<number | null>(100);
    const isRevisao = ref(true);
    const situacaoAtual = ref<any>(SituacaoSubprocesso.NAO_INICIADO);
    const houveAlteracaoCadastro = ref(false);
    const fluxoSubprocesso = {
        iniciarRevisaoCadastro: vi.fn().mockResolvedValue(true),
        cancelarInicioRevisaoCadastro: vi.fn().mockResolvedValue(true),
    };

    const setup = () => useCadastroRevisaoSemMudancas({
        codigoSubprocesso,
        isRevisao: computed(() => isRevisao.value),
        situacaoAtual: computed(() => situacaoAtual.value),
        houveAlteracaoCadastro: computed(() => houveAlteracaoCadastro.value),
        fluxoSubprocesso,
    });

    beforeEach(() => {
        vi.clearAllMocks();
        codigoSubprocesso.value = 100;
        isRevisao.value = true;
        situacaoAtual.value = SituacaoSubprocesso.NAO_INICIADO;
        houveAlteracaoCadastro.value = false;
    });

    it("deve identificar quando precisa iniciar revisão", () => {
        const {precisaIniciarRevisao} = setup();
        expect(precisaIniciarRevisao.value).toBe(true);
    });

    it("deve iniciar revisão se necessário", async () => {
        const {iniciarRevisaoSeNecessario, loadingInicioRevisao} = setup();

        await iniciarRevisaoSeNecessario();

        expect(fluxoSubprocesso.iniciarRevisaoCadastro).toHaveBeenCalledWith(100);
        expect(loadingInicioRevisao.value).toBe(false);
    });

    it("deve cancelar início da revisão se necessário", async () => {
        situacaoAtual.value = SituacaoSubprocesso.REVISAO_CADASTRO_EM_ANDAMENTO;
        const {cancelarInicioRevisaoSeNecessario} = setup();

        await cancelarInicioRevisaoSeNecessario();

        expect(fluxoSubprocesso.cancelarInicioRevisaoCadastro).toHaveBeenCalledWith(100);
    });

    it("deve atualizar checkbox silenciosamente", () => {
        const {disponibilizacaoSemMudancas, atualizarCheckboxSemMudancasSilenciosamente} = setup();

        atualizarCheckboxSemMudancasSilenciosamente(true);

        expect(disponibilizacaoSemMudancas.value).toBe(true);
    });

    it("deve sincronizar estado inicial", () => {
        situacaoAtual.value = SituacaoSubprocesso.REVISAO_CADASTRO_EM_ANDAMENTO;
        const {disponibilizacaoSemMudancas, sincronizarDisponibilizacaoSemMudancasInicial} = setup();

        sincronizarDisponibilizacaoSemMudancasInicial();

        expect(disponibilizacaoSemMudancas.value).toBe(true);
    });

    it("deve desabilitar checkbox quando houver alteração", () => {
        houveAlteracaoCadastro.value = true;
        const {checkboxSemMudancasDesabilitado} = setup();
        expect(checkboxSemMudancasDesabilitado.value).toBe(true);
    });

    it("deve lidar com falha ao iniciar revisão", async () => {
        const {iniciarRevisaoSeNecessario} = setup();
        fluxoSubprocesso.iniciarRevisaoCadastro.mockResolvedValue(false);

        await iniciarRevisaoSeNecessario();

        expect(fluxoSubprocesso.iniciarRevisaoCadastro).toHaveBeenCalled();
    });

    it("deve lidar com falha ao cancelar revisão", async () => {
        situacaoAtual.value = SituacaoSubprocesso.REVISAO_CADASTRO_EM_ANDAMENTO;
        const {cancelarInicioRevisaoSeNecessario} = setup();
        fluxoSubprocesso.cancelarInicioRevisaoCadastro.mockResolvedValue(false);

        await cancelarInicioRevisaoSeNecessario();

        expect(fluxoSubprocesso.cancelarInicioRevisaoCadastro).toHaveBeenCalled();
    });
});
