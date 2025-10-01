# Resumo dos Testes E2E por Caso de Uso (CDU)

## Visão Geral

Este documento apresenta um resumo dos 21 casos de uso (CDU) do sistema, detalhando a estrutura e a filosofia dos testes end-to-end implementados com Playwright.

O objetivo principal é garantir que cada arquivo de teste (`spec.ts`) seja uma **narrativa de usuário legível**, livre de detalhes técnicos, enquanto a complexidade e a robustez são encapsuladas em camadas de abstração (helpers).

## Estrutura dos Testes por CDU

(A lista de CDUs e suas funcionalidades permanece a mesma...)

### CDU-01: Realizar login e exibir estrutura das telas
- **Funcionalidades testadas:** Login, tratamento de erros, logout e visibilidade de componentes por perfil (ADMIN/SERVIDOR).

### CDU-02: Visualizar Painel
- **Funcionalidades testadas:** Controles de visibilidade por perfil, ordenação de tabelas, filtros e navegação.

... (demais 20 CDUs listados de forma concisa) ...

### CDU-21: Finalizar processo
- **Funcionalidades testadas:** Bloqueio de finalização, modal de confirmação, sucesso e notificações.

## Arquitetura Semântica em 3 Camadas

A estrutura dos testes foi desenhada para separar responsabilidades, maximizar a legibilidade e facilitar a manutenção. 

### Camada 1: Dados (`helpers/dados/constantes-teste.ts`)

É a fonte única da verdade para todos os dados estáticos da UI.

- **Responsabilidade:** Centralizar seletores (`SELETORES`), textos visíveis (`TEXTOS`), URLs (`URLS`) e rótulos (`ROTULOS`).
- **Regra de Ouro:** Nenhuma "string mágica" (seletores ou textos da UI) deve existir fora deste arquivo. Qualquer mudança na UI exige a atualização de apenas uma constante.

### Camada 2: Linguagem de Domínio (`helpers/`)

Esta camada traduz as intenções de negócio e as ações do usuário em código executável. É onde a lógica e a complexidade dos testes residem.

- **Responsabilidade:** Encapsular toda a interação com o Playwright (`page.locator`, `expect`, etc.).
- **Estrutura:**
  - `acoes/`: Funções que executam ações de negócio (ex: `criarProcessoCompleto`, `registrarAceiteRevisao`).
  - `verificacoes/`: Funções que validam estados da aplicação (ex: `verificarProcessoFinalizadoNoPainel`, `verificarModalVisivel`).
  - `navegacao/`: Funções para login e navegação entre páginas (ex: `loginComoGestor`, `navegarParaCadastroAtividades`).
  - `utils/`: Utilitários genéricos e robustos para interações, como `clicarElemento` e `preencherCampo`, que lidam com múltiplos seletores de fallback.

### Camada 3: Especificações (`cdu-xx.spec.ts`)

Cada arquivo de teste é a representação de um cenário de usuário, escrito de forma declarativa.

- **Responsabilidade:** Orquestrar chamadas aos helpers da Camada 2 para contar uma história.
- **Regra de Ouro:** Arquivos `.spec.ts` são **proibidos** de conter qualquer lógica de controle (`if/else`, `try/catch`) ou chamadas diretas ao Playwright (`expect`, `page.locator`, `page.getBy...`).
- **Exemplo de um teste:**
  ```typescript
  test('Deve registrar o aceite do gestor com sucesso', async ({page}) => {
      await acessarAnaliseRevisaoComoGestor(page);
      await registrarAceiteRevisao(page, 'Cadastro aprovado pelo gestor.');
      await verificarAceiteRegistradoComSucesso(page);
  });
  ```

## Princípios de Manutenção e Refatoração

- **A Complexidade Pertence aos Helpers:** Se uma ação ou verificação exige lógica condicional, múltiplos seletores ou tratamento de erros, essa complexidade deve ser encapsulada em um helper na Camada 2.
- **Reutilize os Utilitários de Fallback:** As funções em `refactoring-utils.ts` (`localizarElemento`, `clicarElemento`, `preencherCampo`) devem ser usadas para criar ações e verificações resilientes, evitando a repetição de código.
- **Mantenha a Narrativa:** Ao escrever ou refatorar um teste, leia-o em voz alta. Se não soar como uma sequência de ações de um usuário, ele precisa ser simplificado.

## Cobertura e Características

(As seções de Cobertura de Perfis, Tipos de Processo, Fluxos, Características Técnicas e Cobertura Funcional permanecem válidas.)

## Conclusão

A arquitetura de testes atual prioriza a clareza e a manutenibilidade, tratando os testes como documentação viva dos casos de uso do sistema. A refatoração dos helpers consolidou a robustez das interações em utilitários reutilizáveis, simplificando drasticamente a camada de especificação.
