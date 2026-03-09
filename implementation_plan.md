# Plano de Correção: Alinhamento CDU-25 - Aceite em Bloco

Este plano visa alinhar a implementação do frontend e os testes E2E com o requisito oficial [etc/reqs/cdu-25.md](file:///c:/sgc/etc/reqs/cdu-25.md), corrigindo discrepâncias de textos e garantindo que o fluxo de aceite em bloco funcione conforme especificado.

## Propostas de Mudanças

### Backend

A lógica de visibilidade hierárquica recursiva já foi implementada no [ProcessoDetalheBuilder.java](file:///c:/sgc/backend/src/main/java/sgc/processo/service/ProcessoDetalheBuilder.java) e [ProcessoConsultaService.java](file:///c:/sgc/backend/src/main/java/sgc/processo/service/ProcessoConsultaService.java) através do uso de [buscarCodigosDescendentes](file:///c:/sgc/backend/src/main/java/sgc/processo/service/ProcessoValidacaoService.java#143-149). O GESTOR agora vê apenas subprocessos de sua unidade e subordinadas.

### Frontend

- **Botão Principal**: Mudar para "Aceitar mapas em bloco".
- **Título do Modal**: Mudar para "Aceite de mapas em bloco".
- **Texto de Instrução**: Mudar para "Selecione as unidades para aceite dos mapas correspondentes".
- **Botão de Confirmação**: Mudar para "Registrar aceite".
- **Mensagem de Sucesso**: Mudar para "Mapas aceitos em bloco".

- Alterar `TipoTransicao.MAPA_VALIDACAO_ACEITA`:
    - `descMovimentacao`: "Mapa de competências aceito"
    - `templateAlerta`: "Validação do mapa de competências da unidade %s submetida para análise"

#### [MODIFY] [SubprocessoTransicaoService.java](file:///c:/sgc/backend/src/main/java/sgc/subprocesso/service/SubprocessoTransicaoService.java)

- Em [executarAceiteValidacao](file:///c:/sgc/backend/src/main/java/sgc/subprocesso/service/SubprocessoTransicaoService.java#368-411):
    - Mudar `motivoAnalise` para "Aceite de mapa".
    - Mudar `observacoes` para "De acordo com a validação do mapa realizada pela unidade".

> [!NOTE]
> Como o componente é compartilhado com o aceite de cadastro (CDU-22), utilizarei lógica condicional básica para exibir o texto correto se houver mapas na lista de elegíveis.

### E2E

#### [MODIFY] [cdu-25.spec.ts](file:///c:/sgc/e2e/cdu-25.spec.ts)

Atualizar o teste Playwright para refletir as novas strings e garantir o sucesso dos cenários:

- Atualizar locators dos botões e textos do modal.
- Atualizar a expectativa da mensagem de toast de sucesso.
- Manter o timeout de 60s para evitar falhas por lentidão no ambiente Windows.

## Plano de Verificação

### Testes Automatizados

Executar a suíte de testes E2E específica:
```bash
npx playwright test e2e/cdu-25.spec.ts --project=chromium
```

### Verificação Manual

O usuário pode validar:
1. Logar como GESTOR de uma unidade (ex: COORD_21).
2. Acessar um processo com subprocessos em "Mapa validado" ou "Mapa com sugestões" localizados na unidade.
3. Verificar se o botão "Aceitar mapa de competências em bloco" aparece.
4. Validar se o modal segue exatamente as strings do CDU-25.
5. Confirmar o aceite e verificar o redirecionamento para o Painel com a mensagem correta.
