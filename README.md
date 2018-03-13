# qegal
Rule-based fact extraction in a megamodeling context

* Can be used for mining repositories in a megamodeling context with Jena reasoning.

# What is contained:
* The top-level folders correspond to Eclipse projects (that can directly by imported after checkout).
* The top-level project [org.softlang.qegal](https://github.com/softlang/qegal/tree/master/org.softlang.qegal) contains the mining related aspects.
* The top-level projects [org.softlang.qegal.lang](https://github.com/softlang/qegal/tree/master/org.softlang.qegal.lang), [org.softlang.qegal.lang.ide](https://github.com/softlang/qegal/tree/master/org.softlang.qegal.lang.ide) and [org.softlang.qegal.lang.ui](https://github.com/softlang/qegal/tree/master/org.softlang.qegal.lang.ui) contain the Xtext IDE Support.

* The data-set, including the repository lists and all results, are available in the [data folder of project org.softlang.qegal] (https://github.com/softlang/qegal/tree/master/org.softlang.qegal/data) (for preserving relative references).
* All available rules for EMF and layout detection are located in the java package [org.softlang.qegal.modules] (https://github.com/softlang/qegal/tree/master/org.softlang.qegal/src/main/java/org/softlang/qegal/modules) as '.qegal' files. The rules can be accessed using a simple text editor or the deployed XText IDE.
* All available (mining) processes are located in the java package [org.softlang.qegal.process] (https://github.com/softlang/qegal/tree/master/org.softlang.qegal/src/main/java/org/softlang/qegal/process) an can be used to read or update the relative data folder.

# How to reproduce the inference:
* Import the [org.softlang.qegal](https://github.com/softlang/qegal/tree/master/org.softlang.qegal) project into an Eclipse workspace.
* Use Eclipse Maven to automatically build the project.
* Create a file "config.properties" with the following entries:
* ``temp=<filepath to a temporary folder>`` (used to check out the target repositories and might require up to 250GB)
* ``login_git=< github-username >`` (needed to use the Git API)
* ``password_git=< github-password >`` (needed to use the Git API)
* 

# How to write and apply a custom mining:

# How to install XText IDE support:
