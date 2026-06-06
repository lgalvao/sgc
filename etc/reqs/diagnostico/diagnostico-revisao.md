# Diagnóstico - revisão dos requisitos

## Objetivo

Consolidar anotações de revisão dos requisitos de diagnóstico de competências técnicas a partir de três insumos:

- fluxo negocial acordado com os clientes em [SGC_ Fluxo de Diagnóstico de Competências Técnicas.pdf](/Users/leonardo/Downloads/SGC_%20Fluxo%20de%20Diagno%CC%81stico%20de%20Compete%CC%82ncias%20Te%CC%81cnicas.pdf);
- casos de uso já existentes em [etc/reqs](/Users/leonardo/sgc/etc/reqs) e [etc/reqs/diagnostico](/Users/leonardo/sgc/etc/reqs/diagnostico);
- transcrição automática da reunião correta sobre o fluxo de diagnóstico.

Este documento não substitui os CDUs. Ele é um caderno de revisão para separar:

- o que já parece consolidado;
- o que conflita com o fluxo negocial;
- o que ainda precisa de confirmação pela transcrição completa ou por validação com a área.

## Escopo atual

O conjunto relevante de casos de uso é:

- [cdu-41.md](/Users/leonardo/sgc/etc/reqs/cdu-41.md) a [cdu-49.md](/Users/leonardo/sgc/etc/reqs/cdu-49.md): mais consolidados e já refletidos em implementação, mas ainda revisáveis;
- [cdu-50.md](/Users/leonardo/sgc/etc/reqs/diagnostico/cdu-50.md) a [cdu-54.md](/Users/leonardo/sgc/etc/reqs/diagnostico/cdu-54.md): mais crus e com sinais claros de extrapolação ou detalhamento prematuro.

## Síntese do fluxo negocial já confirmada

Com base no PDF, o fluxo negocial hoje já sustenta estas afirmações:

1. A SEDOC cria e inicia processos de diagnóstico, podendo haver mais de um processo simultâneo.
2. Todas as unidades participantes recebem prazo inicial comum, mas a SEDOC pode ajustar prazo de unidade individualmente.
3. A SEDOC tem visão global da árvore hierárquica e do andamento das unidades, com monitoramento orientado por prazo.
4. O servidor realiza autoavaliação por competência, preenchendo `Importância` e `Domínio`.
5. O CHEFE acompanha o andamento de todos os servidores da unidade.
6. O CHEFE cria e mantém a avaliação de consenso a partir da autoavaliação concluída.
7. O servidor consulta apenas a própria avaliação de consenso e a aprova quando estiver de acordo.
8. Se o CHEFE editar uma avaliação de consenso após a aprovação do servidor, a aprovação precisa ser refeita.
9. O CHEFE também informa a situação de capacitação por competência.
10. O diagnóstico da unidade só pode ser concluído quando todos os consensos estiverem aprovados e a situação de capacitação tiver sido preenchida, ressalvada a impossibilidade de avaliação de algum servidor.
11. O GESTOR acompanha a árvore das unidades subordinadas, podendo validar ou devolver diagnósticos.
12. O fluxo de validação sobe hierarquicamente até a SEDOC, que homologa.
13. Após a homologação, o sistema calcula gaps de competência e quantidade de ocupações críticas para fins de relatório.

## Pontos já consolidados nos CDUs e aderentes ao fluxo

Os pontos abaixo parecem estáveis e só devem sofrer ajuste redacional fino, não mudança conceitual:

