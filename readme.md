## Sudoku solver based on Neural Networks

### Pipeline

1. Define location of sudoku puzzle on given image	
    * **Solution:** train neural network that will find binary classified mask - sudoku area or not sudoku area
    * **Input:** image of 224x224x3px resolution
    * **Output:** image of binari classified mask of 224x224px resolution
    * **Reached accuracy**: **.*%
2. Split sudoku area on 9 equal squares (correspondingly to 3x3 sub-matrices of puzzle) <sub>_(partly implemented)_</sub>
    * According to binary mask 4 corner points can be found and polygon can be fetched
    * Each side of 4-side poligon is splitted on 3 equal parts (due to assumtion that sudoku has simmetric structure)
    * All side parts of polygon are connected with correcponding part on opposite side
    * Intersection of connections assign transformed sub-matrix square
    * For each sub-matrix square apply geometrical transfomatrion to make it straight
3. Recognize digits and holes in sub-matrices
    * **Solution:** train neural network that will recognize sudoku sub-matrices (3x3) and return array of recognized digits or 0 if hole on particular position
    * **Input:** image of 64x64x3px resolution with segmented sudoku sub-matrix
    * **Output:** array (1x9) of recognized digits or 0 if some particular position had no digit
    * **Reached accuracy:** 88.7% on validation set of 300 sub-matrices
4. Concatenate results and get fully recognized sudoku matrix
5. Solve recognized sudoku puzzle
     * **Solution:** train neural network that will solve sudoku and return 2-array of assumed digits
     * **Input:** array (9x9) with digits or 0 if it's a emply cell
     * **Output:** array (9x9) with assumed digits that is a solution of puzzle
     * **Reached accuracy:** 92+%
