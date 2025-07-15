import {defineStore} from 'pinia'
import {useProcessosStore} from './processos'

export const usePainelStore = defineStore('painel', {
    getters: {
        totalProcessos: () => useProcessosStore().processos.length,
        emAndamento: () => useProcessosStore().processos.filter(p => p.situacao === 'Em andamento').length,
        finalizados: () => useProcessosStore().processos.filter(p => p.situacao === 'Finalizado').length,
        pendenciasPorUnidade: () => [
            {unidade: 'COSIS', status: 'Aguardando', badge: 'bg-warning text-dark'},
            {unidade: 'SESEL', status: 'Finalizado', badge: 'bg-success'},
            {unidade: 'SEDESENV', status: 'Devolvido', badge: 'bg-danger'}
        ],
        notificacoes: () => [
            {tipo: 'info', texto: 'Prazo para COSIS: 10/07', badge: 'bg-info text-dark'},
            {tipo: 'danger', texto: 'SEDESENV devolvido para ajustes', badge: 'bg-danger'}
        ],
        processosSubordinadas: () => [
            {nome: 'Processo 2024/1', status: 'Em andamento', badge: 'bg-warning text-dark'},
            {nome: 'Processo 2023/2', status: 'Finalizado', badge: 'bg-success'}
        ],
        situacaoCadastro: () => 'Em andamento',
        descricaoMapa: () => 'Visualize o mapa de competências da sua unidade (atual e anteriores).'
    }
}) 