package top.wefor.dragmorepagepractice.chiji;

import android.content.Context;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.design.widget.CoordinatorLayout;
import android.support.v4.app.Fragment;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import java.util.ArrayList;

import top.wefor.dragmorepagepractice.DragLoadMoreBehavior;
import top.wefor.dragmorepagepractice.R;

/**
 * Created on 2017/12/25.
 *
 * @author ice
 */

public class ListFragment extends Fragment {
    private static final String TAG = "xyz list";
    private static final String KEY_POS = "pos";

    private RecyclerView mRecyclerView;
    private TextView mPosTv;
    private String mPos;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
    }

    public static ListFragment newInstance(String pos) {
        Bundle bundle = new Bundle();
        bundle.putString(KEY_POS, pos);
        ListFragment listFragment = new ListFragment();
        listFragment.setArguments(bundle);
        return listFragment;
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_list, container, false);
        mRecyclerView = view.findViewById(R.id.recyclerView);
        view.findViewById(R.id.btn_back).setOnClickListener(v -> {
            final View bottomLayout = getActivity().findViewById(R.id.bottom_layout);
            if (bottomLayout != null && bottomLayout.getLayoutParams() instanceof CoordinatorLayout.LayoutParams) {
                final CoordinatorLayout.LayoutParams layoutParams = (CoordinatorLayout.LayoutParams) bottomLayout.getLayoutParams();
                if (layoutParams.getBehavior() instanceof DragLoadMoreBehavior) {
                    final DragLoadMoreBehavior behavior = (DragLoadMoreBehavior) layoutParams.getBehavior();
                    behavior.showMorePage(false);
                }
            }
        });
        mPosTv = view.findViewById(R.id.pos_tv);
        mPos = getArguments().getString(KEY_POS);
        Log.i(TAG, "onCreateView");
        return view;
//        return super.onCreateView(inflater, container, savedInstanceState);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        mMyAdapter = new MyAdapter();
        mRecyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        mRecyclerView.setHasFixedSize(true);
        mRecyclerView.setAdapter(mMyAdapter);
        mPosTv.setText(mPos);
        setData();
        Log.i(TAG, "onViewCreated");
    }

    private void setData() {
        mMyAdapter.mTitles.clear();
        for (int i = 0; i < 25; i++) {
            String item = "Winter is coming " + i;
            mMyAdapter.mTitles.add(item);
        }
        mMyAdapter.notifyDataSetChanged();
    }

    @Override
    public void onAttachFragment(Fragment childFragment) {
        super.onAttachFragment(childFragment);
        Log.i(TAG, "onAttachFragment");
    }

    @Override
    public void onStart() {
        super.onStart();
        Log.i(TAG, "onStart");
    }

    @Override
    public void onResume() {
        super.onResume();
        Log.i(TAG, "onResume");
    }

    @Override
    public void onPause() {
        super.onPause();
        Log.i(TAG, "onPause");
    }

    @Override
    public void onStop() {
        super.onStop();
        Log.i(TAG, "onStop");
    }

    @Override
    public void onDetach() {
        super.onDetach();
        Log.i(TAG, "onDetach");
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        Log.i(TAG, "onDestroyView");
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        Log.i(TAG, "onDestroy");
    }

    private MyAdapter mMyAdapter;

    private class MyAdapter extends RecyclerView.Adapter<MyHolder> {
        ArrayList<String> mTitles = new ArrayList<>();

        @Override
        public MyHolder onCreateViewHolder(ViewGroup parent, int viewType) {
            View view = getLayoutInflater().inflate(R.layout.item_list, parent, false);
            return new MyHolder(view);
        }

        @Override
        public void onBindViewHolder(MyHolder holder, int position) {
            holder.titleTv.setText(mTitles.get(position));
        }

        @Override
        public int getItemCount() {
            return mTitles.size();
        }
    }

    private static class MyHolder extends RecyclerView.ViewHolder {

        TextView titleTv;
        ImageView picIv;

        MyHolder(View itemView) {
            super(itemView);
            titleTv = itemView.findViewById(R.id.title_tv);
            picIv = itemView.findViewById(R.id.pic_iv);
        }
    }

}
