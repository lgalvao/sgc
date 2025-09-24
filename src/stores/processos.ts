import {defineStore} from 'pinia'
import processosMock from '../mocks/processos.json' assert {type: 'json'}
import subprocessosMock from '../mocks/subprocessos.json' assert {type: 'json'}
import {Movimentacao, Processo, SituacaoProcesso, Subprocesso, TipoProcesso} from '@/types/tipos'
import {useUnidadesStore} from './unidades'

import {useAlertasStore} from './alertas'
import {generateUniqueId, parseDate} from '@/utils'
import {SITUACOES_SUBPROCESSO} from '@/constants/situacoes'
import {useNotificacoesStore} from './notificacoes'

export function mapTipoProcesso(tipo: string): TipoProcesso {
    switch (tipo) {
        case 'Mapeamento':
            return TipoProcesso.MAPEAMENTO;
        case 'Revisão':
            return TipoProcesso.REVISAO;
        case 'Diagnóstico':
            return TipoProcesso.DIAGNOSTICO;
        default:
            return TipoProcesso.MAPEAMENTO;
    }
}

export function mapSituacaoProcesso(situacao: string): SituacaoProcesso {
    switch (situacao) {
        case 'Criado':
            return SituacaoProcesso.CRIADO;
        case 'Em andamento':
            return SituacaoProcesso.EM_ANDAMENTO;
        case 'Finalizado':
            return SituacaoProcesso.FINALIZADO;
        default:
            return SituacaoProcesso.CRIADO;
    }
}

function parseProcessoDates(processo: Omit<Processo, 'dataLimite' | 'dataFinalizacao' | 'tipo' | 'situacao'> & {
    dataLimite: string,
    dataFinalizacao?: string | null,
    tipo: string,
    situacao: string
}): Processo {
    return {
        ...processo,
        tipo: mapTipoProcesso(processo.tipo),
        situacao: mapSituacaoProcesso(processo.situacao),
        dataLimite: parseDate(processo.dataLimite) || new Date(),
        dataFinalizacao: processo.dataFinalizacao ? parseDate(processo.dataFinalizacao) : null,
    };
}

function parseSubprocessoDates(pu: Partial<Subprocesso>): Subprocesso {
    return {
        id: pu.id || 0,
        idProcesso: pu.idProcesso || 0,
        unidade: pu.unidade || '',
        situacao: pu.situacao || SITUACOES_SUBPROCESSO.NAO_INICIADO,
        unidadeAtual: pu.unidadeAtual || '',
        unidadeAnterior: pu.unidadeAnterior || null,
        dataLimiteEtapa1: typeof pu.dataLimiteEtapa1 === 'string' ? parseDate(pu.dataLimiteEtapa1) || new Date() : new Date(),
        dataFimEtapa1: typeof pu.dataFimEtapa1 === 'string' ? parseDate(pu.dataFimEtapa1) : null,
        dataLimiteEtapa2: typeof pu.dataLimiteEtapa2 === 'string' ? parseDate(pu.dataLimiteEtapa2) : null,
        dataFimEtapa2: typeof pu.dataFimEtapa2 === 'string' ? parseDate(pu.dataFimEtapa2) : null,
        sugestoes: pu.sugestoes || undefined,
        observacoes: pu.observacoes || undefined,
        movimentacoes: pu.movimentacoes || [],
        analises: pu.analises || [],
        idMapaCopiado: pu.idMapaCopiado || undefined,
    };
}

