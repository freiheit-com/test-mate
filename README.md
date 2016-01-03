[![Build Status](https://travis-ci.org/freiheit-com/test-mate.svg?branch=master)](https://travis-ci.org/freiheit-com/test-mate)

Usage:

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

The projects in this file are tried to be created in the statistic server. The result
is printed to stdout.

To ignore a project in the creation mark it with `:skip true` (this allows to keep
a file with all projects in the statistic server and only append new projects).

# Coverage data Aggregation

    test-mate aggregate <path/to/jacoco_coverage.xml> my/package/one my/package/two ...

Note: You can supply the special package "/" to aggregate the overall coverage result.

# Analysis

## test-need

    test-mate test-need <path/to/jacoco_coverage.xml> <path/to/git/repo>

TODO: Describe test-need analysis?
