## Testes E2E (Playwright)

- **Localização**: Todos os testes E2E estão no diretório `spec/`.
- **Servidor**: O Servidor já está rodando na porta padrão. Não tente executar novamente.
- **Timeouts**: Os testes E2E devem ter timeouts curtos, de no máximo 5000ms, pois não há backend e está tudo mockado.
  Se um teste não passar, o problema geralmente estará em outro lugar, não no timeout. Não altere timeouts.
- **Estrutura**: Cada arquivo `.spec.ts` testa uma funcionalidade específica (ex: `login.spec.ts` para o fluxo de login,
  `cad-atividades.spec.ts` para o cadastro de atividades).
- **Autenticação**: O login é realizado uma vez antes de cada teste ou suíte, utilizando a função auxiliar `login` de
  `utils/auth.ts`, garantindo que os testes operem em um estado autenticado.
- **Boas práticas**:
    - Use sempre que possível `data-testid` estáveis para formulários e botões. Crie esses ids no código sob testes, se
      necessário.
    - Evite seletores baseados em classes CSS ou IDs gerados dinamicamente, pois são menos estáveis.
- **Login e Perfis**: Use sempre IDs de servidores válidos que existem nos dados mockados. Verifique os perfis disponíveis
  para cada servidor antes de criar testes.
- **Dados Mockados**: Trabalhe com os dados existentes nos arquivos JSON ao invés de tentar interceptar ou mockar dados.
  Verifique sempre o conteúdo dos arquivos de mock antes de escrever asserções.
- **Testes Dinâmicos**: Para elementos que podem ou não existir (como listas vazias), use verificações condicionais
  ao invés de asserções fixas. Exemplo: verificar se há linhas na tabela antes de testar ordenação.
- **Funções Auxiliares**: Crie funções auxiliares para ações repetitivas (ex: `createCompetencia`, `navigateToMapa`):
    * Reduza duplicação de código entre testes
    * Facilite manutenção quando seletores mudarem
    * Torne os testes mais legíveis e focados no que está sendo testado
- **Cobertura de CDU**: Para testes de casos de uso (CDU):
    * Leia a especificação completa antes de escrever testes
    * Crie um teste para cada item principal do fluxo
    * Valide pré-condições, ações e resultados esperados
    * Use comentários nos testes referenciando os itens do CDU (ex: `// item 9.1`)
- **Modais e Confirmações**: Para testes que envolvem modais:
    * Sempre aguarde a abertura do modal antes de interagir
    * Use seletores específicos para botões dentro do modal
    * Verifique o fechamento do modal após ações
    * Teste tanto confirmação quanto cancelamento quando aplicável
- **Limitação de Interações**: Para evitar timeouts em loops:
    * Limite o número de elementos selecionados (ex: `Math.min(count, 3)`)
    * Use `.first()` quando apenas um elemento for suficiente para o teste
    * Evite loops longos que podem causar instabilidade