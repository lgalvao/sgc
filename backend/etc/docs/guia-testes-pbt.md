# Lições Aprendidas na Implementação de Property-Based Testing (PBT) no SGC

Este documento resume as melhores práticas e aprendizados adquiridos durante a criação de testes de propriedade com **jqwik** e **Mockito** no projeto SGC.

## 1. Configuração de Massa de Dados (Arbitraries)

*   **Unicidade de Identificadores:** Ao gerar listas de objetos (como `Subprocesso` ou `Unidade`), use `Arbitraries.longs().set()` ou `.uniqueElements()` para garantir que IDs não se repitam. IDs duplicados causam falsos negativos no Mockito quando ele tenta diferenciar chamadas para o mesmo mock.
*   **Encadeamento de Arbitrários:** Use `.flatMap()` para criar dependências entre dados gerados. Exemplo: gerar um código de subprocesso e depois usá-lo para criar um `Subprocesso` mockado com esse código.
*   **Dados Customizados:** Para domínios complexos, use classes de suporte (ex: `ProcessoETunidades`) para agrupar estados que precisam caminhar juntos durante o teste.

## 2. Integração com Mockito

*   **Mockagem de Facades e Services:** PBTs de workflow (como `SubprocessoCadastroWorkflowService`) exigem a mockagem de todas as dependências do construtor. Use `mock(Class.class)` em vez de `@Mock` quando precisar de controle total dentro do método da `@Property`.
*   **Verificação de Atomicidade:** 
    *   Simule falhas em índices específicos de uma lista (`doThrow(...).when(...).acao(..., itemFalho)`).
    *   Use `verify(service, never()).metodo(...)` para garantir que itens que viriam após o erro na lista não foram processados.
*   **Captura de Argumentos Complexos:** Use `argThat(arg -> ...)` para validar campos específicos dentro de objetos de comando (Request/DTO), especialmente quando esses objetos são criados dentro do método testado.

## 3. Padrões de Código e Invariantes

*   **Uso de Builders:** Sempre utilize o padrão `.builder()` (Lombok `@SuperBuilder`) para instanciar Entidades e DTOs nos testes. Isso torna o código mais resiliente a mudanças de estrutura e evita erros de compilação por alteração em assinaturas de construtores.
*   **Case Insensitivity:** Testes de unicidade de descrição (Atividades/Conhecimentos) devem sempre validar variações de caixa (`toLowerCase()`, `toUpperCase()`) para garantir que a lógica de negócio lida corretamente com duplicidade semântica.
*   **Enums e Constantes:** Atenção aos valores exatos de Enums (ex: `ATIVA` vs `ATIVO`). Verifique sempre a definição do Enum antes de instanciar o mock.

## 4. Troubleshooting de Erros Comuns

*   **NullPointerException em Snapshots:** Ao testar entidades que fazem "snapshots" de outras (como `UnidadeProcesso` ao ler `Unidade`), garanta que todos os campos obrigatórios da entidade origem (ex: `situacao`, `tipo`) estão preenchidos no builder.
*   **Closures e Variáveis Final:** Dentro de `argThat` ou `verify`, variáveis externas usadas para comparação de IDs devem ser final ou efetivamente final. Atribua o ID a uma variável local `final Long targetId = obj.getCodigo()` antes da verificação.
*   **Ambiguidade em Mocks:** Quando um mapper ou service possui sobrecarga de métodos (overload), use `any(ClasseEspecifica.class)` em vez de `any()` para evitar erros de compilação ou ambiguidade na execução do mock.

## 5. Fluxo de Execução

*   **Spring Context:** PBTs unitários são significativamente mais rápidos do que Integration Tests (`@SpringBootTest`). Prefira isolar a lógica de negócio e as políticas de acesso usando mocks agressivos para garantir que os milhares de casos gerados pelo `jqwik` rodem em segundos.
*   **Tagging:** Todos os testes PBT devem possuir a anotação `@Tag("PBT")` (de `net.jqwik.api`) na classe. Isso permite filtrar a execução de testes pesados no CI/CD ou localmente (ex: `./gradlew test -Djunit.jupiter.extensions.autodetection.enabled=true ...`).
