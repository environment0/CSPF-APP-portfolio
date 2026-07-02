package com.example.cspf.Hospital;

import android.Manifest;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.app.ActivityCompat;
import androidx.fragment.app.Fragment;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;
import com.example.cspf.R;
import com.example.cspf.UnsafeHttps;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptor;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.material.floatingactionbutton.ExtendedFloatingActionButton;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;
import org.json.JSONArray;
import org.json.JSONObject;
import java.util.Locale;
import java.util.concurrent.TimeUnit;

public class NearbyHospitalsFragment extends Fragment implements OnMapReadyCallback {
    private static final String API_KEY = "AIzaSyD7EMaiYLr35L2ssPQpiHkjdrwd0KthF0A";
    private static final int LOCATION_PERMISSION_REQUEST_CODE = 1;
    private static final int RADIUS = 5000;

    private GoogleMap mMap;
    private FusedLocationProviderClient fusedLocationClient;
    private LatLng currentLocation;
    private SwipeRefreshLayout swipeRefreshLayout;
    private final OkHttpClient client = UnsafeHttps.getUnsafeOkHttpClient()
            .connectTimeout(30, TimeUnit.SECONDS)
            .readTimeout(30, TimeUnit.SECONDS)
            .writeTimeout(30, TimeUnit.SECONDS)
            .build();

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_nearby_hospitals, container, false);

        swipeRefreshLayout = view.findViewById(R.id.swipe_refresh);
        swipeRefreshLayout.setOnRefreshListener(this::refreshHospitals);

        return view;
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        Locale.setDefault(new Locale("ko", "KR"));

        fusedLocationClient = LocationServices.getFusedLocationProviderClient(requireActivity());

        SupportMapFragment mapFragment = (SupportMapFragment) getChildFragmentManager()
                .findFragmentById(R.id.map_container);
        if (mapFragment != null) {
            mapFragment.getMapAsync(this);
        }

        ExtendedFloatingActionButton locationButton = view.findViewById(R.id.current_location_button);
        locationButton.setOnClickListener(v -> moveToCurrentLocation());
    }

    private void refreshHospitals() {
        if (currentLocation != null) {
            mMap.clear();
            searchNearbyVeterinaryClinics(currentLocation);
        }
        swipeRefreshLayout.setRefreshing(false);
    }

    @Override
    public void onMapReady(@NonNull GoogleMap googleMap) {
        mMap = googleMap;
        mMap.getUiSettings().setMyLocationButtonEnabled(true);
        mMap.getUiSettings().setZoomControlsEnabled(true);

        if (checkLocationPermission()) {
            mMap.setMyLocationEnabled(true);
            getCurrentLocation();
        }

        mMap.setOnMarkerClickListener(marker -> {
            Toast.makeText(requireContext(), marker.getSnippet(), Toast.LENGTH_SHORT).show();
            return false;
        });
    }

    private boolean checkLocationPermission() {
        if (ActivityCompat.checkSelfPermission(requireContext(),
                Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(requireActivity(),
                    new String[]{Manifest.permission.ACCESS_FINE_LOCATION},
                    LOCATION_PERMISSION_REQUEST_CODE);
            return false;
        }
        return true;
    }

    private void getCurrentLocation() {
        if (checkLocationPermission()) {
            fusedLocationClient.getLastLocation().addOnSuccessListener(requireActivity(), location -> {
                if (location != null) {
                    currentLocation = new LatLng(location.getLatitude(), location.getLongitude());
                    mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(currentLocation, 15));
                    searchNearbyVeterinaryClinics(currentLocation);
                } else {
                    Toast.makeText(requireContext(), "위치를 가져올 수 없습니다.", Toast.LENGTH_SHORT).show();
                }
            });
        }
    }

    private void moveToCurrentLocation() {
        if (currentLocation != null) {
            mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(currentLocation, 15));
        } else {
            Toast.makeText(requireContext(), "현재 위치를 가져올 수 없습니다.", Toast.LENGTH_SHORT).show();
        }
    }

    private void searchNearbyVeterinaryClinics(LatLng location) {
        String url = String.format(Locale.US,
                "https://maps.googleapis.com/maps/api/place/nearbysearch/json" +
                        "?location=%f,%f&radius=%d&type=veterinary_care&language=ko&key=%s",
                location.latitude, location.longitude, RADIUS, API_KEY);

        Request request = new Request.Builder()
                .url(url)
                .build();

        client.newCall(request).enqueue(new okhttp3.Callback() {
            @Override
            public void onResponse(@NonNull okhttp3.Call call, @NonNull Response response) throws java.io.IOException {
                if (response.isSuccessful() && response.body() != null) {
                    String responseData = response.body().string();
                    try {
                        JSONObject jsonResponse = new JSONObject(responseData);
                        JSONArray results = jsonResponse.getJSONArray("results");

                        for (int i = 0; i < results.length(); i++) {
                            JSONObject place = results.getJSONObject(i);
                            String name = place.getString("name");
                            String address = place.optString("vicinity", "주소 정보 없음");

                            JSONObject location_json = place.getJSONObject("geometry")
                                    .getJSONObject("location");
                            LatLng clinicLocation = new LatLng(
                                    location_json.getDouble("lat"),
                                    location_json.getDouble("lng"));

                            requireActivity().runOnUiThread(() -> {
                                MarkerOptions markerOptions = new MarkerOptions()
                                        .position(clinicLocation)
                                        .title(name)
                                        .snippet("주소: " + address)
                                        .icon(getHospitalMarkerIcon());
                                mMap.addMarker(markerOptions);
                            });
                        }
                    } catch (Exception e) {
                        e.printStackTrace();
                        showError("데이터 처리 중 오류가 발생했습니다.");
                    }
                } else {
                    showError("병원 정보를 불러올 수 없습니다.");
                }
            }

            @Override
            public void onFailure(@NonNull okhttp3.Call call, @NonNull java.io.IOException e) {
                showError("네트워크 오류가 발생했습니다.");
            }
        });
    }

    private void showError(String message) {
        requireActivity().runOnUiThread(() ->
                Toast.makeText(requireContext(), message, Toast.LENGTH_SHORT).show()
        );
    }

    private BitmapDescriptor getHospitalMarkerIcon() {
        Bitmap originalBitmap = BitmapFactory.decodeResource(getResources(),
                R.drawable.animal_hospital_marker);
        Bitmap scaledBitmap = Bitmap.createScaledBitmap(originalBitmap, 100, 100, false);
        return BitmapDescriptorFactory.fromBitmap(scaledBitmap);
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions,
                                           @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == LOCATION_PERMISSION_REQUEST_CODE) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                if (mMap != null) {
                    if (checkLocationPermission()) {
                        mMap.setMyLocationEnabled(true);
                        getCurrentLocation();
                    }
                }
            } else {
                Toast.makeText(requireContext(), "위치 권한이 필요합니다.", Toast.LENGTH_SHORT).show();
            }
        }
    }
}
