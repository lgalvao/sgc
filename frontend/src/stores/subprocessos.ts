import {defineStore} from 'pinia'
import {
    AceitarCadastroRequest,
    DevolverCadastroRequest,
    HomologarCadastroRequest,
    Subprocesso,
    SubprocessoDetalhe
} from '@/types/tipos'
import {useNotificacoesStore} from './notificacoes'
import * as subprocessoService from '@/services/subprocessoService'
import {useProcessosStore} from "@/stores/processos";

export const useSubprocessosStore = defineStore('subprocessos', {
    state: () => ({
        subprocessoDetalhe: null as SubprocessoDetalhe | null,
    }),
    actions: {
        async fetchSubprocessoDetalhe(id: number) {
            const notificacoes = useNotificacoesStore();
            try {
                this.subprocessoDetalhe = await subprocessoService.obterSubprocessoDetalhe(id);
            } catch (error) {
                notificacoes.erro('Erro ao buscar detalhes do subprocesso', 'Não foi possível carregar as informações.');
            }
        },

        async disponibilizarCadastro(idSubprocesso: number) {
            const notificacoes = useNotificacoesStore();
            try {
                await subprocessoService.disponibilizarCadastro(idSubprocesso);
                notificacoes.sucesso('Cadastro disponibilizado', 'O cadastro foi enviado para a próxima etapa.');
                // Atualizar o estado local para refletir a mudança
                const processosStore = useProcessosStore();
                if (processosStore.processoDetalhe) {
                    await processosStore.fetchProcessoDetalhe(processosStore.processoDetalhe.codigo);
                }
            } catch (error) {
                notificacoes.erro('Erro ao disponibilizar', 'Não foi possível concluir a ação.');
            }
        },

        async disponibilizarRevisaoCadastro(idSubprocesso: number) {
            const notificacoes = useNotificacoesStore();
            try {
                await subprocessoService.disponibilizarRevisaoCadastro(idSubprocesso);
                notificacoes.sucesso('Revisão disponibilizada', 'A revisão do cadastro foi enviada para a próxima etapa.');
                const processosStore = useProcessosStore();
                if (processosStore.processoDetalhe) {
                    await processosStore.fetchProcessoDetalhe(processosStore.processoDetalhe.codigo);
                }
            } catch (error) {
                notificacoes.erro('Erro ao disponibilizar', 'Não foi possível concluir a ação.');
            }
        },

        async devolverCadastro(idSubprocesso: number, req: DevolverCadastroRequest) {
            const notificacoes = useNotificacoesStore();
            try {
                await subprocessoService.devolverCadastro(idSubprocesso, req);
                notificacoes.sucesso('Cadastro devolvido', 'O cadastro foi devolvido para ajustes.');
                const processosStore = useProcessosStore();
                if (processosStore.processoDetalhe) {
                    await processosStore.fetchProcessoDetalhe(processosStore.processoDetalhe.codigo);
                }
            } catch (error) {
                notificacoes.erro('Erro ao devolver', 'Não foi possível concluir a ação.');
            }
        },

        async aceitarCadastro(idSubprocesso: number, req: AceitarCadastroRequest) {
            const notificacoes = useNotificacoesStore();
            try {
                await subprocessoService.aceitarCadastro(idSubprocesso, req);
                notificacoes.sucesso('Cadastro aceito', 'A análise foi registrada com sucesso.');
                const processosStore = useProcessosStore();
                if (processosStore.processoDetalhe) {
                    await processosStore.fetchProcessoDetalhe(processosStore.processoDetalhe.codigo);
                }
            } catch (error) {
                notificacoes.erro('Erro ao aceitar', 'Não foi possível registrar a análise.');
            }
        },

        async homologarCadastro(idSubprocesso: number, req: HomologarCadastroRequest) {
            const notificacoes = useNotificacoesStore();
            try {
                await subprocessoService.homologarCadastro(idSubprocesso, req);
                notificacoes.sucesso('Cadastro homologado', 'O cadastro foi homologado com sucesso.');
                const processosStore = useProcessosStore();
                if (processosStore.processoDetalhe) {
                    await processosStore.fetchProcessoDetalhe(processosStore.processoDetalhe.codigo);
                }
            } catch (error) {
                notificacoes.erro('Erro ao homologar', 'Não foi possível concluir a homologação.');
            }
        },

        async devolverRevisaoCadastro(idSubprocesso: number, req: DevolverCadastroRequest) {
            const notificacoes = useNotificacoesStore();
            try {
                await subprocessoService.devolverRevisaoCadastro(idSubprocesso, req);
                notificacoes.sucesso('Revisão devolvida', 'A revisão do cadastro foi devolvida para ajustes.');
                const processosStore = useProcessosStore();
                if (processosStore.processoDetalhe) {
                    await processosStore.fetchProcessoDetalhe(processosStore.processoDetalhe.codigo);
                }
            } catch (error) {
                notificacoes.erro('Erro ao devolver', 'Não foi possível concluir a ação.');
            }
        },

        async aceitarRevisaoCadastro(idSubprocesso: number, req: AceitarCadastroRequest) {
            const notificacoes = useNotificacoesStore();
            try {
                await subprocessoService.aceitarRevisaoCadastro(idSubprocesso, req);
                notificacoes.sucesso('Revisão aceita', 'A análise da revisão foi registrada com sucesso.');
                const processosStore = useProcessosStore();
                if (processosStore.processoDetalhe) {
                    await processosStore.fetchProcessoDetalhe(processosStore.processoDetalhe.codigo);
                }
            } catch (error) {
                notificacoes.erro('Erro ao aceitar', 'Não foi possível registrar a análise.');
            }
        },

        async homologarRevisaoCadastro(idSubprocesso: number, req: HomologarCadastroRequest) {
            const notificacoes = useNotificacoesStore();
            try {
                await subprocessoService.homologarRevisaoCadastro(idSubprocesso, req);
                notificacoes.sucesso('Revisão homologada', 'A revisão do cadastro foi homologada com sucesso.');
                const processosStore = useProcessosStore();
                if (processosStore.processoDetalhe) {
                    await processosStore.fetchProcessoDetalhe(processosStore.processoDetalhe.codigo);
                }
            } catch (error) {
                notificacoes.erro('Erro ao homologar', 'Não foi possível concluir a homologação.');
            }
        },

        reset() {
            this.subprocessoDetalhe = null;
        },

        async homologarValidacao(idSubprocesso: number, req: any) {
            const notificacoes = useNotificacoesStore();
            console.log('homologarValidacao', idSubprocesso, req);
            // try {
            //     await subprocessoService.homologarValidacao(idSubprocesso, req);
            //     notificacoes.sucesso('Validação homologada', 'A validação foi homologada com sucesso.');
            //     const processosStore = useProcessosStore();
            //     if (processosStore.processoDetalhe) {
            //         await processosStore.fetchProcessoDetalhe(processosStore.processoDetalhe.codigo);
            //     }
            // } catch (error) {
            //     notificacoes.erro('Erro ao homologar', 'Não foi possível concluir a homologação.');
            // }
        },

        async aceitarValidacao(idSubprocesso: number, req: any) {
            const notificacoes = useNotificacoesStore();
            console.log('aceitarValidacao', idSubprocesso, req);
            // try {
            //     await subprocessoService.aceitarValidacao(idSubprocesso, req);
            //     notificacoes.sucesso('Validação aceita', 'A análise da validação foi registrada com sucesso.');
            //     const processosStore = useProcessosStore();
            //     if (processosStore.processoDetalhe) {
            //         await processosStore.fetchProcessoDetalhe(processosStore.processoDetalhe.codigo);
            //     }
            // } catch (error) {
            //     notificacoes.erro('Erro ao aceitar', 'Não foi possível registrar a análise.');
            // }
        },

        async devolverValidacao(idSubprocesso: number, req: any) {
            const notificacoes = useNotificacoesStore();
            console.log('devolverValidacao', idSubprocesso, req);
            // try {
            //     await subprocessoService.devolverValidacao(idSubprocesso, req);
            //     notificacoes.sucesso('Validação devolvida', 'A validação foi devolvida para ajustes.');
            //     const processosStore = useProcessosStore();
            //     if (processosStore.processoDetalhe) {
            //         await processosStore.fetchProcessoDetalhe(processosStore.processoDetalhe.codigo);
            //     }
            // } catch (error) {
            //     notificacoes.erro('Erro ao devolver', 'Não foi possível concluir a ação.');
            // }
        }
    }
});