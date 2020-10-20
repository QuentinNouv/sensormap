package com.siame.nouvel.sensormap;

import android.content.Context;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.widget.TextView;

import org.osmdroid.util.GeoPoint;
import org.osmdroid.views.MapView;
import org.osmdroid.views.overlay.Marker;
import org.osmdroid.views.overlay.infowindow.InfoWindow;

public class SensorInfoWindow extends InfoWindow {
    static int titreId;
    static int valueId;
    static int longitudeId;
    static int latitudeId;

    String sensor;
    MQTTBuffer buffer;

    private static void setResId(Context context) {
        titreId = context.getResources().getIdentifier("id/titre", (String) null, context.getPackageName());
        valueId = context.getResources().getIdentifier("id/value", (String) null, context.getPackageName());
        longitudeId = context.getResources().getIdentifier("id/longitude", (String) null, context.getPackageName());
        latitudeId = context.getResources().getIdentifier("id/latitude", (String) null, context.getPackageName());
        if (titreId == 0 | valueId == 0 | longitudeId == 0 | latitudeId == 0) {
            Log.e("Siame", "ALED ?");
        }
    }


    public SensorInfoWindow(int layoutResId, MapView mapView, String sensor, MQTTBuffer buffer) {
        super(layoutResId, mapView);
        if (titreId == 0) {
            setResId(mapView.getContext());
        }

        this.sensor = sensor;
        this.buffer = buffer;

        this.mView.setOnTouchListener(new View.OnTouchListener() {
            public boolean onTouch(View v, MotionEvent e) {
                if (e.getAction() == 1) {
                    SensorInfoWindow.this.close();
                }

                return true;
            }
        });
    }

    @Override
    public void onOpen(Object item) {
        if (this.mView == null) {
            Log.w("Siame", "EhEhEh!");
        } else {
            TextView titre = this.mView.findViewById(titreId);
            titre.setText(sensor);

            TextView value = this.mView.findViewById(valueId);
            value.setText(buffer.getTemperature(sensor));

            Marker sensorMarker = (Marker) item;
            GeoPoint gp = sensorMarker.getPosition();

            TextView longitude = this.mView.findViewById(longitudeId);
            longitude.setText(String.format("%f", gp.getLongitude()));

            TextView latitude = this.mView.findViewById(latitudeId);
            latitude.setText(String.format("%f", gp.getLatitude()));
        }


    }

    @Override
    public void onClose() {

    }
}
