#! /usr/bin/env python
# -*- coding: utf-8 -*-

import pandas as pd
import os
from sklearn import metrics
from glob import glob
import numpy as np
import argparse

if __name__ == '__main__':

    '''
        Path to config file
    '''
    parser = argparse.ArgumentParser()
    parser.add_argument("input", help="Path to directory with prediction in csv format. You shouldn`t manually edit the files! Only prediction result should be in the csv format(sep=';') in the input.")
    parser.add_argument("output", help="Path to output table.")
    parser.add_argument("-v", help="Verbose. Print metrics.", action="store_true")


    args = parser.parse_args()

    input = args.input
    output = args.output
    verbose = args.v

    folds = glob(os.path.join(input, "*.CSV"))

    union = pd.DataFrame()
    print("Parse results")
    for f in folds:
        tbl = pd.read_csv(f, sep=";", header=4, decimal=",")
        union = pd.concat([union, tbl])
    print(f"Folds union {union.shape}")

    union = union.drop(columns=["Substructure Descriptors", "New Descriptors", "Possible Activities at Pa > Pi"])
    union = union.rename(columns={union.columns[0]: "activity"})
    activities = union.columns[1:]
    prediction = union.query("activity in @activities")
    print("Calculate metrics")
    result = pd.DataFrame(columns=["Activity", "AUROC", "Average precision"])
    for i, a in enumerate(activities):
        pred = np.where(prediction.loc[prediction[a].notnull(), a] <= 0, 0, prediction.loc[prediction[a].notnull(), a])
        true = np.where(prediction.loc[prediction[a].notnull(), "activity"] == a, 1, 0)
        try:
            roc_auc = metrics.roc_auc_score(true, pred)
            pr_auc = metrics.average_precision_score(true, pred)
            if verbose:
                print(f"{a}\t{roc_auc}\t{pr_auc}")
            result.loc[i] = [a, roc_auc, pr_auc]
        except ValueError as ve:
            print(f"{a} {ve}")

    result.to_csv(output, sep = ";", header = True, index = False)
    print(f"Total activities {len(result.index)}")
    print(f"Mean AUROC {round(result['AUROC'].mean(),4)}")
    print(f"Mean AUC-PR {round(result['Average precision'].mean(),4)}")
