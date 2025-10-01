## Padrões e Boas Práticas para Testes E2E (Playwright)

Este documento consolida as regras, padrões e lições aprendidas para a criação e manutenção de testes E2E com Playwright
no projeto.

### 0. Como/quando executar os testes

* Use `npx playwright test` para rodar todos os testes.
* Para rodar um teste específico, use `npx playwright test e2e/nome-do-teste.spec.ts`.
* Não rode todo os testes frequentemente. Foque em testes específicos durante o desenvolvimento.
* Use `npx playwright test --last-failed` quando estiver corrigindo problemas identificados nos testes.
* **Estratégias de Depuração**: Em caso de falhas inesperadas, especialmente em testes de UI, utilize `page.pause()` dentro do teste para pausar a execução e inspecionar o estado da página no navegador Playwright. Isso é útil para entender por que um elemento não está visível ou uma interação não está ocorrendo como esperado.

### 1. Estrutura e Configuração

* **Localização**: Todos os testes E2E estão no diretório `e2e/`.
* **Estrutura de Arquivos**: Cada arquivo `.spec.ts` deve testar uma funcionalidade específica (ex: `login.spec.ts`,
  `cad-atividades.spec.ts`).
* **Servidor**: O servidor de desenvolvimento já está rodando em segundo plano. Não tente executá-lo novamente.
* **Nao use timeouts**: Nao crie timeouts. Como o backend é mockado, falhas de timeout indicam problemas no seletor
  ou na lógica do teste, ou em problemas no próprio sistema. Não sao problemas em performance ou ma falta de esperas. Se
  precisar que algo apareça antes de agir, use um expect.

### 2. Seletores e Interações

* **Prefira `data-testid`**: Use sempre `data-testid` para selecionar elementos interativos como formulários e botões.
  Se necessário, adicione-os ao código-fonte da aplicação.
* **Evite Seletores Frágeis**: Não use seletores baseados em classes CSS ou IDs gerados dinamicamente.
* **Seletores Precisos e Combinados São Cruciais**: Evite seletores ambíguos. A violação do "strict mode" do Playwright é um sinal
  claro de imprecisão. Quando um seletor genérico (`getByRole`) retorna múltiplos elementos, combine-o com outros localizadores (ex: `page.getByRole('button', {name: 'Aceitar'}).and(page.locator('.btn-primary'))`) ou seletores estruturais para garantir a seleção do elemento correto.

### 3. Lógica de Teste e Asserções

* **Evite Lógica Condicional (`if` statements)**: Testes devem ser determinísticos. Evite `if` statements para controlar o fluxo do teste com base no estado da UI. Em vez disso, crie testes separados para cada cenário ou garanta que a UI esteja no estado esperado antes das asserções. A presença de `if`s pode mascarar problemas e tornar os testes frágeis.
* **Prefira Testes Atômicos**: Testes grandes que cobrem múltiplos cenários são frágeis, lentos e difíceis de depurar.
  Crie testes concisos que validem aspectos específicos.
* **Estratégias de Espera Robustas**: Além de evitar `waitForTimeout`, utilize `page.waitForLoadState('networkidle')` em `beforeEach` ou antes de interações críticas para garantir que a página e todos os seus recursos (incluindo CSS e JS) estejam completamente carregados e estáveis. Isso previne erros de carregamento de recursos (`net::ERR_ABORTED`) e falhas de visibilidade de elementos. Para elementos assíncronos (animações, notificações), espere por um estado específico (ex: `expect(locator).toBeHidden()`).

### 4. Dados Mockados

* **Entenda os Dados**: Antes de escrever asserções, leia os arquivos de mock (`.json`) em `src/mocks/` para entender o
  estado inicial dos dados.
* **Dados Mockados Devem Suportar o Cenário de Teste**: Garanta que os dados mockados reflitam o estado exato necessário para que os elementos da UI estejam visíveis e as interações funcionem conforme o esperado. Se um botão só aparece sob certas condições, o mock deve simular essas condições.
* **Trabalhe com os Dados Existentes**: Não tente interceptar ou mockar dados em tempo de execução. As manipulações nos
  testes devem refletir um impacto real nos dados mockados existentes.
* **Adição de Dados**: Se for necessário, adicione novos dados diretamente aos arquivos canônicos em `src/mocks/`.
  Garanta que os IDs sejam únicos.
