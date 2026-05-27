import {beforeEach, describe, expect, it, vi} from "vitest";
import {useInvalidacaoNavegacao} from "../useInvalidacaoNavegacao";
import {usePainelStore} from "@/stores/painel";
import {useSubprocessoStore} from "@/stores/subprocesso";

import {useOrganizacaoStore} from "@/stores/organizacao";
import {createPinia, setActivePinia} from "pinia";

const invalidateQueriesMock = vi.fn();

vi.mock("@pinia/colada", () => ({
    useQueryCache: () => ({
        invalidateQueries: invalidateQueriesMock,
        setQueryData: vi.fn(),
        getQueryData: vi.fn(),
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
        vi.spyOn(painel, 'invalidar');
        vi.spyOn(subprocesso, 'invalidar');

        const {atualizarFluxoProcesso} = useInvalidacaoNavegacao();
        atualizarFluxoProcesso();

        expect(painel.invalidar).toHaveBeenCalled();
        expect(invalidateQueriesMock).toHaveBeenCalledWith({key: ["painel"]});
        expect(invalidateQueriesMock).toHaveBeenCalledWith({key: ["processo"]});
        expect(subprocesso.invalidar).toHaveBeenCalled();
        expect(invalidateQueriesMock).toHaveBeenCalledWith({key: ["mapa"]});
        expect(invalidateQueriesMock).toHaveBeenCalledWith({key: ["unidade"]});
        expect(invalidateQueriesMock).toHaveBeenCalledWith({key: ["dados-tela-unidade"]});
        expect(invalidateQueriesMock).toHaveBeenCalledWith({key: ["unidade", "arvore-elegibilidade"]});
    });

    it("deve atualizar subprocesso e painel sem mexer em processo nem mapas", () => {
        const painel = usePainelStore();
        const subprocesso = useSubprocessoStore();

        vi.spyOn(painel, 'invalidar');
        vi.spyOn(subprocesso, 'invalidar');

        const {atualizarFluxoSubprocessoEPainel} = useInvalidacaoNavegacao();

        atualizarFluxoSubprocessoEPainel();

        expect(painel.invalidar).toHaveBeenCalled();
        expect(invalidateQueriesMock).toHaveBeenCalledWith({key: ["painel"]});
        expect(invalidateQueriesMock).not.toHaveBeenCalledWith({key: ["processo"]});
        expect(subprocesso.invalidar).toHaveBeenCalled();
        expect(invalidateQueriesMock).not.toHaveBeenCalledWith({key: ["mapa"]});
    });

    it("deve atualizar o fluxo de mapa com subprocesso específico", () => {
        const painel = usePainelStore();
        const subprocesso = useSubprocessoStore();

        vi.spyOn(painel, "invalidar");
        vi.spyOn(subprocesso, "invalidar");

        const {atualizarFluxoMapa} = useInvalidacaoNavegacao();
        atualizarFluxoMapa(321);

        expect(painel.invalidar).toHaveBeenCalled();
        expect(invalidateQueriesMock).toHaveBeenCalledWith({key: ["painel"]});
        expect(invalidateQueriesMock).toHaveBeenCalledWith({key: ["processo"]});
        expect(invalidateQueriesMock).toHaveBeenCalledWith({key: expect.arrayContaining(["mapa", 321]), exact: true});
        expect(subprocesso.invalidar).toHaveBeenCalled();
    });

    it("deve atualizar dados organizacionais", () => {
        const painel = usePainelStore();
        const organizacao = useOrganizacaoStore();
        vi.spyOn(painel, "invalidar");
        vi.spyOn(organizacao, "invalidar");

        const {atualizarDadosOrganizacionais} = useInvalidacaoNavegacao();
        atualizarDadosOrganizacionais();

        expect(invalidateQueriesMock).toHaveBeenCalledWith({key: ["unidade"]});
        expect(invalidateQueriesMock).toHaveBeenCalledWith({key: ["dados-tela-unidade"]});
        expect(invalidateQueriesMock).toHaveBeenCalledWith({key: ["unidade", "arvore-elegibilidade"]});
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

    it("deve resetar estado da sessão ao fazer logout", () => {
        const painel = usePainelStore();
        const subprocesso = useSubprocessoStore();
        const organizacao = useOrganizacaoStore();
        vi.spyOn(painel, "resetar");
        vi.spyOn(subprocesso, "resetar");
        vi.spyOn(organizacao, "resetar");

        const {resetarEstadoSessao} = useInvalidacaoNavegacao();
        resetarEstadoSessao();

        expect(painel.resetar).toHaveBeenCalled();
        expect(subprocesso.resetar).toHaveBeenCalled();
        expect(organizacao.resetar).toHaveBeenCalled();
        expect(invalidateQueriesMock).toHaveBeenCalledWith({key: ["mapa"]});
        expect(invalidateQueriesMock).toHaveBeenCalledWith({key: ["unidade"]});
        expect(invalidateQueriesMock).toHaveBeenCalledWith({key: ["dados-tela-unidade"]});
        expect(invalidateQueriesMock).toHaveBeenCalledWith({key: ["unidade", "arvore-elegibilidade"]});
    });
});
