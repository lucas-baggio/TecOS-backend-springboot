#!/bin/bash

check_and_kill_port() {
    PORT=8080
    PID=$(lsof -t -i :$PORT)
    if [ -n "$PID" ]; then
        echo "Porta $PORT está em uso pelo PID $PID. Encerrando processo..."
        kill -9 $PID
        sleep 1
        if lsof -t -i :$PORT > /dev/null; then
            echo "Erro: Não foi possível encerrar o processo na porta $PORT."
            exit 1
        else
            echo "Processo na porta $PORT encerrado com sucesso."
        fi
    else
        echo "Porta $PORT está livre."
    fi
}

check_and_kill_port

set -a
source .env
set +a

mvn spring-boot:run
