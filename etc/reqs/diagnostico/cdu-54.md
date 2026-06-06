# CDU-54 - Gerar relatório de situação de capacitação

Ator: ADMIN, GESTOR

Maturidade: Média

Base principal: fluxo negocial acordado no PDF e discussão da reunião sobre uso agregado dos dados de capacitação.

## Pré-condições

- Login realizado com perfil ADMIN ou GESTOR
- Existência de processo de diagnóstico finalizado ou unidade homologada com dados consolidados disponíveis

## Fluxo principal

1. O usuário acessa a área de relatórios de diagnóstico.

2. O usuário seleciona o relatório `Situação de capacitação`.

3. O sistema mostra a tela `Relatório de situação de capacitação`, contendo:
   - seletor de processo de diagnóstico;
   - filtros de escopo compatíveis com o perfil do usuário;
   - botão `Gerar`;
   - opções de exportação disponíveis.

4. Para o perfil GESTOR, o escopo do relatório se limita à própria unidade e às unidades subordinadas acessíveis ao usuário.

5. Para o perfil ADMIN, o escopo do relatório pode abranger todas as unidades participantes do processo.

6. O usuário seleciona o processo, o escopo desejado e clica em `Gerar`.

7. O sistema apresenta prévia agregada do relatório, sem identificação nominal de servidores, contendo:
   - processo selecionado;
   - unidade ou conjunto de unidades abrangidas;
   - competência;
   - quantitativos por situação de capacitação: `NA`, `AC`, `EC`, `C` e `I`.

8. O usuário exporta o relatório em formato disponível.

9. O sistema gera arquivo institucional com identificação do sistema, processo, data/hora de geração, usuário gerador e dados agregados do relatório.

## Observação

- O relatório oficial de situação de capacitação deve ser agregado e sem nomes. Consulta nominal, se necessária, deve ocorrer dentro do sistema e sob regra de acesso.
- A reunião relaciona a situação de capacitação à análise posterior de efetividade: depois de propor capacitação, interessa observar se os gaps diminuíram em ciclos seguintes.
- Comparações históricas entre ciclos devem ser tratadas como expansão deste relatório ou como requisito complementar.
