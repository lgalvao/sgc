# CDU-48 - Concluir diagnóstico de uma unidade

Ator: CHEFE

## Pré-condições

- Usuário logado com perfil CHEFE
- Existência de processo de diagnóstico em andamento envolvendo a unidade do usuário
- Subprocesso da unidade localizado na unidade do usuário

## Fluxo principal

1. No `Painel`, o usuário acessa um processo de diagnóstico em andamento

2. O sistema mostra a tela `Detalhes do subprocesso`, conforme o caso de uso [CDU-42.md](cdu-42.md)`.

2. O usuário aciona o botão `Concluir diagnóstico`.

3. O sistema verifica se todas os servidores estão com a situação 'Avaliação de consenso aprovada' ou
   'Avaliação impossibilitada'; e se as situações de capacitação foram preenchidas para todas as competências.

3.1. Caso haja campos não preenchidos ou situações dos servidores fora das especificadas acima, o sistema mostra a
mensagem "Ainda existem avaliações e situações de capacitações não preenchidas.", interrompe a operação e permanece na
tela `Detalhes do subprocesso`.

3.2. Caso esteja tudo preenchido, o sistema mostra um modal de confirmação:

- título: "Conclusão de diagnóstico";
- texto: "Confirma a conclusão do diagnóstico da unidade?";
- botões `Cancelar` e `Concluir diagnóstico`.

4. O usuário aciona `Concluir diagnóstico`.

5. O sistema altera a situação do subprocesso para `Concluído`.

6. O sistema registra uma movimentação para o subprocesso com:
    - `Descrição`: "Diagnóstico concluído para a unidade [SIGLA_UNIDADE_SUBPROCESSO] ".
    - `Data/hora`: [Data/hora atual];
    - `Unidade origem`: [SIGLA_UNIDADE_SUBPROCESSO];
    - `Unidade destino`: [SIGLA_UNIDADE_SUPERIOR];

7. O sistema envia uma notificação por e-mail para a unidade imediatamente superior:

   ```text
   Assunto: SGC: Diagnóstico da unidade [SIGLA_UNIDADE_SUBPROCESSO] submetido para análise

   Prezado(a) responsável pela [SIGLA_UNIDADE_SUPERIOR],

   O diagnóstico da unidade [SIGLA_UNIDADE_SUBPROCESSO] no processo [DESCRICAO_PROCESSO] foi concluído e submetido à análise das unidades superiores.

   Realize a análise acessando o Sistema de Gestão de Competências (SGC): [URL_SISTEMA].
   ```

8. O sistema cria internamente um alerta:
    - `Descrição`: "Diagnóstico da unidade [SIGLA_UNIDADE_SUBPROCESSO] submetido para análise"
    - `Processo`: [DESCRICAO_PROCESSO]
    - `Data/hora`: [Data/hora atual]
    - `Unidade de origem`: [SIGLA_UNIDADE_SUBPROCESSO]
    - `Unidade de destino`: [SIGLA_UNIDADE_SUPERIOR]

9. O sistema redireciona para o `Painel` e mostra a mensagem "Diagnóstico concluído".