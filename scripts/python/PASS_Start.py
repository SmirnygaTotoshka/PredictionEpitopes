#! /usr/bin/env python
# -*- coding: utf-8 -*-

import argparse
import os
import multiprocessing
import time
import glob
from random import shuffle


def launch(proc, partition, program):
    for p in partition:
        print(f"{proc} {os.path.basename(p)} {os.path.basename(program)}")
        command = f"{program} {p}"
        code = os.system(command)
        print(f"{os.path.basename(p)} {code}")


if __name__ == '__main__':

    '''
        Path to config file
    '''
    start_time = time.time()
    parser = argparse.ArgumentParser()
    parser.add_argument("program", help="Path to config file.")
    parser.add_argument("input", help="Directory for pass input.")
    parser.add_argument("threads", help="Number of pass copy launch in one time.")
    args = parser.parse_args()

    program = args.program
    input = args.input
    threads = int(args.threads)

    if not os.path.exists(input):
        raise Exception("Input path doesn`t exist")

    if not os.path.exists(program):
        raise Exception("Program path doesn`t exist")

    launches = glob.glob(os.path.join(input, '*.txt'))
    shuffle(launches)
    #print(launches)

    total = len(launches)
    if total < threads:
        threads = total
    size_part = total // threads
    size_last_part = size_part + (total - size_part * threads)

    # procs - количество ядер
    # calc - количество операций на ядро

    processes = []
    # делим вычисления на количество ядер
    for proc, start in zip(range(threads), range(0, total, size_part)):
        if proc == threads - 1:
            partition = launches[start:start + size_last_part]
        else:
            partition = launches[start:start + size_part]

        p = multiprocessing.Process(target=launch, args=(proc, partition, program))
        processes.append(p)
        p.start()

    # Ждем, пока все ядра
    # завершат свою работу.
    for p in processes:
        p.join()
