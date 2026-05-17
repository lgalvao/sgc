# CDU-47 - Concluir diagnóstico da unidade

Ator: CHEFE

## Pré-condições

- Login realizado com perfil CHEFE
- Processo de diagnóstico em andamento
- Subprocesso com localização atual na própria unidade
- Todos os servidores da unidade com avaliação individual em `Consenso aprovado` ou `Avaliação impossibilitada`
- Informações de ocupações críticas preenchidas para todos os servidores e competências da unidade

## Fluxo principal

1. No `Detalhes do subprocesso`, o usuário clica no card `Diagnóstico da equipe`.

2. O sistema mostra a tela `Diagnóstico da equipe`, com o botão `Concluir diagnóstico da unidade`.

3. O usuário clica em `Concluir diagnóstico da unidade`.

4. O sistema verifica se todas as avaliações individuais estão em `Consenso aprovado` ou `Avaliação impossibilitada`,
   e se as ocupações críticas foram integralmente preenchidas.

5. Caso positivo, o sistema mostra modal de confirmação com:
   - título `Concluir diagnóstico da unidade`;
   - texto `Confirma a conclusão do diagnóstico da unidade [SIGLA_UNIDADE_SUBPROCESSO]?`;
   - botões `Cancelar` e `Concluir`.

6. Caso o usuário escolha `Cancelar`, o sistema interrompe a operação e permanece na mesma tela.

7. O usuário confirma.

8. O sistema altera a situação do subprocesso para 'Concluído'.

9. O sistema registra uma movimentação para o subprocesso com:
   - `Data/hora`: [Data/hora atual];
   - `Unidade origem`: [SIGLA_UNIDADE_SUBPROCESSO];
   - `Unidade destino`: [SIGLA_UNIDADE_SUPERIOR];
   - `Descrição`: 'Diagnóstico concluído'.

10. O sistema envia notificação por e-mail para a unidade superior:

    ```text
    Assunto: SGC: Diagnóstico da unidade [SIGLA_UNIDADE_SUBPROCESSO] submetido para análise

    Prezado(a) responsável pela [SIGLA_UNIDADE_SUPERIOR],

    O diagnóstico da unidade [SIGLA_UNIDADE_SUBPROCESSO] no processo [DESCRICAO_PROCESSO] foi concluído e submetido para análise.

    A análise já pode ser realizada no Sistema de Gestão de Competências ([URL_SISTEMA]).
    ```

11. O sistema cria internamente um alerta com:
    - `Descrição`: "Diagnóstico da unidade [SIGLA_UNIDADE_SUBPROCESSO] submetido para análise"
    - `Processo`: [DESCRICAO_PROCESSO]
    - `Data/hora`: [Data/hora atual]
    - `Unidade de origem`: [SIGLA_UNIDADE_SUBPROCESSO]
    - `Unidade de destino`: [SIGLA_UNIDADE_SUPERIOR]

12. O sistema redireciona para o `Painel` e mostra a mensagem `Diagnóstico concluído`.

## Fluxo alternativo

1. No passo 4, caso exista pendência, o sistema mostra a mensagem `Ainda existem avaliações ou ocupações críticas
   pendentes.` e interrompe a operação.
