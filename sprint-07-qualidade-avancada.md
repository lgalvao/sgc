# Sprint 7: Qualidade Avançada

**Baseado em:** `analise-junit-nova.md` - Onda 7

## Objetivo
Elevar a robustez real dos testes, verificando comportamentos complexos e reduzindo repetição.

## Tarefas
- Parametrizar testes repetitivos (usando `@ParameterizedTest`).
- Completar asserções de exceção (verificar mensagem, não apenas o tipo).
- Testar efeitos colaterais de eventos assíncronos (se houver listeners).

## Critérios de Aceite
- Menos código duplicado em testes de cenários similares.
- Asserções de exceção mais rigorosas.
- Maior verificação de comportamento real do sistema.

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
