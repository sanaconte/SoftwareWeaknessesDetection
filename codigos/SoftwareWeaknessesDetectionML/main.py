import operator

from numpy import set_printoptions
# Load libraries
from sklearn.preprocessing import LabelEncoder
from pandas import read_csv
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

#def cmd_create_model():
#    print_banner("Creating model")

#    global model, train_features

#    sel_ds = config.get_str('dataset', 'SelectedDataset')

#    X, Y = transform.get_xy(sel_ds, 'training_set', language, vuln_type, selected_features)

#    X.sort_index(axis=1, inplace=True)

#    if train_features is None:
#        train_features = X.columns

#    model = train.select_model(language, vuln_type, X, Y)

# Press the green button in the gutter to run the script.
if __name__ == '__main__':
    # Load dataset
    #url = "https://raw.githubusercontent.com/jbrownlee/Datasets/master/iris.csv"

    #init global varibles
    dir = "E:/TFM/Trabalho/SoftwareWeaknessesDetection/codigos/SoftwareWeaknessesDetectionML"
    vuln_type = "NPD"
    #vuln_type = "CI"
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

    # box and whisker plots
    ##dataset.plot(kind='box', subplots=True, layout=(2, 2), sharex=False, sharey=False)
    #dataset.plot(kind='box', subplots=True, sharex=False, sharey=False)
    #pyplot.show()

    # histograms
    #dataset.hist()
    #pyplot.show()

    #scatter plot matrix
    #scatter_matrix(dataset)
    #pyplot.show()

    # Split-out validation dataset
    array = dataset.values
    ## valores de atributos/features
    X = array[:, 3:]
    ## valores de classe
    y = array[:, 2:3]
    #y = y.flatten()
    label_encoder = LabelEncoder()
    y = label_encoder.fit_transform(y)
    #print(X)
    #print(y)


    # Selecting features
    kFeatures = 5
    #print(dataset.columns)
    #X = select_features2(X, y, kFeatures)
    X = select_features(X, y, kFeatures, dataset)
    #X = sync_features(X)
    #print(X)
    ## Divisão da dataset
    #80% de dados para treino (dados de treino)
    #20% de dados para validação (dados de teste)

    X_train, X_validation, Y_train, Y_validation = train_test_split(X, y, test_size=0.30, random_state=1, stratify=y)
    # Spot Check Algorithms
    models = []
    models.append(('LR', LogisticRegression(solver='liblinear', multi_class='ovr', penalty='l2', C=0.1, class_weight='balanced', n_jobs=-1)))
    models.append(('LDA', LinearDiscriminantAnalysis()))
    models.append(('KNN', KNeighborsClassifier()))
    models.append(('CART', DecisionTreeClassifier()))
    models.append(('NB', GaussianNB()))
    models.append(('SVM', SVC(gamma='auto')))

    # evaluate each model in turn
    results = []
    names = []
    for name, model in models:
        kfold = StratifiedKFold(n_splits=10, random_state=1, shuffle=True)
        cv_results = cross_val_score(model, X_train, Y_train, cv=kfold, scoring='accuracy')
        results.append(cv_results)
        names.append(name)
        print('%s: %f (%f)' % (name, cv_results.mean(), cv_results.std()))


    # Compare Algorithms
    pyplot.boxplot(results, labels=names)
    pyplot.title('Algorithm Comparison')
    pyplot.show()


    # Make predictions on validation dataset
    model = LogisticRegression(solver='liblinear', multi_class='ovr')
    model_type = "LogisticRegression"
    #model = LinearDiscriminantAnalysis()
    #model = SVC(gamma='auto')
    #model = LogisticRegression(solver='liblinear', multi_class='ovr')
    model.fit(X_train, Y_train)
    predictions = model.predict(X_validation)

    # Evaluate predictions
    print(accuracy_score(Y_validation, predictions))
    print(confusion_matrix(Y_validation, predictions))
    print(classification_report(Y_validation, predictions))
    print_metrics(model=model, X=X_validation, Y=Y_validation)

    # display model
    #display_pr_curve(title="%s %s" % (vuln_type, model_type), model=model, X=X_validation, Y=Y_validation)


    # Save Model Using Pickle
    #mode = 'a' if os.path.exists(final_model_file) else 'wb'
    #print(mode)
    #pickle.dump(model, open(final_model_file, 'wb'))
    orig = dataset #dataset['PROJECT_NAME']
    data.store_data(model, orig, X_validation, Y_validation, just_outliers=False)



# See PyCharm help at https://www.jetbrains.com/help/pycharm/
