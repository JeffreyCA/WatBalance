package com.cg.watbalance.preferences;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Build;
import android.os.Bundle;
import android.preference.Preference;
import android.preference.PreferenceFragment;
import android.preference.PreferenceManager;
import android.support.v7.widget.Toolbar;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.LinearLayout;

import com.cg.watbalance.R;
import com.cg.watbalance.login;

public class Preferences extends Activity {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        MyPreferenceFragment myPrefFrag = new MyPreferenceFragment();
        // myPrefFrag.setContext(getBaseContext());
        // getFragmentManager().beginTransaction().replace(android.R.id.content, myPrefFrag).commit();
        getFragmentManager().beginTransaction()
                .replace(android.R.id.content, new MyPreferenceFragment())
                .commit();
    }

    @Override
    protected void onPostCreate(Bundle savedInstanceState) {
        super.onPostCreate(savedInstanceState);

        LinearLayout root;

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            root = (LinearLayout) findViewById(android.R.id.list).getParent().getParent();
        }
        else {
            root = (LinearLayout) findViewById(android.R.id.list).getParent();
        }

        Toolbar bar = (Toolbar) LayoutInflater.from(this).inflate(R.layout.settings_toolbar, root,
                false);
        bar.setTitle("Settings");
        root.addView(bar, 0); // insert at top
        bar.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });
    }

    public static class MyPreferenceFragment extends PreferenceFragment {
        Context myContext;

        @Override
        public void onCreate(final Bundle savedInstanceState) {
            super.onCreate(savedInstanceState);
            addPreferencesFromResource(R.xml.preferences);

            Preference myPref = findPreference("logout");
            myPref.setOnPreferenceClickListener(new Preference.OnPreferenceClickListener() {
                public boolean onPreferenceClick(Preference pref) {
                    SharedPreferences myPreferences = PreferenceManager.getDefaultSharedPreferences(myContext);
                    SharedPreferences.Editor myEditor = myPreferences.edit();
                    myEditor.remove("login");
                    myEditor.apply();
                    Intent myIntent = new Intent(myContext, login.class);
                    startActivity(myIntent);
                    return true;
                }
            });
        }

        public void setContext(Context newContext) {
            myContext = newContext;
        }
    }
}
