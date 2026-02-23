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
    buscarContextoEdicao as serviceBuscarContextoEdicao,
    buscarSubprocessoDetalhe as serviceFetchSubprocessoDetalhe,
    buscarSubprocessoPorProcessoEUnidade as serviceBuscarSubprocessoPorProcessoEUnidade,
    validarCadastro as serviceValidarCadastro,
} from "@/services/subprocessoService";
import {
    alterarDataLimiteSubprocesso as serviceAlterarDataLimite,
    reabrirCadastro as serviceReabrirCadastro,
    reabrirRevisaoCadastro as serviceReabrirRevisaoCadastro,
} from "@/services/processoService";
import {usePerfilStore} from "@/stores/perfil";
import {useProcessosStore} from "@/stores/processos";
import {useFeedbackStore} from "@/stores/feedback";
import {useUnidadesStore} from "@/stores/unidades";
import {useMapasStore} from "@/stores/mapas";
import {useAtividadesStore} from "@/stores/atividades";
import {mapMapaCompletoDtoToModel} from "@/mappers/mapas";
import {mapAtividadeVisualizacaoToModel} from "@/mappers/atividades";
import {mapSubprocessoDetalheDtoToModel} from "@/mappers/subprocessos";
import type {
    AceitarCadastroRequest,
    DevolverCadastroRequest,
    HomologarCadastroRequest,
    SituacaoSubprocesso,
    SubprocessoDetalhe,
} from "@/types/tipos";
import {useErrorHandler} from "@/composables/useErrorHandler";
import {normalizeError} from "@/utils/apiError";
import {logger} from "@/utils";