- [cdu-41.md](/Users/leonardo/sgc/etc/reqs/cdu-41.md): início do processo pelo `ADMIN` com notificações para unidades operacionais e intermediárias.
- [cdu-42.md](/Users/leonardo/sgc/etc/reqs/cdu-42.md): autoavaliação individual do servidor.
- [cdu-44.md](/Users/leonardo/sgc/etc/reqs/cdu-44.md) e [cdu-45.md](/Users/leonardo/sgc/etc/reqs/cdu-45.md): manutenção e aprovação da avaliação de consenso.
- [cdu-46.md](/Users/leonardo/sgc/etc/reqs/cdu-46.md): impossibilidade de avaliação como mecanismo excepcional para não travar a unidade.
- [cdu-47.md](/Users/leonardo/sgc/etc/reqs/cdu-47.md): preenchimento da situação de capacitação.
- [cdu-48.md](/Users/leonardo/sgc/etc/reqs/cdu-48.md): conclusão do diagnóstico da unidade condicionada a consensos aprovados ou impossibilitados e à situação de capacitação preenchida.
- [cdu-49.md](/Users/leonardo/sgc/etc/reqs/cdu-49.md): acompanhamento de unidades subordinadas por árvore e detalhamento do subprocesso.

## Ajustes já evidentes, mesmo antes da transcrição completa

### UX e navegação

- Os requisitos não devem mais depender de card `Monitoramento`.
- O monitoramento de diagnóstico precisa ser descrito inline no detalhe do subprocesso.
- A árvore de acompanhamento deve enfatizar situação do subprocesso; campos como `localização atual` só entram se forem realmente relevantes para o negócio, e hoje não parecem ser.

### Visibilidade dos dados

O PDF confirma apenas que:

- o servidor consulta a própria avaliação de consenso;
- o gestor visualiza o andamento das unidades subordinadas e pode detalhá-las;
- o fluxo sobe hierarquicamente até a SEDOC.

Com isso, ainda não está automaticamente confirmado que perfis superiores possam ver todos os valores brutos de autoavaliação. A formulação atual de [cdu-50.md](/Users/leonardo/sgc/etc/reqs/diagnostico/cdu-50.md), que restringe a visualização a apenas consenso vigente, parece plausível, mas ainda precisa ser confirmada como decisão explícita.

### Conclusão do diagnóstico

O fluxo negocial deixa claro que:

- a conclusão da unidade depende do término das avaliações aplicáveis;
- a impossibilidade é exceção para destravar a conclusão;
- a unidade superior é notificada quando a unidade conclui.

Os recortes já transcritos da reunião reforçam uma leitura importante:

- as avaliações podem ocorrer em paralelo;
- a sinalização de conclusão não pode ocorrer antes de todos os participantes necessários terem terminado.

Isso sustenta o comportamento já descrito em [cdu-48.md](/Users/leonardo/sgc/etc/reqs/cdu-48.md) e também deve influenciar a redação de análise hierárquica em [cdu-50.md](/Users/leonardo/sgc/etc/reqs/diagnostico/cdu-50.md) e [cdu-51.md](/Users/leonardo/sgc/etc/reqs/diagnostico/cdu-51.md).

## Qualidade da transcrição

A transcrição automática completa foi gerada em `/tmp/reuniao-diagnostico/reuniao-diagnostico-mlx.txt`.

O áudio é útil para extração negocial, mas a transcrição tem ruído relevante:

- troca palavras frequentes, como `avaliação`, `gestor`, `SEDOC`, `C13` e nomes de unidades;
- preserva razoavelmente a sequência das decisões e perguntas;
- degrada muito no trecho final, com repetições longas de `não`, que devem ser descartadas;
- deve ser usada como apoio de análise, não como ata literal.

As anotações abaixo tratam a transcrição como evidência de direção negocial. Onde o PDF já confirma a regra, a fonte principal continua sendo o PDF.

## Achados da reunião

Trechos já analisados da reunião correta apontam estes temas:

1. Sigilo e transparência ainda demandam formulação cuidadosa.
   - Houve discussão explícita sobre "grau de sigilo", sobre quem vê quais avaliações e sobre tensão entre privacidade e transparência.
   - Isso sugere que os CDUs devem separar claramente:
     - visibilidade do próprio servidor;
     - visibilidade da chefia da unidade;
     - visibilidade hierárquica superior;
     - eventual impossibilidade de expor avaliações individualizadas para além da unidade.
   - O áudio também indica uma diferença importante entre consulta no sistema e relatório oficial: o detalhe nominal pode existir para quem tem permissão de consulta, mas o relatório para circulação institucional tende a ser agregado e sem nomes.

