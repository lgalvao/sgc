# CDU-31 - Configurar sistema

**Ator:** ADMIN

## Pré-condições

- Usuário logado com perfil ADMIN.

## Fluxo principal

1. O usuário clica no ícone de configurações (engrenagem) na barra de navegação.

2. O sistema mostra a tela `Configurações` com o valor atual das seguintes configurações, permitindo edição:
    - `Dias para inativação de processos`: Dias depois da finalização de um processo para que seja considerado inativo.
      Com valor inteiro >= 1. Referenciado nos requisitos como `DIAS_INATIVACAO_PROCESSO`.
    - `Dias para indicação de alerta como não lido`: Quantidade de dias após o envio de um alerta para que 
      passe a ser considerado lido pelo sistema. Referenciado nos requisitos como `DIAS_ALERTA_NOVO`.

3. O usuário altera os valores das configurações e aciona `Salvar`.

4. O sistema salva as configurações. O efeito deve ser imediato sobre outras partes do sistema; especificamente:
    - processos finalizados há mais de `DIAS_INATIVACAO_PROCESSO` dias deixam de aparecer no `Painel` e passam a
      aparecer apenas no `Histórico`.
    - alertas enviados há mais de `DIAS_ALERTA_NOVO` dias passam a ser considerados lidos pelo sistema e deixam de ser
      destacados em negrito.