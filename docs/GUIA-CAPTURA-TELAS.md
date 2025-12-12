# Guia Completo - Captura de Telas para Refinamento de UI

## 📋 Índice

1. [Visão Geral](#visão-geral)
2. [Como Funciona](#como-funciona)
3. [Guia Rápido](#guia-rápido)
4. [Referência Completa](#referência-completa)
5. [Estrutura de Arquivos](#estrutura-de-arquivos)
6. [Troubleshooting](#troubleshooting)

---

## Visão Geral

Esta suite de testes Playwright foi criada especificamente para capturar screenshots de todas as telas do sistema SGC em suas diversas situações, facilitando o refinamento manual da interface.

### Objetivo

**NÃO é um teste de regressão visual automático.**

É uma **ferramenta de documentação visual** que:
- Captura automaticamente dezenas de telas em diferentes estados
- Organiza as capturas por categoria
- Facilita a análise manual para identificar melhorias de UI
- Documenta visualmente o sistema

### Estatísticas

- **8 categorias** organizadas
- **50+ screenshots** automáticas
- **4 resoluções** testadas (desktop, tablet, mobile)
- **5 perfis** de usuário diferentes
- **600+ linhas** de código de teste

---

## Como Funciona

### Fluxo de Trabalho

```
┌─────────────────────────────────────────────────────────────┐
│ 1. Executar Testes                                          │
│    ./scripts/capturar-telas.sh                              │
│                                                              │
│    ↓ Playwright navega pelo sistema automaticamente         │
│                                                              │
│ 2. Screenshots Geradas                                      │
│    screenshots/                                              │
│    ├── 01-autenticacao--01-login-inicial.png                │
│    ├── 02-painel--01-painel-admin-vazio.png                 │
│    └── ... (50+ arquivos)                                   │
│                                                              │
│    ↓ Análise manual                                         │
│                                                              │
│ 3. Visualizar e Analisar                                    │
│    ./scripts/visualizar-telas.sh                            │
│    (Abre visualizador HTML interativo)                      │
│                                                              │
│    ↓ Identificar melhorias                                  │
│                                                              │
│ 4. Implementar Refinamentos                                 │
│    (Fazer alterações no CSS/UI baseadas na análise)         │
│                                                              │
│    ↓ Recapturar para validar                                │
│                                                              │
│ 5. Repetir o Ciclo                                          │
│    (Executar novamente para validar mudanças)               │
└─────────────────────────────────────────────────────────────┘
```

---

## Guia Rápido

### Instalação (Primeira Vez)

```bash
# Instalar dependências (se ainda não instalou)
npm install

# Instalar navegador Playwright
npx playwright install chromium
```

### Uso Básico

```bash
# 1. Capturar todas as telas
./scripts/capturar-telas.sh

# 2. Visualizar as capturas
./scripts/visualizar-telas.sh
```

### Uso Avançado

```bash
# Capturar apenas uma categoria
./scripts/capturar-telas-categoria.sh painel

# Capturar com navegador visível (debug)
./scripts/capturar-telas.sh --headed

# Capturar categoria específica com UI do Playwright
./scripts/capturar-telas-categoria.sh mapa --ui
```

---

## Referência Completa

### Scripts Disponíveis

#### 1. `capturar-telas.sh`
**Propósito:** Captura todas as screenshots do sistema

**Uso:**
```bash
./scripts/capturar-telas.sh [opcoes]
```

**Opções:**
- `--headed` - Executa com navegador visível
- `--debug` - Pausa em cada ação
- `--ui` - Abre interface do Playwright

**Exemplo:**
```bash
./scripts/capturar-telas.sh --headed
```

#### 2. `capturar-telas-categoria.sh`
**Propósito:** Captura screenshots de uma categoria específica

**Uso:**
```bash
./scripts/capturar-telas-categoria.sh <categoria> [opcoes]
```

**Categorias:**
- `autenticacao` - Login e autenticação
- `painel` - Painel principal
- `processo` - Processos
- `subprocesso` - Subprocessos e atividades
- `mapa` - Mapas de competências
- `navegacao` - Navegação e menus
- `estados` - Estados do sistema
- `responsividade` - Múltiplas resoluções
- `all` - Todas (padrão)

**Exemplo:**
```bash
./scripts/capturar-telas-categoria.sh painel --headed
```

#### 3. `visualizar-telas.sh`
**Propósito:** Inicia servidor HTTP e abre visualizador

**Uso:**
```bash
./scripts/visualizar-telas.sh
```

**Funcionalidades do Visualizador:**
- Galeria organizada de screenshots
- Filtros por categoria
- Busca em tempo real
- Estatísticas de capturas
- Zoom em modal para visualização ampliada
- Interface responsiva

### NPM Scripts

```bash
# Captura completa
npm run test:e2e:captura

# Captura com navegador visível
npm run test:e2e:captura:headed

# Interface UI do Playwright
npm run test:e2e:captura:ui
```

### Playwright Direto

```bash
# Todas as capturas
npx playwright test e2e/captura-telas.spec.ts

# Apenas uma categoria
npx playwright test e2e/captura-telas.spec.ts --grep "Painel Principal"

# Com opções
npx playwright test e2e/captura-telas.spec.ts --headed --debug
```

---

## Estrutura de Arquivos

### Arquivos de Teste

```
e2e/
└── captura-telas.spec.ts          # Suite principal (600+ linhas)
    ├── 01 - Autenticação          # Testes de login
    ├── 02 - Painel Principal      # Painel em vários perfis
    ├── 03 - Fluxo de Processo     # Criação/edição de processos
    ├── 04 - Subprocesso           # Atividades e conhecimentos
    ├── 05 - Mapa de Competências  # Criação de mapas
    ├── 06 - Navegação e Menus     # Elementos de navegação
    ├── 07 - Estados e Situações   # Diferentes estados
    └── 08 - Responsividade        # Múltiplas resoluções
```

### Screenshots Geradas

```
screenshots/
├── README.md                              # Documentação
├── visualizador.html                      # Visualizador web
├── .gitkeep                               # Manter no git
│
├── 01-autenticacao--01-login-inicial.png
├── 01-autenticacao--02-login-erro.png
├── 01-autenticacao--03-selecao-perfil.png
│
├── 02-painel--01-admin-vazio.png
├── 02-painel--02-criar-processo.png
├── 02-painel--10-gestor.png
│
├── 03-processo--01-edicao.png
├── 03-processo--02-modal-iniciar.png
│
├── 04-subprocesso--01-dashboard.png
├── 04-subprocesso--02-atividades-vazio.png
│
├── 05-mapa--01-vazio.png
├── 05-mapa--02-modal-criar.png
│
├── 06-navegacao--01-menu-principal.png
│
├── 07-estados--01-processo-criado.png
│
└── 08-responsividade--01-desktop-1920.png
```

### Scripts

```
scripts/
├── capturar-telas.sh              # Captura completa
├── capturar-telas-categoria.sh    # Captura por categoria
└── visualizar-telas.sh            # Visualizador
```

---

## Troubleshooting

### Problema: Screenshots vazias ou incompletas

**Causa:** Elementos não carregaram antes da captura

**Solução:** Aumentar tempos de espera em `captura-telas.spec.ts`
```typescript
await page.waitForTimeout(1000); // Aumentar de 500 para 1000
```

### Problema: Testes falhando

**Diagnóstico:**
```bash
# Executar com navegador visível
./scripts/capturar-telas.sh --headed

# Modo debug (pausa a cada passo)
./scripts/capturar-telas.sh --debug
```

**Verificações:**
1. Backend está rodando?
2. Frontend está rodando?
3. Banco de dados está acessível?

### Problema: "Navegador não encontrado"

**Solução:**
```bash
npx playwright install chromium
```

### Problema: Visualizador não abre

**Causa:** Servidor HTTP não disponível

**Solução:**
```bash
# Instalar Python ou Node.js

# Ou abrir manualmente:
cd screenshots
python3 -m http.server 8000
# Abrir http://localhost:8000/visualizador.html
```

### Problema: Screenshots não aparecem no visualizador

**Causa:** Servidor não está servindo os arquivos PNG

**Solução:**
Certifique-se de que está executando o servidor no diretório correto:
```bash
cd screenshots
python3 -m http.server 8000
```

---

## Dicas e Boas Práticas

### 1. Execute Regularmente
Capture screenshots regularmente durante o desenvolvimento para documentar a evolução da UI.

### 2. Compare Antes e Depois
Mantenha capturas antigas em um diretório separado para comparação manual.

### 3. Use Categorias para Focar
Use o script de categoria quando estiver trabalhando em uma área específica:
```bash
./scripts/capturar-telas-categoria.sh mapa --headed
```

### 4. Compartilhe com a Equipe
As screenshots são ótimas para discussões de design em revisões de código.

### 5. Documente Decisões
Use as screenshots como referência ao documentar decisões de UI/UX.

### 6. Adicione Novas Capturas
Edite `captura-telas.spec.ts` para adicionar novas telas conforme o sistema evolui:

```typescript
test('Nova tela a capturar', async ({page}) => {
    await page.goto('/nova-tela');
    await capturarTela(page, 'categoria', 'nome', {fullPage: true});
});
```

---

## Suporte

Para problemas ou sugestões:
1. Verifique a seção [Troubleshooting](#troubleshooting)
2. Consulte a documentação do Playwright: https://playwright.dev
3. Verifique issues no repositório

---

## Licença

Este projeto faz parte do SGC (Sistema de Gestão de Competências).
