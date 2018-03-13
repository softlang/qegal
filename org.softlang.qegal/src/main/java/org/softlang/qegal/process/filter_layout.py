import pandas as pd
import numpy as np
import math
import matplotlib.pyplot as plt
from lxml.objectify import annotate
import requests
import json
import base64
import config
import seaborn as sns

if __name__ == '__main__':
    pd.set_option('display.max_columns', 100)
    pd.set_option('display.width', 1000)
    plt.rcParams["font.family"] = "consolas"
    plt.rcParams["font.size"] = 14

    softlang = 'http://org.softlang.com/'

    repositories = pd.read_csv( '../../../../../../../data/repository_layout.csv', encoding='ISO-8859-1')

    vanilla_file = '../../../../../../../data/repository_vanilla.csv'

    repositories = repositories.fillna(0)

    repositories.rename(columns=dict(
        (c, c.replace('http://org.softlang.com/qegal/', '').replace('http://org.softlang.com/', '')) for c in
        repositories.columns), inplace=True)


    builds = ['Manifest','Pom', 'Gradle', 'Ant']
    for build in builds:
        repositories[build] = repositories[build].apply(lambda x: min(x, 1))

    repositories['uniform_build'] = repositories.apply(lambda x: sum([x[i] for i in builds]) == 1, axis=1)

    uniform = repositories[repositories.uniform_build].sum(axis=0)
    vanilla = repositories[
        repositories.uniform_build & (repositories.components == 1) & (repositories.duplicates == 0)]

    print(vanilla)
    vanilla.to_csv(vanilla_file, index=False)

    # build_tools = repositories[
    #     [softlang + 'Manifest', softlang + 'Pom', softlang + 'Gradle', softlang + 'Ant']].applymap(lambda x: min(1, x))
    # exclusive_build_tools = build_tools[build_tools.sum(axis=1) == 1]
    #
    # print(build_tools.sum(axis=0))
    #
    sns.set(style="whitegrid")
    fig, ax = plt.subplots(figsize=(10,4))

    temp = pd.concat([repositories[builds].sum(axis=0).to_frame("build"),
                      uniform[builds].to_frame("uniform"),
                      vanilla[builds].sum(axis=0).to_frame("vanilla")]
                     , axis=1)
    # temp = pd.DataFrame([repositories[builds].sum(axis=0), repositories[builds].sum(axis=0)],
    #                     columns=['build', 'uniform_build'])
    temp['type'] = temp.index
    print(temp)
    sns.set_color_codes("pastel")
    sns.barplot(data=temp, x='build', y='type',
                label="Heterogeneous Buildsystem", color="b")

    sns.set_color_codes("muted")
    sns.barplot(data=temp, x='uniform', y='type',
                label="Homogeneous Buildsystem", color="b")

    sns.set_color_codes("pastel")
    sns.barplot(data=temp, x='vanilla', y='type',
                label="Vanilla EMF", color="y")

    # Add a legend and informative axis label
    ax.legend(ncol=2, loc="lower right", frameon=True)
    ax.set(ylabel="",
           xlabel="Total Repositories")
    sns.despine(left=True, bottom=True)
    fig.subplots_adjust(bottom=0.20, left=0.30, right=0.95, top=0.80)

    plt.show()
