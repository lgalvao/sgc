# Módulo `subprocesso`

## Visão geral

O módulo `sgc.subprocesso` implementa o ciclo de execução de um processo por unidade organizacional.

Ele cobre consulta, cadastro, transições de workflow, validação, notificações, histórico e permissões de interface.

## Camadas

- `SubprocessoController`: API REST em `/api/subprocessos`.
- `dto/`: contratos de entrada/saída.
- `model/`: entidades e repositórios do domínio.
- `service/`: regras de negócio.

## Serviços principais

- `SubprocessoConsultaService`: leitura de detalhe, contexto, status, permissões e listas.
- `SubprocessoService`: operações administrativas e manutenção do subprocesso.
- `CadastroFluxoService`: fluxo de cadastro e revisão de cadastro.
- `SubprocessoTransicaoService`: transições de workflow e operações de mapa.
- `SubprocessoValidacaoService`: validações de consistência e pré-condições.
- `SubprocessoAcessoService`: regras de autorização e capacidade de ação.
- `SubprocessoVisualizacaoService`: composição de respostas de visualização.
- `SubprocessoContextoConsultaService`: montagem de contexto para UI.
- `SubprocessoSituacaoService`: reconciliação de situação conforme estado do mapa.
- `SubprocessoNotificacaoService`: notificações decorrentes de transições.
- `AnaliseHistoricoService` e `LocalizacaoSubprocessoService`: histórico e apoio de localização.

## Responsabilidades funcionais

- gestão do cadastro de atividades;
- disponibilização, devolução, aceite e homologação de cadastro/revisão;
- gestão e validação de mapa;
- operações em bloco;
- histórico de análise e movimentação;
- cálculo de permissões estruturadas para a interface.

## Regras arquiteturais

- API expõe DTOs, não entidades JPA.
- Acesso controlado por `@PreAuthorize` + `SgcPermissionEvaluator`.
- Convenção de endpoint com ações explícitas via `POST`.

## Testes

Da raiz do repositório:

```bash
./gradlew :backend:test
```
