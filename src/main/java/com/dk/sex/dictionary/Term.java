package com.dk.sex.dictionary;

public class Term {
	private String payload;
	private int refTime;
	private float subTermRefDiffRatio;
	private boolean applied;

	public Term(String payload, int refTime, float subTermRefDiffRatio,
			boolean applied) {
		super();
		this.payload = payload;
		this.refTime = refTime;
		this.subTermRefDiffRatio = subTermRefDiffRatio;
		this.applied = applied;
	}

	public String getPayload() {
		return payload;
	}

	public void setPayload(String payload) {
		this.payload = payload;
	}

	public int getRefTime() {
		return refTime;
	}

	public void setRefTime(int refTime) {
		this.refTime = refTime;
	}

	public float getSubTermRefDiffRatio() {
		return subTermRefDiffRatio;
	}

	public void setSubTermRefDiffRatio(float subTermRefDiffRatio) {
		this.subTermRefDiffRatio = subTermRefDiffRatio;
	}

	public boolean isApplied() {
		return applied;
	}

	public void setApplied(boolean applied) {
		this.applied = applied;
	}

	@Override
	public String toString() {
		StringBuilder builder = new StringBuilder();
		builder.append("Term [payload=").append(payload).append(", refTime=")
				.append(refTime).append(", subTermRefDiffRatio=")
				.append(subTermRefDiffRatio).append(", applied=")
				.append(applied).append("]");
		return builder.toString();
	}
}