2. A expressão `avaliação da equipe` tende a ser ambígua.
   - A reunião sugere que ela significa, na prática, a avaliação que a equipe faz do gestor.
   - Convém revisar nomenclaturas de tela, requisito e comunicação para evitar leitura errada de que se trata de avaliação coletiva de todos os servidores da equipe.

3. Há distinção entre competências para gestores e para servidores.
   - A reunião indica que o conjunto de competências de gestores pode ser diferente do conjunto de competências de servidores.
   - Isso impacta formulações genéricas que falem apenas em "mapa de competências da unidade" sem distinguir o tipo de avaliado.
   - Há indícios de que cada gestor estratégico define cinco competências e pesos para seu escopo, e que essa definição pode variar por macro-unidade.

4. O cadastro prévio de competências parece mais estruturado do que os CDUs atuais deixam transparecer.
   - Há indícios de definição por macro-unidade e reaproveitamento de cadastro anterior.
   - Isso talvez não deva entrar nos CDUs de execução do diagnóstico em si, mas precisa ser considerado para não descrever o formulário como se ele surgisse de configuração única e homogênea para todos.

5. A avaliação de gestores é um fluxo correlato, mas não idêntico ao diagnóstico técnico dos servidores.
   - O áudio menciona autoavaliação do gestor, avaliação pelo superior imediato e avaliação pela equipe subordinada.
   - Também há discussão de quem sinaliza a conclusão: para gestor, o superior imediato só consegue sinalizar quando a equipe tiver terminado.
   - Isso não deve contaminar indevidamente os CDUs de diagnóstico técnico dos servidores, mas precisa ser documentado se o sistema tratar avaliação de gestores no mesmo módulo.

6. O critério de participação pode depender do maior período no ciclo avaliativo.
   - A reunião menciona casos de mudança de lotação ou função durante o ano e a regra de considerar onde a pessoa passou mais tempo.
   - Esse ponto não aparece claramente nos CDUs atuais e pode exigir requisito próprio de formação do público-alvo/snapshot.

7. Relatórios são relevantes para evolução histórica e efetividade de capacitação.
   - O áudio traz exemplos de comparar gaps entre ciclos e observar se uma capacitação reduziu determinado gap.
   - Isso sugere que os relatórios não são apenas fotografia final do processo; eles também precisam servir à análise longitudinal, ainda que isso possa ficar fora da primeira implementação.

8. Relatório oficial deve ser agregado e sem nomes.
   - O áudio aponta que hoje nomes são retirados da planilha antes de encaminhar resultados para outra área.
   - A necessidade aparece como anonimização ou agregação dentro do sistema.
   - O relatório oficial deve trazer unidade, competência/gap e quantitativos ou médias, mas não lista nominal de pessoas.
   - A consulta detalhada com nomes pode existir dentro do sistema, condicionada às permissões.

## Revisão direcionada por CDU

## Revisão aplicada nos CDUs 50 a 54

Primeira rodada aplicada:

- [cdu-50.md](/Users/leonardo/sgc/etc/reqs/diagnostico/cdu-50.md): removida a dependência de `localização atual`, separada consulta operacional de relatório oficial e explicitada a necessidade de regra de visibilidade por perfil.
- [cdu-51.md](/Users/leonardo/sgc/etc/reqs/diagnostico/cdu-51.md): restringido o fluxo em bloco a aceite/validação, com comunicação consolidada e movimentações individualizadas.
- [cdu-52.md](/Users/leonardo/sgc/etc/reqs/diagnostico/cdu-52.md): reduzidos os detalhes inferidos de e-mail e preservada a distinção entre homologação de unidades e finalização do processo.
- [cdu-53.md](/Users/leonardo/sgc/etc/reqs/diagnostico/cdu-53.md): relatório de gaps reorientado para saída agregada e sem nomes, com fórmula de cálculo ainda pendente de validação.
- [cdu-54.md](/Users/leonardo/sgc/etc/reqs/diagnostico/cdu-54.md): relatório de situação de capacitação reorientado para saída agregada e sem nomes, com comparação histórica tratada como expansão.

