# Diretório de Testes

Este diretório contém os testes unitários e de componentes para o código-fonte localizado em `src/`. A _framework_ de teste utilizada é o [Vitest](https://vitest.dev/), em conjunto com a [Vue Test Utils](https://test-utils.vuejs.org/) para testes de componentes Vue.

## Objetivo

O objetivo destes testes é garantir a qualidade e a corretude do código de forma isolada. Eles são rápidos de executar e fornecem feedback imediato durante o desenvolvimento, ajudando a prevenir regressões.

## Tipos de Testes

1.  **Testes Unitários (`*.spec.ts` ou `*.test.ts`)**
    - Foco: Testar a menor unidade de código possível de forma isolada.
    - O que é testado:
        - Funções em `utils/`
        - Funções de mapeamento em `mappers/`
        - Validadores em `validators/`
        - Lógica de negócio em `composables/`
        - _Getters_ e _actions_ das _stores_ Pinia em `stores/`
    - Estes testes não devem renderizar componentes nem fazer chamadas de rede reais (as dependências externas são _mockadas_).

2.  **Testes de Componentes (`*.spec.ts` ou `*.test.ts`)**
    - Foco: Testar componentes Vue de forma isolada da aplicação.
    - O que é testado:
        - Renderização correta com base nas `props` recebidas.
        - Emissão de eventos (`emits`) quando ocorrem interações do usuário.
        - Renderização de _slots_.
        - Comportamento reativo e lógica interna do componente.
    - A Vue Test Utils é usada para montar (`mount`) os componentes em um ambiente de teste e para interagir com eles.

## Estrutura e Convenções

- Os arquivos de teste devem ter o mesmo nome do arquivo que estão testando, com o sufixo `.spec.ts` ou `.test.ts`.
- É preferível colocar os arquivos de teste em um diretório `__tests__` dentro da pasta da funcionalidade, mas eles também podem ser agrupados aqui.

## Como Executar

Para executar todos os testes unitários e de componentes, utilize o comando a partir do diretório raiz do _frontend_:

```bash
npm run test:unit
```

Para executar em modo _watch_, que re-executa os testes a cada alteração de arquivo:

```bash
npm run test:unit -- --watch
```