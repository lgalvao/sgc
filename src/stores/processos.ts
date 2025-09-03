import {defineStore} from 'pinia'
import processosMock from '../mocks/processos.json'
import subprocessosMock from '../mocks/subprocessos.json'
import {Movimentacao, Processo, SituacaoProcesso, Subprocesso, TipoProcesso} from '@/types/tipos'
import {useConfiguracoesStore} from './configuracoes'; // Import the new store
import {useUnidadesStore} from './unidades'
import {useAnalisesStore} from './analises'
import {useAlertasStore} from './alertas'
import {parseDate} from '@/utils/dateUtils'
import {SITUACOES_SUBPROCESSO} from '@/constants/situacoes'
import {generateUniqueId} from '@/utils/idGenerator'
import {useNotificacoesStore} from './notificacoes'

function mapTipoProcesso(tipo: string): TipoProcesso {
    switch (tipo) {
        case 'Mapeamento': return TipoProcesso.MAPEAMENTO;
        case 'Revisão': return TipoProcesso.REVISAO;
        case 'Diagnóstico': return TipoProcesso.DIAGNOSTICO;
        default: return TipoProcesso.MAPEAMENTO;
    }
}

function mapSituacaoProcesso(situacao: string): SituacaoProcesso {
    switch (situacao) {
        case 'Criado': return SituacaoProcesso.CRIADO;
        case 'Em andamento': return SituacaoProcesso.EM_ANDAMENTO;
        case 'Finalizado': return SituacaoProcesso.FINALIZADO;
        default: return SituacaoProcesso.CRIADO;
    }
}

function parseProcessoDates(processo: Omit<Processo, 'dataLimite' | 'dataFinalizacao' | 'tipo' | 'situacao'> & { dataLimite: string, dataFinalizacao?: string | null, tipo: string, situacao: string }): Processo {
    return {
        ...processo,
        tipo: mapTipoProcesso(processo.tipo),
        situacao: mapSituacaoProcesso(processo.situacao),
        dataLimite: parseDate(processo.dataLimite) || new Date(),
        dataFinalizacao: processo.dataFinalizacao ? parseDate(processo.dataFinalizacao) : null,
    };
}

function parseSubprocessoDates(pu: Omit<Subprocesso, 'dataLimiteEtapa1' | 'dataLimiteEtapa2' | 'dataFimEtapa1' | 'dataFimEtapa2'> & { dataLimiteEtapa1?: string | null, dataLimiteEtapa2?: string | null, dataFimEtapa1?: string | null, dataFimEtapa2?: string | null }): Subprocesso {
    return {
        ...pu,
        dataLimiteEtapa1: pu.dataLimiteEtapa1 ? parseDate(pu.dataLimiteEtapa1) || new Date() : new Date(),
        dataLimiteEtapa2: pu.dataLimiteEtapa2 ? parseDate(pu.dataLimiteEtapa2) : null,
        dataFimEtapa1: pu.dataFimEtapa1 ? parseDate(pu.dataFimEtapa1) : null,
        dataFimEtapa2: pu.dataFimEtapa2 ? parseDate(pu.dataFimEtapa2) : null,
    };
}

