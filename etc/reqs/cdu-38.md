# CDU-38 - Acompanhar notificações por e-mail

**Ator:** ADMIN

## Descrição

Permite ao administrador acompanhar as notificações de e-mail registradas pelo sistema, inspecionar seu conteúdo, consultar detalhes operacionais e reenfileirar notificações com falha definitiva.

## Pré-condições

- Usuário autenticado com perfil ADMIN.

## Fluxo principal

1. O usuário acessa `Notificações` no menu de administração.

2. O sistema mostra a tela `Notificações`.

3. Ao abrir a tela, o sistema consulta até 50 notificações administrativas recentes registradas no sistema.

4. O sistema apresenta a listagem com as colunas:
   - `Destinatário`
   - `Tipo`
   - `Assunto`
   - `Status`
   - `Quando`
   - ações da linha

5. O sistema ordena os itens priorizando notificações mais críticas primeiro. Dentro de uma mesma situação, os itens mais recentes aparecem antes.

6. Para cada linha, o sistema pode disponibilizar as seguintes ações:
   - `Detalhes`, para abrir os dados completos da notificação;
   - `Preview`, quando houver corpo HTML salvo;
   - `Reenviar`, apenas quando a situação for `Falha definitiva`.
   - Estas são as situações de notificação: `Pendente`, `Enviando`, `Enviado`, `Falha temporária` e `Falha definitiva`.

7. Ao acionar `Detalhes`, o sistema abre um modal `Detalhes da notificação`, exibindo:
   - destinatário;
   - tipo;
   - situação;
   - criado em;
   - enviado em;
   - próxima tentativa;
   - falhas anteriores;
   - último erro.

8. Ao acionar `Preview`, o sistema abre um modal com o assunto da notificação e mostra:
   - destinatário;
   - data/hora de criação;
   - conteúdo HTML do e-mail renderizado em visualização isolada.

9. Ao acionar `Reenviar` em uma notificação com falha definitiva, o sistema mostra um modal de confirmação com a pergunta `Confirma o reenvio deste e-mail específico para [DESTINATARIO]?`.

10. Se o usuário confirmar, o sistema recoloca a notificação na fila de envio e mostra a mensagem `E-mail recolocado na fila de envio`.

11. Após o reenvio com sucesso, o sistema recarrega a listagem.

12. O usuário pode acionar `Atualizar` a qualquer momento para recarregar a listagem manualmente.

13. Se não houver notificações para exibir, o sistema mostra o estado vazio `Sem notificações`.

14. Se o ambiente não for de produção e houver URL configurada para o leitor de e-mail de testes, o sistema mostra o link `Leitor de e-mail de testes`, abrindo-o em nova aba.
