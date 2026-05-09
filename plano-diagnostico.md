# Plano de implementação do módulo de diagnóstico

## 1. Objetivo

Implementar o módulo de Diagnóstico de Competências no SGC aproveitando a arquitetura já existente de processo, subprocesso, mapa, organização, alerta, relatório e segurança, sem criar um fluxo paralelo ao restante do sistema.

## 2. Contexto atual do sistema

### 2.1 Capacidades já existentes que ajudam o módulo

- O sistema já suporta `TipoProcesso.DIAGNOSTICO` no backend e no frontend.
- O início de processos de diagnóstico já cria subprocessos por unidade e copia o mapa vigente da unidade para o subprocesso.
- O workflow de subprocesso já possui três situações de diagnóstico: `DIAGNOSTICO_AUTOAVALIACAO_EM_ANDAMENTO`, `DIAGNOSTICO_MONITORAMENTO` e `DIAGNOSTICO_CONCLUIDO`.
- A tela de subprocesso já exibe cards específicos de diagnóstico no frontend.
- A exclusão administrativa em homologação já prevê tabelas futuras de diagnóstico (`diagnostico`, `avaliacao_servidor`, `ocupacao_critica`), o que indica uma modelagem parcialmente antecipada.

### 2.2 Restrições e padrões que o novo módulo precisa respeitar

- O backend é um monólito modular organizado por domínio, com controllers, DTOs, entidades/repos e services especializados.
- O `subprocesso` continua sendo o núcleo do workflow por unidade e das permissões estruturadas consumidas pela UI.
- Leitura depende de hierarquia e escrita depende da localização atual do subprocesso; novas ações do diagnóstico precisam entrar nesse mesmo modelo.
- Para revisão e diagnóstico, somente unidades com mapa vigente podem participar do processo.
- O frontend espera respostas já orientadas à interface e usa rotas modulares, stores Pinia setup e componentes Vue com `<script setup lang="ts">`.

### 2.3 Lacunas já visíveis no repositório

- Ainda não existe um domínio backend explícito para diagnóstico com entidades, controllers e services próprios.
- Os cards de diagnóstico no frontend já navegam para rotas nomeadas, mas essas rotas e telas ainda não estão implementadas.
- A documentação de acesso já descreve apenas o fluxo macro do diagnóstico, mas não detalha permissões finas para autoavaliação, consenso, impossibilidade, ocupações críticas, conclusão e validação.
- Há suporte inicial para abrir processos de diagnóstico, porém faltam persistência, regras de negócio, notificações, relatórios e fluxo hierárquico completo do módulo.

## 3. Encaixe do módulo na arquitetura atual

### 3.1 Backend

O módulo deve nascer como um novo domínio `sgc.diagnostico`, reutilizando:

- `sgc.processo` para criação, início, alteração de prazo, finalização/homologação e ações em bloco;
- `sgc.subprocesso` para situação por unidade, localização atual, histórico e permissões estruturadas;
- `sgc.organizacao` para snapshot de unidades, servidores, chefias e hierarquia;
- `sgc.alerta` e notificações por e-mail para efeitos colaterais do workflow;
- `sgc.relatorio` para exportações e cálculos pós-processamento.

### 3.2 Frontend

O frontend deve incorporar o módulo como extensão natural do fluxo de subprocesso:

- novas rotas para autoavaliação, consenso/monitoramento e ocupações críticas;
- views próprias de diagnóstico;
- serviços HTTP específicos;
- tipos TypeScript alinhados aos DTOs do backend;
- reaproveitamento do detalhe do subprocesso como ponto de entrada principal.

## 4. Passos para implementação

### Etapa 1 — Fechar o desenho funcional e de dados

1. Consolidar a máquina de estados do diagnóstico nos três níveis: processo, subprocesso e servidor.
2. Mapear quais dados pertencem ao processo, ao subprocesso, ao servidor e à competência.
3. Definir quais informações precisam ser congeladas no início do processo:
   - árvore de unidades;
   - responsáveis e chefias;
   - servidores participantes;
   - mapa de competências técnicas vigente da unidade.
