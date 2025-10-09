# Guia de Testes para o Sistema SGC (Sistema de Gestão de Conhecimentos)

## Índice
1. [Introdução](#introdução)
2. [Ferramentas e Frameworks Utilizados](#ferramentas-e-frameworks-utilizados)
3. [Estrutura de Testes](#estrutura-de-testes)
4. [Práticas Recomendadas](#práticas-recomendadas)
5. [Tipos de Testes](#tipos-de-testes)
6. [Estratégias de Mocking](#estratégias-de-mocking)
7. [Padrões de Teste por Camada](#padrões-de-teste-por-camada)
8. [Dicas e Truques](#dicas-e-truques)

## Introdução

Este guia fornece diretrizes e melhores práticas para a criação de testes no Sistema de Gestão de Conhecimentos (SGC). O SGC é uma aplicação Java Spring Boot com arquitetura multi-camada, que exige uma abordagem cuidadosa para garantir cobertura de testes eficaz e manutenível.

## Ferramentas e Frameworks Utilizados

- **JUnit 5**: Framework principal para testes unitários e de integração
- **Mockito**: Framework para criação de mocks e stubs
- **Spring Test**: Extensões do Spring para testes integrados
- **MockMvc**: Para testes de controllers REST
- **Gradle**: Sistema de build que orquestra os testes
- **MapStruct**: Para mapeamento de objetos (com testes específicos)
- **Lombok**: Redução de código boilerplate (afeta testes)

## Estrutura de Testes

### Organização de Pacotes
```
backend/
src/
├── test/
│   └── java/
│       └── sgc/
│           ├── subprocesso/
│           │   ├── SubprocessoServiceTest.java
│           │   ├── SubprocessoControleTest.java
│           │   ├── dto/
│           │   │   └── SubprocessoDTOTest.java
│           │   └── modelo/
│           │       └── ModeloTest.java
│           ├── atividade/
│           ├── mapa/
│           └── ...
```

### Convenções de Nomenclatura
- Classes de teste: `{NomeClasse}Test.java`
- Métodos de teste: `ação_condição_resultado()` (ex: `obterDetalhes_SubprocessoNaoEncontrado_LancaErro`)
- Pacotes de teste espelham a estrutura do pacote principal

## Práticas Recomendadas

### 1. Princípio AAA (Arrange, Act, Assert)
```java
@Test
void exemploTeste() {
    // Arrange (Organizar)
    SubprocessoService service = new SubprocessoService(...);
    when(repositorio.findById(1L)).thenReturn(Optional.of(subprocesso));
    
    // Act (Agir)
    SubprocessoDto resultado = service.obterDetalhes(1L, "ADMIN", null);
    
    // Assert (Validar)
    assertNotNull(resultado);
    verify(repositorio).findById(1L);
}
```

## Padrões de Teste por Camada

### 1. Testes para Serviços (Service Layer)
- Foco na lógica de negócios
- Teste as regras de validação e transições de estado
- Verifique chamadas a repositórios e outros serviços
- Teste tratamento de exceções de domínio
- Exemplo para o SubprocessoService:

```java
@Test
void disponibilizarCadastro_SubprocessoComMapa_RealizaDisponibilizacao() {
    // Given (dado um subprocesso válido)
    Unidade unidade = new Unidade();
    unidade.setCodigo(1L);
    unidade.setSigla("SIGLA");
    
    Unidade unidadeSuperior = new Unidade();
    unidadeSuperior.setCodigo(2L);
    unidade.setUnidadeSuperior(unidadeSuperior);
    
    Mapa mapa = new Mapa();
    mapa.setCodigo(10L);
    
    Subprocesso subprocesso = new Subprocesso();
    subprocesso.setCodigo(1L);
    subprocesso.setUnidade(unidade);
    subprocesso.setMapa(mapa);
    subprocesso.setSituacaoId("CADASTRO_EM_ELABORACAO");
    
    when(repositorioSubprocesso.findById(1L)).thenReturn(Optional.of(subprocesso));

    // When (quando o metodo é chamado)
    service.disponibilizarCadastro(1L);

    // Then (então verifique o comportamento esperado)
    assertEquals("CADASTRO_DISPONIBILIZADO", subprocesso.getSituacaoId());
    verify(repositorioSubprocesso).save(subprocesso);
    verify(repositorioMovimentacao).save(any(Movimentacao.class));
}
```

### 2. Testes para Controladores (Controller Layer)
- Teste endpoints REST completos
- Verifique códigos de status HTTP
- Valide formato e conteúdo da resposta
- Teste validação de entrada e tratamento de erros
- Exemplo para o SubprocessoControle:

```java
@Test
void devolverCadastro_DadosValidos_RealizaDevolucao() throws Exception {
    // Given
    DevolverCadastroReq request = new DevolverCadastroReq("Motivo", "Observações");
    SubprocessoDto resultado = new SubprocessoDto();
    resultado.setCodigo(1L);
    
    when(service.devolverCadastro(eq(1L), eq("Motivo"), eq("Observações"), any(String.class)))
            .thenReturn(resultado);

    // When & Then
    mockMvc.perform(post("/api/subprocessos/1/devolver-cadastro")
            .contentType(MediaType.APPLICATION_JSON)
            .content(objectMapper.writeValueAsString(request)))
            .andExpect(status().isOk())
            .andExpect(content().contentType(MediaType.APPLICATION_JSON));

    verify(service).devolverCadastro(eq(1L), eq("Motivo"), eq("Observações"), any(String.class));
}
```

### 3. Testes para DTOs e Mappers (Data Layer)
- Teste construtores e métodos de acesso
- Valide mapeamentos entre entidades e DTOs
- Verifique conversões de dados
- Exemplo:

```java
@Test
void subprocessoMapper_MapsEntityToDtoCorrectly() {
    // Given
    Subprocesso entity = new Subprocesso();
    entity.setCodigo(1L);
    entity.setSituacaoId("CADASTRO_DISPONIBILIZADO");
    
    // When
    SubprocessoDto dto = mapper.toDTO(entity);
    
    // Then
    assertEquals(1L, dto.getCodigo());
    assertEquals("CADASTRO_DISPONIBILIZADO", dto.getSituacaoId());
}
```

### 4. Testes de Entidades e Repositórios
- Teste métodos de negócio da entidade
- Teste validações e construtores
- Para repositórios, geralmente use testes de integração com @DataJpaTest
- Exemplo:

```java
@DataJpaTest
class SubprocessoRepoTest {
    
    @Autowired
    private TestEntityManager entityManager;
    
    @Autowired
    private SubprocessoRepo repositorio;
    
    @Test
    void findByProcessoCodigo_ComSubprocessos_RetornaLista() {
        Processo processo = new Processo();
        processo.setDescricao("Processo Teste");
        entityManager.persist(processo);
        
        Subprocesso subprocesso = new Subprocesso(processo, ...);
        entityManager.persist(subprocesso);
        entityManager.flush();
        
        List<Subprocesso> resultado = repositorio.findByProcessoCodigo(processo.getCodigo());
        
        assertThat(resultado).hasSize(1);
        assertThat(resultado.get(0).getProcesso().getCodigo()).isEqualTo(processo.getCodigo());
    }
}
```

## Estratégias de Mocking

### Quando Usar Mocks
- Classes de repositório (DAOs) - para isolar a lógica de negócio
- Serviços externos (notificações, e-mails, APIs) - para evitar dependências externas
- Componentes pesados ou com efeitos colaterais (envio de e-mail, notificações)
- Classes de infraestrutura (datasource, transações)

### Boas Práticas com Mockito
```java
// 1. Injetar mocks com @Mock e @InjectMocks
@ExtendWith(MockitoExtension.class)
class SubprocessoServiceTest {
    
    @Mock
    private SubprocessoRepo repositorio;

    @Mock
    private MovimentacaoRepo movimentacaoRepo;

    @InjectMocks
    private SubprocessoService service;

    // 2. Evitar stubbing desnecessário
    when(repositorio.findById(1L)).thenReturn(Optional.of(subprocesso));
    // Não faça stubbing se o método não for chamado no teste

    // 3. Verificar interações
    verify(repositorio).save(subprocesso);
}

// 4. Usar ArgumentCaptor para verificar parâmetros
@Test
void metodo_RealizaOperacao_ComParametrosCorretos() {
    ArgumentCaptor<Movimentacao> captor = ArgumentCaptor.forClass(Movimentacao.class);
    
    service.disponibilizarCadastro(1L);
    
    verify(movimentacaoRepo).save(captor.capture());
    Movimentacao movimentacao = captor.getValue();
    assertEquals("Disponibilização do cadastro", movimentacao.getDescricao());
}

// 5. Teste de void methods com doThrow/doNothing
@Test
void metodo_LancaExcecao_UsandoDoThrow() {
    doThrow(new ErroEntidadeNaoEncontrada("Não encontrado"))
        .when(repositorio).deleteById(999L);
        
    assertThrows(ErroEntidadeNaoEncontrada.class, 
        () -> service.excluir(999L));
}
```

### Mocks Lenientes
```java
// Para evitar UnnecessaryStubbingException quando necessário
@BeforeEach
void setUp() {
    // Pode ser configurado globalmente ou por teste
    MockitoAnnotations.openMocks(this);
}
```

### Mocking de Componentes Complexos
- Para services com múltiplas dependências, priorize testar o comportamento
- Verifique sequência de chamadas apenas quando crítica
- Evite "testes de boneco de pano" (testes que apenas verificam chamadas sem lógica real)

```java
@Test
void devolverCadastro_FluxoCompleto_RealizaTodasOperacoes() {
    // Given
    Subprocesso subprocesso = criarSubprocessoComUnidade();
    when(repositorio.findById(1L)).thenReturn(Optional.of(subprocesso));
    
    // When
    service.devolverCadastro(1L, "Motivo", "Obs", "Usuario");

    // Then
    assertEquals("CADASTRO_EM_ELABORACAO", subprocesso.getSituacaoId());
    verify(repositorio).save(subprocesso);
    verify(analiseRepo).save(any(AnaliseCadastro.class));
    verify(movimentacaoRepo).save(any(Movimentacao.class));
    verify(notificacaoService).enviarEmail(any(), any(), any());
}
```

## Padrões de Teste por Camada

### 1. Serviços (Service Layer)
```java
@ExtendWith(MockitoExtension.class)
class SubprocessoServiceTest {
    
    @Mock
    private SubprocessoRepo repositorio;
    
    @InjectMocks
    private SubprocessoService service;
    
    @Test
    void obterDetalhes_SubprocessoEncontrado_RetornaDetalhes() {
        // Teste de lógica de negócio
        // Verifique todas as regras de validação
        // Verifique chamadas a repositórios e outros serviços
    }
}
```

### 2. Controladores (Controller Layer)
```java
@ExtendWith(MockitoExtension.class)
class SubprocessoControleTest {
    
    private MockMvc mockMvc;
    
    @Mock
    private SubprocessoService service;
    
    @BeforeEach
    void setUp() {
        SubprocessoControle controle = new SubprocessoControle(
            service, // injetar dependências
            // outros serviços
        );
        mockMvc = MockMvcBuilders.standaloneSetup(controle).build();
    }
    
    @Test
    void obterPorId_SubprocessoEncontrado_Retorna200() throws Exception {
        mockMvc.perform(get("/api/subprocessos/1"))
               .andExpect(status().isOk())
               .andExpect(jsonPath("$.situacao").value("EM_EDICAO"));
    }
}
```

### 3. DTOs e Mappers
- Testes simples para construtores e getters/setters
- Testes para mappers do MapStruct
```java
@Test
void subprocessoMapper_MapsEntityToDtoCorrectly() {
    SubprocessoMapper mapper = Mappers.getMapper(SubprocessoMapper.class);
    // Teste do mapeamento
}
```

### 4. Entidades e Repositórios
- Testes para construtores e métodos de negócio da entidade
- Testes de repositório normalmente em testes de integração

## Dicas e Truques

### 1. Organização de Testes Complexos
```java
@Test
void fluxoCompleto_WorkflowSubprocesso() {
    // Setup inicial
    Subprocesso subprocesso = criarSubprocesso();
    
    // Execução do fluxo
    service.disponibilizarCadastro(subprocesso.getId());
    service.aceitarCadastro(subprocesso.getId(), "obs", "usuario");
    service.homologarCadastro(subprocesso.getId(), "obs", "usuario");
    
    // Verificações finais
    assertEquals("CADASTRO_HOMOLOGADO", subprocesso.getSituacaoId());
    verify(repositorio, times(3)).save(any());
}
```

### 2. Tratamento de Exceções
```java
@Test
void metodo_EntradaInvalida_LancaExcecao() {
    ErroDominioNaoEncontrado exception = assertThrows(
        ErroDominioNaoEncontrado.class,
        () -> service.metodoInvalido(999L)
    );
    assertEquals("Mensagem esperada", exception.getMessage());
}
```

### 3. Testes de Validação
```java
@Test
void criarSubprocesso_DadosInvalidos_RetornaErroValidacao() throws Exception {
    SubprocessoDto dto = new SubprocessoDto();
    // Configurar dados inválidos
    
    mockMvc.perform(post("/api/subprocessos")
            .contentType(MediaType.APPLICATION_JSON)
            .content(objectMapper.writeValueAsString(dto)))
            .andExpect(status().isBadRequest());
}
```

### 4. Uso de Argument Captors
```java
@Test
void metodo_RealizaOperacao_ComParametrosCorretos() {
    ArgumentCaptor<Subprocesso> captor = ArgumentCaptor.forClass(Subprocesso.class);
    
    service.metodoDeTeste(1L);
    
    verify(repositorio).save(captor.capture());
    Subprocesso capturado = captor.getValue();
    assertEquals("SITUACAO_ESPERADA", capturado.getSituacaoId());
}
```

### 5. Configuração de Dados para Testes
```java
// Métodos auxiliares para criar dados de teste
private Subprocesso criarSubprocessoAtivo() {
    Subprocesso sp = new Subprocesso();
    sp.setSituacaoId("CADASTRO_EM_ELABORACAO");
    sp.setUnidade(criarUnidade());
    sp.setProcesso(criarProcesso());
    return sp;
}
```

### 6. Testes de Camada de Segurança
- Para endpoints que exigem autenticação/autorização
- Testar diferentes perfis e permissões
- Verificar restrições de acesso

### 7. Organização de Testes para Fluxos de Trabalho
- Teste cada etapa do fluxo individualmente
- Teste o fluxo completo final
- Verifique restrições de estado (não pode aceitar antes de disponibilizar, etc.)

### 8. Execução Eficiente de Testes
- Use perfis de teste (test profile)
- Evite testes lentos na execução padrão
- Separe testes de integração de testes unitários
- Use @ActiveProfiles("test") quando necessário

## Ferramentas e Convenções de Teste

### Frameworks e Bibliotecas
- **JUnit 5**: Framework padrão para testes, com suporte a @ParameterizedTest, @Nested, etc.
- **Mockito**: Framework para mocking, com suporte a @Mock, @Spy, ArgumentCaptor
- **AssertJ**: Biblioteca para assertions fluídas e legíveis
- **JsonPath**: Para validação de respostas JSON em testes de controller
- **MapStruct**: Geração automática de mappers (testes verificam corretude)

### Convenções de Código de Teste

#### 1. Organização de Imports
```java
// Organize imports em ordem lógica:
// 1. Pacotes do JDK
// 2. Bibliotecas externas
// 3. Pacotes do projeto
// 4. Static imports no final

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;
```

#### 2. Estrutura de Teste com Setup e Teardown
```java
@ExtendWith(MockitoExtension.class)
class ExemploServiceTest {
    
    @Mock
    private ExemploRepo repositorio;
    
    @InjectMocks
    private ExemploService service;
    
    private ObjectMapper objectMapper;
    
    @BeforeEach
    void setUp() {
        objectMapper = new ObjectMapper();
        // Configurações adicionais se necessário
    }
    
    @Test
    void testeExemplo() {
        // Seu teste aqui
    }
}
```

#### 3. Validação de Testes
- Use assertions específicas quando possível (assertEquals, assertTrue, etc.)
- Prefira assertThat do AssertJ para mensagens de erro mais claras
- Evite testes que passam silenciosamente sem verificações

#### 4. Cobertura de Testes
- Almeje alta cobertura de código para lógica de negócios crítica
- Cobertura de 80%+ é um bom objetivo para módulos críticos
- Mais importante que a porcentagem é testar fluxos críticos e casos de borda
- Use ferramentas como JaCoCo para medir cobertura

#### 5. Perfis e Configurações de Teste
- Use application-test.properties para configurações específicas de teste
- Configure banco de dados em memória para testes de integração
- Desative funcionalidades não essenciais nos testes (como envio de e-mail)

```properties
# application-test.properties
spring.datasource.url=jdbc:h2:mem:testdb
spring.jpa.hibernate.ddl-auto=create-drop
logging.level.org.springframework.web=DEBUG
```

#### 6. Comandos Gradle para Testes
```bash
# Executar todos os testes
./gradlew test

# Executar um teste específico
./gradlew test --tests "NomeClasseTest"

# Executar testes de uma camada específica
./gradlew test --tests "*servico*"

# Gerar relatório de cobertura
./gradlew test jacocoTestReport
```

## Conclusão

Este guia fornece uma base sólida para a criação de testes de alta qualidade no sistema SGC. A aplicação consistente dessas práticas garantirá:
- Maior confiabilidade no código
- Fácil manutenção e refatoração
- Detecção precoce de bugs
- Maior confiança nas entregas
- Documentação executável do comportamento esperado do sistema

Lembre-se de revisar e atualizar os testes conforme a evolução do sistema e as lições aprendidas em cada ciclo de desenvolvimento.

## Recursos Adicionais
- [Documentação Oficial do JUnit 5](https://junit.org/junit5/docs/current/user-guide/)
- [Documentação do Mockito](https://site.mockito.org/)
- [Guia do Spring Testing](https://docs.spring.io/spring-framework/docs/current/reference/html/testing.html)
- [Boas Práticas de Teste com Spring Boot](https://spring.io/guides/gs/testing-web/)