export const useProcessosStore = defineStore('processos', {
    state: () => ({
        processos: processosMock.map(parseProcessoDates) as Processo[],
        // eslint-disable-next-line @typescript-eslint/no-explicit-any
        subprocessos: (subprocessosMock as any[]).map(parseSubprocessoDates) as Subprocesso[],
        movements: [] as Movimentacao[]
    }),
    getters: {
        getUnidadesDoProcesso: (state) => (idProcesso: number): Subprocesso[] => {
            return state.subprocessos.filter(pu => pu.idProcesso === idProcesso);
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
        removerProcesso(idProcesso: number) {
            // Coletar os IDs dos subprocessos relacionados ANTES de removê-los
            const subprocessosDoProcesso = this.subprocessos.filter(sp => sp.idProcesso === idProcesso);
            const subprocessosIds = subprocessosDoProcesso.map(sp => sp.id);

            // Remove o processo
            const processoIndex = this.processos.findIndex(p => p.id === idProcesso);
            if (processoIndex !== -1) {
                this.processos.splice(processoIndex, 1);
            }
            // Remove todos os subprocessos relacionados
            this.subprocessos = this.subprocessos.filter(sp => sp.idProcesso !== idProcesso);
            // Remove todas as movimentações relacionadas
            this.movements = this.movements.filter(m => !subprocessosIds.includes(m.idSubprocesso));
        },
        editarProcesso(dadosProcesso: {
            id: number,
            descricao: string,
            tipo: TipoProcesso,
            dataLimite: Date,
            unidades: string[]
        }) {
            const processoIndex = this.processos.findIndex(p => p.id === dadosProcesso.id);
            if (processoIndex !== -1) {
                // Atualizar dados do processo
                this.processos[processoIndex] = {
                    ...this.processos[processoIndex],
                    descricao: dadosProcesso.descricao,
                    tipo: dadosProcesso.tipo,
                    dataLimite: dadosProcesso.dataLimite
                };
                
                // Atualizar subprocessos - remover os antigos e adicionar os novos
                this.subprocessos = this.subprocessos.filter(sp => sp.idProcesso !== dadosProcesso.id);
                
                const novosSubprocessos = dadosProcesso.unidades.map((unidadeSigla) => ({
                    id: generateUniqueId(),
                    idProcesso: dadosProcesso.id,
                    unidade: unidadeSigla,
                    dataLimiteEtapa1: dadosProcesso.dataLimite,
                    dataLimiteEtapa2: dadosProcesso.dataLimite,
                    dataFimEtapa1: null,
                    dataFimEtapa2: null,
                    unidadeAtual: unidadeSigla,
                    unidadeAnterior: null,
                    situacao: SITUACOES_SUBPROCESSO.NAO_INICIADO,
                    movimentacoes: [],
                    analises: []
                }));
                
                this.subprocessos.push(...novosSubprocessos);
            }
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
            unidadeUsuario: string
        }) {
            const {idProcesso, unidades, tipoAcao, unidadeUsuario} = payload;

            // Processar cada unidade
            for (const siglaUnidade of unidades) {
                const subprocessoIndex = this.subprocessos.findIndex(
                    pu => pu.idProcesso === idProcesso && pu.unidade === siglaUnidade
                );

                if (subprocessoIndex !== -1) {
                    const subprocesso = this.subprocessos[subprocessoIndex];

                    if (tipoAcao === 'aceitar') {
                        // Registrar movimentação
                        this.addMovement({
                            idSubprocesso: subprocesso.id, // Usar subprocesso diretamente
                            unidadeOrigem: unidadeUsuario,
                            unidadeDestino: 'Unidade superior hierárquica',
                            descricao: 'Cadastro de atividades e conhecimentos validado em bloco'
                        });

                        // Atualizar situação do subprocesso
                        this.subprocessos[subprocessoIndex] = {
                            ...subprocesso,
                            // Manter a mesma situação por enquanto, já que estamos simulando
                            movimentacoes: subprocesso.movimentacoes || [],
                        };
                    } else {
                        // Para ADMIN - homologar
                        // Registrar movimentação
                        this.addMovement({
                            idSubprocesso: subprocesso.id, // Usar subprocesso diretamente
                            unidadeOrigem: 'SEDOC',
                            unidadeDestino: 'SEDOC',
                            descricao: 'Cadastro de atividades e conhecimentos homologado em bloco'
                        });

                        // Atualizar situação do subprocesso
                        const novaSituacao = subprocesso.situacao.includes('Revisão')
                            ? 'Revisão do cadastro homologada'
                            : 'Cadastro homologado';

                        this.subprocessos[subprocessoIndex] = {
                            ...subprocesso,
                            situacao: novaSituacao,
                            movimentacoes: subprocesso.movimentacoes || [],
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
            const {idProcesso, unidade, etapa, novaDataLimite} = payload;

            const subprocessoIndex = this.subprocessos.findIndex(
                pu => pu.idProcesso === idProcesso && pu.unidade === unidade
            );

            if (subprocessoIndex !== -1) {
                const subprocesso = this.subprocessos[subprocessoIndex];

                // Atualizar a data limite da etapa especificada
                if (etapa === 1) {
                    this.subprocessos[subprocessoIndex] = {
                        ...subprocesso,
                        dataLimiteEtapa1: novaDataLimite,
                        movimentacoes: subprocesso.movimentacoes || [],
                    };
                } else if (etapa === 2) {
                    this.subprocessos[subprocessoIndex] = {
                        ...subprocesso,
                        dataLimiteEtapa2: novaDataLimite,
                        movimentacoes: subprocesso.movimentacoes || [],
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
            perfil: string
        }) {
            const {idProcesso, unidade, perfil} = payload;
            const unidadesStore = useUnidadesStore();
            const alertasStore = useAlertasStore();
            const notificacoesStore = useNotificacoesStore();

            const subprocessoIndex = this.subprocessos.findIndex(
                pu => pu.idProcesso === idProcesso && pu.unidade === unidade
            );

            if (subprocessoIndex !== -1) {
                const subprocesso = this.subprocessos[subprocessoIndex];

                if (perfil === 'ADMIN') {
                    // ADMIN: homologar diretamente (consolidar efeitos)
                    this.addMovement({
                        idSubprocesso: subprocesso.id,
                        unidadeOrigem: 'SEDOC',
                        unidadeDestino: 'SEDOC',
                        descricao: 'Mapa de competências homologado'
                    });
                    this.subprocessos[subprocessoIndex] = {
                        ...subprocesso,
                        situacao: SITUACOES_SUBPROCESSO.MAPA_HOMOLOGADO,
                        dataFimEtapa2: subprocesso.dataFimEtapa2 || new Date(),
                        analises: [], // Limpar histórico de análise
                        movimentacoes: subprocesso.movimentacoes || [],
                    };
                } else {
                    // GESTOR: enviar para superior
                    const unidadeSuperior = unidadesStore.getUnidadeImediataSuperior(unidade); // <-- Movido para cá
                    if (!unidadeSuperior) {
                        throw new Error('Unidade superior não encontrada');
                    }
                    this.addMovement({
                        idSubprocesso: subprocesso.id,
                        unidadeOrigem: unidade,
                        unidadeDestino: unidadeSuperior,
                        descricao: 'Mapa de competências validado'
                    });

                    // Atualizar subprocesso (consolidar efeitos)
                    this.subprocessos[subprocessoIndex] = {
                        ...subprocesso,
                        unidadeAtual: unidadeSuperior,
                        unidadeAnterior: unidade,
                        situacao: SITUACOES_SUBPROCESSO.MAPA_VALIDADO,
                        dataFimEtapa2: new Date(),
                        analises: [],
                        movimentacoes: subprocesso.movimentacoes || [],
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
            unidade: string
        }) {
            const {idProcesso, unidade} = payload;

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
                this.addMovement({
                    idSubprocesso: subprocesso.id,
                    unidadeOrigem: unidade,
                    unidadeDestino: unidadeInferior,
                    descricao: 'Devolução da validação do mapa de competências para ajustes'
                });

                // Determinar nova situação
                let novaSituacao: Subprocesso['situacao'];
                if (unidadeInferior === subprocesso.unidade) {
                    // Retornando para a própria unidade
                    novaSituacao = SITUACOES_SUBPROCESSO.MAPA_DISPONIBILIZADO;
                    // Resetar dataFimEtapa2 conforme requisitos
                    this.subprocessos[subprocessoIndex] = {
                        ...subprocesso,
                        unidadeAtual: unidadeInferior,
                        unidadeAnterior: unidade,
                        situacao: novaSituacao,
                        dataFimEtapa2: null,
                        movimentacoes: subprocesso.movimentacoes || [],
                    };
                } else {
                    // Retornando para unidade diferente (SEDOC fazendo ajustes)
                    novaSituacao = SITUACOES_SUBPROCESSO.MAPA_CRIADO;
                    // Atualizar subprocesso
                    this.subprocessos[subprocessoIndex] = {
                        ...subprocesso,
                        unidadeAtual: unidadeInferior,
                        unidadeAnterior: unidade,
                        situacao: novaSituacao,
                        movimentacoes: subprocesso.movimentacoes || [],
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
                    descricao: `Validação do mapa de competências da unidade ${subprocesso.unidade} devolvida para ajustes`
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
            const {idProcesso, unidade, sugestoes} = payload;
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
                    sugestoes: sugestoes,
                    dataFimEtapa2: new Date(), // Definir data/hora de conclusão da etapa 2
                    analises: [], // Excluir histórico de análise
                    movimentacoes: subprocesso.movimentacoes || [],
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
            const {idProcesso, unidade} = payload;
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
                    situacao: SITUACOES_SUBPROCESSO.MAPA_VALIDADO,
                        dataFimEtapa2: new Date(),
                        analises: [],
                        movimentacoes: subprocesso.movimentacoes || [],
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