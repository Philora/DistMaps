package com.dist.maps;

import android.app.Application;
import android.arch.lifecycle.LiveData;
import android.arch.lifecycle.MutableLiveData;
import android.arch.lifecycle.ViewModel;
import android.content.Context;

import java.util.List;

public class MapsViewModel extends ViewModel {

    MapsRepository  mapsRepository;
    LiveData<List<Maps>> listLiveData;

}
