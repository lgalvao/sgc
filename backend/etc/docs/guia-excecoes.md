# Guia de Decisão para Exceções - SGC

Este documento orienta desenvolvedores na escolha da exceção apropriada para cada situação.

## 1. Fluxograma de Decisão

```
┌─────────────────────────────────────────────────────────────┐
│ Um erro ocorreu. Qual exceção lançar?                       │
└─────────────────────────────────────────────────────────────┘
                            │
                            ▼
        ┌───────────────────────────────────────────┐
        │ É um erro que NUNCA deveria ocorrer se o  │
        │ sistema está configurado e funcionando    │
        │ corretamente?                             │
        └───────────────────────────────────────────┘
                    │                   │
              SIM   │                   │   NÃO
                    ▼                   ▼
        ┌─────────────────────┐   ┌─────────────────────┐
        │ ErroInterno         │   │ Erro de Negócio     │
        │ (e subclasses)      │   │ (continuar)         │
        └─────────────────────┘   └─────────────────────┘
                                            │
                                            ▼
                    ┌───────────────────────────────────────┐
                    │ É um recurso/entidade não encontrado? │
                    └───────────────────────────────────────┘
                                    │           │
                              SIM   │           │   NÃO
                                    ▼           ▼
                    ┌──────────────────────┐   │
                    │ErroEntidadeNao       │   │
                    │Encontrada (404)      │   │
                    └──────────────────────┘   │
                                               ▼
                    ┌───────────────────────────────────────┐
                    │ É uma violação de permissão/acesso?   │
                    └───────────────────────────────────────┘
                                    │           │
                              SIM   │           │   NÃO
                                    ▼           ▼
                    ┌──────────────────────┐   │
                    │ErroAccessoNegado     │   │
                    │(403)                 │   │
                    └──────────────────────┘   │
                                               ▼
                    ┌───────────────────────────────────────┐
                    │ É uma validação de dados de entrada?  │
                    └───────────────────────────────────────┘
                                    │           │
                              SIM   │           │   NÃO
                                    ▼           ▼
                    ┌──────────────────────┐   │
                    │ErroValidacao (422)   │   │
                    └──────────────────────┘   │
                                               ▼
                    ┌───────────────────────────────────────┐
                    │ É uma operação em estado/situação     │
                    │ inválido de workflow?                 │
                    └───────────────────────────────────────┘
                                    │           │
                              SIM   │           │   NÃO
                                    ▼           ▼
                    ┌──────────────────────┐   │
                    │ErroXxxEmSituacao     │   │
                    │Invalida (422)        │   │
                    │- ErroProcessoEm...   │   │
                    │- ErroMapaEm...       │   │
                    └──────────────────────┘   │
                                               ▼
                    ┌───────────────────────────────────────┐
                    │ É uma violação de regra de negócio?   │
                    └───────────────────────────────────────┘
                                    │
                              SIM   │
                                    ▼
                    ┌──────────────────────┐
                    │ErroProcesso ou       │
                    │exceção específica    │
                    │do domínio (409)      │
                    └──────────────────────┘
```

## 2. Erros Internos vs Erros de Negócio

### 2.1. Erros Internos (ErroInterno e subclasses)

**Características**:

- Indicam bugs, configuração incorreta ou violação de invariantes
- NUNCA deveriam ocorrer se sistema está funcionando corretamente
- Retornam HTTP 500 (Internal Server Error)
- Logados como ERROR com stack trace completo
- Mensagem genérica ao usuário (não expor detalhes internos)

**Quando usar**:

```java
✓ Configuração ausente ou inválida
✓ Dados corrompidos (FK inválida, entidade obrigatória ausente)
✓ Estado "impossível" se UI funciona corretamente
✓ Switch/case sem match em enum completo
✓ Violação de invariante do sistema
```

**Quando NÃO usar**:

```java
✗ Validação de entrada do usuário
✗ Recurso não encontrado
✗ Operação em estado incorreto (pode acontecer com múltiplos usuários)
✗ Violação de permissão
```

**Subclasses**:

| Classe                  | Quando Usar                             | Exemplo                           |
|-------------------------|-----------------------------------------|-----------------------------------|
| `ErroConfiguracao`      | Problemas de configuração               | JWT secret ausente ou muito curto |
| `ErroInvarianteViolada` | Violação de invariante do sistema       | FK obrigatória não encontrada     |
| `ErroEstadoImpossivel`  | Estado que UI impede mas backend valida | Tipo de enum desconhecido         |

