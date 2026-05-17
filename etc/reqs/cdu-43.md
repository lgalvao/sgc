# CDU-43 - Consultar avaliação de consenso

Ator: SERVIDOR

## Pré-condições

- Login realizado com perfil SERVIDOR
- Existência de avaliação de consenso criada para o próprio servidor

## Fluxo principal

1. No `Painel`, o usuário acessa um processo de diagnóstico em andamento.

2. O sistema mostra a tela `Detalhes do subprocesso` da unidade do servidor.

3. O usuário clica no card `Diagnóstico da equipe`.

4. O sistema mostra a tela `Autoavaliação de diagnóstico`, em modo de consulta do consenso vigente do próprio
   servidor.

5. O sistema apresenta, para cada competência:
   - a descrição da competência;
   - o detalhamento das atividades e conhecimentos, quando solicitado;
   - os valores de `Importância` e `Domínio` registrados no consenso vigente.

6. O usuário visualiza apenas o seu próprio consenso.

## Observação

PENDÊNCIA DE REFINAMENTO: esta especificação presume que a consulta do consenso reutiliza a mesma tela base da
autoavaliação em modo de leitura. Confirmar depois se a área de negócio deseja uma tela distinta ou informações
adicionais de histórico.
