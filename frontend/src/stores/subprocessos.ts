import {defineStore} from 'pinia'
import {AceitarCadastroRequest, DevolverCadastroRequest, HomologarCadastroRequest, ProcessoDetalhe} from '@/types/tipos'
import {useNotificacoesStore} from './notificacoes'
import {useProcessosStore} from "@/stores/processos";
import {usePerfilStore} from "@/stores/perfil"; // Adicionar esta linha
import { fetchSubprocessoDetalhe } from "@/services/subprocessoService";
import {
    aceitarCadastro,
    aceitarRevisaoCadastro,
    devolverCadastro,
    devolverRevisaoCadastro,
    disponibilizarCadastro,
    disponibilizarRevisaoCadastro,
    homologarCadastro,
    homologarRevisaoCadastro
} from "@/services/cadastroService";

async function _executarAcao(
    acao: () => Promise<any>,
    sucessoMsg: string,
    erroMsg: string
): Promise<boolean> {
    const notificacoes = useNotificacoesStore();
    try {
        await acao();
        notificacoes.sucesso(sucessoMsg, `${sucessoMsg} com sucesso.`);
        const processosStore = useProcessosStore();
        if (processosStore.processoDetalhe) {
            await processosStore.fetchProcessoDetalhe(processosStore.processoDetalhe.codigo);
        }
        return true;
    } catch {
        notificacoes.erro(erroMsg, `Não foi possível concluir a ação: ${erroMsg}.`);
        return false;
    }
}

export const useSubprocessosStore = defineStore('subprocessos', {
    state: () => ({
        subprocessoDetalhe: null as ProcessoDetalhe | null,
    }),
    actions: {
        async fetchSubprocessoDetalhe(id: number) {
            const notificacoes = useNotificacoesStore();
            const perfilStore = usePerfilStore(); // Adicionar esta linha
            try {
                // Obter perfil e unidadeCodigo do perfilStore
                const perfil = perfilStore.perfilSelecionado;
                const unidadeSelecionadaCodigo = perfilStore.unidadeSelecionada;
                let unidadeCodigo: number | null = null;

                if (perfil && unidadeSelecionadaCodigo) {
                    const perfilUnidade = perfilStore.perfisUnidades.find(pu =>
                        pu.perfil === perfil && pu.unidade.codigo === unidadeSelecionadaCodigo
                    );
                    if (perfilUnidade) {
                        unidadeCodigo = perfilUnidade.unidade.codigo;
                    }
                }

                if (perfil && unidadeCodigo !== null) {
                    this.subprocessoDetalhe = await fetchSubprocessoDetalhe(id, perfil, unidadeCodigo);
                } else {
                    notificacoes.erro('Erro ao buscar detalhes do subprocesso', 'Informações de perfil ou unidade não disponíveis.');
                }
            } catch {
                notificacoes.erro('Erro ao buscar detalhes do subprocesso', 'Não foi possível carregar as informações.');
            }
        },

        async disponibilizarCadastro(idSubprocesso: number) {
            return _executarAcao(
                () => disponibilizarCadastro(idSubprocesso),
                'Cadastro disponibilizado',
                'Erro ao disponibilizar'
            );
        },

        async disponibilizarRevisaoCadastro(idSubprocesso: number) {
            return _executarAcao(
                () => disponibilizarRevisaoCadastro(idSubprocesso),
                'Revisão disponibilizada',
                'Erro ao disponibilizar'
            );
        },

        async devolverCadastro(idSubprocesso: number, req: DevolverCadastroRequest) {
            return _executarAcao(
                () => devolverCadastro(idSubprocesso, req),
                'Cadastro devolvido',
                'Erro ao devolver'
            );
        },

        async aceitarCadastro(idSubprocesso: number, req: AceitarCadastroRequest) {
            return _executarAcao(
                () => aceitarCadastro(idSubprocesso, req),
                'Cadastro aceito',
                'Erro ao aceitar'
            );
        },

        async homologarCadastro(idSubprocesso: number, req: HomologarCadastroRequest) {
            return _executarAcao(
                () => homologarCadastro(idSubprocesso, req),
                'Cadastro homologado',
                'Erro ao homologar'
            );
        },

        async devolverRevisaoCadastro(idSubprocesso: number, req: DevolverCadastroRequest) {
            return _executarAcao(
                () => devolverRevisaoCadastro(idSubprocesso, req),
                'Revisão devolvida',
                'Erro ao devolver'
            );
        },

        async aceitarRevisaoCadastro(idSubprocesso: number, req: AceitarCadastroRequest) {
            return _executarAcao(
                () => aceitarRevisaoCadastro(idSubprocesso, req),
                'Revisão aceita',
                'Erro ao aceitar'
            );
        },

        async homologarRevisaoCadastro(idSubprocesso: number, req: HomologarCadastroRequest) {
            return _executarAcao(
                () => homologarRevisaoCadastro(idSubprocesso, req),
                'Revisão homologada',
                'Erro ao homologar'
            );
        },

        reset() {
            this.subprocessoDetalhe = null;
        },


    }
});