**Exemplo**:

```java
// ✓ CORRETO - Erro interno
if (secret.length() < 32) {
    throw new ErroConfiguracao("JWT secret deve ter no mínimo 32 caracteres");
}

// ✓ CORRETO - Invariante violada
Unidade superior = unidade.getUnidadeSuperior();
if (superior == null && unidade.getTipo() == OPERACIONAL) {
    // Unidade operacional DEVE ter superior. Isso indica dados corrompidos.
    throw new ErroInvarianteViolada(
        "Unidade operacional sem unidade superior: " + unidade.getSigla()
    );
}

// ✗ INCORRETO - Este é um erro de negócio!
if (processo.getSituacao() != CRIADO) {
    throw new ErroEstadoImpossivel("Processo não está em estado CRIADO");
    // Use ErroProcessoEmSituacaoInvalida - pode ocorrer legitimamente
}
```

### 2.2. Erros de Negócio

**Características**:

- São esperados durante uso normal do sistema
- Podem ocorrer legitimamente (múltiplos usuários, condições de corrida)
- Retornam status HTTP 4xx apropriado
- Logados como WARN (não ERROR)
- Mensagem clara e útil ao usuário

## 3. Catálogo de Exceções de Negócio

### 3.1. ErroEntidadeNaoEncontrada (404 Not Found)

**Quando usar**: Recurso solicitado não existe no sistema

**Exemplos**:

```java
// ✓ CORRETO
Processo processo = processoRepo.findById(codigo)
    .orElseThrow(() -> new ErroEntidadeNaoEncontrada("Processo", codigo));

// ✓ CORRETO - com mensagem customizada
Unidade unidade = unidadeRepo.findBySigla(sigla)
    .orElseThrow(() -> new ErroEntidadeNaoEncontrada(
        "Unidade com sigla '" + sigla + "' não encontrada"
    ));
```

### 3.2. ErroValidacao (422 Unprocessable Content)

**Quando usar**: Validação de dados de entrada ou regras de negócio

**Características**:

- Requisição sintaticamente correta mas semanticamente inválida
- Pode incluir detalhes sobre campos específicos
- Usuário pode corrigir e reenviar

**Exemplos**:

```java
// ✓ CORRETO - Validação simples
if (atividades.isEmpty()) {
    throw new ErroValidacao("O subprocesso deve ter pelo menos uma atividade");
}

// ✓ CORRETO - Com detalhes
Map<String, String> detalhes = Map.of(
    "campo", "email",
    "valor", email,
    "motivo", "Formato inválido"
);
throw new ErroValidacao("Dados de entrada inválidos", detalhes);
```

### 3.3. ErroAccessoNegado (403 Forbidden)

**Quando usar**: Usuário autenticado mas sem permissão para a ação

**Exemplo**:

```java
// ✓ CORRETO
if (!usuario.getTituloEleitoral().equals(tituloTitular)) {
    throw new ErroAccessoNegado(
        "Apenas o titular da unidade pode executar esta ação"
    );
}
```

### 3.4. ErroXxxEmSituacaoInvalida (422 Unprocessable Content)

**Quando usar**: Operação em estado de workflow incorreto

**Importante**: Este é um erro de negócio ESPERADO (pode ocorrer com múltiplos usuários ou condições de corrida)

**Classes disponíveis**:

- `ErroProcessoEmSituacaoInvalida`
- `ErroMapaEmSituacaoInvalida`
- `ErroAtividadesEmSituacaoInvalida`

**Exemplos**:

```java
// ✓ CORRETO
if (processo.getSituacao() != SituacaoProcesso.CRIADO) {
    throw new ErroProcessoEmSituacaoInvalida(
        "Processo só pode ser iniciado se estiver em estado CRIADO"
    );
}

// ✓ CORRETO
if (subprocesso.getSituacao() != REVISAO_CADASTRO_DISPONIBILIZADA) {
    throw new ErroProcessoEmSituacaoInvalida(
        "Ação de homologar só pode ser executada em revisões disponibilizadas"
    );
}
```

### 3.5. ErroProcesso e Exceções Específicas de Domínio (409 Conflict)

**Quando usar**: Violação de regras de negócio não cobertas acima

**Exemplos**:

- `ErroProcesso` - regras gerais de processo
- `ErroUnidadesNaoDefinidas` - processo sem unidades
- `ErroMapaNaoAssociado` - mapa não vinculado
- etc.

## 4. Casos Especiais

### 4.1. Unidade Superior Não Encontrada

