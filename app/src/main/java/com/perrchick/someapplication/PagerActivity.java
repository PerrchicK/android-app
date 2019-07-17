package com.perrchick.someapplication;

import android.net.Uri;
import android.os.Bundle;
import android.util.Log;

import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentStatePagerAdapter;
import androidx.viewpager.widget.ViewPager;

import com.perrchick.someapplication.ui.fragments.PageFragment;

public class PagerActivity extends AppCompatActivity implements PageFragment.OnFragmentInteractionListener {

    //import android.support.v4.app.Fragment;
    //import android.support.v4.app.FragmentManager;
    //import android.support.v4.app.FragmentStatePagerAdapter;
    //import android.support.v4.view.ViewPager;
    //import android.support.v7.app.AppCompatActivity;

    private static final String TAG = PagerActivity.class.getSimpleName();

    private ViewPager viewPager;
    private SomePagerAdapter viewPagerAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_pager);

        // Instantiate a ViewPager and a PagerAdapter.
        viewPager = (ViewPager) findViewById(R.id.view_pager);
        viewPagerAdapter = new SomePagerAdapter(getSupportFragmentManager());
        viewPager.setAdapter(viewPagerAdapter);
    }

    @Override
    public void onFragmentInteraction(Uri uri) {
        Log.d(TAG, "onFragmentInteraction: " + uri);
    }

    private class SomePagerAdapter extends FragmentStatePagerAdapter {
        public SomePagerAdapter(FragmentManager fm) {
            super(fm);
        }

        @Override
        public Fragment getItem(int position) {
            return PageFragment.newInstance("page number " + position);
        }

        @Override
        public int getCount() {
            return 3;
        }
    }
}
