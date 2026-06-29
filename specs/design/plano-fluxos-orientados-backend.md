# Plano de Refatoração para Backend Orientado a Fluxos

## Objetivo

Reduzir a centralidade acidental de `SubprocessoController` e `SubprocessoService` na camada de aplicação, reorganizando o
backend em torno dos fluxos principais do sistema:

- mapeamento-cadastro
- mapeamento-mapa
- revisao-cadastro
- revisao-mapa
- diagnostico

Sem remover `Subprocesso` como conceito central do domínio, da persistência, da segurança e da localização.

## Problema atual

Hoje há um desbalanceamento entre:

- `Subprocesso` como núcleo legítimo do domínio
- `subprocesso` como eixo excessivo de controller e aplicação

Na prática:

- [SubprocessoController.java](/Users/leonardo/sgc/backend/src/main/java/sgc/subprocesso/SubprocessoController.java) concentra uma superfície HTTP muito ampla e heterogênea.
- [SubprocessoService.java](/Users/leonardo/sgc/backend/src/main/java/sgc/subprocesso/service/SubprocessoService.java) mistura:
  - CRUD administrativo
  - criação de subprocessos por tipo de processo
  - manutenção de mapa
  - importação de atividades
  - atualização de situação
  - orquestração transversal
- [DiagnosticoController.java](/Users/leonardo/sgc/backend/src/main/java/sgc/diagnostico/DiagnosticoController.java) já é uma exceção parcial, mas ainda ancorada em `/api/subprocessos/{codSubprocesso}/diagnostico/...`.
- [ProcessoService.java](/Users/leonardo/sgc/backend/src/main/java/sgc/processo/service/ProcessoService.java) continua conhecendo explicitamente múltiplas trilhas operacionais.

O resultado é:

- superfície HTTP inchada
- serviços com responsabilidades misturadas
- leitura difícil do backend por caso de uso
- diagnóstico tratado parcialmente como domínio próprio e parcialmente como apêndice tardio de subprocesso

## Princípio arquitetural

### O que deve continuar centrado em `Subprocesso`

`Subprocesso` continua sendo núcleo estrutural. Faz sentido mantê-lo para:

- entidade persistida
- repositório
- permissão sobre recurso
- localização atual
- histórico e movimentações
- consultas transversais
- contexto-base compartilhado

Isso inclui, por exemplo:

- `SubprocessoRepo`
- `LocalizacaoSubprocessoService`
- `SubprocessoConsultaService` em sua parte transversal
- regras de segurança baseadas em `"Subprocesso"`

### O que deve deixar de ficar centrado em `Subprocesso`

Não faz sentido manter em `SubprocessoController` e `SubprocessoService` a maior parte da orquestração de:

- cadastro
- revisão de cadastro
- mapa
- revisão/mapa ajustado
- diagnóstico

Esses comportamentos devem aparecer na arquitetura como fluxos explícitos.

## Estado-alvo

### Camada HTTP

Substituir progressivamente a borda unificada por controllers orientados a fluxo:

- `MapeamentoCadastroController`
- `RevisaoCadastroController`
- `MapeamentoMapaController`
- `RevisaoMapaController`
- `DiagnosticoController`

Observações:

- `DiagnosticoController` já existe, mas precisa ser incorporado ao mesmo modelo de organização e deixar de parecer uma exceção acoplada a `/api/subprocessos`.
- os endpoints ainda podem continuar aceitando `codSubprocesso`, porque ele é a identidade operacional real do fluxo;
- o ganho não está em esconder o subprocesso, e sim em explicitar a intenção do fluxo.

### Camada de aplicação

Substituir a noção de um `SubprocessoService` amplo por serviços estreitos por fluxo:

- `MapeamentoCadastroAppService`
- `RevisaoCadastroAppService`
- `MapeamentoMapaAppService`
- `RevisaoMapaAppService`
- `DiagnosticoAppService` ou manutenção do conjunto `Diagnostico*Service` com fronteira mais clara

E manter um núcleo transversal enxuto:

