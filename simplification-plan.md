# Plano de Simplificação do Backend

## 1. Introdução

Este documento descreve uma análise da arquitetura do backend com o objetivo de identificar áreas de complexidade desnecessária ("over-engineering") e propor simplificações. A análise foi guiada pelo princípio de que os requisitos definidos no diretório `/reqs` são a fonte autoritativa do comportamento esperado do sistema.

A análise concluiu que, em geral, a arquitetura do backend é bem projetada e justifica sua complexidade na maioria das áreas. No entanto, foi identificado um ponto principal de divergência entre a implementação e os requisitos que representa uma oportunidade clara para simplificação.

## 2. Análise da Arquitetura

### 2.1. Arquitetura Orientada a Eventos (`processo`, `alerta`, `notificacao`)

- **Observação:** O sistema utiliza eventos de domínio do Spring (`ApplicationEventPublisher`) para desacoplar o módulo `processo` dos módulos de `alerta` e `notificacao`. Quando um processo é iniciado, ele publica um `ProcessoIniciadoEvento`.
- **Análise:** O `EventoProcessoListener` consome este evento para orquestrar a criação de múltiplos alertas e o envio de e-mails para todos os participantes do processo. Esta lógica de notificação é complexa e poderia impactar a performance da transação principal se fosse executada de forma síncrona e direta.
- **Conclusão:** A arquitetura orientada a eventos é **justificada**. Ela promove um baixo acoplamento, melhora a manutenibilidade ao separar as responsabilidades de negócio das de comunicação, e abre a possibilidade para a execução assíncrona das notificações, tornando a aplicação mais escalável e responsiva. **Nenhuma simplificação é recomendada aqui.**

### 2.2. Divisão de Controladores (`subprocesso`)

- **Observação:** A lógica de API para o `subprocesso` é dividida em quatro classes de `Controller` distintas: `SubprocessoCrudControle`, `SubprocessoCadastroControle`, `SubprocessoMapaControle` e `SubprocessoValidacaoControle`.
- **Análise:** Cada controlador gerencia uma fase distinta e complexa do ciclo de vida do subprocesso:
    - `Crud`: Operações básicas de CRUD.
    - `Cadastro`: O fluxo de trabalho da etapa de registro de atividades.
    - `Mapa`: A manipulação e edição do mapa de competências.
    - `Validação`: O fluxo de trabalho da etapa de validação do mapa.
- **Conclusão:** A divisão dos controladores é **justificada**. Unificar estas classes resultaria em um "God Controller" com dezenas de endpoints, dificultando a navegação e a manutenção do código. A separação atual segue o Princípio da Responsabilidade Única, onde cada classe gerencia um subdomínio coeso do `subprocesso`. **Nenhuma simplificação é recomendada aqui.**

### 2.3. Divisão de Serviços (`mapa`)

- **Observação:** O módulo `mapa` contém múltiplos serviços, incluindo `MapaService`, `CopiaMapaService`, e `ImpactoMapaService`.
- **Análise:** Cada serviço tem uma responsabilidade clara e distinta:
    - `MapaService`: Gerencia o estado de um mapa (CRUD, leitura e a complexa operação de salvar o mapa completo).
    - `CopiaMapaService`: Executa a operação de "cópia profunda" de um mapa e toda a sua hierarquia de entidades filhas.
    - `ImpactoMapaService`: Compara dois mapas (o vigente e o em revisão) para identificar diferenças, correspondendo ao CDU-12.
- **Conclusão:** A separação dos serviços é **justificada**. As responsabilidades são logicamente independentes e complexas. Agrupá-las em um único serviço diminuiria a coesão e a clareza do código. **Nenhuma simplificação é recomendada aqui.**

## 3. Principal Ponto para Simplificação: Implementação do CDU-15

### 3.1. Divergência entre Requisito e Implementação

- **Requisito (`/reqs/cdu-15.md`):** O caso de uso "Manter mapa de competências" descreve um fluxo de interação onde o usuário realiza operações atômicas e individuais: criar uma competência, editar uma competência e excluir uma competência.
- **Implementação:** A memória do projeto e a análise do código (`SubprocessoMapaControle`) confirmam que a funcionalidade foi primariamente implementada através do endpoint `/api/subprocessos/{id}/mapa-completo/atualizar`. Este endpoint utiliza uma estratégia de "salvar o mapa inteiro", onde o frontend envia o estado completo do mapa, e o backend sincroniza a base de dados, inferindo o que foi criado, alterado ou excluído.
- **Análise:** Esta abordagem é significativamente mais complexa do que o necessário. A lógica de sincronização em `MapaService.salvarMapaCompleto` é propensa a erros e menos performática do que operações diretas. O mais importante é que ela **não corresponde ao fluxo de trabalho descrito no requisito**.

### 3.2. A Implementação Correta já Existe

A investigação revelou que o backend **já possui os endpoints corretos** que implementam as operações de CRUD individuais, conforme especificado no CDU-15:
- `POST /api/subprocessos/{id}/competencias` (Adicionar)
- `PUT /api/subprocessos/{id}/competencias/{competenciaId}` (Atualizar)
- `DELETE /api/subprocessos/{id}/competencias/{competenciaId}` (Remover)

Estes endpoints são servidos pelo `CompetenciaService`, que contém a lógica de negócio granular e direta para cada operação.

### 3.3. Plano de Ação para Simplificação

1.  **Frontend:** A equipe de frontend deve ser notificada para refatorar a tela de "Edição de mapa" para utilizar os endpoints de CRUD individuais (`/api/subprocessos/{id}/competencias`) em vez do endpoint de "salvar o mapa inteiro".
2.  **Backend:** Uma vez que o frontend tenha migrado para os novos endpoints, o seguinte pode ser feito no backend para remover o código desnecessário:
    - O endpoint `POST /api/subprocessos/{id}/mapa-completo/atualizar` em `SubprocessoMapaControle` deve ser marcado como obsoleto (`@Deprecated`) e, eventualmente, removido.
    - O método `salvarMapaCompleto` em `MapaService` e seu chamador em `SubprocessoMapaWorkflowService` podem ser removidos, simplificando drasticamente o `MapaService`.
    - Os testes associados a esta funcionalidade de "salvar o mapa inteiro" também devem ser removidos.

## 4. Conclusão

A arquitetura geral do backend é sólida. A principal oportunidade de simplificação reside em alinhar a implementação do CDU-15 com seus requisitos, abandonando a complexa estratégia de "salvar o mapa inteiro" em favor das operações de CRUD individuais que já estão implementadas e disponíveis na API. Isso irá simplificar o código, reduzir a chance de bugs e alinhar o comportamento do sistema com a especificação funcional.