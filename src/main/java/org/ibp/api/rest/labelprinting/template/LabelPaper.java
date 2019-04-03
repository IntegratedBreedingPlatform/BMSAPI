
package org.ibp.api.rest.labelprinting.template;

/**
 * The Class LabelPaper.
 *
 * Super class for the label printing paper template to be used
 */
public class LabelPaper {

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
	public LabelPaper(float cellHeight, float marginLeft, float marginRight, float marginTop, float marginBottom, float fontSize,
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
	 * Sets the cell height.
	 *
	 * @param cellHeight the new cell height
	 */
	public void setCellHeight(float cellHeight) {
		this.cellHeight = cellHeight;
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
	 * Sets the margin left.
	 *
	 * @param marginLeft the new margin left
	 */
	public void setMarginLeft(float marginLeft) {
		this.marginLeft = marginLeft;
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
	 * Sets the margin right.
	 *
	 * @param marginRight the new margin right
	 */
	public void setMarginRight(float marginRight) {
		this.marginRight = marginRight;
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
	 * Sets the margin top.
	 *
	 * @param marginTop the new margin top
	 */
	public void setMarginTop(float marginTop) {
		this.marginTop = marginTop;
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
	 * Sets the margin bottom.
	 *
	 * @param marginBottom the new margin bottom
	 */
	public void setMarginBottom(float marginBottom) {
		this.marginBottom = marginBottom;
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
	 * Sets the font size.
	 *
	 * @param fontSize the new font size
	 */
	public void setFontSize(float fontSize) {
		this.fontSize = fontSize;
	}

	/**
	 * Gets the spacing after.
	 *
	 * @return the spacing after
	 */
	public float getSpacingAfter() {
		return this.spacingAfter;
	}

	/**
	 * Sets the spacing after.
	 *
	 * @param spacingAfter the new spacing after
	 */
	public void setSpacingAfter(float spacingAfter) {
		this.spacingAfter = spacingAfter;
	}

}
