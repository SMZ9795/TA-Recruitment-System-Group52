@echo off
echo Compiling...
javac -d bin -sourcepath src src\com\group52\tarecruitment\SwingMain.java
if %errorlevel% neq 0 (
    echo Compilation failed!
    pause
    exit /b %errorlevel%
)
echo Running...
java -cp bin com.group52.tarecruitment.SwingMain
pause