### CDU-41 a CDU-49

Estes casos estão mais maduros, mas ainda merecem revisão focada em:

- remoção de resquícios de UX antiga, se ainda houver;
- consistência de nomenclatura entre `autoavaliação`, `consenso`, `situação de capacitação`, `conclusão`, `aceite` e `homologação`;
- explicitação das regras de visibilidade;
- alinhamento fino entre notificações, alertas e o que o fluxo negocial realmente promete.

### CDU-50 - Analisar diagnóstico

Este CDU tem valor, mas ainda está especulativo em vários pontos.

Pontos que parecem bons:

- análise hierárquica por árvore;
- detalhamento por unidade;
- ações de `devolver`, `registrar aceite` e `homologar`;
- histórico de análise;
- devolução com retorno para a unidade anterior;
- validação subindo pela hierarquia até a SEDOC.

Pontos que precisam revisão:

- remover `localização atual` da árvore, salvo se a reunião trouxer justificativa forte;
- esclarecer se a visão de análise é a mesma do CHEFE ou uma visão resumida para superior hierárquico;
- confirmar o nível de detalhe visível para GESTOR e ADMIN;
- confirmar se `Histórico de análise` é requisito negocial real ou projeção natural de implementação;
- revisar textos de notificação e alerta para não cravar termos que a área não tenha validado;
- confirmar o efeito exato da devolução sobre consensos já aprovados.

Indícios fortes do áudio:

- superiores precisam consultar dados detalhados no sistema para analisar o diagnóstico;
- relatório oficial e consulta operacional não devem ser confundidos;
- a discussão de sigilo ainda não pareceu completamente pacificada, então o CDU precisa explicitar a regra escolhida em vez de deixá-la implícita.

### CDU-51 - Validar diagnósticos em bloco

Este CDU está muito dependente de confirmação.

Pontos com forte apoio do PDF:

- o gestor pode validar várias unidades em bloco;
- essa validação em bloco gera uma notificação consolidada para a unidade superior.

Pontos ainda abertos:

- a UI exata do bloco;
- se o alerta interno também é consolidado;
- se há histórico agregado além das movimentações individuais;
- se a operação em bloco aceita somente `aceite` ou também outro tipo de ação.

Direção recomendada:

- manter o bloco restrito à validação/aceite;
- registrar movimentações por subprocesso;
- gerar comunicação consolidada para a unidade superior, como o PDF prevê;
- evitar inventar agregados adicionais de histórico sem confirmação.

### CDU-52 - Finalizar processo de diagnóstico

O PDF dá base para o encerramento, mas não para todos os detalhes hoje escritos.

Pontos sustentados:

- o processo só se conclui quando todas as unidades concluírem o diagnóstico de seus servidores ou informarem impossibilidade;
- a SEDOC tem visão global do andamento;
- relatórios só fazem sentido após a homologação.

Pontos a revisar:

- o requisito atual fala em `todos os subprocessos homologados`; essa leitura parece coerente, mas o PDF fala mais em conclusão do fluxo e homologação pela raiz do que em uma regra formal já redigida assim;
- os modelos detalhados de e-mail para finalização do processo parecem mais inferidos que acordados;
- a liberação de relatórios apenas após `Finalizado` precisa ser confirmada como regra de produto.
- talvez seja necessário separar `homologação de unidade` de `finalização do processo`, porque o PDF fala em cálculo de gaps após a homologação, enquanto o CDU atual amarra relatórios à finalização completa.

