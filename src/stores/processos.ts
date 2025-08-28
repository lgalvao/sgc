import {defineStore} from 'pinia'
import processosMock from '../mocks/processos.json'
import subprocessosMock from '../mocks/subprocessos.json'
import {Processo, SituacaoProcesso, Subprocesso} from '@/types/tipos'
import {useConfiguracoesStore} from './configuracoes'; // Import the new store
import {useUnidadesStore} from './unidades'
import { parseDate } from '@/utils/dateUtils'
import { SITUACOES_SUBPROCESSO } from '@/constants/situacoes'

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
        },
        async aceitarMapa(payload: {
            idProcesso: number,
            unidade: string,
            observacao?: string
        }) {
            const { idProcesso, unidade, observacao } = payload;
            const unidadesStore = useUnidadesStore();

            const subprocessoIndex = this.subprocessos.findIndex(
                pu => pu.idProcesso === idProcesso && pu.unidade === unidade
            );

            if (subprocessoIndex !== -1) {
                const subprocesso = this.subprocessos[subprocessoIndex];
                const unidadeSuperior = unidadesStore.getUnidadeImediataSuperior(unidade);

                if (!unidadeSuperior) {
                    throw new Error('Unidade superior não encontrada');
                }

                // Registrar análise
                console.log(`[SIMULAÇÃO] Registrando análise de aceite para unidade ${unidade}`);
                console.log(`Observação: ${observacao || 'Nenhuma'}`);

                // Registrar movimentação
                console.log(`[SIMULAÇÃO] Registrando movimentação:`);
                console.log(`  De: ${unidade}`);
                console.log(`  Para: ${unidadeSuperior}`);
                console.log(`  Descrição: Mapa de competências aceito`);

                // Atualizar situação baseado na unidade superior
                let novaSituacao: string;
                if (unidadeSuperior === 'SEDOC') {
                    novaSituacao = SITUACOES_SUBPROCESSO.MAPA_HOMOLOGADO;
                } else {
                    novaSituacao = SITUACOES_SUBPROCESSO.MAPA_VALIDADO;
                }

                // Atualizar subprocesso
                this.subprocessos[subprocessoIndex] = {
                    ...subprocesso,
                    unidadeAtual: unidadeSuperior,
                    unidadeAnterior: unidade,
                    situacao: novaSituacao
                };

                console.log(`[SIMULAÇÃO] Subprocesso ${unidade} atualizado para: ${novaSituacao}`);
                console.log(`[SIMULAÇÃO] Movido para unidade: ${unidadeSuperior}`);

                return Promise.resolve();
            }

            return Promise.reject(new Error('Subprocesso não encontrado'));
        },
        async rejeitarMapa(payload: {
            idProcesso: number,
            unidade: string
        }) {
            const { idProcesso, unidade } = payload;

            const subprocessoIndex = this.subprocessos.findIndex(
                pu => pu.idProcesso === idProcesso && pu.unidade === unidade
            );

            if (subprocessoIndex !== -1) {
                const subprocesso = this.subprocessos[subprocessoIndex];
                const unidadeInferior = subprocesso.unidadeAnterior;

                if (!unidadeInferior) {
                    throw new Error('Unidade anterior não encontrada');
                }

                // Registrar movimentação
                console.log(`[SIMULAÇÃO] Registrando movimentação:`);
                console.log(`  De: ${unidade}`);
                console.log(`  Para: ${unidadeInferior}`);
                console.log(`  Descrição: Mapa de competências devolvido para ajustes`);

                // Determinar nova situação
                let novaSituacao: string;
                if (unidadeInferior === subprocesso.unidade) {
                    // Retornando para a própria unidade
                    novaSituacao = SITUACOES_SUBPROCESSO.MAPA_DISPONIBILIZADO;
                } else {
                    // Retornando para unidade diferente (SEDOC fazendo ajustes)
                    novaSituacao = SITUACOES_SUBPROCESSO.MAPA_CRIADO;
                }

                // Atualizar subprocesso
                this.subprocessos[subprocessoIndex] = {
                    ...subprocesso,
                    unidadeAtual: unidadeInferior,
                    unidadeAnterior: unidade,
                    situacao: novaSituacao
                };

                console.log(`[SIMULAÇÃO] Subprocesso ${unidade} atualizado para: ${novaSituacao}`);
                console.log(`[SIMULAÇÃO] Movido para unidade: ${unidadeInferior}`);

                return Promise.resolve();
            }

            return Promise.reject(new Error('Subprocesso não encontrado'));
        }
    }
})