from collections import Counter

import torch
from skorch import NeuralNetBinaryClassifier
import pandas as pd
from imblearn.over_sampling import SMOTE
from sklearn.feature_extraction.text import TfidfVectorizer
from sklearn.model_selection import StratifiedKFold, train_test_split, GridSearchCV
from sklearn.model_selection import cross_val_score
from sklearn.neural_network import MLPClassifier
import numpy as np
from sklearn.preprocessing import LabelEncoder
from torch import seed, nn

from main import toDataFrame

# load dataset
file = "samate-CI-dataset.csv"
path = "E:/TFM/Trabalho/SoftwareWeaknessesDetection/codigos/CFG_SPOON/dataset/" + file
data = pd.read_csv(path, header=0)
# split into input (X) and output (Y) variables, in numpy arrays
X = data.iloc[:, 3:5].values
Y = data.iloc[:, 5:6].values

# binary encoding of labels
Y = Y.ravel()
encoder = LabelEncoder()
encoder.fit(Y)
Y = encoder.transform(Y)

# ------------- TFIDF process -------------#
X = toDataFrame(X)
# vectorizer = HashingVectorizer(lowercase=False)
vectorizer = TfidfVectorizer(lowercase=False)
# vectorizer = vectorizer.fit(X, y=Y)
# vector = vectorizer.transform(X)
X = vectorizer.fit_transform(X).toarray()

# summarize class distribution
counter = Counter(Y)
print('before SMOTE')
print(counter)

# transform the dataset with SMOTE
oversample = SMOTE()
X, Y = oversample.fit_resample(X, Y)
# summarize the new class distribution
print('after SMOTE')
counter = Counter(Y)
print(counter)

# create model
model = MLPClassifier(hidden_layer_sizes=(60, 60, 60), activation='relu', alpha=0.0001,
                      learning_rate='constant', solver='adam')

X_train, X_validation, Y_train, Y_validation = train_test_split(X, Y, test_size=0.40, random_state=1, stratify=Y)

# evaluate using 10-fold cross validation
#kfold = StratifiedKFold(n_splits=5, shuffle=True, random_state=seed)
kfold = StratifiedKFold(n_splits=10, shuffle=True, random_state=1)
#results = cross_val_score(model, X_train, Y_train, cv=kfold)
results = cross_val_score(model, X_train, Y_train, scoring='accuracy', cv=kfold, n_jobs=-1)
print("mean = %.3f; std = %.3f" % (results.mean(), results.std()))


# model = MLPClassifier()
#
# param_grid = {
#     'hidden_layer_sizes': [(60,60,60), (2,2,2), (50,100,50)],
#     'activation': ['tanh', 'relu'],
#     'solver': ['sgd', 'adam'],
#     'alpha': [0.0001, 0.05],
#     'learning_rate': ['constant','adaptive'],
# }
#
# grid_search = GridSearchCV(model, param_grid, scoring='accuracy', verbose=1, cv=3)
# result = grid_search.fit(X_train, Y_train)
#
# print("Best: %f using %s" % (result.best_score_, result.best_params_))
# means = result.cv_results_['mean_test_score']
# stds = result.cv_results_['std_test_score']
# params = result.cv_results_['params']
# for mean, stdev, param in zip(means, stds, params):
#     print("%f (%f) with: %r" % (mean, stdev, param))