# Sprint 2: Remover LENIENT e Corrigir Stubs

**Baseado em:** `analise-junit-nova.md` - Onda 2

## Contexto do Projeto SGC

### Estado Atual
- **1 ocorrência** de `Strictness.LENIENT` detectada
- Testes unitários usam `@ExtendWith(MockitoExtension.class)`
- O padrão do Mockito 5+ é **strict stubbing** (stubs não utilizados causam falha)

### O Problema do LENIENT
O modo `LENIENT` desabilita verificações importantes:
- Permite stubs configurados mas não usados no teste
- Mascara arranges excessivos que podem indicar design ruim
- Reduz a capacidade do teste de detectar mudanças na classe sob teste

### Como Corrigir
Quando remover `@MockitoSettings(strictness = LENIENT)`:

1. **Stub não usado**: Remova o `when()` desnecessário
2. **Stub usado condicionalmente**: Use `lenient().when()` pontualmente
3. **Muitos mocks**: Considere refatorar o teste ou a classe (code smell)

### Exemplo de Correção
```java
// ANTES (com LENIENT)
@MockitoSettings(strictness = LENIENT)
class MeuServiceTest {
    @Test
    void teste() {
        when(repo.buscar(1L)).thenReturn(obj1); // não usado!
        when(repo.buscar(2L)).thenReturn(obj2); // usado
        service.processar(2L);
    }
}

// DEPOIS (corrigido)
class MeuServiceTest {
    @Test
    void teste() {
        // Removido stub não usado
        when(repo.buscar(2L)).thenReturn(obj2);
        service.processar(2L);
    }
}
```

## Objetivo
Aumentar a qualidade do sinal dos testes unitários, garantindo que stubs não utilizados não sejam permitidos.

## Tarefas
- Localizar todas as ocorrências de `@MockitoSettings(strictness = LENIENT)`.
- Remover a anotação ou o parâmetro `strictness = LENIENT`.
- Executar `./gradlew :backend:test` e corrigir falhas de strict stubbing:
  - Remover stubs não usados
  - Usar `lenient()` apenas quando realmente necessário
  - Considerar refatoração se há excesso de mocks
- Documentar mudanças e razões.

## Comandos de Verificação

### Localizar LENIENT no código
```bash
grep -R "Strictness.LENIENT" backend/src/test --include="*.java" -n
```

### Localizar anotação MockitoSettings
```bash
grep -R "@MockitoSettings" backend/src/test --include="*.java" -n
```

### Executar testes após remoção
```bash
./gradlew :backend:test
```

### Verificar que não há LENIENT remanescente
```bash
# Deve retornar 0
grep -R "Strictness.LENIENT" backend/src/test --include="*.java" | wc -l
```

## Critérios de Aceite
- `grep -R "Strictness.LENIENT" backend/src/test --include="*.java"` não retorna resultados (exceto talvez em classes legadas documentadas, mas o ideal é zero).
- `./gradlew :backend:test` passa integralmente sem erros.
- Nenhum novo uso de `lenient()` sem justificativa em comentário.

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
