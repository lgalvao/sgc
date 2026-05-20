# Relatório de consistência de nomenclatura (SGC)

## Escopo e método

Este relatório foi produzido a partir de dois scripts novos do toolkit:

- `node etc/scripts/sgc.js codigo nomes coletar-simbolos`
- `node etc/scripts/sgc.js codigo nomes auditar-consistencia`

Saídas utilizadas:

- `/home/runner/work/sgc/sgc/etc/qualidade/nomenclatura/latest/simbolos.json`
- `/home/runner/work/sgc/sgc/etc/qualidade/nomenclatura/latest/simbolos-resumo.md`
- `/home/runner/work/sgc/sgc/etc/qualidade/nomenclatura/latest/consistencia.json`
- `/home/runner/work/sgc/sgc/etc/qualidade/nomenclatura/latest/consistencia-resumo.md`

## Inventário geral de símbolos

Snapshot atual:

- Arquivos analisados: **1128**
- Pacotes Java: **51**
- Tipos catalogados (`class/interface/enum/record/type`): **1090**
- Membros catalogados (métodos/funções/construtores): **4437**

Distribuição por linguagem:

| Linguagem | Arquivos | Tipos | Membros |
|---|---:|---:|---:|
| Java | 581 | 772 | 3107 |
| TypeScript | 399 | 253 | 847 |
| JavaScript | 53 | 0 | 277 |
| Vue | 95 | 65 | 206 |

Hotspots de densidade de membros (top 10):

| Arquivo | Tipos | Membros |
|---|---:|---:|
| `backend/src/main/java/sgc/processo/service/ProcessoService.java` | 5 | 92 |
| `backend/src/test/java/sgc/processo/service/ProcessoServiceTest.java` | 15 | 80 |
| `backend/src/test/java/sgc/subprocesso/SubprocessoControllerTest.java` | 6 | 72 |
| `backend/src/main/java/sgc/subprocesso/SubprocessoController.java` | 1 | 60 |
| `backend/src/main/java/sgc/relatorio/RelatorioFacade.java` | 4 | 53 |
| `backend/src/test/java/sgc/subprocesso/service/SubprocessoConsultaServiceTest.java` | 9 | 50 |
| `backend/src/main/java/sgc/mapa/service/MapaManutencaoService.java` | 1 | 44 |
| `backend/src/main/java/sgc/subprocesso/service/SubprocessoConsultaService.java` | 2 | 43 |
| `backend/src/test/java/sgc/mapa/service/ImpactoMapaServiceTest.java` | 3 | 41 |
| `backend/src/test/java/sgc/processo/ProcessoControllerTest.java` | 8 | 39 |

## Diagnóstico de inconsistência

## 1) Pacotes e diretórios

- Pacotes Java fora de `lowercase.dotted`: **0** (positivo).
- Formatos de diretórios encontrados:
  - `minusculo`: 67
  - `kebab-case`: 1
  - `camelCase`: 1
  - `outro`: 2 (`.storybook`, `__tests__`)

Diagnóstico:

- Existe predominância de padrão coerente em diretórios de domínio.
- Ainda há mistura estrutural de formatos de diretório (especialmente pastas técnicas e de teste), o que dificulta regra única de leitura e automação.

## 2) Nome de arquivos

Formatos por extensão:

- `.java`: `PascalCase` 556, `kebab-case` 25
- `.vue`: `PascalCase` 95
- `.ts`: `outro` 247, `camelCase` 73, `minusculo` 58, `kebab-case` 21
- `.js`: `kebab-case` 37, `minusculo` 13, `outro` 3

Diagnóstico:

- Backend Java está majoritariamente consistente (PascalCase), com exceções em arquivos técnicos.
- Frontend/scripts está altamente heterogêneo; o maior volume está em `outro` para `.ts`, mostrando ausência de padrão único consolidado.

## 3) Tipos (classes/interfaces/enums/records/types)

- Tipos fora de PascalCase: **0**.

Diagnóstico:

- A camada de tipos está relativamente madura e consistente.
- O problema principal não está no nome de tipos, e sim em membros, arquivos e convenções cruzadas por área.

## 4) Métodos e funções

- Membros fora de `camelCase`: **321**.

Principais padrões encontrados:

1. **Métodos de repositório Spring Data com underscore em propriedades aninhadas**  
   Ex.: `findBySubprocesso_CodigoOrderByDataHoraCriacaoDesc`, `deleteByMapa_Codigo`, `findByAtividade_Codigo`.
