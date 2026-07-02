package com.example.cspf;

import static android.content.ContentValues.TAG;

import static com.example.cspf.util.ProfileFetcher.fetchProfile;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBarDrawerToggle;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.PopupMenu;
import androidx.appcompat.widget.Toolbar;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.fragment.app.Fragment;
import com.example.cspf.Hospital.NearbyHospitalsFragment;
import com.example.cspf.Hospital.PartnerHospitalsFragment;
import com.example.cspf.News.NewsFragment;
import com.example.cspf.PetLawyer.PetLawyerFragment;
import com.example.cspf.auth.LoginActivity;
import com.example.cspf.cases.CasesFragment;
import com.example.cspf.chatbot.ChatActivity;
import com.example.cspf.chatroom.ChatRoomListActivity;
import com.example.cspf.inquiries.InquiryFragment;
import com.example.cspf.insurance.InsuranceFragment;
import com.example.cspf.notices.NotificationsFragment;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.navigation.NavigationView;

public class MainActivity extends AppCompatActivity {
    private DrawerLayout drawerLayout;
    private NavigationView navigationView;
    private boolean isGuest;
    private FloatingActionButton fab;
    private FloatingActionButton fabChatRoom;
    private SharedPreferences sharedPreferences;
    private TextView headerUsername;
    private TextView loginStatus;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        drawerLayout = findViewById(R.id.drawer_layout);
        navigationView = findViewById(R.id.nav_view);

        sharedPreferences = getSharedPreferences("LoginPrefs", MODE_PRIVATE);
        fetchProfile(this, sharedPreferences);
        boolean isLoggedIn = sharedPreferences.getBoolean("isLoggedIn", false);
        isGuest = getIntent().getBooleanExtra("isGuest", !isLoggedIn);

        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
                this, drawerLayout, toolbar,
                R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        drawerLayout.addDrawerListener(toggle);
        toggle.syncState();

        loadFragment(new NewsFragment());

        View headerView = navigationView.getHeaderView(0);
        headerUsername = headerView.findViewById(R.id.headerUsername);
        loginStatus = headerView.findViewById(R.id.loginStatus);

        displayLoginStatusFromPreferences();
        configureMenuAccess();

        setupFabs();
        navigationView.setNavigationItemSelectedListener(this::onNavigationItemSelected);
    }

    private void setupFabs() {
        fab = findViewById(R.id.fab_chatbot);
        fab.setOnClickListener(v -> {
            if (checkGuestAccess()) {
                navigateToLogin();
                return;
            }
            startActivity(new Intent(MainActivity.this, ChatActivity.class));
        });

        fabChatRoom = findViewById(R.id.fab_chat_room);
        fabChatRoom.setOnClickListener(v -> {
            if (checkGuestAccess()) {
                navigateToLogin();
                return;
            }
            startActivity(new Intent(MainActivity.this, ChatRoomListActivity.class));
        });
    }

    private void displayLoginStatusFromPreferences() {
        String nickname = sharedPreferences.getString("nickname", "Guest");
        Log.d(TAG, "Retrieved nickname from preferences: " + nickname);

        loginStatus.setText("환영합니다.");
        headerUsername.setText(nickname);
    }

    private void configureMenuAccess() {
        Menu menu = navigationView.getMenu();
        Log.d("MainActivity", "isGuest: " + isGuest);

        menu.findItem(R.id.nav_pet_lawyer).setVisible(!isGuest);
        menu.findItem(R.id.nav_insurance).setVisible(!isGuest);
        menu.findItem(R.id.nav_hospitals).setVisible(!isGuest);
        menu.findItem(R.id.nav_inquiries).setVisible(!isGuest);
        menu.findItem(R.id.nav_my_profile).setVisible(!isGuest);
        menu.findItem(R.id.nav_login).setVisible(isGuest);
    }

    private boolean onNavigationItemSelected(@NonNull MenuItem item) {
        Fragment selectedFragment = null;
        int itemId = item.getItemId();

        if (itemId == R.id.nav_news) {
            selectedFragment = new NewsFragment();
        } else if (itemId == R.id.nav_cases) {
            selectedFragment = new CasesFragment();
        } else if (itemId == R.id.nav_notifications) {
            selectedFragment = new NotificationsFragment();
        } else if (itemId == R.id.nav_inquiries) {
            selectedFragment = new InquiryFragment();
        } else if (itemId == R.id.nav_pet_lawyer) {
            if (checkGuestAccess()) {
                navigateToLogin();
                return false;
            }
            selectedFragment = new PetLawyerFragment();
        } else if (itemId == R.id.nav_insurance) {
            if (checkGuestAccess()) {
                navigateToLogin();
                return false;
            }
            selectedFragment = new InsuranceFragment();
        } else if (itemId == R.id.nav_hospitals) {
            if (checkGuestAccess()) {
                navigateToLogin();
                return false;
            }
            showHospitalPopupMenu();
            return true;
        } else if (itemId == R.id.nav_my_profile) {
            startActivity(new Intent(MainActivity.this, ProfileActivity.class));
            return true;
        } else if (itemId == R.id.nav_login) {
            navigateToLogin();
            return true;
        }

        if (selectedFragment != null) {
            loadFragment(selectedFragment);
        }
        drawerLayout.closeDrawers();
        return true;
    }

    private void showHospitalPopupMenu() {
        View view = findViewById(R.id.nav_hospitals);
        PopupMenu popupMenu = new PopupMenu(MainActivity.this, view);
        popupMenu.getMenuInflater().inflate(R.menu.hospitals_submenu, popupMenu.getMenu());

        popupMenu.setOnMenuItemClickListener(item -> {
            Fragment selectedFragment = null;
            int itemId = item.getItemId();

            if (itemId == R.id.nav_nearby_hospitals) {
                selectedFragment = new NearbyHospitalsFragment();
            } else if (itemId == R.id.nav_partner_hospitals) {
                selectedFragment = new PartnerHospitalsFragment();
            }

            if (selectedFragment != null) {
                loadFragment(selectedFragment);
            }
            return true;
        });
        popupMenu.show();
    }

    private void loadFragment(Fragment fragment) {
        getSupportFragmentManager()
                .beginTransaction()
                .replace(R.id.frame_container, fragment)
                .commit();

        boolean isMapFragment = fragment instanceof NearbyHospitalsFragment ||
                fragment instanceof PartnerHospitalsFragment;

        int visibility = isMapFragment ? View.GONE : View.VISIBLE;
        if (fab != null) fab.setVisibility(visibility);
        if (fabChatRoom != null) fabChatRoom.setVisibility(visibility);
    }

    private boolean checkGuestAccess() {
        if (isGuest) {
            Toast.makeText(this, "로그인이 필요합니다.", Toast.LENGTH_SHORT).show();
            return true;
        }
        return false;
    }

    private void navigateToLogin() {
        startActivity(new Intent(this, LoginActivity.class));
    }
}