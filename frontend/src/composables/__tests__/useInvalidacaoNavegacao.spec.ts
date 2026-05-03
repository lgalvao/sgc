import {beforeEach, describe, expect, it, vi} from "vitest";
import {useInvalidacaoNavegacao} from "../useInvalidacaoNavegacao";
import {usePainelStore} from "@/stores/painel";
import {useProcessoStore} from "@/stores/processo";
import {useSubprocessoStore} from "@/stores/subprocesso";
import {useMapasStore} from "@/stores/mapas";
import {createPinia, setActivePinia} from "pinia";

describe("useInvalidacaoNavegacao", () => {
    beforeEach(() => {
        setActivePinia(createPinia());
    });

    it("deve invalidar caches do processo", () => {
        const painel = usePainelStore();
        const processo = useProcessoStore();
        const subprocesso = useSubprocessoStore();
        const mapas = useMapasStore();
        
        vi.spyOn(painel, 'invalidar');
        vi.spyOn(processo, 'invalidar');
        vi.spyOn(subprocesso, 'invalidar');
        vi.spyOn(mapas, 'invalidar');

        const {invalidarCachesProcesso} = useInvalidacaoNavegacao();
        invalidarCachesProcesso();

        expect(painel.invalidar).toHaveBeenCalled();
        expect(processo.invalidar).toHaveBeenCalled();
        expect(subprocesso.invalidar).toHaveBeenCalled();
        expect(mapas.invalidar).toHaveBeenCalled();
    });

    it("deve invalidar caches do subprocesso com opções", () => {
        const painel = usePainelStore();
        const processo = useProcessoStore();
        const subprocesso = useSubprocessoStore();
        
        vi.spyOn(painel, 'invalidar');
        vi.spyOn(processo, 'invalidar');
        vi.spyOn(subprocesso, 'invalidar');

        const {invalidarCachesSubprocesso} = useInvalidacaoNavegacao();
        
        invalidarCachesSubprocesso({incluirPainel: true, incluirProcesso: false});

        expect(painel.invalidar).toHaveBeenCalled();
        expect(processo.invalidar).not.toHaveBeenCalled();
        expect(subprocesso.invalidar).toHaveBeenCalled();
    });

    it("deve limpar estado do subprocesso atual", () => {
        const subprocesso = useSubprocessoStore();
        vi.spyOn(subprocesso, 'limparContextoAtual');

        const {limparEstadoSubprocessoAtual} = useInvalidacaoNavegacao();
        limparEstadoSubprocessoAtual();

        expect(subprocesso.limparContextoAtual).toHaveBeenCalled();
    });
});
