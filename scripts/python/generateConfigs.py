#! /usr/bin/env python
# -*- coding: utf-8 -*-

import argparse
import os
import json
import time

if __name__ == '__main__':

    '''
        Path to config file
    '''
    start_time = time.time()
    parser = argparse.ArgumentParser()
    parser.add_argument("input", help="path to config.")
    parser.add_argument("type", help="0 - converter, 1 - SAR, 2 - CSV")
    args = parser.parse_args()

    input = args.input
    type = int(args.type)

    if not os.path.exists(input):
        raise Exception("Input path doesn`t exist")

    if type < 0 or type > 2:
        raise Exception("Incorrect type")

    with open(input, "r") as cfg:

        '''
            Parsing arguments
        '''
        parameters = json.loads(''.join(cfg.readlines()))
        model_names = parameters["model_names"]
        min_level = int(parameters["min_desc_level"])
        max_level = int(parameters["max_desc_level"])


    if type == 0:
        for m in model_names:
            with open(os.path.join(parameters["local_configs"], m + ".json"),"w", encoding = "utf-8") as cfg:
                converter = {}
                converter["input"] = os.path.join(parameters["csv_input"], m + ".csv")
                converter["output"] = parameters["converter_output"]
                converter["column"] = parameters["converter_column"]
                converter["threads"] = parameters["converter_threads"]
                json.dump(converter, cfg)
    elif type == 1:
        for m in model_names:
            win_sdf = parameters["remote_data"] + "\\" +  m + ".sdf"
            for l in range(min_level, max_level+1):
                win_sar = parameters["remote_SAR"] + "\\" + m + "_" + str(l)
                with open(os.path.join(parameters["remote_work_dir"], m + "_" + str(l) + ".txt"), "w", encoding="utf-8") as cfg:
                    cfg.write("BaseCreate=" + str(l) + ";" + m +"_" + str(l) + "\n")
                    cfg.write("BaseAddNewData=" + win_sdf + ";" + parameters["activity"] + "\n")
                    cfg.write("BaseSave=" + win_sar + "\n")
                    cfg.write("BaseTraining" + "\n")
                    cfg.write("BaseValidation" + "\n")
                    cfg.write("BaseClose")
    else:
        for m in model_names:
            for f in range(1,6):
                win_sdf = parameters["remote_data"]+ "\\" + m +"_test_" + str(f) + ".sdf"
            for l in range(min_level, max_level+1):
                win_sar = parameters["remote_SAR"] + "\\" + m + "_train_" + str(f) + "_" + str(l) + ".MSAR"
                win_output = parameters["remote_output"] + "\\" + m + "_test_" + str(f) + "_" + str(l) + ".csv"
                with open(os.path.join(parameters["remote_work_dir"], m + "_" + str(l) + ".txt"), "w", encoding="utf-8") as cfg:
                    cfg.write("InputName=" + win_sdf + "\n");
                    cfg.write("IdKeyField=" + parameters["record_id"] + "\n");
                    cfg.write("BaseName=" + win_sar + "\n");
                    cfg.write("OutputName=" + win_output + "\n");
