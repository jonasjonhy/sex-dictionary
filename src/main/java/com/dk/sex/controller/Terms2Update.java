package com.dk.sex.controller;

import java.util.Set;

public class Terms2Update {
	private Set<String> terms2Add;
	private Set<String> terms2Del;

	public Set<String> getTerms2Add() {
		return terms2Add;
	}

	public void setTerms(Set<String> terms2Add) {
		this.terms2Add = terms2Add;
	}

	public Set<String> getTerms2Del() {
		return terms2Del;
	}

	public void setTerms2Del(Set<String> terms2Del) {
		this.terms2Del = terms2Del;
	}

	@Override
	public String toString() {
		StringBuilder builder = new StringBuilder();
		builder.append("Terms2Update [terms2Add=").append(terms2Add)
				.append(", terms2Del=").append(terms2Del).append("]");
		return builder.toString();
	}

}
