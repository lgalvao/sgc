import {defineStore} from 'pinia';
import atividadesMock from '../mocks/atividades.json';
import type {Atividade, Conhecimento} from '@/types/tipos';

export const useAtividadesStore = defineStore('atividades', {
    state: () => {
        const atividades = atividadesMock as Atividade[];
        let maxId = 0;
        atividades.forEach(atividade => {
            if (atividade.id > maxId) maxId = atividade.id;
            atividade.conhecimentos.forEach(conhecimento => {
                if (conhecimento.id > maxId) maxId = conhecimento.id;
            });
        });
        return {
            atividades: atividades,
            nextId: maxId + 1,
            atividadesSnapshot: [] as Atividade[], // Adicionado para armazenar o snapshot
        };
    },
    getters: {
        getAtividadesPorSubprocesso: (state) => (idSubprocesso: number): Atividade[] => {
            return state.atividades.filter(a => a.idSubprocesso === idSubprocesso);
        }
    },
    actions: {
        setAtividades(idSubprocesso: number, novasAtividades: Atividade[]) {
            // Remove as atividades antigas para este idSubprocesso
            this.atividades = this.atividades.filter(a => a.idSubprocesso !== idSubprocesso);

            // Adiciona as novas atividades
            this.atividades.push(...novasAtividades);
        },
        adicionarAtividade(atividade: Atividade) {
            atividade.id = this.nextId++;
            // Substituir o array para garantir reatividade
            this.atividades = [...this.atividades, atividade];
            console.log('AtividadesStore: Atividade adicionada. Estado atual:', this.atividades); // Adicionado para depuração
        },
        removerAtividade(atividadeId: number) {
            this.atividades = this.atividades.filter(a => a.id !== atividadeId);
        },
        adicionarConhecimento(atividadeId: number, conhecimento: Conhecimento) {
            const index = this.atividades.findIndex(a => a.id === atividadeId);
            if (index !== -1) {
                const atividade = this.atividades[index];
                conhecimento.id = this.nextId++;
                const updatedAtividade = {
                    ...atividade,
                    conhecimentos: [...atividade.conhecimentos, conhecimento]
                };
                this.atividades.splice(index, 1, updatedAtividade);
            }
        },
        removerConhecimento(atividadeId: number, conhecimentoId: number) {
            const atividade = this.atividades.find(a => a.id === atividadeId);
            if (atividade) {
                atividade.conhecimentos = atividade.conhecimentos.filter(c => c.id !== conhecimentoId);
            }
        },

        async fetchAtividadesPorSubprocesso(idSubprocesso: number) {
            const todasAtividades = atividadesMock as Atividade[];
            const atividadesDoProcesso = todasAtividades.filter(a => a.idSubprocesso === idSubprocesso);

            // Adiciona as atividades buscadas ao estado, evitando duplicatas
            atividadesDoProcesso.forEach(novaAtividade => {
                if (!this.atividades.some(a => a.id === novaAtividade.id)) {
                    this.atividades.push(novaAtividade);
                }
            });
        },

        adicionarMultiplasAtividades(atividades: Atividade[]) {
            const novasAtividadesComId = atividades.map(atividade => {
                const novaAtividade = {...atividade, id: this.nextId++};
                novaAtividade.conhecimentos = novaAtividade.conhecimentos.map(conhecimento => {
                    return {...conhecimento, id: this.nextId++};
                });
                return novaAtividade;
            });
            this.atividades.push(...novasAtividadesComId);
        },
        setAtividadesSnapshot(snapshot: Atividade[]) {
            this.atividadesSnapshot = snapshot;
        }
    }
});