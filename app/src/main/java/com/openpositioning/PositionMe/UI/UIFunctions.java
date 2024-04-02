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
import com.google.android.gms.maps.model.Polyline;
import com.google.android.material.bottomsheet.BottomSheetDialog;
import com.openpositioning.PositionMe.R;

import java.util.List;

public class UIFunctions {

    private final Context context;
    private final GoogleMap mMap;
    private BottomSheetDialog bottomLayerDialog;
    private BottomSheetDialog bottomPathDialog;
    private Polyline fusionPath;
    private Polyline wifiPath;
    private Polyline gnssPath;
    private Polyline pdrPath;
    private View view;

    private static final String PREFS_NAME = "MapTypePrefs";
    private static final String PREF_KEY_MAP_TYPE = "mapType";

    public UIFunctions(Context context, GoogleMap mMap, View view, Polyline fusionPath, Polyline WifiPath, Polyline GNSSPath, Polyline PDRPath) {
        this.context = context;
        this.mMap = mMap;
        this.view = view;
        this.fusionPath = fusionPath;
        this.wifiPath = WifiPath;
        this.gnssPath = GNSSPath;
        this.pdrPath = PDRPath;
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
                boolean isSelectedFusion = prefs.getBoolean("Fusion", true);
                boolean isSelectedWifi = prefs.getBoolean("Wifi", false);
                boolean isSelectedGNSS = prefs.getBoolean("GNSS", false);
                boolean isSelectedPDR = prefs.getBoolean("PDR", false);

                toggleSelection(imageFusion, textFusion, isSelectedFusion);
                toggleSelection(imageWifi, textWifi, isSelectedWifi);
                toggleSelection(imageGNSS, textGNSS, isSelectedGNSS);
                toggleSelection(imagePDR, textPDR, isSelectedPDR);

                setPathVisibility("Fusion", isSelectedFusion);
                setPathVisibility("Wifi", isSelectedWifi);
                setPathVisibility("GNSS", isSelectedGNSS);
                setPathVisibility("PDR", isSelectedPDR);

                View.OnClickListener clickListener = view1 -> {
                    String tag = (String) view1.getTag();
                    boolean isSelected = !prefs.getBoolean(tag, false);
                    prefs.edit().putBoolean(tag, isSelected).apply();
                    toggleSelectionBasedOnTag(tag, isSelected);
                    setPathVisibility(tag, isSelected);
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

    private void setPathVisibility(String tag, boolean isVisible) {
        // Assuming these are already initialized elsewhere
        if (tag.equals("Fusion") && fusionPath != null) {
            fusionPath.setVisible(isVisible);
        } else if (tag.equals("Wifi") && wifiPath != null) {
            wifiPath.setVisible(isVisible);
        } else if (tag.equals("GNSS") && gnssPath != null) {
            gnssPath.setVisible(isVisible);
        } else if (tag.equals("PDR") && pdrPath != null) {
            pdrPath.setVisible(isVisible);
        }
    }

    private void toggleSelection(ImageView imageView, TextView textView, boolean isSelected) {
        if (isSelected) {
            imageView.setBackgroundResource(R.drawable.textview_border);
            textView.setTextColor(Color.parseColor("#1A73E8"));
        } else {
            imageView.setBackground(null);
            textView.setTextColor(Color.BLACK);
        }
    }

    private void toggleSelectionBasedOnTag(String tag, boolean isSelected) {
        ImageView imageView = null;
        TextView textView = null;
        if (tag.equals("Fusion")) {
            imageView = bottomPathDialog.findViewById(R.id.imageFusion);
            textView = bottomPathDialog.findViewById(R.id.textFusion);
        } else if (tag.equals("Wifi")) {
            imageView = bottomPathDialog.findViewById(R.id.imageWifi);
            textView = bottomPathDialog.findViewById(R.id.textWifi);
        } else if (tag.equals("GNSS")) {
            imageView = bottomPathDialog.findViewById(R.id.imageGNSS);
            textView = bottomPathDialog.findViewById(R.id.textGNSS);
        } else if (tag.equals("PDR")) {
            imageView = bottomPathDialog.findViewById(R.id.imagePDR);
            textView = bottomPathDialog.findViewById(R.id.textPDR);
        }

        if (imageView != null && textView != null) {
            toggleSelection(imageView, textView, isSelected);
        }
    }



    private boolean isOnlySelected(String tag, SharedPreferences prefs) {
        // Convert boolean to integer (1 for true, 0 for false) and calculate the sum
        int selectedCount = (prefs.getBoolean("Fusion", false) ? 1 : 0) +
                (prefs.getBoolean("Wifi", false) ? 1 : 0) +
                (prefs.getBoolean("GNSS", false) ? 1 : 0) +
                (prefs.getBoolean("PDR", false) ? 1 : 0);

        // Check if the current item is selected and it's the only one
        return selectedCount == 1 && prefs.getBoolean(tag, false);
    }



}

