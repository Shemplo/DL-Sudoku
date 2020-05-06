#-*- coding: utf-8 -*-

import multiprocessing
import numpy as np


__flatten = lambda l: [item for sublist in l for item in sublist]

def getRandomBatch (data, size : int):
    #print (size)
    choice = np.random.choice (len (data), size)
    #print (len (choice))
    #print (data [choice [0]][0][0])
    X = [[[[col [0]] for col in row] for row in data [choice [i]][0][0]] for i in range (size)]
    y = [__flatten (data [choice [i]][1]) for i in range (size)]
    #print (len(y))
    return X, y