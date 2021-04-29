package org.spldev.formula.expression.transform;

import java.util.*;

import org.spldev.formula.expression.*;
import org.spldev.formula.expression.atomic.*;
import org.spldev.formula.expression.compound.*;
import org.spldev.util.tree.visitor.*;

public class DNFVisitor extends NFVisitor {

	@Override
	public VistorResult firstVisit(List<Expression> path) {
		final Expression node = TreeVisitor.getCurrentNode(path);
		if (node instanceof Or) {
			if (path.size() > 1) {
				isNf = false;
				isClausalNf = false;
				return VistorResult.SkipAll;
			}
			for (final Expression child : node.getChildren()) {
				if (!(child instanceof And)) {
					if (!(child instanceof Atomic)) {
						isNf = false;
						isClausalNf = false;
						return VistorResult.SkipAll;
					}
					isClausalNf = false;
				}
			}
			return VistorResult.Continue;
		} else if (node instanceof And) {
			if (path.size() > 2) {
				isNf = false;
				isClausalNf = false;
				return VistorResult.SkipAll;
			}
			if (path.size() < 2) {
				isClausalNf = false;
			}
			for (final Expression child : node.getChildren()) {
				if (!(child instanceof Atomic)) {
					isNf = false;
					isClausalNf = false;
					return VistorResult.SkipAll;
				}
			}
			return VistorResult.Continue;
		} else if (node instanceof Atomic) {
			if (path.size() > 3) {
				isNf = false;
				isClausalNf = false;
				return VistorResult.SkipAll;
			}
			if (path.size() < 3) {
				isClausalNf = false;
			}
			return VistorResult.SkipChildren;
		} else {
			isNf = false;
			isClausalNf = false;
			return VistorResult.SkipAll;
		}
	}

}
