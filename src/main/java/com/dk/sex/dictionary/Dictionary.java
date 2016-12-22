package com.dk.sex.dictionary;

import java.util.Set;

import com.dk.sex.controller.Terms2Update;

public interface Dictionary {
	public void addRawTerm(String rawStr);
	public void relexiconize();
	public boolean isIncluded(String str);
	public Set<String> getAppliedTermSet();
	public void updateAppliedTermSet(Terms2Update terms2Update);
}