4. Definir o recorte exato do fluxo por ator:
   - ADMIN inicia, altera prazo e homologa;
   - SERVIDOR realiza autoavaliação e aprova ou devolve consenso;
   - CHEFE cria/edita consenso, marca impossibilidade, preenche ocupações críticas e conclui a unidade;
   - GESTOR valida, devolve e valida em bloco.

### Etapa 2 — Modelar persistência e migrações

1. Criar as tabelas e relacionamentos do domínio de diagnóstico.
2. Separar claramente:
   - entidade raiz do diagnóstico por subprocesso;
   - avaliações individuais por servidor e competência;
   - registro de consenso e suas reaberturas;
   - impossibilidades com justificativa;
   - ocupações críticas;
   - eventuais snapshots organizacionais necessários.
3. Garantir compatibilidade com exclusão completa de processo e com auditoria/histórico já existente.
4. Definir índices e unicidades para evitar duplicidade de avaliação por servidor, subprocesso e competência.

### Etapa 3 — Completar o workflow backend

1. Implementar services de domínio para:
   - preparar o diagnóstico ao iniciar o processo;
   - carregar o formulário individual a partir do mapa da unidade;
   - salvar autoavaliação;
   - criar e editar consenso;
   - aprovar ou devolver consenso;
   - registrar impossibilidade;
   - salvar ocupações críticas;
   - concluir diagnóstico da unidade;
   - validar, devolver e validar em bloco;
   - homologar diagnóstico.
2. Integrar as transições com o módulo `subprocesso`, incluindo movimentações e localização atual.
3. Expandir validações de pré-condição para impedir avanço com pendências individuais ou ocupações críticas não preenchidas.
4. Ajustar a finalização/homologação do processo para respeitar o novo fechamento hierárquico do diagnóstico.

### Etapa 4 — Completar segurança e permissões

1. Definir novas ações de permissão para todas as operações do diagnóstico.
2. Atualizar `SgcPermissionEvaluator`, controllers e cálculo de permissões estruturadas da UI.
3. Diferenciar:
   - ações do servidor sobre si mesmo;
   - ações da chefia sobre sua unidade;
   - validações da cadeia hierárquica;
   - ações administrativas globais.
4. Garantir que a UI mostre ou desabilite ações seguindo o mesmo padrão já usado nos demais módulos.

### Etapa 5 — Implementar contratos HTTP e integração frontend

1. Criar endpoints REST do módulo com DTOs específicos.
2. Incluir os dados de diagnóstico necessários no contexto do subprocesso e nas respostas detalhadas.
3. Criar services frontend para:
   - obter formulário de autoavaliação;
   - enviar autoavaliação;
   - obter/salvar consenso;
   - aprovar ou devolver consenso;
   - registrar impossibilidade;
   - obter/salvar ocupações críticas;
   - concluir unidade;
   - validar, devolver e homologar.
4. Adicionar tipos e mapeadores TypeScript correspondentes.

### Etapa 6 — Implementar telas e navegação

1. Registrar as rotas que hoje já são referenciadas pelos cards de diagnóstico.
2. Criar as views de:
   - autoavaliação de diagnóstico;
   - monitoramento/consenso;
   - ocupações críticas.
3. Ajustar a experiência da tela de subprocesso para refletir o estado do diagnóstico por perfil.
4. Exibir pendências, justificativas, bloqueios de avanço e histórico de devoluções de forma consistente com o padrão atual do sistema.

### Etapa 7 — Notificações, alertas e rastreabilidade

1. Disparar e-mails e alertas internos nos momentos definidos pela especificação:
   - início do processo;
   - conclusão de autoavaliação;
   - criação/edição de consenso;
   - devolução para ajuste;
   - conclusão da unidade;
   - validação em bloco;
   - alteração de prazo;
   - homologação final.
2. Registrar movimentações e histórico analítico com descrições próprias do diagnóstico.
3. Padronizar mensagens para manter consistência com o restante do SGC.

### Etapa 8 — Relatórios e pós-processamento

1. Implementar os cálculos de gaps por servidor, competência, unidade e consolidação superior.
2. Implementar a consolidação de ocupações críticas por unidade e cadeia hierárquica.
3. Expor exportações em PDF, Excel e CSV no domínio de relatórios.
4. Garantir que os cálculos só sejam liberados após a homologação do diagnóstico.

### Etapa 9 — Testes e cobertura

