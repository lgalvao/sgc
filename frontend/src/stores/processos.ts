import {defineStore} from 'pinia'
import {
    AtualizarProcessoRequest,
    CriarProcessoRequest,
    Movimentacao,
    Processo,
    ProcessoResumo,
    SubprocessoElegivel,
    TipoProcesso
} from '@/types/tipos'
import {generateUniqueId} from '@/utils'
import * as painelService from '../services/painelService'
import {Page} from '@/services/painelService'
import * as processoService from '../services/processoService'
import {usePerfilStore} from "@/stores/perfil";

export const useProcessosStore = defineStore('processos', {
    state: () => ({
        processosPainel: [] as ProcessoResumo[],
        processosPainelPage: {} as Page<ProcessoResumo>,
        processoDetalhe: null as Processo | null,
        subprocessosElegiveis: [] as SubprocessoElegivel[],
        processosFinalizados: [] as ProcessoResumo[],
        movements: [] as Movimentacao[],
    }),
    getters: {
        getUnidadesDoProcesso: (state) => (idProcesso: number): ProcessoResumo[] => {
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
        async fetchProcessosPainel(
            perfil: string,
            unidade: number,
            page: number,
            size: number,
            sort?: keyof ProcessoResumo,
            order?: 'asc' | 'desc'
        ) {
            const response = await painelService.listarProcessos(perfil, unidade, page, size, sort, order);
            this.processosPainel = response.content;
            this.processosPainelPage = response;
        },
        async fetchProcessosFinalizados() {
            this.processosFinalizados = await processoService.fetchProcessosFinalizados();
        },
        async fetchProcessoDetalhe(idProcesso: number) {
            this.processoDetalhe = await processoService.obterDetalhesProcesso(idProcesso);
        },
        async fetchSubprocessosElegiveis(idProcesso: number) {
            this.subprocessosElegiveis = await processoService.fetchSubprocessosElegiveis(idProcesso);
        },
        async criarProcesso(payload: CriarProcessoRequest) {
            const novoProcesso = await processoService.criarProcesso(payload);
            const perfilStore = usePerfilStore();
            if (perfilStore.perfilSelecionado && perfilStore.unidadeSelecionada) {
                await this.fetchProcessosPainel(perfilStore.perfilSelecionado, Number(perfilStore.unidadeSelecionada), 0, 10);
            }
            return novoProcesso;
        },
        async atualizarProcesso(idProcesso: number, payload: AtualizarProcessoRequest) {
            await processoService.atualizarProcesso(idProcesso, payload);
            const perfilStore = usePerfilStore();
            if (perfilStore.perfilSelecionado && perfilStore.unidadeSelecionada) {
                await this.fetchProcessosPainel(perfilStore.perfilSelecionado, Number(perfilStore.unidadeSelecionada), 0, 10);
            }
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
            codProcesso: number,
            unidades: string[],
            tipoAcao: 'aceitar' | 'homologar',
            unidadeUsuario: string
        }) {
            await processoService.processarAcaoEmBloco(payload);
            // Após a ação em bloco, recarregar os detalhes do processo para refletir as mudanças
            await this.fetchProcessoDetalhe(payload.codProcesso);
        },
        async alterarDataLimiteSubprocesso(id: number, dados: { novaData: string }) {
            await processoService.alterarDataLimiteSubprocesso(id, dados);
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