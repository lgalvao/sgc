# Sprint 4: Padronização Mecânica

**Baseado em:** `analise-junit-nova.md` - Onda 4

## Contexto do Projeto SGC

### Estado Atual da Padronização
- **478 anotações @DisplayName** (múltiplos usos em ~98 arquivos de teste)
- **56 anotações @Nested** para organização de cenários
- **0 testes parametrizados** - será implementado no Sprint 7
- Coexistem múltiplos padrões de nomenclatura:
  - `deve{Acao}Quando{Condicao}` (padrão recomendado) ✅
  - `test{Acao}` (padrão antigo) ❌
  - Nomes com underscore `test_cenario_especifico` ❌

### Padrão Oficial do Projeto
Conforme `AGENTS.md` e `backend/README.md`:
- **Nomenclatura de métodos**: `deve{Acao}Quando{Condicao}` (camelCase, português)
- **@DisplayName**: Obrigatório, frase descritiva em português
- **@Nested**: Usar para agrupar cenários relacionados
- **Estrutura AAA**: Comentários `// Arrange`, `// Act`, `// Assert`

### Exemplos de Arquivos a Padronizar

#### Antes (Inconsistente)
```java
@Test
void test() {
    // código misturado sem estrutura
}

@Test
void shouldCreateProcesso() {
    // nome em inglês
}
```

#### Depois (Padronizado)
```java
@Test
@DisplayName("Deve criar processo quando dados válidos")
void deveCriarProcessoQuandoDadosValidos() {
    // Arrange
    var dados = ProcessoFixture.dadosValidos();
    
    // Act
    var resultado = service.criar(dados);
    
    // Assert
    assertThat(resultado).isNotNull();
}
```

### Uso de @Nested para Organização

```java
@DisplayName("ProcessoService")
class ProcessoServiceTest {
    
    @Nested
    @DisplayName("Criação de processo")
    class CriacaoProcesso {
        
        @Test
        @DisplayName("Deve criar processo quando dados válidos")
        void deveCriarProcessoQuandoDadosValidos() { }
        
        @Test
        @DisplayName("Deve lançar exceção quando dados inválidos")
        void deveLancarExcecaoQuandoDadosInvalidos() { }
    }
    
    @Nested
    @DisplayName("Atualização de processo")
    class AtualizacaoProcesso {
        // ...
    }
}
```

## Objetivo
Garantir consistência de nomenclatura e estrutura para facilitar a leitura e manutenção.

## Tarefas
- Renomear métodos de teste para o padrão `deve{Acao}Quando{Condicao}`.
- Adicionar `@DisplayName` em todos os testes de controller, service e integração.
- Introduzir `@Nested` em classes de teste grandes (>10 métodos) para agrupar cenários.
- Garantir estrutura AAA explícita com comentários.
- Padronizar em lotes por módulo (ex: todos os testes de `processo`, depois `subprocesso`, etc).

## Comandos de Verificação

### Listar testes sem @DisplayName
```bash
# Testes que não têm @DisplayName
find backend/src/test -name "*Test.java" -exec grep -L "@DisplayName" {} \;
```

### Contar progresso de padronização
```bash
# Total de @DisplayName
grep -R "@DisplayName" backend/src/test --include="*.java" | wc -l

# Total de @Nested
grep -R "@Nested" backend/src/test --include="*.java" | wc -l

# Total de arquivos de teste
find backend/src/test -name "*Test.java" | wc -l
```

### Identificar métodos com padrão antigo
```bash
# Métodos que começam com "test"
grep -R "void test[A-Z]" backend/src/test --include="*.java"
```

### Verificar estrutura AAA
```bash
# Testes que já usam comentários AAA
grep -R "// Arrange\|// Act\|// Assert" backend/src/test --include="*.java" | wc -l
```

### Executar testes após padronização
```bash
./gradlew :backend:test
```

## Estratégia de Execução

### Abordagem por Módulo
1. **Sprint 4.1**: Padronizar módulo `processo` (orquestrador)
2. **Sprint 4.2**: Padronizar módulo `subprocesso` (workflow)
3. **Sprint 4.3**: Padronizar módulos `mapa` e `atividade`
4. **Sprint 4.4**: Padronizar módulos `comum`, `util`, `sgrh`, `unidade`
5. **Sprint 4.5**: Padronizar testes de integração

### Checklist por Classe de Teste
- [ ] Métodos renomeados para `deve{Acao}Quando{Condicao}`
- [ ] `@DisplayName` adicionado em cada `@Test`
- [ ] Classes grandes organizadas com `@Nested`
- [ ] Comentários AAA explícitos
- [ ] Testes executam com sucesso

## Critérios de Aceite
- `./gradlew :backend:test` passa sem erros.
- Diferença de estilo reduzida (padronização aplicada em massa ou em lotes significativos).
- Meta: >90% dos testes com `@DisplayName` e nomenclatura correta.
- Classes com >10 testes organizadas com `@Nested`.

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
