# CDU-52 - Finalizar processo de diagnóstico

Ator: ADMIN

Maturidade: Média

Base principal: fluxo negocial acordado no PDF, com revisão dos pontos não fechados na reunião.

## Pré-condições

- Login realizado com perfil ADMIN
- Existência de processo de diagnóstico na situação `Em andamento`
- Todas as unidades participantes do processo tiveram seus diagnósticos homologados pela SEDOC

## Fluxo principal

1. No `Painel`, o usuário clica em um processo de diagnóstico na situação `Em andamento`.

2. O sistema mostra a tela `Detalhes do processo`, com a árvore de todas as unidades participantes e a situação atual de cada diagnóstico.

3. O usuário clica em `Finalizar`.

4. O sistema verifica se todos os subprocessos das unidades participantes estão na situação `Homologado`.

5. Caso exista unidade ainda não homologada, o sistema mostra a mensagem `Não é possível finalizar o processo enquanto houver unidades com diagnóstico ainda não homologado` e interrompe a operação.

6. Caso todas as unidades estejam homologadas, o sistema solicita confirmação de finalização.

7. Caso o usuário escolha `Cancelar`, o sistema interrompe a operação e permanece na mesma tela.

8. O usuário confirma.

9. O sistema muda a situação do processo para `Finalizado`.

10. O sistema disponibiliza os relatórios consolidados do diagnóstico conforme as regras de acesso e agregação aplicáveis.

11. O sistema notifica as unidades participantes sobre a finalização do processo.

12. O sistema redireciona para o `Painel` e mostra a mensagem `Processo finalizado`.

## Observação

- O PDF afirma que, com a homologação, o sistema calcula gaps e ocupações críticas para relatórios. Este CDU assume a finalização completa do processo como marco de disponibilização geral dos relatórios consolidados.
- Caso o produto permita consulta parcial após homologações individuais, isso deve ser explicitado em requisito próprio.
- Os modelos exatos de e-mail de finalização ainda não estão fechados pelo material revisado e devem ser especificados separadamente antes da implementação.
