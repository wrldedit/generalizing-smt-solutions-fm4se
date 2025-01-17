# Generalizing SMT Solutions

This repository entails work which was conducted as part of the module "Formal Methods for Software Engineering" at the Bauhaus University Weimar in the Winter Semester 2024.

## Project Brief

Usually, SMT solvers produce single solutions (models) for satisfiable SMT problems. The aim of this project is to generalize single solutions to a meaningful description of multiple solutions. The core tasks entail:

- Define descriptions of multiple solutions,
- Find and implement algorithms to generate descriptions

## Project Scope

Initial thoughts on Program components - work in progress

a. retrieve model SMT model declarations and assertions (source from tasks)

b. description engine (plug in values, track results)

c. API + run commands to FM Playground (source from tasks)

Distinctions to keep in mind / develop

a. supported datatypes (boolean, int, ???)

Questions

a. what is the required output? -SMT Formula? "representation in a single model like structure"
b. what could be the work around for bounds in Z3(see lecture example https://github.com/Z3Prover/z3/issues/6941)

**Submission**: Submit each permalink in- [src/main/java/de/buw/fm4se/modelchecking/task/Tasks.java (task_1a-c)](src/main/java/de/buw/fm4se/modelchecking/task/Tasks.java)