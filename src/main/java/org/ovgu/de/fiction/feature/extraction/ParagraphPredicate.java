package org.ovgu.de.fiction.feature.extraction;

import java.util.function.Predicate;

import org.ovgu.de.fiction.model.Word;
import org.ovgu.de.fiction.utils.FRConstants;

public class ParagraphPredicate implements Predicate<Word> {

	public boolean test(Word word) {
		if (word.getLemma().equals(FRConstants.P_TAG) || word.getLemma().equals(FRConstants.S_TAG)) {
			return true;
		}
		return false;
	}
}
