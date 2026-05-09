# Plano de implementação do módulo de Diagnóstico

## 1. Objetivo

Consolidar a implementação do módulo de Diagnóstico do SGC aproveitando a arquitetura já existente de `processo`, `subprocesso`, `mapa`, `organizacao`, `alerta` e `relatorio`, sem quebrar os contratos atuais de mapeamento e revisão.

## 2. Contexto do sistema atual

### 2.1 Estrutura já existente no produto

- O sistema já reconhece o tipo de processo `DIAGNOSTICO`.
- O backend já trata processo e subprocesso como eixo central do workflow por unidade.
- O frontend já diferencia o tipo de processo e já renderiza cards específicos para diagnóstico no detalhamento do subprocesso.
- O banco já possui tabelas para `diagnostico`, `avaliacao_servidor` e `ocupacao_critica`.

### 2.2 Pontos concretos já implementados

- `backend/src/main/java/sgc/processo/model/TipoProcesso.java` já inclui `DIAGNOSTICO`.
- `backend/src/main/java/sgc/subprocesso/model/SituacaoSubprocesso.java` já inclui as situações `DIAGNOSTICO_AUTOAVALIACAO_EM_ANDAMENTO`, `DIAGNOSTICO_MONITORAMENTO` e `DIAGNOSTICO_CONCLUIDO`.
- `backend/src/main/java/sgc/processo/service/ProcessoService.java` já inicia subprocessos de diagnóstico.
- `backend/src/main/java/sgc/subprocesso/service/SubprocessoService.java` já cria subprocesso de diagnóstico copiando o mapa vigente da unidade.
- `backend/src/main/resources/db/schema.sql` já define tabelas e restrições ligadas ao diagnóstico.
- `frontend/src/components/processo/SubprocessoCards.vue` já exibe cards de diagnóstico.

### 2.3 Lacunas do sistema atual frente à especificação preliminar

- Não há um módulo backend explícito `sgc.diagnostico` com controllers, DTOs, entidades, repositórios e services próprios.
- Não há rotas frontend implementadas para `AutoavaliacaoDiagnostico`, `OcupacoesCriticasDiagnostico` e `MonitoramentoDiagnostico`.
- O detalhamento de subprocesso ainda não expõe contexto específico de diagnóstico além dos cards.
- As permissões atuais estão centradas no workflow de mapeamento/revisão e ainda não cobrem integralmente os papéis de SERVIDOR, CHEFE, GESTOR e ADMIN no fluxo novo.
- Não há fluxo completo de consenso, aprovação do servidor, devolução, impossibilidade, validação hierárquica em bloco e homologação final do diagnóstico.
- Não há geração de relatórios de gaps e ocupações críticas nos formatos solicitados.

### 2.4 Tensão entre o contexto atual e a nova especificação

- Hoje o processo global do SGC termina em `FINALIZADO`; a nova especificação fala em `HOMOLOGADO`.
- Hoje o subprocesso de diagnóstico existente no código evolui para `Autoavaliação em andamento -> Monitoramento -> Concluído`; a nova especificação adiciona estados de validação e homologação na cadeia hierárquica.
- Hoje o início do diagnóstico no código já cria subprocesso em `DIAGNOSTICO_AUTOAVALIACAO_EM_ANDAMENTO`; a especificação preliminar diz que o subprocesso nasce em `Não iniciado`.
- O schema existente da tabela `diagnostico` usa situações `EM_ANDAMENTO`, `CONCLUIDO`, `VALIDADO` e `HOMOLOGADO`, mas esse ciclo não está integrado ao workflow atual de `subprocesso`.

## 3. Diretriz de encaixe arquitetural

### 3.1 Papel recomendado de cada módulo

- `processo`: continua responsável por criação, início, prazo global, homologação global e ações em bloco.
- `subprocesso`: continua representando o progresso da unidade, localização atual, permissões estruturadas, movimentações e integração com a cadeia hierárquica.
- `diagnostico`: deve concentrar regras próprias de autoavaliação, consenso, ocupações críticas, cálculo de gaps e consolidação da unidade.
- `organizacao`: continua fornecendo hierarquia, responsáveis, chefias, servidores elegíveis e regras especiais de lotação/perfil.
- `alerta`: deve ser reutilizado para alertas internos e notificações por e-mail.
- `relatorio`: deve concentrar exportação PDF, Excel e CSV do novo módulo.

