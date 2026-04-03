# Plano detalhado de simplificação (investigação profunda)

## Objetivo
Reduzir complexidade acidental no SGC sem alterar regras de negócio, preservando testes verdes e melhorando legibilidade/manutenibilidade.

## Metodologia usada (com medições)

### 1) Mapa de hotspots por tamanho de arquivo
Comando usado:

```bash
python - <<'PY'
from pathlib import Path
roots=['backend/src/main','frontend/src','etc/scripts']
files=[]
for r in roots:
 p=Path(r)
 for f in p.rglob('*'):
  if 'node_modules' in f.parts: continue
  if f.is_file() and f.suffix in {'.java','.ts','.vue','.js','.mjs'}:
   try:n=sum(1 for _ in open(f,encoding='utf-8'))
   except: continue
   files.append((n,str(f)))
for n,f in sorted(files, reverse=True)[:15]:
 print(f"{n:4} {f}")
PY
```

Fatos observados:
- `etc/scripts/qa/snapshot-coletar-execucao.mjs`: **874 linhas** (maior hotspot de scripts de QA).
- `backend/src/main/java/sgc/e2e/E2eController.java`: **762 linhas**.
- `backend/src/main/java/sgc/subprocesso/service/SubprocessoTransicaoService.java`: **736 linhas**.
- Várias views/specs do frontend também acima de 500 linhas.

**Conclusão:** há concentração de responsabilidades em poucos arquivos extensos.

### 2) Complexidade ciclomática aproximada por função (heurística)
Comando usado:

```bash
python - <<'PY'
from pathlib import Path
import re
EXTS={'.java','.js','.mjs','.ts'}
rows=[]
for f in Path('.').rglob('*'):
 if 'node_modules' in f.parts or not f.is_file() or f.suffix not in EXTS: continue
 if not any(p in f.parts for p in ['backend','frontend','etc']): continue
 txt=f.read_text(encoding='utf-8',errors='ignore').splitlines()
 for i,l in enumerate(txt,1):
  s=l.strip()
  if re.match(r'^(async\\s+)?function\\s+\\w+\\s*\\(',s) or re.match(r'^(public|private|protected)\\s+[^=;]+\\)\\s*\\{\\s*$',s):
   depth=0;found=False
   for j in range(i-1,len(txt)):
    depth += txt[j].count('{')-txt[j].count('}')
    if '{' in txt[j]: found=True
    if found and depth==0:
     end=j+1;break
   body='\\n'.join(txt[i-1:end])
   cc=1+sum(body.count(k) for k in [' if ',' for ',' while ',' case ',' catch ','&&','||','?'])
   rows.append((cc,end-i+1,str(f),i,end))
for cc,ln,f,s,e in sorted(rows, reverse=True)[:12]:
 print(f"CC~{cc:3} {ln:4}L {f}:{s}-{e}")
PY
```

Fatos observados:
- `extrairCoberturaJacoco` em `snapshot-coletar-execucao.mjs`: **CC~33 (53 linhas)**.
- `extrairCoberturaFrontend` no mesmo arquivo: **CC~25 (78 linhas)**.
- Outras funções desse arquivo também no topo global de complexidade.

**Conclusão:** parte relevante da complexidade do toolkit de QA estava concentrada em um único script com lógica repetida (cálculo de percentuais, parse de contadores e agregação de totais).

### 3) Verificação de justificativa da complexidade
- Complexidade **justificada**: múltiplas fontes de dados (JUnit XML, JaCoCo XML, V8 JSON, lint, typecheck, Playwright) e consolidação em snapshot único.
- Complexidade **não justificada** (acidental): repetição de fórmulas e parse numérico em blocos quase idênticos dentro de `extrairCoberturaFrontend` e `extrairCoberturaJacoco`.

## Plano de simplificação acionável

## Fase 1 — Scripts de QA (em andamento)
1. **Extrair utilitários puros de cobertura** para remover repetição de cálculo/contagem.
2. **Reduzir funções longas** (`extrairCoberturaFrontend`, `extrairCoberturaJacoco`) quebrando em helpers pequenos.
3. **Manter contrato de saída JSON idêntico** (sem breaking change).
4. **Validar com testes/lint do pacote `etc/scripts`**.

Critério de pronto da Fase 1:
- redução mensurável de tamanho/CC das funções-alvo;
- testes relevantes passando.

## Fase 2 — Backend (próxima)
1. Medir métodos mais complexos em `E2eController` e `SubprocessoTransicaoService`.
2. Extrair "orquestração" para services auxiliares com limite máximo de 3 parâmetros (DTO command quando necessário).
3. Proteger comportamento com testes de integração focados em fluxo.

Critério de pronto:
- menos branches por método crítico;
- sem regressão em `./gradlew :backend:test`.

## Fase 3 — Frontend (próxima)
1. Identificar views com responsabilidades mistas (fetch + transformação + render + regra de domínio).
2. Mover transformação para composables/services tipados.
3. Padronizar mensagens de erro com `normalizeError` e remover duplicações de parsing.

Critério de pronto:
- redução de blocos de lógica no `<script setup>` de views grandes;
- `npm run typecheck`, `npm run lint` e `npm run test:unit` verdes.

## Execução iniciada (Fase 1)
A Fase 1 foi iniciada imediatamente após este plano com refatoração incremental do coletor de snapshot.

Resultados parciais medidos após a refatoração:
- `extrairCoberturaFrontend`: de **78 linhas / CC~25** para **41 linhas / CC~11**.
- `extrairCoberturaJacoco`: de **CC~33** para **CC~21**.
- Sem mudança funcional esperada no formato de retorno.

## Riscos e mitigação
- **Risco:** mudança sutil no arredondamento de percentuais.
  - **Mitigação:** helper único de percentual com teste de regressão via CLI.
- **Risco:** divergência de totais consolidados por erro de agregação.
  - **Mitigação:** manter estrutura de saída e executar testes/lint direcionados.

## Evidências de validação executadas nesta rodada
- `npm --prefix etc/scripts test -- test/sgc.test.js -t snapshot`
- `npm --prefix etc/scripts run lint`

Observação: a coleta completa do dashboard (`npm run qa:dashboard`) falhou neste ambiente por ausência de relatório JaCoCo esperado (`backend/build/reports/jacoco/test/jacocoTestReport.xml`), portanto foi tratada como evidência de limitação de ambiente da etapa completa, não da refatoração local.
