param(
  [int]$StartPort = 8173
)

$ErrorActionPreference = "Stop"
$rootDir = [System.IO.Path]::GetFullPath($PSScriptRoot)

if (-not (Test-Path (Join-Path $rootDir "index.html"))) {
  Write-Host "[ERREUR] Fichier index.html introuvable dans $rootDir" -ForegroundColor Red
  Write-Host "Veuillez d'abord EXTRAIRE (Decompresser) le fichier ZIP avant de lancer Start_CERAMICA_PC.bat." -ForegroundColor Yellow
  exit 1
}

$mimeTypes = @{
  ".html" = "text/html; charset=utf-8"
  ".htm"  = "text/html; charset=utf-8"
  ".css"  = "text/css; charset=utf-8"
  ".js"   = "application/javascript; charset=utf-8"
  ".mjs"  = "application/javascript; charset=utf-8"
  ".json" = "application/json; charset=utf-8"
  ".svg"  = "image/svg+xml"
  ".png"  = "image/png"
  ".jpg"  = "image/jpeg"
  ".jpeg" = "image/jpeg"
  ".gif"  = "image/gif"
  ".ico"  = "image/x-icon"
  ".woff" = "font/woff"
  ".woff2"= "font/woff2"
  ".ttf"  = "font/ttf"
}

$listener = $null
$port = $StartPort

for ($p = $StartPort; $p -le ($StartPort + 10); $p++) {
  $candidate = New-Object System.Net.HttpListener
  $candidate.Prefixes.Add("http://localhost:$p/")
  try {
    $candidate.Start()
    $listener = $candidate
    $port = $p
    break
  } catch {
    try { $candidate.Close() } catch {}
  }
}

if ($null -eq $listener) {
  Write-Host "[ERREUR] Impossible de demarrer le serveur local sur les ports $StartPort..$($StartPort + 10)." -ForegroundColor Red
  exit 2
}

$url = "http://localhost:$port/"
Write-Host ""
Write-Host "  CERAMICA POS PC pret sur : $url" -ForegroundColor Green
Write-Host "  Ne fermez pas cette fenetre tant que vous utilisez CERAMICA POS." -ForegroundColor Yellow
Write-Host ""

$browserCandidates = @(
  "$env:ProgramFiles\Google\Chrome\Application\chrome.exe",
  "${env:ProgramFiles(x86)}\Google\Chrome\Application\chrome.exe",
  "$env:LocalAppData\Google\Chrome\Application\chrome.exe",
  "${env:ProgramFiles(x86)}\Microsoft\Edge\Application\msedge.exe",
  "$env:ProgramFiles\Microsoft\Edge\Application\msedge.exe",
  "$env:ProgramFiles\Mozilla Firefox\firefox.exe",
  "${env:ProgramFiles(x86)}\Mozilla Firefox\firefox.exe"
)

$launched = $false
foreach ($exe in $browserCandidates) {
  if ($exe -and (Test-Path $exe)) {
    try {
      Start-Process -FilePath $exe -ArgumentList $url | Out-Null
      $launched = $true
      break
    } catch {}
  }
}

if (-not $launched) {
  Start-Process $url | Out-Null
}

try {
  while ($listener.IsListening) {
    $context = $listener.GetContext()
    $request = $context.Request
    $response = $context.Response

    try {
      $rawPath = [System.Uri]::UnescapeDataString($request.Url.AbsolutePath)
      if ([string]::IsNullOrWhiteSpace($rawPath) -or $rawPath -eq "/") {
        $rawPath = "/index.html"
      }

      $relPath = $rawPath.TrimStart("/") -replace "/", [System.IO.Path]::DirectorySeparatorChar
      $fullPath = [System.IO.Path]::GetFullPath((Join-Path $rootDir $relPath))

      if (-not $fullPath.StartsWith($rootDir, [System.StringComparison]::OrdinalIgnoreCase)) {
        $response.StatusCode = 403
        $response.Close()
        continue
      }

      if ((Test-Path $fullPath) -and (Get-Item $fullPath).PSIsContainer) {
        $fullPath = Join-Path $fullPath "index.html"
      }

      if (-not (Test-Path $fullPath -PathType Leaf)) {
        $response.StatusCode = 404
        $errBytes = [System.Text.Encoding]::UTF8.GetBytes("Not Found")
        $response.ContentType = "text/plain; charset=utf-8"
        $response.ContentLength64 = $errBytes.Length
        $response.OutputStream.Write($errBytes, 0, $errBytes.Length)
        $response.Close()
        continue
      }

      $ext = [System.IO.Path]::GetExtension($fullPath).ToLowerInvariant()
      $contentType = $mimeTypes[$ext]
      if (-not $contentType) {
        $contentType = "application/octet-stream"
      }

      $bytes = [System.IO.File]::ReadAllBytes($fullPath)
      $response.StatusCode = 200
      $response.ContentType = $contentType
      $response.Headers["Cache-Control"] = "no-store"
      $response.ContentLength64 = $bytes.Length
      $response.OutputStream.Write($bytes, 0, $bytes.Length)
      $response.OutputStream.Close()
    } catch {
      try {
        $response.StatusCode = 500
        $response.Close()
      } catch {}
    }
  }
} finally {
  if ($listener) {
    $listener.Stop()
    $listener.Close()
  }
}
