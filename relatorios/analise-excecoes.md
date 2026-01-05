# Relatório de Análise: Tratamento de Exceções no SGC

**Data:** 2026-01-05  
**Autor:** GitHub Copilot Agent  
**Versão:** 1.0

---

## 1. Sumário Executivo

Este relatório apresenta uma análise abrangente do sistema atual de tratamento de exceções do SGC, identificando problemas de complexidade, fragmentação e inconsistência. O estudo examinou 27 arquivos Java que lançam exceções, mais de 76 lançamentos de exceções de domínio e 13 usos de exceções genéricas da JDK.

### Principais Descobertas

1. **Mistura de exceções internas e de negócio**: `IllegalStateException` e `IllegalArgumentException` são usadas tanto para erros internos (programação defensiva) quanto para violações de regras de negócio
2. **Fragmentação**: Existe a classe `ErroSituacaoInvalida` (não tratada adequadamente) enquanto múltiplas exceções específicas como `ErroProcessoEmSituacaoInvalida` e `ErroMapaEmSituacaoInvalida` são usadas em paralelo
3. **Inconsistência**: Situações similares são tratadas com exceções diferentes em partes diferentes do sistema
4. **Falta de distinção clara**: Não há separação explícita entre erros que nunca deveriam ocorrer (bugs) e erros de negócio esperados

---

## 2. Estrutura Atual de Exceções

### 2.1. Hierarquia Formal

```
RuntimeException
    └── ErroNegocioBase (abstract, implements ErroNegocio)
            ├── ErroEntidadeNaoEncontrada (404)
            ├── ErroAccessoNegado (403)
            ├── ErroAutenticacao (401)
            ├── ErroRequisicaoSemCorpo (400)
            ├── ErroValidacao (422) *
            ├── ErroProcesso (409)
            ├── ErroProcessoEmSituacaoInvalida (422)
            ├── ErroMapaEmSituacaoInvalida (422)
            ├── ErroAtividadesEmSituacaoInvalida (422)
            ├── ErroMapaNaoAssociado (específico)
            ├── ErroUnidadesNaoDefinidas (422)
            ├── ErroUnidadeNaoEncontrada (404)
            ├── ErroAlerta (409)
            └── ErroParametroPainelInvalido (400)
    
    └── ErroSituacaoInvalida (não implementa ErroNegocio) **
    
    └── IllegalStateException (JDK)
    └── IllegalArgumentException (JDK)
```

**\* Nota**: `ErroValidacao` não estende `ErroNegocioBase`, apenas é uma `RuntimeException` com tratamento específico  
**\*\* Problema**: `ErroSituacaoInvalida` não implementa `ErroNegocio`, então não é tratada adequadamente pelo `RestExceptionHandler`

### 2.2. Distribuição de Uso

| Tipo de Exceção | Frequência | Status HTTP | Contexto de Uso |
|-----------------|-----------|-------------|-----------------|
| `ErroValidacao` | 20 | 422 | Validações de negócio, dados inválidos |
| `ErroEntidadeNaoEncontrada` | 17 | 404 | Recursos não encontrados |
| `ErroAccessoNegado` | 15 | 403 | Violações de permissão |
| `IllegalStateException` | 13 | 409 | **MISTO: interno + negócio** |
| `ErroProcesso` | 8 | 409 | Violações de regras de processo |
| `ErroAutenticacao` | 4 | 401 | Falhas de autenticação |
| `ErroProcessoEmSituacaoInvalida` | 3 | 422 | Estado inválido de processo |
| Outros | 12 | Variados | Casos específicos |

---

## 3. Problemas Identificados

### 3.1. PROBLEMA CRÍTICO: Mistura de Exceções Internas e de Negócio

**Descrição**: `IllegalStateException` é usada para dois propósitos conflitantes:

#### Uso Correto (Programação Defensiva - Erros Internos)
```java
// sgc/seguranca/GerenciadorJwt.java:38
throw new IllegalStateException(
    "FALHA DE SEGURANÇA: A propriedade 'aplicacao.jwt.secret' não foi alterada do padrão inseguro."
);

// sgc/seguranca/GerenciadorJwt.java:49
throw new IllegalStateException("JWT secret deve ter no mínimo 32 caracteres");

// sgc/processo/service/ProcessoService.java:157
log.error("ERRO INTERNO: Tentativa de criar processo com unidade INTERMEDIARIA");
throw new IllegalStateException("Erro interno: unidade não elegível foi enviada ao backend");
```

