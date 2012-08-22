package co.touchlab.ormlitedemo;

import android.app.Activity;
import android.app.ProgressDialog;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.TextView;
import co.touchlab.ormlitedemo.data.*;
import com.j256.ormlite.dao.Dao;
import com.j256.ormlite.stmt.PreparedQuery;

import java.sql.SQLException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
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

        TextView subTitle = (TextView)findViewById(R.id.article_sub_title);
        subTitle.setText(model.getSubTitleText());

        TextView articleText = (TextView)findViewById(R.id.article_text);
        articleText.setText(model.article.getText());

        findViewById(R.id.divider).setVisibility(View.VISIBLE);
    }

    private static class ArticleModel
    {
        private Article article;
        private List<Author> authorList = new ArrayList<Author>();
        private long commentCount;
        SimpleDateFormat formatter = new SimpleDateFormat(" 'on' M/d/yyyy h:mm a");

        private ArticleModel(Article article)
        {
            if (article == null)
                throw new IllegalArgumentException("Passed in article must be non-null!");

            this.article = article;
        }

        public String getSubTitleText()
        {
            Collections.sort(authorList);
            StringBuilder sb = new StringBuilder("Created by");
            if (authorList.size() == 0)
                sb.append(" unknown");
            else
            {
                sb.append(" ").append(authorList.get(0).getName());
                if (authorList.size() > 1)
                {
                    for (int i = 1; i < authorList.size(); i++)
                        sb.append(", ").append(authorList.get(i).getName());
                }
            }
            sb.append(formatter.format(article.getPublishedDate()));

            return sb.toString();
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
        private int articleId;

        private LoadArticleTask(ArticleActivity host, int id)
        {
            this.host = host;
            this.articleId = id;
        }

        @Override
        protected ArticleModel doInBackground(Void... voids)
        {
            DatabaseHelper helper = DatabaseHelper.getInstance(host);
            try
            {
                //First, we use queryForId() to get the Article itself.
                Dao<Article,Integer> articleDao = helper.getArticleDao();
                Article article = articleDao.queryForId(articleId);
                ArticleModel model = new ArticleModel(article);

                //Then, we get all the Authors for this Article. This will demo the QueryBuilder class.
                Dao authorDao = helper.getArticleAuthorDao();
                PreparedQuery query = authorDao.queryBuilder()
                        .where()
                        .eq(ArticleAuthor.ARTICLE_ID_COLUMN, articleId)
                        .prepare();
                //Now, run the query
                List results = authorDao.query(query);
                for (Object item : results)
                {
                    Author author = ((ArticleAuthor)item).getAuthor();
                    model.authorList.add(author);
                }

                //Finally, get a count of the comments. We will use the QueryBuilder class again.
                Dao<Comment, Integer> commentDao = helper.getCommentDao();
                PreparedQuery<Comment> countQuery = commentDao.queryBuilder()
                        .setCountOf(true)
                        .where()
                        .eq(Comment.ARTICLE_COLUMN, articleId)
                        .prepare();
                model.commentCount = commentDao.countOf(countQuery);

                return model;
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