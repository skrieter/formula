package org.sk.prop4j.structure.compound;

import java.util.*;

import org.sk.prop4j.structure.*;

/**
 * A logical connector that is {@code true} iff the number of its children that
 * are {@code true} is equal to a given number.
 *
 * @author Sebastian Krieter
 */
public class Between extends Cardinal {

	public Between(List<Formula> nodes, int min, int max) {
		super(nodes, min, max);
	}

	private Between(Between oldNode) {
		super(oldNode);
	}

	@Override
	public Between clone() {
		return new Between(this);
	}

	@Override
	public String getName() {
		return "between-" + min + "-" + max;
	}

	@Override
	public void setMin(int min) {
		super.setMin(min);
	}

	@Override
	public void setMax(int max) {
		super.setMax(max);
	}

}
