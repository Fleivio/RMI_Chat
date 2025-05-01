#!/bin/bash

echo "Empacotando aplicação..."

jar cvf build/jars/ServerChat.jar \
    -C build/classes interfaces \
    -C build/classes server

jar cvf build/jars/UserChat.jar \
    -C build/classes interfaces \
    -C build/classes client

echo "JARs criados em build/jars/"