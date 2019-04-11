
package org.ibp.api.rest.labelprinting.template;

/**
 * The Class LabelPaper.
 *
 * Super class for the label printing paper template to be used
 */
public enum LabelPaper {

	PAPER_3_BY_7_A4(108f, 15, 0, 42, 5, 6.8f, 0f),
	PAPER_3_BY_8_A4(97f, 15, 0, 37, 5, 6.8f, 0f),
	PAPER_3_BY_10_A4(72.5f, 6, 2, 17.5f, 5, 4.8f, 9f),
	PAPER_3_BY_7_LETTER(108f, 10, 0, 17, 5, 6.8f, 0f),
	PAPER_3_BY_8_LETTER(98.1f, 5, 0, 0, 5, 6.8f, 0f),
	PAPER_3_BY_10_LETTER(72.5f, 2, 2, 33.3f, 5, 4.8f, 0f);
	/** The cell height. */
	private float cellHeight;

	/** The margin left. */
	private float marginLeft;

	/** The margin right. */
	private float marginRight;

	/** The margin top. */
	private float marginTop;

	/** The margin bottom. */
	private float marginBottom;

	/** The font size. */
	private float fontSize;

	/** The spacing after. */
	private float spacingAfter;

	/**
	 * Instantiates a new label paper.
	 *
	 * @param cellHeight the cell height
	 * @param marginLeft the margin left
	 * @param marginRight the margin right
	 * @param marginTop the margin top
	 * @param marginBottom the margin bottom
	 * @param fontSize the font size
	 * @param spacingAfter the spacing after
	 */
	LabelPaper(float cellHeight, float marginLeft, float marginRight, float marginTop, float marginBottom, float fontSize,
			float spacingAfter) {
		this.cellHeight = cellHeight;
		this.marginLeft = marginLeft;
		this.marginRight = marginRight;
		this.marginTop = marginTop;
		this.marginBottom = marginBottom;
		this.fontSize = fontSize;
		this.spacingAfter = spacingAfter;
	}

	/**
	 * Gets the cell height.
	 *
	 * @return the cell height
	 */
	public float getCellHeight() {
		return this.cellHeight;
	}

	/**
	 * Gets the margin left.
	 *
	 * @return the margin left
	 */
	public float getMarginLeft() {
		return this.marginLeft;
	}

	/**
	 * Gets the margin right.
	 *
	 * @return the margin right
	 */
	public float getMarginRight() {
		return this.marginRight;
	}

	/**
	 * Gets the margin top.
	 *
	 * @return the margin top
	 */
	public float getMarginTop() {
		return this.marginTop;
	}

	/**
	 * Gets the margin bottom.
	 *
	 * @return the margin bottom
	 */
	public float getMarginBottom() {
		return this.marginBottom;
	}

	/**
	 * Gets the font size.
	 *
	 * @return the font size
	 */
	public float getFontSize() {
		return this.fontSize;
	}

	/**
	 * Gets the spacing after.
	 *
	 * @return the spacing after
	 */
	public float getSpacingAfter() {
		return this.spacingAfter;
	}

}
