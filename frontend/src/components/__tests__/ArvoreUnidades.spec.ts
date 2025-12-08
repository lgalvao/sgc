import {beforeEach, describe, expect, it} from "vitest";
import type {Unidade} from "@/types/tipos";

/**
 * Testes para o componente ArvoreUnidades
 *
 * Estrutura da árvore de teste:
 *
 * STIC (2) - INTEROPERACIONAL
 *   └─ COSIS (6) - INTERMEDIARIA
 *       ├─ SEDESENV (8) - OPERACIONAL
 *       ├─ SEDIA (9) - OPERACIONAL
 *       └─ SESEL (10) - OPERACIONAL
 *
 * SGP (200) - INTERMEDIARIA
 *   └─ CAS (201) - INTEROPERACIONAL (pode participar sem filhas!)
 *       └─ SAS (202) - OPERACIONAL
 */

describe("ArvoreUnidades - Seleção Hierárquica", () => {
  let unidades: Unidade[];

  beforeEach(() => {
    // Mock da árvore de unidades
    unidades = [
      {
        codigo: 2,
          nome: "Secretaria de Informática e Comunicações",
          sigla: "STIC",
          tipo: "INTEROPERACIONAL",
        filhas: [
          {
            codigo: 6,
              nome: "Coordenadoria de Sistemas",
              sigla: "COSIS",
              tipo: "INTERMEDIARIA",
            filhas: [
              {
                codigo: 8,
                  nome: "Seção de Desenvolvimento de Sistemas",
                  sigla: "SEDESENV",
                  tipo: "OPERACIONAL",
                  filhas: [],
              },
              {
                codigo: 9,
                  nome: "Seção de Dados e Inteligência Artificial",
                  sigla: "SEDIA",
                  tipo: "OPERACIONAL",
                  filhas: [],
              },
              {
                codigo: 10,
                  nome: "Seção de Sistemas Eleitorais",
                  sigla: "SESEL",
                  tipo: "OPERACIONAL",
                  filhas: [],
              },
            ],
          },
        ],
      },
      {
        codigo: 200,
          nome: "Secretaria de Gestao de Pessoas",
          sigla: "SGP",
          tipo: "INTERMEDIARIA",
        filhas: [
          {
            codigo: 201,
              nome: "Coordenadoria de Atenção ao Servidor",
              sigla: "CAS",
              tipo: "INTEROPERACIONAL",
            filhas: [
              {
                codigo: 202,
                  nome: "Seção de Atenção ao Servidor",
                  sigla: "SAS",
                  tipo: "OPERACIONAL",
                  filhas: [],
              },
            ],
          },
        ],
      },
    ];

    // unidadesSelecionadasLocal = [];
  });

  // Funções auxiliares (copiadas do componente)
  function getTodasSubunidades(unidade: Unidade): number[] {
    let subunidades: number[] = [];
    if (unidade.filhas) {
      for (const filha of unidade.filhas) {
        subunidades.push(filha.codigo);
        subunidades = subunidades.concat(getTodasSubunidades(filha));
      }
    }
    return subunidades;
  }

    function encontrarUnidade(
        codigo: number,
        unidadesArray: Unidade[],
    ): Unidade | null {
    for (const unidade of unidadesArray) {
      if (unidade.codigo === codigo) {
        return unidade;
      }
      if (unidade.filhas && unidade.filhas.length > 0) {
        const encontrada = encontrarUnidade(codigo, unidade.filhas);
        if (encontrada) return encontrada;
      }
    }
    return null;
  }

  function isFolha(unidade: Unidade): boolean {
    return !unidade.filhas || unidade.filhas.length === 0;
  }

  // Simula o comportamento do watcher
    function processarSelecao(
        selecionadasAnteriores: number[],
        selecionadasNovas: number[],
    ): number[] {
        const adicionadas = selecionadasNovas.filter(
            (c) => !selecionadasAnteriores.includes(c),
        );
        const removidas = selecionadasAnteriores.filter(
            (c) => !selecionadasNovas.includes(c),
        );

    let novaSelecao = [...selecionadasNovas];

    // Processar adicionadas
    for (const codigo of adicionadas) {
      const unidade = encontrarUnidade(codigo, unidades);
      if (unidade) {
        const subunidades = getTodasSubunidades(unidade);
        for (const sub of subunidades) {
          if (!novaSelecao.includes(sub)) {
            novaSelecao.push(sub);
          }
        }
      }
    }

    // Processar removidas
    // Só processar se foi UMA ÚNICA unidade removida (clique do usuário)
    // Se várias foram removidas = cascata automática, não processar
    if (removidas.length === 1) {
      const codigo = removidas[0];
      const unidade = encontrarUnidade(codigo, unidades);
      if (unidade && !isFolha(unidade)) {
        // É uma unidade com filhas
        // Usuário clicou para desmarcar - remover todas as filhas
        const subunidades = getTodasSubunidades(unidade);
          novaSelecao = novaSelecao.filter((c) => !subunidades.includes(c));
      }
    }

    // Verificar pais
    function verificarPais(unidadesArray: Unidade[]) {
      // Primeiro processar recursivamente as filhas
      for (const unidade of unidadesArray) {
        if (unidade.filhas && unidade.filhas.length > 0) {
          verificarPais(unidade.filhas);
        }
      }

      // Depois processar este nível
      for (const unidade of unidadesArray) {
        if (!isFolha(unidade)) {
          // Verificar apenas as FILHAS DIRETAS, não todas as descendentes
            const filhasDirectas = unidade.filhas?.map((f) => f.codigo) || [];

            const selecionadas = filhasDirectas.filter((s) =>
                novaSelecao.includes(s),
            ).length;
          const total = filhasDirectas.length;

          if (selecionadas === total && total > 0) {
            if (!novaSelecao.includes(unidade.codigo)) {
              novaSelecao.push(unidade.codigo);
            }
          } else if (selecionadas === 0) {
            // Nenhuma selecionada → desmarcar o pai
            // EXCETO: INTEROPERACIONAL que estava explicitamente marcada antes
              const estaExplicitamenteMarcada = selecionadasAnteriores.includes(
                  unidade.codigo,
              );
              if (
                  unidade.tipo === "INTEROPERACIONAL" &&
                  estaExplicitamenteMarcada
              ) {
              // Manter marcada
            } else {
                  novaSelecao = novaSelecao.filter((c) => c !== unidade.codigo);
            }
          } else {
            // Algumas selecionadas → estado indeterminate
            // EXCETO: INTEROPERACIONAL que estava explicitamente marcada antes
              const estaExplicitamenteMarcada = selecionadasAnteriores.includes(
                  unidade.codigo,
              );
              if (
                  unidade.tipo === "INTEROPERACIONAL" &&
                  estaExplicitamenteMarcada
              ) {
              // Manter marcada
            } else {
                  novaSelecao = novaSelecao.filter((c) => c !== unidade.codigo);
            }
          }
        }
      }
    }

    verificarPais(unidades);

    return novaSelecao;
  }

    describe("Regra 1: Selecionar pai → seleciona todas filhas", () => {
        it("deve selecionar todas filhas ao marcar COSIS", () => {
      const antes: number[] = [];
      const depois: number[] = [6]; // Marcar COSIS

      const resultado = processarSelecao(antes, depois);

            expect(resultado).toContain(6); // COSIS
            expect(resultado).toContain(8); // SEDESENV
            expect(resultado).toContain(9); // SEDIA
      expect(resultado).toContain(10); // SESEL
      // STIC tem apenas uma filha (COSIS), então será marcada também
            expect(resultado).toContain(2); // STIC
      expect(resultado).toHaveLength(5);
    });

        it("deve selecionar todas filhas e netas ao marcar STIC", () => {
      const antes: number[] = [];
      const depois: number[] = [2]; // Marcar STIC

      const resultado = processarSelecao(antes, depois);

            expect(resultado).toContain(2); // STIC
            expect(resultado).toContain(6); // COSIS
            expect(resultado).toContain(8); // SEDESENV
            expect(resultado).toContain(9); // SEDIA
      expect(resultado).toContain(10); // SESEL
      expect(resultado).toHaveLength(5);
    });
  });

    describe("Regra 2: Desmarcar pai → desmarca todas filhas", () => {
        it("deve desmarcar todas filhas ao desmarcar COSIS", () => {
      const antes: number[] = [6, 8, 9, 10]; // Tudo marcado
      const depois: number[] = []; // Desmarcar tudo via COSIS

      const resultado = processarSelecao(antes, depois);

      expect(resultado).toEqual([]);
    });

        it("deve desmarcar tudo ao desmarcar STIC", () => {
      const antes: number[] = [2, 6, 8, 9, 10]; // Tudo marcado
      const depois: number[] = []; // Desmarcar tudo via STIC

      const resultado = processarSelecao(antes, depois);

      expect(resultado).toEqual([]);
    });
  });

    describe("Regra 3: Selecionar todas filhas → pai fica marcado", () => {
        it("deve marcar COSIS quando todas as 3 filhas forem marcadas", () => {
      const antes: number[] = [];
      const depois: number[] = [8, 9, 10]; // Marcar as 3 filhas

      const resultado = processarSelecao(antes, depois);

            expect(resultado).toContain(6); // COSIS deve estar marcada
            expect(resultado).toContain(8); // SEDESENV
            expect(resultado).toContain(9); // SEDIA
      expect(resultado).toContain(10); // SESEL
      // Nota: STIC (2) também será marcada porque COSIS está marcada
            expect(resultado).toContain(2); // STIC
      expect(resultado).toHaveLength(5);
    });

        it("deve marcar STIC e COSIS quando todas filhas/netas forem marcadas", () => {
      const antes: number[] = [];
      const depois: number[] = [8, 9, 10]; // Marcar as 3 netas

      const resultado = processarSelecao(antes, depois);

            expect(resultado).toContain(2); // STIC
            expect(resultado).toContain(6); // COSIS
            expect(resultado).toContain(8); // SEDESENV
            expect(resultado).toContain(9); // SEDIA
      expect(resultado).toContain(10); // SESEL
      expect(resultado).toHaveLength(5);
    });
  });

    describe("Regra 4: Desmarcar uma filha → pai fica indeterminate", () => {
        it("deve remover COSIS da lista quando desmarcar SEDIA (indeterminate)", () => {
      const antes: number[] = [6, 8, 9, 10]; // Tudo marcado
            const depois: number[] = [6, 8, 10]; // Desmarcar SEDIA (9)

      const resultado = processarSelecao(antes, depois);

      expect(resultado).not.toContain(6); // COSIS não deve estar (indeterminate)
            expect(resultado).toContain(8); // SEDESENV deve permanecer
      expect(resultado).not.toContain(9); // SEDIA não deve estar
            expect(resultado).toContain(10); // SESEL deve permanecer
    });

        it("deve manter outras filhas marcadas ao desmarcar uma", () => {
      const antes: number[] = [2, 6, 8, 9, 10]; // Tudo marcado
            const depois: number[] = [2, 6, 8, 10]; // Desmarcar SEDIA

      const resultado = processarSelecao(antes, depois);

      // SEDIA deve ser removida
      expect(resultado).not.toContain(9);

      // SEDESENV e SESEL devem permanecer
      expect(resultado).toContain(8);
      expect(resultado).toContain(10);

      // COSIS fica indeterminate (removida - é INTERMEDIARIA)
      expect(resultado).not.toContain(6);

      // STIC PERMANECE (é INTEROPERACIONAL)
      expect(resultado).toContain(2);
    });
  });

    describe("Regra 5: Desmarcar todas filhas → pai fica desmarcado", () => {
        it("deve remover COSIS quando todas filhas forem desmarcadas", () => {
      const antes: number[] = [6, 8, 9, 10]; // Tudo marcado
            const depois: number[] = []; // Desmarcar tudo

      const resultado = processarSelecao(antes, depois);

            expect(resultado).toEqual([]);
    });
  });

    describe("Cenários complexos", () => {
        it("deve lidar com seleção incremental", () => {
      // Passo 1: Marcar SEDESENV
      let resultado = processarSelecao([], [8]);
      expect(resultado).toEqual([8]);
      expect(resultado).not.toContain(6); // COSIS não marcada

            // Passo 2: Marcar SEDIA
      resultado = processarSelecao(resultado, [...resultado, 9]);
      expect(resultado).toContain(8);
      expect(resultado).toContain(9);
      expect(resultado).not.toContain(6); // COSIS ainda não marcada

            // Passo 3: Marcar SESEL
      resultado = processarSelecao(resultado, [...resultado, 10]);
            expect(resultado).toContain(6); // COSIS agora deve estar marcada
      expect(resultado).toContain(8);
      expect(resultado).toContain(9);
      expect(resultado).toContain(10);
    });

        it("cenário do bug: marcar STIC, depois desmarcar uma filha", () => {
      // Passo 1: Marcar STIC
      let resultado = processarSelecao([], [2]);
      expect(resultado).toHaveLength(5); // 2, 6, 8, 9, 10

            // Passo 2: Desmarcar SEDIA
            const semSedia = resultado.filter((c) => c !== 9);
      resultado = processarSelecao(resultado, semSedia);

            // SEDIA deve estar removida
      expect(resultado).not.toContain(9);

            // SEDESENV e SESEL devem PERMANECER marcadas
      expect(resultado).toContain(8);
      expect(resultado).toContain(10);

            // COSIS fica indeterminate (removida - é INTERMEDIARIA)
      expect(resultado).not.toContain(6);

            // STIC PERMANECE (é INTEROPERACIONAL)
      expect(resultado).toContain(2);

            // Total: apenas 2, 8 e 10
      expect(resultado).toHaveLength(3);
      expect(resultado).toContain(2);
      expect(resultado).toContain(8);
      expect(resultado).toContain(10);
    });

        it("deve marcar raiz quando todas filhas/netas estiverem marcadas", () => {
      // Marcar manualmente todas as netas (8, 9, 10)
      const resultado = processarSelecao([], [8, 9, 10]);

            // Deve marcar COSIS (pai das netas)
      expect(resultado).toContain(6);

            // Deve marcar STIC (raiz)
      expect(resultado).toContain(2);

            // Deve conter todas as unidades
      expect(resultado).toContain(8);
      expect(resultado).toContain(9);
      expect(resultado).toContain(10);

            // Total: 5 unidades (2, 6, 8, 9, 10)
      expect(resultado).toHaveLength(5);
    });

        it("deve desmarcar todas filhas/netas ao desmarcar uma unidade pai", () => {
      // Cenário: Marcar STIC (que marca tudo)
      let resultado = processarSelecao([], [2]);
      expect(resultado).toHaveLength(5); // 2, 6, 8, 9, 10

            // Desmarcar STIC
      resultado = processarSelecao(resultado, []);

            // Todas unidades devem estar desmarcadas
            expect(resultado).not.toContain(2); // STIC
            expect(resultado).not.toContain(6); // COSIS
            expect(resultado).not.toContain(8); // SEDESENV
            expect(resultado).not.toContain(9); // SEDIA
      expect(resultado).not.toContain(10); // SESEL

            expect(resultado).toEqual([]);
    });

        it("deve desmarcar filhas ao desmarcar COSIS", () => {
      // Cenário: Marcar COSIS (que marca suas 3 filhas)
      let resultado = processarSelecao([], [6]);
      expect(resultado).toHaveLength(5); // 2, 6, 8, 9, 10

            // Desmarcar COSIS
      resultado = processarSelecao(resultado, []);

            // Todas unidades devem estar desmarcadas
      expect(resultado).toEqual([]);
    });

        it("deve manter hierarquia ao marcar/desmarcar níveis intermediários", () => {
      // Passo 1: Marcar SEDESENV e SEDIA (2 de 3)
      let resultado = processarSelecao([], [8, 9]);
      expect(resultado).toContain(8);
      expect(resultado).toContain(9);
      expect(resultado).not.toContain(6); // COSIS não marcada (indeterminate)
      expect(resultado).not.toContain(2); // STIC não marcada (indeterminate)

            // Passo 2: Adicionar SESEL (agora todas as 3)
      resultado = processarSelecao(resultado, [...resultado, 10]);
            expect(resultado).toContain(6); // COSIS marcada
            expect(resultado).toContain(2); // STIC marcada
      expect(resultado).toHaveLength(5);

            // Passo 3: Remover SESEL novamente
            resultado = processarSelecao(
                resultado,
                resultado.filter((c) => c !== 10),
            );
      expect(resultado).not.toContain(10); // SESEL removida
            expect(resultado).not.toContain(6); // COSIS volta a indeterminate (INTERMEDIARIA)
            expect(resultado).toContain(2); // STIC PERMANECE (INTEROPERACIONAL)
            expect(resultado).toContain(8); // SEDESENV permanece
            expect(resultado).toContain(9); // SEDIA permanece
      expect(resultado).toHaveLength(3);
    });
  });

    describe("Unidades INTEROPERACIONAIS", () => {
        it("deve permitir selecionar CAS independentemente de suas filhas", () => {
      // CAS é INTEROPERACIONAL - pode ser selecionada sozinha
      const resultado = processarSelecao([], [201]);

            // CAS deve estar selecionada
      expect(resultado).toContain(201);

            // Filha SAS também deve ser selecionada (propagação normal)
      expect(resultado).toContain(202);

            // SGP deve estar marcada também (todas filhas diretas marcadas)
      expect(resultado).toContain(200);

            expect(resultado).toHaveLength(3);
    });

        it("CAS pode ficar marcada sem suas filhas (característica INTEROPERACIONAL)", () => {
      // Passo 1: Marcar CAS (marca CAS e SAS)
      let resultado = processarSelecao([], [201]);
      expect(resultado).toContain(201);
      expect(resultado).toContain(202);
      expect(resultado).toContain(200);

            // Passo 2: Desmarcar SAS
            resultado = processarSelecao(
                resultado,
                resultado.filter((c) => c !== 202),
            );

      // CAS deve PERMANECER marcada (INTEROPERACIONAL!)
      expect(resultado).toContain(201);

            // SAS não deve estar
      expect(resultado).not.toContain(202);

            // SGP permanece marcada (CAS ainda está marcada, e é filha de SGP)
      expect(resultado).toContain(200);

            // [201, 200]
      expect(resultado).toHaveLength(2);
      expect(resultado).toContain(201);
      expect(resultado).toContain(200);
    });

        it("STIC também pode ficar marcada sem todas as filhas", () => {
      // Marcar STIC (marca tudo)
      let resultado = processarSelecao([], [2]);
      expect(resultado).toHaveLength(5); // [2, 6, 8, 9, 10]

            // Desmarcar SEDIA (9)
            resultado = processarSelecao(
                resultado,
                resultado.filter((c) => c !== 9),
            );

      // STIC deve PERMANECER (INTEROPERACIONAL)
      expect(resultado).toContain(2);

            // COSIS não deve estar (é INTERMEDIARIA, fica indeterminate)
      expect(resultado).not.toContain(6);

            // Outras filhas permanecem
      expect(resultado).toContain(8);
      expect(resultado).toContain(10);
      expect(resultado).not.toContain(9);

            // [2, 8, 10]
      expect(resultado).toHaveLength(3);
    });

        it("deve marcar CAS quando sua filha for selecionada", () => {
      // Selecionar SAS
      const resultado = processarSelecao([], [202]);

            // SAS selecionada
      expect(resultado).toContain(202);

            // CAS deve ser marcada (todas filhas selecionadas)
      expect(resultado).toContain(201);

            // SGP deve estar marcada (todas filhas diretas marcadas)
      expect(resultado).toContain(200);

            expect(resultado).toHaveLength(3);
    });

        it("deve comparar comportamento INTEROPERACIONAL vs INTERMEDIARIA", () => {
      // STIC (INTEROPERACIONAL) com filhas
      let resultado = processarSelecao([], [2]);
      expect(resultado).toContain(2);
      expect(resultado).toContain(6);
      expect(resultado).toContain(8);
      expect(resultado).toContain(9);
      expect(resultado).toContain(10);
      expect(resultado).toHaveLength(5);

            // CAS (INTEROPERACIONAL) com filha
      resultado = processarSelecao([], [201]);
      expect(resultado).toContain(201);
      expect(resultado).toContain(202);
      expect(resultado).toContain(200); // SGP marcada (comportamento normal)
      expect(resultado).toHaveLength(3);
    });
  });
});
