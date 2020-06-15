package com.github.maoabc.aterm.db;

import android.content.Context;

import androidx.room.Database;
import androidx.room.Room;
import androidx.room.RoomDatabase;

import com.github.maoabc.aterm.db.entities.SshServer;
import com.github.maoabc.aterm.db.source.SshServerDao;


/**
 * Created by mao on 17-11-19.
 */

@Database(entities =
        {SshServer.class},
        version = 1, exportSchema = false)
public abstract class ATermDatabase extends RoomDatabase {
    private volatile static ATermDatabase sInstance;

    private static final String DATABASE_NAME = "sshServers.db";


    public abstract SshServerDao sshServerDao();


    public static ATermDatabase getInstance(final Context context) {
        if (sInstance == null) {
            synchronized (ATermDatabase.class) {
                if (sInstance == null) {
                    sInstance = buildDatabase(context.getApplicationContext());
                }
            }
        }
        return sInstance;
    }

    /**
     * Build the database. {@link Builder#build()} only sets up the database configuration and
     * creates a new instance of the database.
     * The SQLite database is only created when it's accessed for the first time.
     */
    private static ATermDatabase buildDatabase(final Context appContext) {
        return Room.databaseBuilder(appContext, ATermDatabase.class, DATABASE_NAME).build();
    }


}
