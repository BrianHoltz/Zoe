Zoe
===

Zoe: organisms hunt, fight, mate, and reproduce 
under the control of programs that mutate and evolve.

## Building

To build the project from source:

```bash
./build.sh
```

This will compile all Java source files and create `Zoe.jar`.

Alternatively, you can build manually:

```bash
mkdir -p bin
javac -d bin -sourcepath src $(find src -name "*.java")
jar cfm Zoe.jar src/META-INF/MANIFEST.MF -C bin .
```

## Running

Run the application using:

```bash
java -classpath Zoe.jar org.holtz.zoe.zoeswing.ZoeFrame
```

Or if you prefer the applet version:

```bash
java -classpath Zoe.jar org.holtz.zoe.zoeswing.ZoeApplet
```

## Requirements

- Java JDK 8 or higher
