# Plano de consistência de botões

## Contexto

O frontend vinha misturando dois conceitos diferentes:

- `pode*`: ação executável no estado e na localização atuais.
- `habilitar*`: ação liberada para clique no contexto atual.

Na prática, várias telas estavam usando `pode*` para decidir se o botão existia, o que contrariava a regra de UX documentada em [C:\sgc\etc\reqs\regras-acesso.md](C:\sgc\etc\reqs\regras-acesso.md):

- esconder apenas quando o perfil nunca pode executar a ação;
- desabilitar quando o perfil pode executar em algum cenário, mas o estado ou a localização atuais impedem.

## Problemas identificados

### Mapa

- `MapaView` já tinha correções locais para `ADMIN`, mas ainda dependia de exceções na própria view.
- Ações como `Disponibilizar`, `Devolver` e `Homologar` estavam sendo mostradas por lógica ad hoc, não por um contrato central.

### Cadastro

- `CadastroAcoesHeader` escondia `Devolver`, ação principal, `Importar` e `Disponibilizar` com base em `pode*`.
- `Disponibilizar` e `Importar` não respeitavam integralmente os flags `habilitar*`.

### Subprocesso

- `Alterar data limite`, `Reabrir cadastro`, `Reabrir revisão` e `Enviar lembrete` apareciam apenas quando a ação estava executável naquele momento.
- Para `ADMIN`, isso fazia botões desaparecerem em vez de ficarem visíveis e inativos.

## Estratégia

### 1. Centralizar visibilidade em `useAcesso`

Adicionar flags derivadas de perfil para uso direto nas telas:

- `mostrarAlterarDataLimite`
- `mostrarReabrirCadastro`
- `mostrarReabrirRevisao`
- `mostrarEnviarLembrete`
- `mostrarImportarAtividades`
- `mostrarDisponibilizarCadastro`
- `mostrarDevolverCadastro`
- `mostrarApresentarSugestoes`
- `mostrarValidarMapa`
- `mostrarDisponibilizarMapa`
- `mostrarDevolverMapa`

Também ajustar `acaoPrincipalCadastro` e `acaoPrincipalMapa` para refletirem a ação principal do perfil mesmo quando estiver inativa.

### 2. Corrigir os consumidores

- `CadastroAcoesHeader.vue`
  - usar `mostrar*` para renderização;
  - usar `habilitar*` para `disabled`.
- `MapaView.vue`
  - remover exceções locais de visibilidade baseadas em perfil;
  - usar apenas o contrato central.
- `SubprocessoView.vue`
  - manter botões administrativos visíveis para `ADMIN`;
  - desabilitar quando a situação atual não permitir.

### 3. Reforçar testes

Cobrir pelo menos:

- `useAcesso.spec.ts`
  - visibilidade por perfil;
  - ação principal visível porém desabilitada fora do estado correto.
- `MapaViewSomenteLeitura.spec.ts`
  - ações do menu visíveis e desabilitadas quando necessário.
- testes de cadastro e subprocesso
  - botões administrativos e de análise visíveis para o perfil correto mesmo fora do estado habilitado.

## Critério de aceite

- nenhum botão de workflow desaparece se o perfil atual puder executá-lo em algum ponto do fluxo;
- o clique só fica ativo quando os `habilitar*` ou equivalentes permitirem;
- a lógica de visibilidade deixa de ficar espalhada em exceções por tela.
