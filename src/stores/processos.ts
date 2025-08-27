import {defineStore} from 'pinia'
import processosMock from '../mocks/processos.json'
import subprocessosMock from '../mocks/subprocessos.json'
import {Processo, SituacaoProcesso, Subprocesso} from '@/types/tipos'
import {useConfiguracoesStore} from './configuracoes'; // Import the new store
import { parseDate } from '@/utils/dateUtils'

function parseProcessoDates(processo: any): Processo {
    return {
        ...processo,
        dataLimite: parseDate(processo.dataLimite) || new Date(),
        dataFinalizacao: parseDate(processo.dataFinalizacao),
    };
}

function parseSubprocessoDates(pu: any): Subprocesso {
    return {
        ...pu,
        dataLimiteEtapa1: parseDate(pu.dataLimiteEtapa1),
        dataLimiteEtapa2: parseDate(pu.dataLimiteEtapa2),
        dataFimEtapa1: parseDate(pu.dataFimEtapa1),
        dataFimEtapa2: parseDate(pu.dataFimEtapa2),
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
        },
        async alterarDataLimiteSubprocesso(payload: {
            idProcesso: number,
            unidade: string,
            etapa: number,
            novaDataLimite: Date
        }) {
            const { idProcesso, unidade, etapa, novaDataLimite } = payload;
            
            const subprocessoIndex = this.subprocessos.findIndex(
                pu => pu.idProcesso === idProcesso && pu.unidade === unidade
            );
            
            if (subprocessoIndex !== -1) {
                const subprocesso = this.subprocessos[subprocessoIndex];
                
                // Atualizar a data limite da etapa especificada
                if (etapa === 1) {
                    this.subprocessos[subprocessoIndex] = {
                        ...subprocesso,
                        dataLimiteEtapa1: novaDataLimite
                    };
                } else if (etapa === 2) {
                    this.subprocessos[subprocessoIndex] = {
                        ...subprocesso,
                        dataLimiteEtapa2: novaDataLimite
                    };
                }
                
                // Registrar movimentação
                console.log(`[SIMULAÇÃO] Registrando movimentação:`);
                console.log(`  De: SEDOC`);
                console.log(`  Para: SEDOC`);
                console.log(`  Descrição: Data limite da etapa ${etapa} alterada para ${novaDataLimite.toISOString().split('T')[0]}`);
                
                // Criar alerta
                console.log(`[SIMULAÇÃO] Criando alerta:`);
                console.log(`  Descrição: Data limite da etapa ${etapa} alterada para ${novaDataLimite.toISOString().split('T')[0]}`);
                console.log(`  Processo: ${idProcesso}`);
                console.log(`  Data/hora: ${new Date().toISOString()}`);
                console.log(`  Unidade de origem: SEDOC`);
                console.log(`  Unidade de destino: ${unidade}`);
                
                // Enviar notificação
                console.log(`[SIMULAÇÃO] Enviando notificação por e-mail para a unidade ${unidade}:`);
                console.log(`  Assunto: SGC: Data limite de etapa alterada - ${unidade}`);
                console.log(`  Prezado(a) responsável pela ${unidade},`);
                console.log(`  A data limite da etapa ${etapa} no processo foi alterada para ${novaDataLimite.toISOString().split('T')[0]}.`);
                console.log(`  Mais informações no Sistema de Gestão de Competências.`);
                
                return Promise.resolve();
            }
            
            return Promise.reject(new Error('Subprocesso não encontrado'));
        }
    }
})