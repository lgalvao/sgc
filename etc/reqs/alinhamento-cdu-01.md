# Alinhamento CDU-01 - Realizar login e exibir estrutura das telas

## Cobertura atual do teste

O teste `cdu-01.spec.ts` cobre os seguintes cenários:

- ✅ Autenticação com credenciais inválidas e exibição da mensagem de erro
- ✅ Login bem-sucedido com perfil único (GESTOR_COORD_11)
- ✅ Seleção de perfil quando há múltiplos perfis/unidades (ADMIN com 2 perfis)
- ✅ Exibição de navegação para ADMIN: links SGC, Painel, Unidades, Relatórios, Histórico, Parâmetros, Administradores
- ✅ Exibição de navegação com restrições para GESTOR: SGC, Painel, Minha unidade (sem Unidades), sem botões administrativos
- ✅ Exibição de navegação com restrições para CHEFE: idem GESTOR
- ✅ Exibição de navegação com restrições para SERVIDOR: idem GESTOR
- ✅ Exibição de informações do usuário (Perfil - Sigla da unidade) e ícone de logout
- ✅ Exibição de rodapé com texto "© SESEL/COSIS/TRE-PE"

## Lacunas em relação ao requisito

1. **Autenticação via API AD não verificada**: O requisito especifica (passo 4) que o sistema deve verificar credenciais através da API do "Sistema Acesso AD" do TRE-PE. O teste apenas valida que a mensagem de erro é exibida, sem validar o mecanismo de integração com AD.

2. **Consultoria de perfis e unidades não explícita**: O requisito menciona (passo 6) consulta às views do banco de dados para recuperar perfis e unidades. O teste não valida este comportamento internamente.

3. **Regras de determinação de perfis não todas cobertas**: O requisito (passo 7) descreve 4 regras específicas para determinar perfis (ADMIN, GESTOR, CHEFE, SERVIDOR). O teste não cobre explicitamente:
   
   - Um usuário com múltiplas unidades sendo testado (apenas testa múltiplos perfis)
   - A regra de "não responsável pela unidade" (SERVIDOR) não há um teste específico validando que apenas servidores sem responsabilidade recebem este perfil

4. **Pré-condição não validada**: O requisito exige (pré-condições) que "Usuário deve estar cadastrado no SGRH com lotação ativa em alguma unidade". O teste não valida este pré-requisito.

5. **Comportamento de múltiplas unidades SEM múltiplos perfis**: O requisito (passo 8.2) menciona expansão da tela para seleção de perfil/unidade se houver "múltiplos perfis OU unidades". O teste cobre múltiplos perfis, mas não testa o cenário de um usuário com um perfil único mas múltiplas unidades (por substituição/atribuição temporária).

6. **Links SGC não validam navegação**: O teste verifica a visibilidade do link com nome "SGC", mas não valida que clicar nele abre a tela "Painel" (requisito passo 9.1.1).

7. **Link Relatórios não validado**: O requisito menciona (passo 9.1.1) um link "Relatórios", mas o teste apenas valida sua presença, não seu comportamento.

8. **Ícone de logout não completamente testado**: O teste valida que `btn-logout` está visível, mas não valida que clicar nele faz logout e mostra a tela Login novamente.

9. **Rodapé incompleto**: O requisito (passo 9.3) menciona "Desenvolvido por SESEL/COSIS/TRE-PE", o teste verifica "© SESEL/COSIS/TRE-PE", faltando "Desenvolvido por" e também não valida a informação de versão do sistema.

## Alterações necessárias no teste E2E

- Adicionar teste que valida login com um usuário que possui múltiplas unidades mas apenas um perfil
- Adicionar teste que valida o comportamento do ícone logout (clique e redirecionamento para Login)
- Adicionar teste que valida navegação ao clicar no ícone/logo SGC (deve abrir Painel)
- Adicionar teste que valida navegação do link "Relatórios"
- Adicionar teste específico validando a regra de perfil SERVIDOR (usuário sem responsabilidade por unidade)
- Corrigir validação do rodapé para incluir "Desenvolvido por" e versão do sistema (ou adaptar o teste conforme a implementação real)
- Validar que o campo "Configurações" ou "Parâmetros" está consistente entre requisito e implementação
- Adicionar validação de que a tela `Painel` é exibida inicialmente (passo 10 do requisito)

## Notas e inconsistências do requisito

- Há possível inconsistência de nomenclatura entre "Configurações" (requisito) e "Parâmetros" (teste)
- O requisito é claro quanto às regras de determinação de perfis (passo 7), mas não descreve como o sistema diferencia um SERVIDOR de outros perfis em múltiplas situações de hierarquia
- O requisito menciona "Reabertura do cadastro de atividades" (referência futura a CDU-06), sugerindo interdependência com outros CDUs
- A integração com "Sistema Acesso do TRE-PE" não é testada no E2E (esperado, pois é externo), mas não há clareza se há testes de contrato ou mocks
