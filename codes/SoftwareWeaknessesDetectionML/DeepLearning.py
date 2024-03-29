# pytorch mlp for binary classification
from collections import Counter

from imblearn.over_sampling import SMOTE
from numpy import vstack
from pandas import read_csv
from sklearn.feature_extraction.text import TfidfVectorizer
from sklearn.preprocessing import LabelEncoder
from sklearn.metrics import accuracy_score
from torch.utils.data import Dataset
from torch.utils.data import DataLoader
from torch.utils.data import random_split
from torch import Tensor
from torch.nn import Linear
from torch.nn import ReLU
from torch.nn import Sigmoid
from torch.nn import Module
from torch.optim import SGD
from torch.nn import BCELoss
from torch.nn.init import kaiming_uniform_
from torch.nn.init import xavier_uniform_

from main import toDataFrame


# dataset definition
class CSVDataset(Dataset):
    # load the dataset
    def __init__(self, path):
        # load the csv file as a dataframe
        df = read_csv(path, header=None)
        # store the inputs and outputs
        self.X = df.values[:, :-1]
        self.y = df.values[:, -1]
        # ensure input data is floats
        self.X = self.X.astype('float32')
        # label encode target and ensure the values are floats
        self.y = LabelEncoder().fit_transform(self.y)
        self.y = self.y.astype('float32')
        self.y = self.y.reshape((len(self.y), 1))

    # number of rows in the dataset
    def __len__(self):
        return len(self.X)

    # get a row at an index
    def __getitem__(self, idx):
        return [self.X[idx], self.y[idx]]

    # get indexes for train and test rows
    def get_splits(self, n_test=0.33):
        # determine sizes
        test_size = round(n_test * len(self.X))
        train_size = len(self.X) - test_size
        # calculate the split
        return random_split(self, [train_size, test_size])


class CSVDatasetAdapted(Dataset):
    # load the dataset
    def __init__(self, path):
        # load the csv file as a dataframe
        df = read_csv(path, header=0)
        # store the inputs and outputs
        self.X = df.values[:, 3:5]
        self.Y = df.values[:, 5:6]

        # label encode target and ensure the values are floats
        self.Y = self.Y.ravel()
        self.Y = LabelEncoder().fit_transform(self.Y)
        #self.Y = self.Y.astype('float32')
        #self.Y = self.Y.reshape((len(self.Y), 1))

        self.X = toDataFrame(self.X)
        vectorizer = TfidfVectorizer(lowercase=False)
        self.X = vectorizer.fit_transform(self.X).toarray()

        oversample = SMOTE()
        self.X, self.Y = oversample.fit_resample(self.X, self.Y)

        # ensure input data is floats
        #self.X = self.X.astype('float32')

        #self.y = self.y.astype('float32')
        #self.y = self.y.reshape((len(self.y), 1))

    # number of rows in the dataset
    def __len__(self):
        return len(self.X)

    # get a row at an index
    def __getitem__(self, idx):
        return [self.X[idx], self.Y[idx]]

    # get indexes for train and test rows
    def get_splits(self, n_test=0.33):
        # determine sizes
        test_size = round(n_test * len(self.X))
        train_size = len(self.X) - test_size
        # calculate the split
        return random_split(self, [train_size, test_size])

# model definition
class MLP(Module):
    # define model elements
    def __init__(self, n_inputs):
        super(MLP, self).__init__()
        # input to first hidden layer
        self.hidden1 = Linear(n_inputs, 245000)
        kaiming_uniform_(self.hidden1.weight, nonlinearity='relu')
        self.act1 = ReLU()
        # second hidden layer
        self.hidden2 = Linear(10, 8)
        kaiming_uniform_(self.hidden2.weight, nonlinearity='relu')
        self.act2 = ReLU()
        # third hidden layer and output
        self.hidden3 = Linear(8, 1)
        xavier_uniform_(self.hidden3.weight)
        self.act3 = Sigmoid()

    # forward propagate input
    def forward(self, X):
        # input to first hidden layer
        X = self.hidden1(X)
        X = self.act1(X)
         # second hidden layer
        X = self.hidden2(X)
        X = self.act2(X)
        # third hidden layer and output
        X = self.hidden3(X)
        X = self.act3(X)
        return X

