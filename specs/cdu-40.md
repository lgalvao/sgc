# CDU-40 - Consultar feedbacks enviados

## Atores

- ADMIN

## Descrição

Permite ao administrador consultar os feedbacks enviados pelos usuários, visualizar seus detalhes, inspecionar metadados
de contexto e visualizar a captura de tela associada, de forma ampliada, quando ela estiver disponível no servidor.

## Pré-condições

- Perfil de homologação ou testes. Funcionalidade NÃO disponível em produção
- Usuário autenticado com perfil `ADMIN`.
- Módulo de feedback disponível no ambiente atual.

## Fluxo principal

1. O usuário aciona `Feedbacks` no menu `Ações especiais`.

2. O sistema mostra a tela `Feedbacks`.

3. Ao abrir a tela, o sistema consulta até 100 feedbacks recentes registrados no sistema.

4. Enquanto a consulta estiver em andamento, o sistema mostra o estado de carregamento da página.

5. Se a consulta falhar, o sistema mostra um alerta de erro com a mensagem retornada pela API.

6. Se não houver feedbacks para exibir, o sistema mostra o estado vazio `Nenhum feedback registrado`.

7. Se houver feedbacks, o sistema apresenta uma listagem com as colunas:
    - `Tipo`
    - `Usuário`
    - `Nota`
    - `Rota`
    - `Enviado em`
    - ação `Detalhes`

8. O sistema ordena os feedbacks do mais recente para o mais antigo.

9. Na listagem:
    - o tipo é apresentado com rótulo textual, cor e ícone;
    - o usuário é apresentado com nome e código;
    - a nota é resumida em texto simples, removendo marcações HTML;
    - a data/hora é apresentada em formato brasileiro.

10. O usuário pode acionar `Atualizar` para recarregar manualmente a listagem.

11. Ao acionar `Detalhes`, o sistema abre o modal `Detalhes do feedback`.

12. No modal, o sistema mostra:
    - `Tipo`
    - `Usuário`
    - `Rota`
    - `Enviado em`
    - `Captura`
    - `Nota`

13. O campo `Nota` do modal preserva a formatação HTML originalmente enviada no feedback.

14. No campo `Captura`:
    - se houver screenshot disponível no servidor, o sistema mostra a miniatura da imagem;
    - se não houver screenshot disponível, o sistema mostra `Não disponível no servidor`.

15. Ao clicar na miniatura da captura, o sistema abre um segundo modal com a imagem ampliada.

16. Se houver `metadataJson`, o sistema mostra uma seção `Metadados` em formato tabular.

17. Na apresentação dos metadados, o sistema aplica estes tratamentos:
    - combina `rotaCaminho` e `rotaQuery` em um único campo `Rota`;
    - combina `perfilAtivo` e `unidadeAtiva` em um único campo `Acesso`;
    - combina `larguraTela` e `alturaTela` em um único campo `Resolução`;
    - traduz algumas chaves técnicas para rótulos amigáveis, como `tituloPagina` e `idioma`;
    - resume `userAgent` em uma identificação amigável de navegador e sistema operacional;
    - oculta chaves internas redundantes já absorvidas pela apresentação amigável.

18. Se `metadataJson` estiver ausente, o sistema não mostra a seção `Metadados`.
