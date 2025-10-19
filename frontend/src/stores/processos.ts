import {defineStore} from 'pinia'
import {Movimentacao, Processo, Subprocesso} from '@/types/tipos'
import {generateUniqueId} from '@/utils'
import * as painelService from '../services/painelService'
import {Page} from '../services/painelService'
import * as processoService from '../services/processoService'
import {
  AtualizarProcessoRequest,
  CriarProcessoRequest,
  ProcessoDetalhe,
  ProcessoResumo,
  SituacaoSubprocesso,
  TipoProcesso
} from '@/types/tipos'

export const useProcessosStore = defineStore('processos', {
    state: () => ({
        processosPainel: [] as ProcessoResumo[],
        processosPainelPage: {} as Page<ProcessoResumo>,
        processoDetalhe: null as ProcessoDetalhe | null, // Para armazenar o processo detalhado
        processosFinalizados: [] as Processo[],
        // As propriedades abaixo serão tratadas em etapas futuras ou removidas se não forem mais necessárias
        // processos: [] as Processo[], // Removido
        // subprocessos: [] as Subprocesso[], // Removido
        movements: [] as Movimentacao[] // Manter se ainda for usado para mocks internos ou outras lógicas
    }),
    getters: {
        getUnidadesDoProcesso: (state) => (idProcesso: number): Subprocesso[] => {
            // Se o processoDetalhe estiver carregado e for o processo correto, usar seus subprocessos
            if (state.processoDetalhe && state.processoDetalhe.codigo === idProcesso) {
                return state.processoDetalhe.resumoSubprocessos;
            }
            // Caso contrário, retornar vazio ou buscar de outra forma se necessário
            return [];
        },
        // Subprocessos elegíveis para aceitação em bloco (GESTOR)
        getSubprocessosElegiveisAceiteBloco: (state) => (idProcesso: number, siglaUnidadeUsuario: string) => {
            if (state.processoDetalhe && state.processoDetalhe.codigo === idProcesso) {
                return state.processoDetalhe.resumoSubprocessos.filter(s => s.unidade.sigla === siglaUnidadeUsuario);
            }
            return [];
        },

        // Subprocessos elegíveis para homologação em bloco (ADMIN)
        getSubprocessosElegiveisHomologacaoBloco: (state) => (idProcesso: number) => {
            if (state.processoDetalhe && state.processoDetalhe.codigo === idProcesso) {
                return state.processoDetalhe.resumoSubprocessos;
            }
            return [];
        },
        getMovementsForSubprocesso: (state) => (idSubprocesso: number) => {
            return state.movements.filter(m => (m as any).idSubprocesso === idSubprocesso).sort((a, b) => new Date(b.dataHora).getTime() - new Date(a.dataHora).getTime());
        }
    },
    actions: {
        async fetchProcessosPainel(perfil: string, unidade: number, page: number, size: number) {
            const response = await painelService.listarProcessos(perfil, unidade, page, size);
            this.processosPainel = response.content;
            this.processosPainelPage = response;
        },
        async fetchProcessosFinalizados() {
            const response = await processoService.fetchProcessosFinalizados();
            this.processosFinalizados = response;
        },
        async fetchProcessoDetalhe(idProcesso: number) {
            this.processoDetalhe = await processoService.obterDetalhesProcesso(idProcesso);
        },
        async criarProcesso(payload: CriarProcessoRequest) {
            await processoService.criarProcesso(payload);
        },
        async atualizarProcesso(idProcesso: number, payload: AtualizarProcessoRequest) {
            await processoService.atualizarProcesso(idProcesso, payload);
        },
        async removerProcesso(idProcesso: number) {
            await processoService.excluirProcesso(idProcesso);
        },
        async iniciarProcesso(idProcesso: number, tipo: TipoProcesso, unidadesIds: number[]) {
            await processoService.iniciarProcesso(idProcesso, tipo, unidadesIds);
            // Após iniciar, é uma boa prática recarregar os detalhes para refletir a mudança de estado
            await this.fetchProcessoDetalhe(idProcesso);
        },
        async finalizarProcesso(idProcesso: number) {
            await processoService.finalizarProcesso(idProcesso);
            // Após finalizar, recarregar os detalhes para refletir a mudança de estado
            await this.fetchProcessoDetalhe(idProcesso);
        },
        async processarCadastroBloco(payload: {
            idProcesso: number,
            unidades: string[],
            tipoAcao: 'aceitar' | 'homologar',
            unidadeUsuario: string
        }) {
            // Esta lógica deve ser movida para o backend.
            // Por enquanto, vamos simular a chamada ao serviço se houver um endpoint para isso.
            // Se não houver, esta action precisará ser refeita para chamar o backend.
            console.warn('processarCadastroBloco: Esta action deve chamar um endpoint de backend.');
            // Exemplo de como seria se houvesse um serviço:
            // await processoService.processarCadastroBloco(payload);

            // Lógica de simulação temporária (remover quando o backend estiver pronto)
            const {idProcesso, unidades, tipoAcao, unidadeUsuario} = payload;
            if (this.processoDetalhe && this.processoDetalhe.codigo === idProcesso) {
                unidades.forEach(siglaUnidade => {
                    const unidadeParticipante = (this.processoDetalhe?.unidades || []).find(up => up.sigla === siglaUnidade);
                    if (unidadeParticipante) {
                        if (tipoAcao === 'aceitar') {
                            unidadeParticipante.situacaoSubprocesso = SituacaoSubprocesso.MAPA_VALIDADO;
                        } else {
                            unidadeParticipante.situacaoSubprocesso = SituacaoSubprocesso.MAPA_HOMOLOGADO;
                        }
                    }
                });
            }
            // Fim da lógica de simulação temporária
        },
        async alterarDataLimiteSubprocesso(payload: {
            idProcesso: number,
            unidade: string,
            etapa: number,
            novaDataLimite: Date
        }) {
            // Esta lógica deve ser movida para o backend.
            console.warn('alterarDataLimiteSubprocesso: Esta action deve chamar um endpoint de backend.');
            // Exemplo de como seria se houvesse um serviço:
            // await processoService.alterarDataLimiteSubprocesso(payload);
        },
        async aceitarMapa(payload: {
            idProcesso: number,
            unidade: string,
            perfil: string
        }) {
            // Esta lógica deve ser movida para o backend.
            console.warn('aceitarMapa: Esta action deve chamar um endpoint de backend.');
            // Exemplo de como seria se houvesse um serviço:
            // await processoService.aceitarMapa(payload);
        },
        async rejeitarMapa(payload: {
            idProcesso: number,
            unidade: string
        }) {
            // Esta lógica deve ser movida para o backend.
            console.warn('rejeitarMapa: Esta action deve chamar um endpoint de backend.');
            // Exemplo de como seria se houvesse um serviço:
            // await processoService.rejeitarMapa(payload);
        },
        async apresentarSugestoes(payload: {
            idProcesso: number,
            unidade: string,
            sugestoes: string
        }) {
            // Esta lógica deve ser movida para o backend.
            console.warn('apresentarSugestoes: Esta action deve chamar um endpoint de backend.');
            // Exemplo de como seria se houvesse um serviço:
            // await processoService.apresentarSugestoes(payload);
        },
        async validarMapa(payload: {
            idProcesso: number,
            unidade: string
        }) {
            // Esta lógica deve ser movida para o backend.
            console.warn('validarMapa: Esta action deve chamar um endpoint de backend.');
            // Exemplo de como seria se houvesse um serviço:
            // await processoService.validarMapa(payload);
        },
        addMovement(movement: Omit<Movimentacao, 'codigo' | 'dataHora'>) {
            const newMovement: Movimentacao = {
                codigo: generateUniqueId(),
                dataHora: new Date().toISOString(),
                ...movement
            };
            this.movements.push(newMovement);
        }
    }
})