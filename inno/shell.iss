[Code]
{ Goes in HKEY_CLASSES_ROOT }
const DirectoryShellKeyPrefix = 'Directory\shell\';
const DirectoryBackgroundShellKeyPrefix = 'Directory\Background\shell\';
const DefaultValueName = '';
const IconValueName = 'Icon';

procedure ExplorerAddCommandItem(KeyName: string; LabelText: string; IconPath: string; Command: string);
var
    DirShellKey: string;
    DirShellCommandKey: string;
    DirBGShellKey: string;
    DirBGShellCommandKey: string;
begin
    DirShellKey := DirectoryShellKeyPrefix + KeyName;
    DirShellCommandKey := DirectoryShellKeyPrefix + KeyName + '\command';
    DirBGShellKey := DirectoryBackgroundShellKeyPrefix + KeyName;
    DirBGShellCommandKey := DirectoryBackgroundShellKeyPrefix + KeyName + '\command';

    {Write shell item and icon for Directories}
    if not RegWriteStringValue(HKEY_CLASSES_ROOT, DirShellKey, DefaultValueName, LabelText)
    then Log(Format('Error writing registry key: [%s]', [DirShellKey]));
    if not RegWriteStringValue(HKEY_CLASSES_ROOT, DirShellKey, IconValueName, IconPath)
    then Log(Format('Error writing registry key: [%s]', [DirShellKey]));

    {Write shell item command for Directories}
    if not RegWriteStringValue(HKEY_CLASSES_ROOT, DirShellCommandKey, DefaultValueName, Command)
    then Log(Format('Error writing registry key: [%s]', [DirShellCommandKey]));

    {Write shell item and icon for Directory Backgrounds}
    if not RegWriteStringValue(HKEY_CLASSES_ROOT, DirBGShellKey, DefaultValueName, LabelText)
    then Log(Format('Error writing registry key: [%s]', [DirBGShellKey]));
    if not RegWriteStringValue(HKEY_CLASSES_ROOT, DirBGShellKey, IconValueName, IconPath)
    then Log(Format('Error writing registry key: [%s]', [DirBGShellKey]));

    {Write shell item command for Directory Backgrounds}
    if not RegWriteStringValue(HKEY_CLASSES_ROOT, DirBGShellCommandKey, DefaultValueName, Command)
    then Log(Format('Error writing registry key: [%s]', [DirBGShellCommandKey]));
end;

procedure ExplorerRemoveCommandItem(KeyName: string);
var
    DirShellKey: string;
    DirBGShellKey: string;
begin
    DirShellKey := DirectoryShellKeyPrefix + KeyName;
    DirBGShellKey := DirectoryBackgroundShellKeyPrefix + KeyName;

    {Remove shell item and icon for Directories}
    if not RegDeleteKeyIncludingSubkeys(HKEY_CLASSES_ROOT, DirShellKey)
    then Log(Format('Error removing registry key: [%s]', [DirShellKey]));
    
    {Remove shell item and icon for Directory Backgrounds}
    if not RegDeleteKeyIncludingSubkeys(HKEY_CLASSES_ROOT, DirBGShellKey)
    then Log(Format('Error removing registry key: [%s]', [DirBGShellKey]));
end;
