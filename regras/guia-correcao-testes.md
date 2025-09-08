# Lições Aprendidas na Correção de Testes E2E (Playwright)

Este documento resume o processo de depuração e refatoração de testes e2e que apresentava falhas intermitentes e
estruturais. O objetivo é registrar as lições aprendidas para acelerar a resolução de problemas futuros.

## Processo de Correção: Passo a Passo

O trabalho foi dividido em duas fases principais, focando em dois arquivos de teste distintos.

### Fase 1: Correção de Seletores e Boas Práticas (`cad-mapa.spec.ts`)

1. **Análise Inicial:** A primeira análise revelou que os testes falhavam por inconsistências básicas entre o código da
   aplicação e o código do teste.
2. **Detecção de Problemas:**
    * **IDs de Teste (`data-testid`) Incorretos:** O teste buscava por `data-testid="btn-editar-competencia"`, mas o
      componente definia `data-testid="editar-competencia"`.
    * **Seletores Frágeis:** O teste usava seletores de classe CSS (ex: `.alert.alert-info`) para encontrar elementos
      dinâmicos como notificações, o que é uma prática instável.
3. **Ações de Correção:**
    * Padronizamos os `data-testid`s no componente Vue para corresponderem às expectativas do teste.
    * Substituímos o seletor de classe da notificação por um `data-testid` dedicado, tornando o teste mais robusto a
      mudanças de estilo.
4. **Resultado:** As correções resolveram todas as falhas no arquivo, validando a abordagem de usar seletores estáveis.

### Fase 2: Refatoração e Depuração Avançada (`impacto-mapa.spec.ts`)

1. **Análise de Cobertura:** A análise inicial mostrou que o teste principal era excessivamente complexo, testando
   múltiplas funcionalidades (adição, edição, remoção) em um único caso de teste. Além disso, ele continha *
   *expectativas incorretas**, pois verificava o resultado de ações que nunca eram executadas.
2. **Refatoração (Sugestão do Usuário):** Seguindo a sugestão de quebrar o teste gigante, a estratégia foi adotada para
   criar testes atômicos, cada um focado em um único tipo de impacto (ex: `deve exibir impacto de ATIVIDADE REMOVIDA`).
3. **Análise de Dados Mockados:** Para criar os novos testes, foi crucial ler os arquivos (`.json`) de mock para
   entender o estado inicial dos dados e garantir que as manipulações nos testes (ex: alterar uma atividade) tivessem um
   impacto real em uma competência existente.
4. **Depuração Iterativa de Falhas:** Após a refatoração, os novos testes começaram a falhar em cascata, revelando
   problemas mais profundos:
    * **Falha 1 (Ambiguidade de Seletor):** O seletor `hasText: 'Programação em Java'` causou uma **violação de "strict
      mode"** porque correspondia a múltiplos elementos na página ("Programação em Java" e "Programação em
      JavaScript/Vue").
    * **Correção 1:** O seletor foi corrigido para usar uma expressão regular de correspondência exata:
      `hasText: new RegExp(/^Programação em Java$/)`.
    * **Falha 2 (Timeout e Notificações):** Os testes começaram a exceder o tempo limite. A causa raiz era que as *
      *notificações de sucesso (toasts)** apareciam na tela e bloqueavam os cliques do Playwright nos elementos abaixo
      delas.
    * **Correção 2:** Criamos uma função auxiliar `waitForNotification` para esperar ativamente a notificação aparecer e
      depois desaparecer. Isso foi adicionado após cada ação que disparava uma notificação. Também usamos `test.slow()`
      para dar mais tempo para a suíte de testes ser executada.
    * **Falha 3 (Ambiguidade de Seletor de Seção):** A falha de "strict mode" persistiu, mas em um nível mais alto. O
      seletor que identificava a seção da competência impactada era ambíguo, pois o texto usado no filtro correspondia a
      múltiplos cards na tela.
    * **Correção 3:** O seletor da seção foi refinado para ser mais específico, baseando-se na estrutura HTML do
      componente (ex: um card que contém um `.card-header` com um texto exato).
5. **Instabilidade do Ambiente:** Após uma série de correções, todos os testes falharam subitamente por timeout no
   carregamento inicial. Uma nova execução, sem alterações de código, resultou no sucesso de todos os testes, indicando
   que a causa da última falha foi uma instabilidade externa no ambiente de teste.

## Lições Aprendidas

1. **Prefira Testes Atômicos:** Testes grandes que cobrem múltiplos cenários são frágeis, lentos e difíceis de depurar.
   Testes pequenos e focados, cada um validando uma única coisa, são a base de uma suíte de testes saudável.
2. **Seletores Precisos São Cruciais:** Evite seletores ambíguos. A violação de "strict mode" é um sinal claro de
   imprecisão. Use `data-testid` sempre que possível e recorra a expressões regulares (`RegExp`) ou seletores
   estruturais para garantir que você está interagindo com o elemento correto.
3. **Espere Ativamente, Não use Tempos Fixos:** Nunca use esperas com tempo fixo (`waitForTimeout`). Para lidar com
   elementos assíncronos como animações ou notificações, espere por um estado específico (ex:
   `waitFor({ state: 'hidden' })`).
4. **Entenda seus Dados de Teste:** Ao testar funcionalidades complexas, entenda o estado inicial da sua aplicação (seja
   de um banco de dados de teste ou de arquivos de mock). Isso é essencial para escrever asserções corretas e
   significativas.
5. **Considere a Instabilidade do Ambiente:** Se uma suíte de testes estável falhar catastroficamente de forma
   inesperada, considere a possibilidade de um problema externo antes de mergulhar em alterações de código. Uma nova
   execução pode economizar tempo de depuração.