**Análise**: Estes são **erros de programação ou configuração** que nunca deveriam ocorrer se o sistema estiver configurado e funcionando corretamente. O UI correto impede que esses casos aconteçam.

#### Uso Incorreto (Regras de Negócio)
```java
// SubprocessoCadastroWorkflowService.java:119
if (unidadeAnalise == null) {
    throw new IllegalStateException(
        "Unidade superior não encontrada para o subprocesso " + codSubprocesso
    );
}

// SubprocessoCadastroWorkflowService.java:171
if (sp.getSituacao() != MAPEAMENTO_CADASTRO_DISPONIBILIZADO) {
    throw new IllegalStateException(
        "Ação de homologar só pode ser executada em cadastros disponibilizados."
    );
}
```

**Análise**: Estes são **erros de negócio legítimos** que podem ocorrer durante o uso normal do sistema (dados inconsistentes, condições de corrida, estado modificado por outro usuário). Deveriam usar exceções de domínio apropriadas.

**Impacto**:
- ✗ Erros de negócio esperados são tratados como falhas internas (500)
- ✗ Logs inadequados (ERROR em vez de WARN para negócio)
- ✗ Mensagens genéricas para o usuário
- ✗ Impossibilidade de distinguir bugs reais de violações de negócio

### 3.2. Fragmentação: ErroSituacaoInvalida vs Exceções Específicas

**Problema**: Existem múltiplas formas de expressar "situação inválida":

1. `ErroSituacaoInvalida` (genérica, não tratada adequadamente)
2. `ErroProcessoEmSituacaoInvalida` (específica, estende ErroNegocioBase)
3. `ErroMapaEmSituacaoInvalida` (específica, estende ErroNegocioBase)
4. `ErroAtividadesEmSituacaoInvalida` (específica, estende ErroNegocioBase)
5. `IllegalStateException` (JDK, usada incorretamente)

**Análise**:
- `ErroSituacaoInvalida` está **definida mas nunca lançada** no código
- Existe apenas como documentação em JavaDoc
- As exceções específicas fazem o trabalho correto
- Causa confusão para desenvolvedores sobre qual usar

**Recomendação**: Remover ou transformar em exceção base abstrata

### 3.3. ErroValidacao Inconsistente

**Problema**: `ErroValidacao` não estende `ErroNegocioBase`:

```java
@Getter
@ResponseStatus(HttpStatus.UNPROCESSABLE_CONTENT)
public class ErroValidacao extends RuntimeException {
    // Não implementa ErroNegocio
}
```

**Impacto**:
- Precisa de handler específico no `RestExceptionHandler`
- Não segue o padrão unificado
- Duplicação de lógica de tratamento

### 3.4. Falta de Documentação Clara

**Problema**: Não existe documentação explícita sobre:
- Quando usar cada tipo de exceção
- Diferença entre erros internos e de negócio
- Guia de escolha de exceção para novos desenvolvedores

**Evidência**: Uso inconsistente mostra que desenvolvedores não têm clareza

---

## 4. Análise do Frontend

### 4.1. Estrutura de Normalização de Erros

O frontend possui um sistema bem estruturado em `/frontend/src/utils/apiError.ts`:

```typescript
export type ErrorKind =
  | 'validation'      // 400, 422
  | 'notFound'        // 404
  | 'conflict'        // 409
  | 'unauthorized'    // 401
  | 'forbidden'       // 403
  | 'network'         // Erro de rede
  | 'unexpected';     // 500
```

**Pontos Positivos**:
- ✓ Categorização clara de erros
- ✓ Normalização consistente via `normalizeError()`
- ✓ Distinção entre erros inline e globais
- ✓ Utilities para casos comuns (`getOrNull`, `existsOrFalse`)

**Problema**:
- ✗ Depende de status HTTP corretos do backend
- ✗ Status 409 para `IllegalStateException` confunde: pode ser interno ou negócio
- ✗ Sem distinção de erros "impossíveis" vs "esperados"

---

