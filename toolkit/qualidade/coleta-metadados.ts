import {execa} from "execa";
import type {MetadadosControleVersao} from "./coleta-fotografia.js";

async function coletarMetadadosGit(base: string): Promise<MetadadosControleVersao> {
    const ramo = (await execa("git", ["rev-parse", "--abbrev-ref", "HEAD"], {cwd: base})).stdout.trim();
    const revisao = (await execa("git", ["rev-parse", "HEAD"], {cwd: base})).stdout.trim();
    return {ramo, revisao};
}

export {coletarMetadadosGit};
