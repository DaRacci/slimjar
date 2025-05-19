<h1 align="center"><p style="color:orange;">Archive Notice</p></h1>
As I no longer do any jvm related coding I do not maintain this anymore.

See [this fork](https://github.com/CrazyDev05/slimjar) for an maintained fork.

---

<h1 align="center">Slim Jar</h1>
<h3 align="center">Runtime Dependency Management</h3>
  <div align="center">
    <a href="https://github.com/DaRacci/slimjar/">
        <img src="https://img.shields.io/github/license/DaRacci/slimjar">
    </a>
    <a href="https://github.com/DaRacci/slimjar/actions/workflows/gradle.yml">
        <img src="https://github.com/DaRacci/slimjar/actions/workflows/gradle.yml/badge.svg">
    </a>
    <a href="https://plugins.gradle.org/plugin/io.github.slimjar">
        <img src="https://img.shields.io/maven-metadata/v.svg?label=gradle-plugin&metadataUrl=https%3A%2F%2Frepo.racci.dev%2Freleases%2Fdev%2Fracci%2Fslimjar%2Fdev.racci.slimjar.gradle.plugin%2Fmaven-metadata.xml">
    </a>
    <a href="https://repo.racci.dev/releases/dev/racci/releases/slimjar/slimjar/slimjar">
        <img src="https://img.shields.io/maven-metadata/v.svg?label=maven&metadataUrl=https%3A%2F%2Frepo.racci.dev%2Freleases%2Fdev%2Fracci%2Fslimjar%2Fslimjar%2Fmaven-metadata.xml">
    </a>
  </div>

<hr>

<h4>What is SlimJar?</h4>

SlimJar allows you to download and load up dependencies at runtime as an alternative to shading your dependencies. This helps you reduce build output size and share downloaded dependencies between projects at client side. It is built mainly with the gradle eco-system in mind and is easily configurable being an almost a drop-in replacement/add-on to gradle projects.

<h4>Why use SlimJar?</h4>

SlimJar makes the process of switching out jars easier by providing jars that are much lesser in size, all "slimmed" dependencies are already available and do not need to be explicitly moved back to your working directory during an update or change. This can be extremely useful for users who have lower bandwidth connections to push large updates to production or testing environments. It also provides vital features such as package relocation, module isolation, auto configuration generation...etc with the simplicity of minor tweaks in your build file.

<hr>

<h2 align="center">Usage Example</h2>
<h4 align="center">Note: Use the shadowJar task to compile your project</h4>
<br><br>


```java
// this needs to be ran before you reference your dependencies
ApplicationBuilder.appending("MyApplicationName").build()
```
(NOTE: If you have specified relocations and are running in a IDE or any environment that does not use the shadowjar-ed build file, use the `ignoreRelocation` flag while running by using `-DignoreRelocation` in your runner arguments)
*build.gradle.kts*
```kotlin
plugins {
  id("com.github.johnrengelman.shadow") version "7.1.2"
  id("dev.racci.slimjar") version "1.3.2"
}
dependencies {
  implementation slimjar("1.2.9")
  slim("group.id:artifact.id:version")
}

slimJar {
  relocate("a.b.c", "m.n.o")
}
```

(For Kotlin DSL, to use the `slimjar` extension in dependencies block, you will need the following import - `import io.github.slimjar.func.slimjar`)

<br>
<br>
<h2 align="center">Development setup</h2>


```shell
git clone https://github.com/DaRacci/slimjar.git
# or via
gh repo clone DaRacci/slimjar

cd slimjar && ./gradlew test
```
<br>
<br>
<h2 align="center">Releases</h2>

* https://plugins.gradle.org/plugin/dev.racci.slimjar
* https://repo.racci.dev/releases/dev/racci/slimjar/slimjar/1.2.9

Distributed under the MIT licence. See ``LICENSE`` for more information.

<br>
<br>
<h2 align="center">Contributing</h2>

1. Fork it (<https://github.com/DaRacci/slimjar/fork>)
2. Create your feature branch (`git checkout -b feature/abcd`)
3. Commit your changes (`git commit -am 'Added some feature abcd'`)
4. Push to the branch (`git push origin feature/fooBar`)
5. Create a new Pull Request
