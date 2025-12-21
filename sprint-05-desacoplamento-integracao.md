# Sprint 5: Desacoplar Testes CDU do Seed Global

**Baseado em:** `analise-junit-nova.md` - Onda 5

## Objetivo
Tornar os testes de integração isolados, robustos e paralelizáveis, removendo a dependência de dados globais (`data.sql`).

## Tarefas
- Escolher e aplicar consistentemente uma estratégia:
  - **Estratégia A:** `@Sql` por classe/teste com datasets mínimos.
  - **Estratégia B:** Setup programático via repositórios + fixtures no `@BeforeEach`.
- Refatorar testes de Casos de Uso (CDU) para não dependerem de IDs hardcoded do seed global.

## Critérios de Aceite
- Nenhum teste de CDU depende de identificadores globais hardcoded sem que o dado seja criado explicitamente no próprio teste ou setup da classe.
- Testes continuam passando.

---

## Diretrizes para agentes de IA (Regras de Ouro)

1. **PRs Pequenos:** Um tema por PR.
2. **Critérios Universais de Aceite:**
   - `./gradlew test` (ou `mvn test`) passa.
   - Não aumentar flakiness (nenhum teste novo com `Thread.sleep`).
   - Não reintroduzir `Strictness.LENIENT`.
   - Sem hardcode em integração sem criação explícita.
3. **Não refatorar produção** a menos que estritamente necessário para o teste.

## Guia de Estilo (Obrigatório)

### Estrutura AAA
```java
@Test
@DisplayName("Deve criar processo quando dados válidos")
void deveCriarProcessoQuandoDadosValidos() {
    // Arrange
    // Act
    // Assert
}
```

### Nomenclatura
- **Método:** `deve{Acao}Quando{Condicao}`
- **Variáveis:** Português, descritivas.
- **Agrupamento:** `@Nested` por feature/fluxo.

### Mockito
- **Proibido:** `Strictness.LENIENT` (padrão).
- **Preferência:** Stubs locais.

## Checklist de Revisão

- [ ] Testes passam local/CI.
- [ ] `LENIENT` não aparece no diff.
- [ ] Não houve adição de `Thread.sleep`.
- [ ] Integração não depende de seed global sem setup explícito.
- [ ] PR descreve comandos executados e métricas simples (grep/contagem de arquivos).
