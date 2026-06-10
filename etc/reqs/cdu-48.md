# CDU-48 - Concluir diagnóstico da unidade

Ator: CHEFE

## Pré-condições

- Usuário logado com perfil CHEFE
- Existência de processo de diagnóstico em andamento envolvendo a unidade do usuário
- Subprocesso da unidade localizado na unidade do usuário
- Todos os servidores da unidade com avaliação individual na situação 'Avaliação de consenso aprovada' ou 'Avaliação
  impossibilitada'
- Situação de capacitação preenchida para todos os servidores/competências da unidade

## Fluxo principal

1. No `Painel`, o usuário acessa um processo de diagnóstico em andamento

2. O sistema mostra a tela  `Detalhes do subprocesso`, conforme o caso de uso [CDU-42.md](cdu-42.md)`.

2. O usuário clica em `Concluir diagnóstico`.

3. O sistema verifica se todas as autoavaliações estão com a situação `Avaliação de consenso aprovada` ou
   `Avaliação impossibilitada`; também verifica se as situações de capacitação foram preenchidas para todas as
   competências.

   3.1. Caso existam campos não preenchidos, o sistema mostra a mensagem
   `Ainda existem avaliações e situações de capacitações não preenchidas.`, interrompe a operação e permanece na tela
   `Detalhes do subprocesso`.

   3.2. Caso a verificação confirme que está tudo preenchido, o sistema mostra um modal de confirmação com:
    - título `Conclusão de diagnóstico`;
    - texto `Confirma a conclusão do diagnóstico da unidade?`;
    - botões `Cancelar` e `Concluir`.

   Se o usuário escolher `Cancelar`, o sistema interrompe a operação e permanece na mesma tela.

4. O usuário aciona `Concluir`.

5. O sistema altera a situação do subprocesso para `Concluído`.

6. O sistema registra uma movimentação para o subprocesso com:
    - `Data/hora`: [Data/hora atual];
    - `Unidade origem`: [SIGLA_UNIDADE_SUBPROCESSO];
    - `Unidade destino`: [SIGLA_UNIDADE_SUPERIOR];
    - `Descrição`: `Diagnóstico concluído`.

7. O sistema envia uma notificação por e-mail para a unidade superior:

   ```text
   Assunto: SGC: Diagnóstico da unidade [SIGLA_UNIDADE_SUBPROCESSO] submetido para análise

   Prezado(a) responsável pela [SIGLA_UNIDADE_SUPERIOR],

   O diagnóstico da unidade [SIGLA_UNIDADE_SUBPROCESSO] no processo [DESCRICAO_PROCESSO] foi concluído e submetido à análise das unidades superiores.

   Realize a análise acessando o Sistema de Gestão de Competências (SGC): [URL_SISTEMA].
   ```

8. O sistema cria internamente um alerta com:
    - `Descrição`: "Diagnóstico da unidade [SIGLA_UNIDADE_SUBPROCESSO] submetido para análise"
    - `Processo`: [DESCRICAO_PROCESSO]
    - `Data/hora`: [Data/hora atual]
    - `Unidade de origem`: [SIGLA_UNIDADE_SUBPROCESSO]
    - `Unidade de destino`: [SIGLA_UNIDADE_SUPERIOR]

9. O sistema redireciona para o `Painel` e mostra a mensagem `Diagnóstico concluído`.
