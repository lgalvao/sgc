# Plano de Correção dos Testes Falhos

Este documento detalha a análise e o plano de ação para corrigir os testes que estão falhando na suite de testes do backend.

## Análise dos Testes Falhos

A investigação revelou três causas principais para os testes falhos:

1.  **Erros de Validação (`422 Unprocessable Entity`)**: A reativação de uma regra de validação que exige que toda `Atividade` seja associada a pelo menos uma `Competencia` invalidou os dados de teste em múltiplos casos de uso (CDU-09, CDU-10, CDU-16, CDU-17), causando falhas generalizadas. A tentativa de correção falhou por um entendimento incorreto do modelo de dados. A análise do código revelou uma relação `@ManyToMany` direta entre `Competencia` e `Atividade`, onde `Atividade` é a dona da relação.

2.  **`NullPointerException` em CDU-21 (`500 Internal Server Error`)**: Uma falha no tratamento de `null` no método `criarSubprocessoParaRevisao` do `ProcessoService` causa um `NullPointerException` quando uma `UnidadeMapa` não tem um `mapaVigente` associado, resultando em um erro 500 inesperado durante a finalização do processo.

3.  **Resultados Incorretos em CDU-02 (Painel)**: A lógica do `PainelService` para buscar processos para os perfis `GESTOR` e `CHEFE` está incorreta. O método `paraProcessoResumoDto` considera apenas o primeiro participante de um processo, ignorando os demais e fazendo com que a consulta hierárquica não retorne todos os processos esperados.

## Plano de Ação

O plano será executado de forma iterativa, com validação a cada passo:

1.  **Correção dos Erros de Validação (422)**
    *   **CDU-09, CDU-10, CDU-16, CDU-17 (Casos de Sucesso)**: Modificar a configuração de teste (`setUp`) para associar corretamente as entidades. O padrão a ser seguido é:
        1.  Salvar a `Competencia`.
        2.  Salvar a `Atividade`.
        3.  Adicionar a `Competencia` à `Set<Competencia>` da `Atividade` (`atividade.getCompetencias().add(competencia)`).
        4.  Salvar a `Atividade` novamente para persistir a relação.
    *   **CDU-17 (Caso de Falha)**: Ajustar o teste que espera a falha de validação (`disponibilizarMapa_comAtividadeNaoAssociada_retornaBadRequest`) para garantir que o cenário de falha seja corretamente montado: uma `Atividade` deve existir no mapa sem estar associada a nenhuma `Competencia`.

2.  **Execução de Testes Direcionados**: Executar os testes para CDU-09, CDU-10, CDU-16 e CDU-17 para validar que os erros `422` foram resolvidos.

3.  **Correção do Erro em CDU-21 (500)**
    *   Adicionar um `null-check` no método `criarSubprocessoParaRevisao` em `ProcessoService.java` para verificar se `unidadeMapa.getMapaVigente()` não é nulo antes de tentar acessar `getCodigo()`, lançando uma `ErroProcesso` se for nulo.

4.  **Correção do Erro em CDU-02 (Painel)**
    *   Refatorar o método `paraProcessoResumoDto` em `PainelService.java` para lidar corretamente com processos que podem não ter participantes, evitando `NullPointerException`s e garantindo que a lógica de negócio não dependa de uma suposição incorreta.

5.  **Execução Completa da Suite de Testes**: Após a aplicação de todas as correções, executar a suite de testes completa do backend (`./gradlew :backend:test`) para garantir que todas as falhas foram resolvidas e nenhum novo problema foi introduzido.

6.  **Submissão**: Após a confirmação de que todos os testes passaram, submeter as alterações.
