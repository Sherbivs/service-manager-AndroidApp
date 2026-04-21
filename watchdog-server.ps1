# =============================================================================
#  Service Manager — Server Watchdog (PowerShell)
#  Keeps node server.js alive. Restarts within 10 seconds of a crash.
#  Uses [System.Diagnostics.Process] with CreateNoWindow = $true, which maps
#  to the Win32 CREATE_NO_WINDOW flag — the ONLY reliable way to prevent
#  console windows on Windows.  VBS SW_HIDE is a hint; this is a guarantee.
# =============================================================================

$ErrorActionPreference = 'SilentlyContinue'
$dir     = $PSScriptRoot
$url     = 'http://localhost:3500/api/system'

while ($true) {
    # --- Health check: is the server already responding? --------------------
    $status = 0
    try {
        $r = Invoke-WebRequest -Uri $url -TimeoutSec 3 -UseBasicParsing -ErrorAction Stop
        $status = $r.StatusCode
    } catch {}

    if ($status -ne 200) {
        # --- Launch node.exe with CREATE_NO_WINDOW -------------------------
        $psi = New-Object System.Diagnostics.ProcessStartInfo
        $psi.FileName               = 'node.exe'
        $psi.Arguments              = 'server.js'
        $psi.WorkingDirectory       = $dir
        $psi.CreateNoWindow         = $true   # CREATE_NO_WINDOW flag
        $psi.UseShellExecute        = $false  # Required for CreateNoWindow
        $psi.RedirectStandardOutput = $false
        $psi.RedirectStandardError  = $false

        $proc = [System.Diagnostics.Process]::Start($psi)
        $proc.WaitForExit()

        # node exited — wait 10 seconds then restart
        Start-Sleep -Seconds 10
    } else {
        # Already running — check again in 15 seconds
        Start-Sleep -Seconds 15
    }
}
