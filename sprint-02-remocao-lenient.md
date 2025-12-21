# Sprint 2: Remover LENIENT e Corrigir Stubs

**Baseado em:** `analise-junit-nova.md` - Onda 2

## Objetivo
Aumentar a qualidade do sinal dos testes unitários, garantindo que stubs não utilizados não sejam permitidos.

## Tarefas
- Remover `@MockitoSettings(strictness = LENIENT)`.
- Corrigir stubs não usados e arranges excessivos que causavam falhas sem o modo LENIENT.

## Critérios de Aceite
- `grep -R "Strictness.LENIENT" -n` não retorna resultados no código de teste (exceto talvez em classes legadas que serão removidas posteriormente, mas o ideal é zero).
- Suíte de testes passa integralmente.

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
