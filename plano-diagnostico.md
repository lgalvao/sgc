# Plano de Implementação do Diagnóstico

## Objetivo

Alinhar a implementação do fluxo de diagnóstico de competências técnicas com os casos de uso revisados `CDU-41` até
`CDU-47`, preservando a integridade histórica do processo mesmo após mudanças na estrutura organizacional e na equipe.

## Matriz resumida

| CDU | Situação | Leitura |
|---|---|---|
| `CDU-41 - Iniciar processo de diagnóstico` | `Parcial` | Processo, subprocessos, notificações e alertas existem, mas o diagnóstico ainda não usa snapshot da equipe no início do processo. |
| `CDU-42 - Realizar autoavaliação` | `Parcial` | Há tela, autosave e conclusão, mas a escala ainda está em `NA + 1..5` e não `NA + 1..6`. O toggle de `Atividade e conhecimentos` também não está claro na implementação atual. |
| `CDU-43 - Acompanhar diagnóstico da unidade` | `Parcial` | A tela de monitoramento existe, mas parte das ações da chefia ainda está concentrada em outra superfície. |
| `CDU-44 - Manter avaliação de consenso` | `Parcial` | O caso de uso foi simplificado corretamente para cobrir criar/visualizar/editar dentro de `manter`, mas a tela atual ainda não implementa a grade completa `Servidor / Chefe / Consenso` nem o autopreenchimento do consenso. |
| `CDU-45 - Aprovar avaliação de consenso` | `Parcial` | A aprovação existe, mas a navegação e a apresentação ainda podem ficar mais aderentes ao caso de uso. |
| `CDU-46 - Indicar impossibilidade de avaliação` | `Aderente` | Modal com justificativa obrigatória, mudança de estado e descarte dos dados parciais já existem. |
| `CDU-47 - Preencher situação de capacitação` | `Parcial` | O comportamento existe com autosave, mas a implementação ainda usa o conceito e o naming de `ocupações críticas`. |

## Decisões de negócio já fechadas

1. O diagnóstico precisa usar snapshot da estrutura relevante e dos servidores participantes no momento do início do
   processo.
2. A implementação deve continuar válida mesmo após mudanças posteriores de lotação, hierarquia e composição da equipe.
3. `Manter avaliação de consenso` já cobre criar, visualizar e editar; não precisamos fragmentar essas ações em casos
   de uso separados.

## Priorização

1. Fluxo e tela de manutenção do consenso
2. Fluxo de monitoramento da chefia
3. Aprovação de consenso pelo servidor
4. Alinhamento de `ocupações críticas` para `situação de capacitação`
5. Fechamento de testes

## Andamento atual

### Entregue neste ciclo

1. Plano inicial consolidado em repositório.
2. Primeiro corte de snapshot no backend:
   - nome do servidor congelado no diagnóstico;
   - unidade do contexto lida pelo snapshot do processo, e não apenas pela unidade viva do subprocesso;
   - registros de capacitação inicializados já na criação do diagnóstico.
3. Escala funcional ajustada para `NA + 1..6` na UI:
   - `NA` tratado como valor explícito, não como `null`;
   - renderização ajustada para mostrar `NA`;
   - cálculo de gap deixando de tratar `NA` como zero real.

### Pendente estrutural do snapshot

1. O diagnóstico ainda inicializa a equipe a partir da lotação viva no momento da abertura.
2. Ainda falta decidir e implementar se a composição da equipe será congelada em tabela própria do diagnóstico ou se o
   vínculo atual em `AvaliacaoServidor`/`OcupacaoCritica` será considerado suficiente como snapshot funcional.
3. Notificações de início para servidores ainda usam leitura viva por lotação.

## Plano por frente

### 1. Snapshot do diagnóstico

Objetivo:
- congelar a equipe participante e a estrutura relevante no início do processo;
- impedir que mudanças organizacionais posteriores invalidem o processo em andamento.

Backend:
- preservar no domínio do diagnóstico os dados mínimos necessários para identidade histórica do servidor e da unidade;
- parar de depender de leituras vivas onde a informação precisa ser histórica;
- criar avaliações individuais e registros de capacitação a partir do estado congelado de abertura;
- revisar consultas do diagnóstico para ler o snapshot como fonte de verdade da equipe do processo.

Impacto esperado:
- elimina dependência da lotação viva;
- garante integridade histórica e reprodutibilidade do diagnóstico.

