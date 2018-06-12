package runsql_anony;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class JavaRegexClass {

   public static int getNonAsciiCharCount ( String inputVarchar)
   {
       int count = 0;

       try {
           Pattern pat = Pattern.compile( "[^\\x00-\\x7F]" );
           Matcher mat = pat.matcher( inputVarchar) ;
           while (mat.find())
               count++;
       } catch (java.lang.NullPointerException e) {
           count = 0;
       }

       return count;
   }


   public static int getRegExMatchCount ( String inputVarchar , String regexPat)
   {
       int count = 0;
       try {
           Pattern pat = Pattern.compile( regexPat );
           Matcher mat = pat.matcher( inputVarchar) ;
           while (mat.find())
               count++;
       } catch (java.lang.NullPointerException e) {
           count = 0;
       }

       return count;
   }


   public static String replaceRegexJava (  String inputVarchar , String regexPat , String replaceWith)
   {
       String result=null;

       try {
           result = inputVarchar.replaceAll(regexPat ,  replaceWith  );
       } catch (java.lang.NullPointerException e) {
          if(inputVarchar==null ||  regexPat == null )
              result=null;
       }

       return result;
   }

}
