# Plano de Melhorias E2E

## Situação atual consolidada

- `smoke.spec.ts` está verde.
- `captura-telas.spec.ts` está verde.
- Os blocos `cdu-0*.spec.ts`, `cdu-1*.spec.ts` e `cdu-2*.spec.ts` estão verdes.
- No bloco `cdu-3*.spec.ts`, `cdu-30`, `cdu-31`, `cdu-32`, `cdu-34`, `cdu-35` e `cdu-36` já estão ajustados.
- O único ponto ainda aberto no bloco `cdu-3` é `cdu-33.spec.ts`.

## O que foi consolidado

### Semântica de notificações

- Os testes foram alinhados à UI atual, reduzindo dependência de textos transitórios legados.
- Onde a aplicação redireciona para o painel, a validação foi trocada para estado final observável:
  - URL;
  - situação do subprocesso;
  - botões e permissões disponíveis;
  - histórico persistente.

### Navegação e helpers

- A navegação para cadastro em visualização e mapa foi reforçada com rotas semânticas, em vez de depender apenas de cards clicáveis.
- O uso de helpers compartilhados foi ampliado para reduzir duplicação e fragilidade.
- Os cenários mantiveram a diretriz de evitar `force`, `try/catch` e condicionais oportunistas nos testes.

### Fixtures de backend

- Fixtures de backend já são usadas para reduzir custo estrutural quando o objetivo não é validar o workflow completo pela UI.
- Já existem fixtures úteis para estados profundos de mapeamento, incluindo processo com mapa homologado.
- A estratégia se mostrou correta para evitar estouros artificiais de `20s` em cenários seriais longos.

### Ajustes funcionais descobertos pelos testes

- A navegação de visualização em subprocessos finalizados foi corrigida para continuar acessível.
- Testes de relatórios foram atualizados para a UI atual baseada em abas, selects e geração de PDF.
- Casos de reabertura e lembrete foram alinhados ao comportamento real da aplicação e ao histórico persistente.

## Pendência atual

### `cdu-33.spec.ts`

O cenário de reabertura de revisão ainda depende de uma preparação confiável de subprocesso em estado elegível para `Reabrir Revisão`.

Estado atual da investigação:

- o teste antigo por UI ficou longo demais e estourava o timeout global do arquivo serial;
- foi iniciada a migração para fixture de backend;
- foi criado um novo endpoint de teste para revisão com mapa homologado;
- o teste de backend desse endpoint ainda está falhando com `422`.

### Causa já identificada

Para revisão, não basta apenas existir um subprocesso com situação avançada: a unidade precisa ter **mapa vigente** reconhecido pelas validações de negócio.

Na prática, isso significa que:

- o mapeamento anterior da unidade precisa ter sido efetivamente finalizado; e
- a fixture de revisão precisa reproduzir esse pré-requisito de forma consistente.

## Próximo passo recomendado

1. Fechar a fixture/endpoint de revisão homologada no backend.
2. Validar `E2eFixtureEndpointTest`.
3. Rerodar `e2e/cdu-33.spec.ts` com saída em `.txt`.
4. Rerodar o bloco `cdu-3*.spec.ts` completo com `--workers=1` e saída em `.txt`.

## Critérios para considerar a rodada concluída

- `cdu-33.spec.ts` verde sem aumento de timeout;
- bloco `cdu-3*.spec.ts` verde;
- preparação de revisão feita por fixture aderente às regras de negócio reais;
- sem reintroduzir `force`, `waitForTimeout`, `try/catch` ou condicionais para mascarar falhas.
