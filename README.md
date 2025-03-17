# Generalizing SMT Solutions

This repository implements a framework for generalizing SMT solutions.

*This work is conducted in partial fulfillment of the module **"Formal Methods for Software Engineering"** at the **Bauhaus University Weimar** (Winter Semester 2024).*

## Overview

SMT solvers typically produce single solutions (models) for satisfiable SMT problems. The aim of this project is to generalize single solutions to a meaningful description of multiple solutions. To this end, this project:

1) Develops meaningful descriptions of multiple solutions / generalizations,
2) Finds and implements algorithms to verify whether generalizations hold for a given formula,
3) Tests the algorithms with a comprehensive test suite,
4) Provides interactive analysis tools for exploring formula properties

## Implementation Roadmap Checklist

- [x] Z3 Setup and Integration
- [x] Model Enumeration
- [x] First Set of Descriptions
  - [x] Boolean (Always, Never True) Verification
  - [x] Interval Verification
- [x] Description Test Cases
  - [x] Fixed Value Test Cases
  - [x] Boolean Test Cases
- [x] User Interaction  
- [x] Interval Detection
  - [x] Naive / Brute Force
  - [x] Binary Search
- [x] Further Testing
  - [x] Interval Testing
  - [x] Performance Testing

## Project Structure

```
app/src/main/java/generalizing/smt/solutions/fm4se/
├── App.java                    # Main application class with examples
├── SMTConnector.java           # Z3 solver interface
├── InteractiveAnalyzer.java    # Interactive analysis session
└── strategies/
    ├── bool/                   # Boolean analysis strategies
    │   ├── BooleanStrategy.java
    │   ├── ModelBasedBooleanStrategy.java
    │   └── FormulaBasedBooleanStrategy.java
    └── integer/                # Integer analysis strategies
        ├── IntegerStrategy.java
        ├── NaiveIntegerStrategy.java
        └── BinarySearchIntegerStrategy.java
```

## Features

### Boolean Analysis
- **Model-Based Analysis**: Examines actual solutions to find patterns
- **Formula-Based Analysis**: Analyzes logical structure to identify relationships
- **Combined Analysis**: Uses both approaches for comprehensive results
- Detects:
  - Fixed values (variables that are always true/false)
  - Implications between variables

### Integer Analysis
- **Naive Strategy**: Linear expansion
- **Binary Search Strategy**: Exponential expansion + Binary Search
- Detects:
  - Variable bounds


## Project Setup

*Note that Z3 is not available as a Maven dependency, so we need to manually add it to the project.*

1. Download Z3 from [here](https://github.com/Z3Prover/z3)
2. Create a `libs` directory in the `app` folder and copy:
   - `com.microsoft.z3.jar` from Z3's Java bindings
   - `libz3.dll` (Windows) or equivalent for your OS
3. The project uses Gradle with the following key configurations:
   ```gradle
   // Dependencies
   implementation 'org.sosy-lab:java-smt:3.14.3'
   implementation files('libs/com.microsoft.z3.jar')
   
   // Java version
   java {
       toolchain {
           languageVersion = JavaLanguageVersion.of(17)
       }
   }
   
   // Native library handling
   systemProperty 'java.library.path', "${projectDir}/libs"
   ```

The project automatically handles native library copying and path configuration through Gradle tasks.

## To Dos

1. Is there a desired output format?
   - SMT Formula vs. "representation in a single model-like structure"
   - Need to clarify the preferred format for generalized solutions

2. What are the workarounds for bounds in Z3?
   - Reference: Lecture example and [Z3 issue #6941](https://github.com/Z3Prover/z3/issues/6941)
   - Need to investigate handling of bounded variables and domains

3. What are other interesting generalizations?
   - How to navigate the model / solution space?
   - Current implementation includes implications and range relationships
   - Future work could include more complex patterns

4. What about performance?
   - Model Based vs Formula Based
   - Linear vs Binary Search for integer analysis
   - Current implementation shows binary search is more efficient for large ranges
