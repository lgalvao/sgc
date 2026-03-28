import {createConsola} from "consola";

function obterNivel() {
    if (process.env.VITEST) {
        return 1;
    }

    if (process.env.NODE_ENV === "production") {
        return 3;
    }

    return 4;
}

const logger = createConsola({
    level: obterNivel(),
    formatOptions: {
        date: true,
        columns: 100
    }
});

export default logger;
