# Plano de Adoção de Testes Baseados em Propriedade (PBT)

## 1. Visão Geral e Objetivos

Este documento descreve a estratégia para introduzir **Testes Baseados em Propriedade (Property-Based Testing - PBT)** no projeto SGC. O objetivo é aumentar a robustez dos testes unitários, saindo da verificação baseada em exemplos fixos para a verificação de **invariantes** e **comportamentos** que devem ser verdadeiros para qualquer entrada válida gerada.

### Benefícios Esperados
- **Cobertura de Casos de Borda:** Detecção de erros em cenários não previstos (ex: strings vazias, números negativos, datas bissextas, caracteres unicode) que testes baseados em exemplos costumam perder.
- **Documentação Executável:** As propriedades descrevem *o que* o código deve fazer em um nível mais abstrato (regras de negócio).
- **Redução de Código de Teste:** Um único teste de propriedade pode substituir dezenas de testes de exemplo parametrizados.

---

## 2. Ferramentas e Dependências

As ferramentas já foram identificadas e, no caso do backend, já estão incluídas no projeto.

### 2.1 Backend (Java)
- **Biblioteca:** [JQwik](https://jqwik.net/)
- **Status:** Já presente no `build.gradle.kts` (`net.jqwik:jqwik:1.9.2`).
- **Integração:** Funciona nativamente com JUnit 5.

### 2.2 Frontend (TypeScript/Vue)
- **Biblioteca:** [fast-check](https://fast-check.dev/)
- **Ação Necessária:** Adicionar ao projeto via npm.
- **Comando:** `npm install --save-dev fast-check`

---

## 3. Estratégia de Implementação (Backend)

A implementação será gradual, focando primeiro em utilitários puros e lógica de domínio central.

### 3.1 Candidatos Iniciais

| Categoria | Classe/Componente | Propriedade a Testar (Exemplo) | Prioridade |
|-----------|-------------------|--------------------------------|------------|
| **Utilitários** | `sgc.comum.util.FormatadorData` | Parsing e formatação são inversos (round-trip); datas inválidas lançam exceção correta. | Alta |
| **Domínio** | `sgc.mapa.model.Mapa` | Invariantes de estado (ex: não pode ter lista de competencias nula). | Alta |
| **Negócio** | `sgc.processo.service.ProcessoService` (Regras de Estado) | Transições de `SituacaoProcesso` inválidas devem sempre falhar, independente dos dados do processo. | Média |
| **Segurança** | `sgc.seguranca.Sanitizacao` (HtmlPolicy) | Idempotência: `sanitizar(sanitizar(x)) == sanitizar(x)`; Tags proibidas nunca estão presentes na saída. | Alta |

### 3.2 Exemplo de Implementação (Java/JQwik)

Arquivo: `backend/src/test/java/sgc/comum/util/FormatadorDataPropertyTest.java`

```java
import net.jqwik.api.*;
import java.time.LocalDate;

class FormatadorDataPropertyTest {

    @Property
    void formatacaoDeDataNuncaRetornaNulo(@ForAll LocalDate data) {
        String resultado = FormatadorData.formatar(data);
        assert resultado != null;
        assert !resultado.isEmpty();
    }

    @Property
    void roundTripParsing(@ForAll LocalDate data) {
        String formatado = FormatadorData.formatar(data);
        LocalDate recuperado = FormatadorData.parse(formatado);
        assert data.equals(recuperado);
    }
}
```

---

## 4. Estratégia de Implementação (Frontend)

No frontend, o foco será em validadores de formulário e helpers de exibição.

### 4.1 Candidatos Iniciais

| Categoria | Componente/Função | Propriedade a Testar | Prioridade |
|-----------|-------------------|----------------------|------------|
| **Helpers** | Formatação de CPF/CNPJ | `format(strip(x)) == format(x)`; Strings aleatórias não quebram o formatter. | Alta |
| **Validação** | Regras de validação customizadas | Entradas inválidas (ex: emails malformados gerados) sempre retornam `false`. | Alta |
| **Componentes** | Componentes de Tabela/Lista | Renderização com arrays de tamanhos variados (0 a 1000 itens) não quebra a UI. | Média |

### 4.2 Exemplo de Implementação (Vitest + fast-check)

Arquivo: `frontend/src/utils/formatadores.spec.ts`

```typescript
import { test } from 'vitest';
import fc from 'fast-check';
import { formatarCpf } from './formatadores';

test('formatarCpf deve manter apenas números na versão limpa', () => {
  fc.assert(
    fc.property(fc.string(), (texto) => {
      // Propriedade: O formatador não deve "crashar" com input arbitrário
      try {
        const resultado = formatarCpf(texto);
        return typeof resultado === 'string';
      } catch (e) {
        return false;
      }
    })
  );
});
```

---

## 5. Plano de Execução

### Fase 1: Configuração e POC (Dia 1)
1.  **Frontend:** Instalar `fast-check`.
2.  **Backend:** Criar um teste de propriedade simples para `sgc.comum.util.FormatadorData` (ou similar) para validar o setup do JQwik.
3.  **Frontend:** Criar um teste de propriedade para um utilitário de formatação.

### Fase 2: Domínio Crítico (Dia 2-3)
1.  Identificar lógica de validação complexa nos Services de `Processo` e `Mapa`.
2.  Implementar testes gerativos para garantir que regras de negócio (ex: "Processo em revisão deve ter mapa copiado") sejam respeitadas para quaisquer combinações de dados válidos.

### Fase 3: Integração no CI/CD
1.  Garantir que os testes de propriedade rodem com os comandos padrão (`./gradlew test`, `npm run test:unit`).
2.  Configurar o "Seed" do gerador aleatório nos logs de falha para permitir reprodução determinística de erros encontrados.

## 6. Boas Práticas Adotadas

1.  **Separação:** Testes de propriedade podem ficar na mesma pasta dos testes unitários, mas recomenda-se o sufixo `PropertyTest` (Java) ou `.prop.spec.ts` (JS) se forem muito extensos.
2.  **Determinismo:** Sempre logar a semente (seed) usada na geração para reproduzir falhas.
3.  **Performance:** Testes de propriedade podem ser lentos se gerarem milhares de casos. Manter o número de execuções (samples) razoável para testes de CI (ex: 100 execuções).

## 7. Próximos Passos
- Aprovar este plano.
- Executar a instalação do `fast-check` no frontend.
- Criar o primeiro teste de propriedade no backend (`FormatadorData` ou equivalente).