1. Criar testes de unidade para regras do domínio de diagnóstico.
2. Criar testes de controller para contratos HTTP e segurança.
3. Criar testes de integração cobrindo o fluxo completo do diagnóstico.
4. Criar testes de frontend para rotas, views, formulários e estados de permissão.
5. Criar ou expandir cenários E2E para o fluxo ponta a ponta por perfil.

### Etapa 10 — Implantação incremental

1. Entregar primeiro a fundação do domínio e o fluxo individual mínimo.
2. Entregar depois consenso, impossibilidade e ocupações críticas.
3. Em seguida liberar conclusão da unidade, validação hierárquica e homologação.
4. Finalizar com relatórios, exportações e ajustes operacionais.

## 5. Sequência sugerida de execução

1. Modelagem de dados e migrações.
2. Permissões e workflow backend.
3. Endpoints e DTOs.
4. Views e rotas do frontend.
5. Notificações e alertas.
6. Relatórios e exportações.
7. Testes integrados e E2E.

## 6. Ambiguidades e dúvidas em aberto

1. **Snapshot organizacional:** o requisito fala em copiar a árvore de unidades e servidores no início, mas não define onde esse snapshot será persistido nem se ele substitui leituras futuras da estrutura viva.
2. **Escopo do processo por unidade:** não está explícito se unidades intermediárias sem equipe apenas validam subordinadas ou também podem ter diagnóstico próprio.
3. **Servidor participante:** falta a regra para servidores admitidos, removidos, movimentados ou afastados depois do início do processo.
4. **Formulário de diagnóstico:** o texto indica `Importância` e `Domínio` por competência, mas não explicita se ambos são preenchidos pelo servidor em todos os casos ou se parte do dado pertence apenas ao consenso da chefia.
5. **Regra C13 da IN 68:** a especificação menciona 5 competências com pesos somando 10, mas não define onde essas competências são escolhidas, quem configura os pesos e como isso convive com o formulário geral por unidade.
6. **Modelo do consenso:** não está claro se o consenso substitui integralmente a autoavaliação original, se mantém trilha comparativa campo a campo ou se possui versão própria por reabertura.
7. **Impossibilidade de avaliação:** falta definir se o servidor impossibilitado sai totalmente dos cálculos de gaps, relatórios e ocupações críticas ou se entra como categoria separada.
8. **Ocupações críticas:** a especificação fala em situação de capacitação por servidor, mas o nome sugere visão por ocupação; falta definir o objeto principal da funcionalidade e a fonte do cadastro de ocupações.
9. **Conclusão da unidade:** o documento exige todos os servidores concluídos ou impossibilitados, mas não detalha como tratar consenso devolvido, servidor sem chefia ou unidade sem equipe ativa.
10. **Validação em bloco:** falta definir se uma unidade inválida bloqueia todo o lote ou se o sistema processa parcialmente e retorna o resultado por unidade.
11. **Alteração de prazo:** o requisito menciona prazo por unidade ou por processo, mas não esclarece se isso altera `dataLimiteEtapa1`, uma nova data do diagnóstico ou ambos.
12. **Homologação final:** o texto diz que a homologação ocorre após todas as validações hierárquicas, mas não define se isso fecha o processo inteiro ou apenas libera relatórios mantendo o processo em andamento.
13. **Relatórios de gaps:** a fórmula informada é resumida e não explica tratamento para `NA`, pesos, impossibilidades, consenso devolvido e consolidação hierárquica.
14. **Permissões do servidor:** a especificação funcional exige ações individuais, porém o modelo atual de permissões do sistema foi pensado principalmente para ADMIN, GESTOR e CHEFE em subprocessos de unidade; será necessário explicitar o recorte de escrita do SERVIDOR.
15. **UI já parcialmente preparada:** os cards de diagnóstico já existem, mas a ausência de rotas e telas sugere que é preciso decidir se o módulo será entregue dentro do fluxo atual de subprocesso ou em uma navegação dedicada.

## 7. Resultado esperado ao final

Ao concluir este plano, o SGC deve tratar diagnóstico como um terceiro fluxo completo de processo, com persistência própria, segurança alinhada ao modelo atual, telas por perfil, validação hierárquica, notificações, relatórios e rastreabilidade equivalentes aos módulos de mapeamento e revisão.