export const useProcessosStore = defineStore('processos', {
    state: () => ({
        processos: processosMock.map(parseProcessoDates) as Processo[],
        subprocessos: subprocessosMock.map(parseSubprocessoDates) as Subprocesso[],
        movements: [] as Movimentacao[]
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
        },
        getMovementsForSubprocesso: (state) => (idSubprocesso: number) => {
            return state.movements.filter(m => m.idSubprocesso === idSubprocesso).sort((a, b) => b.dataHora.getTime() - a.dataHora.getTime());
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
                        // Registrar movimentação
                        const subprocessoAceite = this.subprocessos.find(pu => pu.idProcesso === idProcesso && pu.unidade === siglaUnidade);
                        if (subprocessoAceite) {
                            this.addMovement({
                                idSubprocesso: subprocessoAceite.id,
                                unidadeOrigem: unidadeUsuario,
                                unidadeDestino: 'Unidade superior hierárquica',
                                descricao: 'Cadastro de atividades e conhecimentos validado em bloco'
                            });
                        }
                        
                        // Atualizar situação do subprocesso
                        this.subprocessos[subprocessoIndex] = {
                            ...subprocesso,
                            // Manter a mesma situação por enquanto, já que estamos simulando
                        };
                    } else {
                        // Para ADMIN - homologar
                        // Registrar movimentação
                        const subprocessoHomologar = this.subprocessos.find(pu => pu.idProcesso === idProcesso && pu.unidade === siglaUnidade);
                        if (subprocessoHomologar) {
                            this.addMovement({
                                idSubprocesso: subprocessoHomologar.id,
                                unidadeOrigem: 'SEDOC',
                                unidadeDestino: 'SEDOC',
                                descricao: 'Cadastro de atividades e conhecimentos homologado em bloco'
                            });
                        }
                        
                        // Atualizar situação do subprocesso
                        const novaSituacao = subprocesso.situacao.includes('Revisão') 
                            ? 'Revisão do cadastro homologada' 
                            : 'Cadastro homologado';
                            
                        this.subprocessos[subprocessoIndex] = {
                            ...subprocesso,
                            situacao: novaSituacao
                        };
                    }
                }
            }
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
                this.addMovement({
                    idSubprocesso: subprocesso.id,
                    unidadeOrigem: 'SEDOC',
                    unidadeDestino: 'SEDOC',
                    descricao: `Data limite da etapa ${etapa} alterada para ${novaDataLimite.toISOString().split('T')[0]}`
                });

                // Enviar notificação por e-mail
                const notificacoesStore = useNotificacoesStore();
                notificacoesStore.email(
                    `SGC: Data limite de etapa alterada - ${unidade}`,
                    `Responsável pela ${unidade}`,
                    `Prezado(a) responsável pela ${unidade},\n\nA data limite da etapa ${etapa} no processo foi alterada para ${novaDataLimite.toISOString().split('T')[0]}.\n\nMais informações no Sistema de Gestão de Competências.`
                );
                
                return Promise.resolve();
            }
            
            return Promise.reject(new Error('Subprocesso não encontrado'));
        },
        async aceitarMapa(payload: {
            idProcesso: number,
            unidade: string,
            observacao?: string,
            perfil: string
        }) {
            const { idProcesso, unidade, observacao, perfil } = payload;
            const unidadesStore = useUnidadesStore();
            const analisesStore = useAnalisesStore();
            const alertasStore = useAlertasStore();
            const notificacoesStore = useNotificacoesStore();

            const subprocessoIndex = this.subprocessos.findIndex(
                pu => pu.idProcesso === idProcesso && pu.unidade === unidade
            );

            if (subprocessoIndex !== -1) {
                const subprocesso = this.subprocessos[subprocessoIndex];
                const unidadeSuperior = unidadesStore.getUnidadeImediataSuperior(unidade);

                if (!unidadeSuperior) {
                    throw new Error('Unidade superior não encontrada');
                }

                // Registrar análise de validação
                analisesStore.registrarAnalise({
                    idSubprocesso: subprocesso.id,
                    dataHora: new Date(),
                    unidade: unidade,
                    resultado: 'Aceite',
                    observacao: observacao
                });

                if (perfil === 'ADMIN') {
                    // ADMIN: homologar diretamente
                    this.subprocessos[subprocessoIndex] = {
                        ...subprocesso,
                        situacao: SITUACOES_SUBPROCESSO.MAPA_HOMOLOGADO
                    };
                } else {
                    // GESTOR: enviar para superior
                    this.addMovement({
                        idSubprocesso: subprocesso.id,
                        unidadeOrigem: unidade,
                        unidadeDestino: unidadeSuperior,
                        descricao: 'Mapa de competências validado'
                    });

                    // Atualizar subprocesso
                    this.subprocessos[subprocessoIndex] = {
                        ...subprocesso,
                        unidadeAtual: unidadeSuperior,
                        unidadeAnterior: unidade,
                        situacao: SITUACOES_SUBPROCESSO.MAPA_VALIDADO
                    };

                    // Enviar email para unidade superior
                    notificacoesStore.email(
                        `SGC: Validação do mapa de competências da ${unidade} submetida para análise`,
                        `Responsável pela ${unidadeSuperior}`,
                        `Prezado(a) responsável pela ${unidadeSuperior},\n\nA validação do mapa de competências da ${unidade} no processo ${idProcesso} foi submetida para análise por essa unidade.\n\nA análise já pode ser realizada no Sistema de Gestão de Competências.`
                    );

                    // Criar alerta interno
                    alertasStore.criarAlerta({
                        unidadeOrigem: unidade,
                        unidadeDestino: unidadeSuperior,
                        dataHora: new Date(),
                        idProcesso: idProcesso,
                        descricao: `Validação do mapa de competências da ${unidade} submetida para análise`
                    });
                }

                return Promise.resolve();
            }

            return Promise.reject(new Error('Subprocesso não encontrado'));
        },
        async rejeitarMapa(payload: {
            idProcesso: number,
            unidade: string,
            observacao?: string
        }) {
            const { idProcesso, unidade, observacao } = payload;
            const analisesStore = useAnalisesStore();

            const subprocessoIndex = this.subprocessos.findIndex(
                pu => pu.idProcesso === idProcesso && pu.unidade === unidade
            );

            if (subprocessoIndex !== -1) {
                const subprocesso = this.subprocessos[subprocessoIndex];
                const unidadeInferior = subprocesso.unidadeAnterior;

                if (!unidadeInferior) {
                    throw new Error('Unidade anterior não encontrada');
                }

                // Registrar análise de validação
                analisesStore.registrarAnalise({
                    idSubprocesso: subprocesso.id,
                    dataHora: new Date(),
                    unidade: unidade,
                    resultado: 'Devolução',
                    observacao: observacao
                });

                // Registrar movimentação
                this.addMovement({
                    idSubprocesso: subprocesso.id,
                    unidadeOrigem: unidade,
                    unidadeDestino: unidadeInferior,
                    descricao: 'Devolução da validação do mapa de competências para ajustes'
                });

                // Determinar nova situação
                let novaSituacao: string;
                if (unidadeInferior === subprocesso.unidade) {
                    // Retornando para a própria unidade
                    novaSituacao = SITUACOES_SUBPROCESSO.MAPA_DISPONIBILIZADO;
                    // Resetar dataFimEtapa2 conforme requisitos
                    this.subprocessos[subprocessoIndex] = {
                        ...subprocesso,
                        unidadeAtual: unidadeInferior,
                        unidadeAnterior: unidade,
                        situacao: novaSituacao,
                        dataFimEtapa2: null
                    };
                } else {
                    // Retornando para unidade diferente (SEDOC fazendo ajustes)
                    novaSituacao = SITUACOES_SUBPROCESSO.MAPA_CRIADO;
                    // Atualizar subprocesso
                    this.subprocessos[subprocessoIndex] = {
                        ...subprocesso,
                        unidadeAtual: unidadeInferior,
                        unidadeAnterior: unidade,
                        situacao: novaSituacao
                    };
                }

                // Enviar notificação por email para unidade de devolução
                const notificacoesStore = useNotificacoesStore();
                notificacoesStore.email(
                    `SGC: Validação do mapa de competências da ${subprocesso.unidade} devolvida para ajustes`,
                    `Responsável pela ${unidadeInferior}`,
                    `Prezado(a) responsável pela ${unidadeInferior},\n\nA validação do mapa de competências da ${subprocesso.unidade} no processo ${idProcesso} foi devolvida para ajustes.\n\nAcompanhe o processo no Sistema de Gestão de Competências.`
                );

                // Criar alerta interno
                const alertasStore = useAlertasStore();
                alertasStore.criarAlerta({
                    unidadeOrigem: unidade,
                    unidadeDestino: unidadeInferior,
                    dataHora: new Date(),
                    idProcesso: idProcesso,
                    descricao: `Cadastro de atividades e conhecimentos da unidade ${subprocesso.unidade} devolvido para ajustes`
                });

                return Promise.resolve();
            }

            return Promise.reject(new Error('Subprocesso não encontrado'));
        },
        async apresentarSugestoes(payload: {
            idProcesso: number,
            unidade: string,
            sugestoes: string
        }) {
            const { idProcesso, unidade, sugestoes } = payload;
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

                // Registrar movimentação
                this.addMovement({
                    idSubprocesso: subprocesso.id,
                    unidadeOrigem: unidade,
                    unidadeDestino: unidadeSuperior,
                    descricao: 'Apresentação de sugestões para o mapa de competências'
                });

                // Atualizar situação e armazenar sugestões
                this.subprocessos[subprocessoIndex] = {
                    ...subprocesso,
                    unidadeAtual: unidadeSuperior,
                    unidadeAnterior: unidade,
                    situacao: SITUACOES_SUBPROCESSO.MAPA_COM_SUGESTOES,
                    sugestoes: sugestoes
                };

                // Simular envio de e-mail
                const notificacoesStore = useNotificacoesStore();
                notificacoesStore.email(
                    `SGC: Sugestões apresentadas para o mapa de competências da ${unidade}`,
                    `Responsável pela ${unidadeSuperior}`,
                    `Prezado(a) responsável pela ${unidadeSuperior},\n\nA unidade ${unidade} apresentou sugestões para o mapa de competências elaborado no processo ${idProcesso}.\n\nA análise dessas sugestões já pode ser realizada no Sistema de Gestão de Competências.`
                );

                return Promise.resolve();
            }

            return Promise.reject(new Error('Subprocesso não encontrado'));
        },
        async validarMapa(payload: {
            idProcesso: number,
            unidade: string
        }) {
            const { idProcesso, unidade } = payload;
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

                // Registrar movimentação
                this.addMovement({
                    idSubprocesso: subprocesso.id,
                    unidadeOrigem: unidade,
                    unidadeDestino: unidadeSuperior,
                    descricao: 'Validação do mapa de competências'
                });

                // Atualizar situação
                this.subprocessos[subprocessoIndex] = {
                    ...subprocesso,
                    unidadeAtual: unidadeSuperior,
                    unidadeAnterior: unidade,
                    situacao: SITUACOES_SUBPROCESSO.MAPA_VALIDADO
                };

                // Simular envio de e-mail
                const notificacoesStore = useNotificacoesStore();
                notificacoesStore.email(
                    `SGC: Validação do mapa de competências da ${unidade} submetida para análise`,
                    `Responsável pela ${unidadeSuperior}`,
                    `Prezado(a) responsável pela ${unidadeSuperior},\n\nA unidade ${unidade} validou o mapa de competências elaborado no processo ${idProcesso}.\n\nA análise dessa validação já pode ser realizada no Sistema de Gestão de Competências.`
                );

                return Promise.resolve();
            }

            return Promise.reject(new Error('Subprocesso não encontrado'));
        },
        addMovement(movement: Omit<Movimentacao, 'id' | 'dataHora'>) {
            const newMovement: Movimentacao = {
                id: generateUniqueId(),
                dataHora: new Date(),
                ...movement
            };
            this.movements.push(newMovement);
        }
    }
})