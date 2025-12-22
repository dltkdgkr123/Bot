### Commands

Windows CMD / PowerShell

```text
# Run the application
gradlew clean bootRun

# Run all integration tests
gradlew clean test

# Run tests for a specific chapter
gradlew clean testCh01
gradlew clean testCh02
..
```

▶ WSL / Linux / macOS
```text
# Run the application
./gradlew clean bootRun

# Run all integration tests
./gradlew clean test

# Run tests for a specific chapter
./gradlew clean testCh01
./gradlew clean testCh02
..
```



### Development Setup

style
- settings -> editor -> code style -> scheme -> import XML
```
./style/intellij-java-google-style.xml
ref: https://github.com/google/styleguide/blob/gh-pages/intellij-java-google-style.xml

./.editorconfig
```

VM option

- help -> edit custom vm options -> copy&paste this options
```
-Dfile.encoding=UTF-8
-Dconsole.encoding=UTF-8

--add-exports=jdk.compiler/com.sun.tools.javac.api=ALL-UNNAMED
--add-exports=jdk.compiler/com.sun.tools.javac.code=ALL-UNNAMED
--add-exports=jdk.compiler/com.sun.tools.javac.file=ALL-UNNAMED
--add-exports=jdk.compiler/com.sun.tools.javac.parser=ALL-UNNAMED
--add-exports=jdk.compiler/com.sun.tools.javac.tree=ALL-UNNAMED
--add-exports=jdk.compiler/com.sun.tools.javac.util=ALL-UNNAMED
```

git-ignored

- requires .properties files
```
./src/main/resources/application.properties
./src/main/resources/application-dev.properties
./src/main/resources/application-prod.properties
./TODO.md
```

additional
```
- intelliJ plugins
    - lombok
        - annotation processing at compile-time
    - google-java-format
        - automatic code formatting during development/build
        - requires additional IntelliJ settings (see below)
        - ref: https://github.com/google/google-java-format/blob/master/README.md#intellij-jre-config
```