## 5. Casos de Uso Problemáticos

### 5.1. Unidade Superior Não Encontrada

**Ocorrências**: 4 vezes em `SubprocessoCadastroWorkflowService`

```java
Unidade unidadeAnalise = sp.getUnidade().getUnidadeSuperior();
if (unidadeAnalise == null) {
    throw new IllegalStateException(
        "Unidade superior não encontrada para o subprocesso " + codSubprocesso
    );
}
```

**Análise**:
- **É um erro interno?** Se a hierarquia de unidades está correta no banco, isso nunca deveria acontecer
- **É um erro de negócio?** Se dados podem ficar inconsistentes, é esperado
- **Decisão**: Provavelmente **erro interno**, pois indica corrupção de dados ou falha na validação de integridade referencial

**Recomendação**: Usar exceção interna customizada, logar como ERROR com traceId

### 5.2. Validação de Situação de Workflow

**Ocorrências**: 8 vezes em `SubprocessoCadastroWorkflowService`

```java
if (sp.getSituacao() != REVISAO_CADASTRO_DISPONIBILIZADA) {
    throw new IllegalStateException(
        "Ação de homologar só pode ser executada em revisões..."
    );
}
```

**Análise**:
- **É um erro interno?** Se o UI está correto, botões/ações só aparecem no estado certo
- **É um erro de negócio?** Se múltiplos usuários ou condições de corrida podem mudar o estado, é esperado
- **Decisão**: **Erro de negócio**, pois pode ocorrer legitimamente (outro usuário mudou estado, sessão expirou, etc.)

**Recomendação**: Usar `ErroProcessoEmSituacaoInvalida` ou similar (422), logar como WARN

---

## 6. Recomendações Detalhadas

### 6.1. Criar Hierarquia de Exceções Internas

**Objetivo**: Distinguir claramente erros que nunca deveriam ocorrer em produção

```java
/**
 * Exceção base para erros internos que indicam bugs, configuração
 * incorreta ou violação de invariantes do sistema.
 * 
 * <p>Estas exceções NÃO são erros de negócio esperados. Sempre
 * resultam em HTTP 500 e são logadas como ERROR com traceId.
 * 
 * <p>Exemplos:
 * - Configuração inválida (JWT secret não configurado)
 * - Violação de invariantes (dados corrompidos no banco)
 * - Estado impossível se UI funciona corretamente
 */
public abstract class ErroInterno extends RuntimeException {
    public ErroInterno(String message) {
        super(message);
    }
    
    public ErroInterno(String message, Throwable cause) {
        super(message, cause);
    }
}

// Subclasses específicas
public class ErroConfiguracao extends ErroInterno { }
public class ErroInvarianteViolada extends ErroInterno { }
public class ErroEstadoImpossivel extends ErroInterno { }
```

**Handler no RestExceptionHandler**:
```java
@ExceptionHandler(ErroInterno.class)
protected ResponseEntity<Object> handleErroInterno(ErroInterno ex) {
    String traceId = UUID.randomUUID().toString();
    log.error("[{}] ERRO INTERNO - Este é um bug que precisa ser corrigido: {}", 
              traceId, ex.getMessage(), ex);
    
    // Mensagem genérica para usuário (não expor detalhes internos)
    return buildResponseEntity(
        new ErroApi(
            HttpStatus.INTERNAL_SERVER_ERROR,
            "Erro interno do sistema. Por favor, contate o suporte com o código: " + traceId,
            "ERRO_INTERNO",
            traceId
        )
    );
}
```

### 6.2. Refatorar IllegalStateException

**Plano de Ação**:

1. **Categorizar cada uso** (13 ocorrências):
   - Interno: 4 casos (JWT, configuração, unidade INTERMEDIARIA, arquivo seed)
   - Negócio: 9 casos (validações de workflow)

2. **Substituir usos internos**:
   ```java
   // ANTES
   throw new IllegalStateException("JWT secret deve ter no mínimo 32 caracteres");
   
   // DEPOIS
   throw new ErroConfiguracao("JWT secret deve ter no mínimo 32 caracteres");
   ```

