


import operator

import numpy as np

from main import select_features, toDataFrame
from numpy import set_printoptions
# Load libraries
from sklearn.preprocessing import LabelEncoder
from pandas import read_csv
from analysis import data
from pandas.plotting import scatter_matrix
from matplotlib import pyplot
from sklearn.model_selection import train_test_split, KFold
from sklearn.model_selection import cross_val_score
from sklearn.model_selection import StratifiedKFold
from sklearn.metrics import classification_report
from sklearn.metrics import confusion_matrix
from sklearn.metrics import accuracy_score
from sklearn.linear_model import LogisticRegression
from sklearn.tree import DecisionTreeClassifier
from sklearn.neighbors import KNeighborsClassifier
from sklearn.discriminant_analysis import LinearDiscriminantAnalysis
from sklearn.naive_bayes import GaussianNB
from sklearn.svm import SVC
from analysis.metrics import display_pr_curve, print_metrics

from tools.ascii import print_banner, print_notice, print_error
import pickle
# This is a sample Python script.

import os
cwd = os.getcwd()

#dir = "E:/TFM/Trabalho/SoftwareWeaknessesDetection/codigos/SoftwareWeaknessesDetectionML"
dir = cwd #"/SoftwareWeaknessesDetectionML"
#final_model_file = dir+"/pickles/samate_CI_finalized_model.sav"
#vectorizer_file_name = dir+"/pickles/vectorizer-CI.sav"
vectorizer_file_name = dir+"/pickles/vectorizer-NPD.sav"
final_model_file = dir + "/pickles/samate_NPD_finalized_model.sav"

#CI
#file = "test-ci-dataset.csv"
#file = "test-ci-opentsdb-dataset.csv"
#file = "AmazeFileManagerDataset.csv";
#file = "OpenTSDB-Dataset.csv";
#file = "vulnerable-java-application-main.csv"
#file ="java-sec-code.csv"
#file = "simple-spring-project.csv"
#file="injections-main-dataset.csv"
#file="SAMATE-TESTE.csv"


#NPD
#file = "null-pointer-dereference-dataset.csv"
#file = "dbus-java-master-dataset.csv"
file = "selenium-dataset.csv"

#url = "E:/TFM/Trabalho/SoftwareWeaknessesDetection/codigos/CFG_SPOON/dataset/"+file
url = dir+"/dataset/"+file
dataset = read_csv(url, header=0)

array = dataset.values
Xnew = array[:, 3:5]
X = Xnew

vectorizer = pickle.load(open(vectorizer_file_name, 'rb'))
X = toDataFrame(X)
vector = vectorizer.transform(X)
X = vector.toarray()
#Y = array[:, 2:3]
#label_encoder = LabelEncoder()
#Y = label_encoder.fit_transform(Y)

#X = select_features(X, Y, kFeatures, dataset)

#print(X)

# load the model from disk
model = pickle.load(open(final_model_file, 'rb'))
predictions = model.predict(X)
probas = model.predict_proba(X)
orig = dataset
data.store_data(model, orig, X, just_outliers=False)
print("Vulnerable=%s" % (np.sum(predictions == 1)))
print("Not vulnerable=%s" % (np.sum(predictions == 0)))

# show the inputs and predicted outputs
for i in range(len(Xnew)):
     print("X=%s, Predicted=%s, Prob=%s" % (Xnew[i], predictions[i], probas[i]))