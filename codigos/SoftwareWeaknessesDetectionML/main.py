import operator
from collections import Counter

import numpy
import numpy as np
import pandas as pd
from imblearn.over_sampling import SMOTE
from imblearn.pipeline import Pipeline
from imblearn.under_sampling import RandomUnderSampler
from numpy import where
from sklearn.feature_extraction.text import TfidfVectorizer, HashingVectorizer
# Load libraries
from sklearn.preprocessing import LabelEncoder
from pandas import read_csv
from pandas.plotting import scatter_matrix
from matplotlib import pyplot
from sklearn.model_selection import train_test_split, KFold, RepeatedStratifiedKFold
from sklearn.model_selection import cross_val_score
from sklearn.model_selection import StratifiedKFold
from sklearn.metrics import classification_report, roc_curve, roc_auc_score
from sklearn.metrics import confusion_matrix
from sklearn.metrics import accuracy_score
from sklearn.linear_model import LogisticRegression, SGDClassifier
from sklearn.tree import DecisionTreeClassifier
from sklearn.neighbors import KNeighborsClassifier
from sklearn.discriminant_analysis import LinearDiscriminantAnalysis
from sklearn.naive_bayes import GaussianNB
from sklearn.svm import SVC
from analysis.metrics import display_pr_curve, print_metrics
from analysis import data
from tools.ascii import print_banner, print_notice, print_error
import pickle
# This is a sample Python script.
import os
# Press Shift+F10 to execute it or replace it with your code.
# Press Double Shift to search everywhere for classes, files, tool windows, actions, and settings.

from sklearn.utils.multiclass import type_of_target
from sklearn.feature_selection import SelectKBest, chi2
from sklearn.feature_selection import f_classif

train_features = None

def print_hi(name):
    # Use a breakpoint in the code line below to debug your script.
    print(f'Hi, {name}')  # Press Ctrl+F8 to toggle the breakpoint.

def select_features(X, Y, k, dataset):
    #k = config.get_int('model', 'kFeatures')
    global train_features
    head = dataset.columns[3:]
    print_notice("Sorting features based on chi^2 (k=%d):" % k)

    if k < 0 or k > len(X):
        print_error("k should be >= 0 and <= %d (n_features). Got %d." % (len(X), k))
        exit(-1)

    skb = SelectKBest(chi2, k=k)
    skb.fit_transform(X, Y)

    support = skb.get_support()
    #print(support)
    n = 1
    train_features = dict()

    #print(X[support])
    #print(skb.scores_[support])
    for col_name, score in zip(head[support], skb.scores_[support]):
        train_features[col_name] = score

    for feature, score in sorted(train_features.items(), key=operator.itemgetter(1), reverse=True):
        print_notice("%d. %s %.2f" % (n, feature, score))
        n += 1

    train_features = head[support]
    print(list(set(train_features).intersection(dataset.columns)))
    print(len(list(set(train_features).intersection(dataset.columns))))
    return dataset[list(set(train_features).intersection(dataset.columns))]

def select_features2(X, Y, k):
    # feature extraction
    test = SelectKBest(score_func=f_classif, k=k)
    fit = test.fit(X, Y)
    # summarize scores
    #set_printoptions(precision=3)
    #print(fit.scores_)
    features = fit.transform(X)
    # summarize selected features
    print(features[0:5,:])
    return features;

def sync_features(X):
    missing_cols = set(train_features) - set(X.columns)
    print(missing_cols)
    drop_cols = set(X.columns) - set(train_features)

    for c in missing_cols:
        X[c] = 0

    X.drop(drop_cols, axis=1, inplace=True)

    X.sort_index(axis=1, inplace=True)

    return X

def toDataFrame(X):
    col_names = ['col' + str(i) for i in np.arange(X.shape[0]) + 1]
    X = pd.DataFrame(data=X.T, columns=col_names)
    #X = pd.DataFrame(data=X, columns=["Func", "Var"])
    return X

def tokenizing_dataset(dataset):
    from sklearn.feature_extraction.text import CountVectorizer
    count_vect = CountVectorizer()
    print(dataset)
    X_train_counts = count_vect.fit_transform(dataset)

    from sklearn.feature_extraction.text import TfidfTransformer
    tfidf_transformer = TfidfTransformer()
    X_train_tfidf = tfidf_transformer.fit_transform(X_train_counts)
    #tf_transformer = TfidfTransformer(use_idf=False).fit(X_train_counts)
    #X_train_tfidf = tf_transformer.transform(X_train_counts)
    print(X_train_tfidf)
    return X_train_tfidf