### 3.2 Estratégia de modelagem recomendada

- Manter `processo` e `subprocesso` como workflow macro.
- Criar um módulo backend dedicado para diagnóstico, integrado aos módulos existentes, em vez de espalhar a lógica nova dentro de `subprocesso`.
- Usar o mapa vigente copiado para o subprocesso como base das competências técnicas avaliadas.
- Tratar a avaliação individual, o consenso e as ocupações críticas como agregados do diagnóstico da unidade.

## 4. Passos necessários para implementação

### 4.1 Fechar o desenho funcional antes de codificar

- Confirmar o ciclo oficial de situações do processo, subprocesso, diagnóstico da unidade e avaliação individual.
- Definir se a homologação final substitui `FINALIZADO` ou se será uma etapa adicional no processo.
- Definir o que permanece em `subprocesso` e o que passa a existir em `diagnostico`.
- Confirmar quais ações terão endpoint próprio e quais continuarão no `ProcessoController` e `SubprocessoController`.

### 4.2 Criar o módulo backend de diagnóstico

- Criar pacote `backend/src/main/java/sgc/diagnostico`.
- Introduzir controllers, DTOs, entidades, enums, repositórios e services do domínio.
- Representar pelo menos:
  - diagnóstico da unidade;
  - avaliações individuais por servidor e competência;
  - consenso;
  - justificativas de devolução e impossibilidade;
  - ocupações críticas;
  - consolidação e cálculo de gaps.
- Alinhar o modelo novo com o schema já existente ou ajustar o schema para refletir corretamente o domínio final.

### 4.3 Integrar diagnóstico ao ciclo de processo e subprocesso

- Ajustar o início de processo de diagnóstico para congelar a árvore de unidades e servidores conforme a especificação.
- Garantir criação dos subprocessos com a situação inicial correta e com cópia do mapa vigente.
- Fazer a transição do subprocesso acompanhar o avanço real do diagnóstico da unidade.
- Implementar alteração de prazo específica para subprocesso/unidade e também para o processo inteiro.
- Implementar homologação global do diagnóstico somente após a cadeia hierárquica estar concluída.

### 4.4 Implementar avaliação individual

- Criar fluxo de autoavaliação do servidor por competência técnica.
- Persistir importância e domínio na escala prevista.
- Implementar regra específica para servidores C13, incluindo definição de cinco competências e soma obrigatória de pesos igual a 10.
- Implementar criação e edição de consenso pela chefia.
- Implementar aprovação do consenso pelo servidor e reabertura obrigatória quando o consenso for alterado após aprovação.
- Implementar impossibilidade de avaliação com justificativa obrigatória.

### 4.5 Implementar monitoramento e validação da unidade

- Implementar preenchimento de ocupações críticas pela chefia.
- Bloquear conclusão da unidade enquanto houver servidor pendente ou ocupações críticas não preenchidas.
- Implementar conclusão da unidade com notificação para a unidade superior.
- Implementar validar, devolver e validar em bloco para o gestor.
- Garantir rastreabilidade em movimentações, histórico analítico, alertas e e-mails.

### 4.6 Ajustar segurança e permissões

- Revisar `@PreAuthorize`, `SgcPermissionEvaluator` e services de acesso para o fluxo de diagnóstico.
- Expor permissões estruturadas para a UI sem reconstruir perfis no frontend.
- Diferenciar claramente ações de SERVIDOR, CHEFE, GESTOR e ADMIN.
- Garantir leitura por hierarquia e escrita por localização também no novo fluxo.

### 4.7 Implementar frontend do diagnóstico

- Criar rotas modulares de diagnóstico.
- Criar views e componentes para:
  - autoavaliação;
  - consenso;
  - aprovação/devolução;
  - ocupações críticas;
  - monitoramento da unidade;
  - acompanhamento hierárquico e validação em bloco.
