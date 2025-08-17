package org.ainslec.picopica;

public enum LanguageMode {
	
	/** Default is c-like by default, but not supporting multi line comment style to house directives */
	DEFAULT (true /* isParseCLikeMultiLineComments*/ , false /* isParseLeadingSingleHash */, false /* isParseLeadingDoubleHyphen */, true /* isSupportsHtmlBlockComment */),
	
	/** Same as default, but more explicit */
	JAVA (true /* isParseCLikeMultiLineComments*/, false /* isParseLeadingSingleHash */, false /* isParseLeadingDoubleHyphen */, true /* isSupportsHtmlBlockComment */),
	
	/** Same as default, but more explicit */
	C_PLUS_PLUS (true /* isParseCLikeMultiLineComments*/, false /* isParseLeadingSingleHash */, false /* isParseLeadingDoubleHyphen */, true /* isSupportsHtmlBlockComment */),
	
	/** Same as default, but more explicit */
	C_SHARP (true /* isParseCLikeMultiLineComments*/, false /* isParseLeadingSingleHash */, false /* isParseLeadingDoubleHyphen */, true /* isSupportsHtmlBlockComment */),
	
	/** Python doesn't allow directives to span lines, and can only be contained on lines starting with # */
	PYTHON (false /* isParseCLikeMultiLineComments*/, true /* isParseLeadingSingleHash */, false /* isParseLeadingDoubleHyphen */, false /* isSupportsHtmlBlockComment */),
	
	/** SQL doesn't allow directives to span lines, and can only be contained on lines starting with # */
	SQL (true /* isParseCLikeMultiLineComments*/, false /* isParseLeadingSingleHash */, true /* isParseLeadingDoubleHyphen */, false /* isSupportsHtmlBlockComment */);
	
	private boolean parseLeadingSingleHash;
	private boolean parseLeadingDoubleHyphen;
	private boolean parseCLikeMultiLineComments;
	private boolean parseHtmlBlockComments;
	
	private LanguageMode(boolean parseCLikeMultiLineComments, boolean parseLeadingSingleHash, boolean parseLeadingDoubleHyphen, boolean parseHtmlBlockComments) {
		this.parseLeadingSingleHash = parseLeadingSingleHash;
		this.parseLeadingDoubleHyphen = parseLeadingDoubleHyphen;
		this.parseCLikeMultiLineComments = parseCLikeMultiLineComments;
		this.parseHtmlBlockComments = parseHtmlBlockComments;
	}
	
	
	public boolean isParseLeadingDoubleHyphen() {
		return parseLeadingDoubleHyphen;
	}
	
	public boolean isParseLeadingSingleHash() {
		return parseLeadingSingleHash;
	}
	
	public boolean isParseCLikeMultiLineComments() {
		return parseCLikeMultiLineComments;
	}
	
	public boolean isParseHtmlBlockComments() {
		return parseHtmlBlockComments;
	}
}
