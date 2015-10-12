/*
 * Copyright 2014 Nathan VanBenschoten
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.github.shareme.gwsparallelmotion;

import android.app.Fragment;
import android.content.pm.ActivityInfo;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.TypedValue;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CompoundButton;
import android.widget.SeekBar;
import android.widget.Switch;

import com.github.shareme.gwsparallelmotion.library.ParallaxImageView;


public class ParallaxActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_parallax);

        if (savedInstanceState == null) {
            getFragmentManager().beginTransaction()
                    .add(R.id.container, new ParallaxFragment())
                    .commit();
        }
    }

    /**
     * A fragment containing a simple parallax image view and a SeekBar to adjust the
     * parallax intensity.
     */
    public static class ParallaxFragment extends Fragment {

        private ParallaxImageView mBackground;
        private SeekBar mSeekBar;

        private int mCurrentImage;
        private boolean mParallaxSet = true;
        private boolean mPortraitLock = true;

        public ParallaxFragment() { }

        @Override
        public void onCreate(Bundle savedInstanceState) {
            super.onCreate(savedInstanceState);
            setRetainInstance(true);
            setHasOptionsMenu(true);
        }

        @Override
        public View onCreateView(LayoutInflater inflater, ViewGroup container,
                Bundle savedInstanceState) {
            View rootView = inflater.inflate(R.layout.fragment_parallax, container, false);
            if (rootView == null) return null;

            mBackground = (ParallaxImageView) rootView.findViewById(android.R.id.background);
            mSeekBar = (SeekBar) rootView.findViewById(android.R.id.progress);

            setCurrentImage();

            return rootView;
        }

        @Override
        public void onViewCreated(View view, Bundle savedInstanceState) {
            super.onViewCreated(view, savedInstanceState);

            // Set SeekBar to change parallax intensity
            mSeekBar.setMax(10);
            mSeekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
                @Override
                public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                    mBackground.setParallaxIntensity(1f + ((float) progress) / 40);
                }

                @Override
                public void onStartTrackingTouch(SeekBar seekBar) { }

                @Override
                public void onStopTrackingTouch(SeekBar seekBar) { }
            });
            mSeekBar.setProgress(2);
        }

        @Override
        public void onResume() {
            super.onResume();

            if (mParallaxSet) {
                mBackground.registerSensorManager();
            }
        }

        @Override
        public void onPause() {
            mBackground.unregisterSensorManager();
            super.onPause();
        }

        @Override
        public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
            super.onCreateOptionsMenu(menu, inflater);
            inflater.inflate(R.menu.parallax, menu);

            // Add parallax toggle
            final Switch mParallaxToggle = new Switch(getActivity());
            mParallaxToggle.setPadding(0, 0, (int)TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 12, getResources().getDisplayMetrics()), 0);
            mParallaxToggle.setChecked(mParallaxSet);
            mParallaxToggle.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
                @Override
                public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                    if (isChecked) {
                        mBackground.registerSensorManager();
                    } else {
                        mBackground.unregisterSensorManager();
                    }

                    mParallaxSet = isChecked;
                }
            });
            MenuItem switchItem = menu.findItem(R.id.action_parallax);
            if (switchItem != null)
                switchItem.setActionView(mParallaxToggle);

            // Set lock/ unlock orientation text
            if (mPortraitLock) {
                getActivity().setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
                MenuItem orientationItem = menu.findItem(R.id.action_portrait);
                if (orientationItem != null)
                    orientationItem.setTitle(R.string.action_unlock_portrait);
            }
        }

        @Override
        public boolean onOptionsItemSelected(MenuItem item) {
            switch (item.getItemId()) {
                case R.id.action_switch:
                    mCurrentImage ++;
                    mCurrentImage %= 3;
                    setCurrentImage();
                    return true;

                case R.id.action_portrait:
                    if (mPortraitLock) {
                        getActivity().setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_SENSOR);
                        item.setTitle(getString(R.string.action_lock_portrait));
                    } else {
                        getActivity().setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
                        item.setTitle(getString(R.string.action_unlock_portrait));
                    }

                    mPortraitLock = !mPortraitLock;
                    return true;

                default:
                    return super.onOptionsItemSelected(item);
            }
        }

        private void setCurrentImage() {
            if (mCurrentImage == 0) {
                mBackground.setImageResource(R.mipmap.background_pond);
            } else if (mCurrentImage == 1) {
                mBackground.setImageDrawable(getResources().getDrawable(R.mipmap.background_city));
            } else {
                mBackground.setImageBitmap(BitmapFactory.decodeResource(getResources(), R.mipmap.background_rocket_small));
            }
        }

    }

}