- `SubprocessoEstruturaService`
- `SubprocessoConsultaService`
- `LocalizacaoSubprocessoService`
- `SubprocessoFluxoContextoService`
- `SubprocessoTransicaoService` como infraestrutura operacional compartilhada

### Camada de domínio e infraestrutura

Continuam no eixo `subprocesso`:

- entidades
- repositórios
- movimentação
- localização
- permissão
- histórico compartilhado
- contratos transversais de transição

Continuam no eixo `diagnostico`:

- `Diagnostico`
- `AvaliacaoServidor`
- `SituacaoCapacitacao`
- autoavaliação
- consenso
- eventos próprios do diagnóstico

## Diretriz específica para o diagnóstico

O diagnóstico deve entrar no mesmo modelo desde o início do plano.

Ele não deve ser tratado como:

- mero detalhe de subprocesso
- exceção tardia
- módulo isolado sem processo e subprocesso

O modelo desejado é:

- `Processo` continua sendo o ciclo macro também para diagnóstico
- `Subprocesso` continua sendo a execução por unidade também para diagnóstico
- `Diagnostico` continua sendo o agregado especializado do fluxo de diagnóstico

Ou seja:

- processo e subprocesso permanecem como espinha dorsal estrutural;
- diagnóstico deixa de ser “pendurado” na borda de subprocesso e passa a ser uma família explícita de fluxo.

## Estratégia de migração

### Etapa 1. Separar o plano conceitual em duas camadas

Objetivo:

- distinguir claramente o que é núcleo transversal de subprocesso;
- distinguir o que é aplicação orientada a fluxo.

Ações:

- revisar a documentação do módulo `subprocesso`;
- registrar a nova taxonomia de serviços:
  - estrutural
  - consulta transversal
  - transição compartilhada
  - fluxo de cadastro
  - fluxo de mapa
  - fluxo de diagnóstico

Critério de sucesso:

- o time passa a ter um vocabulário estável para o corte;
- “subprocesso” deixa de significar ao mesmo tempo entidade, controller monolítico e todos os casos de uso do sistema.

### Etapa 2. Fatiar a borda HTTP sem mudar semântica

Objetivo:

- reduzir o tamanho lógico de `SubprocessoController` sem alterar comportamento.

Ações:

- extrair os endpoints de cadastro para um controller próprio;
- extrair os endpoints de revisão de cadastro para outro controller ou para o mesmo controller de cadastro com nome explícito de fluxo;
- extrair os endpoints de mapa/validação;
- manter diagnóstico com controller próprio, mas alinhar sua posição no conjunto.

Recomendação prática:

- nesta etapa, os paths HTTP podem continuar compatíveis;
- a separação inicial pode ser só por classe/controller, sem mudança imediata de rota pública.

Critério de sucesso:

- cada controller passa a falar de uma família pequena e coerente de endpoints;
- `SubprocessoController` perde a condição de hub do produto.

### Etapa 3. Esvaziar `SubprocessoService`

Objetivo:

- transformar `SubprocessoService` num serviço estrutural estreito.

Conteúdo que deve sair dele:

- manutenção de mapa orientada a fluxo
- importação de atividades como caso de uso de fluxo
- criação de subprocessos especializada por família de processo
- regras de atualização de situação disparadas por cada fluxo

Conteúdo que pode permanecer:

- criação estrutural de subprocesso
- utilidades administrativas realmente transversais
- operações invariantes de infraestrutura que ainda não pertencem a um fluxo específico

Critério de sucesso:

- o nome `SubprocessoService` volta a descrever um escopo real;
- regras de negócio de fluxo deixam de ficar escondidas num serviço genérico.

### Etapa 4. Explicitar serviços de aplicação por fluxo

Objetivo:

- fazer a leitura do backend acompanhar os fluxos do produto.

Ações sugeridas:

- `MapeamentoCadastroAppService`
  - disponibilizar
  - devolver
  - aceitar
  - homologar
  - ações em bloco correlatas
- `RevisaoCadastroAppService`
  - iniciar
  - cancelar início
  - disponibilizar
  - devolver
  - aceitar
  - homologar
