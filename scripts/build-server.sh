echo "Compilando servidor..."
javac -d build/classes \
    -cp build/classes \
    src/server/*.java --release 17

if [ $? -eq 0 ]; then
    echo "Servidor compilado com sucesso!"
else
    echo "Erro ao compilar servidor!"
    exit 1
fi