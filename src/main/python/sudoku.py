"""
@author AREL
https://www.ocf.berkeley.edu/~arel/sudoku/main.html
"""

import time, datetime
import random, copy
import sys

def construct_puzzle_solution():
    while True:
        try:
            puzzle  = [[0]*9 for i in range(9)] # start with blank puzzle
            rows    = [set(range(1,10)) for i in range(9)] # set of available
            columns = [set(range(1,10)) for i in range(9)] #   numbers for each
            squares = [set(range(1,10)) for i in range(9)] #   row, column and square
            for i in range(9):
                for j in range(9):
                    choices = rows[i].intersection(columns[j]).intersection(squares[(i // 3) * 3 + j // 3])
                    choice  = random.choice(list(choices))
        
                    puzzle[i][j] = choice
        
                    rows[i].discard(choice)
                    columns[j].discard(choice)
                    squares[(i // 3) * 3 + j // 3].discard(choice)

            return puzzle
            
        except IndexError:
            pass

def pluck(puzzle, n=0):
    def canBeA(puz, i, j, c):
        i, j = int (i), int (j)
        v = puz[c // 9][c % 9]
        if puz[i][j] == v: return True
        if puz[i][j] in range(1,10): return False
            
        for m in range(9):
            if not (m == c // 9 and j == c % 9) and puz[m][j] == v: return False
            if not (i == c // 9 and m == c % 9) and puz [i][m] == v: return False
            if not ((i // 3) * 3 + m // 3 == c // 9 and (j // 3) * 3 + m % 3 == c % 9) and puz[(i // 3) * 3 + m // 3][(j // 3) * 3 + m % 3] == v:
                return False

        return True

    cells     = set(range(81))
    cellsleft = cells.copy()
    while len(cells) > n and len(cellsleft):
        cell = random.choice(list(cellsleft))
        cellsleft.discard(cell)

        row = col = square = False

        for i in range(9):
            if i != cell/9:
                if canBeA(puzzle, i, cell%9, cell): row = True
            if i != cell%9:
                if canBeA(puzzle, cell/9, i, cell): col = True
            if not (((cell // 9) // 3) * 3 + i // 3 == cell // 9 and ((cell // 9) % 3) * 3 + i % 3 == cell % 9):
                if canBeA(puzzle, ((cell // 9) // 3) * 3 + i // 3, ((cell // 9) % 3) * 3 + i % 3 , cell): square = True

        if row and col and square:
            continue
        else:
            puzzle[cell // 9][cell % 9] = 0
            cells.discard(cell)

    return (puzzle, len(cells))

def run(n = 28, iter=100):
    all_results = {}
    print ("Constructing a sudoku puzzle.")
    print ("* creating the solution...")
    a_puzzle_solution = construct_puzzle_solution()

    print ("* constructing a puzzle...")
    for i in range(iter):
        puzzle = copy.deepcopy(a_puzzle_solution)
        (result, number_of_cells) = pluck (puzzle, n)
        all_results.setdefault (number_of_cells, []).append(result)
        if number_of_cells <= n: break

    return (all_results, a_puzzle_solution)

def best(set_of_puzzles):
    return set_of_puzzles[min(set_of_puzzles.keys())][0]

def display(puzzle):
    for row in puzzle:
        print (' '.join([str(n or '_') for n in row]))

(results, a_puzzle_solution) = run(n = int (sys.argv [1]))
puzzle  = best (results)
display (puzzle)

filename = "sudokus/" + datetime.datetime.now ().isoformat ().replace (":", "-").replace (".", "-") + ".txt"

with open (filename, "w+t") as f:
    for row in a_puzzle_solution:
        f.write (' '.join ([str (n or '_') for n in row]))
        f.write ('\n')

    f.write ('\n')

    for row in puzzle:
        f.write (' '.join ([str (n or '_') for n in row]))
        f.write ('\n')