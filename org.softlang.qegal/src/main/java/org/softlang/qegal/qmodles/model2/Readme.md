# Validation Artifacts for Ecore to Generator Model to Java files

The following artifacts were created:
* The folder "rules" contains the .qegal rules for the instantiation of the model.
* The folder "exclusion" contains .qegal rules for implementing the selection of projects.
* The file DemoAssertion.java contains Unit Tests that check negative and positive assertions that are persisted in demoassertion+ and demoassertion-.txt
* The file DemoQueryPos.java starts the query for positive instances in built projects. The paths to demo projects have to be provided in a respective .txt file.
* The file QueryNeg.java starts the query for violated integrity constraints.
* The file WildScope.java determines the repositories that we intend to analyze.
* The file WildScopeExclusion.java determines the repos, where we actually search for instances based on exclusion criteria.
* The file WildQueryPos.java starts the query for positive instances in GitHub projects.
