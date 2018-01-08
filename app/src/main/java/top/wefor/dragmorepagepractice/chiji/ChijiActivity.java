package top.wefor.dragmorepagepractice.chiji;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.design.widget.CoordinatorLayout;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.RecyclerView;
import android.view.WindowManager;
import android.widget.FrameLayout;
import android.widget.LinearLayout;
import android.widget.TextView;

import java.util.ArrayList;

import top.wefor.dragmorepagepractice.DragLoadMoreBehavior;
import top.wefor.dragmorepagepractice.R;

/**
 * Created on 2018/1/3.
 *
 * @author ice
 */

public class ChijiActivity extends AppCompatActivity {

    private DragLoadMoreBehavior mDragLoadMoreBehavior;
    private LinearLayout mLinearLayout;

    public static Intent newIntent(Context context) {
        Intent intent = new Intent(context, ChijiActivity.class);
        return intent;
    }

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
        setContentView(R.layout.activity_chiji);
        setContainer();
//        setList();
//        setViewPager();

//        MyBeiSaiErView myBeiSaiErView = (MyBeiSaiErView) findViewById(R.id.myBeiSaiErView);
//        myBeiSaiErView.start(0);

        mLinearLayout = findViewById(R.id.bottom_layout);
        CoordinatorLayout.LayoutParams params = (CoordinatorLayout.LayoutParams) mLinearLayout.getLayoutParams();
        mDragLoadMoreBehavior = (DragLoadMoreBehavior) params.getBehavior();

        mTextView1 = findViewById(R.id.tv1);
        mTextView2 = findViewById(R.id.tv2);

        mTextView1.setOnClickListener(v -> changeFragment(1));
        mTextView2.setOnClickListener(v -> changeFragment(2));
    }

    TextView mTextView1, mTextView2;

    private int mCurentPos = 1;

    private void changeFragment(int pos) {
        if (pos == mCurentPos) {
            return;
        }

        mCurentPos = pos;
        FragmentManager manager = getSupportFragmentManager();
        FragmentTransaction transaction = manager.beginTransaction();
        if (mCurentPos == 1) {
            transaction.remove(mListFragment2);
            transaction.add(R.id.fragment_container, mListFragment1);
        } else {
            transaction.remove(mListFragment1);
            transaction.add(R.id.fragment_container, mListFragment2);
        }
        transaction.commit();
        mDragLoadMoreBehavior.reSetChildScrollView(0);
    }

    private static final String TAG = "xyz list";
    private static final String KEY_POS = "pos";

    private RecyclerView mRecyclerView;
    private FrameLayout mFrameLayout;
    private ListFragment mListFragment1, mListFragment2;

    public void setContainer() {
        mFrameLayout = findViewById(R.id.fragment_container);
        FragmentManager manager = getSupportFragmentManager();
        FragmentTransaction transaction = manager.beginTransaction();
        transaction.add(R.id.fragment_container, mListFragment1 = ListFragment.newInstance("1"));
        mListFragment2 = ListFragment.newInstance("2");
        transaction.commit();
    }

    private ViewPager mViewPager;
    private FragmentPagerAdapter mFragmentPagerAdapter;
    private ArrayList<ListFragment> mListFragments = new ArrayList<>();
    private ArrayList<String> mTitles = new ArrayList<>();

    public void setViewPager() {
//        mViewPager = findViewById(R.id.viewPager);

        mFragmentPagerAdapter = new FragmentPagerAdapter(getSupportFragmentManager()) {
            @Override
            public Fragment getItem(int position) {
                return mListFragments.get(position);
            }

            @Override
            public int getCount() {
                return mTitles.size();
            }

            @Override
            public CharSequence getPageTitle(int position) {
                return mTitles.get(position);
            }
        };

        mViewPager.setAdapter(mFragmentPagerAdapter);

        showList();
    }

    private void showList() {
        mListFragments.clear();
        mTitles.clear();
        for (int i = 0; i < 2; i++) {
            ListFragment listFragment = ListFragment.newInstance(i + "");
            mListFragments.add(listFragment);
            mTitles.add("ice" + i);
        }
        mFragmentPagerAdapter.notifyDataSetChanged();
        new Thread(new Runnable() {
            @Override
            public void run() {

                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {

                    }
                });
            }
        }).start();
    }

}
