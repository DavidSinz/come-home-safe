package com.example.moni.comehomesafe;


import com.google.android.gms.maps.model.LatLng;

import java.util.List;

public interface DownloadListener {

    public void onDownloadFinished(List<LatLng> polyline);

}
