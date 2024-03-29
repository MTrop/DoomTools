; Note from MTrop - the "path environment" code was shamelessly taken (and also modified) from a pertinent answer from Stack Overflow
; https://stackoverflow.com/questions/3304463/how-do-i-modify-the-path-environment-variable-when-running-an-inno-setup-install
; Thanks, Wojciech Mleczek!

[Code]
const EnvironmentKey = 'SYSTEM\CurrentControlSet\Control\Session Manager\Environment';

procedure EnvAddPath(Path: string);
var
    Paths: string;
    PathEntry: string;
    P: Integer;
begin
    { Retrieve current path (use empty string if entry not exists) }
    if not RegQueryStringValue(HKEY_LOCAL_MACHINE, EnvironmentKey, 'Path', Paths)
    then Paths := '';

    { Skip if string already found in path }
	PathEntry := Uppercase(Path) + ';';
	P := Pos(PathEntry, Uppercase(Paths));
    if P > 0 then exit;

	{ If Paths ends in a path separator, do not add another separator, and append to Paths }
	if Copy(Paths, Length(Paths), 1) = ';'
	then Paths := Paths + Path + ';'
	else Paths := Paths + ';' + Path + ';';

    { Overwrite (or create if missing) path environment variable }
    if RegWriteStringValue(HKEY_LOCAL_MACHINE, EnvironmentKey, 'Path', Paths)
    then Log(Format('The [%s] added to PATH: [%s]', [Path, Paths]))
    else Log(Format('Error while adding the [%s] to PATH: [%s]', [Path, Paths]));
end;

procedure EnvRemovePath(Path: string);
var
    Paths: string;
    PathEntry: string;
    P: Integer;
begin
    { Skip if registry entry not exists }
    if not RegQueryStringValue(HKEY_LOCAL_MACHINE, EnvironmentKey, 'Path', Paths) then
        exit;

    { Skip if string not found in path }
	PathEntry := Uppercase(Path) + ';';
    P := Pos(PathEntry, Uppercase(Paths));
    if P = 0 then exit;

    { Update path variable }
    Delete(Paths, P, Length(PathEntry));

    { Overwrite path environment variable }
    if RegWriteStringValue(HKEY_LOCAL_MACHINE, EnvironmentKey, 'Path', Paths)
    then Log(Format('The [%s] removed from PATH: [%s]', [Path, Paths]))
    else Log(Format('Error while removing the [%s] from PATH: [%s]', [Path, Paths]));
end;