# prepare the dataset
def prepare_data(path):
    # load the dataset
    #dataset = CSVDataset(path)
    dataset = CSVDatasetAdapted(path)
    # calculate split
    train, test = dataset.get_splits()
    # prepare data loaders
    train_dl = DataLoader(train, batch_size=32, shuffle=True)
    test_dl = DataLoader(test, batch_size=1024, shuffle=False)
    return train_dl, test_dl

# train the model
def train_model(train_dl, model):
    # define the optimization
    criterion = BCELoss()
    optimizer = SGD(model.parameters(), lr=0.01, momentum=0.9)
    # enumerate epochs
    for epoch in range(100):
        # enumerate mini batches
        for i, (inputs, targets) in enumerate(train_dl):
            # clear the gradients
            optimizer.zero_grad()
            # compute the model output
            yhat = model(inputs)
            # calculate loss
            loss = criterion(yhat, targets)
            # credit assignment
            loss.backward()
            # update model weights
            optimizer.step()

# evaluate the model
def evaluate_model(test_dl, model):
    predictions, actuals = list(), list()
    for i, (inputs, targets) in enumerate(test_dl):
        # evaluate the model on the test set
        yhat = model(inputs)
        # retrieve numpy array
        yhat = yhat.detach().numpy()
        actual = targets.numpy()
        actual = actual.reshape((len(actual), 1))
        # round to class values
        yhat = yhat.round()
        # store
        predictions.append(yhat)
        actuals.append(actual)
    predictions, actuals = vstack(predictions), vstack(actuals)
    # calculate accuracy
    acc = accuracy_score(actuals, predictions)
    return acc

# make a class prediction for one row of data
def predict(row, model):
    # convert row to data
    row = Tensor([row])
    # make prediction
    yhat = model(row)
    # retrieve numpy array
    yhat = yhat.detach().numpy()
    return yhat

# prepare the data
# file = "samate-CI-dataset.csv"
# url = "E:/TFM/Trabalho/SoftwareWeaknessesDetection/codigos/CFG_SPOON/dataset/"+file
# dataset = read_csv(url, header=0)
# dataset = read_csv(url, header=0)
# dataset.dropna(inplace=True)
# array = dataset.values
# X = array[:, 3:5]
# ## valores de classe
# Y = array[:, 5:6]
# # encode class values as integers
# encoder = LabelEncoder()
# encoder.fit(Y)
# Y = encoder.transform(Y)
#
# # ------------- TFIDF process -------------#
# X = toDataFrame(X)
# # vectorizer = HashingVectorizer(lowercase=False)
# vectorizer = TfidfVectorizer(lowercase=False)
# # vectorizer = vectorizer.fit(X, y=Y)
# # vector = vectorizer.transform(X)
# X = vectorizer.fit_transform(X).toarray()
#
# # summarize class distribution
# counter = Counter(Y)
# print('before SMOTE')
# print(counter)
# # scatter_plot(X, Y, counter)
#
# # transform the dataset with SMOTE
# oversample = SMOTE()
# X, Y = oversample.fit_resample(X, Y)
# # summarize the new class distribution
# print('after SMOTE')
# counter = Counter(Y)
# print(counter)

path = 'https://raw.githubusercontent.com/jbrownlee/Datasets/master/ionosphere.csv'
file = "samate-CI-dataset.csv"
path = "E:/TFM/Trabalho/SoftwareWeaknessesDetection/codigos/CFG_SPOON/dataset/" + file

train_dl, test_dl = prepare_data(path)
print(len(train_dl.dataset), len(test_dl.dataset))
print(train_dl)
print(test_dl)
# define the network
model = MLP(34)
# train the model
train_model(train_dl, model)
# evaluate the model
acc = evaluate_model(test_dl, model)
print('Accuracy: %.3f' % acc)
# make a single prediction (expect class=1)
row = [1,0,0.99539,-0.05889,0.85243,0.02306,0.83398,-0.37708,1,0.03760,0.85243,-0.17755,0.59755,-0.44945,0.60536,-0.38223,0.84356,-0.38542,0.58212,-0.32192,0.56971,-0.29674,0.36946,-0.47357,0.56811,-0.51171,0.41078,-0.46168,0.21266,-0.34090,0.42267,-0.54487,0.18641,-0.45300]
yhat = predict(row, model)
print('Predicted: %.3f (class=%d)' % (yhat, yhat.round()))