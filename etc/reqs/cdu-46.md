# CDU-46 - Indicar impossibilidade de avaliação

Ator: CHEFE

Maturidade: Média

Base principal: Fluxo narrado e validado na reunião, complementado por resposta do usuário sobre comportamento "tudo ou nada".

## Pré-condições

- Login realizado com perfil CHEFE
- Processo de diagnóstico em andamento
- Existência de servidor da unidade cuja avaliação individual ainda não esteja na situação `Consenso aprovado`

## Fluxo principal

1. Na tela `Diagnóstico da equipe`, o usuário escolhe o servidor desejado e aciona `Indicar impossibilidade`.

2. O sistema abre modal com:
   - título `Indicar impossibilidade de avaliação`;
   - texto `Confirma a indicação de impossibilidade de avaliação para [NOME_SERVIDOR]?`;
   - campo obrigatório `Justificativa`;
   - botões `Cancelar` e `Confirmar`.

3. Caso o usuário escolha `Cancelar`, o sistema interrompe a operação e permanece na mesma tela.

4. O usuário informa a justificativa e clica em `Confirmar`.

5. O sistema altera a situação da avaliação individual para `Avaliação impossibilitada`.

6. O sistema passa a desconsiderar, para fins de conclusão da unidade e cálculos consolidados do ciclo, quaisquer
   dados parciais anteriormente registrados para aquela avaliação individual.

   PENDÊNCIA DE REFINAMENTO: esta especificação assume comportamento de "tudo ou nada" para a impossibilidade, isto é,
   os dados parciais anteriores deixam de ter efeito no ciclo. Confirmar com a área de negócio se haverá necessidade de
   preservar ou reaproveitar parte desses dados em algum cenário.

7. O sistema mostra a mensagem `Impossibilidade registrada`.
