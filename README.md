[![Build Status](https://travis-ci.org/freiheit-com/test-mate.svg?branch=master)](https://travis-ci.org/freiheit-com/test-mate)

Usage:

# Running test-mate

either

`lein run <parameters>` from the git repository with leiningen installed

or

`test-mate <parameters>` where test-mate is an alias for `java -jar <path-to-test-mate-jar>` that
is used throughout this documentation.

# Configuration

test-mate expects a config file in ~/.test-mate-config.
You can provide the following data in the config file:

    {:statistic-server-url "https://localhost:8443"
     :auth-tokens {
       :publish "test"
     }
     :default-project {
       :project "test"
       :subproject "test-sub"
       :language "java"
     }
    }

Optionally, you can set the Java system property `fdc.test-mate.config.file` with a file-path to the config file.
Setting the system property overrides the configuration in the home directory.

This config file is only needed if you want do interface with statistic server.

# Interfacing with statistic server

## pushing coverage database

    test-mate statistic-server publish-coverage <path/to/jacoco_coverage.xml>

Aggregates the coverage file with "/" and submits the result of this aggregation to
the configured statistic-server.

You can overwrite the default project data with an extra argument to the publish command:

    test-mate statistic-server publish-coverage <path/to/jacoco_coverage.xml> ''{:project "<project-name>" :subproject "<subproject-name>" :language "<language>"}'

The provided data are merged with the default-project data and you can omit any key as long as they are defined
in the config file.

## adding projects

    test-mate statistic-server add-project <path/to/project-def-file>

The project-def-file expects this format:

    [
      {:skip true :project "project-name :subproject "subproject-name" :language "java"}
      {:project "project-name :subproject "subproject-name :language "clojure"}
      {:project "test" :subproject "test-sub3" :language "java"}
    ]

test-mate tries to generate the projects from the file onto the statistic-server. The result
is printed to stdout.


To ignore a project in the creation mark it with `:skip true` (this allows to keep
a file with all projects in the statistic server and only append new projects).

# Coverage data Aggregation

    test-mate aggregate <path/to/coverage_file> package-regex-1 package-regex-2 ...

This command aggregates coverage data for each `package-regex` (Java regex syntax) and prints the result in EDN-format to the
console.

Note: You can supply the special package `/` (or use the regex `.*`) to aggregate the overall coverage result. Sub-package aggregation is not supported by all coverage formats.

test-mate automatically infers the coverage format from `coverage_file`. Currently the following formats are supported:
- Emma/Jacoco (also supports the old coverage format, as written bei cloverage)
- Cobertura (does not yet support the subpackage aggregation)

# Analyse

## test-need

The test-need analysis helps to find classes in Java-based projects that probably need
tests added the most (hence the name test-need).

It outputs a csv file with some metrics for each java file in the repository.

To run the analysis you will need:
- a git repository
- an Emma/Jacoco coverage xml report file

executing `test-mate analysis test-need` will print the options for the analysis, currently:

    test-need analysis options:
    -c, --coverage-file FILE                       coverage file (emma/jacoco format)
    -r, --git-repo REPO                            git repository
    -o, --output FILE         ./test_need_out.csv  output file (csv format)
    -n, --num-commits NUM     1000                 number of commits to consider (this affects runtime)
    -p, --prefix PREFIX       src/main/java/       prefix used to put before class names in the emma report to get a valid file in the git repo

Only the options `--coverage-file` and `--git-repo` are mandatory. With `--output` you can select a
different output file location if the default file location is not your first choice.

The `--num-commits` options controls the considered set of files, for which the repository is scanned. Supplying a high number
affects runtime of the analysis run, since the git operations are not fast enough.

The first ranking is done by absolute uncovered lines for each file. This information is extracted from the
supplied coverage-file. The first `--num-commits` files in this ranked list are considered for further analysis. (The idea
behind this filter: files with good coverage probably do not need tests badly. This assumption can, of course,
be completely wrong).

In order to match files from the coverage-file to files in the git-repository the `--prefix` option can be used. This prefix
is prepended to the file name in the coverage-file and should provide the file location of the java source file in
the git-repository. The analysis prints warnings if it cannot find the file in the git-repository and ultimately ignores the file.

After analysis execution outputs a csv file with this information:

    class,commits,bugfixes,uncovered,lines,last-changed,'=>,coverage,bugfix/uncovered,bugfix/commit,bugfix/lines,days-last-update

with:
* class: the java source file
* commits: the number of commits for this source file
* bugfixes: the number of bugfixes for the source file. A commit is considered a bugfix if the commit message contains
  the string `fix` (case-insensitive) in it
* uncovered: absolute number of uncovered lines
* last-changed: unix-timestamp of last commit for this file

separated by a `=>` follow the derived data, which are:
* coverage: in percentage
* bugfix/uncovered: number of bugfixes per uncovered line
* bugfix/commit: ratio of bugfixes on total commits of this file
* bugfix/lines: ratio of bugfixes on total lines of this file
* days-last-update: 'freshness' of the file. A file that was not accessed in a long time, probably does not need new tests added to it.

Finding an interesting case for a class that needs tests added to it is now a manual task.
The idea is to open the csv file with an editor that supports sorting by different columns
and see which class bubbles up with different metrics.
