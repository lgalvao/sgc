import {createConsola} from "consola";

type ImportMetaComEnv = ImportMeta & {
    env?: {
        MODE?: string;
    };
};

function obterModoAtual(envMode?: string): string {
    if (envMode !== undefined) {
        return envMode;
    }

    const importMetaComEnv = import.meta as ImportMetaComEnv;

    if (importMetaComEnv.env?.MODE) {
        return importMetaComEnv.env.MODE;
    }

    if (typeof process !== "undefined" && process.env?.VITEST) {
        return "test";
    }

    return "development";
}

export const getLogLevel = (envMode?: string) => {
    const mode = obterModoAtual(envMode);

    if (mode === "test") {
        return 1;
    }

    if (mode === "production") {
        return 3;
    }

    return 4;
};

const logger = createConsola({
    level: getLogLevel(),
    formatOptions: {
        date: true,
        columns: 80,
    },
});

export default logger;
