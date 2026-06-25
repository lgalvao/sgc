# CDU-50 - Analisar diagnóstico

Ator: GESTOR, ADMIN

## Pré-condições

- Usuário logado com perfil GESTOR ou ADMIN
- Existência de processo de diagnóstico em andamento

## Fluxo principal

1. No `Painel`, o usuário clica em um processo de diagnóstico em andamento.

2. O sistema mostra a tela `Detalhes do processo`.

3. O usuário aciona uma unidade.

4. O sistema mostra a tela `Detalhes do subprocesso` para a unidade. Os elementos da tela serão:
    - cabeçalho com dados gerais do subprocesso e da unidade, como detalhado em [CDU-07](cdu-07.md),
    - botão `Histórico de análise`; sempre habilitado;
    - botão 'drop-down' `Ações`, que dá acesso às seguintes ações (habilitadas apenas se a localização do subprocesso
      for a unidade do usuário)
        - `Devolver para ajustes` para ambos GESTOR e ADMIN;
        - `Registrar aceite`, apenas para GESTOR;
        - `Homologar`, apenas para ADMIN;
    - lista dos servidores participantes da unidade, *exceto o responsável pela unidade*, com a sua situação individual;

5. O usuário aciona um servidor da lista.

6. O sistema mostra:
    - uma grade somente-leitura com as competências do mapa vigente da unidade, contendo, para cada competência:
        - cabeçalho com nome completo e título eleitoral do servidor selecionado;
        - descrição da competência;
        - colunas `Importância`, `Domínio` e `Situação de capacitação` com os valores referentes ao servidor
          selecionado;
    - seção `Movimentações`, com as movimentações do subprocesso;

---
Se o usuário acionar `Histórico de análise`:

5. O sistema mostra os registros prévios de análise do subprocesso, contendo data/hora, unidade, resultado e observação.

---
Se o usuário acionar `Devolver para ajustes`:

5A. O sistema identifica a unidade de devolução (referida como [SIGLA_UNIDADE_DEVOLUCAO]) como sendo a unidade de origem
da última movimentação do subprocesso.

6. O sistema abre um modal, com título "Devolução de diagnóstico" e texto "Confirma a devolução do diagnóstico da
   unidade [SIGLA_UNIDADE_SUBPROCESSO]?", campo `Justificativa` obrigatório e botões `Cancelar` e `Devolver`.

7. Caso o usuário confirme, o sistema:

7.1. Registra uma análise de validação para o subprocesso com:

- `Data/hora`: [Data/hora atual]
- `Unidade`: [SIGLA_UNIDADE_ATUAL]
- `Resultado`: 'Devolução para ajustes' 
- `Justificativa`: [Justificativa fornecida]

7.2. Muda a situação de todos os servidores da unidade que **nao estejam** na situação 'Avaliação impossibilitada', para
a situação 'Avaliação de consenso criada' (isso faz com o sistema habilite a edição das avaliações de consenso;
ver [CDU-44](cdu-45.md))

8. O sistema envia uma notificação por e-mail para a unidade de origem da última movimentação do subprocesso, seguindo
   este modelo:

```text
Assunto: SGC: Diagnóstico da unidade [SIGLA_UNIDADE_SUBPROCESSO] devolvido para ajustes

Prezado(a) responsável pela [SIGLA_UNIDADE_DEVOLUCAO],

O diagnóstico da unidade [SIGLA_UNIDADE_SUBPROCESSO] no processo [DESCRICAO_PROCESSO] foi devolvido para ajustes.

Realize as mudanças solicitadas, acessando o Sistema de Gestão de Competências (SGC): [URL_SISTEMA].
```

9. O sistema cria um alerta com estes campos:
    - `Descrição`: "Diagnóstico devolvido para ajustes"
    - `Processo`: [DESCRICAO_PROCESSO]
    - `Data/hora`: [Data/hora atual]
    - `Unidade de origem`: [SIGLA_UNIDADE_ATUAL]
    - `Unidade de destino`: [SIGLA_UNIDADE_DEVOLUCAO]

10. O sistema cria uma movimentação para o subprocesso com estes campos:
    - `Descrição`: 'Devolução para ajustes'
    - `Data/hora`: [Data/hora atual]
    - `Unidade origem`: [SIGLA_UNIDADE_ATUAL]
    - `Unidade destino`: [SIGLA_UNIDADE_DEVOLUCAO]

11. O sistema mostra o *toast* "Devolução realizada".

---

Se o usuário acionar `Registrar aceite`:

12. O sistema abre um modal, com título "Aceitar diagnóstico" e texto "Confirma o aceite do diagnóstico da
    unidade [SIGLA_UNIDADE_SUBPROCESSO]?", um campo `Observação` opcional e os botões `Cancelar` e `Aceitar`.

13. O sistema registra uma análise de validação para o subprocesso com estes campos:
    - `Resultado`: 'Aceite'
    - `Data/hora`: [Data/hora atual]
    - `Unidade`: [SIGLA_UNIDADE_ATUAL]
    - `Observação`: [Observação fornecida]

14. O sistema cria internamente um alerta com estes campos:
    - `Descrição`: "Diagnóstico da unidade [SIGLA_UNIDADE_SUBPROCESSO] aceito"
    - `Processo`: [DESCRICAO_PROCESSO]
    - `Data/hora`: [Data/hora atual]
    - `Unidade de origem`: [SIGLA_UNIDADE_ATUAL]
    - `Unidade de destino`: [SIGLA_UNIDADE_SUPERIOR]

15. O sistema envia uma notificação por e-mail para a unidade superior seguindo este modelo:

   ```text
   Assunto: SGC: Diagnóstico da unidade [SIGLA_UNIDADE_SUBPROCESSO] aceito

   Prezado(a) responsável pela [SIGLA_UNIDADE_SUPERIOR],

   O diagnóstico da unidade [SIGLA_UNIDADE_SUBPROCESSO], no processo [DESCRICAO_PROCESSO], foi submetido para análise.

   Realize a análise acessando o Sistema de Gestão de Competências (SGC): [URL_SISTEMA].
   ```

16. O sistema cria uma movimentação para o subprocesso com estes campos:
    - `Descrição`: 'Aceite do diagnóstico'
    - `Data/hora`: Data/hora atual
    - `Unidade origem`: [SIGLA_UNIDADE_ATUAL]
    - `Unidade destino`: [SIGLA_UNIDADE_SUPERIOR]

17. O sistema mostra o *toast* "Aceite registrado".

---

Se o usuário optar por `Homologar` (apenas perfil ADMIN):

18. O sistema abre um modal com título "Homologar diagnóstico" e texto "Confirma a homologação diagnóstico da
    unidade [SIGLA_UNIDADE_SUBPROCESSO]?" e botões `Cancelar` e `Homologar`.

19. O sistema registra uma análise de validação para o subprocesso com estes campos:
    - `Resultado`: 'Homologação'
    - `Data/hora`: [Data/hora atual]
    - `Unidade`: [SIGLA_UNIDADE_ATUAL]

20. O sistema cria uma movimentação para o subprocesso com estes campos:
    - `Descrição`: 'Homologação de diagnóstico'
    - `Data/hora`: [Data/hora atual]
    - `Unidade origem`: [SIGLA_UNIDADE_ATUAL]
    - `Unidade destino`: [SIGLA_UNIDADE_ATUAL]

21. O sistema mostra o *toast* "Diagnóstico homologado".