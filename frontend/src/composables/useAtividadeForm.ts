import {ref} from 'vue';
import {useAtividadesStore} from '@/stores/atividades';
import type {CriarAtividadeRequest} from '@/types/tipos';

export function useAtividadeForm() {
    const novaAtividade = ref('');
    const loadingAdicionar = ref(false);
    const atividadesStore = useAtividadesStore();

    async function adicionarAtividade(
        codSubprocesso: number,
        codMapa: number
    ): Promise<boolean> {
        if (!novaAtividade.value?.trim()) return false;

        loadingAdicionar.value = true;
        try {
            const request: CriarAtividadeRequest = {
                descricao: novaAtividade.value.trim(),
            };
            await atividadesStore.adicionarAtividade(
                codSubprocesso,
                codMapa,
                request
            );
            novaAtividade.value = '';
            return true;
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
