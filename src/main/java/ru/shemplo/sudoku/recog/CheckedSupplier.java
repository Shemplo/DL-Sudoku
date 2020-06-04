package ru.shemplo.sudoku.recog;


public interface CheckedSupplier <T, E extends Exception> {
    
    T supply () throws E;
    
}
