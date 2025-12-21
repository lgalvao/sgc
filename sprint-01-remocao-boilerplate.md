# Sprint 1: Remover Testes Boilerplate (DTO/Model)

**Baseado em:** `analise-junit-nova.md` - Onda 1

## Contexto do Projeto SGC

### Estrutura de Testes
- **Localização:** `backend/src/test/java/sgc/`
- **Total de testes:** 113 arquivos
- **Candidatos a remoção:** Testes em pacotes `dto/` e `model/` de cada módulo

### Exemplos de Arquivos Candidatos
Procurar por testes como:
- `*Dto*Test.java` - testes de DTOs sem validação
- `*Mapper*Test.java` - testes de mappers sem lógica customizada  
- `Model*Test.java` - testes de entidades JPA sem invariantes

### Identificando Boilerplate
Testes que apenas verificam getters/setters gerados pelo Lombok:
```java
@Test
void testGettersSetters() {
    dto.setNome("teste");
    assertEquals("teste", dto.getNome());
}
```

Testes que verificam builders sem validação:
```java
@Test
void testBuilder() {
    var dto = MinhaDto.builder().nome("teste").build();
    assertNotNull(dto);
}
```

## Objetivo
Reduzir ruído e cobertura artificial removendo testes de getters/setters e builders que não agregam valor.

## Tarefas
- Identificar testes de DTOs e entidades que apenas verificam Lombok (getters/setters/builders).
- Remover testes sem regra de negócio ou validação.
- Manter apenas testes onde exista:
  - Validação customizada (anotações `@Valid`, lógica no setter)
  - Serialização/deserialização JSON customizada
  - Invariantes de domínio
  - Lógica de construção complexa

## Comandos de Verificação

### Listar testes de DTOs
```bash
find backend/src/test -path "*/dto/*Test.java" -o -path "*/model/*Test.java"
```

### Executar testes após remoção
```bash
./gradlew :backend:test
```

### Verificar redução de arquivos
```bash
# Antes
find backend/src/test -name "*Test.java" | wc -l
# Após remoção
find backend/src/test -name "*Test.java" | wc -l
```

## Critérios de Aceite
- `./gradlew :backend:test` passa sem erros.
- Redução objetiva de arquivos/linhas de testes inúteis (métricas antes/depois documentadas).
- Nenhum teste com validação real foi removido.

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
