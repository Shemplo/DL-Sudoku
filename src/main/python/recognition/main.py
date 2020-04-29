#-*- coding: utf-8 -*-

import fs_crawler as fsc
import data_converter as dc

SUDOKUS_DIR = "sudokus"

def main ():
    template_dirs = fsc.find_templates (SUDOKUS_DIR)
    print ("Templates: " + str (len (template_dirs)))

    data = []
    for dir_name, blocks, matrix, solution in template_dirs:
        xs_and_ys = dc.template2data (dir_name, blocks, matrix, solution)
        data.extend (xs_and_ys)
    print ("Data: " + str (len (data)))

    

if __name__ == '__main__':
    main ()