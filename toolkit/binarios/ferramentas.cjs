#!/usr/bin/env node
const path = require("node:path");
const {spawnSync} = require("node:child_process");

const caminhoCliTsx = require.resolve("tsx/cli");
const caminhoEntrada = path.resolve(__dirname, "..", "ferramentas.ts");
const processo = spawnSync(process.execPath, [caminhoCliTsx, caminhoEntrada, ...process.argv.slice(2)], {
    stdio: "inherit",
});

if (processo.error) {
    throw processo.error;
}

process.exitCode = processo.status ?? 1;
