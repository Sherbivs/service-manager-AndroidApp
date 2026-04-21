' =============================================================================
'  Service Manager — Server Watchdog Launcher (Silent)
'  Thin wrapper: launches watchdog-server.ps1 with SW_HIDE.
'  The PS1 uses .NET ProcessStartInfo.CreateNoWindow ($true) which maps to
'  the Win32 CREATE_NO_WINDOW flag — the only reliable way to hide console
'  windows.  VBS SW_HIDE (intWindowStyle=0) alone is NOT sufficient for
'  console-subsystem apps (node.exe, cmd.exe) due to conhost.exe race.
' =============================================================================
Option Explicit

Dim oShell, strDir, psCmd

Set oShell = CreateObject("WScript.Shell")
strDir = CreateObject("Scripting.FileSystemObject").GetParentFolderName(WScript.ScriptFullName)

psCmd = "powershell.exe -NoProfile -WindowStyle Hidden -ExecutionPolicy Bypass -File """ & strDir & "\watchdog-server.ps1"""

' intWindowStyle=0 (SW_HIDE) + bWaitOnReturn=True (block so Task Scheduler sees it running)
oShell.Run psCmd, 0, True
