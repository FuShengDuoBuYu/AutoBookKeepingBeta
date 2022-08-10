package com.beta.autobookkeeping.fragment.orderDetail;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentActivity;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentPagerAdapter;
import androidx.lifecycle.Lifecycle;
import androidx.viewpager2.adapter.FragmentStateAdapter;

import java.util.LinkedList;
import java.util.List;

public class TabOrderDetailFragmentPagerAdapter extends FragmentStateAdapter {
    List<Fragment> fragments = new LinkedList<>();
    public TabOrderDetailFragmentPagerAdapter(@NonNull FragmentActivity fragmentActivity) {
        super(fragmentActivity);
    }

    public TabOrderDetailFragmentPagerAdapter(@NonNull Fragment fragment) {
        super(fragment);
    }

    public TabOrderDetailFragmentPagerAdapter(@NonNull FragmentManager fragmentManager, @NonNull Lifecycle lifecycle,List<Fragment> fragments) {
        super(fragmentManager, lifecycle);
        this.fragments = fragments;
    }

    @NonNull
    @Override
    public Fragment createFragment(int position) {
        return fragments.get(position);
    }

    @Override
    public int getItemCount() {
        return fragments.size();
    }
}
