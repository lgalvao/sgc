# Refatoração do Mapeamento JPA de UnidadeProcesso

## Contexto
Durante a execução de testes de integração (`CDU02IntegrationTest` e possivelmente outros), observou-se uma falha ao persistir a entidade `Processo` com seus participantes (`UnidadeProcesso`). O erro `ApplicationContext failure` esconde uma causa raiz relacionada ao mapeamento JPA.

## Problema Identificado
A entidade `UnidadeProcesso` utiliza uma chave composta (`UnidadeProcessoId`) e mapeia o relacionamento com `Processo` da seguinte forma:

```java
@IdClass(UnidadeProcessoId.class)
public class UnidadeProcesso {
    @Id
    @Column(name = "processo_codigo")
    private Long processoCodigo;

    @ManyToOne
    @JoinColumn(name = "processo_codigo", insertable = false, updatable = false)
    private Processo processo;
}
```

Quando um novo `Processo` é criado, ele ainda não possui ID (é `null`). Ao adicionar `UnidadeProcesso` na lista de participantes e tentar salvar o `Processo` (cascade), o Hibernate tenta salvar `UnidadeProcesso`. Como `processoCodigo` está nulo (pois o ID do processo só é gerado após o insert) e a coluna não é gerenciada pelo relacionamento (`insertable=false`), ocorre erro de nulidade ou falha de integridade.

## Solução Proposta: Derived Identity
Para corrigir isso, devemos utilizar o padrão *Derived Identity* da JPA, onde o relacionamento é parte da chave primária e gerencia a coluna FK. Isso permite que o Hibernate propague o ID gerado do pai para o filho automaticamente.

### Mudanças Estruturais
1.  **Classe `UnidadeProcessoId`**:
    *   Renomear o atributo `processoCodigo` para `processo`. Isso é necessário para corresponder ao nome do atributo na entidade, conforme especificação JPA para `Derived Identity`.

2.  **Classe `UnidadeProcesso`**:
    *   Remover o campo `@Id Long processoCodigo`.
    *   Adicionar anotação `@Id` ao relacionamento `Processo processo`.
    *   Remover `insertable = false, updatable = false` do `@JoinColumn` de `processo`.
    *   Atualizar o método factory `criarSnapshot` para setar a referência do objeto `processo` (ex: `snapshot.setProcesso(processo)`) em vez do ID.
    *   Manter um método *getter* de conveniência `getProcessoCodigo()` para preservar compatibilidade com o restante do código que consome essa propriedade.

## Impacto
*   Alteração nas classes `UnidadeProcesso.java` e `UnidadeProcessoId.java`.
*   Ajuste no método `criarSnapshot` em `UnidadeProcesso`.
*   Atualização de queries JPQL em `UnidadeProcessoRepo` (ex: substituir `up.processoCodigo` por `up.processo.codigo`).
*   Nenhuma alteração de banco de dados (schema) é necessária, pois a estrutura da tabela permanece a mesma (`processo_codigo`, `unidade_codigo`).
*   Correção imediata dos testes de integração que dependem da persistência em cascata de Processos.
