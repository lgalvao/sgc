# Sprint 4: Padronização Mecânica

**Baseado em:** `analise-junit-nova.md` - Onda 4

## Objetivo
Garantir consistência de nomenclatura e estrutura para facilitar a leitura e manutenção.

## Tarefas
- Renomear métodos para o padrão `deve{Acao}Quando{Condicao}`.
- Garantir presença de `@DisplayName` em testes de controller, service e integração.
- Introduzir `@Nested` em classes de teste grandes para agrupar cenários.
- Garantir estrutura AAA (Arrange, Act, Assert) explícita.

## Critérios de Aceite
- Testes passam.
- Diferença de estilo reduzida (padronização aplicada em massa ou em lotes significativos).

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
