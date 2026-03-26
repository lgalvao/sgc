param(
    [ValidateSet('rapido', 'completo', 'backend', 'frontend')]
    [string]$Perfil = 'rapido'
)

$ErrorActionPreference = 'Stop'

$diretorioScript = Split-Path -Parent $MyInvocation.MyCommand.Path
$diretorioRaiz = [System.IO.Path]::GetFullPath((Join-Path $diretorioScript '..\..\..'))
$diretorioDashboard = Join-Path $diretorioRaiz 'etc\qa-dashboard'
$diretorioRuns = Join-Path $diretorioDashboard 'runs'
$diretorioLatest = Join-Path $diretorioDashboard 'latest'
$versaoSchema = '1.0.0'

$perfis = @{
    rapido = @('backendUnitario', 'backendCobertura', 'frontendCobertura', 'frontendLint', 'frontendTypecheck')
    completo = @('backendUnitario', 'backendIntegracao', 'backendCobertura', 'frontendCobertura', 'frontendLint', 'frontendTypecheck', 'e2ePlaywright')
    backend = @('backendUnitario', 'backendIntegracao', 'backendCobertura')
    frontend = @('frontendCobertura', 'frontendLint', 'frontendTypecheck')
}

function ConverterEm-CaminhoRelativo {
    param([string]$Caminho)

    $uriBase = New-Object System.Uri(($diretorioRaiz.TrimEnd('\') + '\'))
    $uriAlvo = New-Object System.Uri($Caminho)
    return [System.Uri]::UnescapeDataString($uriBase.MakeRelativeUri($uriAlvo).ToString())
}

function Novo-DiretorioSeNecessario {
    param([string]$Caminho)

    if (-not (Test-Path $Caminho)) {
        New-Item -ItemType Directory -Path $Caminho | Out-Null
    }
}

function Nova-Execucao {
    param(
        [string]$Codigo,
        [string]$Nome,
        [string]$Categoria,
        [string]$Comando,
        [string]$Diretorio
    )

    return [ordered]@{
        codigo = $Codigo
        nome = $Nome
        categoria = $Categoria
        status = 'nao_executado'
        duracaoMs = 0
        comando = $Comando
        diretorio = $Diretorio
        sumario = ''
        metricas = @{}
        erros = @()
        artefatos = @()
    }
}

function Executar-ComandoCapturando {
    param(
        [string]$Arquivo,
        [string[]]$Argumentos,
        [string]$DiretorioTrabalho,
        [string]$ArquivoLog
    )

    $inicio = Get-Date
    $argumentosEscapados = @($Argumentos | ForEach-Object {
        if ($_ -match '\s') {
            '"' + $_.Replace('"', '\"') + '"'
        } else {
            $_
        }
    }) -join ' '
    $comandoCompleto = '"' + $Arquivo + '" ' + $argumentosEscapados + ' > "' + $ArquivoLog + '" 2>&1'

    Push-Location $DiretorioTrabalho
    try {
        & cmd.exe /d /s /c $comandoCompleto | Out-Null
        $codigo = $LASTEXITCODE
        if ($null -eq $codigo) {
            $codigo = 0
        }
    } finally {
        Pop-Location
    }

    $duracaoMs = [math]::Round(((Get-Date) - $inicio).TotalMilliseconds, 0)
    return @{
        codigo = [int]$codigo
        duracaoMs = $duracaoMs
        log = $ArquivoLog
        texto = if (Test-Path $ArquivoLog) { Get-Content $ArquivoLog -Raw } else { '' }
        inicio = $inicio
    }
}

function Testar-ArtefatoFresco {
    param(
        [string]$Caminho,
        [datetime]$InicioExecucao
    )

    if (-not (Test-Path $Caminho)) {
        return $false
    }

    $item = Get-Item $Caminho
    return $item.LastWriteTime -ge $InicioExecucao
}

function Extrair-LinhasFinaisLog {
    param(
        [string]$Texto,
        [int]$Limite = 12
    )

    $linhas = $Texto -split "`r?`n" | Where-Object { $_.Trim() -ne '' }
    if ($linhas.Count -le $Limite) {
        return @($linhas)
    }

    return @($linhas[($linhas.Count - $Limite)..($linhas.Count - 1)])
}

function Consolidar-JUnit {
    param([string]$Diretorio)

    $resultado = @{
        testes = 0
        falhas = 0
        ignorados = 0
        sucessos = 0
        tempoSegundos = 0.0
        arquivosXml = @()
    }

    if (-not (Test-Path $Diretorio)) {
        return $resultado
    }

    $arquivos = Get-ChildItem -Path $Diretorio -Filter '*.xml' -File
    foreach ($arquivo in $arquivos) {
        [xml]$xml = Get-Content $arquivo.FullName
        $suite = $xml.testsuite
        if ($null -eq $suite) {
            continue
        }

        $resultado.testes += [int]$suite.tests
        $resultado.falhas += ([int]$suite.failures + [int]$suite.errors)
        $resultado.ignorados += [int]$suite.skipped
        $resultado.tempoSegundos += [double]$suite.time
        $resultado.arquivosXml += (ConverterEm-CaminhoRelativo $arquivo.FullName)
    }

    $resultado.sucessos = [math]::Max($resultado.testes - $resultado.falhas - $resultado.ignorados, 0)
    return $resultado
}

function Obter-Percentual {
    param(
        [double]$Cobertos,
        [double]$Perdidos
    )

    $total = $Cobertos + $Perdidos
    if ($total -le 0) {
        return 0
    }

    return [math]::Round(($Cobertos / $total) * 100, 2)
}

function Obter-ValorOuPadrao {
    param(
        $Valor,
        $Padrao
    )

    if ($null -eq $Valor) {
        return $Padrao
    }

    return $Valor
}

function Extrair-CoberturaJacoco {
    param([string]$ArquivoXml)

    if (-not (Test-Path $ArquivoXml)) {
        throw "Relatorio JaCoCo nao encontrado em $(ConverterEm-CaminhoRelativo $ArquivoXml)"
    }

    [xml]$xml = Get-Content $ArquivoXml
    $counters = @{}
    foreach ($counter in $xml.report.counter) {
        $counters[$counter.type] = @{
            cobertos = [int]$counter.covered
            perdidos = [int]$counter.missed
            percentual = Obter-Percentual ([int]$counter.covered) ([int]$counter.missed)
        }
    }

    $classes = @()
    foreach ($package in $xml.report.package) {
        foreach ($class in $package.class) {
            $line = $class.counter | Where-Object { $_.type -eq 'LINE' } | Select-Object -First 1
            $branch = $class.counter | Where-Object { $_.type -eq 'BRANCH' } | Select-Object -First 1
            $classes += [pscustomobject]@{
                nome = ($class.name -replace '/', '.')
                linhasPercentual = Obter-Percentual ([int](Obter-ValorOuPadrao $line.covered 0)) ([int](Obter-ValorOuPadrao $line.missed 0))
                branchesPercentual = Obter-Percentual ([int](Obter-ValorOuPadrao $branch.covered 0)) ([int](Obter-ValorOuPadrao $branch.missed 0))
            }
        }
    }

    return @{
        linhas = if ($counters.ContainsKey('LINE')) { $counters.LINE } else { @{ cobertos = 0; perdidos = 0; percentual = 0 } }
        branches = if ($counters.ContainsKey('BRANCH')) { $counters.BRANCH } else { @{ cobertos = 0; perdidos = 0; percentual = 0 } }
        instrucoes = if ($counters.ContainsKey('INSTRUCTION')) { $counters.INSTRUCTION } else { @{ cobertos = 0; perdidos = 0; percentual = 0 } }
        metodos = if ($counters.ContainsKey('METHOD')) { $counters.METHOD } else { @{ cobertos = 0; perdidos = 0; percentual = 0 } }
        classes = @($classes | Sort-Object linhasPercentual | Select-Object -First 20)
    }
}

function Extrair-CoberturaFrontend {
    param([string]$ArquivoJson)

    if (-not (Test-Path $ArquivoJson)) {
        throw "Relatorio V8 nao encontrado em $(ConverterEm-CaminhoRelativo $ArquivoJson)"
    }

    $json = Get-Content $ArquivoJson -Raw | ConvertFrom-Json
    $arquivos = @()
    $totais = @{
        statements = @{ cobertos = 0; total = 0 }
        branches = @{ cobertos = 0; total = 0 }
        functions = @{ cobertos = 0; total = 0 }
        lines = @{ cobertos = 0; total = 0 }
    }

    foreach ($entrada in $json.PSObject.Properties) {
        $dados = $entrada.Value
        $statementTotal = @($dados.s.PSObject.Properties).Count
        $statementCobertos = @($dados.s.PSObject.Properties | Where-Object { $_.Value -gt 0 }).Count
        $functionTotal = @($dados.f.PSObject.Properties).Count
        $functionCobertos = @($dados.f.PSObject.Properties | Where-Object { $_.Value -gt 0 }).Count

        $branchTotal = 0
        $branchCobertos = 0
        foreach ($branch in $dados.b.PSObject.Properties) {
            $valores = @($branch.Value)
            $branchTotal += $valores.Count
            $branchCobertos += @($valores | Where-Object { $_ -gt 0 }).Count
        }

        $lineTotal = @($dados.statementMap.PSObject.Properties).Count
        $lineCobertos = $statementCobertos

        $totais.statements.total += $statementTotal
        $totais.statements.cobertos += $statementCobertos
        $totais.functions.total += $functionTotal
        $totais.functions.cobertos += $functionCobertos
        $totais.branches.total += $branchTotal
        $totais.branches.cobertos += $branchCobertos
        $totais.lines.total += $lineTotal
        $totais.lines.cobertos += $lineCobertos

        $arquivos += [pscustomobject]@{
            arquivo = ConverterEm-CaminhoRelativo $entrada.Name
            statementsPercentual = if ($statementTotal -gt 0) { [math]::Round(($statementCobertos / $statementTotal) * 100, 2) } else { 0 }
            branchesPercentual = if ($branchTotal -gt 0) { [math]::Round(($branchCobertos / $branchTotal) * 100, 2) } else { 0 }
            functionsPercentual = if ($functionTotal -gt 0) { [math]::Round(($functionCobertos / $functionTotal) * 100, 2) } else { 0 }
            linesPercentual = if ($lineTotal -gt 0) { [math]::Round(($lineCobertos / $lineTotal) * 100, 2) } else { 0 }
        }
    }

    return @{
        statements = @{
            cobertos = $totais.statements.cobertos
            total = $totais.statements.total
            percentual = if ($totais.statements.total -gt 0) { [math]::Round(($totais.statements.cobertos / $totais.statements.total) * 100, 2) } else { 0 }
        }
        branches = @{
            cobertos = $totais.branches.cobertos
            total = $totais.branches.total
            percentual = if ($totais.branches.total -gt 0) { [math]::Round(($totais.branches.cobertos / $totais.branches.total) * 100, 2) } else { 0 }
        }
        functions = @{
            cobertos = $totais.functions.cobertos
            total = $totais.functions.total
            percentual = if ($totais.functions.total -gt 0) { [math]::Round(($totais.functions.cobertos / $totais.functions.total) * 100, 2) } else { 0 }
        }
        lines = @{
            cobertos = $totais.lines.cobertos
            total = $totais.lines.total
            percentual = if ($totais.lines.total -gt 0) { [math]::Round(($totais.lines.cobertos / $totais.lines.total) * 100, 2) } else { 0 }
        }
        arquivos = @($arquivos | Sort-Object linesPercentual | Select-Object -First 20)
    }
}

function Extrair-ResumoVitest {
    param([string]$Texto)

    $textoNormalizado = [string]$Texto
    $passados = if ($textoNormalizado -match '(\d+)\s+passed') { [int]$matches[1] } else { 0 }
    $falhas = if ($textoNormalizado -match '(\d+)\s+failed') { [int]$matches[1] } else { 0 }
    $ignorados = if ($textoNormalizado -match '(\d+)\s+skipped') { [int]$matches[1] } else { 0 }

    return @{
        testes = $passados + $falhas + $ignorados
        sucessos = $passados
        falhas = $falhas
        ignorados = $ignorados
    }
}

function Extrair-ResumoLint {
    param([string]$Texto)

    $problemas = if ($Texto -match '(\d+)\s+problems?') { [int]$matches[1] } else { 0 }
    $avisos = if ($Texto -match '(\d+)\s+warnings?') { [int]$matches[1] } else { 0 }
    return @{
        erros = $problemas
        avisos = $avisos
    }
}

function Extrair-ResumoTypecheck {
    param([string]$Texto)

    $linhas = $Texto -split "`r?`n"
    $erros = @($linhas | Where-Object { $_ -match ' error TS\d+:' }).Count
    return @{
        erros = $erros
    }
}

function Extrair-ResumoPlaywright {
    param([string]$Texto)

    $json = $Texto | ConvertFrom-Json
    $stats = $json.stats
    $esperado = [int](Obter-ValorOuPadrao $stats.expected 0)
    $inesperado = [int](Obter-ValorOuPadrao $stats.unexpected 0)
    $ignorados = [int](Obter-ValorOuPadrao $stats.skipped 0)
    $flaky = [int](Obter-ValorOuPadrao $stats.flaky 0)

    return @{
        testes = $esperado + $inesperado + $ignorados + $flaky
        sucessos = $esperado
        falhas = $inesperado
        ignorados = $ignorados
        flaky = $flaky
    }
}

function Obter-StatusExecucao {
    param(
        [int]$CodigoSaida,
        [hashtable]$Metricas
    )

    if ($CodigoSaida -ne 0) {
        return 'falha'
    }

    if (([int](Obter-ValorOuPadrao $Metricas.falhas 0) -gt 0) -or ([int](Obter-ValorOuPadrao $Metricas.erros 0) -gt 0)) {
        return 'falha'
    }

    if (([int](Obter-ValorOuPadrao $Metricas.avisos 0) -gt 0) -or ([int](Obter-ValorOuPadrao $Metricas.flaky 0) -gt 0)) {
        return 'alerta'
    }

    return 'sucesso'
}

function Obter-Git {
    $branch = (git rev-parse --abbrev-ref HEAD).Trim()
    $commit = (git rev-parse HEAD).Trim()
    $commitCurto = (git rev-parse --short HEAD).Trim()
    $worktreeSujo = ((git status --porcelain) | Measure-Object).Count -gt 0

    return @{
        branch = $branch
        commit = $commit
        commitCurto = $commitCurto
        worktreeSujo = $worktreeSujo
    }
}

function Gerar-ResumoMarkdown {
    param([hashtable]$Snapshot)

    $linhas = @(
        '# Resumo do Dashboard de QA',
        '',
        "- Gerado em: $($Snapshot.metadados.geradoEm)",
        "- Perfil: $($Snapshot.metadados.perfilExecucao)",
        "- Branch: $($Snapshot.metadados.git.branch)",
        "- Commit: $($Snapshot.metadados.git.commitCurto)",
        "- Status geral: $($Snapshot.resumo.statusGeral)",
        "- Indice de saude: $($Snapshot.resumo.indiceSaude)",
        '',
        '## Verificacoes',
        '',
        '| Verificacao | Status | Duracao (s) | Sumario |',
        '| --- | --- | ---: | --- |'
    )

    foreach ($verificacao in $Snapshot.verificacoes) {
        $duracaoSegundos = [math]::Round($verificacao.duracaoMs / 1000, 2)
        $linhas += "| $($verificacao.nome) | $($verificacao.status) | $duracaoSegundos | $($verificacao.sumario) |"
    }

    $linhas += ''
    $linhas += '## Hotspots'
    $linhas += ''

    if ($Snapshot.hotspots.Count -eq 0) {
        $linhas += 'Sem hotspots calculados.'
    } else {
        foreach ($hotspot in ($Snapshot.hotspots | Select-Object -First 10)) {
            $linhas += "- $($hotspot.nome): risco $($hotspot.risco)"
        }
    }

    return ($linhas -join "`n") + "`n"
}

function Obter-MetricasNulasFrontend {
    return @{
        functions = $null
        lines = $null
        branches = $null
        statements = $null
        arquivos = @()
    }
}

Novo-DiretorioSeNecessario $diretorioRuns
Novo-DiretorioSeNecessario $diretorioLatest

$timestamp = (Get-Date).ToString('yyyy-MM-ddTHH-mm-ssK').Replace(':', '-')
$diretorioExecucao = Join-Path $diretorioRuns $timestamp
Novo-DiretorioSeNecessario $diretorioExecucao

$adaptadores = @{
    backendUnitario = {
        $execucao = Nova-Execucao 'backend-unitario' 'Backend unitario' 'teste' '.\gradlew.bat :backend:unitTest' 'backend'
        $saida = Executar-ComandoCapturando '.\gradlew.bat' @(':backend:unitTest') $diretorioRaiz (Join-Path $diretorioExecucao 'backend-unitario.log')
        $metricas = Consolidar-JUnit (Join-Path $diretorioRaiz 'backend\build\test-results\unitTest')
        $execucao.status = Obter-StatusExecucao $saida.codigo $metricas
        $execucao.duracaoMs = $saida.duracaoMs
        $execucao.metricas = $metricas
        $execucao.sumario = "$($metricas.sucessos)/$($metricas.testes) testes aprovados no backend unitario."
        $execucao.erros = if ($saida.codigo -eq 0) { @() } else { @(Extrair-LinhasFinaisLog $saida.texto) }
        $execucao.artefatos = @($metricas.arquivosXml + (ConverterEm-CaminhoRelativo $saida.log))
        return $execucao
    }
    backendIntegracao = {
        $execucao = Nova-Execucao 'backend-integracao' 'Backend integracao' 'teste' '.\gradlew.bat :backend:integrationTest' 'backend'
        $saida = Executar-ComandoCapturando '.\gradlew.bat' @(':backend:integrationTest') $diretorioRaiz (Join-Path $diretorioExecucao 'backend-integracao.log')
        $metricas = Consolidar-JUnit (Join-Path $diretorioRaiz 'backend\build\test-results\integrationTest')
        $execucao.status = Obter-StatusExecucao $saida.codigo $metricas
        $execucao.duracaoMs = $saida.duracaoMs
        $execucao.metricas = $metricas
        $execucao.sumario = "$($metricas.sucessos)/$($metricas.testes) testes aprovados no backend integracao."
        $execucao.erros = if ($saida.codigo -eq 0) { @() } else { @(Extrair-LinhasFinaisLog $saida.texto) }
        $execucao.artefatos = @($metricas.arquivosXml + (ConverterEm-CaminhoRelativo $saida.log))
        return $execucao
    }
    backendCobertura = {
        $execucao = Nova-Execucao 'backend-cobertura' 'Backend cobertura' 'cobertura' '.\gradlew.bat :backend:jacocoTestReport' 'backend'
        $saida = Executar-ComandoCapturando '.\gradlew.bat' @(':backend:jacocoTestReport') $diretorioRaiz (Join-Path $diretorioExecucao 'backend-cobertura.log')
        $arquivoXml = Join-Path $diretorioRaiz 'backend\build\reports\jacoco\test\jacocoTestReport.xml'
        $metricas = $null
        if (($saida.codigo -eq 0) -and (Testar-ArtefatoFresco $arquivoXml $saida.inicio)) {
            $metricas = Extrair-CoberturaJacoco $arquivoXml
        }

        $execucao.status = Obter-StatusExecucao $saida.codigo @{}
        $execucao.duracaoMs = $saida.duracaoMs
        $execucao.metricas = $metricas
        $execucao.sumario = if ($null -ne $metricas) {
            "Cobertura backend: $($metricas.linhas.percentual)% de linhas e $($metricas.branches.percentual)% de branches."
        } else {
            'Cobertura backend indisponivel nesta execucao.'
        }
        $execucao.erros = if ($saida.codigo -eq 0) { @() } else { @(Extrair-LinhasFinaisLog $saida.texto) }
        $execucao.artefatos = @()
        if ($null -ne $metricas) {
            $execucao.artefatos += (ConverterEm-CaminhoRelativo $arquivoXml)
        }
        $execucao.artefatos += (ConverterEm-CaminhoRelativo $saida.log)
        return $execucao
    }
    frontendCobertura = {
        $execucao = Nova-Execucao 'frontend-cobertura' 'Frontend cobertura' 'cobertura' 'npm run coverage:unit --prefix frontend' 'frontend'
        $saida = Executar-ComandoCapturando 'npm.cmd' @('run', 'coverage:unit', '--prefix', 'frontend') $diretorioRaiz (Join-Path $diretorioExecucao 'frontend-cobertura.log')
        $arquivoJson = Join-Path $diretorioRaiz 'frontend\coverage\coverage-final.json'
        $cobertura = $null
        if (($saida.codigo -eq 0) -and (Testar-ArtefatoFresco $arquivoJson $saida.inicio)) {
            $cobertura = Extrair-CoberturaFrontend $arquivoJson
        }
        $testes = Extrair-ResumoVitest $saida.texto
        $metricas = @{
            testes = $testes
            cobertura = $cobertura
        }
        $execucao.status = Obter-StatusExecucao $saida.codigo $testes
        $execucao.duracaoMs = $saida.duracaoMs
        $execucao.metricas = $metricas
        $execucao.sumario = if ($null -ne $cobertura) {
            "Cobertura frontend: $($cobertura.lines.percentual)% de linhas com $($testes.sucessos) testes aprovados."
        } else {
            "Cobertura frontend indisponivel nesta execucao com $($testes.sucessos) testes aprovados."
        }
        $execucao.erros = if ($saida.codigo -eq 0) { @() } else { @(Extrair-LinhasFinaisLog $saida.texto) }
        $execucao.artefatos = @()
        if ($null -ne $cobertura) {
            $execucao.artefatos += (ConverterEm-CaminhoRelativo $arquivoJson)
        }
        $execucao.artefatos += (ConverterEm-CaminhoRelativo $saida.log)
        return $execucao
    }
    frontendLint = {
        $execucao = Nova-Execucao 'frontend-lint' 'Frontend lint' 'qualidade' 'npx eslint . --ext .vue,.js,.mjs,.ts --max-warnings 0 --ignore-pattern coverage --ignore-pattern dist' 'frontend'
        $saida = Executar-ComandoCapturando 'npx.cmd' @('eslint', '.', '--ext', '.vue,.js,.mjs,.ts', '--max-warnings', '0', '--ignore-pattern', 'coverage', '--ignore-pattern', 'dist') (Join-Path $diretorioRaiz 'frontend') (Join-Path $diretorioExecucao 'frontend-lint.log')
        $metricas = Extrair-ResumoLint $saida.texto
        $execucao.status = Obter-StatusExecucao $saida.codigo $metricas
        $execucao.duracaoMs = $saida.duracaoMs
        $execucao.metricas = $metricas
        $execucao.sumario = if ($metricas.erros -gt 0) { "Lint frontend encontrou $($metricas.erros) problemas." } else { 'Lint frontend sem problemas.' }
        $execucao.erros = if ($saida.codigo -eq 0) { @() } else { @(Extrair-LinhasFinaisLog $saida.texto) }
        $execucao.artefatos = @((ConverterEm-CaminhoRelativo $saida.log))
        return $execucao
    }
    frontendTypecheck = {
        $execucao = Nova-Execucao 'frontend-typecheck' 'Frontend typecheck' 'qualidade' 'npm run typecheck --prefix frontend' 'frontend'
        $saida = Executar-ComandoCapturando 'npm.cmd' @('run', 'typecheck', '--prefix', 'frontend') $diretorioRaiz (Join-Path $diretorioExecucao 'frontend-typecheck.log')
        $metricas = Extrair-ResumoTypecheck $saida.texto
        $execucao.status = Obter-StatusExecucao $saida.codigo $metricas
        $execucao.duracaoMs = $saida.duracaoMs
        $execucao.metricas = $metricas
        $execucao.sumario = if ($metricas.erros -gt 0) { "Typecheck frontend encontrou $($metricas.erros) erros." } else { 'Typecheck frontend sem erros.' }
        $execucao.erros = if ($saida.codigo -eq 0) { @() } else { @(Extrair-LinhasFinaisLog $saida.texto) }
        $execucao.artefatos = @((ConverterEm-CaminhoRelativo $saida.log))
        return $execucao
    }
    e2ePlaywright = {
        $execucao = Nova-Execucao 'e2e-playwright' 'E2E Playwright' 'teste' 'npx playwright test --reporter=json' '.'
        $saida = Executar-ComandoCapturando 'npx.cmd' @('playwright', 'test', '--reporter=json') $diretorioRaiz (Join-Path $diretorioExecucao 'e2e-playwright.log')
        $metricas = Extrair-ResumoPlaywright $saida.texto
        $execucao.status = Obter-StatusExecucao $saida.codigo $metricas
        $execucao.duracaoMs = $saida.duracaoMs
        $execucao.metricas = $metricas
        $execucao.sumario = "$($metricas.sucessos)/$($metricas.testes) testes E2E aprovados."
        $execucao.erros = if ($saida.codigo -eq 0) { @() } else { @(Extrair-LinhasFinaisLog $saida.texto) }
        $execucao.artefatos = @((ConverterEm-CaminhoRelativo $saida.log))
        return $execucao
    }
}

$inicio = Get-Date
$verificacoes = @()
foreach ($adaptador in $perfis[$Perfil]) {
    Write-Host "Executando $adaptador..."
    $verificacoes += & $adaptadores[$adaptador]
}

$cobertura = @{
    backend = @{
        linhas = ($verificacoes | Where-Object { $_.codigo -eq 'backend-cobertura' } | Select-Object -First 1).metricas.linhas
        branches = ($verificacoes | Where-Object { $_.codigo -eq 'backend-cobertura' } | Select-Object -First 1).metricas.branches
        itensCriticos = @((Obter-ValorOuPadrao (($verificacoes | Where-Object { $_.codigo -eq 'backend-cobertura' } | Select-Object -First 1).metricas.classes) @()))
    }
    frontend = @{
        lines = ($verificacoes | Where-Object { $_.codigo -eq 'frontend-cobertura' } | Select-Object -First 1).metricas.cobertura.lines
        branches = ($verificacoes | Where-Object { $_.codigo -eq 'frontend-cobertura' } | Select-Object -First 1).metricas.cobertura.branches
        functions = ($verificacoes | Where-Object { $_.codigo -eq 'frontend-cobertura' } | Select-Object -First 1).metricas.cobertura.functions
        statements = ($verificacoes | Where-Object { $_.codigo -eq 'frontend-cobertura' } | Select-Object -First 1).metricas.cobertura.statements
        itensCriticos = @((Obter-ValorOuPadrao (($verificacoes | Where-Object { $_.codigo -eq 'frontend-cobertura' } | Select-Object -First 1).metricas.cobertura.arquivos) @()))
    }
}

$qualidade = @{
    lint = ($verificacoes | Where-Object { $_.codigo -eq 'frontend-lint' } | Select-Object -First 1).metricas
    typecheck = ($verificacoes | Where-Object { $_.codigo -eq 'frontend-typecheck' } | Select-Object -First 1).metricas
}

$confiabilidade = @{
    testesIgnorados = (
        ([int](Obter-ValorOuPadrao (($verificacoes | Where-Object { $_.codigo -eq 'backend-unitario' } | Select-Object -First 1).metricas.ignorados) 0)) +
        ([int](Obter-ValorOuPadrao (($verificacoes | Where-Object { $_.codigo -eq 'backend-integracao' } | Select-Object -First 1).metricas.ignorados) 0)) +
        ([int](Obter-ValorOuPadrao (($verificacoes | Where-Object { $_.codigo -eq 'frontend-cobertura' } | Select-Object -First 1).metricas.testes.ignorados) 0)) +
        ([int](Obter-ValorOuPadrao (($verificacoes | Where-Object { $_.codigo -eq 'e2e-playwright' } | Select-Object -First 1).metricas.ignorados) 0))
    )
    testesFlaky = ([int](Obter-ValorOuPadrao (($verificacoes | Where-Object { $_.codigo -eq 'e2e-playwright' } | Select-Object -First 1).metricas.flaky) 0))
    suitesLentas = @($verificacoes | Sort-Object { [int]$_.duracaoMs } -Descending | Select-Object -First 5 | ForEach-Object {
        [pscustomobject]@{
            codigo = $_.codigo
            nome = $_.nome
            duracaoMs = $_.duracaoMs
        }
    })
}

$hotspots = @()
foreach ($classe in $cobertura.backend.itensCriticos) {
    if (($classe.nome -match 'Builder$') -or ($classe.nome -match '\$.*Builder') -or ($classe.nome -match '\.dto\.') -or ($classe.nome -eq 'sgc.comum.ComumDtos')) {
        continue
    }
    $hotspots += [pscustomobject]@{
        codigo = "backend:$($classe.nome)"
        nome = $classe.nome
        risco = [math]::Round(100 - $classe.linhasPercentual, 2)
        motivos = @("Cobertura backend baixa: $($classe.linhasPercentual)%")
    }
}
foreach ($arquivo in $cobertura.frontend.itensCriticos) {
    $hotspots += [pscustomobject]@{
        codigo = "frontend:$($arquivo.arquivo)"
        nome = $arquivo.arquivo
        risco = [math]::Round(100 - $arquivo.linesPercentual, 2)
        motivos = @("Cobertura frontend baixa: $($arquivo.linesPercentual)%")
    }
}
$hotspots = @($hotspots | Sort-Object risco -Descending | Select-Object -First 15)

$resumo = @{
    statusGeral = if (@($verificacoes | Where-Object { $_.status -eq 'falha' }).Count -gt 0) { 'vermelho' } elseif (@($verificacoes | Where-Object { $_.status -eq 'alerta' }).Count -gt 0) { 'amarelo' } else { 'verde' }
    indiceSaude = 0
    totais = @{
        verificacoes = $verificacoes.Count
        sucesso = @($verificacoes | Where-Object { $_.status -eq 'sucesso' }).Count
        falha = @($verificacoes | Where-Object { $_.status -eq 'falha' }).Count
        alerta = @($verificacoes | Where-Object { $_.status -eq 'alerta' }).Count
        naoExecutado = @($verificacoes | Where-Object { $_.status -eq 'nao_executado' }).Count
    }
}

$pontuacao = $resumo.totais.sucesso + ($resumo.totais.alerta * 0.5)
$resumo.indiceSaude = [math]::Round(($pontuacao / [math]::Max($resumo.totais.verificacoes, 1)) * 100, 2)

$snapshot = [ordered]@{
    versaoSchema = $versaoSchema
    metadados = @{
        geradoEm = (Get-Date).ToString('o')
        perfilExecucao = $Perfil
        duracaoTotalMs = [math]::Round(((Get-Date) - $inicio).TotalMilliseconds, 0)
        maquina = $env:COMPUTERNAME
        sistemaOperacional = "$($PSVersionTable.OS)"
        git = (Obter-Git)
    }
    resumo = $resumo
    verificacoes = @($verificacoes)
    cobertura = $cobertura
    qualidade = $qualidade
    confiabilidade = $confiabilidade
    hotspots = @($hotspots)
}

$arquivoSnapshot = Join-Path $diretorioExecucao 'snapshot.json'
$arquivoResumo = Join-Path $diretorioExecucao 'resumo.md'
$arquivoUltimoSnapshot = Join-Path $diretorioLatest 'ultimo-snapshot.json'
$arquivoUltimoResumo = Join-Path $diretorioLatest 'ultimo-resumo.md'

$snapshot | ConvertTo-Json -Depth 12 | Set-Content -Path $arquivoSnapshot -Encoding UTF8
$conteudoResumo = Gerar-ResumoMarkdown $snapshot
$conteudoResumo | Set-Content -Path $arquivoResumo -Encoding UTF8
$snapshot | ConvertTo-Json -Depth 12 | Set-Content -Path $arquivoUltimoSnapshot -Encoding UTF8
$conteudoResumo | Set-Content -Path $arquivoUltimoResumo -Encoding UTF8

Write-Host "Snapshot gerado em $(ConverterEm-CaminhoRelativo $arquivoSnapshot)"
Write-Host "Resumo gerado em $(ConverterEm-CaminhoRelativo $arquivoResumo)"
