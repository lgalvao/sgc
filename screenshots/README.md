# Captura de Telas - SGC

Este diretório contém as capturas de tela automatizadas do sistema SGC para análise e refinamento de UI.

## Objetivo

O objetivo desta suite de testes **NÃO é** fazer testes de regressão visual, mas sim:

- **Capturar screenshots de todas as telas do sistema** em suas diversas situações
- **Facilitar a análise manual** da interface durante o refinamento de UI
- **Documentar visualmente** os diferentes estados e fluxos do sistema

## Como Executar

### Opção 1: Script Shell - Todas as Capturas (Recomendado)

```bash
# Execução básica (headless)
./scripts/capturar-telas.sh

# Com navegador visível (útil para debug)
./scripts/capturar-telas.sh --headed

# Modo debug (pausa em cada ação)
./scripts/capturar-telas.sh --debug

# Interface UI do Playwright
./scripts/capturar-telas.sh --ui
```

### Opção 2: Script Shell - Por Categoria

Útil quando você quer capturar apenas uma categoria específica:

```bash
# Capturar apenas telas de autenticação
./scripts/capturar-telas-categoria.sh autenticacao

# Capturar apenas o painel principal
./scripts/capturar-telas-categoria.sh painel --headed

# Capturar apenas mapas de competências
./scripts/capturar-telas-categoria.sh mapa

# Ver todas as categorias disponíveis
./scripts/capturar-telas-categoria.sh
```

**Categorias disponíveis:**
- `autenticacao` - Telas de login e autenticação
- `painel` - Painel principal
- `processo` - Criação e gerenciamento de processos
- `subprocesso` - Dashboard e atividades
- `mapa` - Mapa de competências
- `navegacao` - Elementos de navegação
- `estados` - Diferentes estados de processo
- `responsividade` - Screenshots em múltiplas resoluções
- `all` - Todas as categorias

### Opção 3: NPM Scripts

```bash
# Execução básica (headless)
npm run test:e2e:captura

# Com navegador visível
npm run test:e2e:captura:headed

# Interface UI do Playwright
npm run test:e2e:captura:ui
```

### Opção 4: Playwright Diretamente

```bash
# Executar apenas os testes de captura
npx playwright test e2e/captura-telas.spec.ts

# Com opções
npx playwright test e2e/captura-telas.spec.ts --headed
npx playwright test e2e/captura-telas.spec.ts --ui
npx playwright test e2e/captura-telas.spec.ts --debug

# Executar apenas uma categoria específica
npx playwright test e2e/captura-telas.spec.ts --grep "01 - Autenticação"
npx playwright test e2e/captura-telas.spec.ts --grep "02 - Painel Principal"
npx playwright test e2e/captura-telas.spec.ts --grep "05 - Mapa de Competências"
```

## Estrutura das Screenshots

As screenshots são organizadas por categoria usando o padrão:

```
screenshots/
├── 01-autenticacao--01-login-inicial.png
├── 01-autenticacao--02-login-erro-credenciais.png
├── 01-autenticacao--03-login-selecao-perfil.png
├── 02-painel--01-painel-admin-vazio.png
├── 02-painel--02-criar-processo-form-vazio.png
├── 03-processo--01-processo-edicao.png
├── 04-subprocesso--01-dashboard-subprocesso.png
├── 05-mapa--01-mapa-vazio.png
├── 06-navegacao--01-menu-principal.png
├── 07-estados--01-processo-criado.png
└── 08-responsividade--01-desktop-1920x1080.png
```

### Categorias de Screenshots

1. **01-autenticacao**: Telas de login, seleção de perfil e erros de autenticação
2. **02-painel**: Painel principal em diferentes perfis (ADMIN, GESTOR, CHEFE)
3. **03-processo**: Criação, edição e gerenciamento de processos
4. **04-subprocesso**: Dashboard de subprocesso, cadastro de atividades e conhecimentos
5. **05-mapa**: Criação e gerenciamento de mapas de competências
6. **06-navegacao**: Elementos de navegação, menus e rodapé
7. **07-estados**: Diferentes estados e situações de processos e subprocessos
8. **08-responsividade**: Screenshots em diferentes resoluções de tela

## O Que É Capturado

### Fluxos Completos

- ✅ Login e autenticação (com e sem múltiplos perfis)
- ✅ Painel principal (ADMIN, GESTOR, CHEFE)
- ✅ Criação e edição de processos
- ✅ Gerenciamento de subprocessos
- ✅ Cadastro de atividades e conhecimentos
- ✅ Criação de mapas de competências
- ✅ Navegação entre seções

### Estados e Situações

- ✅ Formulários vazios
- ✅ Formulários preenchidos
- ✅ Mensagens de validação e erro
- ✅ Modais de confirmação
- ✅ Estados hover em elementos interativos
- ✅ Processos em diferentes situações (CRIADO, EM_ANDAMENTO)
- ✅ Subprocessos em diferentes situações (NAO_INICIADO, DISPONIBILIZADO)

### Responsividade

- ✅ Desktop (1920x1080, 1366x768)
- ✅ Tablet (768x1024)
- ✅ Mobile (375x667)

## Análise Manual

Após a execução:

1. Abra o diretório `screenshots/`
2. Navegue pelas categorias organizadas
3. Compare visualmente as diferentes telas e estados
4. Identifique inconsistências, problemas de layout ou melhorias necessárias
5. Anote os itens para refinamento

## Manutenção

### Adicionar Novas Capturas

Para adicionar novas capturas de tela, edite o arquivo `e2e/captura-telas.spec.ts`:

```typescript
test('Minha nova captura', async ({page}) => {
    // Setup e navegação
    await page.goto('/alguma-tela');
    
    // Capturar screenshot
    await capturarTela(page, 'categoria', 'nome-descritivo', {fullPage: true});
});
```

### Atualizar Capturas Existentes

Basta executar novamente os testes. As screenshots antigas serão substituídas automaticamente.

## Notas Importantes

- **Não versionar screenshots**: As screenshots não devem ser commitadas no git (já estão no `.gitignore`)
- **Limpeza automática**: O script limpa screenshots antigas antes de executar
- **Tempo de execução**: A suite completa leva alguns minutos para executar
- **Estabilidade**: Os testes incluem `waitForTimeout` para garantir que elementos estejam completamente renderizados

## Troubleshooting

### Screenshots vazias ou incompletas

Aumente os tempos de espera em `captura-telas.spec.ts`:

```typescript
await page.waitForTimeout(500); // Aumentar para 1000 ou mais
```

### Testes falhando

1. Verifique se o backend e frontend estão rodando corretamente
2. Execute com `--headed` para ver o que está acontecendo
3. Use `--debug` para pausar em cada passo

### Navegador não inicia

```bash
# Instalar navegadores do Playwright
npx playwright install chromium
```

## Relacionado

- [Testes E2E Principais](../e2e/README.md)
- [Documentação Playwright](https://playwright.dev)
