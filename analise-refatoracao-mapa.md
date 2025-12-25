# Análise de Refatoração: Unificação dos Módulos Mapa e Atividade

## 1. Contexto e Estado Atual

A arquitetura atual apresenta uma separação entre as entidades que compõem o Mapa de Competências.

*   **Módulo `sgc.mapa`**: Contém as entidades `Mapa` e `Competencia`.
*   **Módulo `sgc.atividade`**: Contém as entidades `Atividade` e `Conhecimento`.

### Estrutura de Pacotes Identificada:
*   `Competencia`: `sgc.mapa.internal.model.Competencia`
*   `Mapa`: `sgc.mapa.internal.model.Mapa`
*   `Atividade`: `sgc.atividade.internal.model.Atividade`

### Dependências Cruzadas (Ciclo de Pacotes):
Existe uma **dependência circular** explícita entre os pacotes:
1.  `sgc.mapa` depende de `sgc.atividade`: `Competencia` possui uma coleção `@ManyToMany` de `Atividade`.
2.  `sgc.atividade` depende de `sgc.mapa`: `Atividade` possui referências `@ManyToOne` para `Mapa` e `@ManyToMany` para `Competencia`.

Esta estrutura viola o princípio de dependências acíclicas entre pacotes, dificultando a manutenção e testes isolados.

## 2. Análise de Domínio (DDD)

Do ponto de vista de *Domain-Driven Design*:
*   O `Mapa` atua como uma **Raiz de Agregado** (Aggregate Root).
*   Uma `Atividade` não existe sozinha no sistema; ela sempre pertence a um `Mapa` específico (evidenciado pelo construtor `Atividade(Mapa mapa, ...)` e mapeamento `@ManyToOne` obrigatório).
*   `Conhecimento` é uma entidade fraca (composição) dependente de `Atividade`.
*   `Competencia` já está corretamente localizada no módulo `mapa`.

Manter `Atividade` em um módulo separado cria uma divisão artificial em um conceito que deve ser tratado atomicamente (o Mapa e seus componentes).

## 3. Vantagens e Desvantagens da Unificação

### Vantagens (Pros)
1.  **Eliminação de Ciclos**: Remove a dependência circular entre `sgc.mapa` e `sgc.atividade`, simplificando o grafo de dependências do projeto.
2.  **Alta Coesão**: Todas as regras de negócio referentes à estrutura do mapa (Atividades, Competências, Conhecimentos) ficarão centralizadas.
3.  **Facilidade de Manutenção**: Alterações no ciclo de vida do Mapa (ex: clonagem, versão, exclusão) não exigirão saltos entre múltiplos módulos.
4.  **Simplificação de Transações**: Operações que envolvem salvar um mapa completo ficam contidas no mesmo contexto de serviço/repositório.

### Desvantagens (Cons)
1.  **Aumento do Módulo**: O módulo `sgc.mapa` ficará maior.
    *   *Mitigação*: Organizar internamente em subpacotes (ex: `sgc.mapa.internal.atividade`, `sgc.mapa.internal.competencia`) para manter a organização lógica, mas sob o mesmo namespace pai.
2.  **Esforço de Refatoração**: Exige movimentação de arquivos e atualização massiva de `imports` e testes.

## 4. Recomendação

**Recomendação: UNIFICAR.**
A entidade `Atividade` (e seus sub-componentes como `Conhecimento`) deve ser movida para o módulo `sgc.mapa`. O módulo `sgc.atividade` deve deixar de existir como um módulo de topo independente.

## 5. Plano de Ação para Migração

Para realizar a migração com segurança, siga os passos abaixo:

1.  **Movimentação de Entidades e Repositórios**:
    *   Mover `sgc.atividade.internal.model.*` (`Atividade`, `Conhecimento`, Repos) -> `sgc.mapa.internal.model` (ou `sgc.mapa.internal.model.atividade`).
2.  **Movimentação de Serviços e Lógica de Negócio**:
    *   Mover `AtividadeService` e `ConhecimentoMapper` para `sgc.mapa.internal.service` (ou similar).
3.  **Movimentação da Camada Web**:
    *   Mover `AtividadeController` e DTOs (`AtividadeDto`, `ConhecimentoDto`) para `sgc.mapa.internal` e `sgc.mapa.api`.
4.  **Refatoração de Imports**:
    *   Atualizar todas as referências no projeto (Backend e Testes) para os novos pacotes.
5.  **Ajustes de Banco de Dados**:
    *   Como o schema do banco é definido nas anotações `@Table(schema="sgc")`, a movimentação de pacotes Java **não** altera o banco de dados, desde que os nomes das tabelas permaneçam os mesmos. Não é necessário script de migração SQL.
6.  **Verificação**:
    *   Executar `mvn test` (ou `./gradlew test`) para garantir que nenhuma dependência foi quebrada.
