import torch
from torch import nn, optim
import torch.nn.functional as F

import numpy as np
import pandas as pd
import matplotlib.pyplot as plt

import utils


class ConvNet(nn.Module):
	def __init__(self, c_in, c_out, filt=3, stride=1, padding=1):
		super(ConvNet, self).__init__()

		self.conv = nn.Conv2d(c_in, c_out, (filt, filt), stride=stride, padding=padding)
		self.bn = nn.BatchNorm2d(c_out)
	
	def forward(self, x):
		n, c, h, w = x.shape
		z = F.relu(self.bn(self.conv(x)))
		return z


class Res2Net(nn.Module):
	def __init__(self, c_in, c_out, filt=3, stride=1, padding=1):
		super(Res2Net, self).__init__()

		self.conv1 = nn.Conv2d(c_in, c_out, (filt, filt), stride=stride, padding=padding)
		self.bn1 = nn.BatchNorm2d(c_out)
		self.conv2 = nn.Conv2d(c_in, c_out, (5, 5), stride=stride, padding=2)
		self.bn2 = nn.BatchNorm2d(c_out)
	
	def forward(self, x):
		n, c, h, w = x.shape
		z1 = F.relu(self.bn1(self.conv1(x)))
		z2 = F.relu(self.bn2(self.conv2(z1)) + x)
		return z2


class SudokuNet(nn.Module):
	def __init__(self, n, c_mid, c_in=1, c_out=10):
		super(SudokuNet, self).__init__()
		self.conv1 = ConvNet(c_in, c_mid)

		self.seq = nn.Sequential()
		for i in range(n):
			self.seq.add_module(str(i), Res2Net(c_mid, c_mid, 3, 1, 1))

		self.convLast = nn.Conv2d(c_mid, c_out, (3,3), stride=1, padding=1)
		self.logsoftmax = nn.LogSoftmax(dim=1)

	def forward(self, x):
		z = self.conv1(x)
		z = self.seq(z)
		z = self.logsoftmax(self.convLast(z))
		return z


class SudokuSolver():
	def __init__(self, n, c_mid):
		self.model = SudokuNet(n=n, c_mid=c_mid)
		if torch.cuda.is_available():
			print("cuda")
			model.cuda()
		else:
			print("cpu")
		
		self.opt = optim.Adam(self.model.parameters())
		self.loss = nn.NLLLoss()


	def train_epoch(self, quizes, solutions, batch_size, start, end):
		self.model.train(True)

		for i in range(start, end, batch_size):
			x = quizes[i:i+batch_size]
			y = solutions[i:i+batch_size]
			x = x[:, np.newaxis]
			
			X, y = torch.from_numpy(x), torch.from_numpy(y)

			if torch.cuda.is_available():
				X = X.cuda()
				y = y.cuda()

			self.opt.zero_grad()
			y_ = self.model(X)
			loss_value = self.loss(y_, y)
			loss_value.backward()
			self.opt.step()


	def get_accuracy(self, quizes, solutions, total, batch_size, start, end, iterative=False):
		self.model.train(mode=False)

		total_samples = 0
		total_correct = 0
		known_samples = 0
		known_correct = 0
		zeros_count = 0

		num_zeros = np.empty((total), np.float32)
		zeros_ = np.zeros_like(quizes[0])
		for i in range(quizes.shape[0]):
			num_zeros[i] = np.sum(np.equal(zeros_, quizes[i]))


		for i in range(start, end, batch_size):
			x = quizes[i:i+batch_size]
			y = solutions[i:i+batch_size]
			x = x[:, np.newaxis]

			X, y = torch.from_numpy(x), torch.from_numpy(y)
			zeros_ = torch.zeros_like(y)

			if torch.cuda.is_available():
				X = X.cuda()
				y = y.cuda()
				x_ = torch.from_numpy(quizes[i:i+batch_size]).cuda()
				zeros_ = zeros_.cuda()
			else:
				X = X
				y = y
				x_ = torch.from_numpy(quizes[i:i+batch_size])
				zeros_ = zeros_

			if iterative:
				tries = np.sum(num_zeros[i:i+batch_size])
				y_ = torch.where(torch.eq(x_, 0), torch.full_like(x_, -1), x_).clone().detach()
				y__ = X.clone().detach()
				for _ in range(int(tries)):
					values, indices = torch.max(self.model(y__), axis=1)
					values = torch.where(torch.eq(y_, -1), values, torch.full_like(values, -1))
					
					vals1, inds1 = torch.max(values, 1)
					vals2, inds2 = torch.max(vals1, 1)
					
					y_[0, inds1[0, inds2[0]], inds2[0]] = indices[0, inds1[0, inds2[0]], inds2[0]].item()
					
					y__[0,0,inds1[0, inds2[0]], inds2[0]] = indices[0, inds1[0, inds2[0]], inds2[0]].item()
			else:
				y_ = self.model(X)
				_, y_ = torch.max(y_, axis=1)
			
			   
			total_samples += 81 * batch_size
			sum_ = torch.sum(torch.eq(y_, y).type(torch.FloatTensor))
			total_correct += sum_.item()
			
			known_samp_ = 81 * batch_size - np.sum(num_zeros[i:i+batch_size])
			known_samples += known_samp_
			known_sum_ = torch.sum(torch.eq(y_, x_).type(torch.FloatTensor))
			known_correct += known_sum_.item()
			zeros_count += torch.sum(torch.eq(y_, zeros_).type(torch.FloatTensor)).item()

			print(f'batch: {i}, total_correct: [{int(sum_.item())}/{81*batch_size}], known_correct: [{int(known_sum_.item())}/{int(known_samp_)}], unknown_correct: [{int(sum_.item()) - int(known_sum_.item())}/{int(81*batch_size - known_samp_)}], zeros in answer: {int(zeros_count)}')

		return total_correct / total_samples, known_correct / known_samples, (total_correct - known_correct) / (total_samples - known_samples), zeros_count


	def train_model(self, quizes, solutions, epochs, total, perc, batch_size, test_batch_size, path):
		"""Train model. Split total into train : dev = (1 - perc) : perc"""
		total_accuracies = []
		known_accuracies = []
		unknown_accuracies = []
		zeros_count = []
		for epoch in range(epochs):
			
			self.train_epoch(quizes, solutions, batch_size=batch_size, start=total*perc//100, end=total)
			acc, kn, unkn, zc = self.get_accuracy(quizes, solutions, total, batch_size=test_batch_size, start=0, end=total*perc//100)
			total_accuracies.append(acc)
			known_accuracies.append(kn)
			unknown_accuracies.append(unkn)
			zeros_count.append(zc)
			print(f'epoch: [{epoch+1}/{epochs}], accuracy: {acc}, known_accuracy: {kn}, unknown_accuracy: {unkn}, zeros_count: {zc}')

			torch.save({
				'model_state_dict': self.model.state_dict(),
				'optimizer_state_dict': self.opt.state_dict(),
				'total_accuracies': total_accuracies,
				'known_accuracies': known_accuracies,
				'unknown_accuracies': unknown_accuracies,
				'zeros_count': zeros_count,
				'epochs': epochs}, path)

		return total_accuracies, known_accuracies, unknown_accuracies, zeros_count