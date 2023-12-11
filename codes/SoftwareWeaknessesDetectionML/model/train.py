from sklearn import svm
from sklearn import tree
from sklearn.dummy import DummyClassifier
from sklearn.ensemble import RandomForestClassifier, BaggingClassifier
from sklearn.feature_selection import SelectKBest, chi2
from sklearn.linear_model import LogisticRegression
from sklearn.naive_bayes import BernoulliNB
from sklearn.tree import DecisionTreeClassifier

hyperparameters = {'DecisionTreeClassifier':
                       {'max_depth': [5, 15, 30, None],
                        'min_samples_leaf': [1, 2, 10, 50, 100],
                        'max_features': ['log2', 'sqrt', None],
                        'n_jobs': [-1],
                        'class_weight': ['balanced']},
                   'BernoulliNB': {
                       'alpha': [0.001, 0.003, 0.01, 0.03, 0.1, 1, 3, 10, 30, 100],
                       'binarize': [None]
                   },
                   'RandomForestClassifier':
                       {'n_estimators': [300, 500],
                        'max_depth': [5, 15, 30, None],
                        'min_samples_leaf': [1, 2, 10],
                        'max_features': ['log2', 'sqrt'],
                        'class_weight': ['balanced'],
                        'n_jobs': [-1]
                        },
                   'TAN': {'mbc': [''],
                           'score_type': ['BAYES']
                        },
                   'SVM':
                       {'C': [0.01, 0.1, 1, 10, 100],
                        'gamma': [0.0001, 0.01, 0.1, 1, 10, 100, 'auto'],
                        'kernel': ['rbf'],
                        'probability': [True],
                        'shrinking': [False],
                        'class_weight': ['balanced']
                        },
                   'LogisticRegression':
                       {'penalty': ['l1', 'l2'],
                        'C': [0.001, 0.01, 0.1, 1, 10, 100],
                        'class_weight': ['balanced'],
                        'n_jobs': [-1]
                        },
                   'DummyClassifier': {
                        'strategy': ['most_frequent', 'stratified', 'uniform']},
                   }


def create_model(model_type, params):
    model = None

    if model_type == "DecisionTreeClassifier":
        model = DecisionTreeClassifier()
    elif model_type == "BernoulliNB":
        model = BernoulliNB()
    elif model_type == "RandomForestClassifier":
        model = RandomForestClassifier()
    elif model_type == "SVM":
        model = svm.SVC()
    elif model_type == "LogisticRegression":
        model = LogisticRegression()
    elif model_type == "DummyClassifier":
        model = DummyClassifier()

    if params is not None:
        for parameter, value in params.items():
            setattr(model, parameter, value)

    if model_type == "SVM":
        n_estimators = 10

        # Because SVM is so slow, we use a bagging classifier to speed things up
        model = BaggingClassifier(model, max_samples=1.0 / n_estimators, n_estimators=n_estimators, n_jobs=4)

    return model