# Module extraction - [A]MEX, Depleting Σ-Modules etc.

## Prerequisites
* Linux operating system (necessary for the QBF solvers)
* Java 1.8 or above
* Gradle

## Setup


### Setup the QBF solvers
The java assumes an environmental variable `QBF_LOCATION` exists on the system that points to the **binaries** of one or more QBF solvers.
It's probably best to do this at startup in your .profile or .bash_profile file using a command such as. 

```export QBF_LOCATION=/path/to/quantor:/path/to/sKizzo:/path/to/depqbf```

The module extractor may need to call on several solvers to extract a module if certain solvers take too long or timeout. The order
in which they are listed in the enviromental variable is the order in which used when trying to solve a qbf problem w.r.t module extraction. Be aware that the solver(s) you select will affect the performance of the module extraction so it's worth seeing which ones work best for your application.

The solvers are assumed to be executable linux binaries that take CNF QBF problem encoded in the a `.qdimacs` format as an argument 
i.e `./solver problem.qdimacs` and return `10` if the problem is satisfiable, `20` if it is unsatisfiable. Any other return code will be interpreted as an error. 

### Building the project
1. Import the project into your IDE of choice
2. Use the command `gradle build` to pull in the dependencies and build the project

## Usage

### Examples
A number of examples have been written to show the functionality of the project and are a good place to begin. Assuming you have setup your environment correctly you can find the examples in the package `uk.ac.liv.moduleextraction.examples`

### Running the tests
The separate `test/` directory contains a number of JUnit tests. These can be run using the `gradle test` command.
It is recommended you ensure the tests pass if making any modifications, this  ensure you haven't accidently broken some of the code which is known to be working.
