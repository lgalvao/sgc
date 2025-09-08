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