import {defineStore} from 'pinia'
import unidadesMock from '../mocks/unidades.json' assert {type: 'json'};
import type {Unidade} from '@/types/tipos'

export const useUnidadesStore = defineStore('unidades', {
    state: () => ({
        unidades: unidadesMock as unknown as Unidade[]
    }),
    actions: {
        pesquisarUnidade(this: ReturnType<typeof useUnidadesStore>, sigla: string, units: Unidade[] = this.unidades): Unidade | null {
            for (const unit of units) {
                if (unit.sigla === sigla) return unit
                if (unit.filhas) {
                    const found = this.pesquisarUnidade(sigla, unit.filhas)
                    if (found) return found
                }
            }
            return null
        },
        getUnidadesSubordinadas(siglaUnidade: string): string[] {
            const unidadesEncontradas: string[] = [];

            const findAndCollect = (unidade: Unidade) => {
                unidadesEncontradas.push(unidade.sigla);
                if (unidade.filhas) {
                    unidade.filhas.forEach(findAndCollect);
                }
            };

            const unidadeRaiz = this.pesquisarUnidade(siglaUnidade);
            if (unidadeRaiz) {
                findAndCollect(unidadeRaiz);
            }

            return unidadesEncontradas;
        },
        getUnidadeSuperior(siglaUnidade: string): string | null {
            const findSuperior = (unidades: Unidade[], targetSigla: string, parentSigla: string | null = null): string | null => {
                for (const unidade of unidades) {
                    if (unidade.sigla === targetSigla) {
                        return parentSigla;
                    }
                    if (unidade.filhas) {
                        const superior = findSuperior(unidade.filhas, targetSigla, unidade.sigla);
                        if (superior !== null) {
                            return superior;
                        }
                    }
                }
                return null;
            };

            return findSuperior(this.unidades, siglaUnidade);
        },
        getUnidadeImediataSuperior(siglaUnidade: string): string | null {
            return this.getUnidadeSuperior(siglaUnidade);
        }
    }
})