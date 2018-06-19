package com.dist.maps;

import android.arch.persistence.room.ColumnInfo;
import android.arch.persistence.room.Entity;
import android.arch.persistence.room.PrimaryKey;

@Entity(tableName = "maps_table")
public class Maps {

    @PrimaryKey(autoGenerate = true)
    public int id;

    @ColumnInfo(name = "source")
    public String mSource;

    @ColumnInfo(name = "destination")
    public String mDestination;

    @ColumnInfo(name = "distance")
    public String mDistance;

    @ColumnInfo(name = "duration")
    public String mDuration;

    public String getmSource() {
        return mSource;
    }

    public Maps setmSource(String mSource) {
        this.mSource = mSource;
        return this;
    }

    public String getmDestination() {
        return mDestination;
    }

    public Maps setmDestination(String mDestination) {
        this.mDestination = mDestination;
        return this;
    }

    public String getmDistance() {
        return mDistance;
    }

    public Maps setmDistance(String mDistance) {
        this.mDistance = mDistance;
        return this;
    }

    public String getmDuration() {
        return mDuration;
    }

    public Maps setmDuration(String mDuration) {
        this.mDuration = mDuration;
        return this;
    }

    public int getId() {
        return id;
    }

    public Maps setId(int id) {
        this.id = id;
        return this;
    }
}

