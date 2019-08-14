/*
 * 12/17/2010
 *
 * SortByRelevanceComparator.java - Sorts two Completions by relevance before
 * sorting them lexicographically.
 * 
 * This library is distributed under a modified BSD license.  See the included
 * AutoComplete.License.txt file for details.
 */
package org.fife.ui.autocomplete;

import java.util.Comparator;


/**
 * Compares two <code>Completion</code>s by their relevance before
 * sorting them lexicographically.
 *
 * @author Robert Futrell
 * @version 1.0
 */
public class SortByRelevanceComparator implements Comparator<Completion> {


	/*@Override
	public int compare(Completion c1, Completion c2) {
		int rel1 = c1.getRelevance();
		int rel2 = c2.getRelevance();
		int diff = rel2 - rel1;//rel1 - rel2;
		return diff==0 ? c1.compareTo(c2) : diff;
	}  */
	@Override
	public int compare(Completion c1, Completion c2) {
		String str1 = c1.getInputText();
		String str2 = c2.getInputText();
		String text =  c1.getAlreadyEntered(AutoCompletion.jTextComponent);
		float f1 = Util.getSimIndex(str1.toUpperCase(), text.toUpperCase());
		float f2 = Util.getSimIndex(str2.toUpperCase(), text.toUpperCase());
		if(f1==f2) return str1.length() -   str2.length();
		return (f1-f2)>=0 ? -1 : 1;
	}

}