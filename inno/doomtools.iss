; DoomTools Inno Setup Installer Script

#define DTAppName "DoomTools"
#define DTAppPublisher "MTrop"
#define DTAppURL "https://mtrop.github.io/DoomTools/"
#define DTAppSupportURL "https://mtrop.github.io/DoomTools/"
#define DTAppRepoReleaseURL "https://mtrop.github.io/DoomTools/"
#define DTAppExeName "doomtools-gui.exe"

; For notes - defined on compile via /D
; #define DTAppVersion "0000.00.00"
; #define SrcDirectory "C:\SOURCEDIR"
; #define SrcJREDirectory "C:\JREDIR" (optional)
; #define SrcLicensePath "C:\SOURCEDIR\docs\LICENSE.txt"
; #define BaseOutputFilename "doomtools-setup-versionnum"

#include "environment.iss"
#include "shell.iss"

[Setup]
; App GUID
AppId={{1932D1F3-180D-4CAD-AD72-8F51B51196C2}

AppName={#DTAppName}
AppVerName={#DTAppName}
AppVersion={#DTAppVersion}
AppPublisher={#DTAppPublisher}
AppPublisherURL={#DTAppURL}
AppSupportURL={#DTAppSupportURL}
AppUpdatesURL={#DTAppRepoReleaseURL}
DefaultDirName={autopf}\{#DTAppName}
DefaultGroupName={#DTAppName}
LicenseFile={#SrcLicensePath}
OutputBaseFilename={#BaseOutputFilename}
UninstallDisplayIcon={app}\{#DTAppExeName}
UninstallFilesDir={app}\uninst

ArchitecturesInstallIn64BitMode=x64
ChangesEnvironment=true
Compression=lzma
DisableWelcomePage=no
DisableProgramGroupPage=no
SetupIconFile=doomtools-setup.ico
SolidCompression=yes
WizardStyle=modern
WizardImageFile=installer-image.bmp
WizardImageStretch=yes

AppCopyright=(C) 2019-2024 Matt Tropiano


[Languages]
Name: "english"; MessagesFile: "compiler:Default.isl"

[Tasks]
Name: "desktopicon"; Description: "{cm:CreateDesktopIcon}"; GroupDescription: "{cm:AdditionalIcons}"; Flags: unchecked
#ifdef SrcJREDirectory
Name: "embedJRE"; Description: "Embed a minified Java Runtime (REQUIRED if not already installed, 47.7 MB)"; GroupDescription: "May Be Required:"
#endif
Name: "envPath"; Description: "Add DoomTools to System PATH (for using the Command Line tools)"; GroupDescription: "More Actions:"
Name: "addToExplorerShell"; Description: "Add DoomTools Context Commands to Explorer"; GroupDescription: "More Actions:"; Flags: unchecked

[Files]
; NOTE: Don't use "Flags: ignoreversion" on any shared system files
Source: "{#SrcDirectory}\*"; DestDir: "{app}"; Flags: ignoreversion recursesubdirs createallsubdirs
#ifdef SrcJREDirectory
Source: "{#SrcJREDirectory}\*"; DestDir: "{app}\jre"; Flags: ignoreversion recursesubdirs createallsubdirs; Tasks: embedJRE
#endif

[Icons]
Name: "{group}\{#DTAppName}";                       Filename: "{app}\{#DTAppExeName}"
Name: "{group}\{#DTAppName} CMD";                   Filename: "{cmd}"; Parameters: "/k ""{app}\doomtools-shell.cmd"""; WorkingDir: "%USERPROFILE%"
Name: "{group}\{#DTAppName} Documentation";         Filename: "{app}\docs"; Flags: foldershortcut
Name: "{group}\{#DTAppName} Website";               Filename: "https://mtrop.github.io/DoomTools/"
Name: "{group}\Utilities\DECOHack Editor";          Filename: "{app}\{#DTAppExeName}"; Parameters: "decohack"
Name: "{group}\Utilities\DoomMake";                 Filename: "{app}\{#DTAppExeName}"; Parameters: "doommake-open"
Name: "{group}\Utilities\DoomMake Studio";          Filename: "{app}\{#DTAppExeName}"; Parameters: "doommake-studio"
Name: "{group}\Utilities\DoomMake (New Project)";   Filename: "{app}\{#DTAppExeName}"; Parameters: "doommake-new"
Name: "{group}\Utilities\WADTex Editor";            Filename: "{app}\{#DTAppExeName}"; Parameters: "wadtex"
Name: "{group}\Utilities\WadMerge Editor";          Filename: "{app}\{#DTAppExeName}"; Parameters: "wadmerge"
Name: "{group}\Utilities\WadScript Editor";         Filename: "{app}\{#DTAppExeName}"; Parameters: "wadscript"
Name: "{group}\Utilities\WSwAnTbl Editor";          Filename: "{app}\{#DTAppExeName}"; Parameters: "wswantbl"
Name: "{group}\Utilities\WTexList-WTExport";        Filename: "{app}\{#DTAppExeName}"; Parameters: "wtexlist-wtexport"
Name: "{group}\Utilities\WTexScan-WTExport";        Filename: "{app}\{#DTAppExeName}"; Parameters: "wtexscan-wtexport"
Name: "{group}\{cm:UninstallProgram,{#DTAppName}}"; Filename: {uninstallexe}
Name: "{autodesktop}\{#DTAppName}";                 Filename: "{app}\{#DTAppExeName}"; Tasks: desktopicon

[Run]
Filename: "{app}\{#DTAppExeName}"; Description: "{cm:LaunchProgram,{#StringChange(DTAppName, '&', '&&')}}"; Flags: nowait postinstall skipifsilent

[UninstallDelete]
Type: files; Name: "{app}\jar\*.jar"


; The following is responsible for listening for the install steps to inject PATH changing, if selected.
[Code]
procedure CurStepChanged(CurStep: TSetupStep);
var
    appPath: String;
    exePath: String;
begin
    if (CurStep = ssPostInstall) then
    begin
        appPath := ExpandConstant('{app}');

        if WizardIsTaskSelected('envPath') then
        begin
            EnvAddPath(appPath);
        end;

        if WizardIsTaskSelected('addToExplorerShell') then 
        begin
            exePath := appPath + '\{#DTAppExeName}';
            ExplorerAddCommandItem('doommake-new', 'New Doom&Make Project', exePath, exePath + ' doommake-new "%V"');
            ExplorerAddCommandItem('doommake-open', 'Open Doom&Make Project', exePath, exePath + ' doommake-open "%V"');
            ExplorerAddCommandItem('doommake-studio', 'Open in DoomMake &Studio', exePath, exePath + ' doommake-studio "%V"');
        end;
    end;
end;

procedure CurUninstallStepChanged(CurUninstallStep: TUninstallStep);
begin
    if (CurUninstallStep = usPostUninstall) then
    begin
        EnvRemovePath(ExpandConstant('{app}'));
        ExplorerRemoveCommandItem('doommake-new');
        ExplorerRemoveCommandItem('doommake-open');
        ExplorerRemoveCommandItem('doommake-studio');
    end;
end;
