
cd src
if [ -f ../libs/lib.jar ];
then
	javac -cp ../libs/lib.jar -d ../bin/ org/gextractor/*.java
else
	javac -d ../bin/ org/gextractor/*.java
fi
cd ../bin/
jar -cvfm ../dist/gextractor.jar ../META-INF/MANIFEST.MF org
