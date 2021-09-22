# Contributing to Zeebe-Simple-Tasklist

:tada: First off, thanks for taking the time to contribute! :+1:

## How Can I Contribute?

### Reporting Bugs

If you found a bug or an unexpected behevior then please create
a [new issue](https://github.com/camunda-community-hub/zeebe-simple-tasklist/issues). Before creating an issue, make sure
that there is no issue yet. Any information you provide in the issue, helps to solve it.

### Suggesting Enhancements

If you have an idea how to improve the project then please create
a [new issue](https://github.com/camunda-community-hub/zeebe-simple-tasklist/issues). Describe your idea and the
motivation behind it.

Please note that this is a community-driven project. The maintainers may have not much time to implement new features if
they don't benefit directly from it. So, think about providing a pull request.

### Providing Pull Requests

You want to provide a bug fix or an improvement? Great! :tada:

Before opening a pull request, make sure that there is a related issue. The issue helps to confirm that the behavior is
unexpected, or the idea of the improvement is valid. (Following the rule "Talk, then code")

In order to verify that you don't break anything, you should build the whole project and run all tests. This also apply
the code formatting.

Please note that this is a community-driven project. The maintainers may have no time to review your pull request
immediately. Stay patient!

## Building the Project from Source

You can build the project with [Maven](http://maven.apache.org).

In the root directory:

Run the tests with

```
mvn test
```

Build the JAR files with

```
mvn clean install
```

## Styleguides

### Source Code

The Java code should be formatted using [Google's Java Format](https://github.com/google/google-java-format).

### Git Commit Messages

Commit messages should follow the [Conventional Commits](https://www.conventionalcommits.org/en/v1.0.0/#summary) format.

For example:

```
feat(ui): show completed tasks

* show all tasks that are completed
* ...
```

Available commit types:

* `feat` - enhancements, new features
* `fix` - bug fixes
* `refactor` - non-behavior changes
* `test` - only changes in tests
* `docs` - changes in the documentation, readme, etc.
* `style` - apply code styles
* `build` - changes to the build (e.g. to Maven's `pom.xml`)
* `ci` - changes to the CI (e.g. to GitHub related configs)
