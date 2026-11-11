$Root = Split-Path -Parent (Split-Path -Parent $MyInvocation.MyCommand.Path)
& (Join-Path $Root 'xidew.bat') @args
exit $LASTEXITCODE
