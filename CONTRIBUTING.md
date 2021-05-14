# How to Contribute

This is a quick and simple guide on how work should be contributed to this project.

## Basic Steps
1. Clone, or fork, the project
2. Create your feature branch (`git checkout -b feature/foobar`)
3. Commit your changes (`git commit -m 'feat(*): add more bar to foo'`)
4. Push to the branch (`git push origin feature/foobar`)
5. Create a new merge request when work is complete

### Coding Standards
* Code formatting should follow google style
* Test Driven Development should be adhered to. Any code with pointless tests,
failing tests, or sub-par code coverage is likely to result in code being
rejected (https://www.agilealliance.org/glossary/tdd/)
* Code is expected to be clean, readable and following SOLID principles

### Commit Messages
* All commits must be signed-off by the author (use `name <email-address>` format)
* Use conventional commit format to describe what the commit is intended to do
(see https://www.conventionalcommits.org/en/v1.0.0/)

## Branch Strategy
The project follows Git Flow. Conventionally branches are prefixed with 'F - '
followed by either a ticket number or a very brief description of the change

## Merging a branch

First make sure your branch meets the required standards by running a site report
(`mvn clean install site`) as merge requests for a failing branch are likely to
be rejected.  

Check the "Project Reports" section of the site report and make sure that each
section is clear of errors, warnings, bugs, etc.

Tools like SPOTBUGS and PMD will identify an exhaustive list of non-breaking
issues that are still considered serious enough to prevent a merge being accepted.

Some of these issues are going to be unavoidable (e.g. Law of Demeter) and must
be manually excluded and this is acceptable but blanket rule exclusions will be
rejected.

## Reporting a Problem
If you find an issue with the project and wish to report it, the more information
you can give, the easier it is to find the cause.

Try to include all of these:

* A clear, brief description of the problem
* What was the sequence of events to cause the problem
* If you've witnessed an error message, include the contents
* What steps should be taken to reproduce the problem

If you are having problems, contact the owning team using the email addresses in
the readme
