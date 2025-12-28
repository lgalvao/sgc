# Relatório de Problemas e Status do WIP

## Estado Atual
O sistema backend estava impedido de iniciar devido a dois problemas críticos que foram resolvidos nesta iteração:
1.  **Conflito de Beans:** Havia uma duplicata da classe `AlertaService` em `sgc.alerta.service` conflitanto com `sgc.alerta.AlertaService`. A duplicata foi removida e seus métodos mesclados.
2.  **Dependências Circulares:** Existiam ciclos de dependência impedindo a criação do contexto Spring:
    - `SubprocessoService` <-> `AtividadeService`
    - `SubprocessoService` <-> `AnaliseService`

## Solução Aplicada (Temporária)
Para permitir a inicialização do contexto e a execução dos testes, as dependências circulares foram quebradas da seguinte forma:
- **Eventos:** `AtividadeService` agora publica um `EventoMapaAlterado` em vez de chamar `SubprocessoService` diretamente para atualizar o status do subprocesso.
- **Injeção de Repositório Cruzado:** `AtividadeService` e `AnaliseService` passaram a injetar diretamente o `SubprocessoRepo` em vez de `SubprocessoService` para realizar lookups de leitura.

## Problemas Remanescentes

### 1. Violação de Arquitetura (ArchUnit)
A solução de injetar `SubprocessoRepo` em serviços de outros módulos (`mapa` e `analise`) viola a regra arquitetural que proíbe o acesso direto a repositórios de outros módulos (`services_should_not_access_other_modules_repositories`).

**Erro:** `ArchConsistencyTest > services_should_not_access_other_modules_repositories FAILED`

### 2. Falhas em Testes Unitários
Devido à alteração nas dependências dos serviços (troca de `Service` por `Repo`), vários testes unitários que mockavam os Serviços agora falham ou precisam ser atualizados para mockar os Repositórios.
- `AtividadeServiceTest`
- `AnaliseServiceTest`
- `SubprocessoServiceTest` (afetado pelas mudanças nos construtores/injeções)

## Plano de Correção Recomendado (Próximos Passos)
Para resolver a violação de arquitetura e manter o desacoplamento, recomenda-se a seguinte refatoração:

1.  **Refatorar `AtividadeControle` e `AnaliseControle`:**
    - Mover a lógica de orquestração "Buscar Pai -> Validar -> Criar Filho" para a camada de Controller ou um Facade.
    - O Controller deve injetar `SubprocessoService` para buscar/validar o subprocesso e, em seguida, chamar `AtividadeService` ou `AnaliseService` passando a entidade ou DTO já validado.
2.  **Remover Dependências Cruzadas nos Serviços:**
    - `AtividadeService` e `AnaliseService` devem deixar de injetar `SubprocessoRepo`.
    - `AtividadeService.criar` e `AnaliseService.criar` devem receber os dados necessários (ou a entidade Pai) como argumento, sem precisar buscá-la novamente.
3.  **Atualizar Testes:**
    - Ajustar os testes unitários para refletir a nova arquitetura e assinaturas de métodos.

Esta refatoração garantirá que o ciclo de dependência seja quebrado na camada de serviço (o Controller depende de ambos, mas eles não dependem entre si) e respeitará as regras do ArchUnit.
