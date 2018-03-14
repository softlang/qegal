from itertools import repeat

import pandas as pd
import numpy as np
import math
import matplotlib.pyplot as plt
from lxml.objectify import annotate
import requests
import json
import base64
import time
import config

if __name__ == '__main__':
    pd.set_option('display.max_columns', 100)
    pd.set_option('display.width', 1000)
    plt.rcParams["font.family"] = "consolas"
    plt.rcParams["font.size"] = 14

    headers = {
        'Authorization': 'Basic ' + base64.b64encode((config.login_git + ':' + config.password_git).encode()).decode()}


    def collect(service):
        return json.loads(requests.get(service, headers=headers).text)


    def rate_limit():
        return collect('https://api.github.com/rate_limit')


    rl = rate_limit()['rate']['remaining']


    def rate_collect(service):
        global rl
        while rl < (1 + 5):
            print("sleeping rate limit at " + str(rl))
            time.sleep(30)
            rl = rate_limit()['rate']['remaining']

        rl = rl - 1
        encoded_authorization = 'Basic ' + base64.b64encode(
            (config.login_git + ':' + config.password_git).encode()).decode()
        headers = {'Authorization': encoded_authorization}
        return json.loads(requests.get(service, headers=headers).text)


    ecore_raw = pd.read_csv('../../../../../../../data/files_ecore_raw.csv', encoding='ISO-8859-1')
    genmodel_raw = pd.read_csv('../../../../../../../data/files_genmodel_raw', encoding='ISO-8859-1')
    eobject_raw = pd.read_csv('../../../../../../../data/files_eobject_raw', encoding='ISO-8859-1')
  
    repository_raw_file = '../../../../../../../data/repository_raw.csv'

    ecore_raw['type'] = 'ecore'
    genmodel_raw['type'] = 'genmodel'
    eobject_raw['type'] = 'eobject'

    repositories = data = pd.concat([ecore_raw, genmodel_raw, eobject_raw])[['repository', 'type']]

    repositories = repositories.groupby(by='repository').apply(lambda x: x.groupby('type').count().transpose())
    repositories = repositories.fillna(0)
    repositories.reset_index(level=0, inplace=True)

    # Currently using sampling here.
    print(repositories)

    #repositories = repositories.sample(n=10)


    def add_metadata(x):
        try:
            data = rate_collect('https://api.github.com/repos/' + x.repository)
            x['stargazers_count'] = data['stargazers_count']
            x['forks_count'] = data['forks_count']
            x['watchers_count'] = data['watchers_count']
            x['size'] = data['size']
            x['default_branch'] = data['default_branch']
            x['language'] = data['language']
            x['created_at'] = data['created_at']
            x['updated_at'] = data['updated_at']
            x['pushed_at'] = data['pushed_at']

            latest = rate_collect('https://api.github.com/repos/' + x.repository + '/commits/' + x.default_branch)
            x['sha'] = latest['sha']
            x['sha_data'] = latest['commit']['committer']['date']

        except:
            print('error ' + x.repository)

        print(x.repository)
        return x


    repositories = repositories.apply(add_metadata, axis=1)

    repositories.to_csv(repository_raw_file, index=False)

    print(repositories)
