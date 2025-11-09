# Proposta de Melhorias no Mapeamento JPA

Este documento descreve uma série de sugestões para refatorar e aprimorar o mapeamento objeto-relacional (ORM) das entidades JPA do projeto SGC. O objetivo é utilizar de forma mais eficaz os recursos da JPA para simplificar o código, reduzir a redundância e melhorar a manutenibilidade.

## 1. Relação entre `Processo` e `Unidade`

**Situação Atual:**
- A associação entre `Processo` e `Unidade` é gerenciada por uma entidade intermediária chamada `UnidadeProcesso`.
- A classe `UnidadeProcesso` armazena uma cópia de vários campos da entidade `Unidade` (e.g., `nome`, `sigla`, `tipo`), o que causa redundância de dados e potenciais problemas de sincronização.
- O modelo atual não utiliza um relacionamento `@ManyToOne` ou `@ManyToMany` explícito, dependendo de IDs (`Long codProcesso`, `Long codUnidade`) para representar as associações.

**Proposta de Melhoria:**
- Eliminar a entidade `UnidadeProcesso`.
- Introduzir uma relação `@ManyToMany` direta entre as entidades `Processo` e `Unidade`.

**Vantagens:**
- **Simplificação do Modelo:** Remove uma classe intermediária, tornando o modelo de domínio mais limpo e intuitivo.
- **Eliminação de Redundância:** Evita a duplicação de dados, garantindo que as informações da `Unidade` sejam consistentes.
- **Melhoria na Navegação:** Permite navegar diretamente de um `Processo` para sua coleção de `Unidade` participantes (e vice-versa) através de `processo.getUnidades()`.

**Implementação Sugerida:**

Em `Processo.java`:
```java
@ManyToMany
@JoinTable(
    name = "unidade_processo",
    schema = "sgc",
    joinColumns = @JoinColumn(name = "processo_codigo"),
    inverseJoinColumns = @JoinColumn(name = "unidade_codigo")
)
private Set<Unidade> participantes = new HashSet<>();
```

Em `Unidade.java`:
```java
@ManyToMany(mappedBy = "participantes")
private Set<Processo> processos = new HashSet<>();
```

## 2. Relação entre `Competencia` e `Atividade`

**Situação Atual:**
- As entidades `Competencia` e `Atividade` não possuem um relacionamento direto mapeado.
- A associação entre elas é representada pela tabela de junção `competencia_atividade` no banco de dados.
- Para determinar as competências de uma atividade (ou vice-versa), é necessário realizar consultas manuais na tabela de junção, o que aumenta a complexidade da camada de serviço.

**Proposta de Melhoria:**
- Mapear a relação `@ManyToMany` entre `Competencia` e `Atividade`.

**Vantagens:**
- **Aproveitamento do ORM:** Deixa a JPA gerenciar a tabela de junção, simplificando o código da aplicação.
- **Código Mais Legível:** Permite o acesso direto às coleções associadas (e.g., `atividade.getCompetencias()`).

**Implementação Sugerida:**

Em `Atividade.java`:
```java
@ManyToMany
@JoinTable(
    name = "competencia_atividade",
    schema = "sgc",
    joinColumns = @JoinColumn(name = "atividade_codigo"),
    inverseJoinColumns = @JoinColumn(name = "competencia_codigo")
)
private Set<Competencia> competencias = new HashSet<>();
```

Em `Competencia.java`:
```java
@ManyToMany(mappedBy = "competencias")
private Set<Atividade> atividades = new HashSet<>();
```

## 3. Uso de Associações Diretas em vez de IDs

**Situação Atual:**
- Várias entidades utilizam campos `Long` para armazenar chaves estrangeiras (e.g., `subprocesso_codigo` na entidade `Analise`), em vez de usar associações JPA (`@ManyToOne`).
- Essa abordagem requer que os desenvolvedores carreguem manualmente as entidades relacionadas, aumentando o acoplamento e a verbosidade do código.

**Proposta de Melhoria:**
- Substituir os campos de ID por associações diretas (`@ManyToOne`, `@OneToOne`).

**Vantagens:**
- **Melhor Legibilidade:** O código se torna mais orientado a objetos (e.g., `analise.getSubprocesso().getCodigo()` em vez de `analise.getSubprocessoCodigo()`).
- **Gerenciamento pela JPA:** A JPA pode otimizar o carregamento das entidades associadas (lazy loading) e gerenciar o estado do ciclo de vida.

**Exemplo de Refatoração (em `Analise.java`):**

Antes:
```java
@Column(name = "subprocesso_codigo")
private Long subprocessoCodigo;
```

Depois:
```java
@ManyToOne(fetch = FetchType.LAZY)
@JoinColumn(name = "subprocesso_codigo")
private Subprocesso subprocesso;
```

Essa mudança deve ser aplicada em todas as entidades que atualmente dependem de IDs para representar relacionamentos.
