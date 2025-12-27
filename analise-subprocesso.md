# Análise do Pacote `subprocesso`

Esta análise detalha a estrutura atual do pacote `sgc.subprocesso`, identifica problemas de complexidade e sobreposição de responsabilidades, e sugere refatorações para simplificar a manutenção.

## 1. Visão Geral

O pacote `subprocesso` é um dos módulos centrais do sistema SGC, gerenciando o ciclo de vida do mapeamento de competências para uma unidade específica. Ele implementa uma máquina de estados complexa que reflete os requisitos funcionais descritos nos Casos de Uso, especificamente:
- **Fase de Cadastro**: CDU-11 (Cadastro de Atividades) e CDU-13 (Análise de Cadastro).
- **Fase de Mapa**: CDU-15 (Manter Mapa), CDU-16 (Ajuste de Mapa) e CDU-17 (Disponibilizar Mapa).

Embora a divisão de classes espelhe essa separação funcional, a implementação atual apresenta sinais claros de **fragmentação excessiva** e **duplicação de lógica** (Boilerplate) nos fluxos de aprovação e transição.

## 2. Inventário Atual

### Controladores
O módulo expõe sua API através de 4 controladores principais:
- **`SubprocessoCrudController`**: Operações básicas (listar, buscar por ID).
- **`SubprocessoCadastroController`**: Gerencia o fluxo da etapa de "Cadastro" (disponibilizar, devolver, aceitar, homologar), atendendo ao CDU-13.
- **`SubprocessoMapaController`**: Gerencia operações relacionadas ao mapa (ajuste, validação, sugestões), atendendo aos CDU-15, CDU-16 e CDU-17.
- **`SubprocessoValidacaoController`**: Endpoints específicos para validação de regras de negócio.

### Serviços
A lógica de negócios está distribuída em pelo menos 12 serviços:
- **Core/Genéricos**: `SubprocessoService`, `SubprocessoConsultaService`, `SubprocessoContextoService`, `SubprocessoFactory`, `SubprocessoPermissoesService`.
- **Workflow/Estado**: `SubprocessoCadastroWorkflowService`, `SubprocessoMapaWorkflowService`, `SubprocessoTransicaoService`.
- **Suporte/Outros**: `SubprocessoDtoService`, `SubprocessoEmailService`, `SubprocessoComunicacaoListener`, `SubprocessoMapaService`.

## 3. Problemas Identificados

### 3.1. Duplicação de Lógica de Workflow
Os requisitos (CDU-13, CDU-17) exigem um padrão recorrente para aprovações e devoluções:
1.  Validar estado atual.
2.  Criar registro de `Analise` (histórico).
3.  Atualizar `Situacao` do Subprocesso.
4.  Registrar `Movimentacao` (transição).
5.  Enviar Notificação e Alerta.

**Problema**: Essa sequência é reimplementada, com pequenas variações, em `SubprocessoCadastroWorkflowService` (para cadastro) e `SubprocessoMapaWorkflowService` (para mapa).
**Impacto**: Alterações nas regras de notificação ou logging exigem mudanças em múltiplos arquivos, aumentando o risco de inconsistências.

### 3.2. Sobreposição de Responsabilidades (DTOs vs Core)
- **Problema**: O `SubprocessoCadastroController` chama `subprocessoDtoService.obterCadastro(codigo)`. No entanto, a classe `SubprocessoService` também implementa lógica de busca.
- **Impacto**: A existência de um `DtoService` separado cria confusão sobre onde reside a lógica de transformação de dados.

### 3.3. Fragmentação de Consultas (`ConsultaService`)
- **Problema**: `SubprocessoConsultaService` atua como um wrapper fino (thin wrapper) sobre o `SubprocessoRepo`.
- **Análise**: O `SubprocessoService` já realiza buscas de entidades. Ter um serviço dedicado apenas para encapsular o `findById` do repositório adiciona uma camada de indireção desnecessária.

### 3.4. Complexidade dos Controladores
- Os controladores contêm lógica de orquestração (ex: extração de usuário, sanitização HTML) que infla as classes e acopla a camada Web a múltiplos serviços de backend (`usuarioService`, `subprocessoService`, `workflowService`).

## 4. Sugestões de Refatoração

### 4.1. Consolidação do Workflow (Extração de Executor)
**Ação**: Centralizar a lógica de transição de estado.
**Como**:
- Criar um componente `SubprocessoWorkflowExecutor` ou `WorkflowActionService` que encapsule o padrão "Analisar -> Mover -> Notificar".
- Os serviços específicos (`CadastroWorkflow`, `MapaWorkflow`) passariam apenas os parâmetros variáveis (Tipo de Análise, Próximo Estado, Tipo de Transição) para este executor comum, eliminando a duplicação de código.

### 4.2. Unificação de Serviços de Leitura
**Ação**: Eliminar `SubprocessoConsultaService` e `SubprocessoDtoService`.
**Como**:
- Mover métodos de busca de entidade (`getSubprocesso`) para o `SubprocessoService`.
- Centralizar a montagem de DTOs em `SubprocessoService` ou em Mappers dedicados, tornando o `SubprocessoService` a fachada principal para consultas.

### 4.3. Simplificação do `SubprocessoService` (Facade Pattern)
**Ação**: Transformar `SubprocessoService` em uma Facade de alto nível.
**Como**:
- O `SubprocessoService` deve ser o ponto de entrada principal, delegando para componentes internos quando necessário, mas escondendo a complexidade de ter 12 serviços diferentes dos controladores.

### 4.4. Manutenção da Separação dos Controladores
**Observação**: A separação entre `CadastroController` e `MapaController` faz sentido dado que atendem a atores e momentos diferentes (Unidade vs Admin).
**Melhoria**: Reduzir o acoplamento destes controladores fazendo-os depender apenas da Facade (`SubprocessoService`) ou dos serviços de Workflow consolidados, em vez de injetar dependências granulares.

## 5. Plano de Ação Recomendado

1.  **Refatorar Workflow**: Extrair método comum `registrarAnaliseETransicao(...)` para reutilização entre as fases de Cadastro e Mapa.
2.  **Eliminar Middle Men**: Remover `SubprocessoConsultaService` e `SubprocessoDtoService`, migrando lógica para `SubprocessoService`.
3.  **Padronizar Entradas**: Garantir que validações e sanitizações sejam feitas de forma consistente, preferencialmente via anotações ou interceptors, limpando os controladores.

Essa abordagem respeita a complexidade inerente aos requisitos do negócio (máquina de estados multifacetada) enquanto remove a complexidade acidental (código repetido e fragmentação de classes).
