# 🎯 Cobertura 100% - Referência Rápida

## Comandos Essenciais

```bash
# Pipeline completo de análise
./backend/etc/scripts/cobertura-100.sh

# Análise rápida (usa relatório existente)
node backend/etc/scripts/super-cobertura.cjs

# Análise completa (roda testes primeiro)
node backend/etc/scripts/super-cobertura.cjs --run

# Gerar plano de ação
node backend/etc/scripts/gerar-plano-cobertura.cjs

# Criar esqueleto de teste
node backend/etc/scripts/gerar-testes-cobertura.cjs <NomeClasse>
```

## Fluxo de Trabalho

```
1. Análise    →  ./backend/etc/scripts/cobertura-100.sh
2. Plano      →  cat plano-100-cobertura.md
3. Criar Test →  node backend/etc/scripts/gerar-testes-cobertura.cjs <Classe>
4. Implementar→  (código)
5. Verificar  →  ./gradlew :backend:test
6. Repetir    →  Voltar ao passo 1
```

## Arquivos Gerados

| Arquivo | Conteúdo |
|---------|----------|
| `plano-100-cobertura.md` | 📋 Plano completo com todas lacunas |
| `cobertura-detalhada.txt` | 📊 Análise com tabelas |
| `cobertura_lacunas.json` | 🔧 Dados estruturados (JSON) |
| `analise-testes.md` | 📝 Arquivos sem testes |
| `priorizacao-testes.md` | ⭐ Testes por prioridade |

## Prioridades

- 🔴 **P1**: Services, Validators, Policies (CRÍTICO)
- 🟡 **P2**: Controllers, Mappers (IMPORTANTE)
- 🟢 **P3**: DTOs, Models, Configs (NORMAL)

## Tips

**Cobrir Linhas:**
- Executar cada linha pelo menos uma vez

**Cobrir Branches:**
- `if/else` → Testar TRUE e FALSE
- `switch` → Testar todos cases + default  
- `try/catch` → Testar sucesso e exceção
- `&&/||` → Testar todas combinações

## Ver Relatórios

```bash
# HTML interativo
open backend/build/reports/jacoco/test/html/index.html

# Apenas uma classe
node backend/etc/scripts/verificar-cobertura.cjs ProcessoFacade

# Top 20 com mais gaps
node backend/etc/scripts/verificar-cobertura.cjs --missed | head -n 50
```

## Troubleshooting

```bash
# Limpar e reconstruir
./gradlew :backend:clean :backend:test

# Verificar dependências
npm list xml2js
python3 --version

# Instalar dependências
npm install xml2js
```

---

📖 **Guia completo**: `GUIA-COBERTURA-100.md`
