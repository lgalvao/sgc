# Diagnóstico Arquitetural do Módulo de Diagnóstico

## Objetivo

Mapear onde o módulo de diagnóstico está corretamente isolado por domínio e onde ele passou a duplicar infraestrutura já
existente em subprocesso/processo/alerta, elevando o esforço cognitivo do backend.

## Leitura geral

O diagnóstico **não parece inchado por causa das entidades**. O núcleo próprio faz sentido:

- `Diagnostico`
- `AvaliacaoServidor`
- `SituacaoCapacitacao`
- autoavaliação
- consenso
- situação de capacitação

O aumento de complexidade vem principalmente da **camada de orquestração**, onde diagnóstico passou a manter uma trilha
quase paralela para:

- workflow
- notificações
- validações de etapa
- ações em bloco
- montagem de contexto para telas

## Onde a separação é legítima

Estes pontos parecem ser domínio real, não duplicação acidental:

- [DiagnosticoAvaliacaoService.java](/Users/leonardo/sgc/backend/src/main/java/sgc/diagnostico/service/DiagnosticoAvaliacaoService.java):
  regras de autoavaliação, consenso, impossibilitação e situação de capacitação.
- [DiagnosticoValidacaoService.java](/Users/leonardo/sgc/backend/src/main/java/sgc/diagnostico/service/DiagnosticoValidacaoService.java):
  validações específicas de preenchimento e conclusão do diagnóstico.
- [DiagnosticoConsultaService.java](/Users/leonardo/sgc/backend/src/main/java/sgc/diagnostico/service/DiagnosticoConsultaService.java):
  montagem de DTOs específicos das telas de diagnóstico.
- [Diagnostico.java](/Users/leonardo/sgc/backend/src/main/java/sgc/diagnostico/model/Diagnostico.java) e agregados
  relacionados: estado próprio do domínio.

## Onde há duplicação estrutural

### 1. Workflow de análise paralelo

[DiagnosticoFluxoService.java](/Users/leonardo/sgc/backend/src/main/java/sgc/diagnostico/service/DiagnosticoFluxoService.java)
reimplementa o mesmo formato geral já presente
em [CadastroFluxoService.java](/Users/leonardo/sgc/backend/src/main/java/sgc/subprocesso/service/CadastroFluxoService.java):

- validar situação atual
- descobrir unidade de análise
- descobrir unidade de destino/devolução
- registrar análise
- registrar transição
- disparar notificações
- tratar variantes em bloco

Sinais concretos:

- `devolverDiagnostico(...)` repete o mesmo esqueleto de `executarDevolucao(...)`.
- `validarDiagnostico(...)` repete o papel de `executarAceite(...)`.
- `homologarDiagnostico(...)` repete a estrutura de `executarHomologacao(...)`.
- `aceitarDiagnosticosEmBloco(...)` e `homologarDiagnosticosEmBloco(...)` repetem o mesmo padrão das ações em bloco do
  cadastro/validação.

O diagnóstico
usa [SubprocessoTransicaoService.java](/Users/leonardo/sgc/backend/src/main/java/sgc/subprocesso/service/SubprocessoTransicaoService.java),
mas continua precisando saber demais sobre a coreografia.

### 2. Descoberta de contexto repetida

[DiagnosticoFluxoService.java](/Users/leonardo/sgc/backend/src/main/java/sgc/diagnostico/service/DiagnosticoFluxoService.java)
repete utilitários que já existem em outros fluxos:

- `buscarSuperiorImediato(...)`
- `obterUnidadeDevolucao(...)`

Esses mesmos conceitos também aparecem em:

- [CadastroFluxoService.java](/Users/leonardo/sgc/backend/src/main/java/sgc/subprocesso/service/CadastroFluxoService.java)
- [SubprocessoTransicaoService.java](/Users/leonardo/sgc/backend/src/main/java/sgc/subprocesso/service/SubprocessoTransicaoService.java)

Isso espalha a regra operacional de hierarquia/localização entre múltiplos serviços.

### 3. ProcessoService conhece ramos demais

[ProcessoService.java](/Users/leonardo/sgc/backend/src/main/java/sgc/processo/service/ProcessoService.java) ainda separa
explicitamente:

- cadastro
- validação de mapa
- diagnóstico

No bloco de ações em massa, por exemplo, ele já faz a bifurcação:

- cadastro -> `cadastroFluxoService`
- validação -> `transicaoService`
- diagnóstico -> `diagnosticoFluxoService`

Isso obriga o serviço de processo a conhecer detalhes de três trilhas operacionais diferentes.

### 4. Notificação própria para diagnóstico

[DiagnosticoNotificacaoService.java](/Users/leonardo/sgc/backend/src/main/java/sgc/diagnostico/service/DiagnosticoNotificacaoService.java)
é justificável em parte, porque há eventos exclusivos do diagnóstico. Mas ele também assume responsabilidades genéricas
que já existem em notificações de subprocesso:

- resolução de destinatário
- construção de idempotência
- decisão entre alerta, e-mail direto e superior
- envio individual e em bloco