3. **Substituir usos de negócio**:
   ```java
   // ANTES
   if (sp.getSituacao() != MAPEAMENTO_CADASTRO_DISPONIBILIZADO) {
       throw new IllegalStateException("Ação de homologar só pode ser executada...");
   }
   
   // DEPOIS
   if (sp.getSituacao() != MAPEAMENTO_CADASTRO_DISPONIBILIZADO) {
       throw new ErroProcessoEmSituacaoInvalida("Ação de homologar só pode ser executada...");
   }
   ```

### 6.3. Consolidar Exceções de Situação Inválida

**Opção A - Remover ErroSituacaoInvalida**:
- Manter apenas as exceções específicas (`ErroProcessoEmSituacaoInvalida`, etc.)
- Benefício: Menos classes, mais específico
- Desvantagem: Múltiplas classes para conceito similar

**Opção B - Transformar em Base Abstrata** (RECOMENDADO):
```java
/**
 * Exceção base para operações executadas em estado/situação inválido.
 * Use subclasses específicas por domínio.
 */
public abstract class ErroSituacaoInvalida extends ErroNegocioBase {
    protected ErroSituacaoInvalida(String message, String code) {
        super(message, code, HttpStatus.UNPROCESSABLE_CONTENT);
    }
}

public class ErroProcessoEmSituacaoInvalida extends ErroSituacaoInvalida {
    public ErroProcessoEmSituacaoInvalida(String message) {
        super(message, "PROCESSO_SITUACAO_INVALIDA");
    }
}

// Similarmente para Mapa, Atividades, etc.
```

### 6.4. Padronizar ErroValidacao

**Fazer ErroValidacao estender ErroNegocioBase**:

```java
public class ErroValidacao extends ErroNegocioBase {
    public ErroValidacao(String message) {
        super(message, "VALIDACAO", HttpStatus.UNPROCESSABLE_CONTENT);
    }
    
    public ErroValidacao(String message, Map<String, ?> details) {
        super(message, "VALIDACAO", HttpStatus.UNPROCESSABLE_CONTENT, details);
    }
}
```

**Remover handler específico** do `RestExceptionHandler` (será tratado pelo handler genérico de `ErroNegocioBase`)

### 6.5. Eliminar IllegalArgumentException

**Análise**: Apenas 1 uso encontrado:
```java
// E2eController.java
if (req.getUnidade() == null) {
    throw new IllegalArgumentException("Unidade é obrigatória");
}
```

**Recomendação**: 
- Para endpoints E2E/teste: manter ou usar `ErroValidacao`
- Para código de produção: sempre usar exceções de domínio

### 6.6. Documentar Guia de Decisão

Criar documento **"Guia de Exceções SGC"**:

```markdown
# Guia de Escolha de Exceções - SGC

## 1. Fluxograma de Decisão

É um erro que nunca deveria ocorrer se o sistema funciona corretamente?
    SIM → ErroInterno (e subclasses)
    NÃO → Erro de Negócio (continuar)

É um recurso não encontrado?
    SIM → ErroEntidadeNaoEncontrada (404)
    NÃO → (continuar)

É uma violação de permissão/acesso?
    SIM → ErroAccessoNegado (403)
    NÃO → (continuar)

É uma validação de dados de entrada?
    SIM → ErroValidacao (422)
    NÃO → (continuar)

É uma operação em estado/situação inválido?
    SIM → ErroXxxEmSituacaoInvalida (422)
    NÃO → (continuar)

É uma violação de regra de negócio geral?
    SIM → ErroProcesso ou similar (409)

## 2. Exemplos Práticos

### Erro Interno
- Configuração faltando
- Dados corrompidos (FK inválida)
- Código atingiu branch "impossível"

### Erro de Negócio
- Usuário tenta ação no estado errado
- Dados enviados violam regra
- Condição de corrida altera estado
```

---

## 7. Impacto das Mudanças

### 7.1. Benefícios

1. **Clareza**: Distinção óbvia entre bugs e negócio
2. **Monitoramento**: Alertas diferentes para erro interno vs negócio
3. **Logs**: Severidade adequada (ERROR para interno, WARN para negócio)
4. **UX**: Mensagens mais apropriadas para usuário final
5. **Manutenibilidade**: Novos desenvolvedores sabem o que usar

### 7.2. Esforço de Implementação

