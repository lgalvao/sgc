# Diretório de Mocks

Este diretório contém dados e manipuladores (_handlers_) _mockados_ (simulados) para apoiar o desenvolvimento e os testes da aplicação. O objetivo é permitir que o _frontend_ seja desenvolvido e testado de forma isolada, sem depender de um ambiente de _backend_ ativo.

## Conteúdo

1.  **Dados Mockados (`/data`)**:
    - Arquivos (`.ts` ou `.json`) que exportam objetos ou arrays de dados falsos, mas com a mesma estrutura dos DTOs reais do _backend_.
    - Exemplo: `data/usuarios.ts` pode exportar uma lista de objetos `UsuarioDto` falsos.
    - Esses dados são usados para preencher componentes em testes unitários/de componentes e para as respostas dos _handlers_ de API.

2.  **Manipuladores de API (`/handlers`)**:
    - Arquivos que definem os manipuladores de requisições de API, geralmente usando bibliotecas como [Mock Service Worker (MSW)](https://mswjs.io/).
    - Cada _handler_ intercepta uma chamada de API para um _endpoint_ específico (e.g., `GET /api/usuarios`) e retorna uma resposta _mockada_, utilizando os dados do subdiretório `/data`.
    - Os _handlers_ são agrupados por recurso (e.g., `handlers/usuarioHandlers.ts`).

## Casos de Uso

- **Desenvolvimento Isolado**: Permite que os desenvolvedores executem a aplicação e trabalhem em componentes de UI sem a necessidade de executar o servidor _backend_ localmente.
- **Testes Unitários e de Componentes**: Fornece dados consistentes e previsíveis como `props` para os componentes, garantindo que os testes sejam determinísticos.
- **Testes End-to-End (E2E)**: Permite simular diferentes cenários de resposta da API (sucesso, erro, dados vazios) para validar o comportamento do _frontend_ em cada caso.

A configuração para ativar os _mocks_ (e.g., iniciar o MSW) geralmente se encontra nos arquivos de setup dos testes (`vitest.setup.ts`) ou em um ponto de entrada específico para o modo de desenvolvimento _mockado_.