**Cenário**: Código busca `unidade.getUnidadeSuperior()` e retorna `null`

**Decisão**:

```java
// Se superior é OBRIGATÓRIO pela estrutura do sistema:
if (unidadeSuperior == null) {
    throw new ErroInvarianteViolada(
        "Unidade superior não encontrada - possível corrupção de dados"
    );
}

// Se superior é OPCIONAL (ex: unidade raiz):
if (unidadeSuperior == null) {
    // Tratamento normal, não é erro
}
```

### 4.2. Múltiplos Usuários e Condições de Corrida

**Cenário**: Usuário A inicia ação quando processo está em estado X. Antes de completar, usuário B muda para estado Y.
Ação de A falha.

**Decisão**:

```java
// ✓ CORRETO - Erro de negócio
if (processo.getSituacao() != estadoEsperado) {
    throw new ErroProcessoEmSituacaoInvalida(
        "O estado do processo foi alterado. Por favor, recarregue a página."
    );
}

// ✗ INCORRETO - Não é erro interno!
if (processo.getSituacao() != estadoEsperado) {
    throw new ErroEstadoImpossivel("Estado inesperado");
}
```

## 5. Checklist de Decisão

Antes de lançar uma exceção, pergunte-se:

1. **Este erro pode ocorrer durante uso normal do sistema?**
    - SIM → Erro de Negócio
    - NÃO → Erro Interno

2. **O usuário pode fazer algo para corrigir?**
    - SIM → Erro de Negócio (validação, permissão, estado)
    - NÃO → Provavelmente Erro Interno

3. **Outro usuário ou processo poderia causar este erro?**
    - SIM → Erro de Negócio
    - NÃO → Considere Erro Interno

4. **Este erro indica um bug no código?**
    - SIM → Erro Interno
    - NÃO → Erro de Negócio

## 6. Migração de Código Legado

Se você encontrar:

```java
// ❌ PADRÃO ANTIGO
throw new IllegalStateException("...");
throw new IllegalArgumentException("...");
```

**Refatore para**:

```java
// ✅ NOVO PADRÃO

// Se for erro interno:
throw new ErroConfiguracao("...");          // ou
throw new ErroInvarianteViolada("...");     // ou
throw new ErroEstadoImpossivel("...");

// Se for erro de negócio:
throw new ErroValidacao("...");                    // ou
throw new ErroProcessoEmSituacaoInvalida("...");   // ou
throw new ErroEntidadeNaoEncontrada("...");        // ou
// outra exceção apropriada de negócio
```

## 7. Exemplos Completos

### Exemplo 1: Validação de Workflow (Negócio)

```java
@Transactional
public void homologarCadastro(Long codSubprocesso, Usuario usuario) {
    Subprocesso sp = buscarSubprocesso(codSubprocesso);
    
    // ✓ CORRETO - Pode ocorrer com múltiplos usuários
    if (sp.getSituacao() != MAPEAMENTO_CADASTRO_DISPONIBILIZADO) {
        throw new ErroProcessoEmSituacaoInvalida(
            "Ação de homologar só pode ser executada em cadastros disponibilizados."
        );
    }
    
    // ... resto da lógica
}
```

### Exemplo 2: Invariante Violada (Interno)

```java
private void validarHierarquiaUnidades(Subprocesso sp) {
    Unidade unidade = sp.getUnidade();
    Unidade superior = unidade.getUnidadeSuperior();
    
    // ✓ CORRETO - Indica dados corrompidos, nunca deveria acontecer
    if (superior == null && unidade.getTipo() == TipoUnidade.OPERACIONAL) {
        throw new ErroInvarianteViolada(
            "Unidade operacional sem unidade superior: " + unidade.getSigla() +
            " - possível corrupção de dados"
        );
    }
}
```

### Exemplo 3: Configuração (Interno)

```java
@PostConstruct
public void validarConfiguracao() {
    String apiKey = config.getApiKey();
    
    // ✓ CORRETO - Erro de configuração
    if (apiKey == null || apiKey.isBlank()) {
        throw new ErroConfiguracao(
            "API Key não configurada. Configure a variável EXTERNAL_API_KEY"
        );
    }
}
```

## 8. Referências

- `/backend/src/main/java/sgc/comum/erros/` - Todas as classes de exceção
- `/backend/src/main/java/sgc/comum/erros/RestExceptionHandler.java` - Tratamento centralizado
- `/relatorios/analise-excecoes.md` - Análise completa do sistema
