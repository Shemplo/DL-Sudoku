#!/bin/bash

if [[ -z "$1" ]]; then
    echo "[ERROR] One argument is required: number of templates"
    exit 1
fi

for ((i=0; i < $1; i++)) do
    python3 src/main/python/sudoku.py 50
done