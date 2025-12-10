import {createConsola} from "consola";

const logger = createConsola({
    level: 4,
    formatOptions: {
        date: true,
        columns: 80,
    },
});

export default logger;
