import {defineStore} from 'pinia'
import unidadesMock from '../mocks/unidades.json' with {type: 'json'};
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
            const stack: Unidade[] = [];

            const unidadeRaiz = this.pesquisarUnidade(siglaUnidade);
            if (unidadeRaiz) {
                stack.push(unidadeRaiz);
                while (stack.length > 0) {
                    const currentUnidade = stack.pop()!;
                    unidadesEncontradas.push(currentUnidade.sigla);
                    if (currentUnidade.filhas) {
                        for (let i = currentUnidade.filhas.length - 1; i >= 0; i--) {
                            stack.push(currentUnidade.filhas[i]);
                        }
                    }
                }
            }
            return unidadesEncontradas;
        },
        getUnidadeSuperior(siglaUnidade: string): string | null {
            const stack: { unit: Unidade, parentSigla: string | null }[] = [];

            for (const unidade of this.unidades) {
                stack.push({unit: unidade, parentSigla: null});
            }

            while (stack.length > 0) {
                const {unit: currentUnidade, parentSigla: currentParentSigla} = stack.pop()!;

                if (currentUnidade.sigla === siglaUnidade) {
                    return currentParentSigla;
                }

                if (currentUnidade.filhas) {
                    for (let i = currentUnidade.filhas.length - 1; i >= 0; i--) {
                        stack.push({unit: currentUnidade.filhas[i], parentSigla: currentUnidade.sigla});
                    }
                }
            }
            return null;
        },
        getUnidadeImediataSuperior(siglaUnidade: string): string | null {
            return this.getUnidadeSuperior(siglaUnidade);
        }
    }
})