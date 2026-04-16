#!/bin/bash
mkdir -p bin
echo "⚙️ Compilation..."
javac -d bin -sourcepath src src/com/eglise/ui/Main.java
echo "📦 Création du JAR..."
jar cfe EgliseManager.jar com.eglise.ui.Main -C bin .
echo "🚀 Terminé !"
