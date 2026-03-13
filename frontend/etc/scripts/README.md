# Scripts de Qualidade e Cobertura do Frontend

Este diretório contém scripts utilitários para análise de cobertura, validações de consistência e auditoria de qualidade
do frontend.

## 🚀 Como executar

A maioria dos scripts requer que o relatório de cobertura (`coverage-final.json`) tenha sido gerado previamente.

1. **Gerar cobertura:**
   ```bash
   cd frontend
   npm run coverage:unit
   ```

2. **Executar um script:**
   ```bash
   cd frontend
   node etc/scripts/<nome-do-script>.cjs
   ```

## 📋 scripts Disponíveis

### 1. Análise de Cobertura

* **`verificar-cobertura.cjs`**: Lista todos os arquivos com cobertura de statements abaixo de 80%. É útil para
  identificar componentes que precisam de atenção imediata.
* **`analisar-impacto-cobertura.cjs`**: O script mais estratégico. Ele calcula o "Impacto potencial", mostrando quanto a
  cobertura total do projeto aumentaria (em pontos percentuais) se um arquivo específico atingisse 100%. Use-o para
  priorizar onde escrever testes.

### 2. Auditoria e Validação

* **`audit-frontend-validations.cjs`**: Compara as validações do Frontend (Vue/Zod) com as do Backend (Java/Bean
  Validation). Gera um relatório `frontend-backend-validation-comparison.md` na raiz do projeto para identificar
  discrepâncias.
* **`audit-view-validations.cjs`**: Foca especificamente em links e permissões dentro das Views.
* **`listar-test-ids.cjs`** e **`listar-test-ids-duplicados.cjs`**: Gerencia os atributos `data-test` usados nos testes
  E2E e unitários, garantindo que sejam únicos e fáceis de localizar.
* **`verificar-acessibilidade.js`**: Utiliza regras de acessibilidade para auditar os componentes Vue.

### 3. Utilidade

* **`capturar-telas.cjs`**: Auxilia na captura de screenshots automatizada para documentação ou testes visuais.

---

## 💡 Dica profissional

Antes de começar a aumentar a cobertura, execute:

```bash
node etc/scripts/analisar-impacto-cobertura.cjs
```

Foque nos 3 primeiros arquivos da lista de "Impacto potencial" para obter o maior ganho de cobertura com o menor
esforço.
