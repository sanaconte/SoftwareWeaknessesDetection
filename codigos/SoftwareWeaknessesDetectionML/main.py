import argparse
import sys
import warnings

from sklearn import metrics
from sklearn.exceptions import DataConversionWarning
from sklearn.neural_network import MLPClassifier

warnings.filterwarnings("ignore", category=DataConversionWarning, message="<__array_function__ internals>:200: RuntimeWarning: invalid value encountered in cast")
# import sys
# if not sys.warnoptions:
#     import warnings
#     warnings.simplefilter("ignore", category=RuntimeWarning)

import operator
from collections import Counter
import numpy as np
np.seterr(divide='ignore', invalid='ignore')
import pandas as pd
from imblearn.over_sampling import SMOTE
from imblearn.pipeline import Pipeline
from imblearn.under_sampling import RandomUnderSampler
from numpy import where, arange
from sklearn.calibration import CalibratedClassifierCV
from sklearn.feature_extraction.text import TfidfVectorizer, HashingVectorizer
# Load libraries
from sklearn.preprocessing import LabelEncoder, MinMaxScaler
from pandas import read_csv
from pandas.plotting import scatter_matrix
from matplotlib import pyplot, pyplot as plt
from sklearn.model_selection import train_test_split, KFold, RepeatedStratifiedKFold, GridSearchCV
from sklearn.model_selection import cross_val_score
from sklearn.model_selection import StratifiedKFold
from sklearn.metrics import classification_report, roc_curve, roc_auc_score, ConfusionMatrixDisplay, RocCurveDisplay, \
    PrecisionRecallDisplay, precision_recall_curve
from sklearn.metrics import confusion_matrix
from sklearn.metrics import accuracy_score
from sklearn.linear_model import LogisticRegression, SGDClassifier
from sklearn.svm._libsvm import decision_function
from sklearn.tree import DecisionTreeClassifier
from sklearn.neighbors import KNeighborsClassifier, KNeighborsRegressor, RadiusNeighborsRegressor, NearestNeighbors, \
    RadiusNeighborsClassifier

from sklearn.discriminant_analysis import LinearDiscriminantAnalysis
from sklearn.naive_bayes import GaussianNB, MultinomialNB
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

import os
cwd = os.getcwd()
def load_dataset(file, vuln_type):
    # init global varibles
    global final_model_file, vectorizer_file_name, array

    #dir = "E:/TFM/Trabalho/SoftwareWeaknessesDetection/codigos/SoftwareWeaknessesDetectionML"
    # vuln_type = "NPD"
    #vuln_type = "CI"
    # file="teste1.csv"
    # file = "samate-CI-dataset.csv"
    #file = "samate-NPD-dataset.csv"
    # final_model_file = dir+"/pickles/samate_CI_finalized_model.sav"
    final_model_file = cwd + "/pickles/samate_"+vuln_type+"_finalized_model.sav"
    # vectorizer_file_name = dir + "/pickles/vectorizer-CI.sav"
    vectorizer_file_name = cwd + "/pickles/vectorizer-"+vuln_type+".sav"
    #url = "E:/TFM/Trabalho/SoftwareWeaknessesDetection/codigos/CFG_SPOON/dataset/" + file
    url = cwd + "/dataset/" + file
    dataset = read_csv(url, header=0)
    dataset.dropna(inplace=True)
    # shape
    print("Dimensions of Dataset:")
    print(dataset.shape)
    # head
    print("The first 20 rows of the data:")
    print(dataset.head(20))
    # Statistical Summary
    # descriptions
    print("Statistical Summary:")
    print(dataset.describe())
    # class distribution
    print(dataset.groupby('VULNERABLE').size())
    array = dataset.values


def tokenizing_text_data():
    #global X, Y, vectorizer
    global vectorizer
    ## valores de atributos/features
    X = array[:, 3:5]
    ## valores de classe
    Y = array[:, 5:6]
    # Y=Y.astype(bool)
    # DataConversionWarning: A column-vector y was passed when a 1d array was expected.
    # Please change the shape of y to (n_samples, ), for example using ravel().
    Y = Y.ravel()
    # encode class values as integers
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
    return [X, Y]


def smote_data(X, Y):
    #global X, Y
    # summarize class distribution
    counter = Counter(Y)
    print('before SMOTE')
    print(counter)
    # scatter_plot(X, Y, counter)
    # transform the dataset with SMOTE
    oversample = SMOTE()
    X, Y = oversample.fit_resample(X, Y)
    # summarize the new class distribution
    print('after SMOTE')
    counter = Counter(Y)
    print(counter)
    # scatter_plot(X, Y, counter)
    # Save vectorizer Using Pickle
    pickle.dump(vectorizer, open(vectorizer_file_name, 'wb'))
    return [X, Y]