- `MapeamentoMapaAppService`
  - disponibilizar mapa
  - apresentar sugestões
  - validar
  - devolver validação
  - aceitar validação
  - homologar validação
- `RevisaoMapaAppService`
  - mesmas operações com regras próprias de revisão
- `DiagnosticoAppService`
  - concluir
  - devolver
  - validar
  - homologar
  - ações em bloco

Observação:

- não é obrigatório criar exatamente esses nomes;
- o importante é a separação por fluxo e não por “saco genérico de subprocesso”.

### Etapa 5. Reorganizar criação de subprocessos por família de processo

Objetivo:

- tirar de `SubprocessoService` a responsabilidade de saber criar tudo para todos os processos.

Ações:

- separar criação para mapeamento, revisão e diagnóstico em serviços/fábricas orientados a processo;
- manter apenas primitivas compartilhadas de criação estrutural em núcleo transversal;
- para diagnóstico, manter a inicialização do agregado `Diagnostico` como etapa explícita do mesmo pipeline de criação.

Critério de sucesso:

- criar subprocesso de diagnóstico deixa de ser uma variante escondida de um serviço genérico;
- cada processo principal tem pipeline de criação legível.

### Etapa 6. Reorganizar ações em bloco

Objetivo:

- remover de `ProcessoService` a obrigação de conhecer tantos ramos operacionais.

Ações:

- substituir o branching explícito por executores por família de fluxo;
- cada executor implementa aceitar/homologar/disponibilizar em bloco conforme seu domínio;
- diagnóstico entra como executor de primeira classe, não como terceira trilha especial.

Critério de sucesso:

- `ProcessoService` passa a coordenar alto nível;
- o detalhe operacional vai para executores de fluxo.

### Etapa 7. Revisar a taxonomia final das rotas

Objetivo:

- decidir se a API continuará com âncora em `subprocessos/{codigo}` ou se ganhará rotas mais semânticas por fluxo.

Opções aceitáveis:

- manter `subprocessos/{codigo}` como identidade técnica e só separar controllers;
- ou criar rotas mais explícitas por fluxo, se isso trouxer clareza real e não apenas renomeação cosmética.

Recomendação:

- adiar essa decisão até a camada de aplicação estar organizada;
- não misturar refatoração de responsabilidades com grande renomeação de API pública na primeira rodada.

## Ordem recomendada de execução

1. consolidar a documentação-alvo e a taxonomia dos fluxos;
2. extrair controllers por fluxo sem quebrar rotas;
3. esvaziar `SubprocessoService`;
4. explicitar serviços de aplicação por fluxo;
5. reorganizar criação de subprocessos por família de processo;
6. simplificar ações em bloco em `ProcessoService`;
7. só então reavaliar a taxonomia pública final das rotas.

## Primeiro corte recomendado

O primeiro corte de implementação mais seguro é:

- extrair de `SubprocessoController` toda a família de endpoints de cadastro e revisão de cadastro;
- manter os paths atuais;
- criar um controller próprio para esse fluxo;
- fazer o controller novo depender apenas do serviço de fluxo correspondente;
- deixar `SubprocessoController` com menos assuntos misturados.

Razões:

- baixo risco funcional
- ganho imediato de legibilidade
- prepara a próxima etapa sem exigir renomeação de API

## Riscos

- trocar nomes sem reduzir responsabilidades reais;
- criar controllers novos mantendo services monolíticos por trás;
- separar diagnóstico demais e perder o vínculo estrutural com processo/subprocesso;
- tentar resolver tudo numa única rodada e introduzir regressão em permissões, rotas ou testes.

## Critério de sucesso final

Ao fim do plano:

- `Subprocesso` continua sendo o núcleo estrutural do sistema;
- os fluxos principais passam a aparecer explicitamente na borda HTTP e na aplicação;
- diagnóstico entra no mesmo modelo arquitetural dos demais fluxos;
- `SubprocessoController` e `SubprocessoService` deixam de ser centros cognitivos inchados;
- ler o backend por caso de uso volta a ser simples.
