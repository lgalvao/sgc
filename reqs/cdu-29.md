# CDU-29 - Configurar sistema

**Ator:** ADMIN

**Fluxo principal:**

- ADMIN clica no botão de configurações ('engrenagem') na barra de navegação
- O sistema mostra a tela Configurações com o valor atual das seguintes configurações, permitindo edição.
- Dias para inativação de processos (referenciado neste documento como DIAS_INATIVACAO_PROCESSO): Dias depois da finalização de um processo para que seja considerado inativo. Valor inteiro, 1 ou mais.
- Dias para indicação de alerta como novo (referenciado neste documento como DIAS_ALERTA_NOVO): Dias depois depois de um alerta ser enviado para uma unidade, para que deixe de ser marcado como novo.
- ADMIN altera os valores das configurações e clica em Salvar.
- O sistema mostra mensagem de confirmação e guarda as configurações internamente. O efeito das configurações deve ser imediato.
