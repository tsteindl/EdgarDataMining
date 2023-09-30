# FORM4DataRetrieval

## How it works
Parse JSON from FTP server: 
Visit: https://www.sec.gov/Archives/edgar/daily-index/index.json which gives tree structure to navigate the daily index files.

Recursively traverse tree from index.json files until .idx files are found. They contain the daily indices of submitted FORM 4s (and all other forms). 

Create dictionary/HashMap to assign ArrayList of all found DailyData objects to form type (as of now only Form 4 mapped to decrease memory overhead).

DailyData objects include formType, companyName, CIK, dateFiled, folderPath to Form and fileName. Some examples of form can be looked at in data\forms directory.

Stride the ArrayList for all DailyData objects and visit the url from folderPath. Download this file and create XML from it.

Parse XML using XML-tags from the metatable and use this data to create CSV file.

## Example input
- /edgar/daily-index/2022/QTR1/ -conc=false
- -files=edgar/data/1708176/0001209191-22-020174.txt -conc=false

## Details on how to access EDGAR data
https://www.sec.gov/os/accessing-edgar-data

## Technologies used
- Generic class hierarchies including interfaces and abstract classes enabling building on this implementation (dependency injection)
- Accessing remote FTP server using REST/HTTP requests
- Recursion over Directory tree and scraping data
- Processing strings with Java (parallel) Streams
- Using utility libraries
- Multithreading/Concurrency to attain speedup of x of my first implementation
- FileIO
- JSON/XML Parsing
- Reflection
- Generics (Table)
- Guava
- Building a Parser from XML schema
- implemented Iterable
## Run from terminal

/home/tsteindl/.jdks/openjdk-18.0.2.1/bin/java -javaagent:/snap/intellij-idea-ultimate/451/lib/idea_rt.jar=38695:/snap/intellij-idea-ultimate/451/bin -Dfile.encoding=UTF-8 -Dsun.stdout.encoding=UTF-8 -Dsun.stderr.encoding=UTF-8 -classpath "/home/tsteindl/Documents/Projects/EDGAR Data Mining/target/classes:/home/tsteindl/.m2/repository/com/opencsv/opencsv/5.5.2/opencsv-5.5.2.jar:/home/tsteindl/.m2/repository/org/apache/commons/commons-text/1.9/commons-text-1.9.jar:/home/tsteindl/.m2/repository/commons-beanutils/commons-beanutils/1.9.4/commons-beanutils-1.9.4.jar:/home/tsteindl/.m2/repository/commons-collections/commons-collections/3.2.2/commons-collections-3.2.2.jar:/home/tsteindl/.m2/repository/org/apache/commons/commons-collections4/4.4/commons-collections4-4.4.jar:/home/tsteindl/.m2/repository/org/openjdk/jmh/jmh-core/1.37/jmh-core-1.37.jar:/home/tsteindl/.m2/repository/net/sf/jopt-simple/jopt-simple/5.0.4/jopt-simple-5.0.4.jar:/home/tsteindl/.m2/repository/org/apache/commons/commons-math3/3.6.1/commons-math3-3.6.1.jar:/home/tsteindl/.m2/repository/org/openjdk/jmh/jmh-generator-annprocess/1.37/jmh-generator-annprocess-1.37.jar:/home/tsteindl/.m2/repository/com/google/code/gson/gson/2.8.9/gson-2.8.9.jar:/home/tsteindl/.m2/repository/org/apache/commons/commons-lang3/3.12.0/commons-lang3-3.12.0.jar:/home/tsteindl/.m2/repository/com/google/guava/guava/31.1-jre/guava-31.1-jre.jar:/home/tsteindl/.m2/repository/com/google/guava/failureaccess/1.0.1/failureaccess-1.0.1.jar:/home/tsteindl/.m2/repository/com/google/guava/listenablefuture/9999.0-empty-to-avoid-conflict-with-guava/listenablefuture-9999.0-empty-to-avoid-conflict-with-guava.jar:/home/tsteindl/.m2/repository/com/google/code/findbugs/jsr305/3.0.2/jsr305-3.0.2.jar:/home/tsteindl/.m2/repository/org/checkerframework/checker-qual/3.12.0/checker-qual-3.12.0.jar:/home/tsteindl/.m2/repository/com/google/errorprone/error_prone_annotations/2.11.0/error_prone_annotations-2.11.0.jar:/home/tsteindl/.m2/repository/com/google/j2objc/j2objc-annotations/1.3/j2objc-annotations-1.3.jar:/home/tsteindl/.m2/repository/org/postgresql/postgresql/42.6.0/postgresql-42.6.0.jar:/home/tsteindl/.m2/repository/org/apache/httpcomponents/httpclient/4.5.13/httpclient-4.5.13.jar:/home/tsteindl/.m2/repository/org/apache/httpcomponents/httpcore/4.4.13/httpcore-4.4.13.jar:/home/tsteindl/.m2/repository/commons-logging/commons-logging/1.2/commons-logging-1.2.jar:/home/tsteindl/.m2/repository/commons-codec/commons-codec/1.11/commons-codec-1.11.jar:/home/tsteindl/.m2/repository/com/fasterxml/jackson/core/jackson-databind/2.13.3/jackson-databind-2.13.3.jar:/home/tsteindl/.m2/repository/com/fasterxml/jackson/core/jackson-annotations/2.13.3/jackson-annotations-2.13.3.jar:/home/tsteindl/.m2/repository/com/fasterxml/jackson/core/jackson-core/2.13.3/jackson-core-2.13.3.jar:/home/tsteindl/.m2/repository/com/fasterxml/jackson/datatype/jackson-datatype-jsr310/2.13.4/jackson-datatype-jsr310-2.13.4.jar" Main -path=2022/QTR3/ -conc=true -output=db

