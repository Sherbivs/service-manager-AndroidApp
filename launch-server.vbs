' =============================================================================
'  Service Manager — Silent Server Launcher
'  Starts server.js with no visible console window.
'  If already running, does nothing.  Called by Windows Startup shortcut.
'  Usage:  wscript.exe launch-server.vbs
' =============================================================================
Set objShell = CreateObject("WScript.Shell")

strDir = Replace(WScript.ScriptFullName, WScript.ScriptName, "")
strURL = "http://localhost:3500"

' ---- Check if already running ----
On Error Resume Next
Set objHTTP = CreateObject("MSXML2.ServerXMLHTTP.6.0")
objHTTP.SetTimeouts 1000, 1000, 1000, 1000
objHTTP.Open "GET", strURL & "/api/system", False
objHTTP.Send
intStatus = objHTTP.Status
Set objHTTP = Nothing
On Error GoTo 0

' ---- Start silently if not running ----
If intStatus <> 200 Then
    ' Window style 0 = hidden; False = don't wait for exit
    objShell.Run "cmd /c cd /d """ & strDir & """ && node.exe server.js", 0, False
End If