def split_dataset():
    #global X_train, X_validation, Y_train, Y_validation
    # by setting the “stratify”
    X_train, X_validation, Y_train, Y_validation = train_test_split(X, Y, test_size=0.40, random_state=1, stratify=Y)
    # summarize data distribution
    print('summarize data distribution with stratification method')
    train_0, train_1 = len(Y_train[Y_train == 0]), len(Y_train[Y_train == 1])
    test_0, test_1 = len(Y_validation[Y_validation == 0]), len(Y_validation[Y_validation == 1])
    print('>Train: 0=%d, 1=%d, Test: 0=%d, 1=%d' % (train_0, train_1, test_0, test_1))
    return [X_train, X_validation, Y_train, Y_validation]

def search_best_parameters():
    pass
    # define model and best hyperparameter for SVC
    # model = SVC()
    # kernel = ['poly', 'rbf', 'sigmoid']
    # C = [50, 10, 1.0, 0.1, 0.01]
    # gamma = ['scale']
    # # define grid search
    # grid = dict(kernel=kernel, C=C, gamma=gamma)
    # #cv = RepeatedStratifiedKFold(n_splits=10, n_repeats=3, random_state=1)
    # cv = StratifiedKFold(n_splits=10, shuffle=True, random_state=1)
    # grid_search = GridSearchCV(estimator=model, param_grid=grid, n_jobs=-1, cv=cv, scoring='accuracy', error_score=0)
    # #grid_search = GridSearchCV(estimator=model, param_grid=grid, n_jobs=-1, cv=cv, scoring='roc_auc', error_score=0)
    # grid_result = grid_search.fit(X_train, Y_train)
    # # summarize results
    # print("Best: %f using %s" % (grid_result.best_score_, grid_result.best_params_))
    # means = grid_result.cv_results_['mean_test_score']
    # stds = grid_result.cv_results_['std_test_score']
    # params = grid_result.cv_results_['params']
    # for mean, stdev, param in zip(means, stds, params):
    #     print("%f (%f) with: %r" % (mean, stdev, param))
    # define model and best hyperparameter for Logistic Regression
    # model = LogisticRegression()
    # solvers = ['newton-cg', 'lbfgs', 'liblinear']
    # penalty = ['l2']
    # c_values = [100, 10, 1.0, 0.1, 0.01]
    # # define grid search
    # grid = dict(solver=solvers, penalty=penalty, C=c_values)
    # #cv = RepeatedStratifiedKFold(n_splits=10, n_repeats=3, random_state=1)
    # cv = StratifiedKFold(n_splits=10, shuffle=True, random_state=1)
    # grid_search = GridSearchCV(estimator=model, param_grid=grid, n_jobs=-1, cv=cv, scoring='accuracy', error_score=0)
    # grid_result = grid_search.fit(X_train, Y_train)
    # # summarize results
    # print("Best: %f using %s" % (grid_result.best_score_, grid_result.best_params_))
    # means = grid_result.cv_results_['mean_test_score']
    # stds = grid_result.cv_results_['std_test_score']
    # params = grid_result.cv_results_['params']
    # for mean, stdev, param in zip(means, stds, params):
    #     print("%f (%f) with: %r" % (mean, stdev, param))
    # define model and best hyperparameter for Multinomial Naive Bayes
    # model = MultinomialNB()
    # param = {'alpha': [0.00001, 0.0001, 0.001, 0.1, 1, 10, 100, 1000]}
    # #cv = RepeatedStratifiedKFold(n_splits=10, n_repeats=3, random_state=1)
    # cv = StratifiedKFold(n_splits=10, shuffle=True, random_state=1)
    # grid_search = GridSearchCV(estimator=model, param_grid=param, n_jobs=-1, cv=cv, scoring='accuracy', error_score=0)
    # grid_result = grid_search.fit(X_train, Y_train)
    # # summarize results
    # print("Best: %f using %s" % (grid_result.best_score_, grid_result.best_params_))
    # means = grid_result.cv_results_['mean_test_score']
    # stds = grid_result.cv_results_['std_test_score']
    # params = grid_result.cv_results_['params']
    # for mean, stdev, param in zip(means, stds, params):
    #     print("%f (%f) with: %r" % (mean, stdev, param))
    # define model and best hyperparameter for RadiusNeighborsClassifier
    # model = RadiusNeighborsClassifier()
    # # create pipeline
    # pipeline = Pipeline(steps=[('norm', MinMaxScaler()), ('model', model)])
    # # define model evaluation method
    # #cv = RepeatedStratifiedKFold(n_splits=10, n_repeats=3, random_state=1)
    # cv = StratifiedKFold(n_splits=10, shuffle=True, random_state=1)
    # # define grid
    # grid = dict()
    # grid['model__radius'] = arange(10, 100, 100)
    # # define search
    # search = GridSearchCV(pipeline, grid, scoring='accuracy', cv=cv, n_jobs=-1)
    # # perform the search
    # results = search.fit(X_train, Y_train)
    # # summarize
    # print('Mean Accuracy: %.3f' % results.best_score_)
    # print('Config: %s' % results.best_params_)


