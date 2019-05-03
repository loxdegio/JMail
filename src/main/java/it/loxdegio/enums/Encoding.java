package it.loxdegio.enums;

public enum Encoding {
	BIT7("7bit"),
	BIT8("8bit"),
	BINARY("binary"),
	BASE64("base64"),
	QUOTED_PRINTABLE("quoted-printable");
	
	private String val;
	
	private Encoding(String encoding) {
		this.val = encoding;
	}

	public String getVal() {
		return val;
	}
	
}
