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
    obterPermissoes as serviceObterPermissoes,
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
import type {
    AceitarCadastroRequest,
    DevolverCadastroRequest,
    HomologarCadastroRequest,
    SituacaoSubprocesso,
    SubprocessoDetalhe,
} from "@/types/tipos";
import {useErrorHandler} from "@/composables/useErrorHandler";
import {normalizeError} from "@/utils/apiError";

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

        // Perfis ADMIN e GESTOR são globais e podem acessar qualquer subprocesso
        // sem necessidade de codUnidade específico
        const perfilGlobal = perfil === 'ADMIN' || perfil === 'GESTOR';

        // Validação pré-condição: não lança exceção, apenas popula lastError
        // Mantido padrão original para compatibilidade com testes
        if (!perfil || (!perfilGlobal && codUnidade === null)) {
            const err = new Error("Informações de perfil ou unidade não disponíveis.");
            lastError.value = normalizeError(err);
            subprocessoDetalhe.value = null;
            return;
        }

        await withErrorHandling(async () => {
            subprocessoDetalhe.value = await serviceFetchSubprocessoDetalhe(
                codigo,
                perfil,
                codUnidade as number,
            );
        }, () => {
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
        } catch {
            return null;
        }
    }

    async function buscarContextoEdicao(codigo: number) {
        subprocessoDetalhe.value = null; // Limpa estado anterior
        const perfilStore = usePerfilStore();
        const perfil = perfilStore.perfilSelecionado;
        const codUnidade = perfilStore.unidadeAtual;

        const perfilGlobal = perfil === 'ADMIN' || perfil === 'GESTOR';

        // Validação pré-condição: não lança exceção, apenas popula lastError
        // Mantido padrão original para compatibilidade com testes
        if (!perfil || (!perfilGlobal && codUnidade === null)) {
            const err = new Error("Informações de perfil ou unidade não disponíveis.");
            lastError.value = normalizeError(err);
            return;
        }

        return withErrorHandling(async () => {
            const data = await serviceBuscarContextoEdicao(codigo, perfil, codUnidade as number);

            subprocessoDetalhe.value = data.subprocesso;

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
     * Atualiza o status do subprocesso localmente, sem fazer chamada HTTP.
     * Usado após operações CRUD que retornam o status atualizado.
     */
    function atualizarStatusLocal(status: { codigo: number; situacao: SituacaoSubprocesso; situacaoLabel: string | null }) {
        if (subprocessoDetalhe.value) {
            subprocessoDetalhe.value.situacao = status.situacao;
            subprocessoDetalhe.value.situacaoLabel = status.situacaoLabel;
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

    async function buscarPermissoes(codigo: number) {
        return withErrorHandling(async () => {
            const permissoes = await serviceObterPermissoes(codigo);
            if (subprocessoDetalhe.value && subprocessoDetalhe.value.codigo === codigo) {
                subprocessoDetalhe.value = {
                    ...subprocessoDetalhe.value,
                    permissoes: permissoes
                };
            }
        });
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
        buscarPermissoes,
        disponibilizarCadastro: (codSubrocesso: number) =>
            _executarAcao(
                () => disponibilizarCadastro(codSubrocesso),
                "Cadastro de atividades disponibilizado",
                "Erro ao disponibilizar",
            ),
        disponibilizarRevisaoCadastro: (codSubrocesso: number) =>
            _executarAcao(
                () => disponibilizarRevisaoCadastro(codSubrocesso),
                "Revisão do cadastro de atividades disponibilizada",
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