2. **Nomenclatura de testes backend com estilo BDD usando underscore**  
   Ex.: `marcarComoLidos_quandoSucesso_deveRetornarOk`, `testCriarProcesso_descricaoVazia_falha`.
3. **Combinação de estilos entre áreas**  
   Há regiões com camelCase puro e outras com tokens separados por `_`, gerando padrões conflitantes para manutenção e busca.

## 5) Parâmetros

- Parâmetros fora de camelCase: **7** (baixo volume).
- Parâmetros com uso de `id` (em vez de `codigo`): **5**.

Exemplos relevantes de `id`:

- `backend/src/main/java/sgc/feedback/FeedbackController.java` (`UUID id`)
- `backend/src/main/java/sgc/feedback/FeedbackService.java` (`UUID id`)
- `backend/src/test/java/sgc/fixture/UnidadeFixture.java` (`Long id`)
- `e2e/helpers/helpers-processos.ts` (`testId`)
- `frontend/src/composables/useMapaCompetenciasMutacoes.ts` (`competenciaId`)

Diagnóstico:

- Há desvio pontual, mas crítico por conflitar diretamente com a diretriz do projeto de preferir `codigo` em vez de `id`.

## 6) Conclusão objetiva do estado atual

A nomenclatura não está homogênea no projeto como um todo.  
Mesmo com boa consistência em pacotes Java e tipos, há fragmentação relevante em:

- nomes de arquivo (especialmente TS/JS),
- nomes de métodos/funções (sobretudo repositórios e testes),
- uso de parâmetros `id` versus `codigo`.

Isso confirma que hoje existem **múltiplos dialetos de nomeação coexistindo**.

## Plano detalhado de melhoria

## Fase 1 — Definição oficial e fonte única de verdade

1. Consolidar um guia único de nomenclatura no repositório (backend, frontend, e2e, scripts, testes).
2. Definir matriz explícita `contexto -> formato esperado`:
   - classes/records/enums/interfaces,
   - métodos de produção,
   - métodos de teste,
   - nomes de arquivos por stack,
   - parâmetros (incluindo regra `codigo`).
3. Definir exceções permitidas e justificadas (ex.: métodos derivados de Spring Data quando inevitável).

Critério de aceite:

- Guia aprovado e referenciado nos READMEs principais.

## Fase 2 — Baseline automatizado e orçamento de inconsistência

1. Versionar os artefatos de baseline gerados pelos scripts de nomenclatura.
2. Transformar os indicadores em “budget ratchet”:
   - nenhum novo caso fora de padrão por PR,
   - redução incremental obrigatória por sprint no backlog legado.
3. Integrar execução no fluxo de qualidade para visibilidade contínua.

Critério de aceite:

- Pipeline bloqueia regressão de nomenclatura.

## Fase 3 — Normalização do backend

1. Tratar inconsistências de métodos em camadas de teste e apoio.
2. Revisar métodos de repositório com `_` e decidir padrão alvo:
   - manter exceções com documentação explícita, ou
   - migrar para estratégias alternativas de consulta onde aplicável.
3. Corrigir usos remanescentes de `id` para `codigo` em contratos internos e APIs (com plano de compatibilidade quando necessário).

Critério de aceite:

- Queda mensurável dos 321 membros fora de padrão e eliminação dos usos não justificados de `id`.

## Fase 4 — Normalização do frontend/e2e/scripts

1. Definir e aplicar padrão de nomes de arquivo para `.ts` e `.js` (prioridade para maior volume em `outro`).
2. Padronizar funções/composables/utilitários e nomes de parâmetros (`codigo*` em vez de `*Id` quando aplicável).
3. Corrigir nomenclatura em testes e stories para reduzir drift entre produção e suíte de testes.

Critério de aceite:

- Redução contínua da heterogeneidade por extensão e convergência para no máximo um padrão principal por contexto.

## Fase 5 — Governança contínua

1. Tornar os scripts de nomenclatura etapa recorrente de auditoria.
2. Publicar relatório periódico com:
   - evolução de indicadores,
   - top hotspots,
   - pendências priorizadas.
3. Incluir checklist de nomenclatura no template de PR/revisão.

Critério de aceite:

- Tendência estável de redução de inconsistências por release.

## Ordem de execução recomendada

1. Formalizar regras (Fase 1).  
2. Bloquear regressão automática (Fase 2).  
3. Atacar backend e frontend em paralelo por lotes pequenos (Fases 3 e 4).  
4. Sustentar com governança permanente (Fase 5).

Sem esse encadeamento, a limpeza pontual tende a regredir rapidamente.
