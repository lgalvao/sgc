# DTOs com `@Nullable` - Categoria B

## Objetivo

Registrar os casos em que `@Nullable` nao parece redundante por implementacao, mas sim um sinal de contrato largo demais.

Nesta categoria, o OpenRewrite ajuda como apoio mecanico, mas a decisao principal ainda e semantica e orientada por endpoint.

## Criterio da Categoria B

Um DTO entra na Categoria B quando pelo menos uma destas condicoes ocorre:

- o mesmo request representa mais de uma acao de negocio;
- um campo so e obrigatorio para um subtipo de acao;
- a response agrega blocos opcionais que correspondem a estados diferentes do fluxo;
- o `service` usa `if (campo != null)` como mecanismo de patch parcial, em vez de um comando explicito;
- a UI ou a API consome apenas um subconjunto estavel do contrato, mas o DTO continua carregando outros ramos.

## Inventario inicial

### 1. `AcaoEmBlocoRequest`

Arquivos:

- [AcaoEmBlocoRequest.java](/Users/leonardo/sgc/backend/src/main/java/sgc/processo/dto/AcaoEmBlocoRequest.java)
- [ProcessoService.java](/Users/leonardo/sgc/backend/src/main/java/sgc/processo/service/ProcessoService.java#L255)
- [processoService.ts](/Users/leonardo/sgc/frontend/src/services/processoService.ts#L131)

Sinal:

- `dataLimite` so faz sentido quando `acao == DISPONIBILIZAR`.
- o `service` valida `dataLimite` apenas nesse branch.

Leitura:

- o contrato atual mistura ao menos dois comandos reais:
  - acao em bloco com data limite;
  - acao em bloco sem data limite.

Melhor proximo passo:

- manter o endpoint atual por compatibilidade;
- criar comando interno especializado por acao;
- depois avaliar se vale expor requests separados por endpoint ou por discriminador mais forte.

Ajuda do OpenRewrite:

- media para renomear/adaptar classes internas depois da decisao;
- baixa para descobrir a semantica correta sozinho.

### 2. `ContextoEdicaoResponse`

Arquivos:

- [ContextoEdicaoResponse.java](/Users/leonardo/sgc/backend/src/main/java/sgc/subprocesso/dto/ContextoEdicaoResponse.java)
- [SubprocessoConsultaService.java](/Users/leonardo/sgc/backend/src/main/java/sgc/subprocesso/service/SubprocessoConsultaService.java#L182)
- [subprocessoService.ts](/Users/leonardo/sgc/frontend/src/services/subprocessoService.ts#L35)

Sinal:

- `detalhes` e `mapa` sao opcionais dentro de uma resposta agregada unica.
- o endpoint tem consumidor concentrado no frontend.

Leitura:

- a response parece carregar mais de um estado de tela no mesmo contrato.
- isso dificulta dizer, pelo tipo, o que sempre existe ao abrir contexto de edicao.

Melhor proximo passo:

- mapear exatamente o que a tela usa sempre;
- separar um contrato base obrigatorio do que e bloco opcional;
- considerar response composta com subestruturas nomeadas em vez de campos opcionais soltos.

Ajuda do OpenRewrite:

- baixa antes da decisao estrutural;
- media depois que a nova estrutura estiver definida e for preciso atualizar acessos.

### 3. `AtualizarSubprocessoRequest`

Arquivos:

- [AtualizarSubprocessoRequest.java](/Users/leonardo/sgc/backend/src/main/java/sgc/subprocesso/dto/AtualizarSubprocessoRequest.java)
- [SubprocessoService.java](/Users/leonardo/sgc/backend/src/main/java/sgc/subprocesso/service/SubprocessoService.java#L75)
- [SubprocessoController.java](/Users/leonardo/sgc/backend/src/main/java/sgc/subprocesso/SubprocessoController.java#L79)

Sinal:

- o `service` aplica mudancas campo a campo so quando o valor vem preenchido.
- o DTO funciona como patch generico.

Leitura:

- o contrato nao explicita a intencao da alteracao;
- `null` significa "nao alterar", nao "valor ausente do dominio".

Melhor proximo passo:

- substituir por comandos mais semanticos por tipo de alteracao;
- se a API precisar manter um endpoint unico por enquanto, converter o request externo em comandos internos especializados.

Ajuda do OpenRewrite:

- media para migrar chamadas internas depois da quebra em comandos;
- baixa para escolher a divisao correta.

### 4. `CriarMapaRequest` e `AtualizarMapaRequest`

Arquivos:

- [CriarMapaRequest.java](/Users/leonardo/sgc/backend/src/main/java/sgc/mapa/dto/CriarMapaRequest.java)
- [AtualizarMapaRequest.java](/Users/leonardo/sgc/backend/src/main/java/sgc/mapa/dto/AtualizarMapaRequest.java)
- [MapaManutencaoService.java](/Users/leonardo/sgc/backend/src/main/java/sgc/mapa/service/MapaManutencaoService.java#L185)
- [MapaController.java](/Users/leonardo/sgc/backend/src/main/java/sgc/mapa/MapaController.java#L52)

Sinal:

- os requests misturam criacao, disponibilizacao, sugestoes e homologacao num mesmo contrato.
- os endpoints de `MapaController` nao aparecem como consumidores diretos do frontend atual.

Leitura:

- este pode ser um bom alvo para especializacao por ter baixo acoplamento aparente com a UI atual.

Melhor proximo passo:

- dividir por intencao:
  - criar mapa;
  - registrar disponibilizacao;
  - registrar sugestoes;
  - registrar homologacao.

Ajuda do OpenRewrite:

- alta depois que os novos tipos e metodos forem definidos, especialmente para atualizar chamadas e assinaturas internas.

### 5. `CriarAnaliseRequest`

Arquivos:

- [CriarAnaliseRequest.java](/Users/leonardo/sgc/backend/src/main/java/sgc/subprocesso/dto/CriarAnaliseRequest.java)
- [SubprocessoController.java](/Users/leonardo/sgc/backend/src/main/java/sgc/subprocesso/SubprocessoController.java#L525)
- [SubprocessoTransicaoService.java](/Users/leonardo/sgc/backend/src/main/java/sgc/subprocesso/service/SubprocessoTransicaoService.java#L303)

Sinal:

- o mesmo request atende cadastro e validacao;
- `motivo` e `observacoes` sao opcionais por regra de fluxo, nao necessariamente por problema de modelagem.

Leitura:

- este caso e menos urgente.
- pode continuar como esta se a regra de negocio realmente permitir combinacoes livres entre `motivo`, `observacoes` e `acao`.

Ajuda do OpenRewrite:

- baixa neste momento.

## Ordem sugerida

1. `CriarMapaRequest` e `AtualizarMapaRequest`
2. `AcaoEmBlocoRequest`
3. `ContextoEdicaoResponse`
4. `AtualizarSubprocessoRequest`
5. `CriarAnaliseRequest` apenas se a regra funcional justificar

## Estrategia recomendada

### Fase 1

Nao quebrar contrato HTTP externo.

- introduzir comandos internos especializados;
- adaptar `service` e `facade` para trabalhar com esses comandos;
- manter os requests atuais apenas como fronteira de traducao.

### Fase 2

Depois de estabilizar o uso interno:

- revisar endpoints com consumidor unico;
- separar contratos HTTP quando houver ganho liquido de clareza;
- atualizar frontend junto com testes do fluxo.

### Fase 3

So entao usar OpenRewrite em lote:

- renomear classes e metodos;
- mover chamadas para novos comandos;
- limpar imports e referencias antigas;
- remover DTOs antigos quando nao houver mais dependencias.

## Observacoes

- `MapaController` parece ter baixo consumo direto pelo frontend atual, o que o torna melhor candidato para a primeira especializacao manual.
- `ContextoEdicaoResponse` e sensivel porque o endpoint e consumido pelo frontend atual; aqui vale medir primeiro o uso real da tela.
- `SubprocessoService.processarAlteracoes(...)` e um sinal claro de patch generico e merece refatoracao guiada por regra, nao por busca textual.
