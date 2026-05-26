import {beforeEach, describe, expect, it, vi} from "vitest";
import {useInvalidacaoNavegacao} from "../useInvalidacaoNavegacao";
import {usePainelStore} from "@/stores/painel";
import {useSubprocessoStore} from "@/stores/subprocesso";
import {useMapasStore} from "@/stores/mapas";
import {useUnidadeStore} from "@/stores/unidade";
import {useOrganizacaoStore} from "@/stores/organizacao";
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

    it("deve atualizar o fluxo completo de processo", () => {
        const painel = usePainelStore();
        const subprocesso = useSubprocessoStore();
        const mapas = useMapasStore();
        const unidade = useUnidadeStore();

        vi.spyOn(painel, 'invalidar');
        vi.spyOn(subprocesso, 'invalidar');
        vi.spyOn(mapas, 'invalidar');
        vi.spyOn(unidade, 'invalidar');

        const {atualizarFluxoProcesso} = useInvalidacaoNavegacao();
        atualizarFluxoProcesso();

        expect(painel.invalidar).toHaveBeenCalled();
        expect(invalidateQueriesMock).toHaveBeenCalledWith({key: ["painel"]});
        expect(invalidateQueriesMock).toHaveBeenCalledWith({key: ["processo"]});
        expect(subprocesso.invalidar).toHaveBeenCalled();
        expect(mapas.invalidar).toHaveBeenCalled();
        expect(unidade.invalidar).toHaveBeenCalled();
    });

    it("deve atualizar subprocesso e painel sem mexer em processo nem mapas", () => {
        const painel = usePainelStore();
        const subprocesso = useSubprocessoStore();
        const mapas = useMapasStore();

        vi.spyOn(painel, 'invalidar');
        vi.spyOn(subprocesso, 'invalidar');
        vi.spyOn(mapas, 'invalidar');

        const {atualizarFluxoSubprocessoEPainel} = useInvalidacaoNavegacao();

        atualizarFluxoSubprocessoEPainel();

        expect(painel.invalidar).toHaveBeenCalled();
        expect(invalidateQueriesMock).toHaveBeenCalledWith({key: ["painel"]});
        expect(invalidateQueriesMock).not.toHaveBeenCalledWith({key: ["processo"]});
        expect(subprocesso.invalidar).toHaveBeenCalled();
        expect(mapas.invalidar).not.toHaveBeenCalled();
    });

    it("deve atualizar o fluxo de mapa com subprocesso específico", () => {
        const painel = usePainelStore();
        const subprocesso = useSubprocessoStore();
        const mapas = useMapasStore();

        vi.spyOn(painel, "invalidar");
        vi.spyOn(subprocesso, "invalidar");
        vi.spyOn(mapas, "invalidar");

        const {atualizarFluxoMapa} = useInvalidacaoNavegacao();
        atualizarFluxoMapa(321);

        expect(painel.invalidar).toHaveBeenCalled();
        expect(invalidateQueriesMock).toHaveBeenCalledWith({key: ["painel"]});
        expect(invalidateQueriesMock).toHaveBeenCalledWith({key: ["processo"]});
        expect(subprocesso.invalidar).toHaveBeenCalled();
        expect(mapas.invalidar).toHaveBeenCalledWith(321);
    });

    it("deve atualizar dados organizacionais", () => {
        const painel = usePainelStore();
        const unidade = useUnidadeStore();
        const organizacao = useOrganizacaoStore();

        vi.spyOn(painel, "invalidar");
        vi.spyOn(unidade, "invalidar");
        vi.spyOn(organizacao, "invalidar");

        const {atualizarDadosOrganizacionais} = useInvalidacaoNavegacao();
        atualizarDadosOrganizacionais();

        expect(unidade.invalidar).toHaveBeenCalled();
        expect(organizacao.invalidar).toHaveBeenCalled();
        expect(painel.invalidar).toHaveBeenCalled();
        expect(invalidateQueriesMock).toHaveBeenCalledWith({key: ["painel"]});
    });

    it("deve limpar estado do subprocesso atual", () => {
        const subprocesso = useSubprocessoStore();
        vi.spyOn(subprocesso, 'limparContextoAtual');

        const {limparEstadoSubprocessoAtual} = useInvalidacaoNavegacao();
        limparEstadoSubprocessoAtual();

        expect(subprocesso.limparContextoAtual).toHaveBeenCalled();
    });
});
