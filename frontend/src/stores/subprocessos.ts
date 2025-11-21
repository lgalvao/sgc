import {defineStore} from "pinia";
import {
    aceitarCadastro,
    aceitarRevisaoCadastro,
    devolverCadastro,
    devolverRevisaoCadastro,
    disponibilizarCadastro,
    disponibilizarRevisaoCadastro,
    homologarCadastro,
    homologarRevisaoCadastro,
} from "@/services/cadastroService";
import {
    buscarSubprocessoPorProcessoEUnidade,
    fetchSubprocessoDetalhe,
} from "@/services/subprocessoService";
import {usePerfilStore} from "@/stores/perfil"; // Adicionar esta linha
import {useProcessosStore} from "@/stores/processos";
import type {
    AceitarCadastroRequest,
    DevolverCadastroRequest,
    HomologarCadastroRequest,
    SubprocessoDetalhe,
} from "@/types/tipos";
import {useNotificacoesStore} from "./notificacoes";

async function _executarAcao(
    acao: () => Promise<any>,
    sucessoMsg: string,
    erroMsg: string,
): Promise<boolean> {
    const notificacoes = useNotificacoesStore();
    try {
        await acao();
        notificacoes.sucesso(sucessoMsg, `${sucessoMsg}.`);
        const processosStore = useProcessosStore();
        if (processosStore.processoDetalhe) {
            await processosStore.fetchProcessoDetalhe(
                processosStore.processoDetalhe.codigo,
            );
        }
        return true;
    } catch {
        notificacoes.erro(erroMsg, `Não foi possível concluir a ação: ${erroMsg}.`);
        return false;
    }
}

export const useSubprocessosStore = defineStore("subprocessos", {
    state: () => ({
        subprocessoDetalhe: null as SubprocessoDetalhe | null,
    }),
    actions: {
        async alterarDataLimiteSubprocesso(
            id: number,
            dados: { novaData: string },
        ) {
            const processosStore = useProcessosStore();
            await processosStore.alterarDataLimiteSubprocesso(id, dados);
        },
        async fetchSubprocessoDetalhe(id: number) {
            const notificacoes = useNotificacoesStore();
            const perfilStore = usePerfilStore(); // Adicionar esta linha
            try {
                // Obter perfil e codUnidade do perfilStore
                const perfil = perfilStore.perfilSelecionado;
                const unidadeSelecionadaCodigo = perfilStore.unidadeSelecionada;
                let unidadeCodigo: number | null = null;

                if (perfil && unidadeSelecionadaCodigo) {
                    const perfilUnidade = perfilStore.perfisUnidades.find(
                        (pu) =>
                            pu.perfil === perfil &&
                            pu.unidade.codigo === unidadeSelecionadaCodigo,
                    );
                    if (perfilUnidade) {
                        unidadeCodigo = perfilUnidade.unidade.codigo;
                    }
                }

                if (perfil && unidadeCodigo !== null) {
                    this.subprocessoDetalhe = await fetchSubprocessoDetalhe(
                        id,
                        perfil,
                        unidadeCodigo,
                    );
                } else {
                    notificacoes.erro(
                        "Erro ao buscar detalhes do subprocesso",
                        "Informações de perfil ou unidade não disponíveis.",
                    );
                }
            } catch {
                notificacoes.erro(
                    "Erro ao buscar detalhes do subprocesso",
                    "Não foi possível carregar as informações.",
                );
            }
        },

        async fetchSubprocessoPorProcessoEUnidade(
            codProcesso: number,
            siglaUnidade: string,
        ): Promise<number | null> {
            const notificacoes = useNotificacoesStore();
            try {
                const dto = await buscarSubprocessoPorProcessoEUnidade(
                    codProcesso,
                    siglaUnidade,
                );
                return dto.codigo;
            } catch {
                notificacoes.erro(
                    "Erro",
                    "Não foi possível encontrar o subprocesso para esta unidade.",
                );
                return null;
            }
        },

        async disponibilizarCadastro(codSubrocesso: number) {
            return _executarAcao(
                () => disponibilizarCadastro(codSubrocesso),
                "Cadastro disponibilizado",
                "Erro ao disponibilizar",
            );
        },

        async disponibilizarRevisaoCadastro(codSubrocesso: number) {
            return _executarAcao(
                () => disponibilizarRevisaoCadastro(codSubrocesso),
                "Revisão disponibilizada",
                "Erro ao disponibilizar",
            );
        },

        async devolverCadastro(
            codSubrocesso: number,
            req: DevolverCadastroRequest,
        ) {
            return _executarAcao(
                () => devolverCadastro(codSubrocesso, req),
                "Cadastro devolvido",
                "Erro ao devolver",
            );
        },

        async aceitarCadastro(codSubrocesso: number, req: AceitarCadastroRequest) {
            return _executarAcao(
                () => aceitarCadastro(codSubrocesso, req),
                "Cadastro aceito",
                "Erro ao aceitar",
            );
        },

        async homologarCadastro(
            codSubrocesso: number,
            req: HomologarCadastroRequest,
        ) {
            return _executarAcao(
                () => homologarCadastro(codSubrocesso, req),
                "Cadastro homologado",
                "Erro ao homologar",
            );
        },

        async devolverRevisaoCadastro(
            codSubrocesso: number,
            req: DevolverCadastroRequest,
        ) {
            return _executarAcao(
                () => devolverRevisaoCadastro(codSubrocesso, req),
                "Revisão devolvida",
                "Erro ao devolver",
            );
        },

        async aceitarRevisaoCadastro(
            codSubrocesso: number,
            req: AceitarCadastroRequest,
        ) {
            return _executarAcao(
                () => aceitarRevisaoCadastro(codSubrocesso, req),
                "Revisão aceita",
                "Erro ao aceitar",
            );
        },

        async homologarRevisaoCadastro(
            codSubrocesso: number,
            req: HomologarCadastroRequest,
        ) {
            return _executarAcao(
                () => homologarRevisaoCadastro(codSubrocesso, req),
                "Revisão homologada",
                "Erro ao homologar",
            );
        },
    },
});
