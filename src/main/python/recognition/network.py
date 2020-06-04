#-*- coding: utf-8 -*-

import multiprocessing
import numpy as np


__flatten = lambda l: [item for sublist in l for item in sublist]

def getRandomBatch (data, size : int, split = False):
    #print (str (size) + " / " + str (len (data)))
    choice = np.random.choice (len (data), size)
    #print (len (choice))
    #print (data [choice [0]][0][0])
    X = [[[[(col [0] + col [1] + col [2]) / 3] for col in row] for row in data [choice [i]][0][0]] for i in range (size)]
    y = [__flatten (data [choice [i]][1]) for i in range (size)]
    #print (len(y))
    if split:
        choosen = set (choice)
        #print ("Choosen: " + str (choosen) + " / " + str (len (choosen)))
        rest_data = [data [i] for i in range (len (data)) if i not in choosen]
        return X, y, rest_data
    else:
        return X, y