def tokenizing(X_train, X_validation):
    from sklearn.feature_extraction.text import CountVectorizer
    count_vect = CountVectorizer()
    X_train = toDataFrame(X_train)
    print('antes')
    print(X_train)
    X_train_counts = count_vect.fit_transform(X_train)

    from sklearn.feature_extraction.text import TfidfTransformer
    #tf_transformer = TfidfTransformer(use_idf=False).fit(X_train_counts)
    #X_train_tf = tf_transformer.transform(X_train_counts)
    #X_train = X_train_tf.toarray()

    tfidf_transformer = TfidfTransformer()
    X_train_tfidf = tfidf_transformer.fit_transform(X_train_counts)
    X_train = X_train_tfidf;
    print('depois')
    print(X_train)
    X_validation = toDataFrame(X_validation)
    X_new_counts = count_vect.transform(X_validation)
    X_new_tfidf = tfidf_transformer.transform(X_new_counts)
    X_validation = X_new_tfidf;
    return [X_train, X_validation]


def scatter_plot(X, Y, counter):
    # scatter plot of examples by class label
    for label, _ in counter.items():
        row_ix = where(Y == label)[0]
        pyplot.scatter(X[row_ix, 0], X[row_ix, 1], label=str(label))
    pyplot.legend()
    pyplot.show()

