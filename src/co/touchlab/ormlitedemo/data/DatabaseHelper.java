package co.touchlab.ormlitedemo.data;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.util.Log;
import com.j256.ormlite.android.apptools.OrmLiteSqliteOpenHelper;
import com.j256.ormlite.dao.Dao;
import com.j256.ormlite.support.ConnectionSource;
import com.j256.ormlite.table.TableUtils;

import java.sql.SQLException;

/**
 * A SqliteOpenHelper that we will use as a singleton in our application.
 */
public class DatabaseHelper extends OrmLiteSqliteOpenHelper
{
    public static final String TAG = "DatabaseHelper";
    public static final String DATABASE_NAME = "demo.db";
    private static final int DATABASE_VERSION = 1;

    private static DatabaseHelper instance;

    /**
     * @param c A context to use when initializing the database for the first time.
     * @return A single instance of DatabaseHelper.
     */
    public static synchronized DatabaseHelper getInstance(Context c)
    {
        if(instance == null)
            instance = new DatabaseHelper(c);

        return instance;
    }

    private DatabaseHelper(Context context)
    {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase sqLiteDatabase, ConnectionSource connectionSource)
    {
        //Here we use the Annotations on our classes and OrmLite to create all of our tables in our relation.
        try
        {
            //When using foreign keys, we must be sure to create tables in the proper order
            TableUtils.createTable(connectionSource, Category.class);
            TableUtils.createTable(connectionSource, Article.class);
            TableUtils.createTable(connectionSource, ArticleCategory.class);
            TableUtils.createTable(connectionSource, Author.class);
            TableUtils.createTable(connectionSource, ArticleAuthor.class);
            TableUtils.createTable(connectionSource, Comment.class);
        }
        catch (SQLException e)
        {
            Log.e(TAG, "Unable to create tables.", e);
            throw new RuntimeException(e);
        }
    }

    @Override
    public void onUpgrade(SQLiteDatabase sqLiteDatabase, ConnectionSource connectionSource, int oldVersion, int newVersion)
    {
        try
        {
            //When using foreign keys, we must be sure to drop tables in the proper order, again
            TableUtils.dropTable(connectionSource, Comment.class, false);
            TableUtils.dropTable(connectionSource, ArticleAuthor.class, false);
            TableUtils.dropTable(connectionSource, ArticleCategory.class, false);
            TableUtils.dropTable(connectionSource, Category.class, false);
            TableUtils.dropTable(connectionSource, Author.class, false);
            TableUtils.dropTable(connectionSource, Article.class, false);
        }
        catch (SQLException e)
        {
            Log.e(TAG, "Unable to drop tables.", e);
            throw new RuntimeException(e);
        }
    }

    @Override
    public void onOpen(SQLiteDatabase db)
    {
        super.onOpen(db);
        //When using API level 8 or higher, we can use referential integrity with foreign keys.
        if (!db.isReadOnly())
            db.execSQL("PRAGMA foreign_keys=ON;");
    }

    /* DAO object accessors with specific types. */

    @SuppressWarnings("unchecked")
    public Dao<Article, Integer> getArticleDao() throws SQLException
    {
        return getDao(Article.class);
    }

    @SuppressWarnings("unchecked")
    public Dao<Category, Integer> getCategoryDao() throws SQLException
    {
        return getDao(Category.class);
    }

    @SuppressWarnings("unchecked")
    public Dao<Comment, Integer> getCommentDao() throws SQLException
    {
        return getDao(Comment.class);
    }

    @SuppressWarnings("unchecked")
    public Dao<Author, Integer> getAuthorDao() throws SQLException
    {
        return getDao(Author.class);
    }

    public Dao getArticleAuthorDao() throws SQLException
    {
        return getDao(ArticleAuthor.class);
    }

    public Dao getArticleCategoryDao() throws SQLException
    {
        return getDao(ArticleCategory.class);
    }
}
