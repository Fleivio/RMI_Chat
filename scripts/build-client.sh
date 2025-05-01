#!/bin/bash

# Compila o cliente e suas dependências
echo "Compilando cliente..."
javac -d build/classes \
    -cp build/classes \
    src/client/*.java --release 17

if [ $? -eq 0 ]; then
    echo "Cliente compilado com sucesso!"
else
    echo "Erro ao compilar cliente!"
    exit 1
fi