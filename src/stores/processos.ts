import {defineStore} from 'pinia'
import processosMock from '../mocks/processos.json'
import subprocessosMock from '../mocks/subprocessos.json'
import {Processo, SituacaoProcesso, Subprocesso} from '@/types/tipos'
import {useConfiguracoesStore} from './configuracoes'; // Import the new store

function parseProcessoDates(processo: any): Processo {
    return {
        ...processo,
        dataLimite: new Date(processo.dataLimite),
        dataFinalizacao: processo.dataFinalizacao ? new Date(processo.dataFinalizacao) : null,
    };
}

function parseSubprocessoDates(pu: any): Subprocesso {
    return {
        ...pu,
        dataLimiteEtapa1: pu.dataLimiteEtapa1 ? new Date(pu.dataLimiteEtapa1) : null,
        dataLimiteEtapa2: pu.dataLimiteEtapa2 ? new Date(pu.dataLimiteEtapa2) : null,
        dataFimEtapa1: pu.dataFimEtapa1 ? new Date(pu.dataFimEtapa1) : null,
        dataFimEtapa2: pu.dataFimEtapa2 ? new Date(pu.dataFimEtapa2) : null,
    };
}

export const useProcessosStore = defineStore('processos', {
    state: () => ({
        processos: processosMock.map(parseProcessoDates) as Processo[],
        subprocessos: subprocessosMock.map(parseSubprocessoDates) as Subprocesso[]
    }),
    getters: {
        getUnidadesDoProcesso: (state) => (idProcesso: number): Subprocesso[] => {
            return state.subprocessos.filter(pu => pu.idProcesso === idProcesso);
        },
        isProcessoInativo: () => (processo: Processo): boolean => {
            const configuracoesStore = useConfiguracoesStore();
            if (processo.situacao === SituacaoProcesso.FINALIZADO && processo.dataFinalizacao) {
                const finalizacaoDate = new Date(processo.dataFinalizacao);
                const today = new Date();
                const diffTime = Math.abs(today.getTime() - finalizacaoDate.getTime());
                const diffDays = Math.ceil(diffTime / (1000 * 60 * 60 * 24));
                return diffDays > configuracoesStore.diasInativacaoProcesso;
            }
            return false;
        },
        // Subprocessos elegíveis para aceitação em bloco (GESTOR)
        getSubprocessosElegiveisAceiteBloco: (state) => (idProcesso: number, siglaUnidadeUsuario: string) => {
            return state.subprocessos.filter(pu => 
                pu.idProcesso === idProcesso && 
                pu.unidadeAtual === siglaUnidadeUsuario &&
                (pu.situacao === 'Cadastro disponibilizado' || pu.situacao === 'Revisão do cadastro disponibilizada')
            );
        },
        
        // Subprocessos elegíveis para homologação em bloco (ADMIN)
        getSubprocessosElegiveisHomologacaoBloco: (state) => (idProcesso: number) => {
            return state.subprocessos.filter(pu => 
                pu.idProcesso === idProcesso && 
                (pu.situacao === 'Cadastro disponibilizado' || pu.situacao === 'Revisão do cadastro disponibilizada')
            );
        }
    },
    actions: {
        adicionarProcesso(novoProcesso: Processo) {
            this.processos.push(novoProcesso);
        },
        adicionarsubprocessos(subprocessosArray: Subprocesso[]) {
            subprocessosArray.forEach((pu: Subprocesso) => {
                this.subprocessos.push(pu);
            });
        },
        finalizarProcesso(idProcesso: number) {
            const processo = this.processos.find(p => p.id === idProcesso);
            if (processo) {
                processo.situacao = SituacaoProcesso.FINALIZADO;
                processo.dataFinalizacao = new Date(); // Agora é um objeto Date
            }
        },
        async processarCadastroBloco(payload: {
            idProcesso: number,
            unidades: string[],
            tipoAcao: 'aceitar' | 'homologar',
            observacao?: string,
            unidadeUsuario: string
        }) {
            const { idProcesso, unidades, tipoAcao, observacao, unidadeUsuario } = payload;
            
            // Processar cada unidade
            for (const siglaUnidade of unidades) {
                const subprocessoIndex = this.subprocessos.findIndex(
                    pu => pu.idProcesso === idProcesso && pu.unidade === siglaUnidade
                );
                
                if (subprocessoIndex !== -1) {
                    const subprocesso = this.subprocessos[subprocessoIndex];
                    
                    if (tipoAcao === 'aceitar') {
                        // Para GESTOR - aceitar e encaminhar
                        // Registrar análise
                        console.log(`[SIMULAÇÃO] Registrando análise de aceite para unidade ${siglaUnidade}`);
                        console.log(`Observação: ${observacao || 'Nenhuma'}`);
                        
                        // Registrar movimentação
                        console.log(`[SIMULAÇÃO] Registrando movimentação:`);
                        console.log(`  De: ${unidadeUsuario}`);
                        console.log(`  Para: Unidade superior hierárquica`);
                        console.log(`  Descrição: Cadastro de atividades e conhecimentos validado em bloco`);
                        
                        // Atualizar situação do subprocesso
                        this.subprocessos[subprocessoIndex] = {
                            ...subprocesso,
                            // Manter a mesma situação por enquanto, já que estamos simulando
                        };
                    } else {
                        // Para ADMIN - homologar
                        // Registrar movimentação
                        console.log(`[SIMULAÇÃO] Registrando movimentação:`);
                        console.log(`  De: SEDOC`);
                        console.log(`  Para: SEDOC`);
                        console.log(`  Descrição: Cadastro de atividades e conhecimentos homologado em bloco`);
                        
                        // Atualizar situação do subprocesso
                        const novaSituacao = subprocesso.situacao.includes('Revisão') 
                            ? 'Revisão do cadastro homologada' 
                            : 'Cadastro homologado';
                            
                        this.subprocessos[subprocessoIndex] = {
                            ...subprocesso,
                            situacao: novaSituacao
                        };
                        
                        console.log(`[SIMULAÇÃO] Subprocesso ${siglaUnidade} atualizado para: ${novaSituacao}`);
                    }
                }
            }
            
            // Simular criação de alertas
            console.log(`[SIMULAÇÃO] Criando alertas para ${unidades.length} unidades`);
            
            // Simular envio de notificações
            console.log(`[SIMULAÇÃO] Enviando notificações para unidades superiores`);
            
            return Promise.resolve();
        }
    }
})