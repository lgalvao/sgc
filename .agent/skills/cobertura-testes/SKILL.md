---
name: cobertura-testes
description: Executa e analisa a cobertura de testes do backend utilizando os scripts especializados do SGC. Permite gerar relatórios rápidos, identificar lacunas profundas e criar planos de ação para atingir metas de cobertura.
metadata:
  version: "1.0"
  author: "SGC-Team"
---

# Skill: Cobertura de Testes (Backend)

Esta skill orquestra as ferramentas de análise de cobertura JaCoCo no ambiente do SGC.

## Scripts Integrados

| Script | Função |
|--------|---------|
| `verificar-cobertura.cjs` | Relatório resumido e rápido da cobertura atual. |
| `super-cobertura.cjs` | Análise profunda de lacunas em branches e condições complexas. |
| `analisar-cobertura.cjs` | Comparação detalhada entre pacotes e classes. |
| `gerar-plano-cobertura.cjs` | Cria um arquivo Markdown com os próximos passos para aumentar a cobertura. |

## Como Utilizar

### 1. Preparação
Antes de rodar as análises, é necessário garantir que o arquivo `jacocoTestReport.xml` esteja atualizado:
```bash
./gradlew :backend:test :backend:jacocoTestReport
```

### 2. Análise Rápida
Para uma visão geral imediata:
```bash
node backend/etc/scripts/verificar-cobertura.cjs
```

### 3. Identificação de Lacunas (Deep Dive)
Para encontrar linhas específicas que não estão sendo testadas:
```bash
node backend/etc/scripts/super-cobertura.cjs
```

### 4. Geração de Plano de Trabalho
Para gerar um relatório técnico das classes prioritárias:
```bash
node backend/etc/scripts/gerar-plano-cobertura.cjs
```

## Referências
- `test-coverage-plan.md` no root do projeto.
- `coverage-tracking.md` para acompanhamento de metas.
