import {ref} from 'vue';
import type {CriarAtividadeRequest} from '@/types/tipos';
import * as atividadeService from '@/services/atividadeService';

export function useAtividadeForm() {
    const novaAtividade = ref('');
    const loadingAdicionar = ref(false);

    async function adicionarAtividade(
        codSubprocesso: number,
        codMapa: number
    ): Promise<any> {
        if (!novaAtividade.value?.trim()) return null;

        loadingAdicionar.value = true;
        try {
            const request: CriarAtividadeRequest = {
                descricao: novaAtividade.value.trim(),
            };
            const response = await atividadeService.criarAtividade(request, codMapa);
            novaAtividade.value = '';
            return response;
        } finally {
            loadingAdicionar.value = false;
        }
    }

    return {
        novaAtividade,
        loadingAdicionar,
        adicionarAtividade
    };
}
