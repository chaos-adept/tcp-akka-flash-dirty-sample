protoc --plugin=protoc-gen-as3=%~dp0\protoc-gen-as3.bat -I=%~dp0\..\..\..\akka-server\src\main\proto\ --as3_out=%~dp0\..\..\src\main\actionscript %~dp0\..\..\..\akka-server\src\main\proto\packet.proto

pause