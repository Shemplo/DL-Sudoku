#-*- coding: utf-8 -*-

import os
import re

from functools import reduce

__block_img_pattern = "block-[0-8]\\.png"
__dir_name_pattern = ".*?/\d{4}-\d{2}-\d{2}T\d{2}.*"

__block_img_matcher = lambda value: \
    1 if (re.match (__block_img_pattern, value)) else 0

__solution_matcher = lambda value: \
    1 if (re.match ("solution\\.txt", value)) else 0

__matrix_matcher = lambda value: \
    1 if (re.match ("matrix\\.txt", value)) else 0

def __files_counter (dir_files, pattern):
    mask = map (pattern, dir_files)
    return reduce (lambda a, b: a + b, mask)

def __is_template_dir (dir_name, dir_files):
    return re.match (__dir_name_pattern, dir_name) \
        and __files_counter (dir_files, __block_img_matcher) == 9 \
        and __files_counter (dir_files, __solution_matcher) == 1 \
        and __files_counter (dir_files, __matrix_matcher) == 1

def __prepare_templte (dir_name, files):
    blocks = list (filter (__block_img_matcher, files))
    return (dir_name, blocks, "matrix.txt", "solution.txt")

def find_templates (dir):
    return [
        __prepare_templte (dir_name, files) \
        for dir_name, dirs, files in os.walk (dir) \
        if __is_template_dir (dir_name, files)
    ]