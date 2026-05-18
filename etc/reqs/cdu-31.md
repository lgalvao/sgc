# CDU-31 - Configurar sistema

**Ator:** ADMIN

## Pré-condições
Usuário logado com perfil ADMIN.

## Fluxo principal

1. O usuário clica no ícone de configurações ('engrenagem') na barra de navegação.

2. O sistema mostra a tela `Configurações` com o valor atual das seguintes configurações, permitindo edição. São dois campos:

    2.1. "Dias para inativação de processos": Dias depois da finalização de um processo para que seja considerado inativo. Com valor inteiro >= 1.

    2.2. "Dias para marcação automática de alerta como lido": Quantidade de dias após o envio de um alerta para que ele
    passe a ser considerado lido pelo sistema.

3. O usuário altera os valores das configurações e clica em `Salvar`.
   
4. O sistema guarda as configurações internamente. O efeito das configurações deve ser imediato sobre outras partes do
   sistema.

    4.1. Processos finalizados há mais de `DIAS_INATIVACAO_PROCESSO` dias deixam de aparecer no Painel e passam a
    aparecer apenas no Histórico.

    4.2. Alertas enviados há mais de `DIAS_ALERTA_NOVO` dias passam a ser considerados lidos pelo sistema e deixam de
    ser exibidos em negrito.
