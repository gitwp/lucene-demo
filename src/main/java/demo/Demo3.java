package demo;

import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.analysis.standard.StandardAnalyzer;
import org.apache.lucene.document.*;
import org.apache.lucene.index.DirectoryReader;
import org.apache.lucene.index.IndexReader;
import org.apache.lucene.index.IndexWriter;
import org.apache.lucene.index.IndexWriterConfig;
import org.apache.lucene.queryparser.classic.ParseException;
import org.apache.lucene.queryparser.classic.QueryParser;
import org.apache.lucene.search.IndexSearcher;
import org.apache.lucene.search.Query;
import org.apache.lucene.search.ScoreDoc;
import org.apache.lucene.search.TopDocs;
import org.apache.lucene.store.Directory;
import org.apache.lucene.store.RAMDirectory;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class Demo3 {

    public static void main(String[] args) throws IOException, ParseException {
        // 1,模拟数据库中的记录
        List<Article> articleList = getArticles();
        // 2,建立索引
        Directory directory = doIndex(articleList);
        // 3,搜索,条件(不区分大小写)
        String queryString = "lucene";
        doQuery(queryString, directory);

    }

    private static List<Article> getArticles() {
        List<Article> articleList = new ArrayList<>();
        Article article1 = new Article(1, "Lucene全文检索框架", "Lucene如果信息检索系统在用户发出了检索请求后再去网上找答案", "helloWorld");
        Article article2 = new Article(2, "Lucene全文检索框架", "Lucene入门案例详解", "helloWorld");
        Article article3 = new Article(3, "Lucene全文检索框架", "Lucene教程详解- 曹胜欢- 博客频道- CSDN.NET", "helloWorld");
        Article article4 = new Article(4, "Lucene全文检索框架", "Lucene 使用教程| IT瘾", "helloWorld");
        Article article5 = new Article(5, "Lucene全文检索框架", "lucene教程简介- XIAO_DF - 博客园", "helloWorld");
        articleList.add(article1);
        articleList.add(article2);
        articleList.add(article3);
        articleList.add(article4);
        articleList.add(article5);
        return articleList;
    }

    private static Directory doIndex(List<Article> articleList) throws IOException, ParseException {
        //建立索引库
        Directory directory = new RAMDirectory();
        // 分词器，不同的分词器有不同的规则
        Analyzer analyzer = new StandardAnalyzer();
        IndexWriterConfig indexWriterConfig = new IndexWriterConfig(analyzer);
        IndexWriter indexWriter = new IndexWriter(directory, indexWriterConfig);
        for (Article article : articleList) {
            // 把Article转换为Doucement对象
            Document doc = new Document();
            //根据实际情况，使用不同的Field来对原始内容建立索引， Store.YES表示是否存储字段原始内容
            Field field = new LongPoint("id", article.getId());
            doc.add(field);
            //要排序，必须添加一个同名的NumericDocValuesField
            field = new NumericDocValuesField("id", article.getId());
            doc.add(field);
            //要存储值，必须添加一个同名的StoredField
            field = new StoredField("id", article.getId());
            doc.add(field);
            doc.add(new StringField("author", article.getAuthor(), Field.Store.YES));
            doc.add(new TextField("title", article.getTitle(), Field.Store.YES));
            doc.add(new TextField("content", article.getContent(), Field.Store.YES));
            indexWriter.addDocument(doc);
        }
        indexWriter.close();

        return directory;
    }

    private static void doQuery(String queryString, Directory directory) throws IOException, ParseException {
        Analyzer analyzer = new StandardAnalyzer();
        // 1、把查询字符串转为查询对象(存储的都是二进制文件，普通的String肯定无法查询，因此需要转换)
        QueryParser queryParser = new QueryParser("title", analyzer);// 只在标题里面查询
        Query query = queryParser.parse(queryString);

        // 2、查询，得到中间结果
        IndexReader indexReader = DirectoryReader.open(directory);
        IndexSearcher indexSearcher = new IndexSearcher(indexReader);
        TopDocs topDocs = indexSearcher.search(query, 100);// 根据指定查询条件查询，只返回前n条结果
        int count = topDocs.totalHits;// 总结果数
        ScoreDoc[] scoreDocs = topDocs.scoreDocs;// 按照得分进行排序后的前n条结果的信息

        List<Article> articleList = new ArrayList<Article>();
        // 3、处理中间结果
        for (ScoreDoc scoreDoc : scoreDocs) {
            float score = scoreDoc.score;// 相关度得分
            int docId = scoreDoc.doc; // Document在数据库的内部编号(是唯一的，由lucene自动生成)

            // 根据编号取出真正的Document数据
            Document doc = indexSearcher.doc(docId);

            // 把Document转成Article
            Article article = new Article(
                    Integer.parseInt(doc.getField("id").stringValue()),//需要转为int型
                    doc.getField("title").stringValue(),
                    doc.getField("content").stringValue(),
                    doc.getField("author").stringValue()
            );

            articleList.add(article);
        }

        indexReader.close();
        // ============查询结束====================
        // 显示结果
        System.out.println("总结果数量为:" + articleList.size());
        for (Article article : articleList) {
            System.out.println("id=" + article.getId());
            System.out.println("title=" + article.getTitle());
            System.out.println("content=" + article.getContent());
        }
    }

    /**
     * 文章的信息类，在创建索引的时候，需要将其转换为Document对象
     */
    static class Article {
        private Integer id;
        private String title;
        private String content;
        private String author;

        public Article(Integer id, String title, String content, String author) {
            this.id = id;
            this.title = title;
            this.content = content;
            this.author = author;
        }

        @Override
        public String toString() {
            return "article [id=" + id + ", title=" + title + ", content="
                    + content + "]";
        }

        public Integer getId() {
            return id;
        }

        public void setId(Integer id) {
            this.id = id;
        }

        public String getTitle() {
            return title;
        }

        public void setTitle(String title) {
            this.title = title;
        }

        public String getContent() {
            return content;
        }

        public void setContent(String content) {
            this.content = content;
        }

        public String getAuthor() {
            return author;
        }

        public void setAuthor(String author) {
            this.author = author;
        }
    }
}
