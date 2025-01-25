# Generalizing SMT Solutions

This repository entails work which was conducted as part of the module **"Formal Methods for Software Engineering"** at the **Bauhaus University Weimar** in the **Winter Semester 2024**.

## Project Brief

Usually, SMT solvers produce single solutions (models) for satisfiable SMT problems. The aim of this project is to generalize single solutions to a meaningful description of multiple solutions. The core tasks entail:

- Define descriptions of multiple solutions,
- Find and implement algorithms to generate descriptions

## Working Notes

**Initial thoughts** on Program components - work in progress

a. retrieve model SMT model declarations and assertions (source from tasks)

b. description engine (plug in values, track results)

c. API + run commands to FM Playground (source from tasks)

**Cases / Distinctions** to keep in mind / develop

a. supported datatypes (boolean, int, ???)

## Project requires local Z3 setup

Note that Z3 is not available as a Maven dependency, so we need to manually add it to the project.

1. Download Z3 from [here](https://github.com/Z3Prover/z3)
2. Update `build.gradle`: gradle implementation files ('path/to/your/com.microsoft.z3.jar'), applicationDefaultJvmArgs = ["-Djava.library.path=path/to/your/z3/bin"]
3. Ensure Z3's native library `libz3.dll` (Windows) is in your system PATH or the specified `java.library.path`

**Questions**

a. what is the required output? -SMT Formula? "representation in a single model like structure"
b. what could be the work around for bounds in Z3(see lecture example https://github.com/Z3Prover/z3/issues/6941)