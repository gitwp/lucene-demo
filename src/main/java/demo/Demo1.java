package demo;

import org.apache.lucene.demo.IndexFiles;
import org.apache.lucene.demo.SearchFiles;

import java.io.File;

/**
 * 参照：http://lucene.apache.org/core/6_6_0/demo/overview-summary.html#overview_description
 */
public class Demo1 {

    public static void main(String[] args) throws Exception{
        //1.creating a Lucene index
        String docPath = new File("").getCanonicalPath() + File.separator+"data";
        args = new String[]{"-docs",docPath};
        //This will produce a subdirectory called index which will contain an index of all of the Lucene source code.
        IndexFiles.main(args);

        //2.search the index
        args = new String[]{"-query","lucene"};
        SearchFiles.main(args);

    }

}
