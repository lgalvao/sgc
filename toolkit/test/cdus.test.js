import os from "node:os";
import path from "node:path";
import {mkdtemp} from "node:fs/promises";
import fs from "fs-extra";
import {describe, expect, test} from "vitest";
import {execaNode} from "execa";

const DIRETORIO_RAIZ = path.resolve(import.meta.dirname, "..", "..");
const CAMINHO_SGC = path.join(DIRETORIO_RAIZ, "toolkit", "sgc.js");

async function executarSgc(args, opcoes = {}) {
    return execaNode(CAMINHO_SGC, args, {
        cwd: DIRETORIO_RAIZ,
        reject: false,
        ...opcoes
    });
}

async function criarIntroSituacoes(dirSpecs) {
    await fs.outputFile(
        path.join(dirSpecs, "_intro-situacoes.md"),
        [
            "## Situações",
            "",
            "- **Criado**: Processo cadastrado.",
            "- **Em andamento**: Processo em execução.",
            "- **Finalizado**: Processo encerrado.",
            "- **Diagnóstico**: Tipo de processo de diagnóstico.",
            "- **Autoavaliação concluída**: Avaliação individual concluída."
        ].join("\n")
    );
}

describe("Ferramentas de requisitos dos CDUs", () => {
    test("inventaria formatos implícitos dos CDUs", async () => {
        const base = await mkdtemp(path.join(os.tmpdir(), "sgc-cdus-inventario-"));
        const dirSpecs = path.join(base, "specs");

        await fs.outputFile(
            path.join(dirSpecs, "cdu-01.md"),
            [
                "# CDU-01 - Exemplo",
                "",
                "## Atores",
                "",
                "- ADMIN",
                "",
                "## Pré-condições",
                "",
                "- Usuário autenticado",
                "",
                "## Fluxo principal",
                "",
                "1. O usuário acessa o `Painel`.",
                "2. O sistema muda a situação para 'Em andamento'."
            ].join("\n")
        );
        await criarIntroSituacoes(dirSpecs);

        const resultado = await executarSgc([
            "requisitos",
            "cdus",
            "inventariar",
            "--json",
            "--base",
            base
        ]);

        expect(resultado.exitCode).toBe(0);
        const conteudo = JSON.parse(resultado.stdout);
        expect(conteudo.totalArquivos).toBe(1);
        expect(conteudo.formatosAtor["## Atores"]).toBe(1);
        expect(conteudo.formatosPreCondicoes["## Pré-condições"]).toBe(1);
        expect(conteudo.formatosFluxoPrincipal["## Fluxo principal"]).toBe(1);
        expect(conteudo.situacoesMaisFrequentes["'Em andamento'"]).toBe(1);
        expect(conteudo.elementosUiMaisFrequentes["`Painel`"]).toBe(1);
    });

    test("audita a estrutura canônica mínima dos CDUs", async () => {
        const base = await mkdtemp(path.join(os.tmpdir(), "sgc-cdus-auditoria-"));
        const dirSpecs = path.join(base, "specs");

        await fs.outputFile(
            path.join(dirSpecs, "cdu-01.md"),
            [
                "# CDU-01 - Exemplo válido",
                "",
                "## Atores",
                "",
                "- CHEFE",
                "",
                "## Pré-condições",
                "",
                "- Existe processo em andamento",
                "",
                "## Fluxo principal",
                "",
                "1. O usuário acessa o `Painel`.",
                "2. O sistema mostra a tela `Detalhes`."
            ].join("\n")
        );
        await criarIntroSituacoes(dirSpecs);

        await fs.outputFile(
            path.join(dirSpecs, "cdu-02.md"),
            [
                "# CDU-99 - Exemplo inválido",
                "",
                "Ator: ADMIN",
                "",
                "## Pré-condição",
                "",
                "## Fluxo principal",
                "",
                "1. Primeiro passo.",
                "1. Passo repetido."
            ].join("\n")
        );

        const resultado = await executarSgc([
            "requisitos",
            "cdus",
            "auditar",
            "--json",
            "--base",
            base
        ]);

        expect(resultado.exitCode).toBe(0);
        const conteudo = JSON.parse(resultado.stdout);
        expect(conteudo.resumo.totalArquivos).toBe(2);
        expect(conteudo.resumo.arquivosComErro).toBe(1);

        const invalido = conteudo.relatorio.find(item => item.arquivo === "specs/cdu-02.md");
        expect(invalido.achados.some(achado => achado.regra === "titulo_numero")).toBe(true);
        expect(invalido.achados.some(achado => achado.regra === "atores_canonicos")).toBe(true);
        expect(invalido.achados.some(achado => achado.regra === "pre_condicoes")).toBe(true);
        expect(invalido.achados.some(achado => achado.regra === "numeracao_repetida")).toBe(true);
    });

    test("audita convenções tipográficas dos CDUs", async () => {
        const base = await mkdtemp(path.join(os.tmpdir(), "sgc-cdus-estilo-"));
        const dirSpecs = path.join(base, "specs");

        await fs.outputFile(
            path.join(dirSpecs, "cdu-01.md"),
            [
                "# CDU-01 - Exemplo",
                "",
                "## Atores",
                "",
                "- ADMIN",
                "",
                "## Pré-condições",
                "",
                "- Usuário autenticado com perfil 'ADMIN'",
                "",
                "## Fluxo principal",
                "",
                "1. O sistema mostra um modal com título \"Adicionar administrador\" e usa [SIGLA_UNIDADE]."
            ].join("\n")
        );
        await criarIntroSituacoes(dirSpecs);

        const resultado = await executarSgc([
            "requisitos",
            "cdus",
            "auditar-estilo",
            "--json",
            "--base",
            base
        ]);

        expect(resultado.exitCode).toBe(0);
        const conteudo = JSON.parse(resultado.stdout);
        expect(conteudo.resumo.totalArquivos).toBe(1);
        expect(conteudo.resumo.arquivosComAviso).toBe(1);

        const arquivo = conteudo.relatorio.find(item => item.arquivo === "specs/cdu-01.md");
        expect(arquivo.achados.some(achado => achado.regra === "perfil_em_aspas_simples")).toBe(true);
        expect(arquivo.achados.some(achado => achado.regra === "ui_em_aspas_duplas")).toBe(true);
        expect(arquivo.achados.some(achado => achado.regra === "placeholder_legado")).toBe(true);
    });

    test("inventaria vocabulário controlado dos CDUs", async () => {
        const base = await mkdtemp(path.join(os.tmpdir(), "sgc-cdus-vocabulario-"));
        const dirSpecs = path.join(base, "specs");

        await fs.outputFile(
            path.join(dirSpecs, "cdu-01.md"),
            [
                "# CDU-01 - Exemplo",
                "",
                "## Atores",
                "",
                "- ADMIN",
                "- GESTOR",
                "",
                "## Pré-condições",
                "",
                "- Processo do tipo 'Diagnóstico' na situação 'Em andamento'.",
                "",
                "## Fluxo principal",
                "",
                "1. O usuário abre o `Painel`."
            ].join("\n")
        );
        await criarIntroSituacoes(dirSpecs);

        const resultado = await executarSgc([
            "requisitos",
            "cdus",
            "inventariar-vocabulario",
            "--json",
            "--base",
            base
        ]);

        expect(resultado.exitCode).toBe(0);
        const conteudo = JSON.parse(resultado.stdout);
        expect(conteudo.perfis.ADMIN).toBe(1);
        expect(conteudo.perfis.GESTOR).toBe(1);
        expect(conteudo.situacoes["Em andamento"]).toBe(1);
        expect(conteudo.tiposProcesso["Diagnóstico"]).toBe(1);
        expect(conteudo.elementosUi.Painel).toBe(1);
    });

    test("audita vocabulário controlado dos CDUs", async () => {
        const base = await mkdtemp(path.join(os.tmpdir(), "sgc-cdus-vocabulario-auditar-"));
        const dirSpecs = path.join(base, "specs");

        await fs.outputFile(
            path.join(dirSpecs, "cdu-01.md"),
            [
                "# CDU-01 - Exemplo",
                "",
                "## Atores",
                "",
                "- Admim",
                "",
                "## Pré-condições",
                "",
                "- Processo do tipo 'Diagnostico' na situação 'Auto avaliação concluída'.",
                "",
                "## Fluxo principal",
                "",
                "1. O usuário abre o `Painel`."
            ].join("\n")
        );
        await criarIntroSituacoes(dirSpecs);

        const resultado = await executarSgc([
            "requisitos",
            "cdus",
            "auditar-vocabulario",
            "--json",
            "--base",
            base
        ]);

        expect(resultado.exitCode).toBe(0);
        const conteudo = JSON.parse(resultado.stdout);
        expect(conteudo.resumo.arquivosComAviso).toBe(1);
        const arquivo = conteudo.relatorio.find(item => item.arquivo === "specs/cdu-01.md");
        expect(arquivo.achados.some(achado => achado.regra === "perfil_fora_vocabulario")).toBe(true);
        expect(arquivo.achados.some(achado => achado.regra === "tipo_processo_variacao")).toBe(true);
        expect(arquivo.achados.some(achado => achado.regra === "situacao_variacao")).toBe(true);
    });

    test("inventaria mensagens recorrentes dos CDUs", async () => {
        const base = await mkdtemp(path.join(os.tmpdir(), "sgc-cdus-mensagens-"));
        const dirSpecs = path.join(base, "specs");
        await criarIntroSituacoes(dirSpecs);

        await fs.outputFile(
            path.join(dirSpecs, "cdu-01.md"),
            [
                "# CDU-01 - Exemplo",
                "",
                "## Atores",
                "",
                "- ADMIN",
                "",
                "## Pré-condições",
                "",
                "- Usuário autenticado",
                "",
                "## Fluxo principal",
                "",
                "1. O sistema registra:",
                "   - `Descrição`: \"Cadastro aceito\"",
                "   - `Processo`: :DESCRICAO_PROCESSO:",
                "",
                "   ```text",
                "   Assunto: SGC: Cadastro aceito",
                "   ```",
                "",
                "2. O sistema mostra a mensagem \"Aceite registrado\".",
                "3. O sistema mostra *toast* \"Aceite registrado\"."
            ].join("\n")
        );

        const resultado = await executarSgc([
            "requisitos",
            "cdus",
            "inventariar-mensagens",
            "--json",
            "--base",
            base
        ]);

        expect(resultado.exitCode).toBe(0);
        const conteudo = JSON.parse(resultado.stdout);
        expect(conteudo.descricoes["Cadastro aceito"]).toBe(1);
        expect(conteudo.assuntos["SGC: Cadastro aceito"]).toBe(1);
        expect(conteudo.mensagens["Aceite registrado"]).toBe(1);
        expect(conteudo.toasts["Aceite registrado"]).toBe(1);
    });

    test("audita problemas mecânicos de mensagens dos CDUs", async () => {
        const base = await mkdtemp(path.join(os.tmpdir(), "sgc-cdus-mensagens-auditar-"));
        const dirSpecs = path.join(base, "specs");
        await criarIntroSituacoes(dirSpecs);

        await fs.outputFile(
            path.join(dirSpecs, "cdu-01.md"),
            [
                "# CDU-01 - Exemplo",
                "",
                "## Atores",
                "",
                "- ADMIN",
                "",
                "## Pré-condições",
                "",
                "- Usuário autenticado",
                "",
                "## Fluxo principal",
                "",
                "1. O sistema registra:",
                "   - `Descrição`: \"Mapa  homologado\"",
                "",
                "   ```text",
                "   Assunto: SGC: Reabertura - :SIGLA_UNIDADE:]",
                "   ```"
            ].join("\n")
        );

        const resultado = await executarSgc([
            "requisitos",
            "cdus",
            "auditar-mensagens",
            "--json",
            "--base",
            base
        ]);

        expect(resultado.exitCode).toBe(0);
        const conteudo = JSON.parse(resultado.stdout);
        expect(conteudo.resumo.arquivosComAviso).toBe(1);
        const arquivo = conteudo.relatorio.find(item => item.arquivo === "specs/cdu-01.md");
        expect(arquivo.achados.some(achado => achado.regra === "descricao_espacamento")).toBe(true);
        expect(arquivo.achados.some(achado => achado.regra === "assunto_fechamento_suspeito")).toBe(true);
    });

    test("compara mensagens dos CDUs com mensagens canônicas do código", async () => {
        const base = await mkdtemp(path.join(os.tmpdir(), "sgc-cdus-mensagens-codigo-"));
        const dirSpecs = path.join(base, "specs");
        await criarIntroSituacoes(dirSpecs);

        await fs.outputFile(
            path.join(dirSpecs, "cdu-01.md"),
            [
                "# CDU-01 - Exemplo",
                "",
                "## Atores",
                "",
                "- ADMIN",
                "",
                "## Pré-condições",
                "",
                "- Usuário autenticado",
                "",
                "## Fluxo principal",
                "",
                "1. O sistema registra:",
                "   - `Descrição`: \"Início do processo\"",
                "",
                "   ```text",
                "   Assunto: SGC: Cadastro de atividades e conhecimentos disponibilizado - :SIGLA_UNIDADE_SUBPROCESSO:",
                "   ```",
                "2. O sistema mostra a mensagem \"Homologação efetivada\".",
                "3. O sistema mostra *toast* \"Diagnóstico homologado\"."
            ].join("\n")
        );

        await fs.outputFile(
            path.join(base, "backend", "src", "main", "java", "sgc", "comum", "Mensagens.java"),
            [
                "package sgc.comum;",
                "public final class Mensagens {",
                "    public static final String HIST_PROCESSO_INICIADO = \"Processo iniciado\";",
                "    public static final String HIST_DIAGNOSTICO_HOMOLOGADO = \"Homologação de diagnóstico\";",
                "    public static final String ALERTA_DIAGNOSTICO_HOMOLOGADO = \"Diagnóstico da unidade %s homologado\";",
                "}"
            ].join("\n")
        );

        await fs.outputFile(
            path.join(base, "backend", "src", "main", "java", "sgc", "alerta", "AssuntosNotificacao.java"),
            [
                "package sgc.alerta;",
                "public final class AssuntosNotificacao {",
                "    public static String subprocesso() {",
                "        return \"SGC: Cadastro de atividades e conhecimentos disponibilizado - %s\".formatted(\"ABC\");",
                "    }",
                "}"
            ].join("\n")
        );

        await fs.outputFile(
            path.join(base, "frontend", "src", "constants", "notificacoes.ts"),
            "export const TIPOS_NOTIFICACAO_LABELS = { DIAGNOSTICO_HOMOLOGADO: \"Diagnóstico homologado\" };\n"
        );
        await fs.outputFile(
            path.join(base, "frontend", "src", "constants", "textos-subprocesso.ts"),
            "export const TEXTOS_SUCESSO_SUBPROCESSO = { HOMOLOGACAO_EFETIVADA: \"Homologação efetivada\" };\n"
        );
        await fs.outputFile(
            path.join(base, "frontend", "src", "constants", "textos-mapa.ts"),
            "export const TEXTOS_SUCESSO_MAPA = { SUCESSO_HOMOLOGACAO: \"Mapa homologado\" };\n"
        );
        await fs.outputFile(
            path.join(base, "frontend", "src", "constants", "textos-diagnostico.ts"),
            "export const TEXTOS_DIAGNOSTICO = { SUCESSO_DIAGNOSTICO_HOMOLOGADO: 'Diagnóstico homologado' };\n"
        );
        await fs.outputFile(
            path.join(base, "frontend", "src", "constants", "textos-processo.ts"),
            "export const TEXTOS_SUCESSO_PROCESSO = { PROCESSO_INICIADO: \"Processo iniciado\" };\n"
        );

        const resultado = await executarSgc([
            "requisitos",
            "cdus",
            "auditar-mensagens-codigo",
            "--json",
            "--base",
            base
        ]);

        expect(resultado.exitCode).toBe(0);
        const conteudo = JSON.parse(resultado.stdout);
        expect(conteudo.resumo.itensComReferenciaExata).toBeGreaterThan(0);
        const descricao = conteudo.relatorio.find(item => item.tipo === "descricoes" && item.valor === "Início do processo");
        expect(descricao.referenciasExatas).toHaveLength(0);
        expect(descricao.sugestoes.some(item => item.texto === "Processo iniciado")).toBe(true);
        const mensagem = conteudo.relatorio.find(item => item.tipo === "mensagens" && item.valor === "Homologação efetivada");
        expect(mensagem.referenciasExatas.some(item => item.texto === "Homologação efetivada")).toBe(true);
        const assunto = conteudo.relatorio.find(item => item.tipo === "assuntos" && item.valor === "SGC: Cadastro de atividades e conhecimentos disponibilizado - :SIGLA_UNIDADE_SUBPROCESSO:");
        expect(assunto.referenciasExatas.some(item => item.texto === "SGC: Cadastro de atividades e conhecimentos disponibilizado - :VALOR:")).toBe(true);
        const toast = conteudo.relatorio.find(item => item.tipo === "toasts" && item.valor === "Diagnóstico homologado");
        expect(toast.referenciasExatas.some(item => item.texto === "Diagnóstico homologado")).toBe(true);
    });

    test("inventaria densidade documental dos CDUs", async () => {
        const base = await mkdtemp(path.join(os.tmpdir(), "sgc-cdus-densidade-"));
        const dirSpecs = path.join(base, "specs");
        await criarIntroSituacoes(dirSpecs);

        await fs.outputFile(
            path.join(dirSpecs, "cdu-01.md"),
            [
                "# CDU-01 - Exemplo",
                "",
                "## Atores",
                "",
                "- ADMIN",
                "",
                "## Pré-condições",
                "",
                "- Usuário autenticado",
                "",
                "## Fluxo principal",
                "",
                "1. O usuário abre o `Painel`.",
                "2. O sistema mostra a tela `Detalhes`.",
                "   - item",
                "      - subitem"
            ].join("\n")
        );

        const resultado = await executarSgc([
            "requisitos",
            "cdus",
            "inventariar-densidade",
            "--json",
            "--base",
            base
        ]);

        expect(resultado.exitCode).toBe(0);
        const conteudo = JSON.parse(resultado.stdout);
        expect(conteudo.totalArquivos).toBe(1);
        expect(conteudo.resumo.mediaPalavras).toBeGreaterThan(0);
        expect(conteudo.documentos[0].passos).toBe(2);
    });

    test("inventaria duplicações textuais dos CDUs", async () => {
        const base = await mkdtemp(path.join(os.tmpdir(), "sgc-cdus-duplicacoes-"));
        const dirSpecs = path.join(base, "specs");
        await criarIntroSituacoes(dirSpecs);

        const conteudo = [
            "# CDU-01 - Exemplo",
            "",
            "## Atores",
            "",
            "- ADMIN",
            "",
            "## Pré-condições",
            "",
            "- Usuário autenticado",
            "",
            "## Fluxo principal",
            "",
            "1. O sistema envia notificação.",
            "",
            "```text",
            "Assunto: SGC: Exemplo",
            "",
            "Prezada unidade, acompanhe o processo pelo sistema.",
            "```"
        ].join("\n");

        await fs.outputFile(path.join(dirSpecs, "cdu-01.md"), conteudo);
        await fs.outputFile(path.join(dirSpecs, "cdu-02.md"), conteudo.replace("CDU-01", "CDU-02"));

        const resultado = await executarSgc([
            "requisitos",
            "cdus",
            "inventariar-duplicacoes",
            "--json",
            "--base",
            base
        ]);

        expect(resultado.exitCode).toBe(0);
        const corpo = JSON.parse(resultado.stdout);
        expect(corpo.duplicacoes.length).toBeGreaterThan(0);
    });
});
