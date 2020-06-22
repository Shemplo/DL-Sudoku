## Sudoku solver based on Neural Networks

### Pipeline

1. Define location of sudoku puzzle on image	
2. Split sudoku area on 9 equal squares (correspondingly to 3x3 sub-matrices of puzzle)
3. Recognize digits and holes in sub-matrices
4. Concatenate results and get fully recognized sudoku matrix
5. Solve recognized sudoku puzzle