# Análise de Testes Backend - Sistema SGC (Versão Nova)

**Data:** 17 de dezembro de 2025  
**Escopo:** Suíte de testes do backend (unitários + integração)  
**Base de referência:** Relatório anterior `analise-junit.md`  
**Status atual (conforme relatório anterior):** ✅ todos os testes passando  

---

## 0. Objetivo e público-alvo

Este documento é uma revisão completa e operacional da análise de testes do backend do SGC, com foco em:

- **Aumentar confiabilidade e manutenibilidade** dos testes.
- **Reduzir flakiness e fragilidade** (principalmente nos testes de integração).
- **Padronizar** estrutura e nomenclatura para facilitar leitura e evolução.
- **Orientar execução por agente de IA** (passos mecânicos, critérios de aceite objetivos, PRs pequenos).

Não é apenas um diagnóstico: é um **plano executável**.

---

## 1. Sumário executivo

A suíte tem bons fundamentos: bibliotecas modernas (JUnit 5/Mockito/AssertJ), separação entre unitários e integração e presença de testes de arquitetura (ArchUnit). Entretanto, existem pontos que aumentam custo de manutenção e reduzem a capacidade dos testes de sinalizarem problemas reais:

- **Inconsistências** (nomenclatura, `@DisplayName`, organização com/sem `@Nested`).
- **Sinais de over-mocking** (services com muitas dependências, uso de `LENIENT`).
- **Fragilidade em integração** por dependência de dados hardcoded e seed global (`data.sql`).
- **Ruído**: testes de DTO/Model (getters/setters) gerando “cobertura artificial”.

A recomendação é executar refatorações em ondas curtas, com guardrails e validações automáticas, privilegiando mudanças reversíveis e mensuráveis.

---

## 2. Boas práticas observadas ✅

1. **AssertJ**: asserções fluentes e legíveis.
2. **Separação de tipos de teste**:
   - Unitários: `@ExtendWith(MockitoExtension.class)`
   - Integração: `@SpringBootTest` + `@Transactional`
3. **ArchUnit**: `ArchConsistencyTest.java` reforça regras arquiteturais.
4. **Uso parcial de `@DisplayName`**: já existe base para padronização.
5. **`BaseIntegrationTest`**: reutilização de setup e infra para integração.
6. **Padrão Builder em DTOs**: melhora legibilidade (quando usado corretamente).

---

## 3. Problemas e oportunidades (por categoria)

### 3.1 Consistência

**C1 — Nomenclatura inconsistente de métodos de teste**
- Coexistem 3+ padrões (método curto + `@DisplayName`, método descritivo sem `@DisplayName`, nomes com underscore).
- **Impacto:** leitura e relatórios inconsistentes, maior custo de manutenção.

**Recomendação (padrão único):**
- Método: `deve{Acao}Quando{Condicao}` (camelCase, português)
- `@DisplayName`: frase curta e clara em português

---

**C2 — Uso inconsistente de `@DisplayName`**
- Parte dos testes tem `@DisplayName`, outros não.
- **Impacto:** relatórios menos úteis; menor “documentação viva”.

**Recomendação:** `@DisplayName` obrigatório em testes de service/controller/integration.

---

**C3 — Organização desigual com `@Nested`**
- Classes com muitos testes sem agrupamento.
- **Impacto:** difícil localizar cenários e manter AAA.

**Recomendação:** usar `@Nested` para agrupar por feature/fluxo.

---

### 3.2 Robustez

**R1 — `MockitoSettings(strictness = LENIENT)` mascarando problemas**
- `LENIENT` permite stubs não usados e arranges excessivos.
- **Impacto:** testes passam “por acidente”; baixa qualidade de sinal.

**Recomendação:** remover `LENIENT` e corrigir strict stubbing; usar `lenient()` apenas pontualmente.

---

**R2 — Dados hardcoded e magic numbers**
- IDs como `99L` e credenciais/identificadores fixos.
- Integração acoplada a dados de seed (`data.sql`).

**Recomendação:**
- Unit: constantes/fixtures/builders.
- Integração: **dados por teste** (setup programático ou `@Sql` por classe/teste).

---

**R3 — Asserções de estado incompletas**
- Verifica só 1 atributo e o `save`, sem garantir invariantes.

**Recomendação:** asserts mais completos, validando invariantes relevantes (tipo/situação/relacionamentos).

---

**R4 — Testes de exceção incompletos**
- Verificam apenas o tipo da exceção.

**Recomendação:** verificar também mensagem e/ou campos relevantes (`hasMessageContaining`, causa, códigos).

---

### 3.3 Clareza

**L1 — Setup duplicado e ausência de AAA**
- Arranges repetidos e testes com mistura de fases.

**Recomendação:**
- `@BeforeEach` + helpers privados.
- AAA explícito (`// Arrange`, `// Act`, `// Assert`).

---

**L2 — Testes de getters/setters (DTO/Model) sem valor agregado**
- Testam Lombok/boilerplate.
- **Impacto:** cobertura artificial, tempo de execução, ruído.

**Recomendação:** remover testes de DTO/Model quando não houver regra (validação, serialização, invariantes).

---

**L3 — Cenários complexos sem documentação**
- Workflows e hierarquias configuradas sem explicação.

**Recomendação:** comentários curtos de intenção e `@DisplayName` orientado ao comportamento.

---