Hoje ele está menos problemático do que antes, porque os assuntos já foram centralizados em
[AssuntosNotificacao.java](/Users/leonardo/sgc/backend/src/main/java/sgc/alerta/AssuntosNotificacao.java), mas continua
sendo uma segunda trilha operacional.

### 5. Leitura específica demais de contexto

[DiagnosticoConsultaService.java](/Users/leonardo/sgc/backend/src/main/java/sgc/diagnostico/service/DiagnosticoConsultaService.java)
concentra DTOs legítimos do domínio, mas também reimplementa pedaços de leitura transversal:

- resolução de unidade snapshot
- resolução do mapa vigente da unidade
- derivação de situação do subprocesso para UI
- agregação de movimentações/histórico

Parte disso poderia vir de uma borda mais compartilhada de leitura de subprocesso.

## Mapa de duplicações

| Tema                             | Trilha “geral”                                            | Trilha “diagnóstico”            | Observação                                     |
|----------------------------------|-----------------------------------------------------------|---------------------------------|------------------------------------------------|
| Aceite/devolução/homologação     | `CadastroFluxoService`, `SubprocessoTransicaoService`     | `DiagnosticoFluxoService`       | Mesmo esqueleto de workflow                    |
| Descoberta de superior/devolução | `CadastroFluxoService`, `SubprocessoTransicaoService`     | `DiagnosticoFluxoService`       | Regra operacional espalhada                    |
| Notificação de transição         | `SubprocessoNotificacaoService`                           | `DiagnosticoNotificacaoService` | Infra paralela, ainda que com eventos próprios |
| Ações em bloco                   | `ProcessoService` + fluxos de cadastro/validação          | `DiagnosticoFluxoService`       | Orquestração condicional duplicada             |
| Leitura agregada para UI         | `SubprocessoVisualizacaoService` e leitura de subprocesso | `DiagnosticoConsultaService`    | Parte é domínio; parte é borda compartilhável  |

## O que não deve ser feito

Não parece uma boa direção:

- mover entidades de diagnóstico para dentro de subprocesso;
- tentar “esmagar” autoavaliação/consenso/capacitação em services genéricos;
- eliminar o módulo `diagnostico` como fronteira.

Isso reduziria organização sem reduzir a complexidade real.

## Proposta incremental

### Etapa 1. Extrair um núcleo de workflow de análise

Criar uma abstração operacional pequena, voltada a transições de análise, para encapsular:

- resolver unidade superior imediata;
- resolver unidade de devolução;
- registrar análise;
- registrar transição com ou sem comunicações;
- aplicar política de envio em operações simples e em bloco.

Alvo imediato:

- remover de `DiagnosticoFluxoService` e `CadastroFluxoService` a duplicação de aceite/devolução/homologação;
- manter nesses serviços apenas as decisões específicas do domínio.

### Etapa 2. Enxugar `DiagnosticoFluxoService`

Depois da etapa 1, `DiagnosticoFluxoService` deveria ficar responsável só por:

- inicializar diagnóstico a partir do mapa vigente;
- concluir diagnóstico da unidade;
- decidir quando zerar `dataConclusao`;
- exigir `validarDiagnosticoHomologavel(...)`.

Aceite, devolução e homologação passariam a ser orquestrações finas sobre o núcleo comum.

### Etapa 3. Reduzir o branching em `ProcessoService`

Substituir a atual separação explícita por uma estratégia orientada por capacidade.

Exemplo conceitual:

- uma ação em bloco resolve um executor por “família de subprocesso”;
- o executor sabe como aceitar/homologar aquele grupo.

Isso tira de `ProcessoService` a obrigação de conhecer detalhes de cadastro, mapa e diagnóstico ao mesmo tempo.

### Etapa 4. Rebaixar `DiagnosticoNotificacaoService` a domínio puro

Depois de estabilizar o workflow comum:

- manter em `DiagnosticoNotificacaoService` apenas eventos exclusivos do diagnóstico;
- empurrar infraestrutura compartilhada de entrega/destinatário/idempotência para camada comum de notificação.

### Etapa 5. Revisar a borda de leitura

Separar em `DiagnosticoConsultaService`:

- o que é montagem específica da UI de diagnóstico;
- o que é leitura transversal de subprocesso, unidade snapshot, histórico e localização.

Isso pode reduzir acoplamento com `SubprocessoConsultaService` e `SubprocessoVisualizacaoService`.

## Ordem sugerida

1. Extrair utilitário/serviço compartilhado para contexto de análise.
2. Refatorar `DiagnosticoFluxoService` e `CadastroFluxoService` para usar esse núcleo.
3. Reduzir bifurcação em `ProcessoService`.
4. Consolidar infraestrutura de notificação.
5. Só então revisar a camada de consulta.

## Critério de sucesso

O objetivo não é “misturar diagnóstico com subprocesso”. É este:

- diagnóstico continua como módulo de domínio;
- workflow deixa de existir em duas ou três variações estruturais;
- `ProcessoService` para de conhecer tantos ramos;
- leitura de código para ações como aceitar/devolver/homologar volta a caber em um caminho mental curto.
