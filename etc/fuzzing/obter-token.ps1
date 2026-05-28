# Script utilitário para obter o token JWT de teste para o WuppieFuzz no Windows (PowerShell)
param (
    [string]$BaseUrl = "http://localhost:10000",
    [string]$TituloEleitoral = "191919",
    [string]$Senha = "senha"
)

$ErrorActionPreference = "Stop"

Write-Host "--------------------------------------------------------" -ForegroundColor Cyan
Write-Host "SGC - Obtendo token de autenticação para o WuppieFuzz" -ForegroundColor Cyan
Write-Host "Alvo: $BaseUrl" -ForegroundColor Cyan
Write-Host "Usuário de Teste: $TituloEleitoral" -ForegroundColor Cyan
Write-Host "--------------------------------------------------------" -ForegroundColor Cyan

$body = @{
    tituloEleitoral = $TituloEleitoral
    senha = $Senha
} | ConvertTo-Json

try {
    # Faz o login contra o backend do SGC. 
    # O backend responde com os cookies e com o JSON da sessão.
    $session = New-Object Microsoft.PowerShell.Commands.WebRequestSession
    $response = Invoke-RestMethod -Uri "$BaseUrl/api/usuarios/login" `
                                  -Method Post `
                                  -Body $body `
                                  -ContentType "application/json" `
                                  -WebSession $session

    # Busca pelo cookie 'jwtToken'
    $uri = New-Object System.Uri($BaseUrl)
    $cookies = $session.Cookies.GetCookies($uri)
    $jwtCookie = $cookies | Where-Object { $_.Name -eq "jwtToken" }

    if ($jwtCookie) {
        $token = $jwtCookie.Value
        Write-Host "`n[Sucesso] Autenticação concluída e JWT recuperado!" -ForegroundColor Green
        Write-Host "Insira o cabeçalho a seguir nas requisições do seu fuzzer:" -ForegroundColor Yellow
        Write-Host "Authorization: Bearer $token" -ForegroundColor White
        Write-Host "`nToken bruto para cópia rápida:" -ForegroundColor Gray
        Write-Host $token -ForegroundColor Green
        
        # Copia para o clipboard se estiver rodando em ambiente interativo
        try {
            Set-Clipboard -Value $token
            Write-Host "`n[Dica] O token foi copiado automaticamente para a sua área de transferência!" -ForegroundColor DarkGreen
        } catch {}
    } else {
        Write-Host "`n[Erro] O cookie 'jwtToken' não foi retornado pelo servidor." -ForegroundColor Red
        Write-Host "Verifique se a conta de teste possui apenas 1 perfil associado." -ForegroundColor Yellow
    }
} catch {
    Write-Host "`n[Falha] Não foi possível autenticar." -ForegroundColor Red
    Write-Host "Detalhes do erro: $_" -ForegroundColor Red
    Write-Host "`nCertifique-se de que o backend do SGC está rodando localmente no perfil 'e2e'." -ForegroundColor Yellow
    Write-Host "Comando recomendado: ./gradlew :backend:bootRun -PENV=e2e" -ForegroundColor Yellow
}
