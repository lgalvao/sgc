# Princípios de Contratos, DTOs e Fixtures

Este documento registra regras para evitar a volta dos problemas corrigidos nas rodadas de simplificação de DTOs,
nulidade e limpeza de mocks/fixtures no SGC.

O objetivo é reduzir:

- `@Nullable` redundante;
- branches defensivos impossíveis;
- mocks e fixtures inválidos;
- testes que passam com contratos irreais;
- regressões causadas por entidades mínimas demais para os mapeadores atuais.

## 1. Contrato primeiro

- DTO é contrato, não conveniência.
- Se um campo é obrigatório no fluxo real, o DTO deve refletir isso.
- Não manter `@Nullable` só para facilitar teste, mock ou payload legado interno.
- Quando um endpoint exige estados diferentes, preferir DTOs ou commands especializados em vez de um DTO largo com campos condicionais.

## 2. Endurecer no ponto certo

- Endurecer contratos na fronteira mais próxima do uso real.
- Requests HTTP podem continuar compatíveis por um tempo, mas services e domínio devem convergir para commands semânticos.
- Se um mapper já assume presença de `processo`, `unidade`, `tipo` ou datas, o teste deve fornecer esses dados.
- Não afrouxar mapper de produção para aceitar fixture inválida.

## 3. Fixtures devem ser válidas

- Fixture de teste deve representar um estado possível do sistema.
- Entidade mínima não é entidade válida.
- Se `ProcessoResumoDto.fromEntity(...)` usa `tipo`, `situacao`, `dataCriacao` e `dataLimite`, a fixture de `Processo` deve preencher esses campos.
- Se `SubprocessoListagemDto.fromEntity(...)` usa `processo` e `unidade`, a fixture de `Subprocesso` deve ter ambos associados.
- Se o teste quer cobrir ausência semântica, isso deve aparecer como regra explícita do caso, não como objeto meio vazio.

## 4. Null impossível é erro explícito

- Quando um `null` for estruturalmente impossível, falhar com exceção de domínio clara.
- Preferir `IllegalStateException` com mensagem objetiva a `NullPointerException`.
- Exemplo: item `null` vindo de coleção de repositório, entidade obrigatória ausente em mapper, relacionamento esperado mas não carregado.
- Isso melhora depuração e deixa o teste expressar a invariável quebrada.

## 5. Teste não deve ditar contrato frouxo

- Se uma refatoração aperta contrato real, o primeiro suspeito ao quebrar é o teste.
- Antes de mudar produção, verificar se o mock ainda representa um estado válido.
- Só afrouxar produção quando houver caso real de negócio, requisito ou compatibilidade externa comprovada.

## 6. Regras para backend

- Controllers não devem montar resposta com entidade parcialmente preenchida em teste.
- Testes de controller devem usar factories válidas para DTOs e entidades retornadas por service.
- Toda fixture de `Processo` usada em resposta deve preencher, no mínimo:
  - `codigo`
  - `descricao`
  - `tipo`
  - `situacao`
  - `dataCriacao`
  - `dataLimite` quando o DTO o expõe
- Toda fixture de `Subprocesso` usada em resposta deve preencher, no mínimo:
  - `codigo`
  - `unidade`
  - `processo`
  - `situacao`
  - `dataLimiteEtapa1`

## 7. Regras para frontend

- Types do frontend são fonte de verdade para telas, composables e stores.
- Não usar `any`, `as any` ou payload parcial para “facilitar” spec.
- Se a tela consome `ContextoEdicaoSubprocesso`, o mock deve respeitar `subprocesso`, `detalhes`, `mapa`, `atividadesDisponiveis` e `unidade`.
- Se um modal ou view usa contrato obrigatório, remover fallback defensivo depois de apertar o tipo.
- Branch defensivo sem cenário real deve ser removido, não só coberto.

## 8. Factories compartilhadas antes de mock espalhado

- Quando um tipo começa a ficar mais rígido, criar helper/factory de teste em vez de repetir objeto parcial em vários arquivos.
- Preferir helpers como:
  - `criarProcessoResumoValido()`
  - `criarSubprocessoValido(...)`
  - `criarContextoEdicao(...)`
  - `criarDetalheMinimo(...)`
- O nome da factory deve deixar claro se ela cria um objeto válido de produção ou um cenário propositalmente inválido.

## 9. Compatibilidade temporária tem prazo

- Adaptadores e overloads de compatibilidade são aceitáveis só como etapa de migração.
- Depois que chamadas internas e testes estiverem atualizados, remover a camada de compatibilidade.
- Não deixar adaptador permanente escondendo contrato velho.

## 10. Critérios de revisão

Ao revisar PR ou rodada de refatoração, verificar:

- este `@Nullable` é de domínio ou só sobra de DTO largo?
- este `null` é possível em produção ou só em mock inválido?
- este teste está usando entidade válida para o mapper atual?
- este branch defensivo ainda tem cenário real?
- este service/composable/store expõe mais estado do que o fluxo usa?
- esta compatibilidade ainda é necessária?

## 11. Critérios de aceite para novas mudanças

Uma mudança só deve ser considerada concluída quando:

- os testes usam fixtures válidas para os contratos atuais;
- o typecheck do frontend fecha sem `any` novo introduzido por conveniência;
- o backend não depende de entidade parcial para serializar resposta;
- exceções de invariável quebrada são explícitas;
- o snapshot de smells não piora sem justificativa.

## 12. Regra prática

Quando um teste quebrar após endurecer contrato:

1. Verificar se o mock representa um estado real.
2. Se não representar, corrigir a fixture.
3. Se representar, ajustar o contrato ou o mapper.
4. Só por último introduzir compatibilidade temporária, com plano de remoção.
