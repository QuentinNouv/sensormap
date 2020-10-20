package com.siame.nouvel.sensormap;

import android.content.Context;
import android.util.Log;
import android.util.Pair;
import android.widget.Toast;

import org.eclipse.paho.android.service.MqttAndroidClient;
import org.eclipse.paho.client.mqttv3.DisconnectedBufferOptions;
import org.eclipse.paho.client.mqttv3.IMqttActionListener;
import org.eclipse.paho.client.mqttv3.IMqttDeliveryToken;
import org.eclipse.paho.client.mqttv3.IMqttToken;
import org.eclipse.paho.client.mqttv3.MqttCallbackExtended;
import org.eclipse.paho.client.mqttv3.MqttConnectOptions;
import org.eclipse.paho.client.mqttv3.MqttException;
import org.eclipse.paho.client.mqttv3.MqttMessage;
import org.json.JSONObject;
import org.osmdroid.util.GeoPoint;
import org.osmdroid.views.MapView;
import org.osmdroid.views.overlay.Marker;

import java.util.Arrays;
import java.util.HashMap;

public class MQTTBuffer {

    private HashMap<String, Pair<Marker, String>> lastInfo;
    static private Pair<Marker, String> def = new Pair<>(null, "err");

    private MqttAndroidClient mqttClient;
    final private String siameURL = "tcp://195.220.53.10:10483";
    final private String client = "quentinAndroid";

    public MQTTBuffer(Context ctx, MapView map) {
        this.lastInfo = new HashMap<>();
        mqttClient = new MqttAndroidClient(ctx, siameURL, client);
        mqttClient.setCallback(new MqttCallbackExtended() {
            @Override
            public void connectComplete(boolean b, String s) {
            }

            @Override
            public void connectionLost(Throwable throwable) {

            }

            @Override
            public void messageArrived(String s, MqttMessage mqttMessage) throws Exception {
                JSONObject jsonmsg = new JSONObject(new String(mqttMessage.getPayload()));
                System.out.println(jsonmsg.toString());
                String sensor = jsonmsg.getString("sensor");
                Marker mk;
                if (lastInfo.containsKey(sensor)) {
                    Pair<Marker, String> v = lastInfo.get(sensor);
                    if (v != null) {
                        mk = v.first;
                    } else throw new Exception();
                } else {
                    mk = new Marker(map, ctx);
                }
                mk.setPosition(new GeoPoint(jsonmsg.getDouble("latitude"),
                        jsonmsg.getDouble("longitude")));
                mk.setAnchor(Marker.ANCHOR_CENTER, Marker.ANCHOR_BOTTOM);
                mk.setDefaultIcon();//TODO change
                mk.setTitle(sensor);
                mk.setOnMarkerClickListener(new Marker.OnMarkerClickListener() {
                    @Override
                    public boolean onMarkerClick(Marker marker, MapView mapView) {

                        mapView.getController().animateTo(marker.getPosition());
                        for (Pair<Marker, String> p : lastInfo.values()) {
                            p.first.closeInfoWindow();
                        }
                        marker.showInfoWindow();
                        return true;
                    }
                });
                mk.setInfoWindow(new SensorInfoWindow(R.layout.sensor_info_window, map, sensor, MQTTBuffer.this));
                Pair<Marker, String> entry = new Pair<>(mk, jsonmsg.getString("value"));
                lastInfo.put(sensor, entry);
                map.getOverlays().add(mk);
                map.invalidate();
            }

            @Override
            public void deliveryComplete(IMqttDeliveryToken iMqttDeliveryToken) {

            }
        });
        MqttConnectOptions mqttConnectOptions = new MqttConnectOptions();
        mqttConnectOptions.setAutomaticReconnect(true);
        mqttConnectOptions.setCleanSession(false);

        try {
            mqttClient.connect(mqttConnectOptions, null, new IMqttActionListener() {
                @Override
                public void onSuccess(IMqttToken asyncActionToken) {
                    DisconnectedBufferOptions disconnectedBufferOptions = new DisconnectedBufferOptions();
                    disconnectedBufferOptions.setBufferEnabled(true);
                    disconnectedBufferOptions.setBufferSize(100);
                    disconnectedBufferOptions.setPersistBuffer(false);
                    disconnectedBufferOptions.setDeleteOldestMessages(false);
                    mqttClient.setBufferOpts(disconnectedBufferOptions);
                    Toast.makeText(ctx, "Connected to SIAME MQTT", Toast.LENGTH_SHORT).show();
                    try {
                        mqttClient.subscribe("android/quentin/sensors", 0);
                    } catch (MqttException e) {
                        Log.e("siame", Arrays.toString(e.getStackTrace()));
                    }
                }

                @Override
                public void onFailure(IMqttToken asyncActionToken, Throwable exception) {
                    Log.e("Siame", "erreur to connect to : " + siameURL + " ! :(");
                }
            });


        } catch (MqttException ex) {
            ex.printStackTrace();
        }
    }

    public String getTemperature(String sensor) {
        Pair<Marker, String> v = lastInfo.get(sensor);
        if (v != null) return v.second;
        else return "err";
    }
}
