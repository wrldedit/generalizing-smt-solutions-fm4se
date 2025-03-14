package generalizing.smt.solutions.fm4se;

import com.microsoft.z3.*;
import java.util.*;

public class AtomicBooleanStrategy implements FormulaBasedGeneralizationStrategy {
    private final SMTConnector connector;

    public AtomicBooleanStrategy(SMTConnector connector) {
        this.connector = connector;
    }

    @Override
    public GeneralizationResult apply(BoolExpr formula, CandidateInvariant candidate) {
        Context ctx = connector.getContext();
        StringBuilder result = new StringBuilder("Atomic Boolean Analysis:\n");

        // 1. Extract atomic variables
        Set<String> atomicVars = new HashSet<>();
        extractAtomicVariables(formula, atomicVars, ctx);

        result.append("\nAtomic Variables: ").append(String.join(", ", atomicVars)).append("\n");

        // 2. Analyze relationships between atomic variables
        analyzeAtomicRelationships(formula, atomicVars, result, ctx);

        return new GeneralizationResult("Atomic Boolean Strategy", result.toString());
    }

    private void extractAtomicVariables(BoolExpr expr, Set<String> atomicVars, Context ctx) {
        if (expr.isConst()) {
            if (!expr.equals(ctx.mkTrue()) && !expr.equals(ctx.mkFalse())) {
                atomicVars.add(expr.toString());
            }
            return;
        }

        if (expr.isApp()) {
            FuncDecl decl = expr.getFuncDecl();
            if (decl.getRange().equals(ctx.mkBoolSort()) && decl.getArity() == 0) {
                atomicVars.add(decl.getName().toString());
            }
        }

        for (Expr child : expr.getArgs()) {
            if (child instanceof BoolExpr) {
                extractAtomicVariables((BoolExpr) child, atomicVars, ctx);
            }
        }
    }

    private void analyzeAtomicRelationships(BoolExpr formula, Set<String> atomicVars, StringBuilder result, Context ctx) {
        result.append("\nRelationships between Atomic Variables:\n");

        // Check implications
        result.append("\nImplications:\n");
        for (String var1 : atomicVars) {
            for (String var2 : atomicVars) {
                if (var1.equals(var2)) continue;

                BoolExpr v1 = ctx.mkBoolConst(var1);
                BoolExpr v2 = ctx.mkBoolConst(var2);

                // Check if var1 → var2
                BoolExpr implies = ctx.mkImplies(v1, v2);
                if (isTautology(ctx.mkImplies(formula, implies), ctx)) {
                    result.append("  ").append(var1).append(" → ").append(var2).append("\n");
                }
            }
        }

        // Check equivalences
        result.append("\nEquivalences:\n");
        for (String var1 : atomicVars) {
            for (String var2 : atomicVars) {
                if (var1.equals(var2)) continue;

                BoolExpr v1 = ctx.mkBoolConst(var1);
                BoolExpr v2 = ctx.mkBoolConst(var2);

                BoolExpr equiv = ctx.mkIff(v1, v2);
                if (isTautology(ctx.mkImplies(formula, equiv), ctx)) {
                    result.append("  ").append(var1).append(" ↔ ").append(var2).append("\n");
                }
            }
        }

        // Check mutual exclusions
        result.append("\nMutual Exclusions:\n");
        for (String var1 : atomicVars) {
            for (String var2 : atomicVars) {
                if (var1.equals(var2)) continue;

                BoolExpr v1 = ctx.mkBoolConst(var1);
                BoolExpr v2 = ctx.mkBoolConst(var2);

                BoolExpr bothTrue = ctx.mkAnd(v1, v2);
                if (isUnsat(ctx.mkAnd(formula, bothTrue), ctx)) {
                    result.append("  ").append(var1).append(" ⊕ ").append(var2).append("\n");
                }
            }
        }

        // Check constant values
        result.append("\nConstant Values:\n");
        for (String var : atomicVars) {
            BoolExpr v = ctx.mkBoolConst(var);
            
            // Check if always true
            if (isUnsat(ctx.mkAnd(formula, ctx.mkNot(v)), ctx)) {
                result.append("  ").append(var).append(" is always true\n");
            }
            
            // Check if always false
            if (isUnsat(ctx.mkAnd(formula, v), ctx)) {
                result.append("  ").append(var).append(" is always false\n");
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