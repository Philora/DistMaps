package com.dist.maps;

import android.arch.persistence.db.SupportSQLiteOpenHelper;
import android.arch.persistence.room.Database;
import android.arch.persistence.room.DatabaseConfiguration;
import android.arch.persistence.room.InvalidationTracker;
import android.arch.persistence.room.Room;
import android.arch.persistence.room.RoomDatabase;
import android.content.Context;


@Database(entities = {Maps.class}, version = 1, exportSchema = false)
public abstract class MapsDatabase  extends RoomDatabase{

    public abstract MapsDao mapsDao();


    private static MapsDatabase INSTANCE;

    public static MapsDatabase getDatabase(final Context context) {
        if(INSTANCE == null){
            //TODO read what is synchronized
            synchronized (MapsDatabase.class){
                INSTANCE = Room.databaseBuilder(context,MapsDatabase.class,"maps_database").build();
            }
        }
        return INSTANCE;
    }
}
