package GUI;

import javax.swing.JLabel;
import javax.swing.JTextField;

public class NewFeature {
	String importType;
	JLabel[] labelArray;
	JTextField[] textFieldArray;
	
	public NewFeature() {
		this.importType = "";
		this.labelArray = null;
		this.textFieldArray = null;
	}
	
	public NewFeature(String importType, JLabel[] labelArray, JTextField[] textFieldArray) {
		this.importType = importType;
		this.labelArray = labelArray;
		this.textFieldArray = textFieldArray;
	}

	public String getImportType() {
		return importType;
	}

	public void setImportType(String importType) {
		this.importType = importType;
	}

	public JLabel[] getLabelArray() {
		return labelArray;
	}

	public void setLabelArray(JLabel[] labelArray) {
		this.labelArray = labelArray;
	}

	public JTextField[] getTextFieldArray() {
		return textFieldArray;
	}

	public void setTextFieldArray(JTextField[] textFieldArray) {
		this.textFieldArray = textFieldArray;
	}
}
