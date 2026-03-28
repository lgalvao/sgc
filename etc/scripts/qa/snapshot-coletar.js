#!/usr/bin/env node
import {executarNode} from "../lib/execucao.js";
import logger from "../lib/logger.js";

executarNode("etc/qa-dashboard/scripts/coletar-snapshot.mjs", process.argv.slice(2)).catch((error) => {
    logger.error(`Erro ao coletar snapshot de QA: ${error.message}`);
    process.exit(1);
});
