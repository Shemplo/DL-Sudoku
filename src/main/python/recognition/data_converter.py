#-*- coding: utf-8 -*-

import tensorflow as tf
import re

def img2array (path):
    img = tf.keras.preprocessing.image.load_img (path, target_size = [192, 192])
    x = tf.keras.preprocessing.image.img_to_array (img)
    return tf.keras.applications.mobilenet.preprocess_input (x [tf.newaxis, ...])

def template2data (dir_name, blocks, matrix_file, solution_file):
    matrix = []
    with open (dir_name + "/" + matrix_file, "rt") as f:
        for line in f.readlines ():
            matrix.append (re.split ("\\s+", line.strip ()))

    result = []
    for i in range (9):
        row, col = i // 3, i % 3
        block_matrix = [matrix [r][col * 3 : col * 3 + 3] for r in [row * 3, row * 3 + 1, row * 3 + 2]]
        
        result.append ((img2array (dir_name + "/" + blocks [i]), block_matrix))

    return result
