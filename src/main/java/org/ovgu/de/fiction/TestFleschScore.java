package org.ovgu.de.fiction;

public class TestFleschScore {

	public TestFleschScore() {
		// TODO Auto-generated constructor stub
	}

	public static void main(String[] args) {
		String currentWord="<";
		System.out.println("Syllables in "+currentWord+" is "+countSyllables(currentWord));

	}
	
	private static int countSyllables(String word)
    {
        char[] vowels = { 'a', 'e', 'i', 'o', 'u', 'y' };
        //String currentWord = word;
        int numVowels = 0;
        boolean lastWasVowel = false;
        char[] wc = word.toCharArray();
        for(int j=0;j<wc.length;j++)
        {
            boolean foundVowel = false;
            for (int v=0;v<vowels.length;v++)
            {
                //don't count diphthongs
                if (vowels[v]==wc[j] && lastWasVowel==true)
                {
                    foundVowel = true;
                    lastWasVowel = true;
                    break;
                }
                else if (vowels[v]==wc[j] && !lastWasVowel)
                {
                    numVowels++;
                    foundVowel = true;
                    lastWasVowel = true;
                    break;
                }
            }

            //if full cycle and no vowel found, set lastWasVowel to false;
            if (!foundVowel)
                lastWasVowel = false;
        }
        //remove es, it's _usually? silent
        if (word.length() > 2 && 
            		word.substring(word.length() - 2).equals("es"))
            numVowels--;
        // remove silent e
        else if (word.length() > 1 &&
            word.substring(word.length() - 1).equals("e"))
            numVowels--;

        return numVowels;
    }

}
