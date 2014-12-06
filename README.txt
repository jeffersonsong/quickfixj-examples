Enhanced version of quickfixj examples
1. Cleanup design

order match
1. Integrate with Cucumber test cases.
2. Support multi version of Fix protocol
3. Support cancel replace
4. Implement in scala

Install quickfix/J jars into local maven repository

mvn install:install-file -Dfile=quickfixj-core-1.5.3.jar -DgroupId=quickfixj -DartifactId=quickfixj-core -Dversion=1.5.3 -Dpackaging=jar
mvn install:install-file -Dfile=quickfixj-all-1.5.3.jar -DgroupId=quickfixj -DartifactId=quickfixj-all -Dversion=1.5.3 -Dpackaging=jar

mvn install:install-file -Dfile=quickfixj-msg-fix40-1.5.3.jar -DgroupId=quickfixj -DartifactId=quickfixj-msg-fix40 -Dversion=1.5.3 -Dpackaging=jar
mvn install:install-file -Dfile=quickfixj-msg-fix41-1.5.3.jar -DgroupId=quickfixj -DartifactId=quickfixj-msg-fix41 -Dversion=1.5.3 -Dpackaging=jar
mvn install:install-file -Dfile=quickfixj-msg-fix42-1.5.3.jar -DgroupId=quickfixj -DartifactId=quickfixj-msg-fix42 -Dversion=1.5.3 -Dpackaging=jar
mvn install:install-file -Dfile=quickfixj-msg-fix43-1.5.3.jar -DgroupId=quickfixj -DartifactId=quickfixj-msg-fix43 -Dversion=1.5.3 -Dpackaging=jar
mvn install:install-file -Dfile=quickfixj-msg-fix44-1.5.3.jar -DgroupId=quickfixj -DartifactId=quickfixj-msg-fix44 -Dversion=1.5.3 -Dpackaging=jar
mvn install:install-file -Dfile=quickfixj-msg-fix50-1.5.3.jar -DgroupId=quickfixj -DartifactId=quickfixj-msg-fix50 -Dversion=1.5.3 -Dpackaging=jar