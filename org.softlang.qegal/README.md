# QegaL
Rule-based repository mining

* Mining linguistic patterns with Jena reasoning.
* All triples correspond to MegaL relationships

# How to use:
* Use Eclipse to automatically build the project with Maven.
* Create a file "config.properties" with the following entries:
    * ``temp=<filepath to a temporary folder>``
    * ``rule_mde_workdir=<filepath to deploy repos to be mined>``
    * ``rule_mde_input=<filepath to target repos table as .csv>`` See data/sample_data.csv for an example
    * ``login_git=< github-username >``
    * ``password_git=< github-password >``
* Run org.softlang.qegal.QegalProcess with VM option -Xmx4g at least (more is better).

# Testing strategy:
All rules are covered as follows:
* Every module's name appears as the name of a test class.
* Every rules name appears as the name of a test method.
* Every produced triple (after ``->``) is tested.
* Every result is recomputed in a transparent manner avoiding
  incremental reasoning.