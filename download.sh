#/bin/sh

FILENAME=scala-library-$1.jar

rm class -rf
git rm bcode -rf
echo $FILENAME > bcode/version &&\
wget http://scala-tools.org/repo-snapshots/org/scala-lang/scala-library/2.8.0-SNAPSHOT/$FILENAME && \
unzip $FILENAME -d class && \
java -cp out/production/scala-disassembled:../treegraph/project/boot/scala-2.8.0.Beta1/lib/scala-library.jar:/usr/lib/jvm/java-6-openjdk/lib/tools.jar RecursiveJavaP && \
git add bcode/* && \
git ci -m "$FILENAME"

rm $FILENAME
