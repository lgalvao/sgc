import {defineStore} from "pinia";
import {ref} from "vue";
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
    buscarSubprocessoPorProcessoEUnidade as serviceBuscarSubprocessoPorProcessoEUnidade,
    buscarSubprocessoDetalhe as serviceFetchSubprocessoDetalhe,
} from "@/services/subprocessoService";
import {usePerfilStore} from "@/stores/perfil"; // Adicionar esta linha
import {useProcessosStore} from "@/stores/processos";
import type {
    AceitarCadastroRequest,
    DevolverCadastroRequest,
    HomologarCadastroRequest,
    SubprocessoDetalhe,
} from "@/types/tipos";
import { ToastService } from "@/services/toastService"; // Import ToastService

async function _executarAcao(acao: () => Promise<any>, sucessoMsg: string, erroMsg: string): Promise<boolean> {
    // const notificacoes = useNotificacoesStore(); // Remove this line
    try {
        await acao();
        ToastService.sucesso(sucessoMsg, `${sucessoMsg}.`); // Use ToastService

        const processosStore = useProcessosStore();
        if (processosStore.processoDetalhe) {
            await processosStore.buscarProcessoDetalhe(processosStore.processoDetalhe.codigo);
        }
        return true;
    } catch {
        ToastService.erro(erroMsg, `Não foi possível concluir a ação: ${erroMsg}.`); // Use ToastService
        return false;
    }
}

export const useSubprocessosStore = defineStore("subprocessos", () => {
    const subprocessoDetalhe = ref<SubprocessoDetalhe | null>(null);

    async function alterarDataLimiteSubprocesso(
        id: number,
        dados: { novaData: string },
    ) {
        const processosStore = useProcessosStore();
        await processosStore.alterarDataLimiteSubprocesso(id, dados);
    }

    async function buscarSubprocessoDetalhe(id: number) {
        const perfilStore = usePerfilStore();
        // const notificacoes = useNotificacoesStore(); // Remove this line
        const perfil = perfilStore.perfilSelecionado;
        const codUnidadeSel = perfilStore.unidadeSelecionada;
        let codUnidade: number | null = null;

        if (perfil) {
            if (perfilStore.perfisUnidades.length > 0) {
                const perfilUnidade = perfilStore.perfisUnidades.find(
                    (pu) => pu.perfil === perfil && (codUnidadeSel ? pu.unidade.codigo === codUnidadeSel : true)
                );
                if (perfilUnidade) codUnidade = perfilUnidade.unidade.codigo;
            }
        }

        if (!perfil || codUnidade === null) {
            ToastService.erro( // Use ToastService
                "Erro ao buscar detalhes do subprocesso",
                "Informações de perfil ou unidade não disponíveis.",
            );
            subprocessoDetalhe.value = null;
            return;
        }

        try {
            subprocessoDetalhe.value = await serviceFetchSubprocessoDetalhe(
                id,
                perfil,
                codUnidade,
            );
        } catch {
            ToastService.erro( // Use ToastService
                "Erro ao buscar detalhes do subprocesso",
                "Não foi possível carregar as informações.",
            );
            subprocessoDetalhe.value = null;
        }
    }

    async function buscarSubprocessoPorProcessoEUnidade(
        codProcesso: number,
        siglaUnidade: string,
    ): Promise<number | null> {
        // const notificacoes = useNotificacoesStore(); // Remove this line
        try {
            const dto = await serviceBuscarSubprocessoPorProcessoEUnidade(codProcesso, siglaUnidade);
            return dto.codigo;
        } catch {
            ToastService.erro( // Use ToastService
                "Erro",
                "Não foi possível encontrar o subprocesso para esta unidade.",
            );
            return null;
        }
    }

    return {
        subprocessoDetalhe,
        alterarDataLimiteSubprocesso,
        buscarSubprocessoDetalhe,
        buscarSubprocessoPorProcessoEUnidade,
        disponibilizarCadastro: (codSubrocesso: number) =>
            _executarAcao(
                () => disponibilizarCadastro(codSubrocesso),
                "Cadastro disponibilizado",
                "Erro ao disponibilizar",
            ),
        disponibilizarRevisaoCadastro: (codSubrocesso: number) =>
            _executarAcao(
                () => disponibilizarRevisaoCadastro(codSubrocesso),
                "Revisão disponibilizada",
                "Erro ao disponibilizar",
            ),
        devolverCadastro: (codSubrocesso: number, req: DevolverCadastroRequest) =>
            _executarAcao(
                () => devolverCadastro(codSubrocesso, req),
                "Cadastro devolvido",
                "Erro ao devolver",
            ),
        aceitarCadastro: (codSubrocesso: number, req: AceitarCadastroRequest) =>
            _executarAcao(
                () => aceitarCadastro(codSubrocesso, req),
                "Cadastro aceito",
                "Erro ao aceitar",
            ),
        homologarCadastro: (codSubrocesso: number, req: HomologarCadastroRequest) =>
            _executarAcao(
                () => homologarCadastro(codSubrocesso, req),
                "Cadastro homologado",
                "Erro ao homologar",
            ),
        devolverRevisaoCadastro: (codSubrocesso: number, req: DevolverCadastroRequest) =>
            _executarAcao(
                () => devolverRevisaoCadastro(codSubrocesso, req),
                "Revisão devolvida",
                "Erro ao devolver",
            ),
        aceitarRevisaoCadastro: (codSubrocesso: number, req: AceitarCadastroRequest) =>
            _executarAcao(
                () => aceitarRevisaoCadastro(codSubrocesso, req),
                "Revisão aceita",
                "Erro ao aceitar",
            ),
        homologarRevisaoCadastro: (codSubrocesso: number, req: HomologarCadastroRequest) =>
            _executarAcao(
                () => homologarRevisaoCadastro(codSubrocesso, req),
                "Revisão homologada",
                "Erro ao homologar",
            ),
    };
});
