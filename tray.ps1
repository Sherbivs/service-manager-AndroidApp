# =============================================================================
#  Service Manager — System Tray Icon
#  Polls the dashboard API and shows a green/red status indicator in the
#  Windows notification area.  Double-click opens the dashboard.
# =============================================================================

Add-Type -AssemblyName System.Windows.Forms
Add-Type -AssemblyName System.Drawing

# ---- Prevent duplicate instances ----
$mutex = New-Object System.Threading.Mutex($false, "Global\ServiceManagerTray")
if (-not $mutex.WaitOne(0, $false)) {
    [System.Windows.Forms.MessageBox]::Show(
        "Service Manager tray icon is already running.",
        "Service Manager", "OK", "Information") | Out-Null
    exit
}

# ---- Read port from services.json ----
$configPath = Join-Path $PSScriptRoot "services.json"
$port = 3500
if (Test-Path $configPath) {
    try {
        $cfg = Get-Content $configPath -Raw | ConvertFrom-Json
        if ($cfg.port) { $port = $cfg.port }
    } catch {}
}
$baseUrl = "http://localhost:$port"
$pollMs  = 5000

# ---- Generate coloured circle icons (16 × 16) ----
function New-StatusIcon {
    param(
        [int]$R, [int]$G, [int]$B,
        [int]$GlowR, [int]$GlowG, [int]$GlowB
    )
    $bmp = New-Object System.Drawing.Bitmap(16, 16)
    $gfx = [System.Drawing.Graphics]::FromImage($bmp)
    $gfx.SmoothingMode  = 'AntiAlias'
    $gfx.Clear([System.Drawing.Color]::Transparent)

    # Outer glow
    $glow = New-Object System.Drawing.SolidBrush(
        [System.Drawing.Color]::FromArgb(100, $GlowR, $GlowG, $GlowB))
    $gfx.FillEllipse($glow, 0, 0, 15, 15)
    $glow.Dispose()

    # Inner fill
    $fill = New-Object System.Drawing.SolidBrush(
        [System.Drawing.Color]::FromArgb(255, $R, $G, $B))
    $gfx.FillEllipse($fill, 2, 2, 11, 11)
    $fill.Dispose()

    # Highlight dot
    $hi = New-Object System.Drawing.SolidBrush(
        [System.Drawing.Color]::FromArgb(90, 255, 255, 255))
    $gfx.FillEllipse($hi, 4, 3, 5, 4)
    $hi.Dispose()

    $gfx.Dispose()
    $icon = [System.Drawing.Icon]::FromHandle($bmp.GetHicon())
    return $icon
}

$iconGreen  = New-StatusIcon -R 34 -G 197 -B 94  -GlowR 34 -GlowG 197 -GlowB 94
$iconRed    = New-StatusIcon -R 239 -G 68 -B 68  -GlowR 239 -GlowG 68 -GlowB 68
$iconYellow = New-StatusIcon -R 245 -G 158 -B 11 -GlowR 245 -GlowG 158 -GlowB 11

# ---- NotifyIcon ----
$tray = New-Object System.Windows.Forms.NotifyIcon
$tray.Icon    = $iconRed
$tray.Text    = "Service Manager - Checking..."
$tray.Visible = $true

# ---- Context Menu ----
$menu = New-Object System.Windows.Forms.ContextMenuStrip

$itemOpen = $menu.Items.Add("Open Dashboard")
$itemOpen.Font = New-Object System.Drawing.Font($itemOpen.Font, [System.Drawing.FontStyle]::Bold)
$itemOpen.Add_Click({ Start-Process $baseUrl })

$menu.Items.Add("-") | Out-Null

$itemStartAll = $menu.Items.Add("Start All Services")
$itemStartAll.Add_Click({
    try {
        $resp = Invoke-WebRequest -Uri "$baseUrl/api/services" -TimeoutSec 3 -UseBasicParsing
        $svcs = $resp.Content | ConvertFrom-Json
        foreach ($s in $svcs) {
            if ($s.status -eq "stopped") {
                Invoke-WebRequest -Uri "$baseUrl/api/services/$($s.id)/start" -Method POST -TimeoutSec 5 -UseBasicParsing | Out-Null
            }
        }
    } catch {
        $tray.ShowBalloonTip(3000, "Service Manager", "Cannot reach Service Manager", [System.Windows.Forms.ToolTipIcon]::Error)
    }
})

$itemStopAll = $menu.Items.Add("Stop All Services")
$itemStopAll.Add_Click({
    try {
        $resp = Invoke-WebRequest -Uri "$baseUrl/api/services" -TimeoutSec 3 -UseBasicParsing
        $svcs = $resp.Content | ConvertFrom-Json
        foreach ($s in $svcs) {
            if ($s.status -ne "stopped") {
                Invoke-WebRequest -Uri "$baseUrl/api/services/$($s.id)/stop" -Method POST -TimeoutSec 5 -UseBasicParsing | Out-Null
            }
        }
    } catch {
        $tray.ShowBalloonTip(3000, "Service Manager", "Cannot reach Service Manager", [System.Windows.Forms.ToolTipIcon]::Error)
    }
})

$menu.Items.Add("-") | Out-Null

$itemExit = $menu.Items.Add("Exit Tray Icon")
$itemExit.Add_Click({
    $timer.Stop()
    $tray.Visible = $false
    $tray.Dispose()
    [System.Windows.Forms.Application]::Exit()
})

$tray.ContextMenuStrip = $menu
$tray.Add_DoubleClick({ Start-Process $baseUrl })

# ---- Health Poller ----
$script:prevOnline = $null

function Update-Status {
    try {
        $resp = Invoke-WebRequest -Uri "$baseUrl/api/services" -TimeoutSec 3 -UseBasicParsing -ErrorAction Stop
        $svcs = $resp.Content | ConvertFrom-Json
        $total   = @($svcs).Count
        $running = @($svcs | Where-Object { $_.status -eq "running" }).Count
        $starting = @($svcs | Where-Object { $_.status -eq "starting" }).Count

        if ($running -eq $total -and $total -gt 0) {
            $tray.Icon = $iconGreen
        } elseif ($running -gt 0 -or $starting -gt 0) {
            $tray.Icon = $iconYellow
        } else {
            $tray.Icon = $iconGreen   # Server online but no services running
        }

        $tray.Text = "Service Manager - $running/$total running"

        if ($script:prevOnline -eq $false) {
            $tray.ShowBalloonTip(3000, "Service Manager",
                "Connected - $running/$total services running",
                [System.Windows.Forms.ToolTipIcon]::Info)
        }
        $script:prevOnline = $true
    }
    catch {
        $tray.Icon = $iconRed
        $tray.Text = "Service Manager - Offline"

        if ($script:prevOnline -eq $true) {
            $tray.ShowBalloonTip(3000, "Service Manager",
                "Connection lost",
                [System.Windows.Forms.ToolTipIcon]::Warning)
        }
        $script:prevOnline = $false
    }
}

# ---- Timer ----
$timer = New-Object System.Windows.Forms.Timer
$timer.Interval = $pollMs
$timer.Add_Tick({ Update-Status })
$timer.Start()

# Initial check
Update-Status

# ---- Message Loop (blocks until Exit) ----
[System.Windows.Forms.Application]::Run()

# ---- Cleanup ----
$mutex.ReleaseMutex()
$mutex.Dispose()
