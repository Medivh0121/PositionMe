package com.openpositioning.PositionMe.UI;

import android.content.Context;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.Polyline;
import com.google.android.material.bottomsheet.BottomSheetDialog;
import com.openpositioning.PositionMe.R;

import java.util.List;

public class UIFunctions {

    private final Context context;
    private final GoogleMap mMap;
    private BottomSheetDialog bottomLayerDialog;
    private BottomSheetDialog bottomPathDialog;
    private BottomSheetDialog bottomInfoDialog;
    private final Polyline fusionPath;
    private final Polyline wifiPath;
    private final Polyline gnssPath;
    private final Polyline pdrPath;
    private final View view;

    private final Marker pdrMarker;
    private final Marker fusionMarker;
    private final Marker gnssMarker;
    private final Marker wifiMarker;


    private static final String PREFS_NAME = "MapTypePrefs";
    private static final String PREF_KEY_MAP_TYPE = "mapType";

    public UIFunctions(Context context, GoogleMap mMap, View view, Polyline fusionPath, Polyline WifiPath, Polyline GNSSPath, Polyline PDRPath, Marker fusionMarker, Marker WifiMarker, Marker GNSSMarker, Marker PDRMarker) {
        this.context = context;
        this.mMap = mMap;
        this.view = view;
        this.fusionPath = fusionPath;
        this.wifiPath = WifiPath;
        this.gnssPath = GNSSPath;
        this.pdrPath = PDRPath;
        this.fusionMarker = fusionMarker;
        this.wifiMarker = WifiMarker;
        this.gnssMarker = GNSSMarker;
        this.pdrMarker = PDRMarker;
    }

    public void showLocationInfo(String altitudeStr, String latitudeStr, String longitudeStr, String accStr) {
        SharedPreferences prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);

