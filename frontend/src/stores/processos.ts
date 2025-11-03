import {defineStore} from 'pinia'
import {
    AtualizarProcessoRequest,
    CriarProcessoRequest,
    Movimentacao,
    ProcessoDetalhe,
    ProcessoResumo,
    TipoProcesso
} from '@/types/tipos'
import {generateUniqueId} from '@/utils'
import * as painelService from '../services/painelService'
import {Page} from '@/services/painelService'
import * as processoService from '../services/processoService'

export const useProcessosStore = defineStore('processos', {
    state: () => ({
        processosPainel: [] as ProcessoResumo[],
        processosPainelPage: {} as Page<ProcessoResumo>,
        processoDetalhe: null as ProcessoDetalhe | null, // Para armazenar o processo detalhado
        processosFinalizados: [] as ProcessoResumo[],
        movements: [] as Movimentacao[] // Manter se ainda for usado para mocks internos ou outras lógicas
    }),
    getters: {
        getUnidadesDoProcesso: (state) => (idProcesso: number): ProcessoResumo[] => {
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
                return state.processoDetalhe.resumoSubprocessos.filter(s => s.unidadeNome === siglaUnidadeUsuario);
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
        getMovementsForSubprocesso: (state) => (codSubrocesso: number) => {
            return state.movements.filter(m => (m as any).codSubrocesso === codSubrocesso)
                .sort((a, b) => new Date(b.dataHora).getTime() - new Date(a.dataHora).getTime());
        }
    },
    actions: {
        async fetchProcessosPainel(perfil: string, unidade: number, page: number, size: number) {
            const response = await painelService.listarProcessos(perfil, unidade, page, size);
            this.processosPainel = response.content;
            this.processosPainelPage = response;
        },
        async fetchProcessosFinalizados() {
            this.processosFinalizados = await processoService.fetchProcessosFinalizados();
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
            await processoService.processarAcaoEmBloco(payload);
            // Após a ação em bloco, recarregar os detalhes do processo para refletir as mudanças
            await this.fetchProcessoDetalhe(payload.idProcesso);
        },
        async alterarDataLimiteSubprocesso(id: number, dados: { novaData: string }) {
            await processoService.alterarDataLimiteSubprocesso(id, dados);
            await this.fetchProcessoDetalhe(this.processoDetalhe!.codigo);
        },
        async aceitarMapa(id: number, dados: { observacoes: string }) {
            await processoService.aceitarMapa(id, dados);
            await this.fetchProcessoDetalhe(this.processoDetalhe!.codigo);
        },
        async rejeitarMapa(id: number, dados: { motivo: string; observacoes: string }) {
            await processoService.rejeitarMapa(id, dados);
            await this.fetchProcessoDetalhe(this.processoDetalhe!.codigo);
        },
        async apresentarSugestoes(id: number, dados: { sugestoes: string }) {
            await processoService.apresentarSugestoes(id, dados);
            await this.fetchProcessoDetalhe(this.processoDetalhe!.codigo);
        },
        async validarMapa(id: number) {
            await processoService.validarMapa(id);
            await this.fetchProcessoDetalhe(this.processoDetalhe!.codigo);
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