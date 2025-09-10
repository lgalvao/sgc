# Gui para Correção de Testes E2E (Playwright)

Este documento resume o processo de depuração e refatoração de testes e2e e registra lições aprendidas para acelerar a resolução de problemas futuros.

## Lições aprendidas

* **Prefira Testes Atômicos:** Testes grandes que cobrem múltiplos cenários são frágeis, lentos e difíceis de depurar.
* **Seletores Precisos São Cruciais:** Evite seletores ambíguos. A violação de "strict mode" é um sinal claro de imprecisão. Use `data-testid` sempre que possível e recorra a expressões regulares (`RegExp`) ou seletores estruturais para garantir que você está interagindo com o elemento correto.
* **Espere Ativamente, Não use Tempos Fixos:** Nunca use esperas com tempo fixo (`waitForTimeout`). Para lidar com
   elementos assíncronos como animações ou notificações, espere por um estado específico (ex:
   `waitFor({ state: 'hidden' })`).
* **Entenda seus Dados de Teste:** Ao testar funcionalidades complexas, entenda o estado inicial da sua aplicação (seja de um banco de dados de teste ou de arquivos de mock). Isso é essencial para escrever asserções corretas e significativas.
* **Análise de Dados Mockados:** Para criar os novos testes, foi crucial ler os arquivos (`.json`) de mock para entender o estado inicial dos dados e garantir que as manipulações nos testes (ex: alterar uma atividade) tivessem um impacto real em uma competência existente.
* **Compatibilidade de Tipos com Dados Mockados:** Ao mapear dados mockados sem tipo (ex: de arquivos JSON) para interfaces fortemente tipadas, garanta que o `type cast` para os dados mockados corresponda precisamente ao tipo de entrada esperado da função de parsing. Preste atenção especial às propriedades opcionais (`?`) versus obrigatórias na interface de destino, especialmente para arrays.
* **Tratamento de Notificações em Testes Playwright:** Notificações (toasts) são difíceis de testar devido à sua natureza transitória.
    * Sempre use uma função auxiliar dedicada (ex: `waitForNotification`) que espere ativamente a notificação *aparecer* e depois *desaparecer*.
    * Esta função auxiliar deve ser flexível o suficiente para aceitar diferentes seletores (ex: `data-testid` ou nomes e classe) para garantir a robustez.    