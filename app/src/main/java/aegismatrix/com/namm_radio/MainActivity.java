package aegismatrix.com.namm_radio;

import android.content.res.ColorStateList;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.graphics.Point;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.ColorDrawable;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.LayerDrawable;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.renderscript.Allocation;
import android.renderscript.Element;
import android.renderscript.RenderScript;
import android.renderscript.ScriptIntrinsicBlur;
import android.support.annotation.NonNull;
import android.support.design.widget.AppBarLayout;
import android.support.design.widget.BottomSheetBehavior;
import android.support.design.widget.CoordinatorLayout;
import android.support.design.widget.NavigationView;
import android.support.design.widget.TabLayout;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.content.ContextCompat;
import android.support.v4.view.ViewPager;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.CardView;
import android.support.v7.widget.DividerItemDecoration;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Display;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ImageSwitcher;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.ViewSwitcher;

import com.google.android.exoplayer2.ExoPlayerFactory;
import com.google.android.exoplayer2.SimpleExoPlayer;
import com.google.android.exoplayer2.extractor.DefaultExtractorsFactory;
import com.google.android.exoplayer2.source.ExtractorMediaSource;
import com.google.android.exoplayer2.trackselection.DefaultTrackSelector;
import com.google.android.exoplayer2.trackselection.TrackSelector;
import com.google.android.exoplayer2.upstream.DefaultDataSourceFactory;
import com.google.android.exoplayer2.util.Util;
import com.nostra13.universalimageloader.cache.memory.impl.WeakMemoryCache;
import com.nostra13.universalimageloader.core.DisplayImageOptions;
import com.nostra13.universalimageloader.core.ImageLoader;
import com.nostra13.universalimageloader.core.ImageLoaderConfiguration;
import com.nostra13.universalimageloader.core.assist.ImageScaleType;
import com.nostra13.universalimageloader.core.display.FadeInBitmapDisplayer;
import com.wnafee.vector.MorphButton;
import com.wnafee.vector.compat.AnimatedVectorDrawable;
import com.wnafee.vector.compat.ResourcesCompat;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import aegismatrix.com.namm_radio_buttons.StationButton;

public class MainActivity extends AppCompatActivity {

    /**
     * The {@link android.support.v4.view.PagerAdapter} that will provide
     * fragments for each of the sections. We use a
     * {@link FragmentPagerAdapter} derivative, which will keep every
     * loaded fragment in memory. If this becomes too memory intensive, it
     * may be best to switch to a
     * {@link android.support.v4.app.FragmentStatePagerAdapter}.
     */
    private SectionsPagerAdapter mSectionsPagerAdapter;

    /**
     * The {@link ViewPager} that will host the section contents.
     */
    private ViewPager mViewPager;
    private RecyclerView recyclerView;
    private ProgramRVAdapter programRVAdapter;
    private AppBarLayout appBarLayout;
    private CoordinatorLayout mCoordinatorLayout;
    private ImageLoader imageLoader;
    private List programItemSchedulerModelList = null;
    public Bitmap mImages[] = null;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (savedInstanceState == null) {
            Bundle extras = getIntent().getExtras();
            setTitle(extras.getString("Title"));
        } else {
            setTitle((String) savedInstanceState.getSerializable("Title"));
        }
        setContentView(R.layout.activity_main);

        appBarLayout = (AppBarLayout) findViewById(R.id.appbar);

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        // Create the adapter that will return a fragment for each of the three
        // primary sections of the activity.
        mSectionsPagerAdapter = new SectionsPagerAdapter(getSupportFragmentManager());
        mCoordinatorLayout = (CoordinatorLayout) findViewById(R.id.main_content);


        // Set up the ViewPager with the sections adapter.
        mViewPager = (ViewPager) findViewById(R.id.container);
        mViewPager.setAdapter(mSectionsPagerAdapter);
        mViewPager.addOnPageChangeListener(new ViewPager.OnPageChangeListener() {
            @Override
            public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {

            }

            @Override
            public void onPageSelected(int position) {
                System.out.println();
            }

            @Override
            public void onPageScrollStateChanged(int state) {

            }
        });

