# Testes End-to-End (E2E)

Esta pasta contém os testes End-to-End (E2E) do projeto, que são responsáveis por simular a interação do usuário com a aplicação de ponta a ponta.

## Estrutura dos Testes

Os testes são escritos utilizando o [Playwright](https://playwright.dev/) e estão organizados da seguinte forma:

- **`cdu-XX.spec.ts`**: Cada arquivo `spec.ts` corresponde a um Caso de Uso (CDU) documentado na pasta `/reqs`. Esses arquivos contêm os testes que verificam o comportamento esperado da aplicação para um determinado CDU.

- **`cdu-XX-prep.spec.ts`**: Alguns CDUs requerem um estado específico da aplicação para serem testados. Os arquivos `-prep.spec.ts` são responsáveis por criar os dados necessários para esses testes. Eles geralmente contêm hooks `beforeAll` e `afterAll` para garantir que o ambiente de teste seja configurado e limpo corretamente.

- **`support/`**: Esta pasta contém arquivos de configuração global para os testes.
    - **`vue-specific-setup.ts`**: Estende o executor de testes do Playwright com configurações específicas para o Vue.js, como a interceptação de requisições e o monitoramento de erros do Vue.
    - **`global-setup.ts`**: (Atualmente não utilizado) Contém a lógica para popular o banco de dados antes da execução dos testes.

- **`helpers/`**: Esta pasta contém uma série de funções auxiliares que são utilizadas para abstrair as interações com a UI e a API, tornando os testes mais legíveis e fáceis de manter.

## Helpers

Os helpers estão organizados em subpastas de acordo com a sua funcionalidade:

- **`acoes/`**: Contém funções que executam ações na UI, como clicar em botões, preencher formulários e interagir com modais.
- **`dados/`**: Contém dados de teste, como usuários, processos e seletores de UI.
- **`navegacao/`**: Contém funções para navegar entre as diferentes páginas da aplicação.
- **`setup/`**: Contém funções para configurar o ambiente de teste, como a criação de dados via API.
- **`utils/`**: Contém funções utilitárias, como a geração de nomes únicos e o tratamento de datas.
- **`verificacoes/`**: Contém funções para verificar o estado da UI, como a visibilidade de elementos e o conteúdo de mensagens.

## Como Escrever Novos Testes

Para escrever um novo teste, siga os seguintes passos:

1. Crie um novo arquivo `cdu-XX.spec.ts` na pasta `e2e/`, onde `XX` é o número do Caso de Uso que você está testando.
2. Se o teste exigir dados específicos, crie um arquivo `cdu-XX-prep.spec.ts` para configurar o ambiente de teste.
3. Importe as funções auxiliares necessárias da pasta `e2e/helpers`.
4. Escreva os testes utilizando a sintaxe do Playwright.
5. Utilize as funções de verificação para garantir que a aplicação se comporta como esperado.
