import os from "node:os";
import path from "node:path";
import {mkdtemp} from "node:fs/promises";
import {describe, expect, test} from "vitest";
import {executarSgc} from "./apoio.js";

interface VerificacaoAmbienteJson {
    nome: string;
    status?: string;
    detalhe?: string;
}

describe("Verificação do ambiente do projeto", () => {
    test("identifica corretamente a ausencia de arquivos essenciais e falha com codigo 1", async () => {
        const diretorioVazio = await mkdtemp(path.join(os.tmpdir(), "sgc-ambiente-vazio-"));

        // Executamos a verificacao passando o diretorio temporario vazio como base
        const resultado = await executarSgc(["projeto", "ambiente", "verificar", "--json", "--base", diretorioVazio]);

        // Como arquivos essenciais como gradlew e package.json estão ausentes, deve retornar código de erro 1
        expect(resultado.exitCode).toBe(1);

        const dados = JSON.parse(resultado.stdout);
        expect(dados.statusGeral).toBe("falha");
        expect(dados.totais.falha).toBeGreaterThan(0);

        // Verifica se um dos arquivos obrigatórios ausentes foi reportado como falha
        const falhaGradlew = dados.verificacoes.find((verificacao: VerificacaoAmbienteJson) => verificacao.nome === "gradlew");
        expect(falhaGradlew).toBeDefined();
        expect(falhaGradlew.status).toBe("falha");
        expect(falhaGradlew.detalhe).toContain("gradlew ausente");
    });
}, 30000);
