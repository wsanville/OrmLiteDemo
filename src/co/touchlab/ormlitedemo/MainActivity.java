package co.touchlab.ormlitedemo;

import android.app.Activity;
import android.app.ProgressDialog;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import co.touchlab.ormlitedemo.data.*;
import com.j256.ormlite.dao.Dao;

import java.sql.SQLException;
import java.util.Date;
import java.util.List;

public class MainActivity extends Activity
{
    private static String TAG = "MainActivity";
    private LoadArticleTask task;
    private ProgressDialog dialog;

    @Override
    public void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.main);

        System.setProperty("log.tag.ORMLite", "DEBUG");
        dialog = ProgressDialog.show(this, null, "Loading Articles", true, false);
        task = (LoadArticleTask)getLastNonConfigurationInstance();
        if (task == null)
        {
            task = new LoadArticleTask(this);
            task.execute();
        }
        else
        {
            task.attach(this);
        }

    }

    @Override
    public Object onRetainNonConfigurationInstance()
    {
        task.detach();
        return task;
    }

    public void handleDataLoaded(ArticleListModel model)
    {
        dialog.dismiss();
    }

    private static class ArticleListModel
    {

    }

    private static class LoadArticleTask extends AsyncTask<Void, Void, ArticleListModel>
    {
        private MainActivity host;
        private ArticleListModel payload;
        private boolean done;

        private LoadArticleTask(MainActivity host)
        {
            this.host = host;
        }

        @Override
        protected ArticleListModel doInBackground(Void... voids)
        {
            DatabaseHelper helper = DatabaseHelper.getInstance(host);
            try
            {
                Dao<Article,Integer> articleDao = helper.getArticleDao();
                List<Article> articles = articleDao.queryForAll();
                if (articles.size() == 0)
                    insertSampleData(helper);
            }
            catch (SQLException e)
            {
                Log.e(TAG, "LoadArticleTask doInBackground failed.", e);
                throw new RuntimeException(e);
            }

            return null;
        }

        private void insertSampleData(DatabaseHelper helper) throws SQLException
        {
            final int SAMPLE_COUNT = 50;
            final String SAMPLE_ARTICLE_TEXT = "Aenean id justo non dui sodales molestie quis et nibh. Fusce eu nulla enim, id feugiat nisi. Donec feugiat est eget leo dictum rutrum. Morbi faucibus nulla a urna blandit sed consequat tellus fermentum. Quisque nec turpis eleifend mauris laoreet lacinia. Curabitur sollicitudin arcu quis mauris semper non blandit lectus pharetra. Nam condimentum egestas turpis, nec dictum enim imperdiet pharetra. Sed vel mauris magna. Cras non placerat odio. Donec faucibus odio id dolor elementum non consectetur justo suscipit? Curabitur mollis lectus ac sem consectetur lobortis. Quisque faucibus magna vitae sem auctor ullamcorper?";
            Author[] authors = new Author[] { new Author("First Author", "first@example.com"), new Author("Second Author", "second@example.com"), new Author("Third Author", "third@example.com") };
            Category[] topCategories = new Category[] { new Category("Android Layouts", null), new Category("Databases", null), new Category("Network Communication", null), new Category("Screen Rotation", null), new Category("Threading", null) };
            Category[] subCategories = new Category[] { new Category("SQL", topCategories[1]) };

            //We will need a few DAO objects from our OrmLiteSqliteOpenHelper instance
            Dao<Author, Integer> authorDao = helper.getAuthorDao();
            Dao<Category, Integer> categoryDao = helper.getCategoryDao();
            Dao<Article, Integer> articleDao = helper.getArticleDao();
            Dao articleAuthorDao = helper.getArticleAuthorDao();
            Dao articleCategoryDao = helper.getArticleCategoryDao();

            //Insert all of our sample authors. The DAO will set the database generated ID, which we will need later.
            for (Author author : authors)
                authorDao.create(author);

            //Insert all sample categories
            for (Category category : topCategories)
                categoryDao.create(category);
            for (Category category : subCategories)
                categoryDao.create(category);

            for (int i = 0; i < SAMPLE_COUNT; i++)
            {
                //Make a new Article instance, and call create() on the DAO. That call will set the ID of the object.
                Article article = new Article(new Date(), SAMPLE_ARTICLE_TEXT, "Article " + i);
                articleDao.create(article);

                //Insert cross reference(s) to set the author(s) of this article
                if (i == 0)
                {
                    //Let's make the first article have many authors.
                    for (Author author  : authors)
                        articleAuthorDao.create(new ArticleAuthor(author, article));
                }
                else
                {
                    //Otherwise, we'll assign a single author to the article.
                    Author author = authors[i % authors.length];
                    articleAuthorDao.create(new ArticleAuthor(author, article));
                }

                //Insert another cross reference to set categories for this article.
                int catIndex = i / (SAMPLE_COUNT / topCategories.length);
                Log.d(TAG, "catIndex = " + catIndex);
                Category category = topCategories[catIndex];
                articleCategoryDao.create(new ArticleCategory(article, category));
            }
        }

        @Override
        protected void onPostExecute(ArticleListModel articleListModel)
        {
            done = true;
            payload = articleListModel;
            host.handleDataLoaded(articleListModel);
        }

        public void detach()
        {
            host = null;
        }

        public void attach(MainActivity newActivity)
        {
            host = newActivity;
            if (done)
                host.handleDataLoaded(payload);
        }
    }
}