- Adaptar stores, services e types para os novos contratos.
- Fazer o detalhamento do subprocesso consumir permissões e contexto específicos do diagnóstico.
- Ajustar nomenclatura e textos dos cards conforme a definição funcional final.

### 4.8 Implementar relatórios e pós-processamento

- Calcular gaps por servidor, competência, unidade e consolidações superiores.
- Identificar carência de pessoal capacitado para ocupações críticas.
- Criar exportações PDF, Excel e CSV.
- Garantir que os relatórios só sejam liberados após a homologação definida para o diagnóstico.

### 4.9 Cobrir testes e regressões

- Adicionar testes unitários e de integração do backend para regras de diagnóstico.
- Cobrir permissões, transições, notificações e cálculos.
- Adicionar testes frontend para rotas, telas, cards e fluxos críticos.
- Incluir cenários E2E para autoavaliação, consenso, conclusão, validação, devolução e homologação.
- Revalidar que mapeamento e revisão não sofreram regressão.

## 5. Sequência sugerida de entrega

1. Fechamento do modelo de estados e decisões pendentes.
2. Estrutura backend do módulo `diagnostico`.
3. Integração com `processo` e `subprocesso`.
4. Fluxo de autoavaliação e consenso.
5. Fluxo de ocupações críticas, conclusão e validação.
6. Permissões, alertas e notificações.
7. Frontend completo.
8. Relatórios e cálculos.
9. Cobertura de testes e ajustes finais.

## 6. Ambiguidades e dúvidas em aberto

- A situação final do processo de diagnóstico será `FINALIZADO`, `HOMOLOGADO` ou ambas?
- O subprocesso de diagnóstico deve continuar com o fluxo simples atual ou também ganhar estados intermediários de validação e homologação?
- Na iniciação do processo, o subprocesso de diagnóstico começa em `NAO_INICIADO` ou diretamente em `DIAGNOSTICO_AUTOAVALIACAO_EM_ANDAMENTO`?
- A tabela `diagnostico` já existente será reaproveitada como agregador principal do módulo ou será remodelada?
- O congelamento da árvore no início do processo deve incluir apenas unidades e servidores, ou também responsáveis, chefias, vínculos temporários e mapa vigente copiado?
- A regra de C13 vale para toda autoavaliação do servidor C13 ou apenas para um subconjunto específico de unidades/cargos?
- Quem define as cinco competências e os pesos dos C13 dentro do sistema: ADMIN, chefe da unidade, Secretário ou algum cadastro externo?
- A aprovação do consenso pelo servidor afeta a situação do subprocesso imediatamente ou apenas o estado individual da avaliação?
- A devolução do gestor ocorre no nível do subprocesso inteiro ou pode ocorrer parcialmente por servidor?
- O fluxo alternativo de discordância do servidor exige histórico versionado do consenso anterior?
- A impossibilidade de avaliação exclui o servidor dos cálculos de gap e de ocupação crítica ou apenas permite concluir a unidade com exceção registrada?
- O preenchimento de ocupações críticas será por servidor e competência, por servidor e ocupação, ou por ocupação consolidada da unidade?
- O estado `DIAGNOSTICO_MONITORAMENTO` representa uma etapa operacional com tela, ações e atores próprios, ou apenas uma consolidação intermediária antes do encerramento sem interação adicional?
- A validação em bloco do gestor deve produzir uma única movimentação por unidade, uma movimentação agregada por lote, ou ambas?
- Os relatórios de gaps devem usar domínio exigido do consenso final, da autoavaliação, do mapa vigente ou de outra referência?
- Há necessidade de trilha de auditoria detalhada por edição de autoavaliação, consenso e ocupação crítica?

## 7. Resultado esperado ao final

Ao final da implementação, o SGC deve tratar o diagnóstico como um fluxo de negócio completo, com persistência própria, integração ao ciclo de processo/subprocesso, permissões por papel, telas dedicadas, validação hierárquica, notificações, cálculo de gaps e relatórios consolidados.
