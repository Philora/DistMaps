package com.dist.maps;

import android.arch.persistence.room.Dao;
import android.arch.persistence.room.Insert;
import android.arch.persistence.room.Query;

import java.util.List;

@Dao
public  interface MapsDao {

    @Insert
    public void addMaps(Maps maps);

    @Query("SELECT * from maps_table")
    List<Maps> getMapsList();
}
