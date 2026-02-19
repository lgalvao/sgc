import {createConsola} from "consola";

// Determina o nível de log baseado no ambiente
// Desenvolvimento: INFO (4) - mostra info, warn, error
// Produção: WARN (3) - mostra apenas warn e error
// Teste: ERROR (1) - mostra apenas error
export const getLogLevel = (envMode?: string) => {
    const mode = (envMode !== undefined) ? envMode : (import.meta.env?.MODE || (typeof process !== "undefined" && process.env?.VITEST ? "test" : "development"));
    
    if (mode === "test") {
        return 1; // ERROR only
    } else if (mode === "production") {
        return 3; // WARN and ERROR
    } else {
        return 4; // INFO, WARN, ERROR (development)
    }
};

const logger = createConsola({
    level: getLogLevel(),
    formatOptions: {
        date: true,
        columns: 80,
    },
});

export default logger;
