package co.touchlab.ormlitedemo;

import android.app.Activity;
import android.app.ProgressDialog;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.widget.TextView;
import co.touchlab.ormlitedemo.data.Article;
import co.touchlab.ormlitedemo.data.Author;
import co.touchlab.ormlitedemo.data.DatabaseHelper;
import com.j256.ormlite.dao.Dao;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

/**
 * An Activity for showing more types of queries using OrmLite.
 */
public class ArticleActivity extends Activity
{
    private static String TAG = "ArticleActivity";
    public static String ARTICLE_ID = "ARTICLE_ID";

    private LoadArticleTask task;
    private ProgressDialog dialog;

    public void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.article);

        int articleId = getIntent().getIntExtra(ARTICLE_ID, -1);
        if (articleId == -1)
            throw new RuntimeException("Extra must be passed in!");

        dialog = ProgressDialog.show(this, null, "Loading Articles", true, false);
        /* Kick off a task (if needed) to load the data for this activity.
         * Note: We must be mindful of screen orientation changes here. */
        task = (LoadArticleTask)getLastNonConfigurationInstance();
        if (task == null)
        {
            task = new LoadArticleTask(this, articleId);
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

    public void handleDataLoaded(ArticleModel model)
    {
        dialog.dismiss();
        //Fill out the views for this Activity.
        TextView title = (TextView)findViewById(R.id.article_title);
        title.setText(model.article.getTitle());
    }

    private static class ArticleModel
    {
        private Article article;
        private List<Author> authorList = new ArrayList<Author>();
        private int commentCount;

        private ArticleModel(Article article)
        {
            if (article == null)
                throw new IllegalArgumentException("Passed in article must be non-null!");

            this.article = article;
        }
    }

    /**
     * Task to load the needed items out of the database. This will demo some more ways to query a database with
     * OrmLite.
     */
    private static class LoadArticleTask extends AsyncTask<Void, Void, ArticleModel>
    {
        private boolean done;
        private ArticleModel payload;
        private ArticleActivity host;
        private int id;

        private LoadArticleTask(ArticleActivity host, int id)
        {
            this.host = host;
            this.id = id;
        }

        @Override
        protected ArticleModel doInBackground(Void... voids)
        {
            DatabaseHelper helper = DatabaseHelper.getInstance(host);
            try
            {
                Dao<Article,Integer> articleDao = helper.getArticleDao();
                Article article = articleDao.queryForId(id);
                return new ArticleModel(article);
            }
            catch (SQLException e)
            {
                Log.e(TAG, "Error loading article data.", e);
                throw new RuntimeException(e);
            }
        }

        @Override
        protected void onPostExecute(ArticleModel articleModel)
        {
            done = true;
            payload = articleModel;
            host.handleDataLoaded(articleModel);
        }

        public void detach()
        {
            host = null;
        }

        public void attach(ArticleActivity newActivity)
        {
            host = newActivity;
            if (done)
                host.handleDataLoaded(payload);
        }
    }
}