
# Load libraries
from pandas import read_csv
from pandas.plotting import scatter_matrix
from matplotlib import pyplot
from sklearn.model_selection import train_test_split
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


from tools.ascii import print_banner, print_notice

# This is a sample Python script.

# Press Shift+F10 to execute it or replace it with your code.
# Press Double Shift to search everywhere for classes, files, tool windows, actions, and settings.


def print_hi(name):
    # Use a breakpoint in the code line below to debug your script.
    print(f'Hi, {name}')  # Press Ctrl+F8 to toggle the breakpoint.

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

    file = "139910-v1.0.0.csv"
    url = "E:/TFM/Trabalho/SoftwareWeaknessesDetection/codigos/SoftwareWeaknessesDetectionML/dataset/"+file
    ##names = ['sepal-length', 'sepal-width', 'petal-length', 'petal-width', 'class']
    ##dataset = read_csv(url, skiprows=1, usecols=lambda x: x != 'Node')
    ##dataset = read_csv(url, sep=",", header=1, index_col=1)
    dataset = read_csv(url, header=1)

    #remove first line and first column
    #dataset = dataset.drop(['Node'], axis = 'columns')
    dataset = dataset.drop(dataset.columns[0], axis=1)
    ##dataset = dataset.tail(-1)

    # shape
    #print(dataset.shape)

    # head
    #print(dataset.head(20))

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
    X = array[:, 0:dataset.shape[1]-1]
    ## valores de classe
    y = array[:, dataset.shape[1]-1]
    #print(X)
    ##print(y)
    #X_train, X_validation, Y_train, Y_validation = train_test_split(X, y, test_size=0.20, random_state=1)
    X_train, X_validation, Y_train, Y_validation = train_test_split(X, y, random_state=0)

    #model = LinearDiscriminantAnalysis()
    #name = 'LDA'
    #kfold = StratifiedKFold(n_splits=4, random_state=1, shuffle=True)
    #cv_results = cross_val_score(model, X_train, Y_train, cv=kfold, scoring='accuracy')
    #print('%s: %f (%f)' % (name, cv_results.mean(), cv_results.std()))

    # Spot Check Algorithms
    models = []
    #models.append(('LR', LogisticRegression(solver='liblinear', multi_class='ovr')))
    #models.append(('LDA', LinearDiscriminantAnalysis()))
    #models.append(('KNN', KNeighborsClassifier()))
    models.append(('CART', DecisionTreeClassifier()))
    #models.append(('NB', GaussianNB()))
    #models.append(('SVM', SVC(gamma='auto')))
    # evaluate each model in turn
    results = []
    names = []
    for name, model in models:
        kfold = StratifiedKFold(n_splits=2, random_state=0, shuffle=True)
        cv_results = cross_val_score(model, X_train, Y_train, cv=kfold, scoring='accuracy')
        results.append(cv_results)
        names.append(name)
        print('%s: %f (%f)' % (name, cv_results.mean(), cv_results.std()))

    ...
    # Compare Algorithms
    pyplot.boxplot(results, labels=names)
    pyplot.title('Algorithm Comparison')
    pyplot.show()

    ...
    # Make predictions on validation dataset
    model = SVC(gamma='auto')
    model.fit(X_train, Y_train)
    predictions = model.predict(X_validation)

    # Evaluate predictions
    print(accuracy_score(Y_validation, predictions))
    print(confusion_matrix(Y_validation, predictions))
    print(classification_report(Y_validation, predictions))

# See PyCharm help at https://www.jetbrains.com/help/pycharm/
