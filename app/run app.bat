:: Ensure we are in the correct directory
cd /d "%~dp0"

:: Start the Java application in a separate process
start "CPT Predict App" java -jar .\cptpredict-0.0.1-SNAPSHOT.jar

:: Wait for 10 seconds to give the server time to start
echo Waiting for the application to start...
timeout /t 10 /nobreak

:: Open the default browser to your local address
start http://localhost:8080/settings

echo Application launched!