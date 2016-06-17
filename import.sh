if [ -z $1 ];
then
	echo "Usage: ./import PROJECT"
else
	if [ -f ./libs/lib.jar ];
	then
		cp ../$1/dist/$1.jar ./libs/
		cd libs/
		jar -xf lib.jar
		jar -xf $1.jar
		rm -r META-INF
		rm *.jar
		cp -r * ../bin/
		jar -cvf lib.jar *
	else
		cp ../$1/dist/$1.jar ./libs/lib.jar
		cd ./bin/
		jar -xf ../libs/lib.jar
	fi
fi
