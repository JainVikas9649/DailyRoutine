package app.dailyroutine;

import android.os.Bundle;
import android.os.Handler;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;

import androidx.appcompat.app.AppCompatActivity;
import androidx.cardview.widget.CardView;
import androidx.core.content.ContextCompat;
import androidx.viewpager2.widget.ViewPager2;

import java.util.Arrays;
import java.util.List;

public class SplashLogin extends AppCompatActivity {

    ViewPager2 viewPager;
    LinearLayout dotLayout;
    List<Integer> images;
    List<String> titles;
    SliderAdapter adapter;
    ImageView[] dots;

    private Handler sliderHandler = new Handler();
    private Runnable sliderRunnable;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_travel_slider);

        viewPager = findViewById(R.id.viewPager);
        dotLayout = findViewById(R.id.dotLayout1);

//        images = Arrays.asList(
//                R.drawable.images111,
//                R.drawable.image222,
//                R.drawable.image333
//        );
        CardView signUpCard = findViewById(R.id.signUpCard);
        signUpCard.setVisibility(View.VISIBLE); // or View.GONE
        titles = Arrays.asList(
                "Escape the Ordinary,\nEmbrace the Journey!",
                "Discover Hidden Gems\nAround the World",
                "Adventure Awaits,\nStart Your Journey"
        );

      //  adapter = new SliderAdapter(this, images, titles);
        viewPager.setAdapter(adapter);

      //  setupIndicators(images.size());
        selectDot(0);

        viewPager.registerOnPageChangeCallback(new ViewPager2.OnPageChangeCallback() {
            @Override
            public void onPageSelected(int position) {
                selectDot(position);
                resetAutoSlide(); // restart timer when user manually swipes
            }
        });

        startAutoSlide(); // start auto slide
    }

    private void setupIndicators(int count) {
        dots = new ImageView[count];
        dotLayout.removeAllViews();

        for (int i = 0; i < count; i++) {
            dots[i] = new ImageView(this);
            dots[i].setImageDrawable(ContextCompat.getDrawable(this, R.drawable.indicator_unselected));
            LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(
                    ViewGroup.LayoutParams.WRAP_CONTENT,
                    ViewGroup.LayoutParams.WRAP_CONTENT);
            params.setMargins(8, 0, 8, 0);
            dotLayout.addView(dots[i], params);
        }
    }

    private void selectDot(int index) {
        for (int i = 0; i < dots.length; i++) {
            dots[i].setImageDrawable(ContextCompat.getDrawable(this,
                    i == index ? R.drawable.indicator_selected : R.drawable.indicator_unselected));
        }
    }

    private void startAutoSlide() {
        sliderRunnable = new Runnable() {
            @Override
            public void run() {
                int currentItem = viewPager.getCurrentItem();
                int nextItem = currentItem + 1;

//                if (nextItem >= images.size()) {
//                    // Instantly jump to first item without animation, then animate forward next time
//                    viewPager.setCurrentItem(0, false);
//                    nextItem = 1;
//                    sliderHandler.postDelayed(this, 2000);
//                    return;
//                }

                viewPager.setCurrentItem(nextItem, true);
                sliderHandler.postDelayed(this, 2000);
            }
        };
        sliderHandler.postDelayed(sliderRunnable, 2000);
    }

    private void resetAutoSlide() {
        sliderHandler.removeCallbacks(sliderRunnable);
        sliderHandler.postDelayed(sliderRunnable, 2000);
    }

    @Override
    protected void onPause() {
        super.onPause();
        sliderHandler.removeCallbacks(sliderRunnable);
    }

    @Override
    protected void onResume() {
        super.onResume();
        startAutoSlide();
    }
}