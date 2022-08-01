
# SZZ Improved: 

This implementation of the SZZ algorithm builds upon the original algorithm developed by Śliwerski et al's in "When Do Changes Induce Fixes?". I utilise recent suggestions within the surrounding literature to improve the original algorithm and subsequently better the localisation of fix inducing commits.

This project was undertaken for my Undergraduate disseration thesis at Lancaster University (Achieved grade 80%). 




## Improvements

1. Using Apache projects utilising Jira issue tracking to improve the finding of bug fixing commits due to robust and consistent naming conventions.
2. Only considering .Java files for bug introducing suspects.
3. Generates an outlier boundary to ignore bug fixing commits that affect too many files.
4. Ignoring bug fixing and introducing pairs with more than two years between them.
5. Ignoring bug introducing commit suspects that affect comments or blank lines.


## Run SZZ

To run this project on Linux/Mac

```bash
  ./gradlew run --args="GITHUB-URL JIRA-URL JIRA-KEY"
  e.g
  ./gradlew run --args="https://github.com/apache/ace.git https://issues.apache.org/jira/projects/ACE ACE"
```

To run this project on Windows

```bash
  gradlew run --args="GITHUB-URL JIRA-URL JIRA-KEY"
  e.g
  gradlew run --args="https://github.com/apache/ace.git https://issues.apache.org/jira/projects/ACE ACE"
```


## Output


```bash
  All issues, bug fixing commits and bug introducing commits are saved in csv files.
```



Thomas Watkins

Lancaster University
