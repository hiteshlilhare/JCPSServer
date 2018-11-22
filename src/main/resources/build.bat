@echo off
cls
set cap_file_name=<name-of-cap-file>
set ant_build_xml_file=<name-of-build-xml-file>
set build_dir_relative_path=<relative-path-of-build-dir-wrt-app-dir>
set current_dir=%cd%

REM echo %current_dir%
for %%I in (.) do set "tag_name=%%~nxI"
REM set tag_name=%tag_name:~0,-6%
set clone_dir=%tag_name%.Clone
REM echo %tag_name%
for %%I in (..) do set "repo_name=%%~nxI"
REM echo %repo_name%
for %%I in (../..) do set "github_uid=%%~nxI"
REM echo %github_uid%
set repo_path=..\..\..\..\appsource\%github_uid%\%repo_name%\%clone_dir%
cd %repo_path%
call ant -f %ant_build_xml_file% build

set final_path=..\..\..\..\appstore\%github_uid%\%repo_name%\%tag_name%
REM echo %final_path%
if exist %build_dir_relative_path%\%cap_file_name% (
if exist %final_path%\%cap_file_name% del %final_path%\%cap_file_name%
xcopy %build_dir_relative_path%\%cap_file_name% %final_path%
)
cd %current_dir%