export const useSubprocessosStore = defineStore("subprocessos", () => {
    const subprocessoDetalhe = ref<SubprocessoDetalhe | null>(null);
    const { lastError, clearError, withErrorHandling } = useErrorHandler();
    const feedbackStore = useFeedbackStore();

    async function _executarAcao(acao: () => Promise<any>, sucessoMsg: string, _: string): Promise<boolean> {
        try {
            await withErrorHandling(async () => {
                await acao();
                feedbackStore.show(sucessoMsg, `${sucessoMsg}.`, 'success');

                const processosStore = useProcessosStore();
                if (processosStore.processoDetalhe) {
                    await processosStore.buscarProcessoDetalhe(processosStore.processoDetalhe.codigo);
                }
            });
            return true;
        } catch {
            return false;
        }
    }

    async function alterarDataLimiteSubprocesso(
        codigo: number,
        dados: { novaData: string },
    ) {
        return withErrorHandling(async () => {
            await serviceAlterarDataLimite(codigo, dados);
            // Recarregar os detalhes para refletir a nova data
            await buscarSubprocessoDetalhe(codigo);
        });
    }

    async function buscarSubprocessoDetalhe(codigo: number) {
        subprocessoDetalhe.value = null; // Limpa estado anterior
        const perfilStore = usePerfilStore();
        const perfil = perfilStore.perfilSelecionado;
        const codUnidade = perfilStore.unidadeAtual;

        const perfilGlobal = perfil === 'ADMIN' || perfil === 'GESTOR';

        if (!perfil || (!perfilGlobal && codUnidade === null)) {
            logger.error(`Erro de pré-condição: Perfil ou unidade não disponíveis`);
            const err = new Error("Informações de perfil ou unidade não disponíveis.");
            lastError.value = normalizeError(err);
            subprocessoDetalhe.value = null;
            return;
        }

        await withErrorHandling(async () => {
            const dto = await serviceFetchSubprocessoDetalhe(
                codigo,
                perfil,
                codUnidade as number,
            );
            subprocessoDetalhe.value = mapSubprocessoDetalheDtoToModel(dto);
        }, (err) => {
            logger.error(`Erro ao buscar detalhes do subprocesso ${codigo}:`, err);
            subprocessoDetalhe.value = null;
        });
    }

    async function buscarSubprocessoPorProcessoEUnidade(
        codProcesso: number,
        siglaUnidade: string,
    ): Promise<number | null> {
        try {
            return await withErrorHandling(async () => {
                const dto = await serviceBuscarSubprocessoPorProcessoEUnidade(codProcesso, siglaUnidade);
                return dto.codigo;
            });
        } catch (err) {
            logger.error(`Erro ao buscar ID do subprocesso:`, err);
            return null;
        }
    }

    async function buscarContextoEdicao(codigo: number) {
        subprocessoDetalhe.value = null; // Limpa estado anterior
        const perfilStore = usePerfilStore();
        const perfil = perfilStore.perfilSelecionado;
        const codUnidade = perfilStore.unidadeAtual;
        const perfilGlobal = perfil === 'ADMIN' || perfil === 'GESTOR';

        if (!perfil || (!perfilGlobal && codUnidade === null)) {
            const err = new Error("Informações de perfil ou unidade não disponíveis.");
            lastError.value = normalizeError(err);
            return;
        }

        return withErrorHandling(async () => {
            const data = await serviceBuscarContextoEdicao(codigo, perfil, codUnidade as number);

            subprocessoDetalhe.value = mapSubprocessoDetalheDtoToModel(data.detalhes || data);

            const unidadesStore = useUnidadesStore();
            unidadesStore.unidade = data.unidade;

            const mapasStore = useMapasStore();
            mapasStore.mapaCompleto = data.mapa ? mapMapaCompletoDtoToModel(data.mapa) : null;

            const atividadesStore = useAtividadesStore();
            const atividadesMapped = (data.atividadesDisponiveis || []).map(mapAtividadeVisualizacaoToModel);
            atividadesStore.setAtividadesParaSubprocesso(codigo, atividadesMapped);
        });
    }

    /**
     * Atualiza o status e as permissões do subprocesso localmente, sem fazer chamada HTTP.
     * Usado após operações CRUD que retornam o status e as permissões atualizadas.
     */
    function atualizarStatusLocal(status: { 
        codigo: number; 
        situacao: SituacaoSubprocesso;
        permissoes?: any;
    }) {
        if (subprocessoDetalhe.value) {
            subprocessoDetalhe.value = {
                ...subprocessoDetalhe.value,
                situacao: status.situacao,
                permissoes: status.permissoes || subprocessoDetalhe.value.permissoes
            };
        }
    }

    async function validarCadastro(codSubprocesso: number) {
        return withErrorHandling(async () => {
            return await serviceValidarCadastro(codSubprocesso);
        });
    }

    async function reabrirCadastro(codSubprocesso: number, justificativa: string) {
        return _executarAcao(
            () => serviceReabrirCadastro(codSubprocesso, justificativa),
            "Cadastro reaberto",
            "Erro ao reabrir cadastro",
        );
    }

    async function reabrirRevisaoCadastro(codSubprocesso: number, justificativa: string) {
        return _executarAcao(
            () => serviceReabrirRevisaoCadastro(codSubprocesso, justificativa),
            "Revisão de cadastro reaberta",
            "Erro ao reabrir revisão",
        );
    }



    return {
        subprocessoDetalhe,
        lastError,
        clearError,
        alterarDataLimiteSubprocesso,
        buscarSubprocessoDetalhe,
        buscarContextoEdicao,
        buscarSubprocessoPorProcessoEUnidade,
        atualizarStatusLocal,
        validarCadastro,
        reabrirCadastro,
        reabrirRevisaoCadastro,
        disponibilizarCadastro: (codSubprocesso: number) =>
            _executarAcao(
                () => disponibilizarCadastro(codSubprocesso),
                "Cadastro de atividades disponibilizado",
                "Erro ao disponibilizar",
            ),
        disponibilizarRevisaoCadastro: (codSubprocesso: number) =>
            _executarAcao(
                () => disponibilizarRevisaoCadastro(codSubprocesso),
                "Revisão do cadastro de atividades disponibilizada",
                "Erro ao disponibilizar",
            ),
        devolverCadastro: (codSubprocesso: number, req: DevolverCadastroRequest) =>
            _executarAcao(
                () => devolverCadastro(codSubprocesso, req),
                "Cadastro devolvido",
                "Erro ao devolver",
            ),
        aceitarCadastro: (codSubprocesso: number, req: AceitarCadastroRequest) =>
            _executarAcao(
                () => aceitarCadastro(codSubprocesso, req),
                "Cadastro aceito",
                "Erro ao aceitar",
            ),
        homologarCadastro: async (codSubprocesso: number, req: HomologarCadastroRequest) => {
            const ok = await _executarAcao(
                () => homologarCadastro(codSubprocesso, req),
                "Cadastro homologado",
                "Erro ao homologar",
            );
            if (ok) await buscarSubprocessoDetalhe(codSubprocesso);
            return ok;
        },
        devolverRevisaoCadastro: (codSubprocesso: number, req: DevolverCadastroRequest) =>
            _executarAcao(
                () => devolverRevisaoCadastro(codSubprocesso, req),
                "Revisão devolvida",
                "Erro ao devolver",
            ),
        aceitarRevisaoCadastro: (codSubprocesso: number, req: AceitarCadastroRequest) =>
            _executarAcao(
                () => aceitarRevisaoCadastro(codSubprocesso, req),
                "Revisão aceita",
                "Erro ao aceitar",
            ),
        homologarRevisaoCadastro: async (codSubprocesso: number, req: HomologarCadastroRequest) => {
            const ok = await _executarAcao(
                () => homologarRevisaoCadastro(codSubprocesso, req),
                "Revisão homologada",
                "Erro ao homologar",
            );
            if (ok) await buscarSubprocessoDetalhe(codSubprocesso);
            return ok;
        },
    };
});
