# CDU-52 - Finalizar processo de diagnóstico

Ator: ADMIN

## Pré-condições

- Login realizado com perfil ADMIN
- Existência de processo de diagnóstico na situação `Em andamento`
- Todas as unidades participantes do processo tiveram seus diagnósticos homologados

## Fluxo principal

1. No `Painel`, o usuário clica em um processo de diagnóstico na situação 'Em andamento'.

2. O sistema mostra a tela `Detalhes do processo`, com uma tabela hierárquica com todas as unidades participantes e a situação atual de cada diagnóstico.

3. O usuário clica em `Finalizar`.

4. O sistema verifica se todos os subprocessos das unidades participantes estão na situação `Homologado`.

5. Caso exista unidade ainda não homologada, o sistema mostra a mensagem "Não é possível finalizar o processo enquanto houver unidades com diagnóstico ainda não homologado" e interrompe a operação.

6. Caso todas as unidades estejam homologadas, o sistema solicita confirmação de finalização.
   
7. O usuário confirma.

8. O sistema muda a situação do processo para `Finalizado`.

9. O sistema notifica as unidades participantes sobre a finalização do processo.
   
10. O sistema redireciona para o `Painel` e mostra a mensagem `Processo finalizado`.