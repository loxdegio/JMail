package it.loxdegio.enums;

public enum EmailPriority {
	High("1"), Normal("3"), Low("5");
	
	private String val;
	
	private EmailPriority(String priority) {
		this.val = priority;
	}

	public String getVal() {
		return val;
	}
	
}
