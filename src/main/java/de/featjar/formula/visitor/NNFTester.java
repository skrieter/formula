package de.featjar.formula.visitor;

import de.featjar.formula.structure.formula.IFormula;
import de.featjar.formula.structure.formula.connective.IConnective;
import de.featjar.formula.structure.formula.connective.Not;
import de.featjar.formula.structure.formula.predicate.IPredicate;

import java.util.List;

/**
 * Tests whether a formula is in negation normal form.
 * The formula {@code new Not(new Not(new Literal("x")))} is neither in NNF nor in clausal NNF.
 * The formula {@code new Not(new Literal("x"))} is in NNF, but not in clausal NNF.
 * The formula {@code new Literal(false, "x")} is in NNF and in clausal NNF.
 * TODO: is Implies(a, b) in NNF? do we allow complex operators for NNF or not? currently we do.
 */
public class NNFTester extends ANormalFormTester {

    @Override
    public TraversalAction firstVisit(List<IFormula> path) {
        final IFormula formula = getCurrentNode(path);
        if (formula instanceof IPredicate) {
            return TraversalAction.SKIP_CHILDREN;
        } else if (formula instanceof IConnective) {
            if (formula instanceof Not) {
                isClausalNormalForm = false;
                if (!(((Not) formula).getExpression() instanceof IPredicate)) {
                    isNormalForm = false;
                }
            }
            return TraversalAction.CONTINUE;
        } else {
            return TraversalAction.FAIL;
        }
    }
}
