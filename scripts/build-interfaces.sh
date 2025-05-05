echo "Compilando interfaces..."
javac -d build/classes src/interfaces/*.java --release 17

if [ $? -eq 0 ]; then
    echo "Interfaces compiladas com sucesso!"
else
    echo "Erro ao compilar interfaces!"
    exit 1
fi