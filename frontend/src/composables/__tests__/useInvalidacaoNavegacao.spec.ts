import {beforeEach, describe, expect, it, vi} from "vitest";
import {useInvalidacaoNavegacao} from "../useInvalidacaoNavegacao";
import {usePainelStore} from "@/stores/painel";
import {useProcessoStore} from "@/stores/processo";
import {useSubprocessoStore} from "@/stores/subprocesso";
import {useMapasStore} from "@/stores/mapas";
import {useUnidadeStore} from "@/stores/unidade";
import {createPinia, setActivePinia} from "pinia";

const invalidateQueriesMock = vi.fn();

vi.mock("@pinia/colada", () => ({
    useQueryCache: () => ({
        invalidateQueries: invalidateQueriesMock,
    }),
}));

describe("useInvalidacaoNavegacao", () => {
    beforeEach(() => {
        setActivePinia(createPinia());
        invalidateQueriesMock.mockReset();
    });

    it("deve invalidar caches do processo", () => {
        const painel = usePainelStore();
        const processo = useProcessoStore();
        const subprocesso = useSubprocessoStore();
        const mapas = useMapasStore();
        const unidade = useUnidadeStore();

        vi.spyOn(painel, 'invalidar');
        vi.spyOn(processo, 'invalidar');
        vi.spyOn(subprocesso, 'invalidar');
        vi.spyOn(mapas, 'invalidar');
        vi.spyOn(unidade, 'invalidar');

        const {invalidarCachesProcesso} = useInvalidacaoNavegacao();
        invalidarCachesProcesso();

        expect(painel.invalidar).toHaveBeenCalled();
        expect(invalidateQueriesMock).toHaveBeenCalledWith({key: ["painel"]});
        expect(processo.invalidar).toHaveBeenCalled();
        expect(subprocesso.invalidar).toHaveBeenCalled();
        expect(mapas.invalidar).toHaveBeenCalled();
        expect(unidade.invalidar).toHaveBeenCalled();
    });

    it("deve invalidar caches do subprocesso com opções", () => {
        const painel = usePainelStore();
        const processo = useProcessoStore();
        const subprocesso = useSubprocessoStore();
        const mapas = useMapasStore();

        vi.spyOn(painel, 'invalidar');
        vi.spyOn(processo, 'invalidar');
        vi.spyOn(subprocesso, 'invalidar');
        vi.spyOn(mapas, 'invalidar');

        const {invalidarCachesSubprocesso} = useInvalidacaoNavegacao();

        invalidarCachesSubprocesso({incluirPainel: true, incluirProcesso: false, incluirMapas: false});

        expect(painel.invalidar).toHaveBeenCalled();
        expect(invalidateQueriesMock).toHaveBeenCalledWith({key: ["painel"]});
        expect(processo.invalidar).not.toHaveBeenCalled();
        expect(subprocesso.invalidar).toHaveBeenCalled();
        expect(mapas.invalidar).not.toHaveBeenCalled();
    });

    it("deve limpar estado do subprocesso atual", () => {
        const subprocesso = useSubprocessoStore();
        vi.spyOn(subprocesso, 'limparContextoAtual');

        const {limparEstadoSubprocessoAtual} = useInvalidacaoNavegacao();
        limparEstadoSubprocessoAtual();

        expect(subprocesso.limparContextoAtual).toHaveBeenCalled();
    });
});
