package generalizing.smt.solutions.fm4se;

import com.microsoft.z3.*;
import java.util.*;

public class CompoundBooleanStrategy implements FormulaBasedGeneralizationStrategy {
    private final SMTConnector connector;

    public CompoundBooleanStrategy(SMTConnector connector) {
        this.connector = connector;
    }

    @Override
    public GeneralizationResult apply(BoolExpr formula, CandidateInvariant candidate) {
        Context ctx = connector.getContext();
        StringBuilder result = new StringBuilder("Compound Boolean Analysis:\n");

        // 1. Extract atomic variables and compound expressions
        Set<String> atomicVars = new HashSet<>();
        Set<BoolExpr> compoundExprs = new HashSet<>();
        extractBooleanStructure(formula, atomicVars, compoundExprs, ctx);

        result.append("\nFormula Structure:\n");
        result.append("  Atomic variables: ").append(String.join(", ", atomicVars)).append("\n");
        result.append("  Compound expressions: ").append(compoundExprs.size()).append("\n");

        // 2. Analyze relationships between atomic and compound expressions
        analyzeCompoundRelationships(formula, atomicVars, compoundExprs, result, ctx);

        return new GeneralizationResult("Compound Boolean Strategy", result.toString());
    }

    private void extractBooleanStructure(BoolExpr expr, Set<String> atomicVars, Set<BoolExpr> compoundExprs, Context ctx) {
        if (expr.isConst()) {
            if (!expr.equals(ctx.mkTrue()) && !expr.equals(ctx.mkFalse())) {
                atomicVars.add(expr.toString());
            }
            return;
        }

        if (expr.isApp()) {
            FuncDecl decl = expr.getFuncDecl();
            if (decl.getRange().equals(ctx.mkBoolSort())) {
                if (decl.getArity() == 0) {
                    atomicVars.add(decl.getName().toString());
                } else {
                    compoundExprs.add(expr);
                }
            }
        }

        for (Expr child : expr.getArgs()) {
            if (child instanceof BoolExpr) {
                extractBooleanStructure((BoolExpr) child, atomicVars, compoundExprs, ctx);
            }
        }
    }

    private void analyzeCompoundRelationships(BoolExpr formula, Set<String> atomicVars, Set<BoolExpr> compoundExprs, StringBuilder result, Context ctx) {
        result.append("\nRelationships between Atomic and Compound Expressions:\n");

        // 1. Analyze implications from atomic to compound
        result.append("\nImplications from Atomic to Compound:\n");
        for (String var : atomicVars) {
            BoolExpr v = ctx.mkBoolConst(var);
            for (BoolExpr comp : compoundExprs) {
                // Check if var → compound
                BoolExpr implies = ctx.mkImplies(v, comp);
                if (isTautology(ctx.mkImplies(formula, implies), ctx)) {
                    result.append("  ").append(var).append(" → ").append(comp).append("\n");
                }
            }
        }

        // 2. Analyze implications from compound to atomic
        result.append("\nImplications from Compound to Atomic:\n");
        for (BoolExpr comp : compoundExprs) {
            for (String var : atomicVars) {
                BoolExpr v = ctx.mkBoolConst(var);
                // Check if compound → var
                BoolExpr implies = ctx.mkImplies(comp, v);
                if (isTautology(ctx.mkImplies(formula, implies), ctx)) {
                    result.append("  ").append(comp).append(" → ").append(var).append("\n");
                }
            }
        }

        // 3. Analyze equivalences between atomic and compound
        result.append("\nEquivalences between Atomic and Compound:\n");
        for (String var : atomicVars) {
            BoolExpr v = ctx.mkBoolConst(var);
            for (BoolExpr comp : compoundExprs) {
                BoolExpr equiv = ctx.mkIff(v, comp);
                if (isTautology(ctx.mkImplies(formula, equiv), ctx)) {
                    result.append("  ").append(var).append(" ↔ ").append(comp).append("\n");
                }
            }
        }

        // 4. Analyze mutual exclusions
        result.append("\nMutual Exclusions:\n");
        for (String var : atomicVars) {
            BoolExpr v = ctx.mkBoolConst(var);
            for (BoolExpr comp : compoundExprs) {
                BoolExpr bothTrue = ctx.mkAnd(v, comp);
                if (isUnsat(ctx.mkAnd(formula, bothTrue), ctx)) {
                    result.append("  ").append(var).append(" ⊕ ").append(comp).append("\n");
                }
            }
        }

        // 5. Analyze relationships between compound expressions
        result.append("\nRelationships between Compound Expressions:\n");
        for (BoolExpr comp1 : compoundExprs) {
            for (BoolExpr comp2 : compoundExprs) {
                if (comp1.equals(comp2)) continue;

                // Check implications
                BoolExpr implies = ctx.mkImplies(comp1, comp2);
                if (isTautology(ctx.mkImplies(formula, implies), ctx)) {
                    result.append("  ").append(comp1).append(" → ").append(comp2).append("\n");
                }

                // Check equivalences
                BoolExpr equiv = ctx.mkIff(comp1, comp2);
                if (isTautology(ctx.mkImplies(formula, equiv), ctx)) {
                    result.append("  ").append(comp1).append(" ↔ ").append(comp2).append("\n");
                }

                // Check mutual exclusions
                BoolExpr bothTrue = ctx.mkAnd(comp1, comp2);
                if (isUnsat(ctx.mkAnd(formula, bothTrue), ctx)) {
                    result.append("  ").append(comp1).append(" ⊕ ").append(comp2).append("\n");
                }
            }
        }
    }

    private boolean isTautology(BoolExpr expr, Context ctx) {
        return isUnsat(ctx.mkNot(expr), ctx);
    }

    private boolean isUnsat(BoolExpr expr, Context ctx) {
        Solver solver = ctx.mkSolver();
        solver.add(expr);
        return solver.check() == Status.UNSATISFIABLE;
    }
} 