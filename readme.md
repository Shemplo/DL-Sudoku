## Sudoku solver based on Neural Networks

### Pipeline

1. Define location of sudoku puzzle on given image	
    * **Solution:** train neural network that will find binary classified mask - sudoku area or not sudoku area
    * **Input:** image of 224x224x3px resolution
    * **Output:** image of binary classified mask of 224x224px resolution
    * **Reached accuracy**: 98.7% on validation set
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
     * **Reached accuracy:** 90.2% on validation set

### Repository content

* `src/main/java/**/digits/gen` ([link](https://github.com/Shemplo/DL-Sudoku/tree/master/src/main/java/ru/shemplo/digits/gen)) - supplementary part of this project for generation of different variations of digits (font family, font size, font styles)
* `src/main/java/**/sudoku/gen` ([link](https://github.com/Shemplo/DL-Sudoku/tree/master/src/main/java/ru/shemplo/sudoku/gen)) - supplementary part of this project for generation of different variations of sudoku puzzle images. It takes generater sudoku puzzle matrix (9x9) and variations of digits and then combines them making image
* `src/main/java/**/sudoku/recog` ([link](https://github.com/Shemplo/DL-Sudoku/tree/master/src/main/java/ru/shemplo/sudoku/recog)) - first attempt to cope with task 3 from pipeline using NN framework [Neuroph](http://neuroph.sourceforge.net/) on Java
* `src/main/python/grid_extraction/` ([link](https://github.com/Shemplo/DL-Sudoku/tree/master/src/main/python/grid_extraction)) - solution for task 1 from pipeline
* `src/main/python/recognition` ([link](https://github.com/Shemplo/DL-Sudoku/tree/master/src/main/python/recognition)) - solution for task 3 from pipeline
* `src/main/python/solving` ([link](https://github.com/Shemplo/DL-Sudoku/tree/master/src/main/python/solving)) - solution for task 5 from pipeline
