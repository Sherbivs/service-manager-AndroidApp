$repos = @("c:\Users\sherb\OneDrive\Documents\github\service-manager", "c:\Users\sherb\StudioProjects\service-manager-AndroidApp")
$targets = @("ROUTER.md", "AGENTS.md", "copilot-instructions.md", "Patch.md")

$variants = @{
    ROOT_ROUTER = @{ Tips="- Use root router for system-wide navigation."; Next="1. Validate all subsystem connections."; Trouble="- Root path loops." }
    OPS_ROUTER = @{ Tips="- Check operation logs daily."; Next="1. Run health check scripts."; Trouble="- Permission denied errors." }
    DOCS_ROUTER = @{ Tips="- Keep documentation metadata sync'd."; Next="1. Audit documentation coverage."; Trouble="- Broken internal links." }
    DOCS_ARCHIVE_ROUTER = @{ Tips="- Archive older versions securely."; Next="1. Verify checksums of archived files."; Trouble="- Files manually moved." }
    GITHUB_ROUTER = @{ Tips="- Follow workflow automation rules."; Next="1. Review CI/CD pipeline results."; Trouble="- Action secrets missing." }
    SCRIPTS_ROUTER = @{ Tips="- Use idempotent scripts only."; Next="1. Test scripts in staging."; Trouble="- Environment variable mismatch." }
    PUBLIC_ROUTER = @{ Tips="- Sanitize all public assets."; Next="1. Clear CDN cache after updates."; Trouble="- Assets not loading over HTTPS." }
    ANDROID_APP_ROUTER = @{ Tips="- Follow Android lifecycle events."; Next="1. Review memory leak reports."; Trouble="- UI thread blocking." }
    ANDROID_UI_ROUTER = @{ Tips="- Use view binding for safety."; Next="1. Perform layout performance audit."; Trouble="- Fragment state loss." }
    CONTRACT_PATCH = @{ Tips="- Ensure patch atomicity."; Next="1. Verify patch integrity."; Trouble="- Patch conflict on merge." }
    CONTRACT_AGENTS = @{ Tips="- Define clear agent ownership."; Next="1. Validate agent state transitions."; Trouble="- Agent communication timeouts." }
    CONTRACT_COPILOT = @{ Tips="- Focus prompts on clear metadata."; Next="1. Sync instructions with latest API."; Trouble="- Unresponsive suggestion engine." }
    DEFAULT_ROUTER = @{ Tips="- Standard routing practices apply."; Next="1. Document path changes."; Trouble="- Path resolution failures." }
}

function Get-Variant($fullName, $filename) {
    $norm = $fullName.Replace("\", "/")
    if ($filename -eq "Patch.md") { return "CONTRACT_PATCH" }
    if ($filename -eq "AGENTS.md") { return "CONTRACT_AGENTS" }
    if ($filename -eq "copilot-instructions.md") { return "CONTRACT_COPILOT" }
    if ($filename -eq "ROUTER.md") {
        if ($norm -match "/docs/archive/") { return "DOCS_ARCHIVE_ROUTER" }
        if ($norm -match "/docs/") { return "DOCS_ROUTER" }
        if ($norm -match "/ops/") { return "OPS_ROUTER" }
        if ($norm -match "/\.github/") { return "GITHUB_ROUTER" }
        if ($norm -match "/scripts/") { return "SCRIPTS_ROUTER" }
        if ($norm -match "/public/") { return "PUBLIC_ROUTER" }
        if ($norm -match "/app/src/main/java/.*/ui/") { return "ANDROID_UI_ROUTER" }
        if ($norm -match "/app/ROUTER\.md") { return "ANDROID_APP_ROUTER" }
        if ($norm -match "/service-manager/ROUTER\.md$") { return "ROOT_ROUTER" }
        if ($norm -match "/service-manager-AndroidApp/ROUTER\.md$") { return "ROOT_ROUTER" }
        return "DEFAULT_ROUTER"
    }
    return $null
}

foreach ($repo in $repos) {
    if (-not (Test-Path $repo)) { Write-Host "Repo not found: $repo"; continue }
    Write-Host "`nProcessing Repo: $repo"
    $files = Get-ChildItem $repo -Recurse -File | Where-Object { $targets -contains $_.Name }
    foreach ($file in $files) {
        $variantKey = Get-Variant $file.FullName $file.Name
        if (-not $variantKey) { continue }
        $v = $variants[$variantKey]
        $content = Get-Content $file.FullName -Raw
        if ($content -match "## Tips") {
            $content = $content -replace "(?s)## Tips.*", ""
        }
        $content = $content.TrimEnd() + @"

## Tips
$($v.Tips)

## Next Steps
$($v.Next)

## Troubleshooting
$($v.Trouble)
"@
        Set-Content -Path $file.FullName -Value $content -Encoding utf8
        Write-Host "Updated: $($file.FullName) [$variantKey]"
    }
}

foreach ($repo in $repos) {
    if (-not (Test-Path $repo)) { continue }
    $pass = 0; $fail = 0
    Write-Host "`nValidation Repo: $repo"
    $files = Get-ChildItem $repo -Recurse -File | Where-Object { $targets -contains $_.Name }
    foreach ($file in $files) {
        $text = Get-Content $file.FullName -Raw
        if ($text -match "## Tips" -and $text -match "## Next Steps" -and $text -match "## Troubleshooting") {
            $pass++
        } else {
            $fail++; Write-Host "FAIL: $($file.FullName)"
        }
    }
    Write-Host "PASS: $pass | FAIL: $fail"
}
