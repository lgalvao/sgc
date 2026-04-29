# Plano de Melhoria: Toolkit de Automação (etc/scripts)

Este documento detalha o plano para modernização, consolidação e padronização do toolkit de scripts do SGC, visando reduzir a dívida técnica e aumentar a confiabilidade das automações de projeto.

## 1. Achados Concretos (Diagnóstico)

### 1.1 Fragmentação Técnica
- **Dualidade de Módulos:** Mistura de ESM (`.js`) na raiz/lib e CommonJS (`.cjs`) nos scripts de domínio (backend/frontend). Isso impede o reúso de utilitários centrais sem hacks.
- **Caminhos Frágeis:** Uso extensivo de `path.resolve(__dirname, '../../..')` duplicado em dezenas de arquivos, tornando o toolkit sensível a mudanças de estrutura.

### 1.2 Duplicação de Lógica
- **Parsing de Cobertura:** A lógica para ler relatórios JaCoCo (XML) e V8 (JSON) está espalhada em 3 lugares diferentes (Auditorias de Cobertura, Dashboard de QA e Coletor de Snapshots).
- **Execução de Processos:** Scripts diferentes reinventam o tratamento de execução no Windows (spawn vs exec vs execa) e o tratamento de `stdout/stderr`.

### 1.3 Inconsistência de Interface (UX/DX)
- **Argumentos Ad-hoc:** Loops manuais de `parseArgs` variam em suporte a `--json`, `--help` e tratamento de erros.
- **Saída Visual:** Mistura de logs puros (`console.log`) com utilitários visuais (`picocolors`, `Listr2`), resultando em uma experiência de CLI inconsistente.

---

## 2. Arquitetura Alvo

1. **ESM Everywhere:** Conversão total para ECMAScript Modules (`.js`).
2. **Domínios Ricos em `lib/`:** Extração da lógica de negócio (parsing de XML/JSON, cálculos de métricas) para módulos reutilizáveis em `etc/scripts/lib/dominios/`.
3. **Interface Padronizada:** Todos os scripts devem herdar comportamentos de uma base comum para logs, erros e saída JSON.

---

## 3. Roadmap de Execução

### Fase 1: Fundação e Unificação (Curto Prazo)
- [ ] **Unificar Gestão de Caminhos:** Migrar todos os scripts para usar `DIRETORIO_RAIZ` vindo exclusivamente de `lib/caminhos.js`.
- [ ] **Centralizar Execução:** Substituir todos os `spawn/exec` manuais pela função `executarComando` (ou similar) baseada em `execa`, garantindo suporte nativo a Windows.
- [ ] **Criação de `lib/dominios/`:**
    - `cobertura-java.js`: Parser único para JaCoCo.
    - `cobertura-web.js`: Parser único para Istanbul/V8.

### Fase 2: Modernização de Módulos (Médio Prazo)
- [ ] **Migração CJS -> ESM:** Renomear `.cjs` para `.js` e ajustar `require` para `import`.
- [ ] **Padronização de Argumentos:** Adotar um padrão único de parsing (ou delegar totalmente ao `commander.js` no `sgc.js`) para garantir que todos os comandos suportem `--json` de forma idêntica.

### Fase 3: Consolidação de Snapshots (Longo Prazo)
- [ ] **Snapshot Unificado:** Fazer com que `codigo smells` e `testes analisar` contribuam diretamente para o modelo de dados do `qa snapshot`, evitando a fragmentação de relatórios em `etc/qualidade`.
- [ ] **Schema de Saída:** Definir um JSON Schema para respostas do toolkit, facilitando a integração com CI/CD.

---

## 4. Critérios de Sucesso

1. **Zero duplicidade** de lógica de parsing de cobertura Jacoco/V8.
2. **Todos os scripts** funcionando nativamente em Windows/Linux sem IFs locais.
3. **Consistência visual:** Todos os comandos seguindo a paleta de cores e formato de cabeçalho do `lib/saida.js`.
4. **Reutilização:** Scripts de domínio importando utilitários de `lib/` sem erros de módulo.
