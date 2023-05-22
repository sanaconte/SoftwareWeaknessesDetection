


import operator
from main import select_features
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

# Selecting features
kFeatures = 5
dir = "E:/TFM/Trabalho/SoftwareWeaknessesDetection/codigos/SoftwareWeaknessesDetectionML"
final_model_file = dir+"/pickles/samate_CI_finalized_model.sav"
#final_model_file = dir + "/pickles/samate_NPD_finalized_model.sav"

#file = "test-ci-dataset.csv"
file = "test-ci-opentsdb-dataset.csv"
url = "E:/TFM/Trabalho/SoftwareWeaknessesDetection/codigos/CFG_SPOON/dataset/"+file
dataset = read_csv(url, header=0)

array = dataset.values
X = array[:, 3:]
Y = array[:, 2:3]
label_encoder = LabelEncoder()
Y = label_encoder.fit_transform(Y)

X = select_features(X, Y, kFeatures, dataset)


# load the model from disk
model = pickle.load(open(final_model_file, 'rb'))
#result = loaded_model.score(X, Y)
orig = dataset
data.store_data(model, orig, X, Y, just_outliers=False)
#print(result)