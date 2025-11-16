#!/bin/bash
# Build script for Zoe project

set -e

echo "Building Zoe..."

# Create output directory
mkdir -p bin

# Compile all Java source files
echo "Compiling Java sources..."
javac -d bin -sourcepath src $(find src -name "*.java")

# Create JAR file
echo "Creating JAR file..."
jar cfm Zoe.jar src/META-INF/MANIFEST.MF -C bin .

echo "Build complete! JAR file created: Zoe.jar"
echo ""
echo "To run: java -classpath Zoe.jar org.holtz.zoe.zoeswing.ZoeFrame"

