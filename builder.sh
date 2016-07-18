
cd src
if [ -f ../libs/lib.jar ];
then
	javac $@ -cp ../libs/lib.jar -d ../bin/ org/gextractor/*.java || exit 1
else
	javac $@$@  -d ../bin/ org/gextractor/*.java || exit 1
fi
cd ../bin/
jar -cvfm ../dist/gextractor.jar ../META-INF/MANIFEST.MF org