        if (bottomInfoDialog == null || !bottomInfoDialog.isShowing()) {
            bottomInfoDialog = new BottomSheetDialog(context);
            View bottomLayerView = LayoutInflater.from(context).inflate(R.layout.item_info_bottom_dialog, null);
            bottomInfoDialog.setContentView(bottomLayerView);

            LinearLayout layerLong = bottomLayerView.findViewById(R.id.layer_longitude);

            TextView textLong = bottomLayerView.findViewById(R.id.layer_longitude_text);

            LinearLayout layerLan = bottomLayerView.findViewById(R.id.layer_latitude);
            TextView textLan = bottomLayerView.findViewById(R.id.layer_latitude_text);

            LinearLayout layerAlt = bottomLayerView.findViewById(R.id.layer_altitude);
            TextView textAlt = bottomLayerView.findViewById(R.id.layer_altitude_text);

            TextView textAcc = bottomLayerView.findViewById(R.id.layer_acc_text);

            textAlt.setText(altitudeStr);
            textLan.setText(latitudeStr);
            textLong.setText(longitudeStr);
            textAcc.setText(accStr);

            bottomInfoDialog.show();

        }
    }


    public void showMapTypeDialog() {
        SharedPreferences prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
        int savedMapType = prefs.getInt(PREF_KEY_MAP_TYPE, GoogleMap.MAP_TYPE_NORMAL); // 默认为普通类型

        if (bottomLayerDialog == null || !bottomLayerDialog.isShowing()) {
            bottomLayerDialog = new BottomSheetDialog(context);
            View bottomLayerView = LayoutInflater.from(context).inflate(R.layout.item_layer_bottom_dialog, null);
            bottomLayerDialog.setContentView(bottomLayerView);

            LinearLayout layerDefault = bottomLayerView.findViewById(R.id.layer_default);
            ImageView viewDefault = bottomLayerView.findViewById(R.id.layer_default_view);
            TextView textDefault = bottomLayerView.findViewById(R.id.layer_default_text);

            LinearLayout layerSatellite = bottomLayerView.findViewById(R.id.layer_satellite);
            ImageView viewSatellite = bottomLayerView.findViewById(R.id.layer_satellite_view);
            TextView textSatellite = bottomLayerView.findViewById(R.id.layer_satellite_text);

            LinearLayout layerTerrain = bottomLayerView.findViewById(R.id.layer_terrain);
            ImageView viewTerrain = bottomLayerView.findViewById(R.id.layer_terrain_view);
            TextView textTerrain = bottomLayerView.findViewById(R.id.layer_terrain_text);

            Runnable clearBorders = () -> {
                viewDefault.setBackground(null);
                viewSatellite.setBackground(null);
                viewTerrain.setBackground(null);

                textDefault.setTextColor(Color.parseColor("#3C4043"));
                textSatellite.setTextColor(Color.parseColor("#3C4043"));
                textTerrain.setTextColor(Color.parseColor("#3C4043"));
            };

            layerDefault.setOnClickListener(v -> {
                mMap.setMapType(GoogleMap.MAP_TYPE_NORMAL);
                prefs.edit().putInt(PREF_KEY_MAP_TYPE, GoogleMap.MAP_TYPE_NORMAL).apply();
                clearBorders.run();
                viewDefault.setBackgroundResource(R.drawable.textview_border);
                textDefault.setTextColor(Color.parseColor("#1A73E8"));
            });

            layerSatellite.setOnClickListener(v -> {
                mMap.setMapType(GoogleMap.MAP_TYPE_SATELLITE);
                prefs.edit().putInt(PREF_KEY_MAP_TYPE, GoogleMap.MAP_TYPE_SATELLITE).apply();
                clearBorders.run();
                viewSatellite.setBackgroundResource(R.drawable.textview_border);
                textSatellite.setTextColor(Color.parseColor("#1A73E8"));
            });

            layerTerrain.setOnClickListener(v -> {
                mMap.setMapType(GoogleMap.MAP_TYPE_TERRAIN);
                prefs.edit().putInt(PREF_KEY_MAP_TYPE, GoogleMap.MAP_TYPE_TERRAIN).apply();
                clearBorders.run();
                viewTerrain.setBackgroundResource(R.drawable.textview_border);
                textTerrain.setTextColor(Color.parseColor("#1A73E8"));
            });

            // 根据保存的地图类型设置初始选中状态
            clearBorders.run();
            switch (savedMapType) {
                case GoogleMap.MAP_TYPE_SATELLITE:
                    viewSatellite.setBackgroundResource(R.drawable.textview_border);
                    textSatellite.setTextColor(Color.parseColor("#1A73E8"));
                    break;
                case GoogleMap.MAP_TYPE_TERRAIN:
                    viewTerrain.setBackgroundResource(R.drawable.textview_border);
                    textTerrain.setTextColor(Color.parseColor("#1A73E8"));
                    break;
                case GoogleMap.MAP_TYPE_NORMAL:
                default:
                    viewDefault.setBackgroundResource(R.drawable.textview_border);
                    textDefault.setTextColor(Color.parseColor("#1A73E8"));
                    break;
            }

            bottomLayerDialog.show();
        }
    }

    public void showPathTypeDialog() {
        Button btnChangePathType = view.findViewById(R.id.btnChangePathType);
        btnChangePathType.setOnClickListener(v -> {
            if (bottomPathDialog == null || !bottomPathDialog.isShowing()) {
                bottomPathDialog = new BottomSheetDialog(context);
                View bottomPathView = LayoutInflater.from(context).inflate(R.layout.item_path_bottom_dialog, null);
                bottomPathDialog.setContentView(bottomPathView);

                // Define path type layouts, images, and texts
                LinearLayout layoutFusion = bottomPathView.findViewById(R.id.layoutFusion);
                ImageView imageFusion = bottomPathView.findViewById(R.id.imageFusion);
                TextView textFusion = bottomPathView.findViewById(R.id.textFusion);

                LinearLayout layoutWifi = bottomPathView.findViewById(R.id.layoutWifi);
                ImageView imageWifi = bottomPathView.findViewById(R.id.imageWifi);
                TextView textWifi = bottomPathView.findViewById(R.id.textWifi);

                LinearLayout layoutGNSS = bottomPathView.findViewById(R.id.layoutGNSS);
                ImageView imageGNSS = bottomPathView.findViewById(R.id.imageGNSS);
                TextView textGNSS = bottomPathView.findViewById(R.id.textGNSS);

                LinearLayout layoutPDR = bottomPathView.findViewById(R.id.layoutPDR);
                ImageView imagePDR = bottomPathView.findViewById(R.id.imagePDR);
                TextView textPDR = bottomPathView.findViewById(R.id.textPDR);

                SharedPreferences prefs = context.getSharedPreferences("PathTypeSelection", Context.MODE_PRIVATE);

                // Set initial state and visibility based on saved preferences or defaults
                boolean isSelectedFusion = prefs.getBoolean("Fusion", true);
                boolean isSelectedWifi = prefs.getBoolean("Wifi", false);
                boolean isSelectedGNSS = prefs.getBoolean("GNSS", false);
                boolean isSelectedPDR = prefs.getBoolean("PDR", false);

                toggleSelection(imageFusion, textFusion, isSelectedFusion, "Fusion");
                toggleSelection(imageWifi, textWifi, isSelectedWifi, "Wifi");
                toggleSelection(imageGNSS, textGNSS, isSelectedGNSS, "GNSS");
                toggleSelection(imagePDR, textPDR, isSelectedPDR, "PDR");

                setPathVisibility("Fusion", isSelectedFusion);
                setPathVisibility("Wifi", isSelectedWifi);
                setPathVisibility("GNSS", isSelectedGNSS);
                setPathVisibility("PDR", isSelectedPDR);

                // Click listeners for each layout
                View.OnClickListener clickListener = view1 -> {
                    String tag = (String) view1.getTag();
                    boolean isSelected = prefs.getBoolean(tag, false);

                    // Toggle selection only if another path is visible or if we're selecting the item
                    if (!isSelected || (isSelected && moreThanOnePathSelected(prefs))) {
                        isSelected = !isSelected;
                        prefs.edit().putBoolean(tag, isSelected).apply();
                        toggleSelectionBasedOnTag(tag, isSelected);
                        setPathVisibility(tag, isSelected);
                    }
                };

                layoutFusion.setTag("Fusion");
                layoutFusion.setOnClickListener(clickListener);

                layoutWifi.setTag("Wifi");
                layoutWifi.setOnClickListener(clickListener);

                layoutGNSS.setTag("GNSS");
                layoutGNSS.setOnClickListener(clickListener);

                layoutPDR.setTag("PDR");
                layoutPDR.setOnClickListener(clickListener);
            }

            bottomPathDialog.show();
        });
    }

    public void setPathVisibility(String tag, boolean isVisible) {

        switch (tag) {
            case "Fusion":
                if (fusionPath != null) fusionPath.setVisible(isVisible);
                if (fusionMarker != null) fusionMarker.setVisible(isVisible);
                break;
            case "Wifi":
                if (wifiPath != null) wifiPath.setVisible(isVisible);
                if (wifiMarker != null) wifiMarker.setVisible(isVisible);
                break;
            case "GNSS":
                if (gnssPath != null) gnssPath.setVisible(isVisible);
                if (gnssMarker != null) gnssMarker.setVisible(isVisible);
                break;
            case "PDR":
                if (pdrPath != null) pdrPath.setVisible(isVisible);
                if (pdrMarker != null) pdrMarker.setVisible(isVisible);
                break;
        }
    }


    private boolean moreThanOnePathSelected(SharedPreferences prefs) {
        return (prefs.getBoolean("Fusion", false) ? 1 : 0) +
                (prefs.getBoolean("Wifi", false) ? 1 : 0) +
                (prefs.getBoolean("GNSS", false) ? 1 : 0) +
                (prefs.getBoolean("PDR", false) ? 1 : 0) > 1;
    }

    private void toggleSelectionBasedOnTag(String tag, boolean isSelected) {
        ImageView imageView = bottomPathDialog.findViewById(getImageIdByTag(tag));
        TextView textView = bottomPathDialog.findViewById(getTextIdByTag(tag));
        toggleSelection(imageView, textView, isSelected, tag);
    }

    private int getImageIdByTag(String tag) {
        switch (tag) {
            case "Fusion":
                return R.id.imageFusion;
            case "Wifi":
                return R.id.imageWifi;
            case "GNSS":
                return R.id.imageGNSS;
            case "PDR":
                return R.id.imagePDR;
            default:
                return 0;
        }
    }

    private int getTextIdByTag(String tag) {
        switch (tag) {
            case "Fusion":
                return R.id.textFusion;
            case "Wifi":
                return R.id.textWifi;
            case "GNSS":
                return R.id.textGNSS;
            case "PDR":
                return R.id.textPDR;
            default:
                return 0;
        }
    }

    private void toggleSelection(ImageView imageView, TextView textView, boolean isSelected, String type) {
        if (imageView != null && textView != null) {
            if (isSelected) {
                if (type.equals("Fusion")) {
                    imageView.setBackgroundResource(R.drawable.textview_border_fusion);
                    textView.setTextColor(Color.parseColor("#2F994C"));
                } else if (type.equals("Wifi")) {
                    imageView.setBackgroundResource(R.drawable.textview_border_wifi);
                    textView.setTextColor(Color.parseColor("#E3AA05"));
                } else if (type.equals("GNSS")) {
                    imageView.setBackgroundResource(R.drawable.textview_border_gnss);
                    textView.setTextColor(Color.parseColor("#3C79DE"));
                } else if (type.equals("PDR")) {
                    imageView.setBackgroundResource(R.drawable.textview_border_pdr);
                    textView.setTextColor(Color.parseColor("#D43C30"));
                }
                }
            else {
                imageView.setBackground(null);
                textView.setTextColor(Color.BLACK);
            }
        }
    }


    }

