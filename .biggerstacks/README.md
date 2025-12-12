# BiggerStacks

Allows increasing stack sizes past 64 up to MAXINT/2 (about 1 billion)

<!--## How to build other mods for maven local

1. Create a file in your documents folder and put this in it:

```groovy
task _sourcesJar(type: Jar, dependsOn: classes) {
    classifier = 'sources'
    from sourceSets.main.allSource
}

task _deobfJar(type: Jar) {
    from sourceSets.main.output
    classifier 'deobf'
}

publishing {
    println("Publishing ${project.group}:${project.archivesBaseName}:${project.version}")
    publications {
        mavenJava(MavenPublication) {
            groupId = project.group
            artifactId = project.archivesBaseName
            version = project.version

            artifact jar
            artifact _sourcesJar
            artifact _deobfJar
        }
    }
    repositories {
        maven {
            url "file://" + System.getenv("local_maven")
        }
    }
}
```

2. Find the mod on github and clone it
3. Open the build.gradle of the repo and find the `publishing` task and replace it
   with: `apply from: "${System.getProperty("user.home")}/documents/publish.gradle"`
4. If it doesn't have a publishing block, just put that snippet at the end of the build.gradle
5. Make sure that the `maven-publish` plugin is applied (any repo with a publishing block will have it applied already)
6. Run `cmd /c "gradlew publishToMavenLocal"`, or if you need **Java 8**, run cmd
   /c `gradlew -Dorg.gradle.java.home=C:\PROGRA~1\ECLIPS~1\jdk-8.0.322.6-hotspot publishToMavenLocal"`
7. If you need the path to your java directory, use `DIR /X` to get the directory without spaces (it will have a `~` in
   it)-->

