#!/bin/zsh

set -e

echo 'Building the application'
./mvnw clean package

java -jar './target/rag-springai-ollama-api-1.0.0.jar'