### CDU-53 - Gerar relatório de gaps de diagnóstico

Este é um dos CDUs mais frágeis hoje.

O PDF sustenta apenas:

- após homologação, o sistema calcula gaps de competência;
- a informação aparece em relatórios com vários níveis de consolidação.

O áudio acrescenta indícios fortes de que:

- há interesse em histórico entre ciclos;
- há interesse em avaliar efetividade de capacitações a partir da evolução dos gaps;
- o relatório oficial deve ser agregado e sem nomes;
- nomes podem estar disponíveis apenas em consulta interna no sistema para perfis autorizados;
- há necessidade de um relatório em formato institucional, mas os detalhes de layout ainda não estão fechados.

Pontos ainda não sustentados pelo material já revisto:

- entrada por card específico;
- opções exatas de consolidação `Por servidor`, `Por unidade` e `Por competência`;
- fórmula fechada `Importância - Domínio`;
- formatos e cabeçalho exato de exportação.
- relatório nominal por servidor como saída exportável oficial.

Conclusão provisória:

- reescrever este CDU para partir de relatório agregado e anonimizado;
- remover ou rebaixar a opção `Por servidor` como relatório oficial;
- tratar consulta nominal como funcionalidade de análise no sistema, se for mantida;
- manter cálculo de gap como pendência até validação da fórmula.

### CDU-54 - Gerar relatório de situação de capacitação

Também está mais detalhado do que o insumo negocial hoje garante.

O que o fluxo sustenta:

- a situação de capacitação é coletada durante o diagnóstico;
- ela pode compor relatórios posteriores.

O áudio relaciona situação de capacitação com análise de efetividade:

- depois de propor capacitação, interessa observar se o gap diminuiu em ciclo posterior;
- isso aponta para relatório comparativo/histórico, ou ao menos preservação de dados para comparação futura.

O que ainda parece inferido:

- existência de card específico;
- recorte consolidado exclusivamente por unidade;
- totalizadores exatos por competência e por unidade;
- layout de exportação.
- relatório nominal como saída oficial.

Conclusão provisória:

- tratar este CDU como rascunho de intenção;
- revisar para saída agregada e institucional;
- separar consulta operacional detalhada de relatório exportável.

## Lista objetiva do que ainda revisar nos requisitos

1. Revisar `cdu-41.md` a `cdu-49.md` para remover qualquer resíduo de UX antiga e uniformizar o monitoramento inline.
2. Revisar a terminologia de `avaliação da equipe`, se ela aparecer em algum ponto do material.
3. Explicitar as regras de visibilidade por perfil nos requisitos aplicáveis.
4. Revisar se o conjunto de competências precisa diferenciar gestores e servidores em algum requisito de base.
5. Confirmar o efeito exato da devolução para ajustes.
6. Confirmar a semântica de `aceite`, `homologação` e `finalização do processo`.
7. Validar a fórmula de cálculo dos gaps.
8. Decidir se comparação histórica entre ciclos entra nos CDUs atuais ou em requisito futuro.

## Próximos passos

1. Voltar aos [cdu-41.md](/Users/leonardo/sgc/etc/reqs/cdu-41.md) a [cdu-49.md](/Users/leonardo/sgc/etc/reqs/cdu-49.md) para ajustes finos de terminologia, visibilidade e UX.
2. Separar, se necessário, requisitos próprios para:
   - formação do público-alvo/snapshot;
   - definição de competências por macro-unidade;
   - avaliação de gestores;
   - relatórios agregados e anonimizados.

## Estado desta revisão

Documento atualizado com base em:

- fluxo negocial do PDF;
- leitura dos CDUs existentes;
- transcrição automática completa da reunião correta, com análise temática de trechos relevantes.

As anotações já foram transformadas em uma primeira revisão dos CDUs 50 a 54. Ainda falta revisar os CDUs 41 a 49 e decidir se os temas complementares devem virar requisitos próprios.
