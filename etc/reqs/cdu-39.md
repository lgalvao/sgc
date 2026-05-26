# CDU-55 - Enviar feedback contextual

**Ator:** TODOS

## Descrição

Permite ao usuário autenticado enviar um feedback contextual sobre a tela atual, com classificação, descrição textual, metadados automáticos da navegação e captura de tela opcional.

## Pré-condições

- Perfil de homologação ou testes. Funcionalidade NÃO disponível em produção
- Usuário autenticado.
- Widget de feedback habilitado no build atual.
- Endpoint de feedback disponível no ambiente atual.

## Fluxo principal

1. O sistema mostra um botão flutuante `Enviar feedback`, fixado no canto inferior direito da tela.

2. Ao clicar no botão, o sistema tenta capturar uma imagem da tela atual.

3. Se a captura for concluída com sucesso, o sistema associa a imagem ao feedback em preparação.

4. Se a captura falhar ou exceder o tempo limite interno, o sistema registra a falha localmente e prossegue sem screenshot.

5. O sistema abre o modal `Enviar feedback`.

6. No modal, o sistema apresenta:
   - editor rico para a descrição;
   - opções de classificação `Problema`, `Sugestão` e `Dúvida`;
   - área lateral para a captura de tela.

7. A classificação inicial do feedback é `Problema`.

8. Se houver captura disponível, o sistema mostra a prévia da imagem e o botão `Remover captura`.

9. Se não houver captura disponível, o sistema mostra a mensagem `Nenhuma captura anexada.`

10. O usuário informa a descrição do feedback.

11. O usuário pode alterar a classificação entre:
    - `Problema`
    - `Sugestão`
    - `Dúvida`

12. O usuário pode remover a captura antes do envio.

13. Ao acionar `Enviar feedback`, o sistema valida o conteúdo textual da descrição desconsiderando a marcação HTML.

14. Se a descrição contiver menos de 10 caracteres úteis, o sistema:
    - impede o envio;
    - mostra a mensagem `Descreva o problema com pelo menos 10 caracteres.`

15. Se a descrição for válida, o sistema monta o payload do feedback com:
    - `tipo`
    - `nota`
    - `metadados` automáticos da sessão e da navegação atual
    - `screenshot`, quando houver captura preservada

16. Os metadados enviados automaticamente incluem:
    - usuário autenticado
    - nome e caminho da rota atual
    - query da rota atual
    - título da página
    - perfil e unidade ativos
    - data/hora da captura do contexto
    - fuso horário
    - `userAgent`
    - largura e altura da janela
    - idioma do navegador

17. O sistema envia o feedback para `POST /api/feedback` em `multipart/form-data`, usando:
    - campo `data` com o payload JSON serializado;
    - campo `screenshot` com a imagem, quando houver.

18. Durante o envio:
    - o botão flutuante fica desabilitado e mostra estado de carregamento;
    - os controles do modal ficam desabilitados;
    - o botão `Enviar feedback` mostra indicador visual de envio.

19. Se o envio for concluído com sucesso, o sistema:
    - fecha o modal;
    - mostra o toast `Feedback enviado`;
    - muda temporariamente o botão flutuante para estado de sucesso.

20. Após o intervalo curto de confirmação visual, o botão flutuante volta ao estado normal.

21. Se o envio falhar, o sistema:
    - mantém ou retorna o botão flutuante para estado de erro temporário;
    - mostra o toast `Não foi possível enviar o feedback. Tente novamente.`

22. Após o intervalo curto de erro, o botão flutuante volta ao estado normal.

23. O usuário pode fechar o modal a qualquer momento por `Cancelar`, sem enviar o feedback.

## Observações

- Embora o domínio de feedback suporte o tipo `ELOGIO`, o widget atualmente não expõe essa opção ao usuário.
- O registro persistido usa sempre o usuário autenticado no servidor como fonte de `usuarioCodigo` e `usuarioNome`, ainda que metadados do cliente informem esses campos.
