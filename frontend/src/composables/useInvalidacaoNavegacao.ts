import {usePainelStore} from "@/stores/painel";
import {useProcessoStore} from "@/stores/processo";
import {useSubprocessoStore} from "@/stores/subprocesso";
import {useMapasStore} from "@/stores/mapas";
import {useUnidadeStore} from "@/stores/unidade";

interface OpcoesInvalidacaoSubprocesso {
    incluirPainel?: boolean;
    incluirProcesso?: boolean;
    incluirMapas?: boolean;
    codigoSubprocessoMapa?: number;
}

/**
 * Ponto central de invalidação dos caches de navegação da SPA.
 *
 * ## Por que isso existe
 *
 * O frontend usa stores Pinia como cache de sessão e várias rotas em keepAlive.
 * Quando o usuário navega entre telas sem recarregar a página, os componentes
 * em keepAlive reaproveitam os snapshots das stores via `onActivated`. Se uma
 * ação de escrita (workflow, mutação de dados) não invalidar as stores afetadas,
 * o usuário verá dados desatualizados até um refresh completo da página.
 *
 * ## Regra fundamental
 *
 * **Leitura não invalida. Mutação invalida.**
 * Funções de carga inicial (ex: `sincronizarEstadoInicialContexto`) não devem
 * chamar `store.invalidar()` para caches de outras telas. Apenas ações que
 * alteram dados no backend devem disparar invalidação.
 *
 * ## Quando usar cada função
 *
 * - `invalidarCachesProcesso()`: ações que afetam o processo inteiro
 *   (criar, iniciar, finalizar, remover processo). Invalida tudo.
 *
 * - `invalidarCachesSubprocesso(opcoes)`: ações que afetam um subprocesso
 *   (workflow de cadastro, alteração de data, mapa). Invalida seletivamente:
 *   - `incluirPainel: true` → obrigatório quando a mutação altera campos de
 *     `ProcessoResumo` exibidos no painel (situação, data limite, dados gerais).
 *     Não é necessário para mutações de atividades/competências que não alteram
 *     a situação do processo.
 *   - `incluirProcesso: true` → quando o `ProcessoDetalheView` exibe dados
 *     que foram alterados (ex: ações de bloco).
 *   - `incluirMapas: true` → quando o mapa do subprocesso foi alterado.
 *
 * ## Quem pode chamar este composable
 *
 * Apenas composables de **ação** (fluxo, workflow, formulários de mutação)
 * e views que orquestram essas ações. Composables de orquestração de leitura
 * (ex: `useCadastroOrquestracao`, `useMapaOrquestracao`) só devem chamar
 * `store.invalidar()` para stores do próprio domínio (mapas, contexto de edição),
 * nunca para stores de outras telas (painel, processo).
 */
export function useInvalidacaoNavegacao() {
    const painelStore = usePainelStore();
    const processoStore = useProcessoStore();
    const subprocessoStore = useSubprocessoStore();
    const mapasStore = useMapasStore();
    const unidadeStore = useUnidadeStore();

    function limparEstadoSubprocessoAtual(): void {
        subprocessoStore.limparContextoAtual();
    }

    function invalidarCachesProcesso(): void {
        painelStore.invalidar();
        processoStore.invalidar();
        subprocessoStore.invalidar();
        mapasStore.invalidar();
        unidadeStore.invalidar();
    }

    function invalidarMapas(opcoes?: OpcoesInvalidacaoSubprocesso) {
        if (!(opcoes?.incluirMapas ?? false)) {
            return;
        }

        const codigoSubprocessoMapa = opcoes?.codigoSubprocessoMapa;
        if (typeof codigoSubprocessoMapa === "number") {
            mapasStore.invalidar(codigoSubprocessoMapa);
            return;
        }

        mapasStore.invalidar();
    }

    function invalidarCachesSubprocesso(opcoes?: OpcoesInvalidacaoSubprocesso): void {
        if (opcoes?.incluirPainel) {
            painelStore.invalidar();
        }
        if (opcoes?.incluirProcesso) {
            processoStore.invalidar();
        }
        subprocessoStore.invalidar();
        // Mapas são pesados e nem toda ação de subprocesso altera esse domínio.
        // Mantemos opt-in explícito para evitar recarregamentos desnecessários.
        invalidarMapas(opcoes);
    }

    return {
        invalidarCachesProcesso,
        invalidarCachesSubprocesso,
        limparEstadoSubprocessoAtual,
    };
}
