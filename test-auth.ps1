# Script de prueba para auth-service
Write-Host "🔧 Probando auth-service..."

# Test 1: Registrar nuevo usuario
Write-Host "`n1️⃣ Probando registro de usuario..."
$newUser = @{
    username = "testuser"
    password = "test123"
    email = "test@example.com"
    nombre = "Test"
    apellido = "User"
} | ConvertTo-Json

try {
    $registerResult = Invoke-RestMethod -Uri "http://localhost:8081/api/auth/register" -Method POST -Body $newUser -ContentType "application/json"
    Write-Host "✅ Usuario registrado exitosamente:"
    $registerResult | ConvertTo-Json
} catch {
    Write-Host "❌ Error en registro: $($_.Exception.Message)"
}

# Test 2: Obtener token OAuth2
Write-Host "`n2️⃣ Probando obtención de token..."
$clientAuth = [Convert]::ToBase64String([Text.Encoding]::ASCII.GetBytes("frontend-client:frontend-secret"))
$headers = @{
    "Authorization" = "Basic $clientAuth"
    "Content-Type" = "application/x-www-form-urlencoded"
}
$body = "grant_type=password&username=admin&password=admin123"

try {
    $tokenResult = Invoke-RestMethod -Uri "http://localhost:8081/oauth2/token" -Method POST -Headers $headers -Body $body
    Write-Host "✅ Token obtenido exitosamente:"
    $tokenResult | ConvertTo-Json
    
    # Test 3: Usar token para obtener información del usuario
    Write-Host "`n3️⃣ Probando endpoint autenticado..."
    $authHeaders = @{
        "Authorization" = "Bearer $($tokenResult.access_token)"
    }
    
    $userInfo = Invoke-RestMethod -Uri "http://localhost:8081/api/auth/me" -Method GET -Headers $authHeaders
    Write-Host "✅ Información del usuario:"
    $userInfo | ConvertTo-Json
    
} catch {
    Write-Host "❌ Error en token: $($_.Exception.Message)"
    if ($_.Exception.Response) {
        Write-Host "Status Code: $($_.Exception.Response.StatusCode)"
    }
}

Write-Host "`n🎯 Pruebas completadas!"
