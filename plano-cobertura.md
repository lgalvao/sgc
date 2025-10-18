# Plano de Ação para Aumentar a Cobertura de Testes do Frontend para 95%

## 1. Visão Geral

A cobertura de testes atual do frontend é de **74.09%**. O objetivo é atingir **95%** de cobertura de statements. A análise do relatório de cobertura (`coverage/index.html`) revela que as áreas mais críticas a serem abordadas são:

- **`src/services`**: Cobertura de `6.25%`. Nenhum dos serviços possui testes unitários.
- **`src/mappers`**: Cobertura de `41.74%`. A lógica de mapeamento, crucial para a integridade dos dados entre a API e o frontend, não está devidamente testada.
- **`src/stores`**: Cobertura de `79.61%`. Embora melhor, stores complexos como `processos.ts` e `perfil.ts` têm uma cobertura baixa, arriscando a gestão de estado da aplicação.
- **`src/axios-setup.ts`**: Cobertura de `43.75%`. A configuração do interceptor de erros do Axios não está testada.
- **`src/test-utils`**: Cobertura de `29.21%`. As funções de auxílio para testes não são testadas, o que pode levar a testes não confiáveis.

## 2. Plano Detalhado por Etapas

### Etapa 1: Cobertura dos Serviços (`src/services`)

Esta é a prioridade máxima, dado que a cobertura é quase nula. A ausência de testes aqui significa que a comunicação com o backend é um ponto cego.

1.  **Criar arquivos de teste:** Para cada serviço, será criado um arquivo de teste correspondente em `src/services/__tests__/`.
    - `alertaService.spec.ts`
    - `atividadeService.spec.ts`
    - `painelService.spec.ts`
    - `processoService.spec.ts`
    - `subprocessoService.spec.ts`
    - `usuarioService.spec.ts`
2.  **Mockar o `api` (Axios):** Utilizar `vi.mock('@/axios-setup')` para mockar o módulo do Axios e simular respostas de sucesso e erro da API.
3.  **Implementar testes:** Para cada função em cada serviço, serão criados testes para:
    - Verificar se a chamada da API é feita com a URL e os parâmetros corretos.
    - Simular uma resposta de sucesso e garantir que os dados retornados são os esperados.
    - Simular uma falha (erro de rede, status 4xx/5xx) e garantir que o serviço trata o erro adequadamente (e.g., lança uma exceção ou retorna um valor padrão).

### Etapa 2: Cobertura dos Mappers (`src/mappers`)

A lógica de mapeamento é crítica. Erros aqui podem causar inconsistências de dados em toda a aplicação.

1.  **Expandir testes existentes:** O arquivo `src/mappers/__tests__/mappers.spec.ts` será expandido.
2.  **Testar cada mapper:** Para cada função de mapeamento (e.g., `mapToAlerta`, `mapToProcessoDetalhe`), serão criados testes para:
    - Garantir que um objeto de entrada (DTO da API) é corretamente transformado no objeto de domínio do frontend.
    - Testar casos de borda: campos nulos, vazios ou com formatos inesperados na entrada.
    - Verificar se as transformações (e.g., formatação de datas com `parseISO`) são aplicadas corretamente.

### Etapa 3: Cobertura dos Stores (`src/stores`)

Stores com baixa cobertura, como `processos.ts` e `perfil.ts`, serão o foco.

1.  **Identificar `actions` não testadas:** Analisar os relatórios de cobertura para identificar `actions` e `getters` sem testes.
2.  **`processos.ts`**:
    - Adicionar testes para as actions `fetchProcessos`, `fetchProcessoDetalhe`, `criarProcesso`, etc.
    - Mockar os serviços dependentes (`processoService`).
    - Verificar se o estado (`processos`, `processoDetalhe`) é atualizado corretamente após cada `action`.
    - Testar o tratamento de erros quando os serviços falham.
3.  **`perfil.ts`**:
    - Testar a action `fetchPerfis`.
    - Simular diferentes cenários de resposta do `SgrhService` (e.g., usuário com um ou múltiplos perfis).
    - Verificar se o estado (`perfis`, `unidades`) é populado corretamente.

### Etapa 4: Cobertura de Arquivos Utilitários

1.  **`axios-setup.ts`**:
    - Criar `src/__tests__/axios-setup.spec.ts`.
    - Testar o interceptor de erros. Simular uma chamada de API que retorna um erro (e.g., 401, 500) e verificar se o `useNotificacoesStore` é chamado para exibir a notificação de erro.
2.  **`src/test-utils/helpers.ts` e `uiHelpers.ts`**:
    - Criar arquivos de teste para validar a lógica das funções auxiliares, garantindo que elas funcionem como esperado e não introduzam erros nos próprios testes.

## 3. Execução e Validação

A cada etapa concluída, o comando `npm run coverage:unit` será executado para monitorar o progresso da cobertura. Ao final de todas as etapas, um relatório final será gerado para confirmar que a meta de 95% foi atingida.