def get_models():
    global models
    models = []
    models.append(('Logistic regression', LogisticRegression(solver='newton-cg', C=100, penalty='l2')))
    models.append(('Decision tree', DecisionTreeClassifier()))
    models.append(('Support vector machines', SVC(gamma='auto')))
    models.append(('Naive Bayes', MultinomialNB(alpha=1e-05)))
    models.append(('Neighbor Classifier', RadiusNeighborsClassifier(radius=2.0, weights='distance')))
    models.append(('Multi-layer Perceptron classifier',
                   MLPClassifier(hidden_layer_sizes=(60, 60, 60), activation='relu', alpha=0.0001,
                                 learning_rate='constant', solver='adam')))
    # models.append(('LDA', LinearDiscriminantAnalysis()))
    # models.append(('SVM', SGDClassifier(loss='hinge', penalty='l2',
    #                            alpha=1e-3, random_state=42,
    #                            max_iter=5, tol=None)))


def choose_best_model():
    results = []
    names = []
    global model
    for name, model in models:
        # define pipeline
        # over = SMOTE(sampling_strategy=0.1)
        # under = RandomUnderSampler(sampling_strategy=0.5)
        # steps = [('over', over), ('under', under), ('model', model)]
        # pipeline = Pipeline(steps=steps)
        kfold = StratifiedKFold(n_splits=10, shuffle=True, random_state=1)
        # kfold = RepeatedStratifiedKFold(n_splits=10, n_repeats=3, random_state=1)
        # cv_results = cross_val_score(model, X_train, Y_train, cv=kfold, scoring='accuracy', n_jobs=-1)
        cv_results = cross_val_score(model, X_train, Y_train, scoring='roc_auc', cv=kfold, n_jobs=-1)
        results.append(cv_results)
        names.append(name)
        print('%s %f (%f)' % (name, cv_results.mean(), cv_results.std()))
    # Compare Algorithms
    pyplot.boxplot(results, labels=names)
    pyplot.title('Algorithm Comparison')
    pyplot.show()


def make_prediction_on_validaction_set():
    global model_type, model, predictions
    model_type = 'Logistic Regression'
    model = LogisticRegression(solver='newton-cg', C=100, penalty='l2')
    # model_type = 'Multi-layer Perceptron classifier'
    # model = MLPClassifier(hidden_layer_sizes=(60, 60, 60), activation='relu', alpha=0.0001,
    #                      learning_rate='constant', solver='adam')
    # model_type = "LogisticRegression"
    # model = LinearDiscriminantAnalysis()
    # model_type = 'Support vector machines'
    # model = SVC(gamma='scale', probability=True, C=50, kernel='sigmoid')
    # model = LogisticRegression(solver='liblinear', multi_class='ovr')
    # model_type = 'Multinomial Naive Bayes'
    # model = MultinomialNB(alpha=1e-05)
    # model_type = 'Neighbor Classifier'
    # model = RadiusNeighborsClassifier(radius=2.0, weights='distance')
    # model_type = 'Decision tree'
    # model = DecisionTreeClassifier()
    # model = SGDClassifier(loss='hinge', penalty='l2',
    #                        alpha=1e-3, random_state=42,
    #                        max_iter=5, tol=None)
    model.fit(X_train, Y_train)
    predictions = model.predict(X_validation)
    #return model