* **Compatibilidade de Tipos**: Ao usar dados mockados em código tipado, garanta que o `type cast` corresponda à
  interface de destino, prestando atenção a propriedades opcionais (`?`).

### 5. Padrões de Código e Funções Auxiliares

* **Isolamento de Testes e Setup**: Garanta que cada teste ou grupo de testes seja executado em um ambiente limpo e previsível. Utilize blocos `test.beforeEach` para configurar o estado inicial da aplicação (ex: login, navegação para uma página específica) e garantir que os pré-requisitos do teste sejam atendidos.
* **Funções Auxiliares Robustas**: Crie funções auxiliares para ações repetitivas, mas evite que elas tentem lidar com múltiplas variações de UI ou usem lógicas de fallback genéricas. Funções auxiliares devem ser focadas, usar seletores específicos e falhar diretamente se as pré-condições não forem atendidas. Isso reduz a duplicação, facilita a manutenção e torna os testes mais legíveis e confiáveis.

#### 5.1 Estrutura Modular de Helpers

A estrutura de helpers foi refatorada para seguir o princípio da responsabilidade única:

```
📁 e2e/cdu/helpers/
├── 📁 acoes/         # Ações por domínio
│   ├── acoes-atividades.ts    # Adicionar, editar, remover atividades/conhecimentos
│   ├── acoes-mapa.ts          # Ações relacionadas a mapas de competências
│   ├── acoes-modais.ts        # Interações com modais e confirmações
│   ├── acoes-processo.ts      # Criação, edição, remoção de processos
│   └── index.ts               # Re-exporta todas as ações
├── 📁 verificacoes/  # Verificações por domínio
│   ├── verificacoes-basicas.ts    # Esperas, URLs, elementos visíveis
│   ├── verificacoes-processo.ts   # Verificações específicas de processo
│   ├── verificacoes-ui.ts         # Verificações de interface
│   └── index.ts                   # Re-exporta todas as verificações
├── 📁 navegacao/     # Navegação, login e rotas
│   ├── navegacao.ts           # Funções de navegação e login
│   └── index.ts               # Re-exporta navegação
├── 📁 dados/         # Constantes e dados de teste
│   ├── constantes-teste.ts    # Seletores, textos, URLs, dados mock
│   └── index.ts               # Re-exporta constantes
├── 📁 utils/         # Utilitários gerais
│   ├── utils.ts               # Funções utilitárias (gerar nomes únicos)
│   └── index.ts               # Re-exporta utilitários
└── index.ts          # Ponto de entrada único
```

#### 5.2 Como Usar os Helpers

**Importação centralizada:**
```typescript
import {
    loginComoAdmin,
    navegarParaCadastroAtividades,
    adicionarAtividade,
    verificarElementosPainel,
    SELETORES,
    TEXTOS
} from './helpers';
```

**Exemplo de uso em teste:**
```typescript
test.describe('CDU-08: Manter atividades', () => {
    test.beforeEach(async ({page}) => {
        await loginComoAdmin(page);
    });

    test('deve adicionar atividade', async ({page}) => {
        await navegarParaCadastroAtividades(page, processoId, unidade);
        await adicionarAtividade(page, 'Nova Atividade');
        await verificarElementosPainel(page);
    });
});
```

#### 5.3 Princípios de Organização

* **Responsabilidade Única**: Cada módulo tem uma responsabilidade específica
* **Re-exportação Limpa**: Use `index.ts` para re-exportar funções relacionadas
* **Importação Centralizada**: Importe do `helpers/index.ts` para acesso a tudo
* **Nomenclatura Clara**: Use prefixos como `verificar`, `navegar`, `adicionar`

* **Autenticação e Perfis**: Use as funções de login em `e2e/cdu/helpers/navegacao` antes de cada suíte ou grupo de testes. As funções disponíveis incluem `loginComoAdmin()`, `loginComoGestor()`, `loginComoChefe()`, `loginComoServidor()`. Utilize IDs de servidores válidos que existam nos mocks.
* **Modais e Confirmações**:
    * Sempre aguarde a abertura do modal antes de interagir.
    * Use seletores específicos para os botões dentro do modal.
    * Verifique o fechamento do modal e o resultado da ação (confirmação ou cancelamento).