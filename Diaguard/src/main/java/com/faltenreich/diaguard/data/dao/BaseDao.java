package com.faltenreich.diaguard.data.dao;

import android.util.Log;

import com.faltenreich.diaguard.DiaguardApplication;
import com.faltenreich.diaguard.data.DatabaseHelper;
import com.faltenreich.diaguard.data.entity.BaseEntity;
import com.faltenreich.diaguard.data.entity.Meal;
import com.j256.ormlite.android.apptools.OpenHelperManager;
import com.j256.ormlite.dao.Dao;
import com.j256.ormlite.dao.DaoManager;
import com.j256.ormlite.stmt.DeleteBuilder;

import java.sql.SQLException;
import java.util.List;

/**
 * Created by Faltenreich on 05.09.2015.
 */
public abstract class BaseDao <T extends BaseEntity> {

    private static final String TAG = BaseDao.class.getSimpleName();

    private DatabaseHelper databaseHelper;
    private Class<T> clazz;

    protected BaseDao(Class<T> clazz) {
        this.databaseHelper = OpenHelperManager.getHelper(DiaguardApplication.getContext(), DatabaseHelper.class);
        this.clazz = clazz;
    }

    protected Dao<T, Long> getDao() {
        try {
            return this.databaseHelper.getDao(clazz);
        } catch (SQLException exception) {
            Log.e(TAG, String.format("Could not retrieve Dao of class %s", clazz.getSimpleName()));
            return null;
        }
    }

    public T get(long id) {
        try {
            return getDao().queryBuilder().where().eq(BaseEntity.ID, id).queryForFirst();
        } catch (SQLException exception) {
            Log.e(TAG, String.format("Could not get %s with id %d", clazz.getSimpleName(), id));
            return null;
        }
    }

    public void createOrUpdate(T object) {
        try {
            if (object.getId() > 0) {
                getDao().update(object);
            } else {
                getDao().create(object);
            }
        } catch (SQLException exception) {
            Log.e(TAG, String.format("Could not createOrUpdate %s", clazz.getSimpleName()));
        }
    }

    public int delete(List<T> objects) {
        try {
            return getDao().delete(objects);
        } catch (SQLException exception) {
            Log.e(TAG, "Could not delete list of objects");
            return 0;
        }
    }

    public int delete(T object) {
        try {
            return getDao().delete(object);
        } catch (SQLException exception) {
            Log.e(TAG, String.format("Could not delete %s with id %d", clazz.getSimpleName(), object.getId()));
            return 0;
        }
    }
}