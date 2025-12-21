# Sprint 5: Desacoplar Testes CDU do Seed Global

**Baseado em:** `analise-junit-nova.md` - Onda 5

## Contexto do Projeto SGC

### Estrutura Atual de Testes de Integração
- **Localização**: `backend/src/test/java/sgc/integracao/`
- **Framework**: `@SpringBootTest` com banco H2 em memória
- **Seed Global**: Arquivo `backend/src/test/resources/data.sql` (se existir)
- **Problema**: Testes dependem de IDs hardcoded e dados pré-carregados

### Casos de Uso (CDU) no SGC
O projeto possui testes de integração para casos de uso:
- **CDU02**: Criar e gerenciar processos
- **CDU06**: Workflow de subprocesso
- Outros CDUs relacionados ao fluxo de competências

### O Problema do Seed Global
Quando testes dependem de `data.sql`:
- **Acoplamento**: Testes quebram se seed mudar
- **Não-isolamento**: Testes não podem rodar em paralelo
- **Fragilidade**: IDs hardcoded como `99L` podem não existir
- **Difícil debug**: Precisa entender seed para entender teste

### Estratégias de Solução

#### Estratégia A: @Sql por Classe/Teste
Criar datasets mínimos e explícitos:
```java
@SpringBootTest
@Sql("/sql/cdu02-setup.sql")  // Dataset específico para este teste
class CDU02IntegrationTest {
    
    @Test
    void deveExecutarFluxoCompleto() {
        // Dados já carregados pelo @Sql
        Long processoId = 1L; // ID conhecido do script SQL
        // ...
    }
}
```

#### Estratégia B: Setup Programático (RECOMENDADA)
Criar entidades via repositórios no `@BeforeEach`:
```java
@SpringBootTest
@Transactional
class CDU02IntegrationTest {
    
    @Autowired ProcessoRepository processoRepo;
    @Autowired UsuarioRepository usuarioRepo;
    
    private Usuario usuario;
    private Processo processo;
    
    @BeforeEach
    void setUp() {
        // Criação explícita e isolada
        usuario = usuarioRepo.save(UsuarioFixture.usuarioPadrao());
        processo = processoRepo.save(ProcessoFixture.novo(usuario));
    }
    
    @Test
    @DisplayName("Deve executar fluxo completo do CDU02")
    void deveExecutarFluxoCompletoCDU02() {
        // Arrange
        var dados = criarDadosParaTeste();
        
        // Act
        var resultado = service.executar(processo.getCodigo(), dados);
        
        // Assert
        assertThat(resultado).isNotNull();
    }
}
```

### BaseIntegrationTest
O projeto possui `BaseIntegrationTest` que pode ser estendido:
- Fornece configuração comum para testes de integração
- Pode ser usado para setup compartilhado
- Considere adicionar métodos auxiliares para criação de dados

## Objetivo
Tornar os testes de integração isolados, robustos e paralelizáveis, removendo a dependência de dados globais (`data.sql`).

## Tarefas
- Escolher e documentar a estratégia principal (A ou B).
- **Recomendação**: Usar Estratégia B (setup programático) por ser mais flexível.
- Identificar todos os testes CDU em `backend/src/test/java/sgc/integracao/`.
- Remover dependências de IDs hardcoded do seed global.
- Refatorar cada teste para criar seus próprios dados de teste.
- Usar fixtures criadas no Sprint 3 para setup.
- Considerar criar métodos auxiliares em `BaseIntegrationTest`.

## Comandos de Verificação

### Listar testes de integração
```bash
find backend/src/test/java/sgc/integracao -name "*Test.java"
```

### Identificar IDs hardcoded
```bash
grep -R "99L\|1L\|2L" backend/src/test/java/sgc/integracao --include="*.java"
```

### Verificar uso de @Sql
```bash
grep -R "@Sql" backend/src/test --include="*.java"
```

### Verificar arquivos SQL de seed
```bash
find backend/src/test/resources -name "*.sql"
```

### Executar testes de integração
```bash
./gradlew :backend:test --tests "sgc.integracao.*"
```

### Executar todos os testes
```bash
./gradlew :backend:test
```

## Exemplo de Refatoração

### Antes (Acoplado ao Seed)
```java
@SpringBootTest
class CDU02IntegrationTest {
    
    @Test
    void teste() {
        // Depende de ID do seed global
        Processo processo = processoRepo.findById(99L).get();
        // ...
    }
}
```

### Depois (Isolado)
```java
@SpringBootTest
@Transactional
class CDU02IntegrationTest {
    
    @Autowired ProcessoRepository processoRepo;
    @Autowired UsuarioRepository usuarioRepo;
    
    private Usuario usuario;
    private Processo processo;
    
    @BeforeEach
    void setUp() {
        // Setup explícito e isolado
        usuario = usuarioRepo.save(UsuarioFixture.usuarioPadrao());
        processo = processoRepo.save(ProcessoFixture.novo(usuario));
    }
    
    @Test
    @DisplayName("Deve executar fluxo completo do CDU02")
    void deveExecutarFluxoCompletoCDU02() {
        // Arrange
        Long processoId = processo.getCodigo(); // ID dinâmico
        
        // Act & Assert
        // ...
    }
}
```

## Critérios de Aceite
- Nenhum teste de CDU depende de identificadores globais hardcoded sem que o dado seja criado explicitamente no próprio teste ou setup da classe.
- `./gradlew :backend:test` passa sem erros.
- Testes podem rodar em qualquer ordem (não há dependência de ordem de execução).
- Cada teste é autossuficiente e cria seus próprios dados.

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