def evaluate_prediction_for_final_model():
    global confusion_matrix
    print('%s: accuracy score=%.3f' % (model_type, accuracy_score(Y_validation, predictions)))
    confusion_matrix = metrics.confusion_matrix(Y_validation, predictions)
    print(confusion_matrix)
    cm_display = metrics.ConfusionMatrixDisplay(confusion_matrix=confusion_matrix, display_labels=[True, False])
    cm_display.plot()
    plt.show()
    print(classification_report(Y_validation, predictions))
    # print_metrics(model=model, X=X_validation, Y=Y_validation)
    # display model
    # display_pr_curve(title="%s %s" % (vuln_type, model_type), model=model, X=X_validation, Y=Y_validation)
    # predict probabilities  model (Logistic Regression)
    # Calibrate model
    # calibrator = CalibratedClassifierCV(model, cv='prefit')
    # model = calibrator.fit(X_train, Y_train)
    lr_probs = model.predict_proba(X_validation)
    # keep probabilities for the positive outcome only
    lr_probs = lr_probs[:, 1]
    # calculate scores for model
    lr_auc = roc_auc_score(Y_validation, lr_probs)
    # calculate precision-recall curve
    precision, recall, thresholds = precision_recall_curve(Y_validation, lr_probs)
    # summarize scores
    print('summarize scores')
    print('%s: ROC AUC=%.3f' % (model_type, lr_auc))
    # calculate roc curves
    lr_fpr, lr_tpr, _ = roc_curve(Y_validation, lr_probs)
    # plot the roc curve for the model
    # pyplot.plot(lr_fpr, lr_tpr, marker='.', label=model_type)
    # axis labels
    # pyplot.xlabel('False Positive Rate')
    # pyplot.ylabel('True Positive Rate')
    # show the legend
    # pyplot.legend()
    # show the plot
    # pyplot.show()
    # metrics.plot_roc_curve(model, X_validation, Y_validation)
    # plt.show()
    display = PrecisionRecallDisplay.from_estimator(model, X_validation, Y_validation)
    display.ax_.set_title("Class Precision-Recall curve")
    plt.show()
    # calculate different measures to quantify the quality of the model.
    Accuracy = metrics.accuracy_score(Y_validation, predictions)
    Precision = metrics.precision_score(Y_validation, predictions)
    Sensitivity_recall = metrics.recall_score(Y_validation, predictions)
    Specificity_not_vul = metrics.recall_score(Y_validation, predictions, pos_label=0)
    Specificity_vul = metrics.recall_score(Y_validation, predictions, pos_label=1)
    F1_score = metrics.f1_score(Y_validation, predictions)
    print({"Accuracy": Accuracy, "Precision": Precision, "Sensitivity_recall": Sensitivity_recall,
           "Sensitivity_recall_not_vul": Specificity_not_vul, "Sensitivity_recall_vul": Specificity_vul,
           "F1_score": F1_score})


def save_model():
    # mode = 'a' if os.path.exists(final_model_file) else 'wb'
    # print(mode)
    pickle.dump(model, open(final_model_file, 'wb'))
    # orig = dataset #dataset['PROJECT_NAME']
    # data.store_data(model, orig, X_validation, Y_validation, just_outliers=False)

def test_model_real_data(args):
    dir = cwd  # "/SoftwareWeaknessesDetectionML"
    dataset_file_name = args.test[0]
    vuln_type = args.test[1]
    vectorizer_file_name = dir+"/pickles/vectorizer-"+vuln_type+".sav"
    final_model_file = dir + "/pickles/samate_"+vuln_type+"_finalized_model.sav"

    file = dataset_file_name
    url = dir + "/dataset/" + file
    dataset = read_csv(url, header=0)

    array = dataset.values
    Xnew = array[:, 3:5]
    X = Xnew

    vectorizer = pickle.load(open(vectorizer_file_name, 'rb'))
    X = toDataFrame(X)
    vector = vectorizer.transform(X)
    X = vector.toarray()
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


def create_model(args):
    global X, Y, X_train, X_validation, Y_train, Y_validation
    #args = sys.argv[1:]
    #dataset_file_name = args[0]
    #vuln_type = args[1]
    dataset_file_name = args.create[0]
    vuln_type = args.create[1]
    print_notice("vuln_type: " + str(vuln_type))
    print_notice("dataset_file_name: " + str(dataset_file_name))
    # Load dataset
    load_dataset(dataset_file_name, vuln_type)
    [X, Y] = tokenizing_text_data()
    [X, Y] = smote_data(X, Y)
    # split of the dataset into training and test sets, ensuring that the class distribution is preserved
    [X_train, X_validation, Y_train, Y_validation] = split_dataset()
    # search_best_parameters()
    # Spot Check Algorithms
    get_models()
    # onvert data with TfidfVectorizer
    # [X_train, X_validation] = tokenizing(X_train, X_validation)
    # evaluate each model in turn
    choose_best_model()
    # Make predictions on validation dataset
    make_prediction_on_validaction_set()
    # Evaluate predictions
    evaluate_prediction_for_final_model()
    # Save Model Using Pickle
    save_model()


def main():
    # create parser object
    parser = argparse.ArgumentParser(description="Software Weaknesses Detection Application")
    # defining arguments for parser object
    parser.add_argument("-c", "--create", type=str, nargs=2,
                        metavar=('dataset_file_name','vuln_type'),
                        help="Create and train the model with specified dataset.")

    parser.add_argument("-t", "--test", type=str, nargs=2,
                        metavar=('dataset_file_name','vuln_type'),
                        help="Test model with a specific dataset.")

    # parse the arguments from standard input
    args = parser.parse_args()


    # calling functions depending on type of argument
    if args.create != None:
        create_model(args)
    elif args.test != None:
        test_model_real_data(args)


# Press the green button in the gutter to run the script.
if __name__ == '__main__':
    main()

# See PyCharm help at https://www.jetbrains.com/help/pycharm/
