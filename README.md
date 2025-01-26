# Generalizing SMT Solutions

*..to be extended..*

*This repository entails work which was conducted as part of the module **"Formal Methods for Software Engineering"** at the **Bauhaus University Weimar** in the **Winter Semester 2024***

## Project Brief

*..to be extended..*

*Usually, SMT solvers produce single solutions (models) for satisfiable SMT problems. The aim of this project is to generalize single solutions to a meaningful description of multiple solutions. The core tasks entail:*

*- Define descriptions of multiple solutions,*
*- Find and implement algorithms to generate descriptions,*

## Project Structure and Roadmap

### Stage 1: Setup and Core Components

- [x] Project Environment Setup with Z3
- [x] Input Processing
  - SMT-LIB format parser
  - Optional: User-friendly input format (JSON/DSL)
- [x] Z3 Integration
  - Formula execution
  - Solution retrieval
- [ ] Basic Generalization Algorithms
  - Boolean patterns (Always, Never, Don't Care)
  - Numeric interval detection
- [ ] Output Formatting
  - SMT-compatible formulas
  - Human-readable summaries

### Stage 2: Advanced Features

- [ ] Conditional Generalizations
  - ONLY IF / ALWAYS IF relationships
  - Mixed constraint handling
- [ ] Enhanced Interval Processing
  - Multi-dimensional intervals
  - Variable relationship detection
- [ ] Pattern Recognition
  - Solution clustering
  - Symmetry detection

### Stage 3: Refinement

- [ ] Edge Cases and Error Handling
- [ ] Comprehensive Testing Suite
- [ ] Documentation and Examples

### Other

- [ ] Develop Test Cases
  - [ ] Class methods
  - [ ] Generalization algorithms

## Project Structure

├── main/
│ ├── java/
│ │ ├── parser/ # SMT-LIB parsing
│ │ ├── solver/ # Z3 integration
│ │ ├── generalizer/ # Solution generalization
│ │ └── output/ # Result formatting
│ └── resources/
│ └── examples/ # Example SMT problems
└── test/
└── java/ # Test cases

## Project Setup

*Note that Z3 is not available as a Maven dependency, so we need to manually add it to the project.*

1. Download Z3 from [here](https://github.com/Z3Prover/z3)
2. Update `build.gradle`: gradle implementation files ('path/to/your/com.microsoft.z3.jar'), applicationDefaultJvmArgs = ["-Djava.library.path=path/to/your/z3/bin"]
3. Ensure Z3's native library `libz3.dll` (Windows) is in your system PATH or the specified `java.library.path`
*to be extended*

**Questions**
a. what is the required output? -SMT Formula? "representation in a single model like structure"
b. **what could be the work around for bounds in Z3**(see lecture example https://github.com/Z3Prover/z3/issues/6941)