# CDU-31 - Configurar sistema

**Ator:** ADMIN

## Pré-condições
Usuário logado com perfil ADMIN.

## Fluxo principal

1. O usuário clica no botão de configurações ('engrenagem') na barra de navegação

2. O sistema mostra a tela `Configurações` com o valor atual das seguintes configurações, permitindo edição.

    2.1. Campo "Dias para inativação de processos": Dias depois da finalização de um processo para que seja considerado inativo. Com valor inteiro >= 1.

    2.2. Campo "Dias para indicação de alerta como novo": Dias depois depois de um alerta ser enviado para uma unidade, para que deixe de ser marcado como novo.

3. O usuário altera os valores das configurações e clica em `Salvar`.
   
4. O sistema guarda as configurações internamente. O efeito das configurações deve ser imediato sobre outras partes do sistema.