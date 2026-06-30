# CDU-50 - Analisar diagnóstico

## Atores

- GESTOR
- ADMIN

## Pré-condições

- Usuário logado com perfil GESTOR ou ADMIN
- Existência de processo de diagnóstico em andamento

## Fluxo principal

1. No `Painel`, o usuário clica em um processo de diagnóstico em andamento.

2. O sistema mostra a tela `Detalhes do processo`, como especificado no [CDU-06](cdu-06.md).

3. O usuário aciona uma unidade na tabela hierárquica.

4. O sistema mostra a tela `Detalhes do subprocesso` para a unidade. Os elementos da tela serão:
    - cabeçalho com dados gerais do subprocesso e da unidade, como especificado no [CDU-07](cdu-07.md),
    - botão `Histórico de análise`; sempre habilitado;
    - botão 'drop-down' `Ações`, que dá acesso às seguintes ações (habilitadas apenas se a localização do subprocesso
      for a unidade do usuário)
        - `Devolver para ajustes` para ambos GESTOR e ADMIN;
        - `Registrar aceite`, apenas para GESTOR;
        - `Homologar`, apenas para ADMIN;
    - lista dos servidores participantes da unidade, *exceto o responsável pela unidade*, com a sua situação individual.

5. O usuário aciona um servidor da lista.

6. O sistema mostra:
    - cabeçalho com nome completo e título eleitoral do servidor selecionado;
    - grade com as competências do mapa vigente da unidade; uma linha para cada competência, com:
        - descrição da competência;
        - colunas `Importância`, `Domínio` e `Situação de capacitação` referentes ao servidor selecionado;
    - seção `Movimentações`, com as movimentações do subprocesso;

---
Se o usuário acionar `Histórico de análise`:

- O sistema mostra os registros prévios de análise do subprocesso, contendo data/hora, unidade, resultado e
   observação/justificativa.

---
Se o usuário acionar `Devolver para ajustes`:

- O sistema identifica a unidade de devolução como sendo a unidade de origem da última movimentação do subprocesso
   (referida como :SIGLA_UNIDADE_DEVOLUCAO:).

7. O sistema mostra um modal com título `Devolução de diagnóstico` e texto "Confirma a devolução do diagnóstico da
   unidade :SIGLA_UNIDADE_SUBPROCESSO:?", campo `Justificativa` obrigatório e botões `Cancelar` e `Devolver`.

8. O usuário aciona `Devolver`, e o sistema realiza estas ações:

   8.1. Registra uma análise de validação para o subprocesso:
    - `Data/hora`: [Data/hora atual]
    - `Unidade`: :SIGLA_UNIDADE_ATUAL:
    - `Resultado`: 'Devolução para ajustes'
    - `Justificativa`: [Justificativa fornecida]

   8.2. Muda a situação de todos os servidores da unidade que **nao estejam** na situação 'Avaliação impossibilitada',
   para 'Autoavaliação concluída'. (Isso faz com o sistema volte a habilitar a edição das avaliações de consenso.)

9. O sistema envia notificação por e-mail para a unidade de origem da última movimentação do subprocesso
   (:SIGLA_UNIDADE_DEVOLUCAO:):
    ```text
    Assunto: SGC: Diagnóstico devolvido para ajustes
    
    Prezado(a) responsável pela :SIGLA_UNIDADE_DEVOLUCAO:,
    
    O diagnóstico da sua unidade, no processo :DESCRICAO_PROCESSO:, foi devolvido para ajustes.
    
    Realize as mudanças solicitadas no Sistema de Gestão de Competências (SGC): :URL_SISTEMA:.
    ```

10. O sistema cria um alerta:
    - `Descrição`: "Diagnóstico devolvido para ajustes"
    - `Processo`: :DESCRICAO_PROCESSO:
    - `Data/hora`: [Data/hora atual]
    - `Unidade de origem`: :SIGLA_UNIDADE_ATUAL:
    - `Unidade de destino`: :SIGLA_UNIDADE_DEVOLUCAO:

11. O sistema cria uma movimentação para o subprocesso:
    - `Descrição`: 'Devolução para ajustes'
    - `Data/hora`: [Data/hora atual]
    - `Unidade origem`: :SIGLA_UNIDADE_ATUAL:
    - `Unidade destino`: :SIGLA_UNIDADE_DEVOLUCAO:

    Isso muda a localização do subprocesso para a unidade de devolução.

12. O sistema mostra o *toast* "Devolução realizada".

---

Se o usuário acionar `Registrar aceite`:

13. O sistema abre um modal, com título `Aceitar diagnóstico` e texto "Confirma o aceite do diagnóstico da
    unidade :SIGLA_UNIDADE_SUBPROCESSO:?" e os botões `Cancelar` e `Aceitar`.

14. O usuário aciona `Aceitar`.

15. O sistema registra uma análise de validação para o subprocesso:
    - `Resultado`: 'Aceite'
    - `Data/hora`: [Data/hora atual]
    - `Unidade`: :SIGLA_UNIDADE_ATUAL:

16. O sistema cria um alerta:
    - `Descrição`: "Diagnóstico da unidade :SIGLA_UNIDADE_SUBPROCESSO: aceito"
    - `Processo`: :DESCRICAO_PROCESSO:
    - `Data/hora`: [Data/hora atual]
    - `Unidade de origem`: :SIGLA_UNIDADE_ATUAL:
    - `Unidade de destino`: :SIGLA_UNIDADE_SUPERIOR:

17. O sistema cria uma movimentação para o subprocesso com estes campos:
    - `Descrição`: 'Aceite'
    - `Data/hora`: Data/hora atual
    - `Unidade origem`: :SIGLA_UNIDADE_ATUAL:
    - `Unidade destino`: :SIGLA_UNIDADE_SUPERIOR:

    Isso muda a localização do subprocesso para a unidade superior.

18. O sistema envia notificação por e-mail para a unidade superior:

   ```text
   Assunto: SGC: Diagnóstico da unidade :SIGLA_UNIDADE_SUBPROCESSO: aceito

   Prezado(a) responsável pela :SIGLA_UNIDADE_SUPERIOR:,

   O diagnóstico da unidade :SIGLA_UNIDADE_SUBPROCESSO:, no processo :DESCRICAO_PROCESSO:,
   foi submetido para análise.

   Realize a análise acessando o Sistema de Gestão de Competências (SGC): :URL_SISTEMA:.
   ```

19. O sistema mostra o *toast* "Aceite registrado".

---

Se o usuário optar por `Homologar` (apenas perfil ADMIN):

20. O sistema mostra um modal com título `Homologar diagnóstico` e texto "Confirma a homologação diagnóstico da
    unidade :SIGLA_UNIDADE_SUBPROCESSO:?" e botões `Cancelar` e `Homologar`.

21. O usuário aciona `Homologar`.

22. O sistema registra uma análise de validação para o subprocesso:
    - `Resultado`: 'Homologação'
    - `Data/hora`: [Data/hora atual]
    - `Unidade`: :SIGLA_UNIDADE_ATUAL:

23. O sistema cria uma movimentação para o subprocesso com estes campos:
    - `Descrição`: 'Homologação'
    - `Data/hora`: [Data/hora atual]
    - `Unidade origem`: ADMIN
    - `Unidade destino`: ADMIN

    A localização do subprocesso não é alterada.

24. O sistema mostra o *toast* "Diagnóstico homologado".
