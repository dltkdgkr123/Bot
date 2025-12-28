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

 WSL / Linux / macOS
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


### Development Setup - Eessential


```text
# These files are git-ignored. You must copy them from the `.example` templates.
./src/main/resources/application.properties
./src/main/resources/application-dev.properties
./src/main/resources/application-prod.properties
./TODO.md

# Copy configuration files (Excluding prod)
cp ./src/main/resources/application.properties.example ./src/main/resources/application.properties
cp ./src/main/resources/application-dev.properties.example ./src/main/resources/application-dev.properties
```


```text
# Build the CLI-Java application into a .jar file. This is required to run the .bat scripts in the CLI module.
cd attacker-java/cli/attack
./gradlew clean jar
```


### Development Setup - Additional


style
- settings -> editor -> code style -> scheme -> import XML
```text
./style/intellij-java-google-style.xml
ref: https://github.com/google/styleguide/blob/gh-pages/intellij-java-google-style.xml

./.editorconfig
```

VM option

- help -> edit custom vm options -> copy&paste this options
```text
-Dfile.encoding=UTF-8
-Dconsole.encoding=UTF-8

--add-exports=jdk.compiler/com.sun.tools.javac.api=ALL-UNNAMED
--add-exports=jdk.compiler/com.sun.tools.javac.code=ALL-UNNAMED
--add-exports=jdk.compiler/com.sun.tools.javac.file=ALL-UNNAMED
--add-exports=jdk.compiler/com.sun.tools.javac.parser=ALL-UNNAMED
--add-exports=jdk.compiler/com.sun.tools.javac.tree=ALL-UNNAMED
--add-exports=jdk.compiler/com.sun.tools.javac.util=ALL-UNNAMED
```

intelliJ plugins

```text
- lombok
    - annotation processing at compile-time
- google-java-format
    - automatic code formatting during development/build
    - requires additional IntelliJ settings (see below)
    - ref: https://github.com/google/google-java-format/blob/master/README.md#intellij-jre-config
```
