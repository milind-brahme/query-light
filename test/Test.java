public class Test {
 
	
	/*
	//available options of metrics
	private static RelatednessCalculator[] rcs = { new HirstStOnge(db),
			new LeacockChodorow(db), new Lesk(db), new WuPalmer(db),
			new Resnik(db), new JiangConrath(db), new Lin(db), new Path(db) };
	*/
	private static double compute(String word1, String word2) {
     //   JaroWinkler algorithm = new JaroWinkler();
        uk.ac.shef.wit.simmetrics.similaritymetrics.ChapmanOrderedNameCompoundSimilarity algorithm = new  uk.ac.shef.wit.simmetrics.similaritymetrics.ChapmanOrderedNameCompoundSimilarity();
         double s =  algorithm.getSimilarity(word1, word2);		
		 
		return s;
	}
 
	public static void main(String[] args) {
		String[] words = {"code_name", "account_code_name", "code_desc", "description", "vendor_deact_desc", "find", "collect", "create"};
 
		for(int i=0; i<words.length-1; i++){
			for(int j=i+1; j<words.length; j++){
				double distance = compute(words[i], words[j]);
				System.out.println(words[i] +" -  " +  words[j] + " = " + distance);
			}
		}
	}
}