# Press the green button in the gutter to run the script.
if __name__ == '__main__':
    # Load dataset
    #url = "https://raw.githubusercontent.com/jbrownlee/Datasets/master/iris.csv"

    #init global varibles
    dir = "E:/TFM/Trabalho/SoftwareWeaknessesDetection/codigos/SoftwareWeaknessesDetectionML"
    vuln_type = "NPD"
    #vuln_type = "CI"
    #file="teste1.csv"
    file = "samate-CI-dataset.csv"
    #file = "samate-NPD-dataset.csv"
    final_model_file = dir+"/pickles/samate_CI_finalized_model.sav"
    #final_model_file = dir+"/pickles/samate_NPD_finalized_model.sav"

    url = "E:/TFM/Trabalho/SoftwareWeaknessesDetection/codigos/CFG_SPOON/dataset/"+file
    #url2 = "E:/TFM/Trabalho/SoftwareWeaknessesDetection/codigos/SoftwareWeaknessesDetectionML/dataset/"
    ##names = ['sepal-length', 'sepal-width', 'petal-length', 'petal-width', 'class']
    ##dataset = read_csv(url, skiprows=1, usecols=lambda x: x != 'Node')
    ##dataset = read_csv(url, sep=",", header=1, index_col=1)
    dataset = read_csv(url, header=0)
    dataset.dropna(inplace=True)
    # shape
    print("Dimensions of Dataset:")
    print(dataset.shape)

    # head
    print("The first 20 rows of the data:")
    print(dataset.head(20))

    #Statistical Summary
    #descriptions
    print("Statistical Summary:")
    print(dataset.describe())

    # class distribution
    print(dataset.groupby('VULNERABLE').size())

    # count the number of missing values for each column
    #print(dataset[['Var']])
    #num_missing = (dataset[['Var']]!='').sum()
    # report the results
    #print(num_missing)

    # box and whisker plots
    ##dataset.plot(kind='box', subplots=True, layout=(2, 2), sharex=False, sharey=False)
    #dataset.plot(kind='box', subplots=True, sharex=False, sharey=False)
    #pyplot.show()

    # histograms
    #dataset.hist()
    #pyplot.show()

    array = dataset.values

    ## valores de atributos/features
    X = array[:, 3:5]
    ## valores de classe
    Y = array[:, 5:6]
    #Y=Y.astype(bool)
    Y = Y.ravel()
    # encode class values as integers
    encoder = LabelEncoder()
    encoder.fit(Y)
    Y = encoder.transform(Y)

    X = toDataFrame(X)
    #vectorizer = HashingVectorizer(lowercase=False)
    vectorizer = TfidfVectorizer(lowercase=False)
    vectorizer = vectorizer.fit(X, y=Y)
    vector = vectorizer.transform(X)
    X = vector.toarray()

    # summarize class distribution
    counter = Counter(Y)
    print('before SMOTE')
    print(counter)
    #scatter_plot(X, Y, counter)

    # transform the dataset with SMOTE
    oversample = SMOTE()
    X, Y = oversample.fit_resample(X, Y)
    # summarize the new class distribution
    print('after SMOTE')
    counter = Counter(Y)
    print(counter)
    #scatter_plot(X, Y, counter)

    # split of the dataset into training and test sets, ensuring that the class distribution is preserved
    # by setting the “stratify”
    X_train, X_validation, Y_train, Y_validation = train_test_split(X, Y, test_size=0.5, random_state=2, stratify=Y)

    # summarize data distribution
    print('summarize data distribution with stratification method')
    train_0, train_1 = len(Y_train[Y_train == 0]), len(Y_train[Y_train == 1])
    test_0, test_1 = len(Y_validation[Y_validation == 0]), len(Y_validation[Y_validation == 1])
    print('>Train: 0=%d, 1=%d, Test: 0=%d, 1=%d' % (train_0, train_1, test_0, test_1))

    # Spot Check Algorithms
    models = []
    models.append(('LR', LogisticRegression(solver='liblinear', multi_class='ovr')))
    #models.append(('LDA', LinearDiscriminantAnalysis()))
    models.append(('KNN', KNeighborsClassifier()))
    models.append(('CART', DecisionTreeClassifier()))
    models.append(('NB', GaussianNB()))
    models.append(('SVM', SVC(gamma='auto')))
    # models.append(('SVM', SGDClassifier(loss='log_loss', penalty='l2',
    #                                     alpha=1e-3, random_state=42,
    #                                     max_iter=5, tol=None)))


    #onvert data with TfidfVectorizer
    #[X_train, X_validation] = tokenizing(X_train, X_validation)

    # evaluate each model in turn
    results = []
    names = []
    for name, model in models:
        # define pipeline
        #over = SMOTE(sampling_strategy=0.1)
        #under = RandomUnderSampler(sampling_strategy=0.5)
        #steps = [('over', over), ('under', under), ('model', model)]
        #pipeline = Pipeline(steps=steps)
        kfold = StratifiedKFold(n_splits=10, shuffle=True, random_state=1)
        #kfold = RepeatedStratifiedKFold(n_splits=10, n_repeats=3, random_state=1)
        #cv_results = cross_val_score(model, X_train, Y_train, cv=kfold, scoring='accuracy', n_jobs=-1)
        cv_results = cross_val_score(model, X_train, Y_train, scoring='roc_auc', cv=kfold, n_jobs=-1)
        results.append(cv_results)
        names.append(name)
        print('%s Mean ROC AUC: %f (%f)' % (name, cv_results.mean(), cv_results.std()))


    # Compare Algorithms
    pyplot.boxplot(results, labels=names)
    pyplot.title('Algorithm Comparison')
    pyplot.show()


    # Make predictions on validation dataset
    model = LogisticRegression(solver='liblinear', multi_class='ovr')
    #model_type = "LogisticRegression"
    #model = LinearDiscriminantAnalysis()
    #model = SVC(gamma='auto')
    #model = LogisticRegression(solver='liblinear', multi_class='ovr')
    # model = SGDClassifier(loss='log_loss', penalty='l2',
    #                                     alpha=1e-3, random_state=42,
    #                                     max_iter=5, tol=None)
    model.fit(X_train, Y_train)
    predictions = model.predict(X_validation)

    # Evaluate predictions
    print(accuracy_score(Y_validation, predictions))
    print(confusion_matrix(Y_validation, predictions))
    print(classification_report(Y_validation, predictions))
    #print_metrics(model=model, X=X_validation, Y=Y_validation)

    # display model
    #display_pr_curve(title="%s %s" % (vuln_type, model_type), model=model, X=X_validation, Y=Y_validation)

    # predict probabilities  model (Logistic Regression)
    lr_probs  = model.predict_proba(X_validation)
    # keep probabilities for the positive outcome only
    lr_probs  = lr_probs[:, 1]

    # calculate scores for model (Logistic Regression)
    lr_auc  = roc_auc_score(Y_validation, lr_probs)

    # summarize scores
    print('summarize scores')
    print('Logistic Regression: ROC AUC=%.3f' % (lr_auc))

    # calculate roc curves
    lr_fpr, lr_tpr, _ = roc_curve(Y_validation, lr_probs)

    # plot the roc curve for the model
    pyplot.plot(lr_fpr, lr_tpr, marker='.', label='LogisticRegression')
    # axis labels
    pyplot.xlabel('False Positive Rate')
    pyplot.ylabel('True Positive Rate')
    # show the legend
    pyplot.legend()
    # show the plot
    pyplot.show()


    # Save Model Using Pickle
    #mode = 'a' if os.path.exists(final_model_file) else 'wb'
    #print(mode)
    #pickle.dump(model, open(final_model_file, 'wb'))
    #orig = dataset #dataset['PROJECT_NAME']
    #data.store_data(model, orig, X_validation, Y_validation, just_outliers=False)



# See PyCharm help at https://www.jetbrains.com/help/pycharm/
