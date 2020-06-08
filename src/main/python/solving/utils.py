import torch
from torch import nn, optim
import torch.nn.functional as F

import numpy as np
import pandas as pd
import matplotlib.pyplot as plt


def loadDataset(path, total, shuffle=False, log=True):
	"""Load quizes and solutions as (size, 9, 9) Numpy Array

	Keyword arguments:
	path -- path to *.csv with quizes and solutions 
	total -- load first `total` quizes
	suffle -- shuffle quizes?
	log -- print debugging info?

	Returns:
	(quizes, solutions)
	
	"""
	quizes = np.empty((total, 81), np.float32)
	solutions = np.empty((total, 81), np.int64)

	i=0
	f = open(path)
	for line in f:
		quiz, solution = line.strip().split(",")
		quizes[i] = [int(i) for i in quiz]
		solutions[i] = [int(i) for i in solution]
		i += 1
		if i % (total // 10) == 0: print("loaded", int(i / total * 100), "%")
		if i >= total: break

	quizes = quizes.reshape((-1, 9, 9))
	solutions = solutions.reshape((-1, 9, 9))

	if log: print("quizes\n", quizes.shape, quizes[0])
	if log: print("solutions\n", solutions.shape, solutions[0])

	if shuffle:
		shuf = np.random.permutation(quizes.shape[0])
		if log: print("shuf", shuf.shape, shuf)
		quizes = quizes[shuf]
		solutions = solutions[shuf]

		if log: print("quizes\n", quizes.shape, quizes[0])
		if log: print("solutions\n", solutions.shape, solutions[0])
	return quizes, solutions


def drawBlanksDistribution(quizes, total):
	"""Draws histogram of blanks distribution in quizes"""
	num_zeros = np.empty((total), np.float32)
	zeros_ = np.zeros_like(quizes[0])
	for i in range(num_zeros.shape[0]):
		num_zeros[i] = np.sum(np.equal(zeros_, quizes[i]))

	plt.figure(figsize=(10, 3))
	n, bins, patches = plt.hist(num_zeros, len(np.unique(num_zeros)), width=0.8, density=True)
	plt.xticks(ticks=bins, labels=list(map(int, np.unique(num_zeros))), horizontalalignment='center')
	plt.title("Distribution of blanks in puzzles")
	plt.show()


def plotAccuracy(total_accuracies, known_accuracies, unknown_accuracies, epochs):
	"""Plots accuracy graph by epochs"""
	dat = pd.DataFrame({'a':np.array(total_accuracies), 'kn':np.array(known_accuracies), 'unkn':np.array(unknown_accuracies)})
	dat['epoch'] = np.arange(epochs)+1

	fig, ax = plt.subplots(1, 1, figsize=(10,3))
	ax.plot(dat['epoch'], dat['a'], color='blue', label='Total accuracy')
	ax.plot(dat['epoch'], dat['kn'], color='green', label='Known accuracy')
	ax.plot(dat['epoch'], dat['unkn'], color='red', label='Unknown accuracy')
	ax.legend()
	plt.grid(True)
	plt.xticks(dat['epoch'])
	plt.show()