Estado:
- `parcialmente entregue`

### 2. Escala de avaliação `NA + 1..6`

Objetivo:
- alinhar autoavaliação e consenso ao requisito funcional revisado.

Frontend:
- ajustar opções de nota em autoavaliação e consenso para `NA`, `1`, `2`, `3`, `4`, `5`, `6`.

Backend:
- validar explicitamente os valores aceitos, se isso ainda não estiver protegido em outro nível.

Testes:
- cobrir `NA`;
- cobrir valor `6`;
- cobrir preenchimento completo e conclusão.

Estado:
- `entregue na UI`
- `backend já aceitava 0..6`
- `faltam testes dedicados`

### 3. Manter avaliação de consenso

Objetivo:
- tornar a implementação aderente ao `CDU-44` simplificado.

Frontend:
- manter a ideia de `manter consenso` como superfície única da chefia;
- exibir grade por competência com:
  - valor do servidor;
  - valor do chefe;
  - valor de consenso;
- deixar campos do servidor somente leitura;
- permitir edição dos campos do chefe e do consenso;
- preencher automaticamente o consenso quando servidor e chefe coincidirem;
- manter somente leitura quando o consenso já estiver aprovado.

Backend:
- preservar explicitamente a autoavaliação original do servidor;
- introduzir suporte claro para valores da chefia e do consenso;
- ajustar DTOs e leitura do frontend para a grade tripla.

Próximo corte técnico:
- manter compatibilidade do fluxo atual enquanto a persistência passa a guardar, no mínimo:
  - autoavaliação do servidor;
  - edição da chefia;
  - consenso final.

### 4. Monitoramento da unidade

Objetivo:
- alinhar o monitoramento da chefia ao `CDU-43`.

Frontend:
- usar o monitoramento como lista operacional dos servidores da unidade;
- expor de forma clara as ações cabíveis por situação;
- garantir coerência entre a lista de monitoramento e a tela de manutenção do consenso.

Backend:
- revisar DTOs e estados para que o monitoramento reflita exatamente as situações esperadas do servidor.

### 5. Aprovação de consenso pelo servidor

Objetivo:
- deixar a experiência do servidor mais aderente ao `CDU-45`.

Frontend:
- manter superfície de leitura clara da avaliação de consenso;
- destacar a ação `Aprovar consenso`;
- garantir retorno ao detalhe do subprocesso com mensagem de sucesso.

Backend:
- manter notificação e alerta após aprovação;
- garantir consistência do estado após nova edição do consenso pela chefia.

### 6. Situação de capacitação

Objetivo:
- alinhar o conceito funcional do `CDU-47`.

Frontend:
- revisar textos, rótulos e navegação para falar em `Situação de capacitação`.

Backend:
- decidir se o nome técnico `ocupações críticas` será mantido apenas como legado interno ou se também será migrado.

Observação:
- se o conceito de negócio final for mesmo `situação de capacitação`, o naming atual é fonte de confusão e deve ser
  reduzido progressivamente.

### 7. Regra de conclusão da unidade

Objetivo:
- revisar o fechamento do diagnóstico da unidade com base no modelo final.

Backend:
- confirmar que conclusão só ocorre quando:
  - todos os consensos estiverem aprovados, ou
  - houver impossibilidade formalizada;
  - e as situações de capacitação estiverem preenchidas.

Frontend:
- refletir claramente as pendências que ainda bloqueiam a conclusão.

### 8. Testes

Backend:
- snapshot do diagnóstico no início do processo;
- autoavaliação completa;
- manutenção do consenso;
- reedição após aprovação;
- impossibilidade;
- conclusão da unidade.

Frontend:
- escala `1..6`;
- autosave;
- cache por servidor no consenso;
- readonly após aprovação;
- ações corretas no monitoramento;
- fluxo de capacitação.

## Ordem recomendada de execução

1. Ajustar manutenção do consenso
2. Ajustar monitoramento da chefia
3. Refinar aprovação de consenso pelo servidor
4. Alinhar `situação de capacitação`
5. Reforçar testes

## Próxima etapa prática

Concluir a base do `CDU-44`, começando por preservar explicitamente a autoavaliação original do servidor no backend.
Isso é pré-requisito para:
- exibir `Servidor / Chefe / Consenso` com dados verdadeiros;
- permitir autopreenchimento do consenso sem perder o histórico;
- manter a aprovação do servidor coerente após reedições da chefia.
