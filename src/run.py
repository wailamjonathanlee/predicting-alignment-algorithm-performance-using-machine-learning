#!/usr/bin/env python


import argparse, json, os
from datetime import datetime

from alignclf.utils import *
from alignclf.constants import *
from alignclf.experiments import *


if __name__ == '__main__':
    parser = argparse.ArgumentParser()

    parser.add_argument('-c', action='store',
                        dest='config_json',
                        help='Experiment configuration json file')

    parser.add_argument('-l', action='store',
                        dest='logging_json',
                        help='Python logger configuration json file path')

    args = parser.parse_args()

    if args.config_json is None or args.logging_json is None:
        print('Run as python ./run.py -c [config.json] -l [logging.json]')
        exit(0)

    with open(args.config_json, 'r') as f:
        configs = json.load(f)

    dt = datetime.now().strftime('%Y-%m-%d_%H:%M:%S:%f')

    # make the out directory for this run
    basedir = configs[BASEDIR]
    result_dir = os.path.join(basedir, configs[RESULT_DIR])
    outdir = '_'.join([dt, configs[EXPERIMENT_NAME]])
    outdir = os.path.join(result_dir, outdir)

    os.makedirs(outdir)

    # update the outdir, resultdir
    configs[OUTDIR] = outdir
    configs[RESULT_DIR] = result_dir

    setup_logging(logdir=outdir, default_path=args.logging_json)
    print('Finished setting up logger')

    # if configs[EXPERIMENT_NAME] == 'minimal':
    #     print('Hello world!')
    #     print('Finished running minimal configuration')

    runner = RunnerFactory().build_runner(configs)
    runner.run()