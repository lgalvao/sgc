# 📚 Guia de Documentação JavaDoc - Exceções

**Data:** 2026-01-31  
**Status:** Padrão para Documentação de Exceções

---

## 🎯 Objetivo

Este documento define o padrão para documentar exceções em métodos públicos do SGC usando JavaDoc.

---

## 📋 Regras Gerais

### Quando Documentar Exceções

Documente **sempre** quando um método público ou protegido:

1. Lança exceção **explicitamente** (`throw new ErroNegocio(...)`)
2. Deixa propagar exceção **verificada** (checked exception)
3. Pode lançar exceção de negócio **implicitamente** via chamada a outro método

### Quando NÃO Documentar

- Exceções de runtime genéricas (NullPointerException, IllegalArgumentException) que indicam bugs de programação
- Exceções internas de métodos privados
- Exceções que são capturadas e tratadas internamente

---

## 🔧 Sintaxe

```java
/**
 * Descrição do método.
 *
 * @param parametro descrição do parâmetro
 * @return descrição do retorno
 * @throws TipoExcecao descrição de quando a exceção é lançada
 * @throws OutraExcecao descrição de quando outra exceção é lançada
 */
```

---

## ✅ Exemplos Corretos

### Exemplo 1: Facade com Validação

```java
/**
 * Inicia um processo de mapeamento ou revisão.
 *
 * @param codigo código do processo a ser iniciado
 * @return processo iniciado
 * @throws ErroNegocio se o processo não for encontrado
 * @throws ErroNegocio se o processo não estiver em situação válida para ser iniciado
 * @throws ErroNegocio se houver unidades sem servidor titular
 */
public ProcessoDto iniciarProcesso(Integer codigo) {
    return workflowService.iniciar(codigo);
}
```

### Exemplo 2: Service com Múltiplas Validações

```java
/**
 * Aceita o cadastro de atividades de um subprocesso.
 * 
 * <p>Realiza as seguintes validações:
 * <ul>
 *   <li>Subprocesso deve existir</li>
 *   <li>Subprocesso deve estar em situação CADASTRO_DISPONIBILIZADO ou MAPA_VALIDADO</li>
 *   <li>Usuário deve ter permissão de aceitar cadastro</li>
 * </ul>
 *
 * @param codigo código do subprocesso
 * @param usuario usuário que está aceitando o cadastro
 * @throws ErroNegocio se o subprocesso não for encontrado
 * @throws ErroNegocio se a situação do subprocesso não permitir aceitação
 * @throws AcessoNegadoException se o usuário não tiver permissão
 */
@Transactional
public void aceitarCadastro(Integer codigo, Usuario usuario) {
    // implementação
}
```

### Exemplo 3: Método que Propaga Exceção

```java
/**
 * Busca um processo pelo código e retorna seus detalhes completos.
 *
 * @param codigo código do processo
 * @return detalhes do processo com informações de permissões do usuário
 * @throws ErroNegocio se o processo não for encontrado (propagado de ProcessoService)
 */
public ProcessoDetalheDto buscarDetalhes(Integer codigo) {
    return processoService.buscarDetalhes(codigo);
}
```

### Exemplo 4: Método com Tratamento Condicional

```java
/**
 * Finaliza um processo, tornando vigentes os mapas homologados.
 * 
 * <p>Se houver falha no envio de notificações, registra erro em log
 * mas não interrompe a finalização do processo.
 *
 * @param codigo código do processo a finalizar
 * @throws ErroNegocio se o processo não for encontrado
 * @throws ErroNegocio se o processo não puder ser finalizado (mapas pendentes)
 * @see NotificacaoService#notificarFinalizacao(Integer)
 */
@Transactional
public void finalizarProcesso(Integer codigo) {
    // implementação
}
```

---

## ❌ Exemplos Incorretos

### ❌ Erro 1: Não Documentar Exceção Lançada

```java
// ❌ ERRADO: Lança ErroNegocio mas não documenta
/**
 * Busca um subprocesso pelo código.
 */
public Subprocesso buscar(Integer codigo) {
    return repo.findByCodigo(codigo)
        .orElseThrow(() -> new ErroNegocio("Subprocesso não encontrado"));
}
```

**Correto:**

```java
// ✅ CORRETO
/**
 * Busca um subprocesso pelo código.
 *
 * @param codigo código do subprocesso
 * @return subprocesso encontrado
 * @throws ErroNegocio se o subprocesso não for encontrado
 */
public Subprocesso buscar(Integer codigo) {
    return repo.findByCodigo(codigo)
        .orElseThrow(() -> new ErroNegocio("Subprocesso não encontrado"));
}
```

### ❌ Erro 2: Descrição Genérica

```java
// ❌ ERRADO: Descrição muito genérica
/**
 * @throws ErroNegocio se houver erro
 */
```

**Correto:**

```java
// ✅ CORRETO: Descreve condições específicas
/**
 * @throws ErroNegocio se o processo não for encontrado
 * @throws ErroNegocio se o processo não estiver em situação INICIADO
 */
```

### ❌ Erro 3: Documentar Exception Genérica

```java
// ❌ ERRADO: Exception é muito genérica
/**
 * @throws Exception se algo der errado
 */
```

**Correto:**

```java
// ✅ CORRETO: Tipos específicos de exceção
/**
 * @throws ErroNegocio se a validação falhar
 * @throws AcessoNegadoException se o usuário não tiver permissão
 */
```

---

## 📐 Padrões Específicos do SGC

### Exceções de Negócio

