import {beforeEach, describe, expect, it, vi} from "vitest";
import {useInvalidacaoNavegacao} from "../useInvalidacaoNavegacao";
import {usePainelStore} from "@/stores/painel";
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
        const subprocesso = useSubprocessoStore();
        const mapas = useMapasStore();
        const unidade = useUnidadeStore();

        vi.spyOn(painel, 'invalidar');
        vi.spyOn(subprocesso, 'invalidar');
        vi.spyOn(mapas, 'invalidar');
        vi.spyOn(unidade, 'invalidar');

        const {invalidarCachesProcesso} = useInvalidacaoNavegacao();
        invalidarCachesProcesso();

        expect(painel.invalidar).toHaveBeenCalled();
        expect(invalidateQueriesMock).toHaveBeenCalledWith({key: ["painel"]});
        expect(invalidateQueriesMock).toHaveBeenCalledWith({key: ["processo"]});
        expect(subprocesso.invalidar).toHaveBeenCalled();
        expect(mapas.invalidar).toHaveBeenCalled();
        expect(unidade.invalidar).toHaveBeenCalled();
    });

    it("deve invalidar caches do subprocesso com opções", () => {
        const painel = usePainelStore();
        const subprocesso = useSubprocessoStore();
        const mapas = useMapasStore();

        vi.spyOn(painel, 'invalidar');
        vi.spyOn(subprocesso, 'invalidar');
        vi.spyOn(mapas, 'invalidar');

        const {invalidarCachesSubprocesso} = useInvalidacaoNavegacao();

        invalidarCachesSubprocesso({incluirPainel: true, incluirProcesso: false, incluirMapas: false});

        expect(painel.invalidar).toHaveBeenCalled();
        expect(invalidateQueriesMock).toHaveBeenCalledWith({key: ["painel"]});
        expect(invalidateQueriesMock).not.toHaveBeenCalledWith({key: ["processo"]});
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
