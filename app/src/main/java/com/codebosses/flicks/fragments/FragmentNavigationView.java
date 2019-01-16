package com.codebosses.flicks.fragments;


import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.DefaultItemAnimator;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import butterknife.BindString;
import butterknife.BindView;
import butterknife.ButterKnife;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.codebosses.flicks.R;
import com.codebosses.flicks.adapters.exapndablerecyclerviewadapter.CategoryAdapter;
import com.codebosses.flicks.endpoints.EndpointKeys;
import com.codebosses.flicks.pojo.eventbus.EventBusExpandItems;
import com.codebosses.flicks.pojo.eventbus.EventBusSelectedItem;
import com.codebosses.flicks.pojo.expandrecyclerviewpojo.CategoryItem;
import com.codebosses.flicks.pojo.expandrecyclerviewpojo.CategoryHeader;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

import java.util.Arrays;
import java.util.List;

/**
 * A simple {@link Fragment} subclass.
 */
public class FragmentNavigationView extends Fragment {

    @BindView(R.id.recyclerViewNavigation)
    RecyclerView recyclerViewNavigation;

    //    Adapter fields....
    CategoryAdapter adapter;

    //    Binding resources....
    @BindString(R.string.movies)
    String movies;
    @BindString(R.string.tv_shows)
    String tvShows;
    @BindString(R.string.celebrities)
    String celebrities;
    @BindString(R.string.genre)
    String genre;

    public FragmentNavigationView() {
        // Required empty public constructor
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        View view = inflater.inflate(R.layout.fragment_navigation_view, container, false);
        ButterKnife.bind(this, view);

        LinearLayoutManager layoutManager = new LinearLayoutManager(getActivity());

//        Disabling default animation of recyclerview....
        RecyclerView.ItemAnimator animator = recyclerViewNavigation.getItemAnimator();
        if (animator instanceof DefaultItemAnimator) {
            ((DefaultItemAnimator) animator).setSupportsChangeAnimations(false);
        }
        adapter = new CategoryAdapter(makeCategories());
        recyclerViewNavigation.setLayoutManager(layoutManager);
        recyclerViewNavigation.setAdapter(adapter);

        if (savedInstanceState != null) {
            adapter.onRestoreInstanceState(savedInstanceState);
        }

        EventBus.getDefault().register(this);

        return view;
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        EventBus.getDefault().unregister(this);
    }

    @Override
    public void onSaveInstanceState(@NonNull Bundle outState) {
        super.onSaveInstanceState(outState);
        adapter.onSaveInstanceState(outState);
    }

    private List<CategoryHeader> makeCategories() {
        return Arrays.asList(
                makeMovieHeader(),
                makeTvHeader(),
                makeCelebriyHeader(),
                makeGenreHeader());
//                makeBluegrassGenre());
    }

    private CategoryHeader makeMovieHeader() {
        return new CategoryHeader(movies, makeMovieItems(), R.drawable.ic_electric_guitar);
    }

    private List<CategoryItem> makeMovieItems() {
        CategoryItem upcomingMovies = new CategoryItem(getResources().getString(R.string.upcoming_movies), false);
        CategoryItem topRatedMovies = new CategoryItem(getResources().getString(R.string.top_rated_movies), false);
        CategoryItem latestMovies = new CategoryItem(getResources().getString(R.string.latest_movies), false);
        CategoryItem inTheater = new CategoryItem(getResources().getString(R.string.in_theater), false);

        return Arrays.asList(upcomingMovies, topRatedMovies, latestMovies, inTheater);
    }

    private CategoryHeader makeTvHeader() {
        return new CategoryHeader(tvShows, makeTvItems(), R.drawable.ic_saxaphone);
    }


    private List<CategoryItem> makeTvItems() {
        CategoryItem topRatedTvShows = new CategoryItem(getResources().getString(R.string.top_rated_tv_shows), false);
        CategoryItem latestTvShows = new CategoryItem(getResources().getString(R.string.latest_tv_shows), false);

        return Arrays.asList(topRatedTvShows, latestTvShows);
    }

    private CategoryHeader makeCelebriyHeader() {
        return new CategoryHeader(celebrities, makeCelebrityItems(), R.drawable.ic_violin);
    }

    private List<CategoryItem> makeCelebrityItems() {
        CategoryItem topRatedCelebrity = new CategoryItem(getResources().getString(R.string.top_rated_celebrities), false);

        return Arrays.asList(topRatedCelebrity);
    }

    private CategoryHeader makeGenreHeader() {
        return new CategoryHeader(genre, makeGenreItems(), R.drawable.ic_maracas);
    }


    private List<CategoryItem> makeGenreItems() {
        CategoryItem action = new CategoryItem(getResources().getString(R.string.action), false);
        CategoryItem animated = new CategoryItem(getResources().getString(R.string.animated), false);
        CategoryItem drama = new CategoryItem(getResources().getString(R.string.drama), false);
        CategoryItem science_fiction = new CategoryItem(getResources().getString(R.string.sceince_fiction), false);

        return Arrays.asList(action, animated, drama, science_fiction);
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void eventBusExpandItem(EventBusExpandItems eventBusExpandItems) {
        EventBus.getDefault().post(new EventBusSelectedItem(eventBusExpandItems.getTitle()));
    }

}
