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
; #define SrcLicensePath "C:\SOURCEDIR\docs\LICENSE.txt"
; #define BaseOutputFilename "doomtools-setup-versionnum"

#include "environment.iss"

[Setup]
; App GUID
AppId={{1932D1F3-180D-4CAD-AD72-8F51B51196C2}

AppName={#DTAppName}
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

ChangesEnvironment=true
Compression=lzma
DisableProgramGroupPage=yes
SolidCompression=yes
WizardStyle=modern

[Languages]
Name: "english"; MessagesFile: "compiler:Default.isl"

[Tasks]
Name: "desktopicon"; Description: "{cm:CreateDesktopIcon}"; GroupDescription: "{cm:AdditionalIcons}"; Flags: unchecked
Name: "envPath"; Description: "Add DoomTools to PATH variable (for Command Line tools)"

[Files]
; NOTE: Don't use "Flags: ignoreversion" on any shared system files
Source: "{#SrcDirectory}\{#DTAppExeName}"; DestDir: "{app}"; Flags: ignoreversion
Source: "{#SrcDirectory}\*"; DestDir: "{app}"; Flags: ignoreversion recursesubdirs createallsubdirs

[Icons]
Name: "{group}\{#DTAppName}"; Filename: "{app}\{#DTAppExeName}"
Name: "{group}\{cm:UninstallProgram,{#DTAppName}}"; Filename: {uninstallexe}
Name: "{autodesktop}\{#DTAppName}"; Filename: "{app}\{#DTAppExeName}"; Tasks: desktopicon

[Run]
Filename: "{app}\{#DTAppExeName}"; Description: "{cm:LaunchProgram,{#StringChange(DTAppName, '&', '&&')}}"; Flags: nowait postinstall skipifsilent

; The following is responsible for listening for the install steps to inject PATH changing, if selected.
[Code]
procedure CurStepChanged(CurStep: TSetupStep);
begin
    if (CurStep = ssPostInstall) and WizardIsTaskSelected('envPath')
    then EnvAddPath(ExpandConstant('{app}'));
end;

procedure CurUninstallStepChanged(CurUninstallStep: TUninstallStep);
begin
    if (CurUninstallStep = usPostUninstall)
    then EnvRemovePath(ExpandConstant('{app}'));
end;
