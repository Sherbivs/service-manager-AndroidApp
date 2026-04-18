' =============================================================================
'  Service Manager — Silent Tray Launcher
'  Runs tray.ps1 without a visible console window.
'  Usage:  wscript.exe launch-tray.vbs
' =============================================================================
Set objShell = CreateObject("WScript.Shell")
strDir = Replace(WScript.ScriptFullName, WScript.ScriptName, "")
objShell.Run "powershell.exe -ExecutionPolicy Bypass -WindowStyle Hidden -File """ & strDir & "tray.ps1""", 0, False