## 4. Diretrizes para agentes de IA (modo de execução)

### 4.1 Regras operacionais

- Trabalhar em **PRs pequenos** (1 tema por PR).
- Cada PR deve conter:
  - descrição do objetivo,
  - comandos de validação executados,
  - métricas simples (ex.: arquivos afetados, número de ocorrências removidas por grep).
- **Evitar refatorar produção** junto com refatoração de teste, exceto quando estritamente necessário.

### 4.2 Critérios universais de aceite (para qualquer PR)

1. `./gradlew test` (ou `mvn test`) passa.
2. Não aumentar flakiness (nenhum teste novo com `Thread.sleep`).
3. Não reintroduzir `Strictness.LENIENT`.
4. Sem hardcode em integração sem criação explícita do dado.

---

## 5. Plano de execução em ondas (orientado a agentes)

> Cada onda deve virar 1 PR (ou 2 PRs, se muito grande). O agente deve sempre manter diffs pequenos.

### Onda 0 — Baseline e guardrails (infra de validação)

**Objetivo:** garantir medições e diagnóstico rápido.

**Tarefas**
- Garantir pipeline rodando testes e publicando relatórios.
- Documentar comandos de execução.

**Aceite**
- CI executa testes e falha se houver erro.

---

### Onda 1 — Remover testes “boilerplate” (DTO/Model)

**Objetivo:** reduzir ruído e cobertura artificial.

**Tarefas**
- Remover testes de getters/setters e builders sem regra.
- Manter apenas testes onde exista validação/regra.

**Aceite**
- Testes passam.
- Redução objetiva de arquivos/linhas de testes inúteis.

---

### Onda 2 — Remover `LENIENT` e corrigir stubs

**Objetivo:** aumentar qualidade do sinal dos testes unitários.

**Tarefas**
- Remover `@MockitoSettings(strictness = LENIENT)`.
- Corrigir stubs não usados e arranges excessivos.

**Aceite**
- `grep -R "Strictness.LENIENT" -n` sem resultados.
- Suíte passa.

---

### Onda 3 — Fixtures/builders e deduplicação de setup

**Objetivo:** reduzir duplicação e magic numbers.

**Tarefas**
- Criar pacote `fixture`/`testdata` com builders reutilizáveis.
- Extrair setup comum para `@BeforeEach`.

**Aceite**
- Redução de duplicação em classes alvo.

---

### Onda 4 — Padronização mecânica (nomenclatura, `@DisplayName`, `@Nested`, AAA)

**Objetivo:** consistência e legibilidade.

**Tarefas**
- Renomear métodos para `deve{Acao}Quando{Condicao}`.
- Garantir `@DisplayName` em testes relevantes.
- Introduzir `@Nested` em classes grandes.
- AAA explícito.

**Aceite**
- Testes passam.
- Diferença de estilo reduzida (pelo menos em N classes por PR).

---

### Onda 5 — Desacoplar testes CDU do seed global (`data.sql`)

**Objetivo:** tornar integração isolada, robusta e paralelizável.

**Estratégias possíveis (escolher 1 e aplicar consistentemente)**

**Estratégia A — `@Sql` por classe/teste**
- Criar datasets mínimos e explícitos.

**Estratégia B — Setup programático via repositórios + fixtures**
- Criar entidades no `@BeforeEach`.

**Aceite**
- Nenhum CDU depende de identificadores globais hardcoded sem criação no próprio teste.

---

### Onda 6 — Cobertura: JaCoCo e visibilidade

**Objetivo:** medir e evitar regressões.

**Tarefas**
- Integrar JaCoCo.
- Publicar relatório no CI.
- Gate inicial brando (não reduzir baseline).

**Aceite**
- Relatórios gerados.

---

### Onda 7 — Qualidade avançada

**Objetivo:** elevar robustez real.

**Tarefas**
- Parametrizar testes repetitivos.
- Completar asserts de exceção.
- Testar efeitos colaterais de eventos assíncronos (quando existirem listeners).

**Aceite**
- Menos duplicação.
- Mais verificação de comportamento real.

---

## 6. Guia de estilo (padrão obrigatório)

### 6.1 Estrutura AAA

```java
@Test
@DisplayName("Deve criar processo quando dados válidos")
void deveCriarProcessoQuandoDadosValidos() {
    // Arrange

    // Act

    // Assert
}
```

### 6.2 Nomenclatura

- **Método:** `deve{Acao}Quando{Condicao}`
- **Variáveis:** português, nomes completos e descritivos.
- **Agrupamento:** `@Nested` por feature/fluxo.

### 6.3 Mockito

- Proibido `Strictness.LENIENT` como padrão.
- Preferir stubs locais por teste.

---

## 7. Checklist de revisão (para agente de IA)

Antes de abrir PR:

- [ ] Testes passam local/CI.
- [ ] `LENIENT` não aparece no diff.
- [ ] Não houve adição de `Thread.sleep`.
- [ ] Integração não depende de seed global sem setup explícito.
- [ ] PR descreve comandos executados e métricas simples (grep/contagem de arquivos).

---

## 8. Próximos passos recomendados

1. Executar Onda 1 (remover DTO/Model boilerplate).
2. Executar Onda 2 (`LENIENT`).
3. Introduzir fixtures (Onda 3).
4. Só então padronizar nomes/AAA em lote (Onda 4).
5. Migrar CDUs para padrão único (Onda 5).
