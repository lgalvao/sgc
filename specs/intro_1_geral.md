# Informações gerais

O sistema de Gestão de Competências (SGC) tem como objetivo gerenciar as competências técnicas das unidades
organizacionais do Tribunal. O sistema opera com uma estrutura organizacional hierárquica em que a árvore de unidades
fica subordinada a uma unidade virtual raiz (sigla ADMIN) a partir da qual os processos de mapeamento, revisão e
diagnóstico de competências são iniciados e concluídos, após passarem pela atuação das demais unidades da hierarquia.

Para fins de classificação estrutural (detalhada no [Glossário](intro_2_glossario.md)), as unidades são divididas em:

- **Unidade operacional**: Unidade de ponta, com mais de um servidor e sem unidades subordinadas.

- **Unidade intermediária**: Unidade de chefia/gestão, com unidades subordinadas mas apenas um servidor (o titular)
  lotado.

- **Unidade interoperacional**: Unidade híbrida (atua tanto como chefia quanto "ponta"), que possui unidades
  subordinadas e também mais de um servidor lotado.

Os elementos essenciais do sistema incluem:

- **atividades**: ações específicas desempenhadas por cada unidade no exercício de suas funções;

- **conhecimentos**: conhecimentos técnicos necessários para executar cada atividade (cada atividade terá um ou mais
  conhecimentos)

- **competências**: elementos sintetizantes agrupando atividades relacionadas, criadas e mantidas apenas pelo perfil
  ADMIN;

- **mapas de competências**: conjunto de competências associadas a uma unidade, resultante do processo de mapeamento,
  criadas e mantidas apenas pelo perfil ADMIN

O sistema suporta três tipos de processos:

- **mapeamento**: representa o processo inicial de coleta sistemática das atividades e conhecimentos necessários para
  cada unidade operacional visando a construção do primeiro mapa de competências da unidade;

- **revisão**: consiste na atualização periódica dos mapas de competências vigentes em cada unidade considerando as
  mudanças organizacionais e de atribuições;

- **diagnóstico**: avalia a importância e domínio das competências pelos servidores das unidades, identificando lacunas
  (gaps) e necessidades de capacitação, a partir do mapa de competências vigente de cada unidade.

## Atores e perfis

O sistema opera com os seguintes perfis de usuários, cujas atribuições e acessos são automaticamente reconhecidos com
base na condição de responsabilidade ou lotação em uma unidade, de acordo com o SGRH, ou por atribuição de
responsabilidade temporária, ou cadastro como administrador no SGC. Caso um usuário acumule mais de um perfil ou seja
responsável por mais de uma unidade, será necessário selecionar o perfil e a unidade de trabalho após o primeiro passo
do login.

- **ADMIN**: Administrador do sistema. Esse perfil, que será geralmente exercido por servidores da unidade SEDOC, é
  responsável por criar, configurar e monitorar processos (dos três tipos). Usuários que logarem com este perfil estarão
  associados à unidade raiz 'ADMIN'. O perfil ADMIN é responsável exclusivo por criar e ajustar os mapas de
  competências.

- **GESTOR**: Responsável por uma unidade intermediária ou interoperacional (geralmente será um Coordenador ou
  Secretário). Pode visualizar e validar as informações cadastradas pelas unidades sob sua gestão, submetendo para
  análise da unidade superior, ou devolvendo à unidade subordinada para realização de retificações. Usários com perfil
  GESTOR não podem criar nada no sistema -- apenas validam/devolvem/analisam subprocessos.

- **CHEFE**: Responsável por uma unidade operacional ou interoperacional. Responsável exclusivo por cadastrar atividades
  e conhecimentos de sua unidade.

- **SERVIDOR**: Servidor lotado em uma unidade operacional ou interoperacional. Este perfil só atua nos processos de
  diagnóstico.