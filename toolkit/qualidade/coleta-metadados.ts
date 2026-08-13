import {execa} from "execa";

async function coletarMetadadosGit(base: string): Promise<Record<string, string>> {
    const branch = (await execa("git", ["rev-parse", "--abbrev-ref", "HEAD"], {cwd: base})).stdout.trim();
    const commit = (await execa("git", ["rev-parse", "HEAD"], {cwd: base})).stdout.trim();
    return {branch, commit};
}

export {coletarMetadadosGit};
