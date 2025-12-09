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
    buscarSubprocessoDetalhe as serviceFetchSubprocessoDetalhe,
    buscarSubprocessoPorProcessoEUnidade as serviceBuscarSubprocessoPorProcessoEUnidade,
} from "@/services/subprocessoService";
import {usePerfilStore} from "@/stores/perfil"; // Adicionar esta linha
import {useProcessosStore} from "@/stores/processos";
import {useFeedbackStore} from "@/stores/feedback";
import type {
    AceitarCadastroRequest,
    DevolverCadastroRequest,
    HomologarCadastroRequest,
    SubprocessoDetalhe,
} from "@/types/tipos";

async function _executarAcao(acao: () => Promise<any>, sucessoMsg: string, erroMsg: string): Promise<boolean> {
    const feedbackStore = useFeedbackStore();
    try {
        await acao();
        feedbackStore.show(sucessoMsg, `${sucessoMsg}.`, 'success');

        const processosStore = useProcessosStore();
        if (processosStore.processoDetalhe) {
            await processosStore.buscarProcessoDetalhe(processosStore.processoDetalhe.codigo);
        }
        return true;
    } catch {
        feedbackStore.show(erroMsg, `Não foi possível concluir a ação: ${erroMsg}.`, 'danger');
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
        const feedbackStore = useFeedbackStore();
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

        // Perfis ADMIN e GESTOR são globais e podem acessar qualquer subprocesso
        // sem necessidade de codUnidade específico
        const perfilGlobal = perfil === 'ADMIN' || perfil === 'GESTOR';

        if (!perfil || (!perfilGlobal && codUnidade === null)) {
            feedbackStore.show(
                "Erro ao buscar detalhes do subprocesso",
                "Informações de perfil ou unidade não disponíveis.",
                "danger"
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
            feedbackStore.show(
                "Erro ao buscar detalhes do subprocesso",
                "Não foi possível carregar as informações.",
                "danger"
            );
            subprocessoDetalhe.value = null;
        }
    }

    async function buscarSubprocessoPorProcessoEUnidade(
        codProcesso: number,
        siglaUnidade: string,
    ): Promise<number | null> {
        const feedbackStore = useFeedbackStore();
        try {
            const dto = await serviceBuscarSubprocessoPorProcessoEUnidade(codProcesso, siglaUnidade);
            return dto.codigo;
        } catch {
            feedbackStore.show(
                "Erro",
                "Não foi possível encontrar o subprocesso para esta unidade.",
                "danger"
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
                "Cadastro de atividades disponibilizado",
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
        homologarCadastro: async (codSubrocesso: number, req: HomologarCadastroRequest) => {
            const ok = await _executarAcao(
                () => homologarCadastro(codSubrocesso, req),
                "Cadastro homologado",
                "Erro ao homologar",
            );
            if (ok) await buscarSubprocessoDetalhe(codSubrocesso);
            return ok;
        },
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
        homologarRevisaoCadastro: async (codSubrocesso: number, req: HomologarCadastroRequest) => {
            const ok = await _executarAcao(
                () => homologarRevisaoCadastro(codSubrocesso, req),
                "Revisão homologada",
                "Erro ao homologar",
            );
            if (ok) await buscarSubprocessoDetalhe(codSubrocesso);
            return ok;
        },
    };
});
