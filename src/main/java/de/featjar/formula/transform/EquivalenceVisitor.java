/*
 * Copyright (C) 2022 Sebastian Krieter, Elias Kuiter
 *
 * This file is part of formula.
 *
 * formula is free software: you can redistribute it and/or modify it
 * under the terms of the GNU Lesser General Public License as published by
 * the Free Software Foundation, either version 3.0 of the License,
 * or (at your option) any later version.
 *
 * formula is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.
 * See the GNU Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with formula. If not, see <https://www.gnu.org/licenses/>.
 *
 * See <https://github.com/FeatureIDE/FeatJAR-formula> for further information.
 */
package de.featjar.formula.transform;

import de.featjar.formula.structure.AuxiliaryRoot;
import de.featjar.formula.structure.Formula;
import de.featjar.formula.structure.formula.Predicate;
import de.featjar.formula.tmp.TermMap.Variable;
import de.featjar.formula.structure.formula.connective.And;
import de.featjar.formula.structure.formula.connective.AtLeast;
import de.featjar.formula.structure.formula.connective.AtMost;
import de.featjar.formula.structure.formula.connective.Between;
import de.featjar.formula.structure.formula.connective.BiImplies;
import de.featjar.formula.structure.formula.connective.Choose;
import de.featjar.formula.structure.formula.connective.Connective;
import de.featjar.formula.structure.formula.connective.Implies;
import de.featjar.formula.structure.formula.connective.Not;
import de.featjar.formula.structure.formula.connective.Or;
import de.featjar.formula.structure.formula.connective.Quantifier;
import de.featjar.base.tree.visitor.TreeVisitor;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

public class EquivalenceVisitor implements TreeVisitor<Void, Formula> {

    private boolean fail;

    @Override
    public void reset() {
        fail = false;
    }

    @Override
    public TraversalAction firstVisit(List<Formula> path) {
        final Formula formula = getCurrentNode(path);
        if (formula instanceof Predicate) {
            return TraversalAction.SKIP_CHILDREN;
        } else if (formula instanceof Connective) {
            if (formula instanceof Quantifier) {
                return TraversalAction.FAIL;
            }
            return TraversalAction.CONTINUE;
        } else if (formula instanceof AuxiliaryRoot) {
            return TraversalAction.CONTINUE;
        } else {
            return TraversalAction.FAIL;
        }
    }

    @Override
    public TraversalAction lastVisit(List<Formula> path) {
        final Formula formula = getCurrentNode(path);
        formula.replaceChildren(this::replace);
        if (fail) {
            return TraversalAction.FAIL;
        }
        return TraversalAction.CONTINUE;
    }

    @SuppressWarnings("unchecked")
    private Formula replace(Formula formula) {
        if (((formula instanceof Variable)
                || (formula instanceof Predicate)
                || (formula instanceof And)
                || (formula instanceof Or)
                || (formula instanceof Not))) {
            return null;
        }
        final List<Formula> children = (List<Formula>) formula.getChildren();
        Formula newFormula;
        if (formula instanceof Implies) {
            newFormula = new Or(new Not(children.get(0)), children.get(1));
        } else if (formula instanceof BiImplies) {
            newFormula = new And( //
                    new Or(new Not(children.get(0)), children.get(1)),
                    new Or(new Not(children.get(1)), children.get(0)));
        } else if (formula instanceof AtLeast) {
            newFormula = new And(atLeastK(children, ((AtLeast) formula).getMinimum()));
        } else if (formula instanceof AtMost) {
            newFormula = new And(atMostK(children, ((AtMost) formula).getMaximum()));
        } else if (formula instanceof Between) {
            final Between between = (Between) formula;
            newFormula = new And(
                    new And(atLeastK(children, between.getMinimum())), new And(atMostK(children, between.getMaximum())));
        } else if (formula instanceof Choose) {
            final Choose choose = (Choose) formula;
            newFormula = new And(new And(atLeastK(children, choose.getK())), new And(atMostK(children, choose.getK())));
        } else {
            fail = true;
            return null;
        }
        return newFormula;
    }

    private List<Formula> atMostK(List<? extends Formula> elements, int k) {
        final int n = elements.size();

        // return tautology
        if (k <= 0) {
            return Arrays.asList(Formula.FALSE);
        }

        // return contradiction
        if (k > n) {
            return Arrays.asList(Formula.TRUE);
        }

        return groupElements(elements.stream().map(Not::new).collect(Collectors.toList()), k, n);
    }

    private List<Formula> atLeastK(List<? extends Formula> elements, int k) {
        final int n = elements.size();

        // return tautology
        if (k <= 0) {
            return Arrays.asList(Formula.TRUE);
        }

        // return contradiction
        if (k > n) {
            return Arrays.asList(Formula.FALSE);
        }

        return groupElements(elements, n - k, n);
    }

    private List<Formula> groupElements(List<? extends Formula> elements, int k, final int n) {
        final List<Formula> groupedElements = new ArrayList<>();
        final Formula[] clause = new Formula[k + 1];
        final int[] index = new int[k + 1];

        // the position that is currently filled in clause
        int level = 0;
        index[level] = -1;

        while (level >= 0) {
            // fill this level with the next element
            index[level]++;
            // did we reach the maximum for this level
            if (index[level] >= (n - (k - level))) {
                // go to previous level
                level--;
            } else {
                clause[level] = elements.get(index[level]);
                if (level == k) {
                    final Formula[] clonedClause = new Formula[clause.length];
                    Arrays.copyOf(clause, clause.length);
                    for (int i = 0; i < clause.length; i++) {
                        //						clonedClause[i] = Trees.cloneTree(clause[i]);
                        clonedClause[i] = clause[i];
                    }
                    groupedElements.add(new Or(clonedClause));
                } else {
                    // go to next level
                    level++;
                    // allow only ascending orders (to prevent from duplicates)
                    index[level] = index[level - 1];
                }
            }
        }
        return groupedElements;
    }
}
