# Generalizing SMT Solutions

This repository implements a framework for generalizing SMT solutions.

*--to be extended--*

*This work is conducted in partial fulfillment of the module **"Formal Methods for Software Engineering"** at the **Bauhaus University Weimar** (Winter Semester 2024).*

## Overview

SMT solvers typically produce single solutions (models) for satisfiable SMT problems. The aim of this project is to generalize single solutions to a meaningful description of multiple solutions. To this end, this project:

1) Develops meaningful descriptions of multiple solutions / generalizations,
2) Finds and implements algorithms to verify whether generalizations hold for a given formula,
3) Tests the algorithms with a comprehensive test suite,
4) ...

## Implementation Roadmap Checklist

- [x] Z3 Setup and Integration
- [x] Model Enumeration
- [x] First Set of Descriptions
  - [x] Fixed Value Verification
  - [x] Boolean (Always, Never True) Verification
  - [x] Interval Verification
- [ ] Description Test Cases
  - [ ] Fixed Value Test Cases
  - [ ] Boolean Test Cases
  - [ ] Interval Test Cases
- [ ] Second Set of Descriptions
  - [ ] Conditional Invariants
  - [ ] ...
- [ ] Second Set of Test Cases

## Project Structure

smt-generalization/
├── src/
│   └── main/java/com/example/smt
│      ├── SMTConnector.java                          // Manages the Z3 context and formula checks
│      ├── InteractiveAnalyzer.java                   // Interactive interface for candidate invariant testing
│      ├── Main.java                                  // Application entry point
│      ├── ModelEnumerator.java                       // (Optional) Enumerates models for user intuition
│      ├── GeneralizationResult.java                  // Data structure for invariant check results
│      ├── FormulaBasedGeneralizationStrategy.java    // Interface for formula-based strategies
│      ├── CandidateInvariant.java                    // Encapsulates candidate invariants and produces negation formulas
│      ├── FixedValueFormulaStrategy.java             // Verifies fixed value invariants
│      ├── AlwaysTrueFalseFormulaStrategy.java        // Verifies Boolean invariants
│      └── IntervalFormulaStrategy.java               // Verifies interval invariants
└── test/
    └── java/                                         // Test cases


## Project Setup

*Note that Z3 is not available as a Maven dependency, so we need to manually add it to the project.*

1. Download Z3 from [here](https://github.com/Z3Prover/z3)
2. Update `build.gradle`: gradle implementation files ('path/to/your/com.microsoft.z3.jar'), applicationDefaultJvmArgs = ["-Djava.library.path=path/to/your/z3/bin"]
3. Ensure Z3's native library `libz3.dll` (Windows) is in your system PATH or the specified `java.library.path`
*to be extended*

## Questions and Current Status

1. Is there a desired output format?
   - SMT Formula vs. "representation in a single model-like structure"
   - Need to clarify the preferred format for generalized solutions

2. What are the workarounds for bounds in Z3?
   - Reference: Lecture example and [Z3 issue #6941](https://github.com/Z3Prover/z3/issues/6941)
   - Need to investigate handling of bounded variables and domains

3. What are other interesting generalizations?
   - How to navigate the model / solution space?

4. What about performance?
   - Model Based vs Formula Based
   - ...

[Class Diagram to be inserted here]