        TabLayout tabLayout = (TabLayout) findViewById(R.id.tabs);
        tabLayout.setupWithViewPager(mViewPager);

        recyclerView = (RecyclerView) findViewById(R.id.programLists);
        programItemSchedulerModelList = getIntent().getParcelableArrayListExtra("PS");
        programRVAdapter = new ProgramRVAdapter(getApplicationContext(), programItemSchedulerModelList);
        recyclerView.setLayoutManager(new LinearLayoutManager(getApplicationContext()));
        recyclerView.setHasFixedSize(true);

        DividerItemDecoration dividerItemDecoration = new DividerItemDecoration(getApplicationContext(), LinearLayoutManager.VERTICAL);
        dividerItemDecoration.setDrawable(ContextCompat.getDrawable(getApplicationContext(), R.drawable.programlist_line_divider));
        recyclerView.addItemDecoration(new DividerItemDecoration(getApplicationContext(), 1));
        recyclerView.setAdapter(programRVAdapter);

        initBottomSheetProgramScheduler();

        initNavigationDrawer(toolbar);

        initDisplayOptionsImageLoader();
        blurOutBackgrouund();
    }

    private void initBottomSheetProgramScheduler() {
        BottomSheetBehavior bottomSheetBehavior = BottomSheetBehavior.from(findViewById(R.id.bottom_sheet_layout));
        bottomSheetBehavior.setPeekHeight(128);
        bottomSheetBehavior.setState(BottomSheetBehavior.STATE_COLLAPSED);
        bottomSheetBehavior.setBottomSheetCallback(new BottomSheetBehavior.BottomSheetCallback() {
            @Override
            public void onStateChanged(@NonNull View bottomSheet, int newState) {
                System.out.println("hi");
            }

            @Override
            public void onSlide(@NonNull View bottomSheet, float slideOffset) {

                System.out.println("hiiii");
                appBarLayout.setAlpha(1 - slideOffset);
            }
        });

    }

    private void initDisplayOptionsImageLoader() {
        DisplayImageOptions defaultOptions = new DisplayImageOptions.Builder()
                .cacheOnDisk(true).cacheInMemory(true)
                .imageScaleType(ImageScaleType.EXACTLY)
                .displayer(new FadeInBitmapDisplayer(300)).build();
        ImageLoaderConfiguration config = new ImageLoaderConfiguration.Builder(
                getApplicationContext())
                .defaultDisplayImageOptions(defaultOptions)
                .memoryCache(new WeakMemoryCache())
                .diskCacheSize(100 * 1024 * 1024).build();
        imageLoader = ImageLoader.getInstance();
        imageLoader.init(config);
        String jsonArray = getIntent().getStringExtra("jsonData");
        try {
            JSONArray array = new JSONArray(jsonArray);
            mImages = new Bitmap[array.length()];
            for (int jsonIndex = 0; jsonIndex < mImages.length; jsonIndex++) {
                JSONObject jsonData = ((JSONObject) array.opt(jsonIndex));
                mImages[jsonIndex] = getImageToShow((String) jsonData.opt(GlobalConstants.PROGRAM_IMAGE_TAG));
            }
        } catch (JSONException e) {
            Log.d("Problems with parsing the array from intent", e.getMessage());
        }
    }

    public Bitmap getImageToShow(final String imageURL) {
        Bitmap imageToShow = null;
        try {
            imageToShow = new AsyncTask<String, Void, Bitmap>() {
                @Override
                protected Bitmap doInBackground(String... params) {
                    DisplayImageOptions options = new DisplayImageOptions.Builder().cacheInMemory(true)
                            .cacheOnDisk(true).resetViewBeforeLoading(true)
/*                           .showImageOnFail(fallback)
                            .showImageOnLoading(fallback)*/.build();
                    return imageLoader.loadImageSync(imageURL, options);
                }
            }.execute(imageURL).get();
        } catch (Exception e) {
            Log.e("Image Not Available", e.getMessage());
        }
        return imageToShow;
    }

    private void initNavigationDrawer(Toolbar inToolbar) {
        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawerLayout);
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
                this, drawer, inToolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        drawer.addDrawerListener(new ActionBarDrawerToggle(this, drawer, inToolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close));
        toggle.syncState();

        NavigationView navigationView = (NavigationView) findViewById(R.id.nav_view);
        navigationView.setNavigationItemSelectedListener(new NavigationView.OnNavigationItemSelectedListener() {
            @Override
            public boolean onNavigationItemSelected(@NonNull MenuItem item) {
                return false;
            }
        });
    }

    private void blurOutBackgrouund() {
        Display display = getWindowManager().getDefaultDisplay();
        Point size = new Point();
        display.getSize(size);
        int width = size.x;
        int height = size.y;

        Bitmap bitmap = Bitmap.createScaledBitmap(getImageToShow(GlobalConstants.APP_BACKGROUND_IMAGE), width, height, true);
        //   Bitmap blurredBitmap = blur(bitmap);
        mCoordinatorLayout.setBackground(new BitmapDrawable(getResources(), bitmap));
    }

    public void changeStations(final View view) {
        Button button = (Button) view;
        String text = getTitle() + button.getText().toString();
        button.setText(text.substring(0, getTitle().length()));
        setTitle(text.substring(getTitle().length()));
//        StationsFragment stationsFragment = (StationsFragment) getSupportFragmentManager().getFragments().get(0);
//        stationsFragment.stopPlayer();
//        stationsFragment.imageView.setState(MorphButton.MorphState.END, false);
//        stationsFragment.imageView.performClick();
//        stationsFragment.imageView.invalidate();
        //stationsFragment.changeMediaButtonState(stationsFragment.imageButton, stationsFragment.imageView.getState(), stationsFragment.getStationLinkFromTitle());
    }


    private Bitmap blur(Bitmap inBitmap) {
        //Set the radius of the Blur. Supported range 0 < radius <= 25
        final float BLUR_RADIUS = 25f;

        if (null == inBitmap) return null;

        Bitmap outputBitmap = Bitmap.createBitmap(inBitmap);
        final RenderScript renderScript = RenderScript.create(this);
        Allocation tmpIn = Allocation.createFromBitmap(renderScript, inBitmap);
        Allocation tmpOut = Allocation.createFromBitmap(renderScript, outputBitmap);

        //Intrinsic Gausian blur filter
        ScriptIntrinsicBlur theIntrinsic = ScriptIntrinsicBlur.create(renderScript, Element.U8_4(renderScript));
        theIntrinsic.setRadius(BLUR_RADIUS);
        theIntrinsic.setInput(tmpIn);
        theIntrinsic.forEach(tmpOut);
        tmpOut.copyTo(outputBitmap);
        return outputBitmap;
    }


    @Override
    public void onBackPressed() {
        super.onBackPressed();
    }

    /**
     * A placeholder fragment containing a simple view.
     */
    public static class StationsFragment extends Fragment {

        private int index = 0;
        private final int FADE_IN = 3000, FADE_OUT = 2000, INTERVAL = 3000;
        private ImageSwitcher showsFlipper;
        private boolean isRunning = true;
        private SimpleExoPlayer mExoPlayer;
        private ProgressBar mProgressBar = null;
        public Bitmap imagesToShow[] = null;
        public ImageButton imageButton = null;
        public MorphButton imageView = null;
        /**
         * The fragment argument representing the section number for this
         * fragment.
         */
        private static final String ARG_SECTION_NUMBER = "station_country";

        public StationsFragment() {
        }

        /**
         * Returns a new instance of this fragment for the given section
         * number.
         */
        public static StationsFragment newInstance(int sectionNumber) {
            StationsFragment fragment = new StationsFragment();
            Bundle args = new Bundle();
            args.putInt(ARG_SECTION_NUMBER, sectionNumber);
            fragment.setArguments(args);
            return fragment;
        }

        @Override
        public View onCreateView(LayoutInflater inflater, ViewGroup container,
                                 Bundle savedInstanceState) {
            View rootView = inflater.inflate(R.layout.fragment_main, container, false);
            int fragmentTabInteger = getArguments().getInt(ARG_SECTION_NUMBER);
            switch (fragmentTabInteger) {
                case 1:
                    createNowPlayingFragement(rootView, inflater);
                    break;
                case 2:
                    //for events add here
                    break;
            }
            return rootView;
        }


        private void createNowPlayingFragement(View rootView, LayoutInflater inflater) {

            imagesToShow = ((MainActivity) getActivity()).mImages;
            showsFlipper = (ImageSwitcher) rootView.findViewById(R.id.programflpper);
            Animation aniIn = AnimationUtils.loadAnimation(getContext(),
                    android.R.anim.fade_in);
            aniIn.setDuration(FADE_IN);
            Animation aniOut = AnimationUtils.loadAnimation(getContext(),
                    android.R.anim.fade_out);
            aniOut.setDuration(FADE_OUT);
            showsFlipper.setInAnimation(aniIn);
            showsFlipper.setOutAnimation(aniOut);
            showsFlipper.setFactory(new ViewSwitcher.ViewFactory() {
                @Override
                public View makeView() {
                    ImageView imageView = new ImageView(getContext());
                    imageView.setAdjustViewBounds(true);
                    imageView.setLayoutParams(new ImageSwitcher.LayoutParams(
                            RelativeLayout.LayoutParams.WRAP_CONTENT, RelativeLayout.LayoutParams.WRAP_CONTENT));
                    return imageView;
                }
            });
            showsFlipper.setImageDrawable(new BitmapDrawable(getResources(), imagesToShow[index]));
            // displayMultipleImages();

            drawStationButtons();
            loadStationButtons(rootView);

            //TODO
            mProgressBar = (ProgressBar) rootView.findViewById(R.id.progressBar);

            //  imageButton.setImageDrawable(getGradientLayerWithoutBuffering());
            imageView = (MorphButton) rootView.findViewById(R.id.playbut);
            changeMediaButtonState(imageButton, imageView.getState(), getStationLinkFromTitle());
            imageView.setForegroundTintList(ColorStateList.valueOf(Color.WHITE));
            imageView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View inImageView) {
                    MorphButton morphButton = (MorphButton) inImageView;
                    morphButton.setClickable(false);
                    morphButton.setForegroundTintList(ColorStateList.valueOf(Color.WHITE));
                    AnimatedVectorDrawable drawable = (AnimatedVectorDrawable) ResourcesCompat.getDrawable(getContext(), R.drawable.ic_pause_to_play);
                    drawable.start();
                    changeMediaButtonState(imageButton, morphButton.getState(), getStationLinkFromTitle());
                    morphButton.setClickable(true);
                }
            });
        }

        private void drawStationButtons() {

        }

        protected String getStationLinkFromTitle() {
            if (!GlobalConstants.STATIONLINKS.isEmpty()) {
                return GlobalConstants.STATIONLINKS.get(getActivity().getTitle());
            }
            return null;
        }

        private void loadStationButtons(View rootView) {
            RelativeLayout buttonsLayout = (RelativeLayout) rootView.findViewById(R.id.stationButtons);
            String country = getActivity().getTitle().toString();
            ArrayList<String> stations = new ArrayList(Arrays.asList(GlobalConstants.STATIONS));
            stations.remove(country);
            for (int station = 0; station < stations.size(); station++) {
                if (buttonsLayout.getChildAt(station) instanceof CardView) {
                    CardView theCardView = (CardView) buttonsLayout.getChildAt(station);
                    theCardView.setBackground(new ColorDrawable(Color.TRANSPARENT));
                    theCardView.setRadius(10);
                    theCardView.setCardElevation(10);
                    theCardView.setMaxCardElevation(10);
                    StationButton stationButton = (StationButton) theCardView.getChildAt(0);
                    stationButton.setText(stations.get(station));
                    //stationButton.setCompoundDrawablesWithIntrinsicBounds(0, 0, R.drawable.india, 0);
                }
            }
        }


        private void changeMediaButtonState(ImageButton inImageButton, MorphButton.MorphState morphState, String inLink) {
            if (morphState == MorphButton.MorphState.START) {
                // inImageButton.setImageResource(R.drawable.play_button_buffer);
                Animation rotation = AnimationUtils.loadAnimation(getContext(), R.anim.rotate_buffering);
                rotation.setRepeatCount(Animation.INFINITE);
                //  inImageButton.startAnimation(rotation);
                SimpleExoPlayer player = playMedia(inLink);
                if (player != null) {
                    setExoPlayer(player);
                    //  inImageButton.setImageDrawable(getGradientLayerWithoutBuffering());
                }
            } else if (morphState == MorphButton.MorphState.END) {
                stopPlayer();
                //inImageButton.setImageDrawable(getGradientLayerWithoutBuffering());
            }
        }

        private void stopPlayer() {
            if (getExoPlayer() != null) {
                getExoPlayer().stop();
                getExoPlayer().setPlayWhenReady(false);
                getExoPlayer().release();
            }
        }

        private SimpleExoPlayer playMedia(String link) {
            Handler handler = new Handler();
            TrackSelector trackSelector = new DefaultTrackSelector();
            String userAgent = Util.getUserAgent(getContext(), "NammRadio");

            ExtractorMediaSource sampleSource = new ExtractorMediaSource(Uri.parse(link), new DefaultDataSourceFactory(getContext(), userAgent), new DefaultExtractorsFactory(), ExtractorMediaSource.DEFAULT_MIN_LOADABLE_RETRY_COUNT_LIVE, handler, null, null);

            SimpleExoPlayer nammRadioPlayer = ExoPlayerFactory.newSimpleInstance(getContext(), trackSelector);
            nammRadioPlayer.prepare(sampleSource);
            nammRadioPlayer.setPlayWhenReady(true);
            return nammRadioPlayer;
        }

        private void displayMultipleImages() {
            final Handler handler = new Handler();
            Runnable runnable = new Runnable() {

                @Override
                public void run() {
                    if (isRunning) {
//                        index++;
//                        index = index % imagesToShow.length;
//                        showsFlipper.setImageDrawable(new BitmapDrawable(getResources(), imagesToShow[index]));
//                        handler.postDelayed(this, INTERVAL);
                    }
                }
            };
            handler.postDelayed(runnable, INTERVAL);
        }

        public Drawable getGradientLayerWithoutBuffering() {
            LayerDrawable layerDrawable = (LayerDrawable) getResources().getDrawable(R.drawable.play_button_buffer);
            //GradientDrawable gradientDrawable = (GradientDrawable) layerDrawable.findDrawableByLayerId(R.id.playButtonProgress);
            return null;
        }

        public SimpleExoPlayer getExoPlayer() {
            return mExoPlayer;
        }

        public void setExoPlayer(SimpleExoPlayer mExoPlayer) {
            this.mExoPlayer = mExoPlayer;
        }

    }


    /**
     * A {@link FragmentPagerAdapter} that returns a fragment corresponding to
     * one of the sections/tabs/pages.
     */
    public class SectionsPagerAdapter extends FragmentPagerAdapter {

        public SectionsPagerAdapter(FragmentManager fm) {
            super(fm);
        }

        @Override
        public Fragment getItem(int position) {
            // getItem is called to instantiate the fragment for the given page.
            // Return a PlaceholderFragment (defined as a static inner class below).
            return StationsFragment.newInstance(position + 1);
        }

        @Override
        public int getCount() {
            // Show 3 total pages.
            return 2;
        }


        @Override
        public CharSequence getPageTitle(int position) {
            switch (position) {
                case 0:
                    return "NOW PLAYING";
                case 1:
                    return "EVENTS";
            }
            return null;
        }

    }
}
