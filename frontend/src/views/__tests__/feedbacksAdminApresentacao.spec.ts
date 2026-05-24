import {describe, expect, it} from "vitest";
import {
  formatarMetadados,
  formatarTipo,
  obterIconeTipo,
  obterVarianteTipo,
  resumirNota
} from "../feedbacksAdminApresentacao";

describe("feedbacksAdminApresentacao", () => {
  describe("formatarTipo", () => {
    it("deve retornar o rótulo correto para tipos conhecidos", () => {
      expect(formatarTipo("BUG")).toBe("Bug");
      expect(formatarTipo("sugestao")).toBe("Sugestão");
    });

    it("deve capitalizar tipos desconhecidos", () => {
      expect(formatarTipo("OUTRO")).toBe("Outro");
      expect(formatarTipo("desconhecido")).toBe("Desconhecido");
    });
  });

  describe("obterVarianteTipo", () => {
    it("deve retornar a variante correta para tipos conhecidos", () => {
      expect(obterVarianteTipo("BUG")).toBe("danger");
      expect(obterVarianteTipo("ELOGIO")).toBe("success");
    });

    it("deve retornar secondary para tipos desconhecidos", () => {
      expect(obterVarianteTipo("OUTRO")).toBe("secondary");
    });
  });

  describe("obterIconeTipo", () => {
    it("deve retornar o ícone correto para tipos conhecidos", () => {
      expect(obterIconeTipo("BUG")).toBe("bi-bug");
      expect(obterIconeTipo("QUESTAO")).toBe("bi-question-circle");
    });

    it("deve retornar o ícone padrão para tipos desconhecidos", () => {
      expect(obterIconeTipo("OUTRO")).toBe("bi-chat-left-text");
    });
  });

  describe("resumirNota", () => {
    it("deve retornar string vazia para nota vazia", () => {
      expect(resumirNota("")).toBe("");
    });

    it("deve remover tags HTML e resumir texto longo", () => {
      const nota = "<p>" + "a".repeat(150) + "</p>";
      const resumo = resumirNota(nota);
      expect(resumo.length).toBe(120);
      expect(resumo.endsWith("...")).toBe(true);
    });

    it("deve retornar texto limpo se for curto", () => {
      expect(resumirNota("<p>Teste curto</p>")).toBe("Teste curto");
    });
  });

  describe("formatarMetadados", () => {
    it("deve retornar objeto vazio para entrada nula ou vazia", () => {
      expect(formatarMetadados(null)).toEqual({});
      expect(formatarMetadados("")).toEqual({});
    });

    it("deve lidar com JSON inválido", () => {
      const json = "{invalido}";
      expect(formatarMetadados(json)).toEqual({erro: "JSON inválido", valor: json});
    });

    it("deve formatar metadados completos", () => {
      const metadata = {
        userAgent: "Mozilla/5.0 (X11; Linux x86_64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/120.0.0.0 Safari/537.36",
        perfilAtivo: "ADMIN",
        unidadeAtiva: "UNIDADE X",
        larguraTela: 1920,
        alturaTela: 1080,
        rotaCaminho: "/home",
        rotaQuery: JSON.stringify({id: 123}),
        outraChave: "valor",
        dataEvento: "2026-05-24T10:00:00Z"
      };

      const formatado = formatarMetadados(JSON.stringify(metadata));

      expect(formatado["Navegador"]).toBe("Chrome no Linux");
      expect(formatado["Acesso"]).toBe("ADMIN - UNIDADE X");
      expect(formatado["Resolução"]).toBe("1920x1080");
      expect(formatado["Rota"]).toBe("/home?id=123");
      expect(formatado["OutraChave"]).toBe("valor");
      expect(formatado["DataEvento"]).toBe("24/05/2026 07:00"); // Sem segundos
    });

    it("deve lidar com User Agent vazio ou desconhecido", () => {
      const metadata = {userAgent: ""};
      const formatado = formatarMetadados(JSON.stringify(metadata));
      expect(formatado["Navegador"]).toBe("Desconhecido");

      const metadata2 = {userAgent: "Navegador de Marte"};
      const formatado2 = formatarMetadados(JSON.stringify(metadata2));
      expect(formatado2["Navegador"]).toBe("Outro no Desconhecido");
    });

    it("deve lidar com falha no parse da rotaQuery", () => {
      const metadata = {
        rotaCaminho: "/erro",
        rotaQuery: "{invalido}"
      };
      const formatado = formatarMetadados(JSON.stringify(metadata));
      expect(formatado["Rota"]).toBe("/erro");
    });

    it("deve formatar Acesso com apenas perfil ou apenas unidade", () => {
      expect(formatarMetadados(JSON.stringify({perfilAtivo: "USER"}))["Acesso"]).toBe("USER");
      expect(formatarMetadados(JSON.stringify({unidadeAtiva: "DEPTO"}))["Acesso"]).toBe("DEPTO");
    });

    it("deve detectar diferentes navegadores e SOs", () => {
      const uas = [
        {ua: "Mozilla/5.0 (Windows NT 10.0; Win64; x64) Firefox/120.0", expected: "Firefox no Windows"},
        {ua: "Mozilla/5.0 (Macintosh; Intel Mac OS X 10_15_7) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/120.0.0.0 Safari/537.36 Edg/120.0.0.0", expected: "Edge no macOS"},
        {ua: "Mozilla/5.0 (iPhone; CPU iPhone OS 17_0 like Mac OS X) AppleWebKit/605.1.15 (KHTML, like Gecko) Version/17.0 Mobile/15E148 Safari/604.1", expected: "Safari no iOS"},
        {ua: "Mozilla/5.0 (Linux; Android 14; Pixel 8) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/120.0.0.0 Mobile Safari/537.36", expected: "Chrome no Android"}
      ];

      uas.forEach(({ua, expected}) => {
        const formatado = formatarMetadados(JSON.stringify({userAgent: ua}));
        expect(formatado["Navegador"]).toBe(expected);
      });
    });
  });
});
