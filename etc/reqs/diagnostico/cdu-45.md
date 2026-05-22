# CDU-45 - Preencher ocupações críticas

Ator: CHEFE

Maturidade: Média

Base principal: Fluxo narrado e validado na reunião, com interpretação operacional do conceito de ocupações críticas.

## Pré-condições

- Login realizado com perfil CHEFE
- Processo de diagnóstico em andamento com participação da unidade do usuário

## Fluxo principal

1. No `Painel`, o usuário acessa um processo de diagnóstico em andamento.

2. O sistema mostra a tela `Detalhes do subprocesso` da unidade.

3. O usuário clica no card `Ocupações críticas`.

4. O sistema apresenta uma grade contendo, para cada servidor participante do snapshot da unidade e para cada
   competência vigente da unidade, um campo `Situação de capacitação`.

5. O campo `Situação de capacitação` deverá admitir os seguintes valores:
   - `NA` (Não se aplica);
   - `AC` (A capacitar);
   - `EC` (Em capacitação);
   - `C` (Capacitado);
   - `I` (Instrutor).

6. O usuário informa os valores desejados.

7. O sistema salva automaticamente cada alteração realizada.

8. O sistema mostra a mensagem `Informações atualizadas`.

## Observação

PENDÊNCIA DE REFINAMENTO: a grade por servidor e por competência foi adotada como interpretação principal do conceito de
`Ocupações críticas`. Confirmar se a área de negócio deseja algum resumo adicional por unidade ou outro recorte de
visualização.
