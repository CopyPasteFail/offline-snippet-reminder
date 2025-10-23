@ECHO OFF

SET DIR=%~dp0
IF "%DIR%" == "" SET DIR=.
SET APP_BASE_NAME=%~n0
SET APP_HOME=%DIR%

IF EXIST "%JAVA_HOME%\bin\java.exe" (
    SET JAVA_CMD="%JAVA_HOME%\bin\java.exe"
) ELSE (
    SET JAVA_CMD=java
)

IF NOT EXIST "%JAVA_CMD%" (
    ECHO ERROR: JAVA_HOME is not set and no 'java' command could be found in your PATH.
    EXIT /B 1
)

SET CLASSPATH=%APP_HOME%\gradle\wrapper\gradle-wrapper.jar

"%JAVA_CMD%" %GRADLE_OPTS% -Dorg.gradle.appname=%APP_BASE_NAME% -classpath "%CLASSPATH%" org.gradle.wrapper.GradleWrapperMain %*