| Tarefa | Esforço | Arquivos Afetados | Risco |
|--------|---------|-------------------|-------|
| Criar ErroInterno e subclasses | Baixo | 4 novos | Baixo |
| Adicionar handler no RestExceptionHandler | Baixo | 1 | Baixo |
| Refatorar IllegalStateException | Médio | 6 | Médio |
| Consolidar ErroSituacaoInvalida | Baixo | 2 | Baixo |
| Padronizar ErroValidacao | Baixo | 1 | Baixo |
| Atualizar documentação | Médio | 2 | Baixo |
| Testes | Médio | 10-15 | Médio |

**Total estimado**: 8-12 horas de desenvolvimento + testes

### 7.3. Compatibilidade

- ✓ **Backward compatible**: Novos códigos de erro, mas estrutura JSON mantida
- ✓ **Frontend**: Continua funcionando (usa status HTTP)
- ✓ **API Pública**: Sem breaking changes
- ⚠️ **Logs**: Formato pode mudar (melhoria)

---

## 8. Plano de Implementação Sugerido

### Fase 1: Fundação (Sprint 1)
1. Criar classes `ErroInterno` e subclasses
2. Adicionar handler no `RestExceptionHandler`
3. Criar testes unitários para novos handlers
4. Documentar guia de decisão

### Fase 2: Refatoração (Sprint 2)
5. Refatorar `IllegalStateException` (internos primeiro)
6. Refatorar `IllegalStateException` (negócio)
7. Consolidar `ErroSituacaoInvalida`
8. Padronizar `ErroValidacao`

### Fase 3: Validação (Sprint 3)
9. Executar suite completa de testes
10. Testes E2E para validar comportamento
11. Revisar logs e monitoramento
12. Atualizar documentação final

---

## 9. Conclusão

O sistema atual de exceções do SGC apresenta uma base sólida com `ErroNegocioBase` e `RestExceptionHandler`, mas sofre de:
- **Mistura de conceitos**: interno vs negócio
- **Fragmentação**: múltiplas formas de expressar o mesmo
- **Inconsistência**: falta de padrão claro

As recomendações propostas mantêm a estrutura existente, adicionando clareza e consistência com mudanças mínimas e risco controlado. A implementação gradual permite validação incremental.

**Recomendação Final**: Implementar todas as mudanças propostas em 3 sprints, priorizando a criação de `ErroInterno` e refatoração de `IllegalStateException` por serem os problemas mais críticos.

---

## Anexos

### A. Inventário Completo de Exceções

#### Exceções de Domínio (sgc.comum.erros)
- ErroNegocioBase (abstract)
- ErroNegocio (interface)
- ErroEntidadeNaoEncontrada
- ErroValidacao
- ErroSituacaoInvalida (não usada)
- ErroAccessoNegado
- ErroAutenticacao
- ErroRequisicaoSemCorpo

#### Exceções de Domínio Específico
- processo: ErroProcesso, ErroProcessoEmSituacaoInvalida, ErroUnidadesNaoDefinidas
- subprocesso: ErroMapaEmSituacaoInvalida, ErroAtividadesEmSituacaoInvalida, ErroMapaNaoAssociado
- organizacao: ErroUnidadeNaoEncontrada
- alerta: ErroAlerta
- painel: ErroParametroPainelInvalido

#### Handlers em RestExceptionHandler
1. `handleErroNegocio(ErroNegocioBase)` - principal
2. `handleErroValidacao(ErroValidacao)` - específico
3. `handleErroAutenticacao(ErroAutenticacao)` - específico
4. `handleIllegalStateException(IllegalStateException)` - JDK
5. `handleIllegalArgumentException(IllegalArgumentException)` - JDK
6. `handleAccessDenied(AccessDeniedException)` - Spring Security
7. `handleConstraintViolationException(ConstraintViolationException)` - Bean Validation
8. `handleHttpMessageNotReadable(HttpMessageNotReadableException)` - Spring
9. `handleMethodArgumentNotValid(MethodArgumentNotValidException)` - Spring
10. `handleGenericException(Exception)` - catch-all

### B. Referências

- Código fonte: `/backend/src/main/java/sgc/comum/erros/`
- Documentação: `/regras/backend-padroes.md`
- Frontend: `/frontend/src/utils/apiError.ts`
