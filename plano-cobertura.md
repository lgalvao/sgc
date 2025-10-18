# Plano de Ação para Aumentar a Cobertura de Testes do Frontend para 95%

## 1. Visão Geral

A cobertura de testes atual do frontend é de **91.69%**. O objetivo é atingir **95%** de cobertura de statements. A análise do relatório de cobertura mais recente (`vitest run --coverage`) revela que, embora a maior parte do código esteja bem testada, alguns arquivos específicos estão impedindo o alcance da meta.

As áreas prioritárias são:

- **`src/stores/processos.ts`**: Cobertura de **59.82%**.
- **`src/stores/atividades.ts`**: Cobertura de **64.75%**.
- **`src/services/processoService.ts`**: Cobertura de **70.42%**.
- **`src/axios-setup.ts`**: Cobertura de **43.75%**.

## 2. Plano Detalhado por Etapas

### Etapa 1: Aumentar Cobertura do `processos.ts` Store

Este é o arquivo com a menor cobertura e um dos mais críticos para a aplicação.

1.  **Analisar Funções Não Testadas:** Focar nas `actions` `fetchProcessos`, `fetchProcessoDetalhe`, e `criarProcesso`, que possuem lógica de tratamento de erro e atualização de estado não cobertas.
2.  **Implementar Testes de Erro:** Para cada `action`, simular uma falha na chamada do serviço (`processoService`) e verificar se o `store` trata o erro corretamente (e.g., não quebra a aplicação, atualiza um estado de erro, etc.).
3.  **Verificar Atualizações de Estado:** Garantir que o estado (`processos`, `processoDetalhe`, `isLoading`) é atualizado corretamente após as `actions` serem executadas com sucesso.

### Etapa 2: Aumentar Cobertura do `atividades.ts` Store

Similar ao `processos.ts`, este `store` possui uma quantidade significativa de lógica não testada.

1.  **Testar `actions` Complexas:** Adicionar testes para `importarAtividades`, `salvarAtividade`, `excluirAtividade` e a manipulação de `conhecimentos`.
2.  **Simular Cenários de Falha:** Mockar as chamadas de serviço para falharem e garantir que as `actions` correspondentes lidam com os erros, revertendo o estado ou notificando o usuário conforme necessário.
3.  **Validar `Getters`:** Adicionar testes, se necessário, para os `getters` que dependem do estado modificado por essas `actions`.

### Etapa 3: Aumentar Cobertura do `axios-setup.ts`

Este arquivo tem uma cobertura muito baixa e é fundamental para o tratamento global de erros da API.

1.  **Criar `src/__tests__/axios-setup.spec.ts`:** Se não existir, criar um arquivo de teste para o setup do Axios.
2.  **Testar o Interceptor de Erros:**
    - Mockar uma chamada de API que retorne um erro (e.g., status 500).
    - Verificar se o interceptor captura o erro.
    - Garantir que a `useNotificacoesStore` é chamada com a mensagem de erro correta para notificar o usuário.

### Etapa 4: Finalizar Cobertura dos Serviços e Mappers

1.  **`processoService.ts`:** Adicionar testes para as branches não cobertas, principalmente nos blocos `catch`.
2.  **Outros Serviços:** Revisar `atividadeService.ts` e `usuarioService.ts` para cobrir as linhas restantes.
3.  **Mappers:** Analisar os mappers com baixa cobertura de *branch* (e.g., `unidades.ts`, `mapas.ts`) e adicionar testes para os casos condicionais que não estão sendo validados (e.g., dados de entrada nulos ou opcionais).

## 3. Execução e Validação

A cada etapa concluída, o comando `npm run coverage:unit` será executado para monitorar o progresso. Ao final de todas as etapas, um relatório final será gerado para confirmar que a meta de 95% foi atingida.