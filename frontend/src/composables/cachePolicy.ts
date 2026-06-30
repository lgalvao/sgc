/**
 * Leituras cujo estado é mantido consistente por invalidação explícita
 * após mutações conhecidas do próprio fluxo. Não precisam de expiração temporal.
 */
export const STALE_TIME_CONTROLADO_POR_INVALIDACAO = Infinity;

/**
 * Leituras auxiliares ou de apoio visual, sem dono único de invalidação
 * para mudanças externas à tela atual. Revalidam em janela curta.
 */
export const STALE_TIME_LEITURA_AUXILIAR = 60_000;

/**
 * Debounce padrão para autosave em formulários interativos do frontend.
 */
export const DEBOUNCE_AUTOSAVE_PADRAO_MS = 800;
