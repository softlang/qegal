import pandas as pd
import numpy as np
import math
import matplotlib.pyplot as plt
import seaborn as sns

if __name__ == '__main__':
    pd.set_option('display.max_columns', 100)
    pd.set_option('display.width', 1000)
    plt.rcParams["font.family"] = "consolas"
    plt.rcParams["font.size"] = 14

    softlang = 'http://org.softlang.com/'

    repositories = pd.read_csv('../../../../../../../data/repository_emf.csv', encoding='ISO-8859-1')

    repositories.rename(columns=dict(
        (c, c.replace('http://org.softlang.com/qegal/', '').replace('http://org.softlang.com/', '')) for c in
        repositories.columns), inplace=True)

    repositories = repositories.fillna(value=0.0)

    # Currently missing EJG
    catalog = repositories[[x for x in ['E', 'J', 'G', 'C', 'EJ1', 'EJ2', 'EJ3', 'EE', 'EJc1', 'EJc2', 'EJJ', 'EJG'] if
                            set([x]).issubset(repositories.columns)]]

    describe = catalog.describe().transpose()
    describe['cv'] = describe['std']/ describe['mean']
    describe = describe.transpose()
    print(describe.round(1).to_latex())

    catalog.hist(column='EJ3', bins=100)
    plt.show()

    rcatalog = catalog.apply(lambda x: x.apply(lambda y: min(1, y)), axis=1)

    catalog = catalog.sum(axis=0).to_frame('count')
    rcatalog = rcatalog.sum(axis=0).to_frame('count')

    catalog['id'] = catalog.index
    rcatalog['id'] = rcatalog.index

    catalog = catalog.append(pd.DataFrame([{'id': 'EJc1', 'count': 0.0}]))
    rcatalog = rcatalog.append(pd.DataFrame([{'id': 'EJc1', 'count': 0.0}]))

    sns.set(style="whitegrid")
    fig, ax = plt.subplots(figsize=(10, 4))

    sns.set_color_codes("pastel")
    sns.barplot(data=catalog, y='count', x='id', label="Patterns", color="b")

    sns.set_color_codes("muted")
    sns.barplot(data=rcatalog, y='count', x='id', label="Repositories", color="b")

    # Add a legend and informative axis label
    ax.legend(ncol=2, loc="upper right", frameon=True)
    ax.set(ylabel="Total",
           xlabel="Pattern")
    sns.despine(left=True, bottom=True)
    fig.subplots_adjust(bottom=0.20, left=0.20, right=0.85, top=0.80)

    print(catalog)
    print(rcatalog)
    plt.show()
