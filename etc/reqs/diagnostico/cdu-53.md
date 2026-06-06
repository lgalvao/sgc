# CDU-53 - Gerar relatório de gaps de diagnóstico

Ator: ADMIN, GESTOR

Maturidade: Média

Base principal: fluxo negocial acordado no PDF e discussão da reunião sobre relatórios agregados, histórico e anonimização.

## Pré-condições

- Login realizado com perfil ADMIN ou GESTOR
- Existência de processo de diagnóstico finalizado ou unidade homologada com dados consolidados disponíveis

## Fluxo principal

1. O usuário acessa a área de relatórios de diagnóstico.

2. O usuário seleciona o relatório `Gaps de diagnóstico`.

3. O sistema mostra a tela `Relatório de gaps de diagnóstico`, contendo:
   - seletor de processo de diagnóstico;
   - filtros de escopo compatíveis com o perfil do usuário;
   - botão `Gerar`;
   - opções de exportação disponíveis.

4. Para o perfil GESTOR, o escopo do relatório se limita à própria unidade e às unidades subordinadas acessíveis ao usuário.

5. Para o perfil ADMIN, o escopo do relatório pode abranger todas as unidades participantes do processo.

6. O usuário seleciona o processo, o escopo desejado e clica em `Gerar`.

7. O sistema calcula os gaps de competência a partir dos dados homologados do diagnóstico.

8. O sistema apresenta prévia agregada do relatório, sem identificação nominal de servidores, contendo:
   - processo selecionado;
   - unidade ou conjunto de unidades abrangidas;
   - competência;
   - indicador agregado de gap;
   - quantidade de servidores considerada no agrupamento.

9. O usuário exporta o relatório em formato disponível.

10. O sistema gera arquivo institucional com identificação do sistema, processo, data/hora de geração, usuário gerador e dados agregados do relatório.

## Observação

- O relatório oficial de gaps deve ser agregado e sem nomes. A reunião indica que, quando houver necessidade de ver nomes, isso deve ocorrer por consulta no sistema e sob regra de acesso, não por relatório institucional exportável.
- A fórmula exata de cálculo do gap ainda precisa ser validada como regra funcional. A versão anterior mencionava `Importância - Domínio`, mas o material revisado sustenta apenas a necessidade de cálculo de gap, não a fórmula final.
- A reunião indica interesse em comparação histórica entre ciclos e em avaliar efetividade de capacitações. Isso deve ser tratado como expansão do relatório ou como requisito complementar.
