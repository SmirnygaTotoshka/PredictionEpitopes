#! /usr/bin/env python
# -*- coding: utf-8 -*-

import argparse
import os
import pandas as pd
from glob import glob
import re


if __name__ == '__main__':

    '''
        Path to config file
    '''
    parser = argparse.ArgumentParser()
    parser.add_argument("input", help="Directory for pass input.")
    args = parser.parse_args()

    input = args.input

    tbl = pd.DataFrame(columns=["model_name","descriptor_level","num_subst", "iap", "twentyCV","activity"])
    os.chdir(input)
    results = glob('*.HST')
    header = "No	 Check	 Number	 IAP	 20-Fold	 Activity"
    for r in results:
        with open(r,"r") as f:
            split_name = re.split("_",os.path.splitext(r)[0])
            model_name = split_name[0]
            level = split_name[1]
            lines = f.readlines()
            flag = False
            for line in lines:
                if header in line:
                    flag = True
                    continue
                if line == "\n" and flag:
                    break
                if flag:
                    components = re.split("\t\\s+",line)
                    num_subst = components[2]
                    iap = components[3]
                    twentyCV = components[4]
                    activity = components[5].strip()
                    row = pd.DataFrame.from_dict({"model_name":[model_name],
                                          "descriptor_level":[int(level)],
                                          "num_subst":[int(num_subst)],
                                          "iap":[float(iap.replace(",","."))],
                                          "twentyCV":[float(twentyCV.replace(",","."))],
                                          "activity":[activity]})
                    tbl = pd.concat([tbl,row],ignore_index=True)
            if not flag:
                print(f"{r} not any predictable activity")
    tbl.to_excel("results.xlsx", index = False)

