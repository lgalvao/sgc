# CDU-48 - Preencher situações de capacitação

## Atores

- CHEFE

## Pré-condições

- Login realizado com perfil CHEFE
- Processo de diagnóstico em andamento com participação da unidade do usuário
- Subprocesso localizado na unidade do usuário
- Existência de ao menos um servidor da unidade com avaliação de consenso aprovada

## Fluxo principal

1. No `Painel`, o usuário acessa um processo de diagnóstico em andamento.

2. O sistema mostra a tela `Detalhes do subprocesso`, conforme o caso de uso [CDU-42.md](cdu-42.md)`.

3. O usuário aciona o card `Situação de capacitação`.

4. O sistema mostra a tela `Situação de capacitação`, com duas regiões:

- `Lista de servidores`, com os nomes e situações de cada servidor da unidade, com os servidores em situação
  'Avaliação de consenso aprovada' agrupados no início. Nenhum servidor deve estar selecionado inicialmente.
- `Painel de competências`, com o texto inicial "Selecione um servidor para preencher sua situação de capacitação."

5. O usuário aciona um servidor na `Lista de servidores`.

---

5.1. Se o usuário acionar um servidor com situação **diferente** de 'Avaliação de consenso aprovada'.

5.2. O sistema mostra, no `Painel de competências`, um painel com título "Aguardando aprovação de consenso" e
subtítulo "A situação de capacitação só pode ser preenchida após o servidor aprovar a avaliação de consenso."

---
5.3. Se o usuário acionar um servidor com situação 'Avaliação de consenso aprovada'.

5.4. O sistema mostra, no `Painel de competências`, com um cabeçalho com nome completo e título eleitoral do servidor; e
uma grade com uma linha para cada competência vigente da unidade, com os itens:

- descrição da competência
- campos somente-leitura com o consenso de `Importância` e `Domínio` do servidor
- campo *drop-down* para a `Situação de capacitação` do servidor, admitindo os seguintes valores:
    - `NA - Não se aplica`;
    - `AC - A capacitar`;
    - `EC - Em capacitação`;
    - `C - Capacitado`;
    - `I - Instrutor`.

5.5. O usuário informa os valores de situação de capacitação para cada competência.

5.6. O sistema salva automaticamente cada alteração realizada.

---

6. O usuário escolhe outro servidor e repete o procedimento.
   
7. O usuário aciona `Voltar`.

8. O sistema mostra a tela `Detalhes do subprocesso`. 