Use `ErroNegocio` para violações de regras de negócio:

```java
/**
 * @throws ErroNegocio se [condição específica que viola regra de negócio]
 */
```

Exemplos:
- "se o processo não for encontrado"
- "se a situação do subprocesso não permitir a operação"
- "se houver atividades duplicadas no mapa"
- "se o servidor não tiver perfil de chefe"

### Exceções de Acesso

Use `AcessoNegadoException` (ou tipo do Spring Security) para problemas de permissão:

```java
/**
 * @throws AcessoNegadoException se o usuário não tiver permissão para [ação específica]
 */
```

Exemplos:
- "se o usuário não for titular da unidade"
- "se o usuário não tiver perfil ADMIN ou GESTOR"
- "se o usuário não pertencer à hierarquia da unidade"

### Múltiplas Exceções do Mesmo Tipo

Quando o mesmo tipo de exceção pode ser lançado por motivos diferentes:

```java
/**
 * Valida e salva um mapa de competências.
 *
 * @param mapa mapa a ser salvo
 * @throws ErroNegocio se o subprocesso não for encontrado
 * @throws ErroNegocio se o mapa não tiver atividades
 * @throws ErroNegocio se houver atividades com competências duplicadas
 * @throws ErroNegocio se a situação do subprocesso não permitir edição do mapa
 */
```

---

## 🔍 Checklist de Revisão

Ao documentar um método, verifique:

- [ ] Todas as exceções lançadas explicitamente estão documentadas?
- [ ] Exceções propagadas de métodos chamados estão documentadas?
- [ ] Cada `@throws` descreve **quando** a exceção é lançada?
- [ ] A descrição é **específica** (não genérica)?
- [ ] O tipo da exceção é **específico** (não Exception genérica)?
- [ ] Exceções de runtime de bugs (NPE, IAE) foram omitidas corretamente?

---

## 🎯 Métodos Prioritários para Documentar

1. **Facades públicas** - Interface principal da aplicação
2. **Services públicos** - Lógica de negócio
3. **Controllers REST** - APIs expostas
4. **Métodos de workflow** - Transições de estado críticas

---

## 📊 Exemplo Completo de Facade

```java
package sgc.processo.service;

import sgc.comum.ErroNegocio;
import sgc.seguranca.AcessoNegadoException;
import org.springframework.stereotype.Service;

/**
 * Facade para gerenciamento de processos de mapeamento e revisão.
 *
 * <p>Responsável por orquestrar operações de criação, workflow e consulta
 * de processos. Delega operações especializadas para services específicos
 * mantendo interface coesa para controllers.
 *
 * @see ProcessoCoreService
 * @see ProcessoWorkflowService
 * @see ProcessoContextService
 */
@Service
public class ProcessoFacade {
    
    /**
     * Cria um novo processo de mapeamento ou revisão.
     *
     * @param request dados do processo a criar
     * @return processo criado com código gerado
     * @throws ErroNegocio se já existir processo ativo do mesmo tipo para a unidade
     * @throws ErroNegocio se houver unidades inválidas ou inexistentes
     * @throws AcessoNegadoException se o usuário não tiver perfil ADMIN ou GESTOR
     */
    public ProcessoDto criarProcesso(ProcessoRequest request) {
        // implementação
    }
    
    /**
     * Inicia um processo, criando subprocessos para todas as unidades participantes.
     *
     * @param codigo código do processo a iniciar
     * @return processo iniciado com subprocessos criados
     * @throws ErroNegocio se o processo não for encontrado
     * @throws ErroNegocio se o processo não estiver em situação CADASTRADO
     * @throws ErroNegocio se houver unidades sem servidor titular
     * @throws AcessoNegadoException se o usuário não tiver permissão para iniciar processo
     */
    public ProcessoDto iniciarProcesso(Integer codigo) {
        // implementação
    }
    
    /**
     * Finaliza um processo, tornando vigentes os mapas homologados.
     * 
     * <p>Envia notificações para todas as unidades participantes. Falhas no
     * envio de notificações são registradas em log mas não impedem a finalização.
     *
     * @param codigo código do processo a finalizar
     * @throws ErroNegocio se o processo não for encontrado
     * @throws ErroNegocio se houver subprocessos com mapas não homologados
     * @throws AcessoNegadoException se o usuário não tiver permissão para finalizar
     */
    public void finalizarProcesso(Integer codigo) {
        // implementação
    }
    
    /**
     * Remove um processo e todos os seus subprocessos.
     * 
     * <p><strong>Atenção:</strong> Esta operação é irreversível e remove
     * todos os dados associados ao processo.
     *
     * @param codigo código do processo a remover
     * @throws ErroNegocio se o processo não for encontrado
     * @throws ErroNegocio se o processo já foi iniciado
     * @throws AcessoNegadoException se o usuário não tiver permissão para remover
     */
    public void removerProcesso(Integer codigo) {
        // implementação
    }
}
```

---

## 🚀 Próximos Passos

1. **Auditoria:** Identificar métodos públicos sem documentação de exceções
2. **Priorização:** Começar por Facades e Services críticos
3. **Padronização:** Aplicar templates deste guia
4. **Revisão:** Incluir verificação de JavaDoc em code reviews

---

**Última Atualização:** 2026-01-31  
**Referências:**
- [Oracle JavaDoc Guide](https://www.oracle.com/technical-resources/articles/java/javadoc-tool.html)
- [Google Java Style Guide - Javadoc](https://google.github.io/styleguide/javaguide.html#s7-javadoc)
