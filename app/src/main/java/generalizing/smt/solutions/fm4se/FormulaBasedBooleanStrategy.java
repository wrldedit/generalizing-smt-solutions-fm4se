package generalizing.smt.solutions.fm4se;

import com.microsoft.z3.*;
import java.util.*;

public class FormulaBasedBooleanStrategy implements FormulaBasedGeneralizationStrategy {
    private final SMTConnector connector;

    public FormulaBasedBooleanStrategy(SMTConnector connector) {
        this.connector = connector;
    }

    @Override
    public GeneralizationResult apply(BoolExpr formula, CandidateInvariant candidate) {
        Context ctx = connector.getContext();
        StringBuilder result = new StringBuilder("Formula-Based Analysis:\n\n");

        // Extract atomic variables
        Set<String> atomicVars = new HashSet<>();
        extractAtomicVariables(formula, atomicVars, ctx);

        result.append("Atomic Variables: ").append(String.join(", ", atomicVars)).append("\n\n");

        // Analyze implications
        result.append("Logical Implications:\n");
        for (String var1 : atomicVars) {
            BoolExpr v1 = ctx.mkBoolConst(var1);
            for (String var2 : atomicVars) {
                if (var1.equals(var2)) continue;
                BoolExpr v2 = ctx.mkBoolConst(var2);
                
                // Check if var1 -> var2
                BoolExpr implies = ctx.mkImplies(v1, v2);
                if (isTautology(ctx.mkImplies(formula, implies), ctx)) {
                    result.append("  ").append(var1).append(" -> ").append(var2).append("\n");
                }
            }
        }

        // Find equivalences
        result.append("\nEquivalences:\n");
        for (String var1 : atomicVars) {
            for (String var2 : atomicVars) {
                if (var1.compareTo(var2) >= 0) continue;
                
                BoolExpr v1 = ctx.mkBoolConst(var1);
                BoolExpr v2 = ctx.mkBoolConst(var2);
                
                BoolExpr equiv = ctx.mkEq(v1, v2);
                if (isTautology(ctx.mkImplies(formula, equiv), ctx)) {
                    result.append("  ").append(var1).append(" <-> ").append(var2).append("\n");
                }
            }
        }

        // Find mutual exclusions
        result.append("\nMutual Exclusions:\n");
        for (String var1 : atomicVars) {
            for (String var2 : atomicVars) {
                if (var1.compareTo(var2) >= 0) continue;
                
                BoolExpr v1 = ctx.mkBoolConst(var1);
                BoolExpr v2 = ctx.mkBoolConst(var2);
                
                BoolExpr bothTrue = ctx.mkAnd(formula, v1, v2);
                if (isUnsat(bothTrue, ctx)) {
                    result.append("  ").append(var1).append(" XOR ").append(var2).append("\n");
                }
            }
        }

        return new GeneralizationResult("Formula-Based Strategy", result.toString());
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

    private boolean isTautology(BoolExpr expr, Context ctx) {
        return isUnsat(ctx.mkNot(expr), ctx);
    }

    private boolean isUnsat(BoolExpr expr, Context ctx) {
        Solver solver = ctx.mkSolver();
        solver.add(expr);
        return solver.check() == Status.UNSATISFIABLE;
    }
} 
